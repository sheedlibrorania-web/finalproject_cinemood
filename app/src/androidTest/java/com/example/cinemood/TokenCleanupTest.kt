package com.example.cinemood

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test

class TokenCleanupTest {
    @Test fun sampleUiTestsPreserveConfiguredToken() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = context.getSharedPreferences("tmdb_settings", 0)
        val original = prefs.getString("read_token", "").orEmpty()
        val sentinel = "qa-placeholder-not-a-real-token"
        try {
            assertTrue(prefs.edit().putString("read_token", sentinel).commit())
            AppFlowTest().registrationLoginWatchlistReviewAndLogout()
            assertEquals("Account flow cleanup lost token", sentinel, prefs.getString("read_token", ""))
            DiscoveryScreenTest().sampleSearchMoodWatchlistFiltersAndSettings()
            assertEquals("Discovery cleanup lost token", sentinel, prefs.getString("read_token", ""))
        } finally { check(prefs.edit().putString("read_token", original).commit()) }
    }
}
