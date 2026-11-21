package com.example.cinemood

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView

class MovieDetailsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showScreen(R.layout.activity_movie_details)
        backButton()
        // Read the same key that openMovie() used when it created the Intent.
        val id = intent.getIntExtra(EXTRA_MOVIE_ID, -1)
        val movie = DemoStore.movies.firstOrNull { it.id == id }
        if (movie == null) { message("Movie not found"); finish(); return }
        findViewById<TextView>(R.id.tvMovieTitle).text = movie.title
        findViewById<TextView>(R.id.tvMovieMeta).text = "${movie.year} · ${movie.genre}"
        findViewById<TextView>(R.id.tvOverview).text = movie.overview
        val add = findViewById<Button>(R.id.btnAddWatchlist)
        val remove = findViewById<Button>(R.id.btnRemoveWatchlist)
        val watched = findViewById<CheckBox>(R.id.cbWatched)
        val rating = findViewById<RatingBar>(R.id.ratingBar)
        val notes = findViewById<EditText>(R.id.etNotes)
        watched.isChecked = movie.id in DemoStore.watched
        rating.rating = DemoStore.reviews[id]?.rating ?: 0f
        notes.setText(DemoStore.reviews[id]?.notes ?: "")
        fun refreshButtons() {
            add.isEnabled = id !in DemoStore.savedIds
            add.text = if (id in DemoStore.savedIds) "Added to watchlist" else "Add to watchlist"
            remove.isEnabled = id in DemoStore.savedIds
        }
        refreshButtons()
        add.setOnClickListener { DemoStore.savedIds.add(id); refreshButtons(); message("Added to watchlist") }
        remove.setOnClickListener {
            DemoStore.savedIds.remove(id); DemoStore.watched.remove(id); DemoStore.reviews.remove(id)
            watched.isChecked = false; rating.rating = 0f; notes.setText("")
            refreshButtons(); message("Removed from watchlist")
        }
        watched.setOnCheckedChangeListener { _, checked ->
            if (checked) { DemoStore.savedIds.add(id); DemoStore.watched.add(id) }
            else DemoStore.watched.remove(id)
            refreshButtons()
        }
        findViewById<Button>(R.id.btnSaveReview).setOnClickListener {
            DemoStore.savedIds.add(id)
            if (watched.isChecked) DemoStore.watched.add(id) else DemoStore.watched.remove(id)
            DemoStore.reviews[id] = DemoReview(rating.rating, notes.text.toString())
            refreshButtons(); message("Saved to watchlist")
        }
        findViewById<Button>(R.id.btnShare).setOnClickListener {
            // Implicit Intent: the user chooses another app to share the text.
            val share = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "My CineMood pick: ${movie.title} (${movie.year})")
            }
            try { startActivity(Intent.createChooser(share, "Share movie")) }
            catch (_: ActivityNotFoundException) { message("No sharing app is installed") }
        }
    }
}
