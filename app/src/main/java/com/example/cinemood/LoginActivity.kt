package com.example.cinemood

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton

class LoginActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showScreen(R.layout.activity_login)
        if (session.userId > 0) {
            databaseWork({ it.getUsername(session.userId) }) { savedName ->
                if (savedName != null) {
                    openThroughJourney(DashboardActivity::class.java, Bundle().apply {
                        putString(EXTRA_USERNAME, savedName)
                        putLong("user_id", session.userId)
                    })
                    finish()
                } else session.logout()
            }
        }
        val username = findViewById<EditText>(R.id.etUsername)
        val password = findViewById<EditText>(R.id.etPassword)
        bindPasswordVisibility(password, findViewById<ImageButton>(R.id.btnTogglePassword))
        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            when {
                username.text.toString().isBlank() -> username.error = "Enter your username"
                password.text.toString().isBlank() -> password.error = "Enter your password"
                else -> {
                    val name = username.text.toString().trim()
                    val secret = password.text.toString()
                    val button = findViewById<Button>(R.id.btnLogin)
                    button.isEnabled = false
                    databaseWork({ it.login(name, secret) }, failure = { button.isEnabled = true }) { userId ->
                        button.isEnabled = true
                        if (userId == null) password.error = "Invalid username or password"
                        else {
                            session.save(userId, name)
                            message("Login successful")
                            openThroughJourney(DashboardActivity::class.java, Bundle().apply {
                                putString(EXTRA_USERNAME, name)
                                putLong("user_id", userId)
                            })
                            finish()
                        }
                    }
                }
            }
        }
        findViewById<Button>(R.id.btnRegister).setOnClickListener {
            openThroughJourney(RegisterActivity::class.java)
        }
    }
}
