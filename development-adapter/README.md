# Isolated development adapter

This is a source-only copy of the website's `server/api.mjs`, `app.mjs`,
`admin.mjs`, and `client-ip.mjs`, with marketplace additions. No environment
files, credentials, production settings, website assets, or database were copied.
The original website and downloaded backend remain unchanged.

Requires Node 24.15–24.x. No npm dependencies or installation are needed.

```sh
cd development-adapter
npm test
npm start
```

The launcher starts an in-memory mock backend on an ephemeral loopback port and
the adapter at `http://127.0.0.1:3100`. It does not load environment files.
Only fixed, fake mock credentials are used. Upstream URL validation and the
request transport both reject destinations other than HTTP `127.0.0.1`.
The adapter binds to loopback, not the LAN. Stop it with Ctrl+C.

## Added read routes

| Client request | Backend translation | Response |
| --- | --- | --- |
| `GET /api/listings` | `GET /Listing` with server-owned JSON body | Listing array |
| `GET /api/listings?search=calculus&category=Textbooks` | Same route, filter fields in JSON | Ranked/filtered listing array |
| `GET /api/listings/1` | `GET /Listing/1` with server-owned JSON body | Detail object |
| `GET /api/categories` | `GET /Category` with server-owned JSON body | String array |
| `GET /api/conditions` | `GET /Condition` with server-owned JSON body | String array |

Listing query parameters: `search`, `category`, `isbn`, `user_page`. Duplicate,
empty, unknown, and oversized parameters are rejected. No client-supplied
credentials, forwarding fields, or `for_user` are accepted. Category matching is
the backend's exact name matching. Search preserves the backend's ranking
semantics; it does not promise exclusion of nonmatching titles. There is no
server-side price or condition filter in the inspected contract; these require
client-side filtering. There is no pagination contract.

Responses project known fields and omit seller email and unknown properties.
The list has numeric `id`/`price`, text `title`, `category`, `condition`, `datetime`,
`status`, `user`, nullable `location`, `isbn`, and `image_url`.
Detail has the common fields plus `description`, `first_name`, `last_name`, and
`images_url` (normalized to an empty array when null); use the requested ID as
its identifier. Relative image paths are validated `/uploads/...` paths.
This phase does not add an image proxy or test actual image loading.
The one-item mock has no image and is deliberately not a production dataset.

The original account/session architecture is retained: server-owned application
credentials, service-token forwarding, opaque HttpOnly session cookies, and an
Origin check for non-GET requests. Existing account/admin handler code is copied,
not newly implemented; the mock rejects visitor account writes. Only the mock
service login bootstrap accepts POST, returning a fixed fake token in memory.
No marketplace write routes or messaging routes are added.

## Android integration boundary

Android now consumes these read routes through Retrofit, OkHttp, and Gson at
`http://10.0.2.2:3100/`. Its client rejects other hosts, ports, paths, write
methods, and redirects. Cleartext access is allowed only for that emulator host
in the debug source set; release builds retain Android's cleartext restriction.
See `../LOCAL_ANDROID_TESTING.md` for startup and device checks.
The one-item mock has no images; Android preserves the supplied book artwork as
its placeholder. No remote image requests are made.

## Future production merge review

1. Add `server/marketplace.mjs` and port the import, route recognition, GET-only
   guard, and marketplace dispatch from the copied `server/api.mjs` to the
   original website adapter. Review the diff; do not replace the production file
   wholesale. Keep all existing session, Origin, forwarding, and credential rules.
2. Do NOT merge the development-only loopback restrictions in `backendUrl` and
   `upstream` into production. Do NOT deploy the mock, development launcher, or
   fake fixture settings as production configuration.
3. Add production-repository tests using its existing framework. These local tests
   cover wire-format GET bodies, listing/detail/directories, category filtering,
   search parameter forwarding, secret-field injection, methods, malformed
   responses, session/Origin guards, and unavailable backend handling. They do not
   establish live deployment compatibility or fuzzy-search quality.
4. Confirm production backend/schema compatibility without opening the downloaded
   database. The inspected schema document is older than controller requirements.
5. Decide whether to add a validated image proxy; current detail/listing responses
   contain backend-relative image paths. This copy has no image serving contract.
6. Obtain approval before production testing/deployment. Even backend GET calls
   can update IP records; detail GETs update hit/activity records. The existing
   service-account bootstrap also invokes login. This cannot satisfy a strict
   no-production-database-writes rule merely by restricting client methods to GET.

No ASP.NET or database changes are needed for the four adapter route families if
the deployed backend matches the inspected source. No deployment, GitHub push,
database access, or production requests are part of this development setup.

## Validation results

`npm test`: two tests passed, including real local HTTP requests to the mock.
The sandbox initially blocked loopback sockets; the same tests passed with
permission to bind local sockets. `./gradlew --offline assembleDebug`: BUILD
SUCCESSFUL (33 tasks). Gradle reported native-access and future Gradle 10
deprecation warnings, not compilation errors. The initial sandbox run could not
write the Gradle cache lock; the approved offline run succeeded.
