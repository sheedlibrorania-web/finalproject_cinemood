package com.example.cinemood

import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.example.cinemood.data.CineMoodDbHelper
import com.example.cinemood.data.CineMoodRepository
import com.example.cinemood.data.SessionManager
import com.example.cinemood.network.TmdbSettings
import org.junit.Assert.*
import org.junit.Test

class DiscoveryScreenTest {
    private fun awaitCheck(check: () -> Unit) {
        val deadline = System.currentTimeMillis() + 15000
        var error: Throwable? = null
        while (System.currentTimeMillis() < deadline) {
            try { check(); return } catch (e: Throwable) { error = e; Thread.sleep(150) }
        }
        throw AssertionError("Screen condition timed out", error)
    }
    @Test fun sampleSearchMoodWatchlistFiltersAndSettings() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val session = SessionManager(context)
        val oldId = session.userId; val oldName = session.username
        val settings = TmdbSettings(context); val oldToken = settings.token
        val name = "discovery_test_${System.currentTimeMillis()}"
        val uid = CineMoodRepository(context).use { requireNotNull(it.register(name, "password123")) }
        session.save(uid, name); settings.token = ""
        try {
            ActivityScenario.launch(SearchActivity::class.java).use { scenario ->
                scenario.onActivity {
                    it.findViewById<EditText>(R.id.etQuery).setText("Interstellar")
                    it.findViewById<Button>(R.id.btnSearch).performClick()
                }
                awaitCheck { scenario.onActivity {
                    assertTrue(it.findViewById<TextView>(R.id.tvResultCount).text.contains("Sample mode"))
                    assertEquals(1, it.findViewById<LinearLayout>(R.id.movieContainer).childCount)
                } }
                scenario.onActivity {
                    it.findViewById<EditText>(R.id.etQuery).setText("unfindable_12345")
                    it.findViewById<Button>(R.id.btnSearch).performClick()
                }
                awaitCheck { scenario.onActivity { assertTrue(it.findViewById<TextView>(R.id.tvResultCount).text.contains("No movies found")) } }
            }
            ActivityScenario.launch(MoodActivity::class.java).use { scenario ->
                scenario.onActivity { it.findViewById<Button>(R.id.btnHappy).performClick() }
                awaitCheck { scenario.onActivity {
                    assertTrue(it.findViewById<TextView>(R.id.tvMoodResults).text.contains("Sample mode"))
                    assertEquals(2, it.findViewById<LinearLayout>(R.id.movieContainer).childCount)
                } }
            }
            CineMoodRepository(context).use { repo ->
                val movie = DemoStore.movies.first()
                val id = requireNotNull(repo.addMovie(uid, movie.id.toLong(), movie.title, source = movie.source))
                repo.updateStatus(uid, id, "WATCHED")
            }
            ActivityScenario.launch(WatchlistActivity::class.java).use { scenario ->
                awaitCheck { scenario.onActivity { assertEquals(1, it.findViewById<LinearLayout>(R.id.movieContainer).childCount) } }
                val directory = java.io.File(context.getExternalFilesDir(null), "qa").apply { mkdirs() }
                fun capture(name: String) {
                    val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot() ?: return
                    java.io.FileOutputStream(java.io.File(directory, "$name.png")).use {
                        bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
                    }
                    bitmap.recycle()
                }
                scenario.onActivity { it.findViewById<EditText>(R.id.etWatchlistSearch).requestFocus() }
                capture("watchlist-controls")
                scenario.onActivity { it.findViewById<Spinner>(R.id.spinnerWatchlistFilter).performClick() }
                InstrumentationRegistry.getInstrumentation().waitForIdleSync()
                capture("watchlist-filter-menu")
                androidx.test.espresso.Espresso.pressBack()
                scenario.onActivity { activity ->
                    activity.findViewById<EditText>(R.id.etWatchlistSearch).setText("no matching title")
                    assertEquals(0, activity.findViewById<LinearLayout>(R.id.movieContainer).childCount)
                    activity.findViewById<EditText>(R.id.etWatchlistSearch).setText("")
                    activity.findViewById<Spinner>(R.id.spinnerWatchlistFilter).setSelection(1)
                }
                awaitCheck { scenario.onActivity { assertEquals(0, it.findViewById<LinearLayout>(R.id.movieContainer).childCount) } }
            }
            ActivityScenario.launch(MovieDataSettingsActivity::class.java).use { scenario ->
                scenario.onActivity {
                    assertTrue(it.findViewById<TextView>(R.id.tvDataMode).text.contains("Sample mode"))
                    it.findViewById<EditText>(R.id.etTmdbToken).setText("short")
                    it.findViewById<Button>(R.id.btnSaveToken).performClick()
                    assertNotNull(it.findViewById<EditText>(R.id.etTmdbToken).error)
                    assertTrue(settings.sampleMode)
                }
            }
        } finally {
            check(context.getSharedPreferences("tmdb_settings", 0).edit()
                .putString("read_token", oldToken).commit())
            if (oldId > 0) session.save(oldId, oldName) else session.logout()
            CineMoodDbHelper(context).use { it.writableDatabase.delete("users", "username = ?", arrayOf(name)) }
        }
    }
}
