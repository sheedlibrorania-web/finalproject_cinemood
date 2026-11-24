# CS0053 compliance and evidence register

Prepared November 25, 2026 against the professor’s manual supplied by Stephen. This is a readiness register, not a claim of full compliance. Status meanings: **Evidence available** = linked local/source evidence exists; **Partial** = some evidence exists; **Pending** = action/verification still required; **Reported** = Stephen’s account, not independently inspected here.

## Mandatory requirements

| Requirement | Status | Evidence / next action | Owner |
| --- | --- | --- | --- |
| Approved app/IDE reuse | Reported | Stephen reports instructor approval for existing Kotlin/Android app reuse. Attach the actual approval record. | Stephen |
| Approved proposal/SRS before full development | Partial | [SRS.md](SRS.md) documents the inherited baseline retrospectively. Obtain approval for this course; do not claim it existed before original development. | Stephen |
| Scope, requirements and acceptance criteria | Draft ready | [SRS.md](SRS.md), FR-01–17 and NFR-01–08; team review pending. | Both |
| Development environment/configuration | Evidence available | [SETUP.md](SETUP.md), [IDE inventory](evidence/cs0053/android-studio.txt), [tools report](DEVELOPMENT_TOOLS.md). [IDE screenshots and captions](evidence/cs0053/SCREENSHOTS.md) now captured. | Stephen |
| Five relevant VS Code extensions or approved equivalents | Approval reported; evidence prepared | Stephen reports instructor approval of the Android Studio substitution on November 25, 2026. Five capabilities are documented with actual formatter/debugger/environment/Git captures and lint/IDE output; they are not five installed VS Code extensions. Attach the original approval record if required. | Stephen |
| At least three tool/extension demonstrations | Partial | Real IDE analysis, Gradle tests/lint, formatter, method-breakpoint debugger, Git and emulator evidence collected. [Screenshot index](evidence/cs0053/SCREENSHOTS.md). Demonstrations support the reported approved Android Studio route; no VS Code extension demonstrations claimed. | Both |
| Naming, formatting, comments and modularity | Partial | Source examples/conventions in tools report. No standalone formatter/linter configuration exists beyond IDE defaults and Android Lint; do not claim a configured formatter plugin. | Both |
| Basic validation/security and .gitignore | Evidence available | PasswordPolicy, repository validation/ownership, ignored properties/keys; [quality review](quality/pre-push-review.md). Final history-sensitive review still required before release. | Both |
| Accessible GitHub repository and README | Partial | [Repository](https://github.com/sheedlibrorania-web/finalproject_cinemood), [README](../README.md), [setup](SETUP.md). Confirm instructor access; repository belongs to original contributor Rasheed. | Stephen |
| 15 meaningful commits distributed through development | Pending | Refreshed `origin/master` at `7ff352d` has 9 reachable commits, including 3 merge commits. This is not 15 substantive development commits. All-ref history snapshot also contains a separate main-line commit; do not count unrelated history to inflate the result. Continue genuine work; no fabricated/backdated commits. | Both |
| At least three feature branches merged after testing/review | Partial | Baseline, setup and requirements PRs merged; requirements merge is PR #6 at `7ff352d`. API/design branch is now in progress. Baseline preservation is not automatically a feature branch; clarify whether documentation branches count, and track API/testing work in genuine focused branches. | Both |
| At least three GitHub Issues | Pending verification | First issue form was shown; creation and issue URLs are not verified. Create/link requirements, API/pattern, and clean-setup/release issues. | Both |
| At least two reviewed PR merges | Reported; merge commits verified | [PR #1](https://github.com/sheedlibrorania-web/finalproject_cinemood/pull/1) and [PR #2](https://github.com/sheedlibrorania-web/finalproject_cinemood/pull/2); merged commits in [Git snapshot](evidence/cs0053/git-history.txt). Stephen reports Mark approved; retain actual review evidence. Requirements PR #6 is also merged in the [refreshed history](evidence/api-design/git-history.txt). | Both |
| Contributions from both CS0053 members | Partial | Mark’s original baseline commit and current Josh commits are in history. Mark’s reported reviews should be linked; later documentation/testing contributions must use actual identity. Preserve Rasheed’s original credit. | Both |
| Final version and release tag | Pending | App version 1.0/code 1 in Gradle; final tested CS0053 tag and release artifact not prepared. Do not tag while acceptance gaps remain. | Stephen |
| Working external API | Evidence available | Fresh AutomaticLiveAccessTest passed against real TMDB on this branch; [verification](API_DESIGN_VERIFICATION.md). Recheck final signed APK separately. | Mark |
| Endpoint/method/parameters/JSON docs | Draft ready | [API manual](API_DOCUMENTATION.md) covers actual endpoints, auth, request/response fixture, moods, images, errors and limits; Mark’s review pending. Text attribution exists; required approved TMDB logo remains a release follow-up. | Mark |
| API errors, timeout and empty results | Partial | New fake-connection HTTP tests passed for 401/403, 429, 503/404, response/read timeouts, I/O and malformed/empty JSON, plus resource cleanup. [Actual results](API_DESIGN_VERIFICATION.md). Release-specific message/UI failure presentation and final signed-build acceptance still need verification. | Mark |
| Implemented design pattern | Draft and behavior evidence ready | [Repository explanation](DESIGN_PATTERNS.md) includes actual class diagram, walkthrough, alternative comparisons and 12 freshly passing SQLite tests; Mark’s review pending. | Mark |
| Complete technical design manual | Draft ready | [Technical design](TECHNICAL_DESIGN.md) has architecture/data-flow/ER diagrams, modules, schema/migration, security and tradeoffs. Review pending. | Mark |
| User manual with screenshots | Partial | Earlier CS0011 manual exists outside this repository; audit it against current CS0053 app and put approved current artifacts in the submission package. | Stephen |
| Test plan/results/quality report | Partial | [baseline review](quality/pre-push-review.md), [actual outcomes](quality/instrumentation-results.csv) and current tool report. Complete SRS traceability, final signed-build tests and pending coverage. | Both |
| Independent clean setup | Pending | Mark must follow [SETUP.md](SETUP.md) from a clean checkout and record OS/JDK/SDK, commit, results and any fixes. | Mark |
| References, AI disclosure, contribution accuracy | Partial | README/SRS acknowledge TMDB, Codex and original contributors. Complete library/tool references in final manuals. | Both |
| Presentation and live demonstration | Partial | Script prepared previously; revise to show actual completed tool/Git/pattern evidence. Record only real results. | Both |

## Review gate for this requirements issue

- [x] SRS review draft prepared with acceptance criteria and explicit limits.
- [x] Compliance register prepared with evidence links and remaining gaps.
- [x] Setup and coding/tool conventions documented.
- [x] Current IDE analysis, emulator screenshot and local test/lint evidence collected.
- [x] Add IDE screenshots/source-directory view, actual formatter demonstration and breakpoint/debugger evidence.
- [x] Record Stephen’s report of instructor approval for the Android Studio tool substitution and complete actual tool demonstrations.
- [ ] Attach the original tool-approval record if required by the instructor (not supplied with the report).
- [ ] Attach course SRS acceptance/approval record.
- [ ] Confirm Stephen/Mark responsibility split.
- [x] Stephen reports Mark approved the requirements PR; merge #6 is verified in refreshed history. API/design PR review is a separate pending step.

Keep the GitHub Issue open until its completion criteria are met. Instructor tool approval is now reported; separate SRS acceptance and the original approval records must still be recorded accurately. Mark’s requirements review/merge is now reported complete. No review request, GitHub message, PR publication or merge was performed during this preparation.

## Next work packages

1. Stephen: retain the instructor’s tool-approval record and confirm the separate SRS acceptance status; tool screenshots are now linked above.
2. Mark: review [API](API_DOCUMENTATION.md), [technical design](TECHNICAL_DESIGN.md), [pattern](DESIGN_PATTERNS.md) and [verification](API_DESIGN_VERIFICATION.md); confirm the TMDB logo follow-up before final release.
3. Both: final requirement-by-requirement test report, independent setup, signed APK verification and release.

Do not add features merely to generate commits. If the inherited history cannot satisfy a development-history requirement honestly, disclose the baseline and ask the instructor how it will be assessed.
