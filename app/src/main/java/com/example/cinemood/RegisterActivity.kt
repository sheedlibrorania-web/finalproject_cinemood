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
                password.text.toString().isBlank() -> password.error = "Enter a password"
                com.example.cinemood.data.PasswordPolicy.containsWhitespace(password.text.toString()) ->
                    password.error = getString(R.string.password_no_whitespace)
                password.text.length < 8 -> password.error = "Use at least 8 characters"
                com.example.cinemood.data.PasswordPolicy.containsWhitespace(confirm.text.toString()) ->
                    confirm.error = getString(R.string.password_no_whitespace)
                password.text.toString() != confirm.text.toString() -> confirm.error = "Passwords do not match"
                else -> {
                    val name = username.text.toString().trim()
                    val secret = password.text.toString()
                    val button = findViewById<Button>(R.id.btnCreateAccount)
                    button.isEnabled = false
                    databaseWork({ it.register(name, secret) }, failure = { button.isEnabled = true }) { userId ->
                        button.isEnabled = true
                        if (userId == null) username.error = "Username already exists or input is invalid"
                        else {
                            message("Account created successfully")
                            openThroughJourney(LoginActivity::class.java)
                            finish()
                        }
                    }
                }
            }
        }
    }
}
