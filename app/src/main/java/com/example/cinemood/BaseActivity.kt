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
    protected val session by lazy { com.example.cinemood.data.SessionManager(this) }
    protected fun <T> databaseWork(
        work: (com.example.cinemood.data.CineMoodRepository) -> T,
        failure: () -> Unit = {},
        complete: (T) -> Unit
    ) {
        databaseExecutor.execute {
            val result = runCatching {
                com.example.cinemood.data.CineMoodRepository(applicationContext).use(work)
            }
            runOnUiThread {
                if (!isFinishing && !isDestroyed) {
                    result.fold(complete) {
                        android.util.Log.e("CineMoodDatabase", "Database operation failed", it)
                        message("Unable to access saved data. Please try again.")
                        failure()
                    }
                }
            }
        }
    }
    override fun onStart() {
        super.onStart()
        if (this !is LoginActivity && this !is RegisterActivity && this !is JourneyActivity && session.userId <= 0) {
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }
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
        val back = findViewById<Button>(R.id.btnBack)
        val parent = back.parent as? android.view.ViewGroup
        if (parent != null && parent.indexOfChild(back) > 0) {
            val params = back.layoutParams
            parent.removeView(back)
            parent.addView(back, 0, params)
        }
        back.setOnClickListener { finish() }
    }
    protected fun message(text: String) = Toast.makeText(this, text, Toast.LENGTH_SHORT).show()

    protected fun movieWork(work: () -> List<Movie>, complete: (List<Movie>) -> Unit, failure: (String) -> Unit) {
        networkExecutor.execute {
            val result = runCatching(work)
            runOnUiThread {
                if (!isFinishing && !isDestroyed) result.fold(complete) {
                    failure(it.message?.takeIf { _ -> it is com.example.cinemood.network.MovieServiceException }
                        ?: "Unable to load movies. Please try again.")
                }
            }
        }
    }

    // Every sample movie opens the same details Activity with a different ID.
    protected fun openMovie(movie: Movie) {
        openThroughJourney(MovieDetailsActivity::class.java, Bundle().apply {
            putInt(EXTRA_MOVIE_ID, movie.id)
            putString("movie_json", movie.toJson())
        })
    }
    protected fun openThroughJourney(target: Class<out android.app.Activity>, extras: Bundle? = null) {
        startActivity(Intent(this, target).apply {
            extras?.let { putExtras(it) }
            putExtra("user_id", session.userId)
            if (target == LoginActivity::class.java) {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
        })
    }
    protected fun renderMovies(container: LinearLayout, movies: List<Movie>, showStatus: Boolean = false,
                               watchedIds: Set<Int> = emptySet()) {
        container.removeAllViews()
        movies.forEach { movie ->
            val row = layoutInflater.inflate(R.layout.item_movie, container, false)
            com.example.cinemood.network.PosterLoader.load(row.findViewById(R.id.ivMovieArt), movie.posterPath)
            val status = if (movie.id in watchedIds) "Watched" else "To watch"
            row.findViewById<TextView>(R.id.tvRowTitle).text = movie.title
            row.findViewById<TextView>(R.id.tvRowMeta).text = "${movie.year} · ${movie.genre}"
            row.findViewById<TextView>(R.id.tvRowStatus).text = if (showStatus) "$status  ·  VIEW FILM →" else "VIEW FILM →"
            row.contentDescription = "${movie.title}, ${movie.year}, ${movie.genre}" + if (showStatus) ", $status" else ""
            row.setOnClickListener { openMovie(movie) }
            container.addView(row)
        }
    }
    companion object {
        private val databaseExecutor = java.util.concurrent.Executors.newSingleThreadExecutor()
        private val networkExecutor = java.util.concurrent.Executors.newFixedThreadPool(3)
        const val EXTRA_USERNAME = "com.example.cinemood.USERNAME"
        const val EXTRA_MOVIE_ID = "com.example.cinemood.MOVIE_ID"
        const val EXTRA_JOURNEY_TARGET = "com.example.cinemood.JOURNEY_TARGET"
        const val EXTRA_JOURNEY_EXTRAS = "com.example.cinemood.JOURNEY_EXTRAS"
    }
}
