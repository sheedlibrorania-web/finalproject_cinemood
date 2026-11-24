package com.example.cinemood.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

data class MovieRecord(val watchlistId: Long, val userId: Long, val tmdbMovieId: Long,
    val movieTitle: String, val posterPath: String?, val overview: String?, val status: String,
    val year: Int = 0, val genre: String = "", val source: String = "TMDB") {
    fun asMovie() = com.example.cinemood.Movie(tmdbMovieId.toInt(), movieTitle, year, genre, overview.orEmpty(), posterPath, source)
}
data class ReviewRecord(val reviewId: Long, val watchlistId: Long, val rating: Int, val notes: String?)

/** Same three-table SQLite contract as the original database project. Call off the UI thread. */
class CineMoodRepository(context: Context, databaseName: String = "cinemood.db") : AutoCloseable {
    private val helper = CineMoodDbHelper(context.applicationContext, databaseName)
    private fun derive(password: String, salt: ByteArray, iterations: Int, algorithm: String): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, 256)
        return try { SecretKeyFactory.getInstance(algorithm).generateSecret(spec).encoded }
        finally { spec.clearPassword() }
    }
    private fun hash(password: String): String {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val algorithm = if (android.os.Build.VERSION.SDK_INT >= 26) "PBKDF2WithHmacSHA256" else "PBKDF2WithHmacSHA1"
        return listOf(algorithm, "120000", Base64.encodeToString(salt, Base64.NO_WRAP),
            Base64.encodeToString(derive(password, salt, 120000, algorithm), Base64.NO_WRAP)).joinToString("$")
    }
    private fun verify(password: String, encoded: String): Boolean = try {
        val parts = encoded.split("$")
        // Also reads the original project's three-part SHA256 PBKDF2 hashes.
        val offset = if (parts.size == 4) 1 else 0
        val algorithm = if (offset == 1) parts[0] else "PBKDF2WithHmacSHA256"
        (parts.size == 3 || parts.size == 4) &&
            algorithm in listOf("PBKDF2WithHmacSHA256", "PBKDF2WithHmacSHA1") &&
            MessageDigest.isEqual(Base64.decode(parts[offset + 2], Base64.NO_WRAP),
                derive(password, Base64.decode(parts[offset + 1], Base64.NO_WRAP), parts[offset].toInt(), algorithm))
    } catch (_: IllegalArgumentException) { false }

    fun usernameExists(username: String): Boolean = helper.readableDatabase.rawQuery(
        "SELECT user_id FROM users WHERE username = ? COLLATE NOCASE",
        arrayOf(username.trim())).use { it.moveToFirst() }

    fun register(username: String, password: String): Long? {
        if (username.isBlank() || !PasswordPolicy.isValid(password)) return null
        val values = ContentValues().apply { put("username", username.trim()); put("password_hash", hash(password)) }
        return try { helper.writableDatabase.insertOrThrow("users", null, values) }
        catch (_: SQLiteConstraintException) { null }
    }
    fun login(username: String, password: String): Long? {
        if (username.isBlank() || password.isBlank()) return null
        return helper.readableDatabase.rawQuery(
            "SELECT user_id, password_hash FROM users WHERE username = ? COLLATE NOCASE",
            arrayOf(username.trim())).use {
            if (it.moveToFirst() && verify(password, it.getString(1))) it.getLong(0) else null
        }
    }
    fun getUsername(userId: Long): String? = helper.readableDatabase.rawQuery(
        "SELECT username FROM users WHERE user_id = ?", arrayOf(userId.toString())
    ).use { if (it.moveToFirst()) it.getString(0) else null }

    fun addMovie(userId: Long, tmdbMovieId: Long, title: String,
                 posterPath: String? = null, overview: String? = null,
                 year: Int = 0, genre: String = "", source: String = "TMDB"): Long? {
        if (userId <= 0 || title.isBlank() || tmdbMovieId == 0L ||
            (source == "TMDB" && tmdbMovieId < 0) ||
            (source == "SAMPLE" && tmdbMovieId > 0) || source !in listOf("SAMPLE", "TMDB")) return null
        val values = ContentValues().apply {
            put("user_id", userId); put("tmdb_movie_id", tmdbMovieId); put("movie_title", title.trim())
            put("poster_path", posterPath); put("overview", overview)
            put("movie_year", year); put("movie_genre", genre); put("movie_source", source)
        }
        return try { helper.writableDatabase.insertOrThrow("watchlist", null, values) }
        catch (_: SQLiteConstraintException) { null }
    }
    fun getMovies(userId: Long): List<MovieRecord> {
        val movies = mutableListOf<MovieRecord>()
        helper.readableDatabase.rawQuery("""SELECT watchlist_id, user_id, tmdb_movie_id,
            movie_title, poster_path, overview, status, movie_year, movie_genre, movie_source FROM watchlist
            WHERE user_id = ? ORDER BY date_added DESC, watchlist_id DESC""",
            arrayOf(userId.toString())).use { c ->
            while (c.moveToNext()) movies.add(MovieRecord(c.getLong(0), c.getLong(1), c.getLong(2),
                c.getString(3), c.getString(4), c.getString(5), c.getString(6), c.getInt(7), c.getString(8), c.getString(9)))
        }
        return movies
    }
    fun updateStatus(userId: Long, watchlistId: Long, status: String): Boolean {
        if (status !in listOf("PLANNED", "WATCHED")) return false
        return helper.writableDatabase.update("watchlist", ContentValues().apply { put("status", status) },
            "user_id = ? AND watchlist_id = ?", arrayOf(userId.toString(), watchlistId.toString())) == 1
    }
    fun deleteMovie(userId: Long, watchlistId: Long): Boolean = helper.writableDatabase.delete(
        "watchlist", "user_id = ? AND watchlist_id = ?", arrayOf(userId.toString(), watchlistId.toString())) == 1
    fun getReview(userId: Long, watchlistId: Long): ReviewRecord? = helper.readableDatabase.rawQuery(
        """SELECT r.review_id, r.watchlist_id, r.rating, r.notes FROM reviews r
            JOIN watchlist w ON w.watchlist_id = r.watchlist_id
            WHERE w.user_id = ? AND w.watchlist_id = ?""",
        arrayOf(userId.toString(), watchlistId.toString())).use {
        if (it.moveToFirst()) ReviewRecord(it.getLong(0), it.getLong(1), it.getInt(2), it.getString(3)) else null
    }
    fun saveReview(userId: Long, watchlistId: Long, rating: Int, notes: String?): Boolean {
        if (rating !in 1..5) return false
        val db = helper.writableDatabase
        db.beginTransaction()
        try {
            val owns = db.rawQuery("SELECT watchlist_id FROM watchlist WHERE user_id = ? AND watchlist_id = ?",
                arrayOf(userId.toString(), watchlistId.toString())).use { it.moveToFirst() }
            if (!owns) return false
            val existing = getReview(userId, watchlistId)
            if (existing == null) {
                db.insertOrThrow("reviews", null, ContentValues().apply {
                    put("watchlist_id", watchlistId); put("rating", rating); put("notes", notes)
                })
            } else {
                db.execSQL("UPDATE reviews SET rating = ?, notes = ?, updated_at = CURRENT_TIMESTAMP WHERE watchlist_id = ?",
                    arrayOf<Any?>(rating, notes, watchlistId))
            }
            db.setTransactionSuccessful()
            return true
        } finally { db.endTransaction() }
    }
    fun deleteReview(userId: Long, watchlistId: Long): Boolean = helper.writableDatabase.delete("reviews",
        "watchlist_id = ? AND watchlist_id IN (SELECT watchlist_id FROM watchlist WHERE user_id = ?)",
        arrayOf(watchlistId.toString(), userId.toString())) == 1
    override fun close() { helper.close() }
}
