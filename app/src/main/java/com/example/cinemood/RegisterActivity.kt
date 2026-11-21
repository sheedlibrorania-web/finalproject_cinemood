package com.example.cinemood

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton

class RegisterActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showScreen(R.layout.activity_register)
        backButton()
        val username = findViewById<EditText>(R.id.etUsername)
        val password = findViewById<EditText>(R.id.etPassword)
        val confirm = findViewById<EditText>(R.id.etConfirmPassword)
        bindPasswordVisibility(password, findViewById<ImageButton>(R.id.btnTogglePassword))
        bindPasswordVisibility(
            confirm, findViewById<ImageButton>(R.id.btnToggleConfirmPassword),
            R.string.show_confirm_password, R.string.hide_confirm_password
        )
        findViewById<Button>(R.id.btnCreateAccount).setOnClickListener {
            when {
                username.text.toString().isBlank() -> username.error = "Enter a username"
                password.text.length < 6 -> password.error = "Use at least 6 characters"
                password.text.toString() != confirm.text.toString() -> confirm.error = "Passwords do not match"
                else -> {
                    message("Account created successfully")
                    openThroughJourney(LoginActivity::class.java)
                    finish() // Return to the existing LoginActivity after the loading page.
                }
            }
        }
    }
}
