# Database verification results

Verified on November 22, 2026 (Asia/Manila) against the integrated project supplied in this folder.

| Check | Result |
| --- | --- |
| Debug APK build | Passed |
| Android test APK build | Passed |
| Existing MoodSearch local tests | 6 passed, 0 failures |
| DatabaseIntegrationTest on Pixel_10_Pro, Android 17 | 12 passed |
| Launch LoginActivity on emulator | Passed; Activity Manager reported Status: ok |

The initial Gradle device-test attempt could not install while Android was booting. A later Gradle report listed zero tests and was not accepted as evidence of a pass. After boot completed, both APKs were installed explicitly and the test class was executed directly:

```text
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell am instrument -w -e class com.example.cinemood.data.DatabaseIntegrationTest com.example.cinemood.test/androidx.test.runner.AndroidJUnitRunner

Time: 25.597
OK (12 tests)
```

## Database tests executed

1. Valid registration and login with trimmed username.
2. Reject blank username, short password and whitespace-only password.
3. Reject duplicate username regardless of capitalization.
4. Reject wrong password and empty credentials.
5. Store distinct salted hashes rather than plaintext for two identical passwords.
6. Create/read a saved movie; reject duplicates and blank movie titles.
7. Reject invalid status and persist WATCHED status.
8. Create/read/update/delete a review while preserving its ID during update.
9. Reject ratings below 1 or above 5.
10. Prevent another user from reading, updating or deleting the owner's records.
11. Delete a movie and verify the associated review is removed by foreign-key cascade.
12. Close and reopen the database and verify credentials, saved movie and review remain.

These are repository tests and a startup check. They are not a claim that every visual interaction, rotation, app-process restart, session transition, device version or TMDB behavior has been tested. Follow DATABASE_INTEGRATION.md to manually verify the final group workflow and capture screenshots for submission.
