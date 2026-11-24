package com.example.cinemood

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.cinemood.network.TmdbSettings

class MovieDataSettingsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showScreen(R.layout.activity_movie_data_settings)
        backButton()
        if (!BuildConfig.DEBUG) {
            val status = findViewById<TextView>(R.id.tvDataMode)
            (status.parent as android.view.View).visibility = android.view.View.GONE
            findViewById<TextView>(R.id.tvDataTitle).text = getString(R.string.about_cinemood)
            return
        }
        val settings = TmdbSettings(this)
        val token = findViewById<EditText>(R.id.etTmdbToken)
        val status = findViewById<TextView>(R.id.tvDataMode)
        fun refresh() { status.text = if (settings.sampleMode) getString(R.string.data_sample_mode) else getString(R.string.data_live_mode) }
        refresh()
        findViewById<Button>(R.id.btnSaveToken).setOnClickListener {
            val value = token.text.toString().trim().removePrefix("Bearer ")
            if (value.isBlank() || value.any { it.isWhitespace() } || value.length < 20) {
                token.error = getString(R.string.data_invalid_token)
            } else {
                settings.token = value
                token.text.clear()
                refresh()
                message(getString(R.string.data_token_saved))
            }
        }
        findViewById<Button>(R.id.btnSampleMode).setOnClickListener {
            settings.token = ""
            token.text.clear()
            refresh()
            message(getString(R.string.data_sample_mode))
        }
    }
}
