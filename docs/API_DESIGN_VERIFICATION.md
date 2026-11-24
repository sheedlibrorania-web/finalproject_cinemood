# API and design verification report

Executed November 25, 2026 (Asia/Manila), baseline `7ff352d` plus the current connection-factory/test additions on `docs/api-and-design`. Operator: Codex at Stephen’s request. Device: Pixel 10a emulator, Android 17 / API 37.1. These results apply to this working tree, not a newly signed release artifact.

## Change under test

Production TmdbClient has an optional connection-construction factory with the same default `URL.openConnection()` behavior. Existing transport/body injection remains available and current call sites compile. The factory is used by TmdbHttpTest to supply fake HTTP responses/exceptions while executing the real authentication-header, timeout, status mapping and cleanup branches. No UI, endpoint, account/database behavior or external dependency changed.

## Executed results

| Check | Actual result | Evidence |
| --- | --- | --- |
| Debug build | Pass | [HTTP/build log](evidence/api-design/http-check.log) |
| Debug local unit-test task | Pass, up-to-date outputs reused; existing 10-test password/mood suite was not freshly executed in this task | Same log; prior fresh results in [tools evidence](evidence/cs0053/verification-summary.json) |
| Debug Android Lint | Pass: zero errors, eight warnings | HTTP/build log; generated local lint report |
| HTTP handling | 10 tests passed, zero failures/errors/skips | [HTTP JSON](evidence/api-design/http-results.json) |
| Existing parser/query/mood client tests | 7 passed, zero failures/errors/skips | [parser JSON](evidence/api-design/parser-results.json), [log](evidence/api-design/parser-check.log) |
| Real SQLite repository behavior | 12 passed, zero failures/errors/skips | [repository JSON](evidence/api-design/repository-results.json), [log](evidence/api-design/repository-check.log) |
| Automatic real TMDB search | 1 passed, zero failures/errors/skips; actual API results and SearchActivity display verified | [live JSON](evidence/api-design/live-results.json), [log](evidence/api-design/live-release-check.log) |
| Release assembly | Pass; unsigned output, not a new team-signed submission APK | Live/release log |

Total: **30 executed instrumentation tests**, no failures/errors/skips. The test classes were run separately and results preserved after each run. The first combined-filter invocation selected only TmdbHttpTest according to the emitted XML; the other suites were then explicitly executed one at a time. We report actual XML counts rather than assuming a requested filter executed every class. [Consolidated actual results](evidence/api-design/test-results.csv).

The existing eight lint warnings concern optional KTX replacements, dependency/plugin notices, a drawable optimization and an unused string; these were not suppressed to obtain a pass. See [baseline dispositions](quality/pre-push-review.md).

## What the new HTTP tests establish

- 200 success uses GET, bearer header and JSON accept header; both configured timeouts equal 12,000 ms. The response stream closes and connection disconnects.
- Valid empty JSON results return an empty list without inventing movies.
- 401 and 403 execute the current debug message branch; 429, 503 and 404 map to appropriate retry/unavailable messages.
- Injected SocketTimeoutException during response acquisition or reading produces the timeout message; injected IOException produces the connection message.
- Malformed JSON fails parsing and still closes HTTP resources. UI generic-error mapping is source-reviewed, not newly screen-tested with malformed JSON.

These are synthetic HTTP-path tests, not real TMDB outages or attempts to exceed rate limits. Production’s default connection path is separately exercised by AutomaticLiveAccessTest, which searched real TMDB and verified results on screen. Release-only authorization text is source-reviewed, not executed by the debug instrumentation run.

## Repository-pattern evidence

The 12 repository tests use a dedicated SQLite database, not a fake repository. They exercise registration/login, invalid registration/credentials, salted hashes, account isolation, duplicate saves, status validation, review CRUD, invalid ratings, cascade deletion and persistence across repository/database reopen. The actual methods are linked in [DESIGN_PATTERNS.md](DESIGN_PATTERNS.md). This demonstrates the central data boundary’s behavior; it is not a separate-process restart or a claim of full security assurance.

## Reproduce

Use a test emulator and configure an authorized local build token for the live suite. From the project root, run each instrumentation command separately and preserve its results before the next command replaces generated output:

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.cinemood.TmdbHttpTest
./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.cinemood.TmdbClientTest
./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.cinemood.data.DatabaseIntegrationTest
./gradlew :app:assembleRelease :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.cinemood.AutomaticLiveAccessTest
```

Use Android Studio’s bundled Gradle JDK as explained in [SETUP.md](SETUP.md); Windows uses `gradlew.bat`. Generated output: `app/build/outputs/androidTest-results/connected/debug/` and `app/build/reports/androidTests/connected/debug/`. The repository suite deletes only its dedicated `cinemood_integration_test.db`. The live test creates and cleans up its temporary account, and restores session/token settings as its source describes. Use disposable test devices for repeatable verification; do not assume instrumentation preserves every installation’s state. The debug app was reinstalled/launched after testing.

## Review and release follow-up

- Mark: verify endpoint parameters, mood rules, real-versus-fixture evidence, source diagrams and Repository explanation; confirm original authorship and approve the PR only after checking it.
- Both: capture final signed-APK acceptance evidence and perform the independent setup. This task does not rerun all screen flows, test API 24, measure performance, or validate release signing.
- Attribution gap: About has text attribution but no approved TMDB logo in the inspected XML. Review/add the approved logo in a separate agreed UI change and recheck before final release. See the linked provider guidance in [API_DOCUMENTATION.md](API_DOCUMENTATION.md).
- Broader course gaps (history, Issue links, final manuals/release) remain in [CS0053_COMPLIANCE.md](CS0053_COMPLIANCE.md).

No commit, push, review request or merge was performed by Codex in this task. No real token is included in these reports or fixtures. Students must understand and review AI-assisted implementation/tests/documentation.
