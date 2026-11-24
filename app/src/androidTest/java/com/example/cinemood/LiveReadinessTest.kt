package com.example.cinemood

import androidx.test.platform.app.InstrumentationRegistry
import com.example.cinemood.data.CineMoodRepository
import com.example.cinemood.network.TmdbClient
import com.example.cinemood.network.TmdbSettings
import com.example.cinemood.network.MovieServiceException
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test

/** Uses the configured token without logging it and an isolated disposable database. */
class LiveReadinessTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    @Test fun processRestartCheckpoint() {
        val stage = InstrumentationRegistry.getArguments().getString("checkpoint") ?: return
        val name = "cinemood_process_restart_test.db"
        val prefs = context.getSharedPreferences("qa_restart", 0)
        val token = TmdbSettings(context).token
        assertTrue("Live token required for restart check", token.isNotBlank())
        val fingerprint = java.security.MessageDigest.getInstance("SHA-256").digest(token.toByteArray())
            .joinToString("") { "%02x".format(it) }
        if (stage == "save") {
            val movie = TmdbClient(token).search("Interstellar").first { it.title == "Interstellar" }
            context.deleteDatabase(name)
            CineMoodRepository(context, name).use { repo ->
                val user = requireNotNull(repo.register("restart_test", "password123"))
                val id = requireNotNull(repo.addMovie(user, movie.id.toLong(), movie.title,
                    movie.posterPath, movie.overview, movie.year, movie.genre, movie.source))
                assertTrue(repo.updateStatus(user, id, "WATCHED"))
                assertTrue(repo.saveReview(user, id, 4, "Survives process restart"))
                assertTrue(prefs.edit().putLong("user", user).putLong("entry", id)
                    .putString("fingerprint", fingerprint).commit())
            }
        } else {
            try {
                assertEquals("Token changed across process restart", prefs.getString("fingerprint", ""), fingerprint)
                CineMoodRepository(context, name).use { repo ->
                    val user = prefs.getLong("user", -1)
                    assertEquals("Interstellar", repo.getMovies(user).single().movieTitle)
                    assertEquals("WATCHED", repo.getMovies(user).single().status)
                    val review = repo.getReview(user, prefs.getLong("entry", -1))
                    assertEquals(4, review?.rating)
                    assertEquals("Survives process restart", review?.notes)
                }
            } finally { context.deleteDatabase(name); prefs.edit().clear().commit() }
        }
    }
    private fun shell(command: String): String {
        val descriptor = InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command)
        return android.os.ParcelFileDescriptor.AutoCloseInputStream(descriptor).bufferedReader().use { it.readText().trim() }
    }

    @Test fun savedRecordsRemainReadableWithNetworkDisabled() {
        val token = TmdbSettings(context).token.ifBlank { "offline-test-token" }
        val movie = Movie(157336, "Interstellar", 2014, "Science fiction / Adventure",
            "Saved movie metadata for offline verification.", "/poster.jpg", "TMDB")
        val name = "cinemood_offline_readiness_test.db"
        val wifi = shell("settings get global wifi_on")
        val mobile = shell("settings get global mobile_data")
        context.deleteDatabase(name)
        try {
            var user = 0L
            CineMoodRepository(context, name).use { repo ->
                user = requireNotNull(repo.register("offline_test", "password123"))
                val id = requireNotNull(repo.addMovie(user, movie.id.toLong(), movie.title,
                    movie.posterPath, movie.overview, movie.year, movie.genre, movie.source))
                assertTrue(repo.saveReview(user, id, 5, "Available offline"))
            }
            shell("svc wifi disable"); shell("svc data disable")
            Thread.sleep(2000)
            CineMoodRepository(context, name).use { repo ->
                val saved = repo.getMovies(user).single()
                assertEquals(movie, saved.asMovie())
                assertEquals("Available offline", repo.getReview(user, saved.watchlistId)?.notes)
            }
            try {
                TmdbClient(token).search("Interstellar")
                fail("Expected offline request failure")
            } catch (error: MovieServiceException) {
                assertTrue(error.message.orEmpty().contains("connection") || error.message.orEmpty().contains("timed out"))
            }
        } finally {
            shell("svc wifi " + if (wifi == "1") "enable" else "disable")
            shell("svc data " + if (mobile == "1") "enable" else "disable")
            context.deleteDatabase(name)
        }
    }

    @Test fun liveMovieMetadataAndPersonalRecordsSurviveDatabaseReopen() {
        val token = TmdbSettings(context).token
        assumeTrue("Live token is not configured", token.isNotBlank())
        val movie = TmdbClient(token).search("Interstellar").first { it.title == "Interstellar" }
        assertTrue(movie.id > 0)
        assertFalse(movie.posterPath.isNullOrBlank())
        val name = "cinemood_live_readiness_test.db"
        context.deleteDatabase(name)
        try {
            var user = 0L
            var entry = 0L
            CineMoodRepository(context, name).use { repo ->
                user = requireNotNull(repo.register("live_test", "password123"))
                entry = requireNotNull(repo.addMovie(user, movie.id.toLong(), movie.title,
                    movie.posterPath, movie.overview, movie.year, movie.genre, movie.source))
                assertTrue(repo.updateStatus(user, entry, "WATCHED"))
                assertTrue(repo.saveReview(user, entry, 4, "Live persistence verification"))
            }
            // Retrieval is exclusively local: no network request is involved.
            CineMoodRepository(context, name).use { repo ->
                val saved = repo.getMovies(user).single()
                assertEquals(movie, saved.asMovie())
                assertEquals("WATCHED", saved.status)
                assertEquals(4, repo.getReview(user, entry)?.rating)
                assertEquals("Live persistence verification", repo.getReview(user, entry)?.notes)
            }
        } finally { context.deleteDatabase(name) }
    }

    @Test fun invalidTokenShowsUsefulError() {
        try {
            TmdbClient("invalid-readiness-test-token").search("Interstellar")
            fail("Expected invalid-token rejection")
        } catch (error: MovieServiceException) {
            assertTrue("Expected authentication error, not a network outage",
                error.message.orEmpty().contains("rejected the token"))
        }
    }
}
