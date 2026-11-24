package com.example.cinemood

// Temporary sample data for testing UI interactions. Replace with services later.
object DemoStore {
    private val originalMovies = listOf(
        Movie(1, "Paddington", 2014, "Family / Comedy", "A young bear arrives in London and finds a new home with a kind family."),
        Movie(2, "Little Women", 2019, "Drama / Romance", "Four sisters navigate family, ambition, and love as they grow up."),
        Movie(3, "Interstellar", 2014, "Science fiction / Adventure", "Explorers travel beyond Earth to search for a future for humanity."),
        Movie(4, "The Conjuring", 2013, "Horror / Thriller", "Paranormal investigators help a family facing a frightening presence."),
        Movie(5, "Knives Out", 2019, "Mystery", "A detective investigates the death of a novelist and questions his family."),
        Movie(6, "Finding Nemo", 2003, "Animation / Family", "A father crosses the ocean to find his missing son.")
    )
    // Negative IDs prevent sample records from colliding with real TMDB movie IDs.
    val movies = originalMovies.map { it.copy(id = -it.id, source = "SAMPLE") }
    private val moods = mapOf(
        "Happy" to listOf(1, 6), "Romantic" to listOf(2), "Excited" to listOf(3),
        "Scared" to listOf(4), "Curious" to listOf(5), "Relaxed" to listOf(1, 6), "Sad" to listOf(2)
    )
    fun forMood(mood: String?): List<Movie> =
        if (mood == null) movies else forMoods(listOf(mood))
    fun forMoods(selectedMoods: Collection<String>): List<Movie> {
        val ids = selectedMoods.flatMap { moods[it].orEmpty() }.toSet()
        return movies.filter { -it.id in ids }
    }
}
