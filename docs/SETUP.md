# CineMood setup and execution guide

## Requirements

- Android Studio with a Gradle JDK compatible with the checked-in Android Gradle Plugin; Android Studio’s bundled JDK was used for the baseline verification.
- Android SDK Platform 37, SDK build tools requested during sync, platform tools, and an API 24+ phone or emulator.
- Internet access for the first Gradle dependency download and live TMDB search/posters.
- An authorized TMDB API Read Access Token for live discovery and release packaging.

Checked-in versions: Android Gradle Plugin 9.4.1, Gradle wrapper 9.6.0, compile/target SDK 37, minimum SDK 24, app version 1.0 (version code 1). Java source/target compatibility is 11; this is not an instruction to run Gradle on JDK 11. Do not upgrade the wrapper or dependencies merely to resolve a local setup mismatch.

## Open a clean checkout

1. Clone the repository using the README instructions, or Android Studio’s **Get from Version Control** with the repository URL.
2. Open the folder containing `settings.gradle.kts`, `gradlew` and `app/`.
3. Check out `master` for the merged application. For development, create a focused branch from the updated `master`.
4. Allow Gradle sync. In Android Studio settings, search for **Gradle JDK** and select the bundled JDK if no suitable JDK is configured.
5. In SDK Manager, install SDK Platform 37 and components requested by Gradle. Android Studio generates `local.properties` for this computer’s SDK location; do not copy a teammate’s file.
6. Resolve sync errors before running. Use the checked-in Gradle wrapper instead of installing an unrelated Gradle version.

## Configure movie access

See [TMDB_BUILD_SETUP.md](../TMDB_BUILD_SETUP.md) for the complete configuration contract.

1. Copy `tmdb.properties.example` to `tmdb.properties` in the project root.
2. Set `TMDB_READ_ACCESS_TOKEN` to the authorized Read Access Token, without quotes or a `Bearer ` prefix. This is different from the shorter API key.
3. Sync and rebuild. This value is incorporated at build time; editing the file does not change an APK already installed on a device.
4. Keep `tmdb.properties` local. The build process also accepts the `TMDB_READ_ACCESS_TOKEN` environment variable, which takes precedence over the properties file. An empty environment value also overrides the file; unset an unintended override.

In a fresh debug installation, the build token is used by default. Debug Developer movie settings can save a per-device override, including an empty value to select sample mode; that override takes precedence even after rebuilding with a new token. To restore the build default, use a fresh test emulator or clear app storage only when its local data is disposable. Clearing storage deletes local accounts, watchlists and reviews. Alternatively, save the authorized token in debug settings for that device.

Release builds ignore debug overrides, always use the build configuration and show About CineMood. Packaging refuses a blank token, but a nonblank value is not proof of validity. Never record or publish the token in screenshots, source, request examples or logs.

## Run and verify

1. In Device Manager, start an API 24+ emulator, or connect an Android phone with USB debugging enabled and approve this computer.
2. Select the `app` configuration and the device in the toolbar, then click **Run**.
3. Register a new local account, then sign in. There are no supplied default credentials.
4. Search a recognizable movie title. With a configured token and internet, check live results, a poster and movie details. Without a token, debug uses the labeled sample catalog.
5. Change the selected mood and check recommendations. Save a movie, mark it watched, add a 1–5 rating and notes, then reopen it to verify persistence.
6. Log out and back in to confirm saved records remain associated with the original account.

An emulator is preferable for repeatable tests. This guide was checked against the current configuration; a new independent clean-machine setup has not been executed as part of this documentation edit.

## Repeatable checks

From Android Studio’s Terminal, with the Gradle JDK configured:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest :app:lintDebug
./gradlew :app:connectedDebugAndroidTest
```

Windows: replace `./gradlew` with `gradlew.bat`. The last command requires a running test device. Use a disposable emulator: tests register accounts, modify records and can reset application state. Live tests need a valid build token and internet; inspect test prerequisites rather than treating sample results as proof of API access. Restart checkpoint tests require their separate phase setup; an aggregate runner count alone does not prove a two-process restart test.

Outputs are generated locally:

- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`.
- Unit report: `app/build/reports/tests/testDebugUnitTest/index.html`.
- Debug lint: `app/build/reports/lint-results-debug.html`.
- Instrumentation report: `app/build/reports/androidTests/connected/debug/index.html`.

Record the date, commit, device/API, actual results and limitations when rerunning checks. Existing executed baseline evidence is in [quality/pre-push-review.md](quality/pre-push-review.md).

## Signed classroom APK

1. Configure and verify live access locally.
2. In Android Studio, choose **Build → Generate Signed App Bundle or APK → APK**.
3. Select the team’s existing private keystore and alias. Keep passwords private and preserve the keystore securely so future APK updates use the same signing identity.
4. Choose `release` and generate the APK. Use the path reported by Android Studio; wizard and Gradle output folders may differ.
5. Install that exact signed APK on a test device. Verify login, automatic live search, moods, posters, watchlist changes, offline error handling and recovery.

`assembleRelease` alone does not establish a team-signed distribution APK. A debug APK and a release APK signed with a different key cannot update each other under the same application ID. Use a separate emulator for release verification to avoid uninstalling an app that contains needed records. Store generated APKs outside source control and attach the approved artifact to the final submission/release separately.

## Troubleshooting

| Symptom | Check/action |
| --- | --- |
| SDK location or platform missing | Open SDK Manager, install requested components and let Android Studio create local SDK settings. |
| Gradle JDK mismatch | Select Android Studio’s bundled Gradle JDK and sync again. |
| `gradlew` permission denied on Mac/Linux | Run `chmod +x gradlew`; the wrapper is tracked as executable. |
| Debug still shows sample movies | Check the build-time token, rebuild/reinstall and check for a saved debug override. |
| Release packaging refuses to continue | Configure a nonblank token in the local properties file or build environment, then rebuild. |
| Live request rejected | Verify token validity with the authorized owner; rebuild if the build token changed. Never paste credentials into issue reports. |
| Connection error or timeout | Check device internet connectivity and retry. Saved watchlist records remain local. |
| APK update reports a signing conflict | Use the correct existing signing key or a separate test emulator. Uninstalling deletes local records. |
| Push returns HTTP 403 | Check the authenticated GitHub account and repository collaborator permissions. Local commits remain intact. |

## Collaborator handoff

Share the repository URL and these instructions. Each developer configures their own SDK and authorized token locally. Never share a source archive containing actual tokens, signing keys, passwords or emulator databases. Invite collaborators through GitHub; do not share GitHub account credentials. Review and test changes before merging into `master`.
