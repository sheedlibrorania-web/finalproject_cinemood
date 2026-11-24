package com.example.cinemood

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class DashboardActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showScreen(R.layout.activity_dashboard)
        val username = session.username.ifBlank { "Movie Buff" }
        findViewById<TextView>(R.id.tvWelcome).text = getString(R.string.welcome_username, username)
        findViewById<Button>(R.id.btnSearch).setOnClickListener {
            openThroughJourney(SearchActivity::class.java)
        }
        findViewById<Button>(R.id.btnMood).setOnClickListener {
            openThroughJourney(MoodActivity::class.java)
        }
        findViewById<Button>(R.id.btnWatchlist).setOnClickListener {
            openThroughJourney(WatchlistActivity::class.java)
        }
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            session.logout()
            // Remove the previous screens so Back cannot reopen them after logout.
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }
        val logout = findViewById<Button>(R.id.btnLogout)
        val container = logout.parent as android.view.ViewGroup
        val settings = Button(android.view.ContextThemeWrapper(this, R.style.CM_Secondary)).apply {
            setText(if (BuildConfig.DEBUG) R.string.developer_movie_settings else R.string.about_cinemood)
            setOnClickListener { startActivity(Intent(this@DashboardActivity, MovieDataSettingsActivity::class.java)) }
        }
        container.addView(settings, container.indexOfChild(logout))
    }
}
