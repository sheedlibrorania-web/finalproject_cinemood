# CineMood project guidance

- CineMood is an Android app using Kotlin Activities and XML layouts, SQLite, and TMDB. Preserve the existing architecture and working behavior unless the team explicitly requests a migration.
- Use the team's reference screenshots to guide the visual direction when provided. Share colors, typography, spacing, and component styles through Android resources; do not assume a particular palette before the team chooses it.
- Keep navigation accessible without scrolling to the bottom. Avoid artificial loading delays; show loading and error states for real work.
- Preserve account isolation, watchlist CRUD, persistence, and sample and live movie modes. Never expose API tokens in source, logs, screenshots, or documentation.
- Check layouts against system bars and display cutouts, different screen sizes, larger text, touch targets, contrast, and screen-reader labels.
- When changing UI or navigation, build and test the affected flows and inspect the running emulator when available. Record actual results and any defects and fixes for the course documentation.
- Preserve existing uncommitted work. Coordinate commits and shared changes with the team.
