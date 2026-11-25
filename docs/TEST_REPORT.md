# CineMood testing and quality report

> **Latest verification update — November 25, 2026:** The current isolated source build passed all 130 executed build tasks, 10 fresh unit tests, and debug/release lint (zero errors, eight warnings each). The updated full instrumentation suite reported 40 passed, one explicitly skipped restart checkpoint, zero failures; its separate save/force-stop/verify stages also passed. Sorting and compact 1.5× text checks were executed separately. The exact final team-signed APK subsequently passed 14 focused acceptance checks, including automatic live search, mood switching, review persistence, removal and logout. See [current evidence](evidence/release-readiness/summary.json) and [signed APK acceptance](evidence/release-readiness/signed/RESULTS.md).
>
> The original report below is retained as a historical pre-release record. Its pending clean-build, sorting, restart and signed-APK items are superseded by the linked evidence; remaining broad device/accessibility coverage and Git/submission tasks are not claimed complete. The replacement clean build was Codex-operated on the same Mac with cached dependencies, not an independent Windows retest by Mark.

CS0053 review edition, November 25, 2026. Team: Stephen Josh Canete and Mark Jayson D. Cadano. This is the consolidated pre-release report; it does **not** claim 100% final acceptance. An external Windows review has been received through Mark, but tested an unidentified older ZIP without a live token and did not pass cleanly. Latest-checkout verification and the current final team-signed APK remain pending.

## 1. Scope, baseline and environment

Tested application: merged baseline `e060a21` plus the official-logo/resource changes on `docs/final-manual-and-testing`. Operator: Codex at Stephen’s request. Device: Pixel 10a emulator, Android 17/API 37.1. Android Studio Quail 4/2026.1.4 and bundled Gradle JDK; AGP 9.4.1, wrapper 9.6.0. App version 1.0/code 1; minimum SDK 24. Credential values and personal user records are excluded from evidence.

Scope: local accounts, ownership, watchlist/reviews, migration, parsing/mood requests, real live access, invalid inputs, offline/HTTP failures, logo rendering and screenshot capture. The logo uses the official TMDB path/gradient/aspect ratio; original asset and provenance are in [assets](assets/README.md).

## 2. Method and entry criteria

- Start with the approved merged source and authorized local movie-access configuration; use a dedicated test emulator.
- Build debug/test APKs and release; run local unit-test and lint tasks.
- Install debug and instrumentation APKs; execute the complete AndroidJUnitRunner suite using `adb shell am instrument -w com.example.cinemood.test/androidx.test.runner.AndroidJUnitRunner`.
- Preserve actual logs, verify the terminal completion result, and capture actual rendered screens with a temporary account.
- Map outcomes to the SRS instead of assuming that one passing aggregate covers every acceptance detail. Tests use dedicated/generated records and restore relevant settings/session; source-build and signed-artifact acceptance are separate checks.

## 3. Executed result summary

| Check | Actual result | Evidence |
| --- | --- | --- |
| Debug APK and instrumentation APK assembly | Pass | [build](evidence/final-review/build-check.log), [capture build](evidence/final-review/capture-build.log) |
| Release assembly | Pass, unsigned build; not newly team-signed | Build log |
| Unit-test task | Up-to-date: existing 10 passing unit results reused; no claim of fresh unit execution in this session | Build log; prior [fresh unit summary](evidence/cs0053/verification-summary.json) |
| Debug lint | Zero errors, eight warnings | Build log and structured summary |
| Release lint | Zero errors, eight warnings | Build log and structured summary |
| Full instrumentation suite | **OK (40 tests)**, no reported failures | [raw runner output](evidence/final-review/instrumentation-check.log) |
| Screenshot/attribution capture | Actual screens captured; logo drawable present; visual review performed | [screenshots](evidence/final-review/SCREENSHOTS.md) |
| Independent clean source setup | **Pending Mark** | [result template](CLEAN_SETUP_CHECK.md) |
| Current final signed-APK acceptance | **Not run** | Release gate below |

[Machine-readable summary](evidence/final-review/summary.json). Do not add the older 29/30 test runs to this 40-method run: they overlap. The 40 includes a phase-gated `processRestartCheckpoint` method; without its separately configured two-process phases, that method’s Pass is not proof of process-restart persistence. Database reopen/offline checks are genuine but different tests.

