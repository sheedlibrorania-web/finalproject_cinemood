package com.example.cinemood

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class DashboardActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showScreen(R.layout.activity_dashboard)
        val username = intent.getStringExtra(EXTRA_USERNAME).orEmpty().ifBlank { "Movie Buff" }
        findViewById<TextView>(R.id.tvWelcome).text = "Welcome back, $username."
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
            DemoStore.clearPersonalData()
            // Remove the previous screens so Back cannot reopen them after logout.
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }
    }
}
