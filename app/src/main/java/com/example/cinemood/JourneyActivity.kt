package com.example.cinemood

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView

/** Animated transition for the local demo screens; pauses when the app leaves the foreground. */
class JourneyActivity : BaseActivity() {
    private var hasContinued = false
    private var progressValue = 0
    private var progressAnimation: ValueAnimator? = null
    private var reelAnimation: ObjectAnimator? = null
    private val target: String get() = intent.getStringExtra(EXTRA_JOURNEY_TARGET).orEmpty()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showScreen(R.layout.activity_journey)
        progressValue = savedInstanceState?.getInt("progress") ?: 0
        val title = when (target) {
            DashboardActivity::class.java.name -> "Your movie space awaits"
            LoginActivity::class.java.name -> "Back to your movie space"
            RegisterActivity::class.java.name -> "Your story starts here"
            SearchActivity::class.java.name -> "A new favourite awaits"
            MoodActivity::class.java.name -> "Find tonight’s feeling"
            WatchlistActivity::class.java.name -> "Your saved stories await"
            MovieDetailsActivity::class.java.name -> "Into the story"
            else -> "Setting the scene…"
        }
        findViewById<TextView>(R.id.tvJourneyTitle).text = title
        findViewById<Button>(R.id.btnContinue).setOnClickListener { continueToTarget() }
        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        if (hasContinued) return
        if (progressValue >= 100) { continueToTarget(); return }
        val progress = findViewById<ProgressBar>(R.id.loadingProgress)
        val status = findViewById<TextView>(R.id.tvJourneyStatus)
        // A short visual transition: all current movie data is already stored locally.
        progressAnimation = ValueAnimator.ofInt(progressValue, 100).apply {
            duration = ((100 - progressValue) * 23.5).toLong()
            interpolator = LinearInterpolator()
            addUpdateListener { animation ->
                progressValue = animation.animatedValue as Int
                progress.progress = progressValue
                val message = when {
                    progressValue < 35 -> "Setting the mood…"
                    progressValue < 70 -> "Turning to your next scene…"
                    progressValue < 100 -> "Almost there…"
                    else -> "Ready when you are."
                }
                if (status.text.toString() != message) status.text = message
                if (progressValue == 100) continueToTarget()
            }
            start()
        }
        reelAnimation = ObjectAnimator.ofFloat(findViewById<View>(R.id.loadingReel), View.ROTATION, 0f, 360f).apply {
            duration = 8000L
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE
            start()
        }
    }

    override fun onPause() {
        progressAnimation?.cancel()
        reelAnimation?.cancel()
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("progress", progressValue)
        super.onSaveInstanceState(outState)
    }

    private fun continueToTarget() {
        if (hasContinued || isFinishing) return
        val destination = when (target) {
            DashboardActivity::class.java.name -> Intent(this, DashboardActivity::class.java)
            LoginActivity::class.java.name -> Intent(this, LoginActivity::class.java)
            RegisterActivity::class.java.name -> Intent(this, RegisterActivity::class.java)
            SearchActivity::class.java.name -> Intent(this, SearchActivity::class.java)
            MoodActivity::class.java.name -> Intent(this, MoodActivity::class.java)
            WatchlistActivity::class.java.name -> Intent(this, WatchlistActivity::class.java)
            MovieDetailsActivity::class.java.name -> Intent(this, MovieDetailsActivity::class.java)
            else -> null
        } ?: run {
            message("The next screen is unavailable")
            finish()
            return
        }
        hasContinued = true
        progressAnimation?.cancel()
        intent.getBundleExtra(EXTRA_JOURNEY_EXTRAS)?.let { destination.putExtras(it) }
        destination.putExtra("user_id", session.userId)
        if (target == LoginActivity::class.java.name) {
            destination.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(destination)
        finish()
    }
}
