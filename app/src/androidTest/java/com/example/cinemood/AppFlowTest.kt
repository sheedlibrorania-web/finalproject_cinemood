package com.example.cinemood

import android.content.Intent
import android.widget.RatingBar
import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import com.example.cinemood.data.CineMoodRepository
import com.example.cinemood.data.SessionManager
import com.example.cinemood.network.TmdbSettings
import org.junit.Assert.*
import org.junit.Test

/** Exercises the real screen controls and SQLite together in explicitly selected sample mode. */
class AppFlowTest {
    private fun screenshot(name: String) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val bitmap = instrumentation.uiAutomation.takeScreenshot() ?: return
        val directory = java.io.File(instrumentation.targetContext.getExternalFilesDir(null), "qa").apply { mkdirs() }
        java.io.FileOutputStream(java.io.File(directory, "$name.png")).use {
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
        }
        bitmap.recycle()
    }
    private fun awaitCheck(check: () -> Unit) {
        val deadline = System.currentTimeMillis() + 15000
        var last: Throwable? = null
        while (System.currentTimeMillis() < deadline) {
            try { check(); return } catch (e: Throwable) { last = e; Thread.sleep(150) }
        }
        throw AssertionError("UI condition timed out", last)
    }
    @Test fun registrationLoginWatchlistReviewAndLogout() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val session = SessionManager(context)
        val oldId = session.userId; val oldName = session.username
        val settings = TmdbSettings(context); val oldToken = settings.token
        val name = "ui_test_${System.currentTimeMillis()}"
        val password = "password123"
        session.logout(); settings.token = ""
        try {
            ActivityScenario.launch(RegisterActivity::class.java).use {
                it.onActivity { activity ->
                    activity.findViewById<android.widget.Button>(R.id.btnCreateAccount).performClick()
                    assertEquals("Enter a username", activity.findViewById<android.widget.EditText>(R.id.etUsername).error.toString())
                    activity.findViewById<android.widget.EditText>(R.id.etUsername).setText(name)
                    activity.findViewById<android.widget.EditText>(R.id.etPassword).setText("short")
                    activity.findViewById<android.widget.Button>(R.id.btnCreateAccount).performClick()
                    assertEquals("Use at least 8 characters", activity.findViewById<android.widget.EditText>(R.id.etPassword).error.toString())
                }
                screenshot("registration-validation")
                it.onActivity { activity ->
                    activity.findViewById<android.widget.EditText>(R.id.etPassword).setText(password)
                    activity.findViewById<android.widget.EditText>(R.id.etConfirmPassword).setText("different")
                    activity.findViewById<android.widget.Button>(R.id.btnCreateAccount).performClick()
                    assertEquals("Passwords do not match", activity.findViewById<android.widget.EditText>(R.id.etConfirmPassword).error.toString())
                    activity.findViewById<android.widget.EditText>(R.id.etConfirmPassword).setText(password)
                    activity.findViewById<android.widget.Button>(R.id.btnCreateAccount).performClick()
                }
                awaitCheck { CineMoodRepository(context).use { repo -> assertTrue(repo.usernameExists(name)) } }
                awaitCheck { onView(withId(R.id.btnLogin)).check(matches(isDisplayed())) }
            }
            ActivityScenario.launch(LoginActivity::class.java).use {
                it.onActivity { activity ->
                    activity.findViewById<android.widget.EditText>(R.id.etUsername).setText(name)
                    activity.findViewById<android.widget.EditText>(R.id.etPassword).setText("wrongpassword")
                    activity.findViewById<android.widget.Button>(R.id.btnLogin).performClick()
                }
                awaitCheck { onView(withId(R.id.etPassword)).check(matches(hasErrorText("Invalid username or password"))) }
                it.onActivity { activity ->
                    activity.findViewById<android.widget.EditText>(R.id.etPassword).setText(password)
                    activity.findViewById<android.widget.Button>(R.id.btnLogin).performClick()
                }
                awaitCheck { assertTrue(session.userId > 0) }
                awaitCheck { onView(withId(R.id.tvWelcome)).check(matches(isDisplayed())) }
                screenshot("dashboard")
            }
            val userId = session.userId
            val sample = DemoStore.movies.first { it.title == "Interstellar" }
            val intent = Intent(context, MovieDetailsActivity::class.java).apply {
                putExtra(BaseActivity.EXTRA_MOVIE_ID, sample.id); putExtra("movie_json", sample.toJson())
            }
            ActivityScenario.launch<MovieDetailsActivity>(intent).use { scenario ->
                awaitCheck { onView(withId(R.id.btnAddWatchlist)).check(matches(isEnabled())) }
                scenario.onActivity { it.findViewById<android.widget.Button>(R.id.btnAddWatchlist).performClick() }
                awaitCheck { onView(withId(R.id.btnRemoveWatchlist)).check(matches(isEnabled())) }
                screenshot("movie-saved")
                scenario.onActivity {
                    it.findViewById<RatingBar>(R.id.ratingBar).rating = 4f
                    it.findViewById<android.widget.EditText>(R.id.etNotes).setText("UI integration review")
                    it.findViewById<android.widget.Button>(R.id.btnSaveReview).performClick()
                }
                awaitCheck {
                    CineMoodRepository(context).use { repo ->
                        val saved = repo.getMovies(userId).single()
                        assertEquals("UI integration review", repo.getReview(userId, saved.watchlistId)?.notes)
                    }
                }
                awaitCheck { onView(withId(R.id.btnRemoveWatchlist)).check(matches(isEnabled())) }
                scenario.onActivity { it.findViewById<android.widget.Button>(R.id.btnRemoveWatchlist).performClick() }
                awaitCheck { onView(withText("Cancel")).check(matches(isDisplayed())) }
                screenshot("delete-confirmation")
                onView(withText("Cancel")).perform(click())
                CineMoodRepository(context).use { repo -> assertEquals(1, repo.getMovies(userId).size) }
                scenario.onActivity { it.findViewById<android.widget.Button>(R.id.btnRemoveWatchlist).performClick() }
                awaitCheck { onView(withText("Remove")).check(matches(isDisplayed())) }
                onView(withText("Remove")).perform(click())
                awaitCheck { CineMoodRepository(context).use { repo -> assertTrue(repo.getMovies(userId).isEmpty()) } }
            }
            ActivityScenario.launch(DashboardActivity::class.java).use {
                it.onActivity { activity -> activity.findViewById<android.widget.Button>(R.id.btnLogout).performClick() }
                awaitCheck { assertEquals(-1L, session.userId) }
            }
        } finally {
            // The instrumentation process may exit immediately after cleanup.
            check(context.getSharedPreferences("tmdb_settings", 0).edit()
                .putString("read_token", oldToken).commit())
            if (oldId > 0) session.save(oldId, oldName) else session.logout()
            // Remove only this test's generated account and its cascading records.
            com.example.cinemood.data.CineMoodDbHelper(context).use { helper ->
                helper.writableDatabase.delete("users", "username = ?", arrayOf(name))
            }
        }
    }
}
