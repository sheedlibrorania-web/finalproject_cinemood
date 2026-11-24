# CineMood pre-push quality review

Review date: November 25, 2026. Starting commit: `e958f8d` on `baseline/cs0053-start`.

## Findings and corrections

The original Gradle lint run reproduced the commit-check result: **1 error and 115 warnings**. The debug APK compiled, but lint failed.

- **Android API compatibility:** `windowLightNavigationBar` requires API 27 but appeared in the base theme with minimum API 24. Moved it into a `values-v27` theme override. The base theme uses a dark navigation-bar background for the light buttons on older Android versions. The light-mode design is retained on API 27 and later. Lint validates this resource split; no API 24 emulator run is claimed.
- **UI text resources:** Extracted 73 hardcoded XML labels/hints into shared string resources. A comparison against the committed layouts confirmed all 73 text values were preserved. Parameterized runtime messages now use resource placeholders. Search counts correctly use singular/plural resources.
- **Accessibility and input declarations:** Removed a redundant content description from watchlist search, removed the redundant hint/label pairing in mood search, declared non-credential fields unimportant for autofill, and explicitly retained existing padding around trailing icons and Back controls.
- **Backup policy:** Kept backup disabled and added explicit Android 12+ cloud/device-transfer exclusions for app data. Lint validation is recorded; backup/restore transport behavior has not been manually tested.
- **Launcher resources:** Referenced the existing round icon and provided a monochrome icon layer using the existing foreground asset.
- **Unused imports:** Removed unused imports from two instrumentation-test files.
- **Build launcher:** Restored execute permission for `gradlew` and staged the Git mode change from `100644` to `100755`, so Mac/Linux checkouts can execute it.

No lint baseline or blanket warning suppression was added. Database logic, account isolation, API request rules and existing feature controls were retained.

## Verification

| Check | Result |
| --- | --- |
| Debug build (`:app:assembleDebug`) | Pass |
| Release build (`:app:assembleRelease`) | Pass; unsigned release build, not a newly team-signed distribution APK |
| Unit tests (`:app:testDebugUnitTest`) | 10 tests passed, zero failures/errors |
| Debug lint (`:app:lintDebug`) | Zero errors, 8 warnings |
| Release lint (`:app:lintRelease`) | Zero errors, 8 warnings |
| Instrumentation tests (`:app:connectedDebugAndroidTest`) | 29 tests passed, zero failures/errors/skips on Pixel 10a |
| Diff whitespace validation (`git diff --check`) | Pass |
| Current tracked files scanned for the configured local TMDB token value | Zero matches |
| Private token/local settings/signing files in tracked-file list | None |

Android Studio successfully launched the updated debug application on Pixel 10a, Android 17 / API 37.1. Dashboard and movie-details screenshots from the current screen-flow tests were visually reviewed. Existing historical test records remain separate from this review.

The emulator run included application screen flows, automatic real TMDB search, JSON/request handling, SQLite migration, account ownership/CRUD, whitespace validation and token-cleanup regression checks. The runner's 29-test count includes a phase-gated restart-checkpoint method; no separate two-process restart experiment was performed in this review. Raw per-test outcomes are summarized in `instrumentation-results.csv`.

## Remaining non-blocking warnings

| Warning | Count | Disposition |
| --- | --- | --- |
| Android Gradle Plugin version notice | 1 | Keep the current working version; evaluate upgrades in a separately tested change. |
| AndroidX Core version notice | 1 | Keep the current working version; evaluate the update separately. |
| Optional Kotlin KTX replacements | 4 | Existing transaction and preference operations remain functional; no rewrite needed for this correction. |
| Compound drawable optimization | 1 | Retain the existing weighted heading/icon arrangement and appearance. |
| Unused `mood_brand` string | 1 | Low-impact unused resource; no runtime error. |

The previous commit notification is a historical analysis result. Use the current Gradle lint reports or rerun inspection to see the updated findings.

## Review and commit

The starting baseline commit is unchanged. Corrections are left for a separate reviewed commit; no commit, push or GitHub publication was performed by Codex. Include the new theme, UI string and extraction-rule resource files when committing. The launcher mode change is already staged; other changes remain available in the Commit window.

Suggested message: `Fix Android theme compatibility and UI resource warnings`.

Reference: Android Auto Backup documentation, https://developer.android.com/identity/data/autobackup, consulted through Android CLI documentation search.
