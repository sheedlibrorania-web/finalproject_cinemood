# CineMood CS0053 recording runbook

Target: 22–25 minutes, with live actions included. Stephen operates screen sharing. Mark can deliver his sections if available; if he is unavailable, Stephen may explain the implementation with accurate attribution. Do not impersonate Mark or imply he performed the replacement acceptance checks. Follow any course rule requiring both members to appear/speak.

Before recording: close tmdb.properties, signing dialogs, passwords and unrelated windows. Open CineMood, Android Studio project/source, the actual GitHub repository, the docs and actual test reports. Use a disposable demo account. Check microphone/screen capture with a short trial. Show the tested final APK/version and report actual results; do not show a blank-token/sample screen as live API evidence.

## 00:00–02:00 — Stephen: problem, scope and team

Show README and SRS, then the login screen.

“Good day. We are presenting CineMood for CS0053, Programming Tools and Techniques. Our application helps people choose a movie and keep track of what they want to watch and what they thought about films they have watched. The practical problem is that searching for a title and keeping a personal movie journal usually happen in separate places. CineMood connects discovery with a local personal collection.

“Our target users include students and casual movie viewers. Users can search by title, discover suggestions through moods, open movie information and posters, save films, mark them watched, and add a personal rating and notes. We intentionally excluded streaming, cloud synchronization and social networking to keep our scope achievable. Our mood feature uses transparent genre rules; it is not a machine-learning recommendation engine.

“The Kotlin/Android project was originally developed for CS0011 and reused for CS0053 with instructor approval. The original team included Rasheed Librorania, whose accounts and database contribution remains credited. Mark Cadano worked on movie discovery and the API. I worked on watchlist and integration. For CS0053, Mark and I are the two-person team. We used Codex to assist implementation, tests and documentation, and we remain responsible for understanding the source and accurately explaining the evidence.”

Point out one functional acceptance criterion and one limitation in SRS. Explain how the app will demonstrate them rather than reading the full document.

## 02:00–05:00 — Stephen: environment and approved tools

Show Android Studio source tree and About/version, then tools report and real screenshots if repeating a live demo would take too long.

“We used Android Studio with Kotlin Activities and XML layouts. This is a native Android project rather than a Java desktop application or a Compose app. The project is organized into screen classes, a data package and a network package. We use the checked-in Gradle wrapper so teammates build with the same project version. The setup guide explains the SDK, JDK and device requirements. The Gradle daemon configuration requests JDK 25; the Java source compatibility setting is a different configuration.

“Our instructor approved Android Studio tools as substitutes for the extension requirement. We describe these as IDE capabilities, not five installed VS Code extensions. Kotlin language support provides syntax highlighting, navigation and inspections. Reformat Code makes indentation consistent; our before-and-after evidence uses a disposable copy, not a fabricated application defect. Android Lint checks Android-specific issues such as resources and API compatibility. The debugger lets us pause execution and inspect the call stack and test input, while Logcat can support runtime investigation. Git integration makes branch changes and commit differences visible inside the IDE.

“Here is a real formatter demonstration. The original example has unindented functions; Reformat Code indents them. Here is the debugger evidence: the application paused during password validation with disposable input. After resuming, it displayed the expected short-password error. We do not expose a real password or API token in the recording.”

Show the captured Kotlin code-style setting, actual breakpoint/validation figures and current lint summary. Explain that eight warnings remain documented; do not say the project has no warnings.

## 05:00–08:00 — Stephen: accounts, persistence and collection

In the app, sign in using a disposable account. Show an invalid login or registration input, then complete the valid flow. Never show a real password.

“Accounts are stored locally in SQLite. Usernames are unique without regard to letter case. Registration rejects blank input and passwords shorter than eight characters or containing whitespace. Passwords are stored as salted PBKDF2 hashes rather than plaintext. Local accounts are separate from TMDB credentials: the user’s CineMood username and journal are not sent to TMDB.

“After signing in, the dashboard gives access to movie search, moods and the watchlist. Each account owns its own saved entries. Ownership checks are applied at the data-access boundary, so another account cannot read or change these records. A session keeps the current user signed in; logging out clears that session without deleting the collection.”

Open a saved film; show watched status, four-star rating and a note. Edit the note and save, leave/reopen the film to prove persistence. Explain that uninstall/clear-storage removes local data and no cloud backup/sync is implemented.

## 08:00–11:00 — Mark, or Stephen with credit: live API and moods

Search a recognizable movie title and show real TMDB label/results/posters. Switch two moods and open a film.

“Movie information comes from TMDB. Our client uses HTTPS GET requests to the movie search and discover endpoints. Title search sends an encoded query and common parameters such as language, page and adult-content exclusion. The application reads the JSON results array and converts valid entries into Movie objects. It handles missing optional information with clear fallbacks and removes duplicate or invalid IDs.

“Mood discovery maps the displayed labels to explicit genre IDs. Upbeat and Chill create different filter requests. Multiple matching moods combine inclusion rules, and exclusions do not override explicitly selected genres. It is possible for two moods to share some movies because films can have multiple genres. The current app displays the first results page; it does not claim unlimited recommendations.

“Movie posters are loaded from TMDB’s image CDN. They use a separate loader with background workers, a bounded memory cache and a placeholder for missing or failed images. The details screen opens the Movie data returned by search or saved locally. We do not claim a separate details API call that is not implemented.”

Show API_DOCUMENTATION.md request example. Explain that the bearer value is deliberately a placeholder. Open About and show the official logo/notice. Note that release users do not enter a token; the build is configured by the team and embedded credentials are an acknowledged classroom limitation.

## 11:00–14:00 — Stephen: watchlist controls and deletion

