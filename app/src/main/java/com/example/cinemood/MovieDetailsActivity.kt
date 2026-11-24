package com.example.cinemood

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.cinemood.data.CineMoodRepository
import com.example.cinemood.data.MovieRecord

class MovieDetailsActivity : BaseActivity() {
    private var saved: MovieRecord? = null
    private var loading = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showScreen(R.layout.activity_movie_details)
        backButton()
        if (session.userId <= 0) return
        val id = intent.getIntExtra(EXTRA_MOVIE_ID, -1)
        val movie = intent.getStringExtra("movie_json")?.let { encoded ->
            runCatching { Movie.fromJson(encoded) }.getOrNull()
        } ?: DemoStore.movies.firstOrNull { it.id == id }
        if (movie == null) { message("Movie not found"); finish(); return }
        val userId = session.userId
        findViewById<TextView>(R.id.tvMovieTitle).text = movie.title
        findViewById<TextView>(R.id.tvMovieMeta).text = "${movie.year} · ${movie.genre}"
        findViewById<TextView>(R.id.tvOverview).text = movie.overview
        com.example.cinemood.network.PosterLoader.load(findViewById(R.id.ivPoster), movie.posterPath)
        val add = findViewById<Button>(R.id.btnAddWatchlist)
        val remove = findViewById<Button>(R.id.btnRemoveWatchlist)
        val save = findViewById<Button>(R.id.btnSaveReview)
        val watched = findViewById<CheckBox>(R.id.cbWatched)
        val rating = findViewById<RatingBar>(R.id.ratingBar)
        val notes = findViewById<EditText>(R.id.etNotes)

        fun controls(busy: Boolean) {
            loading = busy
            add.isEnabled = !busy && saved == null
            add.text = if (saved != null) "Added to watchlist" else "Add to watchlist"
            remove.isEnabled = !busy && saved != null
            save.isEnabled = !busy
            watched.isEnabled = !busy
            rating.isEnabled = !busy
            notes.isEnabled = !busy
        }
        fun load() {
            controls(true)
            databaseWork({ repo ->
                val entry = repo.getMovies(userId).firstOrNull { it.tmdbMovieId == id.toLong() }
                entry to entry?.let { repo.getReview(userId, it.watchlistId) }
            }, failure = { controls(false) }) { (entry, review) ->
                saved = entry
                watched.isChecked = entry?.status == "WATCHED"
                rating.rating = review?.rating?.toFloat() ?: 0f
                if (notes.text.toString() != review?.notes.orEmpty()) notes.setText(review?.notes.orEmpty())
                controls(false)
            }
        }
        fun ensureSaved(repo: CineMoodRepository): Long {
            return repo.getMovies(userId).firstOrNull { it.tmdbMovieId == id.toLong() }?.watchlistId
                ?: repo.addMovie(userId, id.toLong(), movie.title, movie.posterPath, movie.overview,
                    movie.year, movie.genre, movie.source)
                ?: error("Could not save movie")
        }
        add.setOnClickListener {
            controls(true)
            databaseWork({ repo -> ensureSaved(repo) }, failure = { controls(false) }) {
                message("Added to watchlist")
                load()
            }
        }
        remove.setOnClickListener {
            val entry = saved ?: return@setOnClickListener
            AlertDialog.Builder(this)
                .setTitle("Remove movie?")
                .setMessage("Remove this movie and its rating and notes from your watchlist?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Remove") { _, _ ->
                    controls(true)
                    databaseWork({ it.deleteMovie(userId, entry.watchlistId) }, failure = { controls(false) }) { deleted ->
                        message(if (deleted) "Removed from watchlist" else "Movie was not found")
                        load()
                    }
                }.show()
        }
        watched.setOnCheckedChangeListener { _, checked ->
            if (!loading) {
                controls(true)
                databaseWork({ repo ->
                    repo.updateStatus(userId, ensureSaved(repo), if (checked) "WATCHED" else "PLANNED")
                }, failure = { controls(false) }) { success ->
                    message(if (success) "Status updated" else "Unable to update status")
                    databaseWork({ it.getMovies(userId).firstOrNull { row -> row.tmdbMovieId == id.toLong() } }, failure = { controls(false) }) {
                        saved = it
                        controls(false)
                    }
                }
            }
        }
        save.setOnClickListener {
            val stars = rating.rating.toInt()
            if (stars !in 1..5) {
                message("Choose a rating from 1 to 5")
                return@setOnClickListener
            }
            val text = notes.text.toString()
            notes.clearFocus()
            controls(true)
            databaseWork({ repo -> repo.saveReview(userId, ensureSaved(repo), stars, text) }, failure = { controls(false) }) { success ->
                message(if (success) "Rating and notes saved" else "Unable to save review")
                load()
            }
        }
        findViewById<Button>(R.id.btnShare).setOnClickListener {
            val share = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "My CineMood pick: ${movie.title} (${movie.year})")
            }
            try { startActivity(Intent.createChooser(share, "Share movie")) }
            catch (_: ActivityNotFoundException) { message("No sharing app is installed") }
        }
        val deleteReview = Button(android.view.ContextThemeWrapper(this, R.style.CM_Quiet)).apply {
            text = "Delete rating and notes"
            setOnClickListener {
                val entry = saved ?: return@setOnClickListener
                AlertDialog.Builder(this@MovieDetailsActivity).setTitle("Delete rating and notes?")
                    .setMessage("The movie will stay in your watchlist.")
                    .setNegativeButton("Cancel", null).setPositiveButton("Delete") { _, _ ->
                        controls(true)
                        databaseWork({ it.deleteReview(userId, entry.watchlistId) }, failure = { controls(false) }) {
                            message(if (it) "Rating and notes deleted" else "No review to delete")
                            load()
                        }
                    }.show()
            }
        }
        (save.parent as android.view.ViewGroup).addView(deleteReview)
        load()
    }
}
