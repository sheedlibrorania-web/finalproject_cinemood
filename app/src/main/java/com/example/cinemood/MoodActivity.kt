package com.example.cinemood

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged

class MoodActivity : BaseActivity() {
    private var selectedMood: String? = null
    private lateinit var moodQuery: EditText
    private val moodButtons = mapOf(
        R.id.btnHappy to "Happy", R.id.btnRomantic to "Romantic", R.id.btnExcited to "Excited",
        R.id.btnScared to "Scared", R.id.btnCurious to "Curious", R.id.btnRelaxed to "Relaxed", R.id.btnSad to "Sad"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showScreen(R.layout.activity_mood)
        backButton()
        moodQuery = findViewById(R.id.etMoodQuery)
        selectedMood = savedInstanceState?.getString("mood")?.takeIf { it in moodButtons.values }
        moodQuery.setText(savedInstanceState?.getString("moodQuery").orEmpty())
        moodQuery.doAfterTextChanged {
            selectedMood = null
            renderRecommendations()
        }
        moodButtons.forEach { (id, mood) ->
            findViewById<Button>(id).setOnClickListener {
                selectMood(if (selectedMood == mood) null else mood)
            }
        }
        findViewById<Button>(R.id.btnAnyMood).setOnClickListener { selectMood(null) }
        findViewById<Button>(R.id.btnClearMoodSearch).setOnClickListener { selectMood(null) }
        findViewById<ImageButton>(R.id.btnSearchMood).setOnClickListener { submitSearch() }
        moodQuery.setOnEditorActionListener { _, action, _ ->
            if (action == EditorInfo.IME_ACTION_SEARCH) { submitSearch(); true } else false
        }
        renderRecommendations()
    }

    private fun selectMood(mood: String?) {
        moodQuery.text.clear()
        selectedMood = mood
        renderRecommendations()
    }

    private fun submitSearch() {
        renderRecommendations()
        getSystemService(InputMethodManager::class.java)
            .hideSoftInputFromWindow(moodQuery.windowToken, 0)
        moodQuery.clearFocus()
    }

    private fun renderRecommendations() {
        val query = moodQuery.text.toString().trim()
        val searching = query.isNotEmpty()
        val matches = if (searching) MoodSearch.matchingMoods(query) else listOfNotNull(selectedMood)
        val browsing = !searching && selectedMood == null
        moodButtons.forEach { (id, value) ->
            findViewById<Button>(id).isSelected = value in matches
        }
        findViewById<Button>(R.id.btnAnyMood).isSelected = browsing
        // Keep recommendations immediately below the search field while typing.
        val optionsVisibility = if (searching) View.GONE else View.VISIBLE
        findViewById<View>(R.id.moodOptions).visibility = optionsVisibility
        findViewById<View>(R.id.tvMoodOptionsLabel).visibility = optionsVisibility
        findViewById<View>(R.id.btnClearMoodSearch).visibility =
            if (moodQuery.text.isNotEmpty()) View.VISIBLE else View.GONE

        val labels = moodButtons.filterValues { it in matches }.keys
            .joinToString(", ") { findViewById<Button>(it).text }
        findViewById<TextView>(R.id.tvMoodHeading).text = when {
            browsing -> getString(R.string.mood_all_movies)
            matches.isEmpty() -> getString(R.string.mood_no_matches_title)
            else -> getString(R.string.mood_edit_heading, labels)
        }
        val movies = if (browsing) DemoStore.movies else DemoStore.forMoods(matches)
        findViewById<TextView>(R.id.tvMoodResults).text = if (searching && matches.isEmpty()) {
            getString(R.string.mood_no_matches_hint)
        } else {
            resources.getQuantityString(R.plurals.mood_recommendation_count, movies.size, movies.size)
        }
        renderMovies(findViewById<LinearLayout>(R.id.movieContainer), movies)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("mood", selectedMood)
        outState.putString("moodQuery", moodQuery.text.toString())
        super.onSaveInstanceState(outState)
    }
}