Save a movie. Show search, Watched/To watch filtering and Title A–Z/Release year sorting. Use a collection with multiple titles when demonstrating sorting.

“The watchlist stores metadata so users can reopen saved information even without repeating the movie search. The visible subset can be narrowed by title and status, and sorting changes the order without deleting records. A clear empty state explains when nothing matches. The count uses singular and plural wording correctly.

“Review deletion and movie removal are separate operations. If I delete the rating and notes, the movie remains saved. If I remove the film from the watchlist, its linked personal review is removed as well. Both destructive UI operations offer confirmation; Cancel preserves the records. I will demonstrate Cancel first so we can see that no data disappears unexpectedly.”

Demonstrate Cancel, then confirm removal for a disposable entry. If time permits, delete a review on another disposable entry. Do not delete the movie you still need for later presentation steps. Demonstrate Back near the top rather than requiring a scroll to the end.

## 14:00–17:00 — Mark, or Stephen with credit: architecture and Repository pattern

Show TECHNICAL_DESIGN.md architecture and ER diagrams, then actual saveReview/deleteMovie source.

“Our main design-pattern example is the concrete CineMoodRepository. The problem is repeated persistence rules: every screen must apply the correct account owner, validate ratings and keep related records consistent. If those SQL checks were copied into Activities, a missing predicate in one screen could expose or alter another user’s data.

“The repository offers operations such as getMovies, updateStatus, saveReview and deleteMovie. Activities ask for these operations instead of composing their own SQL. The database helper manages tables and migration; the repository handles data access and validation. BaseActivity runs database work off the main thread, scopes the repository with use, and returns UI callbacks when the screen is still active.

“In saveReview, the repository validates the rating, starts a transaction, checks ownership, inserts or updates the review, and marks the transaction successful. The finally block ends the transaction. A movie entry has at most one review. The schema’s foreign key removes that review if its saved movie is deleted. The owner predicate is also visible in deleteMovie.

“We selected Repository because it addresses our actual data-access problem. A Singleton controls instance lifetime but would not itself centralize ownership queries and review rules. Our implementation is a practical local repository, not a claim of a complete interface-based cloud-swappable architecture. That limitation is documented honestly.”

Point out one actual test for account isolation/cascade and explain why it depends on the centralized data boundary.

## 17:00–20:00 — Stephen: Git and real collaboration evidence

Show repository Issues #3–#5, merged PRs, actual branch graph and a meaningful diff. Show final version/tag only if actually created.

“We used Git locally and GitHub for repository hosting and review. We preserved the original application as a disclosed baseline instead of pretending the existing application was all written during the later course. We excluded local SDK configuration, private movie configuration, signing keys and generated outputs from source control.

“Our workflow uses focused branches, meaningful commits, push, review and merge. These Issues track actual requirements/tools, API/design work, and clean setup/release. Here are the actual pull requests and merge records. We retained earlier authorship and credit; the repository history is not rewritten to create artificial contributions.

“A commit records a local source change. A branch provides a separate line of work. A push publishes commits to the remote repository. A pull request exposes differences for review before merging. A release tag should identify the exact tested final source; it should not be created before we know which build is being submitted.”

Explain one actual commit/diff in your own words, such as API HTTP test support, the version-qualified theme fix, or the status-bar clipping correction. Do not claim Mark personally ran a check that was replaced by Codex-assisted verification.

## 20:00–23:00 — Both or Stephen: tests, manuals and limitations

Show TEST_REPORT.md, latest clean-build log and structured result summary. Run one short local test if time allows, or open actual results and clearly say they are recorded results rather than a fresh live run.

“We distinguish deterministic fixtures from real API evidence. Fake HTTP connections test status codes, timeouts and cleanup without abusing TMDB. A separate test performs real search and verifies returned data on screen. Database tests use real SQLite and dedicated disposable records. We record failures and retests instead of marking a check passed just because code exists.

“A returned Windows report tested an older ZIP without a live token. We preserved that report and identified its source mismatch. It also helped identify a genuine scrolling overlap, which was corrected and visually retested. We performed an isolated source-copy build here and clearly identify that it is the same computer with reused dependency cache, not an independent Windows test by Mark. The restart procedure has explicit save and verify stages; a default no-op must not be presented as persistence evidence.

“Our user manual follows each major feature with purpose, prerequisites, procedure, expected result, screenshot and troubleshooting. The setup guide explains the actual project root and token setup for developers. The technical manual and API document explain the source so another developer can maintain it. Our final acceptance record identifies the exact APK and any remaining scope limits.”

Show the actual current summary; read its real pass/skip counts rather than memorizing an obsolete count. Demonstrate scrolling without status-bar overlap and show About. If a signed release or approval is still pending, state that accurately rather than calling it complete.

## 23:00–24:00 — Stephen: close

“Our application brings together practical movie discovery and a personal local viewing journal. The project also demonstrates the development practices required by the course: an organized Android environment, approved development tools, real API consumption, GitHub workflow, a suitable data-access pattern, tests and maintainable documentation.

“Our limitations include local-only accounts, first-page discovery and a classroom credential-distribution approach. Future work could improve those areas after revisiting the scope and deployment design. Thank you for reviewing CineMood. We are ready to explain the code and the actual evidence behind the features demonstrated.”

## Last-minute recording checklist

- Use the latest tested APK, not the old review ZIP or stale installed app.
- Have at least three disposable saved movies so filters and sort order are visible.
- Keep real tokens, passwords and signing dialogs closed.
- Screen-share Stephen’s screen throughout; announce speaker handoffs if Mark participates.
- Check the video includes audio and readable source/app windows before uploading.
- Submit the actual recording link/file yourself through the course platform; no automatic course submission is performed by Codex.
