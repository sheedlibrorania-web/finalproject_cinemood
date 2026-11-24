package com.example.cinemood

import android.widget.Button
import android.widget.EditText
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.example.cinemood.data.CineMoodRepository
import org.junit.Assert.*
import org.junit.Test

class RegistrationWhitespaceTest {
    @Test fun rejectsWhitespaceInPasswordAndConfirmationBeforeSaving() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "invalid_space_${System.currentTimeMillis()}"
        ActivityScenario.launch(RegisterActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val username = activity.findViewById<EditText>(R.id.etUsername)
                val password = activity.findViewById<EditText>(R.id.etPassword)
                val confirm = activity.findViewById<EditText>(R.id.etConfirmPassword)
                val submit = activity.findViewById<Button>(R.id.btnCreateAccount)
                username.setText(name)
                listOf("abcd 1234", " password123", "password123 ", "abcd\t1234", "abcd\u00a01234").forEach { value ->
                    password.setText(value); confirm.setText(value); submit.performClick()
                    assertEquals(activity.getString(R.string.password_no_whitespace), password.error.toString())
                    assertFalse(activity.isFinishing)
                }
                password.setText("password123"); confirm.setText("password 123"); submit.performClick()
                assertEquals(activity.getString(R.string.password_no_whitespace), confirm.error.toString())
            }
        }
        CineMoodRepository(context).use { assertFalse(it.usernameExists(name)) }
    }
}
