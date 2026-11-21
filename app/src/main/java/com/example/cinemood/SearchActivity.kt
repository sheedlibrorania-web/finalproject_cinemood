package com.example.cinemood

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

class SearchActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showScreen(R.layout.activity_search)
        backButton()
        val query = findViewById<EditText>(R.id.etQuery)
        query.setText(savedInstanceState?.getString("query") ?: "")
        fun search(requireInput: Boolean) {
            val value = query.text.toString().trim()
            if (requireInput && value.isEmpty()) { query.error = "Enter a movie title"; return }
            val results = DemoStore.movies.filter { it.title.contains(value, ignoreCase = true) }
            findViewById<TextView>(R.id.tvResultCount).text = if (results.isEmpty()) "No sample movies found." else "${results.size} sample movies"
            renderMovies(findViewById<LinearLayout>(R.id.movieContainer), results)
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
