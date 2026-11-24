# CineMood movie access for school builds

Release APK users do not enter a TMDB token. The team supplies authorized movie access configuration before building.

## Configure locally

1. Copy `tmdb.properties.example` to `tmdb.properties` in the project root.
2. Fill in `TMDB_READ_ACCESS_TOKEN` with the authorized API Read Access Token, without quotes or a `Bearer ` prefix. The shorter API key is not interchangeable.
3. Alternatively, configure `TMDB_READ_ACCESS_TOKEN` in the build process environment. The environment value takes precedence, even when empty; remove an unintended override.
4. Sync and rebuild. Release packaging rejects a blank token. Verify the resulting APK against TMDB: accepting a nonblank value at build time does not validate it.
5. Test live search without entering credentials in the release app, then retest mood discovery, posters, saved movies, offline errors and recovery.

`tmdb.properties` is ignored by Git. Never include it in project ZIPs, screenshots, logs or documentation. Each teammate building from source supplies authorized configuration locally. An evaluator can use the separately provided signed APK or follow [source setup instructions](docs/SETUP.md).

## Debug and release behavior

| Build | Movie access |
| --- | --- |
| Fresh debug install | Uses the build token if configured; otherwise uses the labeled six-movie sample catalog. |
| Debug with saved settings | A saved per-device token overrides the build token. Selecting sample mode stores an empty override, which continues to override later rebuilds. |
| Release | Always uses the build token; shows About CineMood instead of token controls. |

For debug, save an authorized token through Developer movie settings or use a fresh emulator to remove an obsolete override. Clearing app storage also removes it but deletes local accounts, watchlists and reviews; use that only on disposable test data.

Build-time tokens are compiled into the APK. Rebuild and redistribute when the token changes or is rotated; changing the local file does not change an installed APK. SQLite accounts and personal records are independent of TMDB access. Failed live requests show errors and do not silently substitute sample movies.

## Distribution limits and attribution

This is the team’s classroom distribution compromise: compiled credentials can be extracted. Ignoring a properties file protects the source repository, not the credential inside the APK. A backend is a future option for public distribution.

This product uses the TMDB API but is not endorsed or certified by TMDB. Live data and images come from [TMDB](https://www.themoviedb.org/). Obtain access through [TMDB API settings](https://www.themoviedb.org/settings/api) and follow the provider’s terms and attribution requirements.
