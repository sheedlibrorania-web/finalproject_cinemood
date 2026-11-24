# CineMood database integration

**Update for schema version 2:** This project now includes TMDB title search, genre-based mood discovery, poster loading and local token settings. Sample mode remains available and is explicitly labeled. The watchlist now supports search, status filtering and sorting. Reviews can be deleted separately. Known original sample movie IDs migrate to negative IDs to avoid collision with real TMDB IDs. See README.md and UPDATED_TEST_RESULTS.md for current behavior and verification; the sections below record the first integration stage.

This copy of Member 1's project connects its existing interface to the three-table SQLite design developed by Rasheed. The original Downloads/CineMood.zip is unchanged. The original database source files were not available at the earlier AndroidStudioProjects path, so the helper and repository were reconstructed from the code in the project conversation and adapted to these screens. Existing accounts from another installation are not transferred by copying source files.

## Open and run

1. Extract the supplied project ZIP to a new folder.
2. Open the extracted CineMood folder in Android Studio and allow Gradle sync.
3. If Android Studio asks for the Android SDK location, select your local SDK. Do not use another member's local.properties.
4. Run the app on a device or emulator supporting API 24 or later.
5. Register a new account with a nonblank username and a nonblank password of at least eight characters. Sign in with that account.
6. Search or browse the existing sample movies, open a movie, and add it to the watchlist.
7. Change watched status, choose a rating from 1 to 5, and save notes. Return to the watchlist.
8. Stop and reopen the app, or log out and sign in again. The saved records should remain. A different account has its own collection.
9. Removing a movie asks for confirmation and removes its associated review when confirmed.

## Integration changes

- data/CineMoodDbHelper.kt creates cinemood.db with users, watchlist and reviews. Foreign keys, unique usernames, unique movies per user, allowed status values and rating bounds are enforced.
- data/CineMoodRepository.kt implements local registration/login, salted PBKDF2 password hashing, account-scoped movie CRUD and review CRUD. It understands the earlier three-part PBKDF2 SHA256 hashes. API 24/25 use PBKDF2 SHA1; later versions use SHA256.
- data/SessionManager.kt saves only the active account ID and username in private preferences. Logout clears the session, not the database.
- BaseActivity runs database operations on a single background executor and handles storage errors. Authenticated screens redirect to login when no session exists. Navigation carries user_id as well as existing movie extras.
- LoginActivity and RegisterActivity now use real database operations and input validation.
- DashboardActivity logs out without deleting saved collections.
- WatchlistActivity loads the current user's saved records and statuses.
- MovieDetailsActivity reads and writes movie status, rating and notes, prevents duplicate additions through the database, and confirms deletion.
- DemoStore retains only the sample movie catalog and mood matching. Personal records are no longer stored in its in-memory collections.

## Boundaries

The supplied project still uses Member 1's local sample movie catalog. This change does not implement TMDB networking. The existing sample IDs are catalog IDs, not verified TMDB IDs; use actual API IDs consistently when the team adds TMDB, and plan a migration for any sample records. Search and mood behavior remain as provided. Review deletion is available in the repository; the current screen removes the review with the saved movie. There is no standalone delete-review button.

## Verification

The project includes the original six local MoodSearch tests and twelve database instrumentation tests in app/src/androidTest/java/com/example/cinemood/data/DatabaseIntegrationTest.kt. Database tests use a dedicated cinemood_integration_test.db and delete only that test database between tests. They cover valid and invalid registration, case-insensitive duplicate usernames, credentials, salted hashes, movie insertion/reading, duplicate rejection, status validation/update, review CRUD, rating bounds, account isolation, cascading deletion and persistence after closing/reopening the database.

Run local tests with `gradlew.bat :app:testDebugUnitTest`. With a connected Android device, run database tests with `gradlew.bat :app:connectedDebugAndroidTest`. Database reopen tests do not replace manually checking app restart, rotation and navigation. Perform the user flow above on the final shared build and record real screenshots, defects and testing improvements for the course documentation.

## AI declaration

Codex assisted with reconstructing the database source from the earlier discussion, adapting it to the supplied screens, and adding tests and this integration note. The team must review, understand and verify the final submitted implementation. Keep actual test results and any further modifications in the project documentation.
