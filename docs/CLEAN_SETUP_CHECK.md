# Independent clean-setup check — received results and latest-source retest

Status: **returned review received; not a clean pass; latest GitHub checkout verification still pending**. See [reconciliation](CLEAN_SETUP_RECONCILIATION.md) and [original received report](evidence/clean-setup/received/CLEAN_SETUP_RESULTS.md).

## Received review identity

- Report received through Mark; stated operator: Codex for the requester, not a signed statement that Mark personally executed the checklist.
- Date: November 25, 2026, Asia/Singapore (same UTC offset as Manila).
- OS: Windows 11 build 10.0.26300.0, amd64.
- IDE/JDK: Android Studio 2026.1.4, JBR 25.0.3; Gradle 9.6.0/AGP 9.4.1.
- Device: fresh Pixel 6-based emulator, Android 17/API 37; additional compact/150%-font sample checks reported.
- Source: extracted ZIP, no repository/commit identity. Latest checkout not verified.
- Authorized token: no. Live/release success not established.
- Evidence received: narrative report only. Referenced screenshots/XML/logs/manifests were not attached.

## Returned checklist result

| Step | Reported actual result | Status / current limit |
| --- | --- | --- |
| Clone/open root | Extracted ZIP; inner CineMood root syncs | Partial; no clone/commit identity |
| SDK/JDK and sync | Succeeds after environment workaround | Reported pass for that environment |
| Authorized local movie configuration | Not configured | Live checks blocked |
| Debug build/launch | Succeeded | Reported pass for downloaded snapshot |
| Accounts/local records | Registration/login/isolation/reviews checks passed | Sample-mode/local evidence |
| Real search/posters | Sample result only; live check failed without token | Not passed |
| Two moods | Sample Upbeat/Spooked results differed | Sample-only pass |
| Persistence | Sample watched/rating/note/session survived force-stop/relaunch | Reported sample persistence pass |
| Watchlist controls | Filter/title/year order and no-match checks worked | Reported sample pass |
| About attribution | Logo absent in reviewed copy | Failed snapshot check; logo now present in current branch |
| Unit/lint/device suite | 10 unit pass; lint 1 error/115 warnings; device 26 pass/2 fail/1 skip | Not a clean pass; old-snapshot identity must be resolved |
| Handoff instructions | Missing docs and old wording | Failed snapshot handoff; current docs supplied in repository |

## Latest-source retest record — still to complete

- Operator/date: pending
- Repository URL and exact commit (`git rev-parse HEAD`): pending
- Clean working tree (`git status`): pending
- OS/IDE/Gradle daemon JDK 25/SDK/device: pending
- Authorized token configured locally (yes/no only): pending
- Current lint counts, unit/device results and log locations: pending
- Live title search/posters/two moods and error recovery: pending
- Current official logo/notice in About and signed-release acceptance: pending
- Defects/fixes/retest outcomes: pending

Retain failures honestly and link later fixes. Do not copy private tokens/signing files into the evidence. The current status-bar overlap and “1 saved films” copy remain open findings; see the reconciliation. No latest-source clean pass, Mark-personal execution, original approval or final signed-APK acceptance is inferred from this report.
