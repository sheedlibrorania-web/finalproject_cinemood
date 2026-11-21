package com.example.cinemood

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MoodSearchTest {
    @Test fun allQuickPickLabelsFindRecommendations() {
        listOf("Upbeat", "Smitten", "Amped", "Spooked", "Intrigued", "Chill", "Wistful").forEach { label ->
            val matches = MoodSearch.matchingMoods(label)
            assertEquals("Expected one mood for $label", 1, matches.size)
            assertFalse("Expected movies for $label", DemoStore.forMoods(matches).isEmpty())
        }
    }

    @Test fun acceptsEverydayFeelingsAndSentences() {
        assertEquals(listOf("Happy"), MoodSearch.matchingMoods("  I feel HAPPY!  "))
        assertEquals(listOf("Relaxed"), MoodSearch.matchingMoods("cozy"))
        assertEquals(listOf("Sad"), MoodSearch.matchingMoods("sad"))
        assertEquals(listOf("Happy"), MoodSearch.matchingMoods("feel-good"))
    }

    @Test fun findsMoodAsTheUserTypes() {
        assertEquals(listOf("Romantic"), MoodSearch.matchingMoods("smi"))
        assertTrue(MoodSearch.matchingMoods("s").isEmpty())
    }

    @Test fun doesNotMatchPartsOfUnrelatedWords() {
        assertTrue(MoodSearch.matchingMoods("unhappy").isEmpty())
        assertTrue(MoodSearch.matchingMoods("gloves").isEmpty())
        assertTrue(MoodSearch.matchingMoods("hungry").isEmpty())
        assertTrue(MoodSearch.matchingMoods("   ").isEmpty())
        assertTrue(MoodSearch.matchingMoods("?!").isEmpty())
    }

    @Test fun combinesMoodsWithoutRepeatingMovies() {
        val matches = MoodSearch.matchingMoods("upbeat and cozy")
        assertEquals(setOf("Happy", "Relaxed"), matches.toSet())
        val movies = DemoStore.forMoods(matches)
        assertFalse(movies.isEmpty())
        assertEquals(movies.map { it.id }.distinct().size, movies.size)
        assertEquals(DemoStore.forMood("Happy"), movies)
    }

    @Test fun anUnmatchedMoodDoesNotRecommendUnrelatedMovies() {
        assertTrue(DemoStore.forMoods(MoodSearch.matchingMoods("hungry")).isEmpty())
        assertEquals(DemoStore.movies, DemoStore.forMood(null))
    }
}