### Instrumentation composition

| Class | Methods reported successful | Purpose |
| --- | --- | --- |
| AppFlowTest | 1 | Real register/login/save/review/removal/logout flow. |
| AutomaticLiveAccessTest | 1 | Real TMDB request and SearchActivity results without per-device token entry. |
| DatabaseMigrationTest | 1 | v1→v2 data preservation. |
| DiscoveryScreenTest | 1 | Sample search/empty states, mood, watchlist filter and settings validation. |
| DocumentationCaptureTest | 1 | Live screenshots and logo drawable, using temporary data. |
| LiveReadinessTest | 4 | Database/offline/invalid-token checks plus the phase-gated restart checkpoint caveat. |
| RegistrationWhitespaceTest | 1 | Password/confirmation whitespace rejection in UI. |
| TmdbClientTest | 7 | Parser, encoding, mood filters, validation and payload round-trip. |
| TmdbHttpTest | 10 | Actual HTTP handling code with fake connections: success, headers/timeouts/cleanup, empty/invalid JSON, 401/403, 429, 503/404, I/O and timeout exceptions. |
| TokenCleanupTest | 1 | Debug sample tests preserve movie-access configuration. |
| DatabaseIntegrationTest | 12 | Real SQLite auth, owner isolation, CRUD, constraints, hashing, cascade and reopen. |

HTTP failure fixtures intentionally do not trigger real rate limits or outages. A real TMDB result is verified separately by AutomaticLiveAccessTest and the screenshot capture. This debug suite does not execute release-only authorization text or prove every final release screen behavior.

## 4. Requirement-by-requirement outcomes

[TEST_CASES.csv](TEST_CASES.csv) contains the full plan, expected results, actual status and evidence limits. The table below is the concise SRS traceability view.

| SRS | Verification and outcome |
| --- | --- |
| FR-01 Registration | Automated valid/invalid/duplicate/whitespace checks passed. |
| FR-02 Authentication/session | AppFlow and SQLite login/logout checks passed. |
| FR-03 Account isolation | Real SQLite cross-account access checks passed. |
| FR-04 Live search | Real search/returned results passed; encoding and blank-input checks passed. |
| FR-05 Mood discovery | Query/alias rules passed; live Chill screen captured. Overlapping recommendations are allowed. |
| FR-06 Details/posters | Live search/details/watchlist/mood images visually reviewed where shown; parser missing-field checks passed. Final signed-build and full image-failure visual matrix pending. |
| FR-07 Saved collection | Create/read/duplicate save checks passed. |
| FR-08 Watched status | Status validation/persistence passed; watched entry captured. |
| FR-09 Personal review | Review CRUD/ratings/notes persistence and invalid ratings passed. |
| FR-10 Delete review only | Repository-level checks passed; earlier user confirmed UI behavior. Fresh final signed UI check pending. |
| FR-11 Remove movie | Cancel/confirm screen flow and cascade checks passed. |
| FR-12 Search/filter/sort | Fresh automated search/filter checks passed; sorting was previously user-reported. Fresh sort acceptance remains pending. |
| FR-13 Persistence/offline | Database reopen and offline-read checks passed; independent process-restart procedure remains open. |
| FR-14 Errors | HTTP/parser fixtures and real invalid-token/offline checks passed. Release-only error wording/UI acceptance remains open. |
| FR-15 Navigation/journal | Screen flow passed; Back visible in screenshots; journal documented as a details section. Exhaustive navigation/accessibility coverage not claimed. |
| FR-16 Modes/attribution | Automatic live debug access and shared-layout logo verified. Current signed release About visibility/control hiding still needs acceptance. |
| FR-17 Upgrade | Migration check passed. |
| NFR-01 Usability | Light-mode visual/lint checks; full TalkBack/large-text matrix pending. |
| NFR-02 Performance | Background workers/timeouts inspected; no measured benchmark, no guaranteed response-time claim. |
| NFR-03 Reliability | Tested paths completed without unexpected app termination; remaining device/error combinations open. |
| NFR-04 Security | Salted-hash/owner checks passed; ignored secrets and current documentation scan checked. No penetration test or complete history-secret audit claimed. |
| NFR-05 Maintainability | Source organization, documentation and reviewed prior PRs; current branch awaits review. |
| NFR-06 Portability | API 37.1 run and build compatibility checked; API 24 runtime and Mark’s machine pending. |
| NFR-07 Testability | Repeatable tests and raw outputs linked. |
| NFR-08 Integrity | Constraints, transactions, cascade and migration tested within stated scope. |

