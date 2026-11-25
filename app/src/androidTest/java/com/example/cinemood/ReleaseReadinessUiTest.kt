package com.example.cinemood

import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.example.cinemood.data.CineMoodDbHelper
import com.example.cinemood.data.CineMoodRepository
import com.example.cinemood.data.SessionManager
import org.junit.Assert.*
import org.junit.Test

/** Real controls and SQLite: no external API or changes to another account's data. */
class ReleaseReadinessUiTest {
    private fun awaitCheck(check: () -> Unit) {
        val deadline = System.currentTimeMillis() + 15000
        var last: Throwable? = null
        while (System.currentTimeMillis() < deadline) {
            try { check(); return } catch (error: Throwable) { last = error; Thread.sleep(100) }
        }
        throw AssertionError("Watchlist did not reach expected state", last)
    }
    @Test fun watchlistSortingFilteringAndCountGrammar() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val session = SessionManager(context)
        val oldId = session.userId; val oldName = session.username
        val name = "release_qa_${System.currentTimeMillis()}"
        val user = CineMoodRepository(context).use { repo ->
            val id = requireNotNull(repo.register(name, "qa-password123"))
            repo.addMovie(id, 910001, "Zulu film", year = 2022)
            repo.addMovie(id, 910002, "Alpha film", year = 2003)
            val watched = requireNotNull(repo.addMovie(id, 910003, "Middle film", year = 2014))
            check(repo.updateStatus(id, watched, "WATCHED"))
            id
        }
        session.save(user, name)
        try {
            ActivityScenario.launch(WatchlistActivity::class.java).use { screen ->
                fun titles(): List<String> {
                    var result = emptyList<String>()
                    screen.onActivity { activity ->
                        assertTrue(activity.findViewById<NestedScrollView>(R.id.screenRoot).clipToPadding)
                        val rows = activity.findViewById<LinearLayout>(R.id.movieContainer)
                        result = (0 until rows.childCount).map { rows.getChildAt(it).findViewById<TextView>(R.id.tvRowTitle).text.toString() }
                    }
                    return result
                }
                awaitCheck { assertEquals(listOf("Middle film", "Alpha film", "Zulu film"), titles()) }
                screen.onActivity { it.findViewById<Spinner>(R.id.spinnerWatchlistSort).setSelection(1) }
                awaitCheck { assertEquals(listOf("Alpha film", "Middle film", "Zulu film"), titles()) }
                screen.onActivity { it.findViewById<Spinner>(R.id.spinnerWatchlistSort).setSelection(2) }
                awaitCheck { assertEquals(listOf("Zulu film", "Middle film", "Alpha film"), titles()) }
                screen.onActivity { it.findViewById<Spinner>(R.id.spinnerWatchlistFilter).setSelection(2) }
                awaitCheck {
                    assertEquals(listOf("Middle film"), titles())
                    screen.onActivity { assertTrue(it.findViewById<TextView>(R.id.tvEmpty).text.startsWith("1 saved film ·")) }
                }
                screen.onActivity { it.findViewById<Spinner>(R.id.spinnerWatchlistFilter).setSelection(1) }
                awaitCheck { assertEquals(listOf("Zulu film", "Alpha film"), titles()) }
                screen.onActivity { it.findViewById<EditText>(R.id.etWatchlistSearch).setText("no such film") }
                awaitCheck { assertTrue(titles().isEmpty()) }
                screen.onActivity {
                    it.findViewById<Spinner>(R.id.spinnerWatchlistFilter).setSelection(0)
                    it.findViewById<EditText>(R.id.etWatchlistSearch).setText("")
                }
                awaitCheck {
                    assertEquals(3, titles().size)
                    screen.onActivity { assertTrue(it.findViewById<TextView>(R.id.tvEmpty).text.startsWith("3 saved films ·")) }
                }
            }
        } finally {
            if (oldId > 0) session.save(oldId, oldName) else session.logout()
            CineMoodDbHelper(context).use { it.writableDatabase.delete("users", "user_id = ?", arrayOf(user.toString())) }
        }
    }
}
