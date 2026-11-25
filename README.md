# CineMood

CineMood is a native Android app for discovering movies by title or mood and keeping a personal watchlist and viewing journal. It uses Kotlin Activities, XML layouts, local SQLite storage and the TMDB API.

## Features

- Local registration, login, saved sessions and logout.
- Salted PBKDF2 password hashes and case-insensitive unique usernames.
- Live movie search, mood-to-genre recommendations, details and posters.
- Account-specific watchlists with watched status, 1–5 ratings and notes.
- Edit or delete a review separately; remove a saved movie after confirmation.
- Watchlist search, status filters and sorting.
- Saved movie metadata and reviews available offline.
- Input validation, loading, empty-state and network-error messages.

Accounts and records stay on the device; there is no cloud account sync. CineMood helps users discover and track movies; it does not stream them.

## Quick start

1. Clone the repository and open its root folder in Android Studio:

   ```bash
   git clone https://github.com/sheedlibrorania-web/finalproject_cinemood.git
   cd finalproject_cinemood
   git switch master
   ```

2. Allow Gradle sync and install the requested SDK components. Use Android Studio’s bundled JDK for Gradle; the project compiles against Android SDK 37 and runs on API 24 or later.
3. For live movie access, copy `tmdb.properties.example` to `tmdb.properties` in the project root. Fill in `TMDB_READ_ACCESS_TOKEN` locally with an authorized TMDB API Read Access Token, then rebuild. Do not commit that file.
4. Choose the `app` run configuration and an emulator or connected phone, then click **Run**.
5. Register a local account, sign in and search for a movie. Passwords require at least eight characters and must contain no whitespace.

A debug build without a token uses the explicitly labeled six-movie sample catalog. Release builds require a token at packaging time and use live access automatically; end users see About CineMood instead of credential controls. Network failures do not silently switch to sample data.

See [Setup and troubleshooting](docs/SETUP.md) for detailed instructions and [TMDB build configuration](TMDB_BUILD_SETUP.md) for token precedence and release behavior.

## Project structure

```text
app/src/main/java/com/example/cinemood/
  *Activity.kt       Screens and interaction handlers
  data/             SQLite helper, repository, password policy and sessions
  network/          TMDB requests and poster loading
app/src/main/res/   XML layouts, themes, text and drawable resources
app/src/test/       Local unit tests
app/src/androidTest/ Device/emulator tests
/docs/              Setup guide and recorded quality evidence
```

The app uses Activities/XML rather than Jetpack Compose. `CineMoodRepository` centralizes local data access and validation; `CineMoodDbHelper` manages the SQLite schema and upgrades.

## Database

| Table | Purpose |
| --- | --- |
| users | Local username, password hash and creation time. |
| watchlist | Owner, movie ID, saved metadata, source and viewing status. |
| reviews | One rating and optional notes per watchlist entry. |

One user owns many watchlist entries, each with zero or one review. Constraints prevent duplicate usernames and duplicate movie saves within an account. Deleting a movie also removes its review; logging out preserves records. Schema version 2 adds saved movie metadata and migrates original sample IDs to negative values to avoid collisions with TMDB IDs.

## Build and test

From the project root with the Gradle JDK configured:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest :app:lintDebug
# Requires a running emulator or connected test device:
./gradlew :app:connectedDebugAndroidTest
```

On Windows, use `gradlew.bat` instead of `./gradlew`. Instrumentation tests can change app data; use a dedicated test emulator. Live checks require an authorized token and internet access. See [SETUP.md](docs/SETUP.md) for test prerequisites and output locations.

The November 25, 2026 baseline review recorded 10 passing unit tests, 29 passing instrumentation tests, and zero lint errors with eight remaining warnings. These are historical executed results, not a guarantee for subsequent changes. The restart-checkpoint test is phase-gated; this review did not execute a separate two-process restart experiment. Details: [quality review](docs/quality/pre-push-review.md), [per-test results](docs/quality/instrumentation-results.csv), [earlier database checks](DATABASE_TEST_RESULTS.md).

## Team and course context

CineMood began as the three-person CS0011 project and is reused for CS0053 with instructor approval. Original contributions remain credited:

- **Mark Jayson D. Cadano:** movie discovery, TMDB integration, search, details and mood recommendations.
- **Rasheed L. Librorania:** accounts, SQLite schema and persistence.
- **Stephen Josh Canete:** watchlist, viewing journal and feature integration.

The CS0053 team is Stephen and Mark. Existing application work is the starting baseline; later CS0053 work and reviews should be recorded honestly in GitHub. Reuse approval does not establish completion of every course evidence requirement.

Use focused branches, meaningful commits under each contributor’s own identity, and reviewed pull requests into `master`. Do not commit credentials, signing keys, local IDE configuration or generated builds. Stephen reports specific instructor approval for Android Studio tool substitutions; the capabilities, actual demonstrations and approval-record limitations are documented in [the tools report](docs/DEVELOPMENT_TOOLS.md).

## Limitations and credits

- Mood recommendations use explicit genre rules, not machine learning. Discovery and search currently show the first page of results.
- Local accounts do not synchronize across devices. Uninstalling or clearing app storage removes local accounts and records.
- A build-time TMDB token is extractable from an APK. This is the team’s classroom distribution approach; a backend is a future option for public distribution.
- This product uses the TMDB API but is not endorsed or certified by TMDB. Live movie information and images come from [TMDB](https://www.themoviedb.org/); attribution is also shown in the app.
- Codex assisted with implementation, tests and documentation. Team members must review, verify and be able to explain submitted work. Original integration history is recorded in [DATABASE_INTEGRATION.md](DATABASE_INTEGRATION.md).

## CS0053 review documents

- [Software Requirements Specification](docs/SRS.md) — current scope and acceptance criteria; approval pending.
- [Course compliance register](docs/CS0053_COMPLIANCE.md) — evidence links, responsibilities and remaining gaps.
- [Development tools and standards](docs/DEVELOPMENT_TOOLS.md) — actual captures, conventions and pending demonstrations.

## API and design review

- [TMDB integration manual](docs/API_DOCUMENTATION.md) — implemented requests, parsing, mood rules, posters and error handling.
- [Technical design](docs/TECHNICAL_DESIGN.md) — architecture, data flow, schema and decisions.
- [Repository pattern](docs/DESIGN_PATTERNS.md) — source walkthrough, diagram, alternatives and behavior evidence.
- [API/design verification](docs/API_DESIGN_VERIFICATION.md) — 30 executed instrumentation checks, live search and release-assembly results, with limitations.

## User guide and pre-release quality

- [User manual](docs/USER_MANUAL.md) — installation, feature procedures, actual screenshots and troubleshooting.
- [Test and quality report](docs/TEST_REPORT.md) — current 40-method instrumentation result, SRS traceability, known limits and signed-release acceptance checklist.
- [Test-case matrix](docs/TEST_CASES.csv) — expected outcomes and actual status by requirement.
- [Mark’s clean-setup record](docs/CLEAN_SETUP_CHECK.md) — pending actual independent results.
- [Official TMDB asset provenance](docs/assets/README.md).
