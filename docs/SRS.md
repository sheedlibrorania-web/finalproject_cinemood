# CineMood — Software Requirements Specification

Course: CS0053, Programming Tools and Techniques. Team: Stephen Josh Canete and Mark Jayson D. Cadano. Version: 1.0 review draft, November 25, 2026. Application baseline: `7741433` (merged documentation PR #2).

**Approval status:** Stephen reports instructor approval to reuse the Kotlin/Android CineMood application from CS0011. This SRS documents the existing baseline retrospectively; it is not evidence that this document was approved before development. Stephen now reports instructor approval for the Android Studio tool substitution (November 25, 2026); the original approval record is not attached. Separate approval of this retrospective SRS has not been explicitly established. Source of course requirements: the professor’s CS0053 project manual supplied by Stephen in the project conversation.

## 1. Problem, objectives and users

Movie viewers need a convenient way to find a movie suited to their current interests and remember what they plan to watch or have already watched. Search results alone do not preserve personal viewing status, ratings or notes.

CineMood aims to (1) retrieve useful live movie information and posters, (2) support transparent mood-based discovery, (3) maintain an account-specific local watchlist and viewing journal, and (4) demonstrate maintainable Android development, real API integration, version control, testing and documentation.

Primary users are casual movie viewers, including students. Evaluators and teammates are secondary users who install, test, inspect and maintain the application. Accounts are local identities on one installation, not TMDB accounts or cloud accounts.

## 2. Scope and constraints

Included: local registration/login/logout, sessions, title search, mood discovery, movie metadata and posters, watchlist CRUD, planned/watched status, one personal rating and optional notes per saved movie, journal, list search/filter/sort, real error feedback, and About/attribution. Debug builds also support an explicitly labeled six-movie sample catalog and developer token controls.

Excluded: movie streaming, ticket purchases, social features, cloud account synchronization, password-reset email, personalized machine learning, pagination beyond the current first page, and a hosted credential proxy. No new feature or architecture migration is authorized by this SRS.

Technology: Android Studio; Kotlin Activities and XML; SQLiteOpenHelper and a repository; HttpURLConnection and JSON for TMDB. Minimum API 24, compile/target API 37, app 1.0/version code 1. Internet is required for live discovery and uncached images. Local saved metadata and personal records remain available offline. Device storage loss or uninstall removes records. APK-embedded tokens are extractable; classroom distribution does not provide server-side secret protection.

## 3. Functional requirements and acceptance criteria

Each criterion is observable behavior to verify; it is not a claim that every device/configuration has been tested.

| ID | Requirement | Acceptance criteria | Existing verification anchor |
| --- | --- | --- | --- |
| FR-01 | Register a local account | Nonblank trimmed username and password of 8+ characters without whitespace create an account; blank/short/whitespace inputs and case-insensitive duplicate usernames are rejected. | PasswordPolicyTest; DatabaseIntegrationTest; RegistrationWhitespaceTest |
| FR-02 | Authenticate and manage session | Correct credentials sign in; incorrect credentials are rejected. Logout returns to login and preserves saved records. | AppFlowTest; DatabaseIntegrationTest |
| FR-03 | Isolate accounts | Account B cannot read, update or delete account A’s saved movies/reviews. | DatabaseIntegrationTest.accountsCannotAccessEachOthersRecords |
| FR-04 | Search live movies | A nonblank title triggers a TMDB request and shows returned titles and metadata, or an understandable empty/error state. Query parameters are encoded. | AutomaticLiveAccessTest; TmdbClientTest |
| FR-05 | Recommend by mood | A recognized selection changes the discovery genre rules; Upbeat and Chill generate different requests. Results can overlap legitimately and are not guaranteed to be disjoint. | MoodSearchTest; TmdbClientTest.upbeatAndChillUseDifferentDiscoveryRequests |
| FR-06 | Display movie details and posters | Opening a movie shows available title, year, genre and overview. Posters appear where available in Search, Mood, Watchlist and Details; missing images use a fallback without blocking navigation. | AppFlowTest; DiscoveryScreenTest; visual poster check still required for final release |
| FR-07 | Save and view movies | Saving adds the movie to the current account with metadata; saving it twice does not create duplicate entries. | DatabaseIntegrationTest.movieCreateReadAndDuplicate |
| FR-08 | Change watched status | Saved movies accept PLANNED or WATCHED; unsupported status values are rejected and valid changes persist. | DatabaseIntegrationTest.statusValidationAndUpdate |
| FR-09 | Maintain personal reviews | A saved movie accepts a rating from 1–5 and optional notes; edits persist; out-of-range ratings are rejected. | DatabaseIntegrationTest.reviewCreateReadUpdateDelete; invalidRatingRejected |
| FR-10 | Delete review independently | Deleting a review removes rating/notes while keeping the saved movie. | DatabaseIntegrationTest.reviewCreateReadUpdateDelete; prior user-reported manual check |
| FR-11 | Remove a saved movie | Cancel preserves the record; confirming removal deletes the movie and its linked review. | AppFlowTest; DatabaseIntegrationTest.deleteMovieCascadesReview |
| FR-12 | Search/filter/sort watchlist | Case-insensitive title search and status filters restrict visible records; newest, title and year sorting follow the selected order; zero matches show an empty state. | DiscoveryScreenTest; WatchlistActivity; prior manual checks |
| FR-13 | Preserve local records | Reopening the database preserves movie metadata, status, ratings and notes. Logging out does not delete data. Process restart is verified separately from database reopen. | DatabaseIntegrationTest; LiveReadinessTest; AppFlowTest |
| FR-14 | Recover from service errors | Missing configuration, invalid authorization, rate limiting, timeout and network errors produce actionable messages without substituting sample movies for failed live results. | TmdbClient error branches; LiveReadinessTest invalid-token/offline checks; fresh HTTP-path fixture coverage in API_DESIGN_VERIFICATION.md; release/UI acceptance remains separate |
| FR-15 | Provide accessible navigation and journal | Back navigation is reachable without scrolling to the end; journal displays saved viewing information and logout remains available. | AppFlowTest; user-reported navigation check; final visual review |
| FR-16 | Separate build modes and attribution | Release uses the build token automatically and shows About; debug may select sample/live explicitly. Live data and images are attributed to TMDB. | AutomaticLiveAccessTest (debug build default); release source inspection; final signed APK check pending |
| FR-17 | Upgrade stored data | Schema v1 upgrades to v2 without losing accounts, review links or saved entries; original sample IDs cannot collide with TMDB IDs. | DatabaseMigrationTest |

## 4. Nonfunctional requirements

| ID | Category | Requirement and acceptance method |
| --- | --- | --- |
| NFR-01 | Usability | Consistent light theme, readable labels and understandable validation/empty states. Check the main flows visually on the release device and with larger text; a full accessibility audit is pending. |
| NFR-02 | Responsiveness | Do not add artificial navigation delays. Database/network work should not block the main UI thread. The TMDB client configures 12-second connect and read timeouts separately; this is not a guaranteed 12-second total operation limit. Measure representative local navigation and live request behavior before claiming a performance target is met. |
| NFR-03 | Reliability | Invalid input, failed API calls and absent optional metadata must not crash the app. Execute unit, instrumentation and error-path checks; record uncovered cases. |
| NFR-04 | Security | Salted PBKDF2 password hashes, parameterized queries and ownership checks protect basic local operations. Do not commit real credentials, local settings or signing keys. Review the final tree/history before publishing; no penetration-test or encrypted-database claim is made. |
| NFR-05 | Maintainability | Keep Activities, data and network concerns organized; reuse repository operations and Android resources. Apply the agreed conventions and review focused pull requests. |
| NFR-06 | Portability | Build from the documented wrapper and SDK requirements; run on API 24+. API-24-specific runtime verification remains pending; the recorded device tests used API 37.1. |
| NFR-07 | Testability | Core password/mood behavior has repeatable unit tests; repository, migration and screen flows have instrumentation tests. Link actual outcomes to the tested baseline and device. |
| NFR-08 | Data integrity | Enforce unique per-account saves and one review per entry; transactions and foreign keys maintain related records. Execute duplicate, ownership, cascade and migration tests. |

## 5. Acceptance, evidence and change control

Existing executed baseline results and limitations are in [quality/pre-push-review.md](quality/pre-push-review.md) and [quality/instrumentation-results.csv](quality/instrumentation-results.csv). Current tool evidence is indexed in [DEVELOPMENT_TOOLS.md](DEVELOPMENT_TOOLS.md). These test-class anchors identify coverage; they do not replace a complete final test report with actual results per requirement.

Final acceptance requires the working signed application, real TMDB response, documented clean setup, linked test evidence, course tool/Git/pattern evidence, reviewed manuals, an accessible repository and a final release tag. Track gaps in [CS0053_COMPLIANCE.md](CS0053_COMPLIANCE.md). Mark reviews the SRS for agreement with actual discovery behavior; Stephen reviews local data/navigation requirements. Obtain instructor SRS acceptance explicitly and record date/location of evidence. Do not backdate approval or present reconstructed planning as a pre-development artifact.

New features, deleted features or architecture changes require team/user agreement before implementation. Documentation corrections may proceed through a reviewed documentation branch.

## 6. Responsibilities and acknowledgment

| Person | Original CS0011 contribution | CS0053 responsibility |
| --- | --- | --- |
| Stephen Josh Canete | Watchlist, status, ratings/notes and integration | Requirements/compliance, tools/setup evidence, local data and integration verification, Git coordination and presentation. |
| Mark Jayson D. Cadano | TMDB discovery, search/details, moods and API errors | API/pattern technical review and documentation, independent setup verification, review/testing and presentation. |
| Rasheed L. Librorania | Accounts, SQLite schema, relationships and persistence | Credited original contributor; not represented as a CS0053 group member. |

CS0053 responsibilities are the proposed working division and should be confirmed in the team review. Git history and review records must reflect actual work. Codex assisted with implementation, testing and these documents; students retain responsibility for accuracy, verification and explaining the submitted source.
