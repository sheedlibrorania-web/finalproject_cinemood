package com.example.cinemood

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

class SearchActivity : BaseActivity() {
    private var requestVersion = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showScreen(R.layout.activity_search)
        backButton()
        val query = findViewById<EditText>(R.id.etQuery)
        query.setText(savedInstanceState?.getString("query") ?: "")
        fun search(requireInput: Boolean) {
            val value = query.text.toString().trim()
            if (requireInput && value.isEmpty()) { query.error = "Enter a movie title"; return }
            val version = ++requestVersion
            val label = findViewById<TextView>(R.id.tvResultCount)
            val container = findViewById<LinearLayout>(R.id.movieContainer)
            val service = com.example.cinemood.network.MovieService(this)
            label.text = "Loading movies…"
            container.removeAllViews()
            movieWork({ if (value.isBlank()) service.discover(emptyList()) else service.search(value) }, { results ->
                if (version == requestVersion) {
                    val mode = if (service.sampleMode) "Sample mode · " else "TMDB · "
                    label.text = mode + if (results.isEmpty()) "No movies found. Try another title." else "${results.size} movies"
                    renderMovies(container, results)
                }
            }, { error -> if (version == requestVersion) label.text = "$error Tap Find a film to retry." })
        }
        findViewById<Button>(R.id.btnSearch).setOnClickListener { search(true) }
        query.setOnEditorActionListener { _, action, _ ->
            if (action == EditorInfo.IME_ACTION_SEARCH) { search(true); true } else false
        }
        search(false)
    }
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("query", findViewById<EditText>(R.id.etQuery).text.toString())
        super.onSaveInstanceState(outState)
    }
}
