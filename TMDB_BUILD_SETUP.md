# CineMood movie access for school builds

Users of the release APK do not need a TMDB account or token. The team configures the token before building.

1. Copy tmdb.properties.example to tmdb.properties in the project root.
2. In tmdb.properties, fill in TMDB_READ_ACCESS_TOKEN with the team's Read Access Token. Do not put quotes around it. Alternatively, set the TMDB_READ_ACCESS_TOKEN environment variable for the build process; it takes precedence.
3. Sync and rebuild the project. Build a signed release APK using Android Studio when ready to submit. Release packaging refuses an empty token; a nonempty token must still be verified against TMDB.
4. Test the resulting release APK on a clean emulator or device: search must work without entering credentials in the app. Retest mood discovery, saved movies, offline errors and recovery.

tmdb.properties is ignored by Git. Never include it in a shared project ZIP, screenshots, logs or documentation. Each teammate building from source must configure their own authorized token locally. The professor can use the prebuilt APK; provide source-build setup instructions separately.

Release builds always use the built-in movie access configuration and show About CineMood with credits. Debug builds retain Developer movie settings, an optional local override and sample mode. An existing debug override can take precedence over a newly configured build token; debug settings remain for testing.

This is a classroom distribution compromise. The token is compiled into the APK and can be extracted. Keeping the properties file out of Git does not make the APK credential secret. A backend remains the appropriate future step for public distribution. If the token is rotated, rebuild and redistribute the APK.

Do not silently replace failed live requests with sample movies. Preserve explicit error and retry feedback. SQLite accounts and personal records remain local and independent of TMDB access.
