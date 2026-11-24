# CineMood

CineMood is a Kotlin Android app for mood-based movie discovery and a personal viewing journal. It uses a local SQLite database for accounts, watchlist entries, watched status, ratings and notes.

## Features

- Registration, login, saved sessions and logout.
- Salted PBKDF2 password hashes; case-insensitive unique usernames.
- Movie title search, details, poster display and mood recommendations.
- Live TMDB mode with a locally supplied API Read Access Token.
- Explicitly labeled sample mode with six movies, available without a token or internet.
- Account-specific saved movies, watched status, ratings from 1 to 5 and notes.
- Edit reviews, delete reviews separately, or remove a saved movie with its review after confirmation.
- Search, status filtering and sorting in the watchlist.
- Persistent saved movie details so the watchlist and viewing journal remain available offline.
- Loading, validation, empty-result and connection-error feedback.

## Run the project

1. Extract the project into a new folder and open the CineMood folder in Android Studio.
2. Allow Gradle sync. Use the Android SDK installed on your own computer; local.properties is deliberately excluded from the source ZIP.
3. Run on an emulator or phone with Android API 24 or later.
4. Register an account with a nonblank username and a password of at least 8 characters with no spaces or other whitespace, then sign in.
5. Use Search or Mood to try the sample catalog immediately. Add a movie, mark it watched and save a rating and notes.
6. In the dashboard, open Movie data settings to configure live TMDB mode if desired.

## Live TMDB setup

**Updated school release setup:** See TMDB_BUILD_SETUP.md. Release APKs use developer-supplied build configuration and expose About CineMood instead of token controls. The per-device instructions below apply only to debug development builds. Credentials compiled into an APK are extractable; this approach is for the classroom demonstration.

The app runs without a TMDB credential in sample mode. Real movie search requires your own API Read Access Token from https://www.themoviedb.org/settings/api. Obtain the token through your own TMDB account, following its registration process and terms. Paste it into Movie data settings and choose Save token and enable live movies. This expects the Read Access Token, not the shorter API key.

The credential is stored only in the app's private preferences and is not part of the source or APK build configuration. It is not displayed again after saving. The app uses it only in the Authorization header for https://api.themoviedb.org. Switching back to sample mode removes the saved token. Live search checks whether the token is accepted; saving it locally is not a claim that it is valid. Do not publish tokens in GitHub, screenshots or documentation.

Movie suggestions use a transparent mood-to-genre mapping; they are not machine learning or mental health advice. Search and discovery currently show the first page of TMDB results. An API or network failure shows an error and does not silently substitute sample results.

This product uses the TMDB API but is not endorsed or certified by TMDB. Movie information and images in live mode come from https://www.themoviedb.org. Credits are also available inside Movie data settings.

## Database

| Table | Purpose |
| --- | --- |
| users | Local account name, password hash and creation time. |
| watchlist | Owner, movie ID, title, overview, poster path, year, genre, source and viewing status. |
| reviews | One personal rating and optional notes per watchlist entry. |

One user has many watchlist records; a watchlist record has zero or one review. Unique constraints prevent duplicate usernames and duplicate movie saves for the same account. Foreign keys remove the related review when a movie is deleted. Logging out clears the session and preserves the database.

Schema version 2 upgrades version 1 with saved movie metadata. Known original sample IDs are migrated to negative IDs so they cannot collide with real TMDB IDs. Account IDs, watchlist IDs and review links are retained.

## Collaboration

Use this complete Android Studio project as the shared project. Make focused changes on feature branches, commit under each member's own account, and have another member review each pull request. Test the combined build before merging. Do not commit local.properties, credentials, emulator databases or generated build folders.

## Verification and AI use

See DATABASE_TEST_RESULTS.md for executed checks and limits, and DATABASE_INTEGRATION.md for the original integration history. New instrumentation tests cover TMDB JSON parsing and request construction, database migration and the real register/login/movie/review/logout screen flow. A live call with the team's own token must be verified separately when that credential is available.

Codex assisted with the database integration, validation, TMDB client, tests and documentation. Students remain responsible for reviewing the code, verifying the final submission and explaining how it works.