## 5. Defects, corrections and known limitations

| Finding | Correction / disposition | Evidence/status |
| --- | --- | --- |
| API-27 navigation-bar attribute in base theme with minSdk 24; original lint 1 error/115 warnings | Version-qualified theme, resource/accessibility fixes and retained compatibility | [Historical pre-push review](quality/pre-push-review.md); current lint 0 errors/8 warnings. No API24 runtime claim. |
| Earlier posters absent from lists and moods returning identical request rules | Corrected before this baseline; regression request tests and current live screen evidence | Existing TmdbClientTest and manual figures; no fabricated historical failure date. |
| About lacked approved TMDB logo | Added official asset as VectorDrawable with original path/gradient/ratio, existing notice retained | Current screenshot and capture check; original [asset provenance](assets/README.md). |
| UI navigation/loading complaints | Earlier user confirmed fix; current routing uses direct destinations | Historical user report and source inspection; no timing benchmark performed here. |
| Eight non-blocking lint warnings per variant | Keep documented dispositions rather than suppressing | Optional KTX changes, dependency/plugin notices, compound drawable optimization and unused string. |
| Data and credential limits | Local-only records, first-page discovery, memory-only posters, extractable APK token | Documented in SRS/API/manual; not silently treated as implemented cloud/security features. |

Screenshot capture iteration/fixes are recorded in the figure index. They affect the test harness, not application behavior; final capture results must be retained alongside any failed iteration rather than represented as an unbroken pass history.

## 6. Exit criteria and final release gate

- [x] Current source builds; lint has zero errors with recorded warnings.
- [x] Complete debug instrumentation suite result retained; fixture and live evidence distinguished.
- [x] User manual and actual screenshots prepared; official logo added.
- [ ] Mark completes clean-checkout setup and records results/fixes.
- [ ] Mark reviews this documentation/UI PR and the team resolves comments.
- [ ] Confirm SRS approval/evidence scope and remaining Git/Issue/contribution requirements.
- [ ] Generate a new team-signed APK containing the current approved changes.
- [ ] Record exact artifact SHA-256, certificate fingerprint, version, tested commit/device/date; verify update installation without losing needed records.
- [ ] Test that signed APK: automatic live search; two moods; posters in lists/details; save/status/review/reopen; filter and each sort; delete review/removal; offline error/recovery; top Back/logout; About logo/notice and absent developer controls.
- [ ] Complete agreed process-restart/minimum-SDK/accessibility checks or document accepted scope limitations.
- [ ] Update this report with actual signed-artifact outcomes, then create final release tag/package.

Quality assessment: no failure in the recorded 40-method debug run and no build/lint errors. The project is ready for peer review and remaining acceptance work; it is not yet declared fully release-accepted. Mark’s setup is not passed by assumption. See [CS0053 compliance](CS0053_COMPLIANCE.md) for non-test submission gaps.

## Received external Windows review — November 25, 2026

The [received review](evidence/clean-setup/received/CLEAN_SETUP_RESULTS.md.txt) reports a successful debug build, 10 passing unit tests, lint failure (1 error/115 warnings), and 29 instrumentation methods (26 reported pass, 2 fail, 1 skip). Its source had no Git identity and lacked changes present in this branch; do not combine these counts with the current 40-method run or relabel them as a current-source failure/pass. The operator is identified as Codex for the requester, not Mark’s personal execution. Raw referenced evidence was not supplied.

[Reconciliation](CLEAN_SETUP_RECONCILIATION.md) records already-addressed findings and unresolved checks. The status-bar scrolling overlap is also visible in our current [review screenshot](evidence/final-review/08-review.png), so it is a confirmed outstanding visual defect, not dismissed as stale ZIP behavior. Watchlist singular-count copy is also still open. Release readiness remains conditional on fixes/retests and latest-source live/clean-setup acceptance.
