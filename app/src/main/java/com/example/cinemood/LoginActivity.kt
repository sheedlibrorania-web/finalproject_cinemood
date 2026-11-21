package com.example.cinemood

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton

class LoginActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showScreen(R.layout.activity_login)
        val username = findViewById<EditText>(R.id.etUsername)
        val password = findViewById<EditText>(R.id.etPassword)
        bindPasswordVisibility(password, findViewById<ImageButton>(R.id.btnTogglePassword))
        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            when {
                username.text.toString().isBlank() -> username.error = "Enter your username"
                password.text.toString().isBlank() -> password.error = "Enter your password"
                else -> {
                    openThroughJourney(DashboardActivity::class.java, Bundle().apply {
                        putString(EXTRA_USERNAME, username.text.toString().trim())
                    })
                    finish() // Back from the dashboard must not reopen the login form.
                }
            }
        }
        findViewById<Button>(R.id.btnRegister).setOnClickListener {
            openThroughJourney(RegisterActivity::class.java)
        }
    }
}
