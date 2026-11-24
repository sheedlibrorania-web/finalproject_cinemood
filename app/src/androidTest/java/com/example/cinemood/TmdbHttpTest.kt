package com.example.cinemood

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cinemood.network.MovieServiceException
import com.example.cinemood.network.TmdbClient
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Exercises the real HTTP handling path without real credentials, traffic or rate-limit abuse. */
@RunWith(AndroidJUnit4::class)
class TmdbHttpTest {
    private class Connection(
        private val status: Int = 200,
        private val body: String = """{"results":[]}""",
        private val responseFailure: IOException? = null,
        private val readFailure: IOException? = null
    ) : HttpURLConnection(URL("https://api.themoviedb.org/3/search/movie")) {
        var disconnected = false
        var streamClosed = false
        override fun connect() = Unit
        override fun usingProxy() = false
        override fun disconnect() { disconnected = true }
        override fun getResponseCode(): Int {
            responseFailure?.let { throw it }
            return status
        }
        override fun getInputStream(): InputStream {
            readFailure?.let { throw it }
            return object : ByteArrayInputStream(body.toByteArray(Charsets.UTF_8)) {
                override fun close() { streamClosed = true; super.close() }
            }
        }
    }

    private fun client(connection: Connection) = TmdbClient(
        "synthetic-test-token", connectionFactory = { address ->
            assertEquals("https", address.protocol)
            assertEquals("api.themoviedb.org", address.host)
            assertEquals("/3/search/movie", address.path)
            assertTrue(address.query.contains("query=A%26B+movie"))
            assertFalse(address.toString().contains("synthetic-test-token"))
            connection
        }
    )

    private fun expectFailure(connection: Connection, expected: String) {
        try {
            client(connection).search("A&B movie")
            fail("Expected a movie service error")
        } catch (error: MovieServiceException) {
            assertEquals(expected, error.message)
        }
        assertTrue("Connection must be disconnected on failure", connection.disconnected)
    }

    @Test fun successUsesBearerHeaderTimeoutsAndClosesResources() {
        val connection = Connection(body = """{"results":[{"id":42,"title":"Fixture movie"}]}""")
        assertEquals("Fixture movie", client(connection).search("A&B movie").single().title)
        assertEquals("GET", connection.requestMethod)
        assertEquals("Bearer synthetic-test-token", connection.getRequestProperty("Authorization"))
        assertEquals("application/json", connection.getRequestProperty("Accept"))
        assertEquals(12000, connection.connectTimeout)
        assertEquals(12000, connection.readTimeout)
        assertTrue(connection.streamClosed)
        assertTrue(connection.disconnected)
    }

    @Test fun emptySuccessIsAnEmptyListRatherThanAnError() {
        val connection = Connection()
        assertTrue(client(connection).search("A&B movie").isEmpty())
        assertTrue(connection.disconnected)
    }

    @Test fun authorizationFailuresUseTheConfiguredBuildMessage() {
        val expected = if (BuildConfig.DEBUG) "TMDB rejected the token. Check it in Movie data settings."
            else "Movie discovery is temporarily unavailable. Please contact the CineMood team."
        for (status in listOf(401, 403)) expectFailure(Connection(status = status), expected)
    }

    @Test fun rateLimitShowsRetryMessage() {
        expectFailure(Connection(status = 429), "TMDB is busy. Please try again shortly.")
    }

    @Test fun serverFailureShowsUnavailableMessage() {
        expectFailure(Connection(status = 503), "TMDB is unavailable. Please try again later.")
    }

    @Test fun unexpectedHttpStatusShowsUnavailableMessage() {
        expectFailure(Connection(status = 404), "TMDB is unavailable. Please try again later.")
    }

    @Test fun timeoutWhileObtainingResponseShowsTimeoutMessage() {
        expectFailure(Connection(responseFailure = SocketTimeoutException()), "The request timed out. Please try again.")
    }

    @Test fun timeoutWhileReadingResponseShowsTimeoutMessage() {
        expectFailure(Connection(readFailure = SocketTimeoutException()), "The request timed out. Please try again.")
    }

    @Test fun ioFailureShowsConnectionMessage() {
        expectFailure(Connection(responseFailure = IOException()), "Cannot reach TMDB. Check your internet connection.")
    }

    @Test fun malformedJsonDoesNotProduceInventedMovies() {
        val connection = Connection(body = "not valid JSON")
        try {
            client(connection).search("A&B movie")
            fail("Expected JSON parsing to fail")
        } catch (_: org.json.JSONException) {
            // BaseActivity.movieWork maps non-service failures to a generic movie loading message.
        }
        assertTrue(connection.streamClosed)
        assertTrue(connection.disconnected)
    }
}
