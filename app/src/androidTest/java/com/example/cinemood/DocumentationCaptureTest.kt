package com.example.cinemood

import android.content.Intent
import android.graphics.Bitmap
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.example.cinemood.data.CineMoodDbHelper
import com.example.cinemood.data.CineMoodRepository
import com.example.cinemood.data.SessionManager
import com.example.cinemood.network.MovieService
import org.junit.Assert.*
import org.junit.Test

/** Reproducible manual figures from real screens, using only a temporary account. Needs live access. */
class DocumentationCaptureTest {
    private fun capture(name: String) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.waitForIdleSync()
        // Allow the asynchronous poster request to render; this is a test delay, not application behavior.
        Thread.sleep(1200)
        val bitmap = requireNotNull(instrumentation.uiAutomation.takeScreenshot())
        val dir = java.io.File(instrumentation.targetContext.getExternalFilesDir(null), "manual").apply { mkdirs() }
        java.io.FileOutputStream(java.io.File(dir, "$name.png")).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        bitmap.recycle()
    }
    private fun awaitCheck(check: () -> Unit) {
        val until = System.currentTimeMillis() + 25000
        var last: Throwable? = null
        while (System.currentTimeMillis() < until) {
            try { check(); return } catch (error: Throwable) { last = error; Thread.sleep(150) }
        }
        throw AssertionError("Documentation screen did not become ready", last)
    }
    @Test fun captureCurrentScreensAndVerifyAttribution() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = context.getSharedPreferences("tmdb_settings", 0)
        val hadOverride = prefs.contains("read_token")
        val oldOverride = prefs.getString("read_token", "").orEmpty()
        val session = SessionManager(context)
        val oldId = session.userId; val oldName = session.username
        val username = "Manual_Demo_${System.currentTimeMillis()}"
        var userId: Long? = null
        try {
            check(prefs.edit().remove("read_token").commit())
            session.logout()
            ActivityScenario.launch(LoginActivity::class.java).use { capture("01-login") }
            ActivityScenario.launch(RegisterActivity::class.java).use { capture("02-register") }
            val movie = MovieService(context).search("Interstellar").first { it.title == "Interstellar" }
            userId = CineMoodRepository(context).use { requireNotNull(it.register(username, "manual-demo-pass")) }
            session.save(userId, username)
            ActivityScenario.launch(DashboardActivity::class.java).use { capture("03-dashboard") }
            ActivityScenario.launch(SearchActivity::class.java).use { screen ->
                screen.onActivity {
                    it.findViewById<EditText>(R.id.etQuery).setText("Interstellar")
                    it.findViewById<Button>(R.id.btnSearch).performClick()
                }
                awaitCheck { screen.onActivity {
                    assertTrue(it.findViewById<TextView>(R.id.tvResultCount).text.startsWith("TMDB"))
                    assertTrue(it.findViewById<LinearLayout>(R.id.movieContainer).childCount > 0)
                } }
                capture("04-search")
            }
            ActivityScenario.launch(MoodActivity::class.java).use { screen ->
                screen.onActivity { it.findViewById<Button>(R.id.btnRelaxed).performClick() }
                awaitCheck { screen.onActivity {
                    assertTrue(it.findViewById<TextView>(R.id.tvMoodResults).text.startsWith("TMDB"))
                    assertTrue(it.findViewById<LinearLayout>(R.id.movieContainer).childCount > 0)
                } }
                capture("05-mood")
                screen.onActivity { it.findViewById<androidx.core.widget.NestedScrollView>(R.id.screenRoot).fullScroll(android.view.View.FOCUS_DOWN) }
                capture("05b-mood-results")
            }
            CineMoodRepository(context).use { repo ->
                val entry = requireNotNull(repo.addMovie(userId, movie.id.toLong(), movie.title,
                    movie.posterPath, movie.overview, movie.year, movie.genre, movie.source))
                check(repo.updateStatus(userId, entry, "WATCHED"))
                check(repo.saveReview(userId, entry, 4, "A memorable movie night. I would watch this again."))
            }
            ActivityScenario.launch(WatchlistActivity::class.java).use { screen ->
                awaitCheck { screen.onActivity { assertEquals(1, it.findViewById<LinearLayout>(R.id.movieContainer).childCount) } }
                capture("06-watchlist")
            }
            val intent = Intent(context, MovieDetailsActivity::class.java).apply {
                putExtra(BaseActivity.EXTRA_MOVIE_ID, movie.id); putExtra("movie_json", movie.toJson())
            }
            ActivityScenario.launch<MovieDetailsActivity>(intent).use { screen ->
                awaitCheck { screen.onActivity { assertTrue(it.findViewById<Button>(R.id.btnRemoveWatchlist).isEnabled) } }
                capture("07-details")
                screen.onActivity { it.findViewById<androidx.core.widget.NestedScrollView>(R.id.screenRoot).fullScroll(android.view.View.FOCUS_DOWN) }
                capture("08-review")
                screen.onActivity { it.findViewById<Button>(R.id.btnRemoveWatchlist).performClick() }
                capture("09-remove-confirmation")
                androidx.test.espresso.Espresso.onView(androidx.test.espresso.matcher.ViewMatchers.withText("Cancel"))
                    .perform(androidx.test.espresso.action.ViewActions.click())
            }
            ActivityScenario.launch(MovieDataSettingsActivity::class.java).use { screen ->
                screen.onActivity {
                    assertNotNull(it.findViewById<android.widget.ImageView>(R.id.ivTmdbLogo).drawable)
                    it.findViewById<androidx.core.widget.NestedScrollView>(R.id.screenRoot).fullScroll(android.view.View.FOCUS_DOWN)
                }
                capture("10-about-credits")
            }
        } finally {
            val edit = prefs.edit()
            if (hadOverride) edit.putString("read_token", oldOverride) else edit.remove("read_token")
            check(edit.commit())
            if (oldId > 0) session.save(oldId, oldName) else session.logout()
            if (userId != null) CineMoodDbHelper(context).use {
                it.writableDatabase.delete("users", "user_id = ?", arrayOf(userId.toString()))
            }
        }
    }
}
