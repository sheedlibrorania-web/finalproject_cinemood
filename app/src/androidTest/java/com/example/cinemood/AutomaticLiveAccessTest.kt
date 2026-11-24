package com.example.cinemood

import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import android.widget.TextView
import android.widget.LinearLayout
import com.example.cinemood.data.CineMoodRepository
import com.example.cinemood.data.CineMoodDbHelper
import com.example.cinemood.data.SessionManager
import com.example.cinemood.network.MovieService
import org.junit.Assert.*
import org.junit.Test

class AutomaticLiveAccessTest {
    @Test fun searchWorksWithoutDeviceTokenSetup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = context.getSharedPreferences("tmdb_settings", 0)
        val hadOverride = prefs.contains("read_token")
        val oldOverride = prefs.getString("read_token", "").orEmpty()
        val session = SessionManager(context)
        val oldId = session.userId; val oldName = session.username
        val username = "automatic_live_${System.currentTimeMillis()}"
        try {
            assertTrue(prefs.edit().remove("read_token").commit())
            val service = MovieService(context)
            assertFalse("Build must supply automatic live access", service.sampleMode)
            val results = service.search("Interstellar")
            assertTrue(results.any { it.title == "Interstellar" && it.source == "TMDB" })
            val user = CineMoodRepository(context).use { requireNotNull(it.register(username, "password123")) }
            session.save(user, username)
            ActivityScenario.launch(SearchActivity::class.java).use { scenario ->
                scenario.onActivity { activity ->
                    activity.findViewById<android.widget.EditText>(R.id.etQuery).setText("Interstellar")
                    activity.findViewById<android.widget.Button>(R.id.btnSearch).performClick()
                }
                val deadline = System.currentTimeMillis() + 20000
                var loaded = false
                while (!loaded && System.currentTimeMillis() < deadline) {
                    scenario.onActivity { activity ->
                        loaded = activity.findViewById<TextView>(R.id.tvResultCount).text.startsWith("TMDB") &&
                            activity.findViewById<LinearLayout>(R.id.movieContainer).childCount > 0
                    }
                    if (!loaded) Thread.sleep(100)
                }
                assertTrue("Search screen must show actual TMDB results", loaded)
            }
        } finally {
            val edit = prefs.edit()
            if (hadOverride && oldOverride.isNotBlank()) edit.putString("read_token", oldOverride)
            else edit.remove("read_token") // Leave this verification device on automatic build configuration.
            check(edit.commit())
            if (oldId > 0) session.save(oldId, oldName) else session.logout()
            CineMoodDbHelper(context).use { it.writableDatabase.delete("users", "username = ?", arrayOf(username)) }
        }
    }
}
