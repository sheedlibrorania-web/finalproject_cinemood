package com.example.cinemood

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cinemood.network.TmdbClient
import com.example.cinemood.network.MovieServiceException
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TmdbClientTest {
    private val response = """{"results":[{"id":157336,"title":"Interstellar","release_date":"2014-11-05","genre_ids":[878,12],"overview":"Explorers travel.","poster_path":"/poster.jpg"}]}"""
    @Test fun parsesMovieAndMissingOptionalFields() {
        val movie = TmdbClient.parseMovies(response).single()
        assertEquals(157336, movie.id); assertEquals(2014, movie.year)
        assertEquals("Science fiction / Adventure", movie.genre); assertEquals("TMDB", movie.source)
        val minimal = TmdbClient.parseMovies("""{"results":[{"id":10,"title":"Unknown","poster_path":null}]}""").single()
        assertNull(minimal.posterPath); assertEquals(0, minimal.year); assertEquals("No overview available.", minimal.overview)
    }
    @Test fun filtersInvalidAdultAndDuplicateResults() {
        assertEquals(1, TmdbClient.parseMovies("""{"results":[{"id":1,"title":"OK"},{"id":1,"title":"OK"},{"id":2,"title":"Adult","adult":true},{"id":0,"title":"Invalid"},{"id":3,"title":""}]}""").size)
        assertTrue(TmdbClient.parseMovies("""{"results":[]}""").isEmpty())
    }
    @Test fun searchEncodesQueryAndPassesTokenOnlyToTransport() {
        var called = false
        TmdbClient("test-token") { address, token ->
            assertTrue(address.startsWith("https://api.themoviedb.org/3/search/movie?"))
            assertTrue(address.contains("query=A%26B+movie")); assertFalse(address.contains("test-token"))
            assertEquals("test-token", token); called = true; response
        }.search("A&B movie")
        assertTrue(called)
    }
    @Test fun moodDiscoveryUsesGenreRules() {
        TmdbClient("test-token") { address, _ ->
            assertTrue(address.contains("with_genres=10751%7C10402%7C14"))
            assertTrue(address.contains("without_genres=27%2C53%2C28%2C12%2C80"))
            response
        }.discover(listOf("Relaxed"))
    }
    @Test fun upbeatAndChillUseDifferentDiscoveryRequests() {
        val addresses = mutableListOf<String>()
        val client = TmdbClient("test-token") { address, _ -> addresses.add(address); response }
        client.discover(listOf("Happy"))
        client.discover(listOf("Relaxed"))
        assertNotEquals(addresses[0], addresses[1])
        assertTrue(addresses[0].contains("with_genres=35%7C16"))
        assertTrue(addresses[1].contains("with_genres=10751%7C10402%7C14"))
    }
    @Test fun missingTokenAndEmptySearchAreRejected() {
        try { TmdbClient("").discover(emptyList()); fail("Expected missing token error") } catch (_: MovieServiceException) {}
        try { TmdbClient("test").search(" "); fail("Expected validation error") } catch (_: IllegalArgumentException) {}
    }
    @Test fun movieNavigationPayloadRoundTrips() {
        val movie = TmdbClient.parseMovies(response).single()
        assertEquals(movie, Movie.fromJson(movie.toJson()))
    }
}
