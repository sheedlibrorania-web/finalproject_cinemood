package com.example.cinemood

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView

class WatchlistActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showScreen(R.layout.activity_watchlist)
        backButton()
    }
    override fun onResume() {
        super.onResume()
        // Refresh when returning from details so changed status is visible.
        val movies = DemoStore.movies.filter { it.id in DemoStore.savedIds }
        findViewById<TextView>(R.id.tvEmpty).text = if (movies.isEmpty()) "Your collection starts with one film. Discover a title in Search or Mood, then save it here for later." else "${movies.size} saved films · Tap a film to revisit your notes."
        renderMovies(findViewById<LinearLayout>(R.id.movieContainer), movies, showStatus = true)
    }
}
