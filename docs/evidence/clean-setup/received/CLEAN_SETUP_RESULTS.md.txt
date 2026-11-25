# CineMood clean setup review

**Overall result: not a clean pass.** The debug app builds, launches, and passes the local account, database, and sample movie checks. Lint fails with **1 error and 115 warnings**. Live TMDB success and signed release acceptance remain unverified. No application source was changed during this review.

## Review identity and setup

- Review performed by Codex for the requester, using `C:\Users\bluek\Downloads\CLEAN_SETUP_CHECK.md`. This is an automated assisted review, not a statement that Mark personally performed or signed the checklist.
- Date: 25 November 2026, Asia/Singapore. Local testing ran approximately 03:53 to 04:18.
- Downloaded project: `C:\Users\bluek\Downloads\Canete_Cadano_Librorania_CineMood\CineMood`.
- Isolated test copy: `review-work\CineMood`, relative to the outer downloaded folder. The test copy uses the same 87 source/configuration files; [source identity](evidence/clean-setup/source-identity.json) reports zero differences. The SHA-256 [file manifest](evidence/clean-setup/source-sha256.txt) identifies this download.
- Repository URL and commit: unavailable. This extracted folder has no `.git` metadata, so cloning and commit identity could not be verified.
- OS: Windows 11, build `10.0.26300.0`, amd64.
- Android Studio: `AI-261.26222.65.2614.16204760` / `AndroidStudio2026.1.4`.
- Gradle: 9.6.0; Android Gradle Plugin: 9.4.1. Gradle JDK: Android Studio JBR OpenJDK 25.0.3. The project explicitly requests a Java 25 Gradle daemon; Java source compatibility 11 is a separate setting.
- Android SDK: compile/target API 37; min API 24. SDK 37.0 and 37.1 and Build Tools 36.0.0 are installed.
- Test device: fresh `CineMood_Review` emulator, based on the existing Pixel 6 configuration, x86_64, Android 17 / API 37, system image `android-37.1/google_apis_ps16k`. Default display 1080 x 2400, density 420, font scale 1.0.
- Authorized token configured: **no** at build/test time. No token value was recorded or invented.

## Checks before reviewing the app

Android Studio initially had the **outer** `Canete_Cadano_Librorania_CineMood` folder open. It displayed manifest errors and “Add Configuration” because that folder is not the Gradle project root. Opening the inner `CineMood` folder in a separate Studio window completed sync and produced the `app` Run configuration. Those initial editor errors should not be treated as confirmed source compilation errors.

The absence of `local.properties` and `tmdb.properties` from the download is intentional. Both are excluded by `.gitignore`. A local SDK path was configured only in the test copy. The supplied Gradle wrapper JAR and scripts are present. No phone was connected; an existing emulator definition was found and a separate fresh test emulator was created without using its saved user data.

The checklist references `SETUP.md`, but the file is missing. The README also references missing `DATABASE_TEST_RESULTS.md` and `DATABASE_INTEGRATION.md`. `TMDB_BUILD_SETUP.md` and `tmdb.properties.example` are present. Missing documents prevent following the exact promised setup/test instructions.

## Checklist result record

| Step | Actual result and evidence | Status |
| --- | --- | --- |
| Clone and open project root using README | Fresh copy of the supplied ZIP contents tested. Inner project root opens and syncs. Repository URL/commit unavailable. | Partial |
| Configure own SDK/JDK and sync | Studio sync succeeded with installed SDK and JBR 25. Initial command-line attempts hit an environment filesystem restriction, documented below. | Pass after environment workaround |
| Copy example token properties and configure locally | Example and ignore rule inspected. No authorized token configured; no real token file copied into evidence. | Blocked for live checks |
| Build and run app | Studio assembled and installed debug APK; login screen launched on the fresh emulator. [Login screenshot](evidence/clean-setup/01-login.png). | Pass |
| Register and sign in with disposable account | Device suite passed required/short/mismatched/whitespace validation, invalid credentials, registration/login/logout, duplicate-name and account-isolation checks. A separate disposable account was registered through the screen controls and signed in. | Pass |
| Search a real movie with poster and no in-app token entry | Sample Interstellar search works and is labeled Sample mode. Automatic live-access test fails because the build has no token. Live poster retrieval is unverified. [Sample search](evidence/clean-setup/05-search-sample.png). | Blocked for live success |
| Try two moods | Upbeat returns Paddington and Finding Nemo; Spooked returns The Conjuring. Mocked transport tests also verify different TMDB genre requests. [Upbeat](evidence/clean-setup/09-mood-upbeat.png), [Spooked](evidence/clean-setup/10-mood-spooked.png). | Pass in sample mode |
| Save movie, status, rating and note | Interstellar, watched status, four stars and note survived force-stop and relaunch; saved session also survived. Device tests cover review editing/deletion, movie deletion/cascade and database reopen. [After restart](evidence/clean-setup/08-review-after-restart.png), [log](evidence/clean-setup/manual-persistence.txt). | Pass in sample mode |
| Search/filter/sort watchlist | Watched and To watch filters, title order, release-year order and no-match state passed through actual controls with three saved films. [Log](evidence/clean-setup/manual-watchlist.txt). | Pass in sample mode |
| Inspect About attribution | Notice and TMDB website are visible in debug settings, but the required official logo is absent from the screen and resources. Release About screen not run. [Credits](evidence/clean-setup/04-about-credits.png). | Fail against checklist |
| Run unit and lint checks | Unit tests: 10 passed. Final lint: 1 error, 115 warnings; lint task fails. Device tests: 26 reported passed, 2 failed, 1 skipped. Details below. | Fail |
| Check handoff instructions | Missing documents, conflicting token wording and missing documented commands found. | Fail |

