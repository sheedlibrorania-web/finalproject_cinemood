# CineMood development tools and standards

Course: CS0053. Evidence collected November 25, 2026 on the inherited application baseline `7741433`. This report distinguishes real captured output, historical evidence, proposed conventions and demonstrations still to do.

## Environment and configuration

Android Studio Quail 4 (2026.1.4) reported CineMood READY through its CLI connection: [inventory](evidence/cs0053/android-studio.txt). Project configuration uses AGP 9.4.1, Gradle wrapper 9.6.0, compile/target SDK 37, minimum SDK 24 and Java source compatibility 11. Gradle uses Android Studio’s bundled JDK; Java source compatibility is not the runtime JDK requirement. Kotlin support is supplied by the existing Android build configuration. Screens use Activities/XML, not Compose.

Follow [SETUP.md](SETUP.md) for installation, SDK/JDK selection, emulator, terminal, token and test setup. Real `tmdb.properties`, `local.properties`, private keys, `.idea/` and build products are excluded by [.gitignore](../.gitignore); teammates configure local settings independently.

## Android Studio equivalents — instructor approval reported

Stephen reported on November 25, 2026 that the instructor approved, in response to the pending Android Studio tool-equivalence requirement. The five entries below are Android Studio capabilities accepted under that reported substitution, not five installed VS Code extensions. An original written approval record was not supplied; attach one if required. This report does not backdate approval or infer a separate SRS approval from the tool decision.

| Capability | Configuration and real project usage | Value | Limitation / evidence status |
| --- | --- | --- | --- |
| Kotlin language support and IDE inspections | Open Kotlin source after sync; use declaration navigation, completion and Analyze/Inspect. Actual CLI-connected IDE analysis of PasswordPolicy.kt is saved below. | Finds ambiguous expressions and supports source understanding. | One informational suggestion was found; not a compile error. Source tree, Kotlin syntax highlighting and inline usage counts appear in the environment screenshot. A separate completion/navigation demonstration is not claimed. |
| IDE formatter and code style | Settings → Editor → Code Style → Kotlin/XML; use Reformat Code on selected code and inspect the diff. | Consistent layout and readable reviews. | Project has no checked-in formatter profile or .editorconfig. This report proposes conventions; the actual Kotlin Coding Conventions setting and a completed formatter demonstration are captured in the screenshot index. |
| Android Lint | Run `:app:lintDebug` through Gradle/Terminal; open generated report. | Android resource, compatibility and accessibility checks. | Does not prove runtime correctness. Real lint output and summary are captured; eight warnings remain. |
| Debugger and Logcat | Use `app` debug configuration, set a method breakpoint in PasswordPolicy.containsWhitespace, Debug the app and inspect a disposable invalid registration input. Filter Logcat by app package. | Inspects actual state and diagnoses runtime problems. | Completed method-breakpoint demonstration with disposable input; call stack/variable and resumed validation are captured. A separate Logcat investigation was not performed. |
| Git integration and history | Git tool window, focused branch, commit diff, Push and reviewed PR. | Traceable changes and peer review. | Local history verifies merges, not every remote review discussion. [history](evidence/cs0053/git-history.txt), [branches](evidence/cs0053/git-branches.txt); retain review screenshots/links. |

## Captured actual usage

See the [screenshot index and captions](evidence/cs0053/SCREENSHOTS.md) for eight new screenshots, the actual steps and cleanup. The captures were operated by Codex at Stephen’s request; they do not claim each student personally performed the demonstrations.

| Evidence | What was done | What it establishes / limit |
| --- | --- | --- |
| [IDE analysis](evidence/cs0053/ide-analysis.txt) | Ran Android Studio analysis on PasswordPolicy.kt through the connected IDE. | IDE returned “Expression should use clarifying parentheses”, informational severity. No file was changed to manufacture a defect. |
| [Build/test/lint log](evidence/cs0053/gradle-verification.txt) | Ran the checked-in wrapper’s unit tests and Android Lint with task rerun enabled and the bundled JDK. | See [structured summary](evidence/cs0053/verification-summary.json) for actual tests/warnings. Does not rerun the device suite. |
| [Emulator capture](evidence/cs0053/emulator-login.png) | Captured the running emulator’s login screen and visually inspected the image. | Shows light-mode app rendering; does not prove login interaction, full accessibility, posters or performance. |
| [Git history](evidence/cs0053/git-history.txt) | Fetched remote refs and recorded actual commits/authors/merge subjects. | Confirms baseline and setup merge history. Separate `main` history is included and identified in compliance register. |
| [Historical device results](quality/instrumentation-results.csv) | Earlier baseline run executed 29 instrumentation methods on Pixel 10a API 37.1. | Existing results, not fresh device execution. Phase-gated restart checkpoint does not prove two-process restart. |

Captures and output are real evidence. The new IDE screenshots supplement the existing logs; remaining course evidence should be reviewed against the instructor’s approved scope. User screenshots from the earlier conversation are not silently represented as newly captured IDE demonstrations.

## Coding standards and source examples

Conventions to apply during future reviewed changes:

