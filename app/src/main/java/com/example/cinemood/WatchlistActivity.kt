package com.example.cinemood

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.EditText
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.AdapterView
import androidx.core.widget.doAfterTextChanged
import com.example.cinemood.data.MovieRecord

class WatchlistActivity : BaseActivity() {
    private var records = emptyList<MovieRecord>()
    private lateinit var query: EditText
    private lateinit var filter: Spinner
    private lateinit var sort: Spinner
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showScreen(R.layout.activity_watchlist)
        backButton()
        val container = findViewById<LinearLayout>(R.id.movieContainer)
        val parent = container.parent as LinearLayout
        val controls = layoutInflater.inflate(R.layout.view_watchlist_controls, parent, false)
        query = controls.findViewById(R.id.etWatchlistSearch)
        filter = controls.findViewById(R.id.spinnerWatchlistFilter)
        sort = controls.findViewById(R.id.spinnerWatchlistSort)
        styleDropdown(filter, R.array.watchlist_filters)
        styleDropdown(sort, R.array.watchlist_sort_orders)
        parent.addView(controls, parent.indexOfChild(container))
        query.setText(savedInstanceState?.getString("watch_query").orEmpty())
        filter.setSelection(savedInstanceState?.getInt("watch_filter") ?: 0)
        sort.setSelection(savedInstanceState?.getInt("watch_sort") ?: 0)
        query.doAfterTextChanged { renderSavedMovies() }
        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) { renderSavedMovies() }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        filter.onItemSelectedListener = listener; sort.onItemSelectedListener = listener
    }

    private fun styleDropdown(spinner: Spinner, options: Int) {
        spinner.setPopupBackgroundResource(R.drawable.cm_dropdown_popup)
        spinner.adapter = object : ArrayAdapter<String>(this, R.layout.item_watchlist_selection,
            resources.getStringArray(options).toList()) {
            override fun getDropDownView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                val view = convertView as? TextView
                    ?: layoutInflater.inflate(R.layout.item_watchlist_option, parent, false) as TextView
                view.text = getItem(position)
                view.isActivated = position == spinner.selectedItemPosition
                view.setTextColor(androidx.core.content.ContextCompat.getColor(this@WatchlistActivity,
                    if (view.isActivated) R.color.cm_accent else R.color.cm_text))
                return view
            }
        }
    }
    override fun onResume() {
        super.onResume()
        // Refresh when returning from details so changed status is visible.
        if (session.userId <= 0) return
        databaseWork({ it.getMovies(session.userId) }) { records ->
            this.records = records
            renderSavedMovies()
        }
    }

    private fun renderSavedMovies() {
        if (!::query.isInitialized || !::sort.isInitialized || !::filter.isInitialized) return
        val text = query.text.toString().trim()
        var visible = records.filter { it.movieTitle.contains(text, ignoreCase = true) && when (filter.selectedItemPosition) {
            1 -> it.status == "PLANNED"
            2 -> it.status == "WATCHED"
            else -> true
        } }
        visible = when (sort.selectedItemPosition) {
            1 -> visible.sortedBy { it.movieTitle.lowercase(java.util.Locale.ROOT) }
            2 -> visible.sortedByDescending { it.year }
            else -> visible
        }
        findViewById<TextView>(R.id.tvEmpty).text = when {
            records.isEmpty() -> "Your collection starts with one film. Find a movie in Search or Mood, then save it here."
            visible.isEmpty() -> "No saved movies match this search or filter."
            else -> resources.getQuantityString(R.plurals.saved_film_count, visible.size, visible.size)
        }
        renderMovies(findViewById(R.id.movieContainer), visible.map { it.asMovie() }, true,
            visible.filter { it.status == "WATCHED" }.map { it.tmdbMovieId.toInt() }.toSet())
    }
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("watch_query", query.text.toString())
        outState.putInt("watch_filter", filter.selectedItemPosition)
        outState.putInt("watch_sort", sort.selectedItemPosition)
        super.onSaveInstanceState(outState)
    }
}
