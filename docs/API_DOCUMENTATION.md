# CineMood API integration manual

Prepared November 25, 2026 for CS0053. Baseline: `7ff352d`; current branch adds a connection-construction test hook and HTTP tests. Review draft for Mark Jayson D. Cadano. The source is authoritative for CineMood’s implemented subset; provider references describe available API behavior, not additional implemented features.

## Provider and selection

The Movie Database (TMDB) supplies movie metadata and poster paths suitable for title search and genre-based discovery. CineMood uses application-level access, not TMDB user login. Local CineMood credentials, saved ratings and notes are never sent to TMDB. The app does not stream movies or synchronize watchlists with TMDB.

Source: [TmdbClient/MovieService/TmdbSettings](../app/src/main/java/com/example/cinemood/network/TmdbClient.kt), [PosterLoader](../app/src/main/java/com/example/cinemood/network/PosterLoader.kt).

## Authentication and configuration

Requests use HTTPS and an `Authorization: Bearer <API_READ_ACCESS_TOKEN>` header plus `Accept: application/json`. The credential is not a query parameter. Obtain an authorized API Read Access Token, not the shorter API key. See [TMDB application authentication](https://developer.themoviedb.org/docs/authentication-application).

Build configuration loads ignored `tmdb.properties`; `TMDB_READ_ACCESS_TOKEN` in the build environment takes precedence, even if empty. Rebuild after changing build configuration. Release packaging rejects a blank token, but does not establish validity. Release always uses the build token; About replaces developer controls. Debug uses a saved per-device override when present, including an empty override for sample mode; otherwise it uses the build token. No token in a fresh debug install selects the labeled six-movie sample catalog. See [build setup](../TMDB_BUILD_SETUP.md) and [source setup](SETUP.md).

Compiled tokens can be extracted from the APK. This is the agreed classroom arrangement, not production secret protection. Rotation requires rebuilding and redistributing. Never commit real credentials or include authorization headers in screenshots, logs or issue reports. The synthetic token used in tests has no service privileges.

## Implemented endpoints

Base URL: `https://api.themoviedb.org/3/`. All requests use GET, default behavior of HttpURLConnection. There are no write requests to TMDB.

| Endpoint | Trigger | Parameters actually supplied |
| --- | --- | --- |
| `search/movie` | User submits nonblank movie title | `query` trimmed and URL-encoded; `include_adult=false`; `language=en-US`; `page=1`. |
| `discover/movie` | Initial Search browsing or Mood browsing/selection | `include_adult=false`; `sort_by=popularity.desc`; `language=en-US`; `page=1`; `with_genres` and `without_genres` when the selected moods require them. |
| `https://image.tmdb.org/t/p/w342{poster_path}` | A visible movie row/details screen loads its poster | Validated path appended to fixed HTTPS image URL; no TMDB bearer header is used for image downloads. |

Provider references: [search](https://developer.themoviedb.org/reference/search-movie), [discover and filter semantics](https://developer.themoviedb.org/reference/discover-movie), [image URL structure](https://developer.themoviedb.org/docs/image-basics).

**Details screen boundary:** CineMood does not currently call `/movie/{id}`. It opens the Movie payload from the search/discovery response, or saved SQLite metadata. Do not demonstrate a nonexistent separate details API request.

## Sanitized request and illustrative response

This is a reproducible request shape with a placeholder header, not a captured request containing a real credential:

```http
GET /3/search/movie?query=A%26B+movie&include_adult=false&language=en-US&page=1 HTTP/1.1
Host: api.themoviedb.org
Authorization: Bearer <API_READ_ACCESS_TOKEN>
Accept: application/json
```

This reduced, invented fixture illustrates the response structure. It is not evidence of a live TMDB response:

```json
{
  "page": 1,
  "results": [
    {
      "id": 42,
      "title": "Fixture movie",
      "adult": false,
      "release_date": "2024-01-01",
      "genre_ids": [35, 16],
      "overview": "Illustrative movie description for documentation.",
      "poster_path": "/fixture.jpg"
    }
  ],
  "total_pages": 1,
  "total_results": 1
}
```

A successful parsed Movie has ID 42, year 2024, genre “Comedy / Animation”, the overview and path, and source TMDB. Live TMDB results vary over time. The previous real-access test is separately recorded in [baseline evidence](quality/instrumentation-results.csv); the new HTTP tests are deterministic fixtures and do not replace live verification.

## Parsing and display

`parseMovies` requires a JSON `results` array. It skips non-object entries, nonpositive IDs, blank titles and entries marked adult; removes duplicate movie IDs; maps known genre IDs locally; and derives year from the first four release-date characters, falling back to 0. Missing genres become “Genre unavailable”; absent overview becomes “No overview available.” Poster paths are retained only when slash-prefixed, with stricter validation in PosterLoader.

Malformed JSON or a missing results array throws a parsing exception. BaseActivity maps non-service failures to a generic loading message; the client does not fabricate sample results. Empty valid results remain an empty list and the Activity shows an empty state. Top-level pagination totals are ignored: the implementation displays page 1 only.

`SearchActivity` validates explicit blank submissions but uses discovery for its initial empty browsing state. `MoodActivity` debounces typed mood searches for 400 ms and suppresses stale callbacks with a request version. The version guard prevents old results replacing newer results; it does not cancel requests already running. Network work uses BaseActivity’s background executor, and rendering returns to the UI thread.

## Mood rules

UI wording is mapped to internal keys by quick-pick buttons/MoodSearch. Rules are not machine learning or mental-health recommendations.

| UI label | Internal key | Included genre IDs / names | Excluded genre IDs |
| --- | --- | --- | --- |
| Upbeat | Happy | 35 Comedy, 16 Animation | 27, 53, 80 |
| Smitten | Romantic | 10749 Romance, 35 Comedy | None |
| Amped | Excited | 28 Action, 12 Adventure | None |
| Spooked | Scared | 27 Horror, 53 Thriller | None |
| Intrigued | Curious | 9648 Mystery, 99 Documentary | None |
| Chill | Relaxed | 10751 Family, 10402 Music, 14 Fantasy | 27, 53, 28, 12, 80 |
| Wistful | Sad | 18 Drama, 10749 Romance | None |
| Anything | No selected key | No inclusion filter | None |

For multiple matched moods, inclusion IDs are combined without duplicates and pipe-separated (`OR`). Exclusions are combined, then any explicitly included genre is removed from the exclusions; remaining exclusions are comma-separated. Different requests may legitimately return overlapping movies. Example Chill query includes `with_genres=10751%7C10402%7C14` and `without_genres=27%2C53%2C28%2C12%2C80`. See [MoodSearch](../app/src/main/java/com/example/cinemood/MoodSearch.kt) and [mood UI](../app/src/main/java/com/example/cinemood/MoodActivity.kt).

## Poster handling

PosterLoader starts with the reel placeholder, accepts only a path matching `/[A-Za-z0-9._-]+`, and uses two background workers plus an 8 MiB in-memory bitmap LRU cache. Connect/read timeouts are eight seconds each. Non-200 responses, decoding and network failures retain the placeholder. A path tag prevents assigning a completed image to a view that now represents another path. This is a memory cache, not an offline disk cache. Saved metadata can be read offline, but a poster not in memory may be unavailable offline. The provider describes configuration-driven image sizing; this app currently hardcodes `w342` and does not call `/configuration`.

## Error behavior and repeatable verification

| Condition | Client/UI behavior | Executed evidence / limit |
| --- | --- | --- |
| Blank explicit title | Reject before a request. | Existing TmdbClientTest validation. |
| Missing token | MovieServiceException asks user to contact the team. | Existing validation test. |
| HTTP 200 | Parse valid results, including empty arrays. | New HTTP success/empty tests and parser tests. |
| HTTP 401 / 403 | Debug suggests checking token settings; release uses a contact-team message. | New fake-connection test executes debug branch; release message is source-reviewed, not release-device tested here. |
| HTTP 429 | “TMDB is busy. Please try again shortly.” | New fake-connection test. No deliberate live rate-limit traffic. |
| HTTP 503 / other status (404 tested) | “TMDB is unavailable. Please try again later.” | New fake-connection tests. |
| SocketTimeoutException obtaining/reading response | “The request timed out. Please try again.” | New tests inject both phases; they do not wait for a real socket timeout. |
| IOException | “Cannot reach TMDB. Check your internet connection.” | New injected I/O test; historical offline check exists separately. |
| Malformed JSON | Parsing fails; screen-level generic failure message. | New parser-failure HTTP test verifies exception/resource cleanup; screen message mapping source-reviewed. |

API connections have independent 12-second connect/read timeouts, not a guaranteed 12-second end-to-end limit. Response streams are closed and connections disconnected in finally. No automatic retry, Retry-After handling, exponential backoff or global client rate limiter exists. Users retry through the existing controls; avoid repeatedly submitting requests or bulk scraping. TMDB says limits can change and clients must respect 429 responses: [rate-limit guidance](https://developer.themoviedb.org/docs/rate-limiting).

## Test architecture and evidence

`TmdbClientTest` uses the existing body-returning transport hook to inspect query strings/JSON; it bypasses HTTP status handling. `TmdbHttpTest` uses the new optional connection factory, returning a fake HttpURLConnection so the real header, response-code, catch and cleanup code executes. Production’s default factory still opens the fixed HTTPS URL. No feature, endpoint, credential behavior or dependency was added. See [API/design verification](API_DESIGN_VERIFICATION.md) for actual results and rerun commands.

## Attribution, terms and release follow-up

The app currently displays TMDB’s required notice and website in About/credits. Provider guidance also requires an approved TMDB logo in that section, subordinate to the app brand. The inspected layout has text attribution but no TMDB logo; this is an open release follow-up, not a completed compliance claim. No UI change was made in this task. Review an approved logo addition with the team before final packaging. See [TMDB attribution/FAQ](https://developer.themoviedb.org/docs/faq). Commercial/public distribution requires a separate review of the provider’s applicable terms and the classroom token arrangement.

All provider references were consulted November 25, 2026. Mark should review endpoint/filter details, limitations and the attribution follow-up against his implementation before approving the PR.