- Kotlin classes/files use PascalCase (`CineMoodRepository.kt`); functions/properties use lowerCamelCase (`saveReview`, `watchlistId`); resource names use lower_snake_case (`activity_watchlist.xml`).
- Use four-space Kotlin indentation and consistent Android Studio formatting. Existing compact declarations remain; no full-code formatting conformance audit is claimed.
- Keep screens in Activities, SQL/persistence in `data/`, and external movie access in `network/`. Reuse the repository rather than duplicating account-scoped SQL inside screens.
- Store reusable visible text in string resources; keep shared themes/components in resources. Comments explain constraints and non-obvious behavior instead of narrating syntax.
- Validate inputs at the screen and data boundary. PasswordPolicy rejects whitespace rather than trimming passwords. Repository validates ratings and scopes operations by user ID; SQL values use parameters.
- Run network/database work off the main thread. Use understandable errors, explicit sample/live labels and real-work loading indicators.
- Never commit/log credentials. Treat APK-embedded TMDB access as a documented classroom limitation, not a protected secret store. Do not upgrade dependencies or add tools without a separately verified reason.

Source examples: [PasswordPolicy](../app/src/main/java/com/example/cinemood/data/PasswordPolicy.kt), [repository](../app/src/main/java/com/example/cinemood/data/CineMoodRepository.kt), [TMDB client](../app/src/main/java/com/example/cinemood/network/TmdbClient.kt), [watchlist](../app/src/main/java/com/example/cinemood/WatchlistActivity.kt), [theme resources](../app/src/main/res/values/cm_styles.xml). These links are also sample-source evidence for the report.

## Repeatable screenshot and demonstration procedure

Use disposable sample input and keep private properties files closed.

1. **Environment/source tree:** show Android Studio with the project tree, a nonsecret Kotlin file and the emulator. Capture the IDE version/configuration separately if needed.
2. **Formatter:** on a temporary local scratch copy of PasswordPolicy, intentionally misindent a line, use Reformat Code and capture before/after. Discard the scratch change; do not commit a manufactured defect. Record actual selected code-style settings.
3. **Lint:** open the actual report or IDE inspection window; show the API-27 theme split and explain the prior minSdk compatibility fix. Do not present the old 115-warning screen as the current state.
4. **Debugger:** place a breakpoint in PasswordPolicy.containsWhitespace; run Debug and submit a disposable short password. Capture the paused stack and nonsecret state, then resume and show validation. Remove the breakpoint afterward. Do not show a real account’s password in variables or Logcat.
5. **Git:** show the real branch graph and Mark’s approved PR, with authorship intact.
6. Record each file/date/operator/action/result in this report. Save actual screenshots under `docs/evidence/cs0053/`; do not mark these pending steps complete until executed.

## If extension substitution is not approved

Use VS Code alongside Android Studio, which remains the Android build/emulator IDE. Candidate relevant extensions previously identified are Kotlin (`mathiasfrohlich.kotlin`), Gradle for Java (`vscjava.vscode-gradle`), Language Support for Java (`redhat.java`), Error Lens (`usernamehw.errorlens`) and Bookmarks (`alefragnani.bookmarks`). Availability, versions and current configuration must be rechecked; listing them is not proof of current installation or course acceptance. Java support is supporting tooling in a Kotlin project, so confirm relevance with the instructor or choose a more appropriate approved alternative.

For each selected extension: install from Extensions by exact ID, record publisher/version, document settings and dependencies, and capture the installed entry. Execute at least three genuine demonstrations (for example Kotlin navigation, a Gradle task in the actual project, and Bookmarks navigation or Error Lens diagnostics where supported). Error Lens only decorates diagnostics provided by another language service; do not claim it supplies Kotlin diagnostics by itself. Report Gradle/AGP import limitations honestly and keep Android builds in Android Studio if the extension cannot import them. Record actual usefulness/conflicts and screenshot the outcome. No VS Code installation/configuration/demo was performed during this documentation task.

## Team reflection and review

Observed: connected IDE analysis provides useful source feedback without altering behavior; lint found a genuine platform compatibility issue; tests cover password/mood rules and repository behavior; Git preserves the reviewed baseline. Limits: informational IDE suggestions differ from lint warnings, passing tests do not cover every device, and the Android Studio tool substitution is reported approved by Stephen, with the original approval record not attached.

Stephen and Mark should add their own short reflections based on tools they personally used. Mark must review factual accuracy and attempt the setup instructions independently. The approval/review actions are tracked in [CS0053_COMPLIANCE.md](CS0053_COMPLIANCE.md).

## Capture completion record — November 25, 2026

- Formatter: **completed** on `/tmp/CineMoodFormatterDemo.kt`, a disposable copy of the existing password-policy logic. Reformat Code indented two functions; before/after captures show the actual result. The project source was not reformatted.
- Settings: **captured** Default IDE scheme using Kotlin Coding Conventions; the IDE warns that indent detection can override settings. Settings were inspected without applying changes.
- Debugger: **completed** using the `app` debug configuration on Pixel 10a API 37.1. A method breakpoint at `PasswordPolicy.containsWhitespace` paused during registration; the stack included RegisterActivity and the variable showed disposable `short6`. Resuming produced “Use at least 8 characters”. No account was created. The breakpoint was removed, the debug session stopped, and a normal Run restored the login screen.
- Environment/version/Git: **captured** source organization, Kotlin source, running emulator, IDE runtime/version and actual Git graph including PR #1/#2 merge subjects. Git screenshots do not replace Mark’s remote approval record.
- Cleanup: verified no tracked application-source changes. Temporary formatting example remains outside the project. Only documentation/evidence is prepared for commit. No commit, push or PR was performed.
