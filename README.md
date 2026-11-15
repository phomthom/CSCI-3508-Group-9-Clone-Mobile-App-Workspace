> The marketplace now reads the local mock API using Retrofit/OkHttp/Gson.
> See [LOCAL_ANDROID_TESTING.md](LOCAL_ANDROID_TESTING.md) for current setup.
> The offline build script below predates external dependencies; use Gradle for
> this version. Accounts remain device-local demos; posting and seller messaging
> are disabled in this read-only integration. The older prototype notes below
> describe the original implementation, not the connected marketplace.

# StudentExchange Android

Native Android application styled from all four supplied StudentExchange screenshots. Project source, extracted assets, build scripts, and APK are contained in this directory. The original four screenshots remain untouched.

## Open and run

Open this folder in Android Studio, select a device running Android 8.0 (API 26) or later, and run `app`.

Build with the included Gradle wrapper:

```sh
./gradlew assembleDebug
```

The project uses Android Gradle Plugin 9.3.3, Gradle 9.5.0, Java 17 source compatibility, and Android SDK 34. Let Android Studio use its bundled JDK. Update the ignored `local.properties` SDK path when moving this project to another computer.

A command-line fallback is included for environments where sandbox restrictions prevent Gradle from opening its local sockets:

```sh
./tools/build-offline.sh
```

The fallback uses the installed Android SDK platform 34, build tools 36.0.0, and Android Studio JDK. Set `ANDROID_HOME` and `JAVA_HOME` for other installations. It compiles resources and Java, produces DEX, aligns the APK, signs with a project-local debug key, and verifies the signature. Do not run concurrent builds in this directory. The fallback's debug key is under `app/build/offline/`; Gradle uses its standard debug key, so switching build methods may require uninstalling the prior debug install.

APK: `app/build/outputs/apk/debug/app-debug.apk`

Install with Android Studio or:

```sh
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Implemented screens and interactions

- Onboarding from the supplied registration screenshot, with Continue, Skip, and Log in.
- Separate registration and login screens matching the supplied login screenshot. Email and password validation, password visibility, duplicate-account checks, and demo autofill.
- Marketplace with supplied artwork, responsive two-column cards, text search, horizontal category navigation, maximum-price and condition filters, and reset.
- Product detail screen with artwork, price, condition, category, campus, description, saved state, and seller-message entry.
- Saved finds persist across launches.
- Posting validates title, price, campus, and description. Choose category and condition, reuse the provided sample illustrations, or select a device photo with a preview and persistent read permission.
- Account screen with local profile, own listings, listing removal, logout, and onboarding replay.
- Inbox saves outgoing messages, timestamps, and links back to listings.
- Core screen location and text fields restore on activity recreation; password fields are intentionally not copied into saved instance state.

## Local prototype boundary

No server, website API, or authentication provider was supplied. This application is fully usable as an **offline prototype**. Accounts, listings, saved items, and outgoing messages persist on this device. Messages are not delivered to other people; no email verification or password-reset email is sent. The recovery dialog explains this limitation. Register with sample credentials rather than a real password. Local account passwords use a unique salt and PBKDF2-HMAC-SHA256. No network or broad storage permissions are requested.

Demo login:

- Email: `demo@university.edu`
- Password: `CampusDemo123`

The sample account address is reserved. Two genuine sample listing illustrations are available in the supplied screenshots, so the initial marketplace contains headphones and a calculus textbook rather than invented stock photographs.

## Reference and asset mapping

| Original reference | Application use |
| --- | --- |
| `homescreenexample.png` | Marketplace colors, banner, categories, cards, navigation, and extracted calculus artwork |
| `clicking on a item example.png` | Product detail design, listing copy, price, campus, and extracted high-resolution headphones artwork |
| `loginexample.png` | Login and registration form styling and extracted StudentExchange wordmark and launcher mark |
| `registration example.png` | Onboarding layout, welcome copy, calls to action, and extracted book illustration |

Extracted images are in `app/src/main/res/drawable-nodpi/`. Native Android widgets implement the interfaces; full screenshots are not used as fake interactive screens. The bottom navigation icons are drawn as density-independent paths. Product images scale within native image views; user-selected photos are downsampled before display.

## Validation

The application has been compiled with `javac`, resource-linked with `aapt2`, converted to DEX with `d8`, packaged, aligned, and debug-signed. Package metadata and bundled image resources are inspected separately. The Gradle wrapper launches successfully, but a full Gradle build is blocked in this execution environment by restricted socket access. ADB is blocked by the same restriction, so emulator interaction and runtime image rendering have not been verified here.

Run the device checks in `DEVICE_CHECKS.md` before treating the prototype as release-ready. This is a debug build, not a production-signed Play Store release.