## Executed automated checks

Android Studio successfully ran `:app:assembleDebug` and installed/launched the result. Through Studio's Gradle tool, the following tasks were executed against the isolated project:

```text
:app:testDebugUnitTest :app:lintDebug :app:assembleDebugAndroidTest --console=plain
:app:lintDebug --console=plain
:app:lintDebug --rerun-tasks --no-configuration-cache --console=plain
```

The combined invocation produced the unit reports and instrumentation APK but failed lint. The final uncached lint run confirms the source-only result after correcting the temporary SDK-path file.

| Check | Exact result |
| --- | --- |
| `MoodSearchTest` | 6 tests, 0 failures, 0 errors, 0 skipped |
| `PasswordPolicyTest` | 4 tests, 0 failures, 0 errors, 0 skipped |
| Final `lintDebug` | 1 error, 115 warnings; failed |
| Device instrumentation suite | 29 test methods: 26 reported passed, 2 failed, 1 skipped; 66.991 seconds |

The instrumentation APK was installed on the fresh emulator and run with:

```text
adb -s emulator-5554 shell am instrument -w -r com.example.cinemood.test/androidx.test.runner.AndroidJUnitRunner
```

The failures are retained as failures:

1. `AutomaticLiveAccessTest.searchWorksWithoutDeviceTokenSetup`: `Build must supply automatic live access`. This clean debug build has no token. This is an unmet live-test precondition, not proof of a defect in sample search.
2. `LiveReadinessTest.invalidTokenShowsUsefulError`: `Expected authentication error, not a network outage`. The request encountered a connection error, so rejection of an invalid token was not established.
3. `LiveReadinessTest.liveMovieMetadataAndPersonalRecordsSurviveDatabaseReopen` was skipped with `Live token is not configured`.

One of the 26 reported passes, `processRestartCheckpoint`, returns immediately when no `checkpoint` argument is supplied. It did **not** exercise its live restart protocol in this run. The separate manual sample-mode force-stop/relaunch check did exercise persistence.

Database integration, migration, sample discovery screens, app registration/login/review/removal/logout flow, whitespace rejection, TMDB JSON/request tests and token-cleanup checks passed. The offline-readiness test passed with saved TMDB-shaped metadata and network disabled; that metadata was a fixture, not a successful live movie download. No AndroidRuntime crash was recorded in the captured app runtime log.

Evidence: [machine-readable counts](evidence/clean-setup/test-summary.json), [full device output](evidence/clean-setup/instrumentation-all.txt), [lint HTML](evidence/clean-setup/lint-results-debug.html), [lint text](evidence/clean-setup/lint-results-debug.txt), and the `TEST-*.xml` files in [the evidence folder](evidence/clean-setup/).

## Problems to address

| Finding | Impact and reproduction | Suggested correction |
| --- | --- | --- |
| Source lint error for API 27 navigation-bar attribute | `app/src/main/res/values/cm_styles.xml:3` sets `android:windowLightNavigationBar`; lint requires API 27 while minSdk is 24. This blocks lint acceptance. It is not evidence that API 24 crashes; API 24 was not available for runtime testing. | Put the API-specific item in an appropriately qualified resource such as `values-v27`, with a suitable base theme. Rerun lint and test the minimum API. |
| Scrolled content overlaps status-bar icons | Open Interstellar details and scroll down to the review controls. The movie title draws under the status-bar clock/icons. Reproduced at default display settings in [saved review](evidence/clean-setup/06-saved-review.png) and [restart verification](evidence/clean-setup/08-review-after-restart.png). `BaseActivity.showScreen` adds inset padding, but the details root uses `clipToPadding="false"`. | Keep scrolling content clipped outside the status-bar inset, or provide an opaque system-bar protection area. Retest scrolling and keyboard/cutout layouts. |
| Required TMDB logo missing | Debug credits display the notice and website but no official logo. The shared settings layout contains only text for credits; no TMDB logo asset exists in the supplied source. | Add the required official asset to the credits layout and verify both debug settings and release About. |
| Incomplete or contradictory handoff documentation | `SETUP.md`, `DATABASE_TEST_RESULTS.md`, and `DATABASE_INTEGRATION.md` are missing. README line 33 says the credential is not part of APK build configuration, while the current build supports a compiled token. README instructions also use the older Movie data settings label. | Supply the referenced files or remove stale links; document exact project root, JDK/SDK requirements, debug/release token precedence, and executable verification commands. |
| Live tests and restart helper need clearer prerequisites | A legitimate token-free sample build fails the automatic-live test. The restart helper reports passed when invoked without its special argument. | Separate live tests into a documented suite or explicitly skip unavailable prerequisites; make restart stages explicit and prevent no-op success from being counted as restart evidence. |
| Minor count grammar | One saved film is labeled `1 saved films`; one search result is labeled `1 movies`. Visible in [persisted watchlist](evidence/clean-setup/07-watchlist-persisted.png) and [compact search](evidence/clean-setup/17-compact-large-text-search.png). | Use quantity strings for singular/plural film and movie counts. |

