package com.example.cinemood

import com.example.cinemood.data.PasswordPolicy
import org.junit.Assert.*
import org.junit.Test

class PasswordPolicyTest {
    @Test fun acceptsEightOrMoreCharactersWithoutWhitespace() {
        assertTrue(PasswordPolicy.isValid("abcd1234"))
        assertTrue(PasswordPolicy.isValid("CineMood!123"))
    }
    @Test fun rejectsBlankAndShortPasswords() {
        listOf("", "       ", "1234567").forEach { assertFalse(PasswordPolicy.isValid(it)) }
    }
    @Test fun rejectsSpacesAnywhereRatherThanCountingOrTrimmingThem() {
        listOf("abcd 1234", " password123", "password123 ", "1234 567", "a b c d e f").forEach {
            assertFalse("Should reject '$it'", PasswordPolicy.isValid(it))
        }
    }
    @Test fun rejectsTabsNewlinesAndUnicodeWhitespace() {
        listOf("abcd\t1234", "abcd\n1234", "abcd\r1234", "abcd\u00a01234", "abcd\u20031234").forEach {
            assertFalse(PasswordPolicy.isValid(it))
        }
    }
}
