package com.example.cinemood.network

import android.content.Context
import com.example.cinemood.BuildConfig
import com.example.cinemood.DemoStore
import com.example.cinemood.Movie
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLEncoder

class MovieServiceException(message: String) : IOException(message)

class TmdbSettings(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("tmdb_settings", Context.MODE_PRIVATE)
    var token: String
        get() = if (BuildConfig.DEBUG) prefs.getString("read_token", BuildConfig.TMDB_READ_ACCESS_TOKEN).orEmpty()
            else BuildConfig.TMDB_READ_ACCESS_TOKEN
        set(value) {
            if (BuildConfig.DEBUG) prefs.edit().putString("read_token", value.trim().removePrefix("Bearer ")).apply()
        }
    val sampleMode: Boolean get() = BuildConfig.DEBUG && token.isBlank()
}

/** Inject a transport in tests; no token is sent anywhere except the fixed TMDB API host. */
class TmdbClient(
    private val token: String,
    private val transport: ((String, String) -> String)? = null
) {
    fun search(query: String): List<Movie> {
        require(query.isNotBlank()) { "Enter a movie title" }
        return parseMovies(request("search/movie", mapOf("query" to query.trim(), "include_adult" to "false")))
    }
    fun discover(moods: Collection<String>): List<Movie> {
        val ids = moods.flatMap { moodGenres[it].orEmpty() }.distinct()
        return parseMovies(request("discover/movie", buildMap {
            put("include_adult", "false"); put("sort_by", "popularity.desc")
            if (ids.isNotEmpty()) put("with_genres", ids.joinToString("|"))
            // Exclude only genres that none of the selected moods explicitly requests.
            val excluded = moods.flatMap { moodExclusions[it].orEmpty() }.distinct() - ids.toSet()
            if (excluded.isNotEmpty()) put("without_genres", excluded.joinToString(","))
        }))
    }
    private fun request(path: String, params: Map<String, String>): String {
        if (token.isBlank()) throw MovieServiceException("Movie discovery is not configured. Please contact the CineMood team.")
        val query = (params + mapOf("language" to "en-US", "page" to "1")).entries.joinToString("&") {
            "${it.key}=${URLEncoder.encode(it.value, "UTF-8")}" }
        val address = "https://api.themoviedb.org/3/$path?$query"
        if (transport != null) return transport.invoke(address, token)
        val connection = URL(address).openConnection() as HttpURLConnection
        return try {
            connection.connectTimeout = 12000; connection.readTimeout = 12000
            connection.setRequestProperty("Authorization", "Bearer $token")
            connection.setRequestProperty("Accept", "application/json")
            when (connection.responseCode) {
                200 -> connection.inputStream.bufferedReader().use { it.readText() }
                401, 403 -> throw MovieServiceException(if (BuildConfig.DEBUG)
                    "TMDB rejected the token. Check it in Movie data settings."
                    else "Movie discovery is temporarily unavailable. Please contact the CineMood team.")
                429 -> throw MovieServiceException("TMDB is busy. Please try again shortly.")
                else -> throw MovieServiceException("TMDB is unavailable. Please try again later.")
            }
        } catch (e: MovieServiceException) { throw e
        } catch (_: SocketTimeoutException) { throw MovieServiceException("The request timed out. Please try again.")
        } catch (_: IOException) { throw MovieServiceException("Cannot reach TMDB. Check your internet connection.")
        } finally { connection.disconnect() }
    }
    companion object {
        val moodGenres = mapOf("Happy" to listOf(35, 16), "Romantic" to listOf(10749, 35),
            "Excited" to listOf(28, 12), "Scared" to listOf(27, 53), "Curious" to listOf(9648, 99),
            "Relaxed" to listOf(10751, 10402, 14), "Sad" to listOf(18, 10749))
        private val moodExclusions = mapOf(
            "Happy" to listOf(27, 53, 80),
            "Relaxed" to listOf(27, 53, 28, 12, 80)
        )
        private val genres = mapOf(28 to "Action", 12 to "Adventure", 16 to "Animation", 35 to "Comedy",
            80 to "Crime", 99 to "Documentary", 18 to "Drama", 10751 to "Family", 14 to "Fantasy",
            36 to "History", 27 to "Horror", 10402 to "Music", 9648 to "Mystery", 10749 to "Romance",
            878 to "Science fiction", 10770 to "TV Movie", 53 to "Thriller", 10752 to "War", 37 to "Western")
        fun parseMovies(json: String): List<Movie> {
            val results = JSONObject(json).getJSONArray("results")
            return (0 until results.length()).mapNotNull { index ->
                val item = results.optJSONObject(index) ?: return@mapNotNull null
                val id = item.optInt("id")
                val title = item.optString("title").takeIf { it != "null" }.orEmpty()
                if (id <= 0 || title.isBlank() || item.optBoolean("adult")) return@mapNotNull null
                val genreIds = item.optJSONArray("genre_ids")
                val genre = if (genreIds != null) (0 until genreIds.length()).mapNotNull {
                    genres[genreIds.optInt(it)] }.joinToString(" / ") else ""
                Movie(id, title, item.optString("release_date").take(4).toIntOrNull() ?: 0,
                    genre.ifBlank { "Genre unavailable" },
                    item.optString("overview").takeIf { it.isNotBlank() && it != "null" } ?: "No overview available.",
                    item.optString("poster_path").takeIf { it.startsWith("/") })
            }.distinctBy { it.id }
        }
    }
}

class MovieService(context: Context) {
    private val settings = TmdbSettings(context)
    val sampleMode: Boolean get() = settings.sampleMode
    fun search(query: String): List<Movie> = if (sampleMode) {
        DemoStore.movies.filter { it.title.contains(query.trim(), ignoreCase = true) }
    } else TmdbClient(settings.token).search(query)
    fun discover(moods: Collection<String>): List<Movie> = if (sampleMode) {
        if (moods.isEmpty()) DemoStore.movies else DemoStore.forMoods(moods)
    } else TmdbClient(settings.token).discover(moods)
}
