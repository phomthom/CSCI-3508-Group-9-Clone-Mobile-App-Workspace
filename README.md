# StudentExchange Android

Native Android application preserving the supplied StudentExchange designs and
navigation, connected through a server-side development adapter. The default
backend is an isolated in-memory mock; no production requests are enabled.

See [LOCAL_ANDROID_TESTING.md](LOCAL_ANDROID_TESTING.md) for build/run instructions
and [BACKEND_INTEGRATION_STATUS.md](BACKEND_INTEGRATION_STATUS.md) for the inspected
backend revision, exact API matrix, validation results and live integration requirements.

Implemented screens include onboarding, registration/login with school selection,
marketplace search/categories/price/condition filters, saved finds, product details
and image galleries, profiles, posting and own-listing management, conversation
inbox, messages and offers. Offers do not represent payments or accepted purchases.

The adapter owns application credentials and backend user tokens. Android receives
only an opaque process-memory session cookie and public API data. No application
secret belongs in Android source, Gradle or APK. IP/admin-token forwarding is disabled.

```sh
./gradlew --offline --no-configuration-cache assembleDebug test lintDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`. Requires Android 8.0/API 26+.
Use Android Studio's bundled JDK and the SDK path in ignored `local.properties`.
The old `tools/build-offline.sh` predates Retrofit dependencies; use Gradle.

Mock accounts: `demo@mock.invalid` and `seller@mock.invalid`, password
`CampusDemo123!`. These are public fake fixtures. Use sample credentials only.
App process or adapter restart requires login. Saved finds remain on-device.

Original screenshot-derived artwork remains in `app/src/main/res/drawable-nodpi/`.
Native widgets implement the UI; full screenshots are not used as interactive screens.
Device-selected photos are downsampled before uploading through the adapter.

Work stays on `robert-android-app`. No commits/pushes, production database changes,
website changes, backend edits or deployment are included.
