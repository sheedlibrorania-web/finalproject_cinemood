# Current user-manual figures

Captured November 25, 2026 on Pixel 10a / Android 17 API 37.1 from baseline `e060a21` plus the authorized TMDB-logo change. These are real debug-build UI screenshots, not mockups or images from a newly team-signed release. Operator: Codex at Stephen’s request, using DocumentationCaptureTest and live TMDB. The temporary account and saved movie/review are cleaned up afterward; no private token/password is displayed.

| Figure | Actual content |
| --- | --- |
| [01-login.png](01-login.png) | Login form with no entered credentials. |
| [02-register.png](02-register.png) | Registration and password confirmation controls. |
| [03-dashboard.png](03-dashboard.png) | Dashboard with disposable documentation account. |
| [04-search.png](04-search.png) | Actual Interstellar live results and available posters. |
| [05-mood.png](05-mood.png) | Chill selected among mood options. |
| [05b-mood-results.png](05b-mood-results.png) | Scrolled live recommendations from the selected mood. |
| [06-watchlist.png](06-watchlist.png) | Saved live movie, watched status, search/filter/sort controls. |
| [07-details.png](07-details.png) | Live movie poster/title/details, saved state. |
| [08-review.png](08-review.png) | Scrolled journal with four-star rating, illustrative notes and review actions. |
| [09-remove-confirmation.png](09-remove-confirmation.png) | Real confirmation dialog; Cancel selected by the test afterward. |
| [10-about-credits.png](10-about-credits.png) | Scrolled shared credits card with official TMDB logo, notice and website. Debug may show developer controls above; release hides that card. |

The screenshots are not altered to imply a different build. Scroll position was adjusted through the actual UI so long pages show the relevant controls. Poster data and TMDB rankings may change. Captured JSON data are not republished as a claimed stable response fixture.

## Capture verification and iteration

The complete 40-method suite, including the initial capture, passed: [runner output](instrumentation-check.log). Visual review found that the first credits/review images needed further scrolling. The first refinement mistakenly cast the existing NestedScrollView to platform ScrollView, causing the **capture test harness** to fail; application source was unaffected. The cast was corrected to the actual AndroidX widget and the targeted capture was rerun. Preserve [failed iteration](capture-iteration.log) and [final targeted result](capture-final-check.log) as the honest correction record; do not hide the failed harness run or call it an application defect.

The final figures were visually inspected. The full suite is not rerun merely because capture-only scrolling changed. Current build/lint and broader test limits are in [TEST_REPORT.md](../../TEST_REPORT.md). Mark’s independent setup and the exact final team-signed APK remain separate pending acceptance work.
