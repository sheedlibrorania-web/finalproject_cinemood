package com.example.cinemood.data

/** Registration policy. Reject whitespace instead of silently changing the password. */
object PasswordPolicy {
    fun containsWhitespace(password: String): Boolean = password.any { it.isWhitespace() }
    fun isValid(password: String): Boolean = password.length >= 8 && !containsWhitespace(password)
}
