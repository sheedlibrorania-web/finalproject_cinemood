package com.example.cinemood

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

// Common screen setup keeps system bars away from form fields and buttons.
abstract class BaseActivity : AppCompatActivity() {
    protected fun showScreen(layoutId: Int) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(layoutId)
        val root = findViewById<android.view.View>(R.id.screenRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        ViewCompat.requestApplyInsets(root)
    }
    protected fun backButton() {
        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
    }
    protected fun message(text: String) = Toast.makeText(this, text, Toast.LENGTH_SHORT).show()

    // Every sample movie opens the same details Activity with a different ID.
    protected fun openMovie(movie: Movie) {
        openThroughJourney(MovieDetailsActivity::class.java, Bundle().apply {
            putInt(EXTRA_MOVIE_ID, movie.id)
        })
    }
    protected fun openThroughJourney(target: Class<out android.app.Activity>, extras: Bundle? = null) {
        startActivity(Intent(this, JourneyActivity::class.java).apply {
            putExtra(EXTRA_JOURNEY_TARGET, target.name)
            extras?.let { putExtra(EXTRA_JOURNEY_EXTRAS, it) }
        })
    }
    protected fun renderMovies(container: LinearLayout, movies: List<Movie>, showStatus: Boolean = false) {
        container.removeAllViews()
        movies.forEach { movie ->
            val row = layoutInflater.inflate(R.layout.item_movie, container, false)
            val status = if (movie.id in DemoStore.watched) "Watched" else "To watch"
            row.findViewById<TextView>(R.id.tvRowTitle).text = movie.title
            row.findViewById<TextView>(R.id.tvRowMeta).text = "${movie.year} · ${movie.genre}"
            row.findViewById<TextView>(R.id.tvRowStatus).text = if (showStatus) "$status  ·  VIEW FILM →" else "VIEW FILM →"
            row.contentDescription = "${movie.title}, ${movie.year}, ${movie.genre}" + if (showStatus) ", $status" else ""
            row.setOnClickListener { openMovie(movie) }
            container.addView(row)
        }
    }
    companion object {
        const val EXTRA_USERNAME = "com.example.cinemood.USERNAME"
        const val EXTRA_MOVIE_ID = "com.example.cinemood.MOVIE_ID"
        const val EXTRA_JOURNEY_TARGET = "com.example.cinemood.JOURNEY_TARGET"
        const val EXTRA_JOURNEY_EXTRAS = "com.example.cinemood.JOURNEY_EXTRAS"
    }
}