The 115 lint warnings comprise 73 `HardcodedText`, 14 `SetTextI18n`, 11 `RtlSymmetry`, 4 `Autofill`, 4 `UseKtx`, 2 `UnusedResources`, and one each of `AndroidGradlePluginVersion`, `GradleDependency`, `DataExtractionRules`, `UseCompoundDrawables`, `MonochromeLauncherIcon`, `ContentDescription`, and `LabelFor`. The accessibility findings concern redundant hint/content-description text in watchlist search and a missing label in the mood layout. Full TalkBack operation was not tested.

## Supplemental screen and persistence checks

- In the normal display configuration, Interstellar was saved as watched with four stars and a note. The app process was force-stopped and launched through LoginActivity; the saved session returned to the dashboard. Reopening the watchlist and film preserved the watched checkbox, all four stars and the exact note. Screenshots before and after were visually compared.
- With Paddington planned and Interstellar watched, each status filter showed the correct single film. Title sorting produced Interstellar then Paddington; an unmatched search produced the correct empty state.
- After adding Finding Nemo, release-year sorting produced Paddington (2014), Interstellar (2014), then Finding Nemo (2003). The [release-year screenshot](evidence/clean-setup/14-watchlist-release-year.png) and manual log retain the result.
- At 720 x 1280, density 320 (360 x 640 dp), and 150% font scale, dashboard actions remained reachable by scrolling. Finding Nemo search returned the expected single sample result, and its title/genre wrapped within the movie card. The main dashboard shortcuts require scrolling past the large hero section, which reduces convenience on a compact display. [Dashboard actions](evidence/clean-setup/16-compact-large-text-actions.png), [search](evidence/clean-setup/17-compact-large-text-search.png).
- The status-bar overlap also reproduces on the compact dashboard and search screens; this is not limited to movie details. Display size, density and font scale were restored after this check.
- The filter popup was inspected after its opening animation settled and showed all three options correctly. The earlier instrumentation screenshot caught the animation and is not classified as a popup clipping defect.

## Environment failures and retests

The initial command-line build and its stacktrace retry both failed before app compilation with `GeneratedClassCompilationException`, caused by `AccessDeniedException` while Java resolved a Gradle JAR path. A small independent Java ZIP/path check reproduced the same restriction with JBR 25 and JDK 21. Android Studio's normal Gradle import/build then succeeded. This is recorded as a limitation of the command-line review environment, not a source build defect. Original logs are retained in `build-tests-lint.txt`, `build-retry-stacktrace.txt` and `environment-zip-check.txt`.

The first lint run reported 2 errors and 115 warnings. One error was in the review-created `local.properties` SDK path formatting. That local file was corrected in the isolated copy. A cached lint rerun retained the old result; the uncached rerun produced **1 error and 115 warnings**. Initial reports are retained as `initial-lint-results-debug.*`; final reports use `lint-results-debug.*`.

Temporary ADB driver retries corrected keyboard focus, a duplicated text selector, and device-size/navigation assumptions during the manual checks. These driver errors are not classified as app failures. The final compact-layout run is `manual-layout-final.txt`. No application code was patched to make tests pass.

## Remaining acceptance work

Configure an authorized token locally and rerun real search/posters, live mood changes, live restart persistence, invalid-token rejection, and connection recovery. Build and test the team's signed release APK separately on a clean device. Verify the release About screen, minimum supported API 24, landscape/cutout variants, and full screen-reader use. The source setup review does not replace the original approval record or signed-APK acceptance.

The original checklist remains unchanged in Downloads; an exact copy is retained with the evidence. All newly created review notes and evidence are under this project's `docs` folder. The test copy, Gradle cache copy, emulator data and temporary drivers are under the outer folder's `review-work` directory and should not be included in a source submission ZIP. The fresh review emulator was stopped after testing; the existing Pixel 6 emulator's saved data was not used.
