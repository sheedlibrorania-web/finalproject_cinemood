package com.example.cinemood

import java.util.Locale

/** Local mood vocabulary, using the same keys as the quick-pick recommendations. */
object MoodSearch {
    private val aliases = mapOf(
        "Happy" to listOf("upbeat", "happy", "cheerful", "joyful", "joy", "feel good", "funny", "laugh", "comedy"),
        "Romantic" to listOf("smitten", "romantic", "romance", "love", "lovestruck", "swoony"),
        "Excited" to listOf("amped", "excited", "energetic", "energized", "pumped", "adventurous", "adventure"),
        "Scared" to listOf("spooked", "scared", "spooky", "scary", "horror", "frightened", "thriller", "suspense"),
        "Curious" to listOf("intrigued", "curious", "mystery", "mysterious", "puzzled", "plot twist"),
        "Relaxed" to listOf("chill", "relaxed", "relaxing", "calm", "cozy", "cosy", "comfort", "mellow", "tired", "stressed", "unwind"),
        "Sad" to listOf("wistful", "sad", "blue", "melancholy", "melancholic", "nostalgic", "tearful", "gloomy", "heartbroken")
    )
    private val words = Regex("[\\p{L}\\p{N}]+")

    fun matchingMoods(query: String): List<String> {
        val tokens = words.findAll(query.lowercase(Locale.ROOT)).map { it.value }.toList()
        if (tokens.isEmpty()) return emptyList()
        val phrase = " ${tokens.joinToString(" ")} "
        val prefix = tokens.singleOrNull()?.takeIf { it.length >= 2 }
        return aliases.filterValues { terms ->
            terms.any { term ->
                phrase.contains(" $term ") || (prefix != null && term.startsWith(prefix))
            }
        }.keys.toList()
    }
}
