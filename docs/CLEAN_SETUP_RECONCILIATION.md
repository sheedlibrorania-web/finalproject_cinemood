# Returned clean-setup review — reconciliation

Reviewed November 25, 2026 against current local branch `docs/final-manual-and-testing`, HEAD `096b696`. The [received report](evidence/clean-setup/received/CLEAN_SETUP_RESULTS.md.txt) is preserved verbatim. No new build/device execution was performed during this reconciliation.

## Identity and scope

The review came through Mark, but explicitly identifies its operator as Codex for the requester. It tested an extracted `Canete_Cadano_Librorania_CineMood` ZIP on Windows 11/Android Studio 2026.1.4/JBR 25 with a fresh Pixel 6-based API 37 emulator. No Git metadata, repository URL or commit identity was available. No authorized token was configured. This is useful external-environment evidence; it does not complete the latest-GitHub-checkout acceptance step.

The combination of 29 instrumentation methods, the original base-theme API-27 attribute, absent official logo, missing documentation and old README wording strongly indicates a pre-cleanup source snapshot. The exact downloaded commit cannot be established without the reported source manifest, which was not attached. The current branch contains the fixes/documents described below; its historical build evidence is separate from the Windows report and must not overwrite that report’s failures.

## Findings and current disposition

| Reported finding | Current comparison | Disposition |
| --- | --- | --- |
| Lint 1 error / 115 warnings | Current base theme does not set the API-27 attribute; it is in values-v27. Current recorded local lint has 0 errors/8 warnings. | Older-snapshot finding; latest checkout must be retested on Windows. Do not claim this receipt reproduces a current regression. |
| Missing logo | Current VectorDrawable and shared About ImageView exist; current screenshot shows the logo and notice. | Already addressed in this branch, subject to final signed-build acceptance. |
| Missing setup/integration docs and contradictory token wording | docs/SETUP.md, root database notes and rewritten README exist in current source. | Re-test correct checkout/root; the reviewed ZIP was incomplete for current instructions. |
| No live token / real movie access not proven | Report honestly records automatic-live failure, invalid-token connection failure and a skipped live test. | Current independent live-access verification remains open. A missing ignored token file is expected; an authorized local token must be supplied separately, never committed. |
| Scrolling text overlaps system status icons | Current layout roots still use clipToPadding=false. Existing current 08-review.png visibly shows movie metadata behind status icons. | **Confirmed current visual defect** from existing screenshot/source. Needs UI correction and scrolling/keyboard/cutout retest; not fixed in this documentation reconciliation. |
| Singular count grammar | Current WatchlistActivity still builds “1 saved films”. Search count already uses plurals. | Watchlist copy defect remains open; search plural issue was already addressed in current source. |
| Restart helper counts a no-op as passed | Existing reports already disclose the phase-gated method; no separate live two-process restart was performed locally. Windows report describes actual sample force-stop persistence. | Keep both distinctions; do not count either as a live-restart protocol pass. |
| JDK/setup detail | Current gradle/gradle-daemon-jvm.properties explicitly requests toolchainVersion=25. | Clarified SETUP.md: Gradle daemon JDK 25 differs from Java source compatibility 11. |

## Returned outcomes retained as reported

- Debug build/launch and 10 unit tests passed on Windows.
- Lint failed: 1 error/115 warnings on that downloaded snapshot.
- Instrumentation: 29 methods, 26 reported passed, 2 failed, 1 skipped. One reported pass was the no-op restart checkpoint. Do not describe all 29 as passing.
- Sample account/ownership/watchlist/review checks passed. Manual sample-mode force-stop persistence, search/filter/title/year order and compact/150%-font observations are reported. These do not establish current live/release acceptance.
- CLI filesystem/SDK-path obstacles were environmental and recorded separately by the reviewer. Opening the inner Gradle project root resolved the initial Android Studio configuration errors.

Only the narrative report was provided. Its linked screenshots, logs, XML counts and source identity manifests are unavailable locally; these outcomes are attributed to the supplied report, not independently re-executed here.

## Required next steps

1. Merge/review the prepared manual/logo changes as appropriate, then have Mark clone/pull the actual current repository and record `git rev-parse HEAD` and a clean `git status`.
2. Configure authorized movie access locally from tmdb.properties.example; do not share/commit the actual token. Follow docs/SETUP.md using the inner Gradle project root and JDK 25.
3. Repeat lint and live search/posters/moods, then report actual results and attach nonsecret raw evidence.
4. Correct the current status-bar overlap and watchlist singular/plural copy in a separately verified change. Preserve this finding as a defect until retested.
5. Keep the current team-signed APK, API24/runtime/accessibility and release-gate checks open. The returned report does not pass them.

The team can continue useful review work, but should not tag the final release or mark the clean-setup Issue fully complete yet.
