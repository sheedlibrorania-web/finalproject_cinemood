# Signed APK acceptance — CineMood 1.0

Executed November 25, 2026 by Codex at Stephen’s request. Tested the exact user-generated APK at `app/release/app-release.apk` on a fresh, separate Android 17/API 37.1 emulator. No existing user installation was uninstalled.

SHA-256: `18d0985692f3383dd2567272bf27106ab3853dc3c9c48809503001b12114ee1a`

**Result: all 14 focused checks passed.** This establishes acceptance of these paths, not 100% coverage of all devices and behaviors.

| Check | Status | Actual result |
| --- | --- | --- |
| Signature | Pass | Original team certificate; APK Signature Scheme v2 verification succeeds. |
| Artifact identity | Pass | Non-META-INF ZIP entries exactly match isolated clean release build. |
| Fresh install and launch | Pass | Installed exact signed APK on blank CineMood_Final_QA Android 17/API37.1 emulator. |
| Registration and login | Pass | Created dedicated releaseqa1125 account and signed in. |
| Automatic live search and posters | Pass | Default live collection and Interstellar search returned real TMDB titles and posters without token input. |
| Mood switching | Pass | Upbeat: Guarding Stars, Steps, Ebenezer. Chill: Ebenezer, 9 Songs, Home Alone; overlapping titles allowed. |
| Watchlist save | Pass | Saved Iron Man; watchlist displayed poster, watched status and singular count. |
| Review persistence | Pass | Four-star rating, watched status and exact test note survived force-stop, relaunch and reopening from watchlist. |
| Release About | Pass | Official logo and attribution visible; token entry and developer mode controls absent. |
| Offline request | Pass | Network disabled on QA emulator: meaningful Cannot reach TMDB / retry message; no silent sample fallback. Wi-Fi and cellular restored. |
| Delete review only | Pass | Confirmed deletion cleared all stars and note while retaining saved movie and watched status. |
| Removal Cancel and Remove | Pass | Cancel preserved entry; confirmed Remove cleared it and watchlist showed empty state. |
| Logout and Back | Pass | Logout returned to sign-in; Back went to launcher instead of authenticated dashboard. |
| Crash buffer | Pass | No entries in emulator crash buffer after checks. |

## Evidence

- [Signature verification](signature-verification.txt) and [artifact identity](artifact-verification.json).
- [Live search](search.png), [saved review](review.png), [after process restart](restart.png), [watchlist](watchlist.png), [mood results](mood-results.png), [release About](about.png), [review deletion](deleted-review.png).
- [Machine-readable results](test-results.json); layout snapshots for About, both moods and offline response are in this folder.

## Limits and remaining submission work

- Focused signed APK smoke checks, not exhaustive device/accessibility/security coverage.
- The 40-pass/1-skip instrumentation suite and 10 unit tests ran separately on debug/isolated source; they were not injected into the signed release.
- Minimum Android 7 compatibility declared but not exercised on API24 hardware.
- No app source changes were made during signed APK verification.
- QA account remains only in dedicated emulator; its test movie was removed and account logged out.
- Final Git review/merge, release tag, submission package and recording remain separate from APK acceptance.
- Minor existing About wording still mentions sample mode although its developer controls are hidden in release; it did not block live operation.
