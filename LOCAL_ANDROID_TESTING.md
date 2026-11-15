# Local Android marketplace

Start the mock-only adapter on your Mac (Node 24.15–24.x):

```sh
cd ~/Documents/StudentExchangeMobile/development-adapter
npm test
npm start
```

Leave that terminal running. It prints `http://127.0.0.1:3100/api/listings`.
There are no dependencies to install, no environment file to create, and no real
credentials to provide. The adapter uses a local in-memory mock and rejects
non-loopback backend URLs. Do not change its upstream guards.

Open `~/Documents/StudentExchangeMobile/` in Android Studio. Sync Gradle, select
an Android emulator running API 26 or newer, choose the debug variant, and Run
`app`. Use Android Studio's bundled JDK. Gradle resolves Retrofit 2.11.0,
converter-gson 2.11.0, OkHttp 4.12.0, and Gson 2.11.0 from Maven Central.

Android connects to `http://10.0.2.2:3100/`: the standard emulator alias for your
Mac's loopback interface. This setup is for the Android emulator, not a physical
phone. No `adb reverse` is necessary. Only the debug manifest permits cleartext,
and only for `10.0.2.2`; the main manifest does not enable global cleartext.

Skip onboarding or choose Explore without logging in. The original cards,
welcome screen, navigation, colors, icons, and images remain. Marketplace data
comes exclusively from the adapter; previously seeded/local listings are not
loaded into the marketplace. Existing local preferences are not deleted.

## Checks

1. Explore displays `Mock calculus textbook`, `$25`, and `Mock campus`.
2. Tap the title or image. The details request loads its description, condition,
   and `Mock Seller`; Back returns to the marketplace.
3. Category chips come from `/api/categories`. Textbooks shows the mock item;
   Electronics shows an empty state. All finds restores it.
4. Type in search. Requests are debounced by 350 ms and older requests are
   canceled. Search uses the backend's ranking semantics; the single mock item
   remains even for unrelated search text.
5. Filters contains choices from `/api/conditions` (`New`, `Good`). `New` hides
   the item; `Good` displays it. Price and Used items only also filter locally.
   Reset returns to all finds and conditions.
6. Save the mock item, navigate to Saved, reopen details, and return to Explore.
   Saves persist locally; no shared saved-item endpoint is called.
7. Rotate in marketplace/details. The screen reloads from the mock API, retaining
   search/category/condition/price state. Pending callbacks are canceled on
   destruction and ignored after navigation.
8. Stop the adapter and reopen Explore. A connection error and Retry appear.
   Restart `npm start` and tap Retry. Details also provide retry on failure.
9. Post remains reachable but reports unavailable. Message seller reports
   unavailable. Accounts are the existing device-local demo, not shared accounts.

The fixture has no product images; supplied book artwork is used as a placeholder.
There is no production connection, image download, authentication API integration,
posting API, or messaging API in the Android client.

Build with `./gradlew assembleDebug`. APK:
`app/build/outputs/apk/debug/app-debug.apk`. The legacy `tools/build-offline.sh`
cannot package Maven dependencies and should not be used for this version.

Changed files: `app/build.gradle`, main manifest, `MainActivity.java`, root README,
development-adapter README. Added: debug manifest/network-security XML,
`api/ApiClient.java`, `api/MarketplaceApi.java`, and this guide. All changes stay
inside StudentExchangeMobile. The original website and downloaded backend source
are untouched. No server secrets are in Android; no production API is contacted.

## Verification performed

`./gradlew assembleDebug` completed successfully. Retrofit/OkHttp/Gson were
resolved, Java compiled, resources merged, and the debug APK packaged. Warnings
concern deprecated Android APIs, JDK native access, and future Gradle 10 support;
there were no compilation errors.

Installed and launched the APK in `emulator-5554`. Verified the mock listing,
detail description/location/seller, category filtering to an empty result,
API-supplied New/Good choices, New excluding and Good restoring the mock item,
Saved navigation, and the disabled Post screen. Stopping the mock adapter then
reopening Explore displayed the connection error and Retry button. The adapter
was stopped after testing; start it using the instructions above before running
the app. Rotation, search typing, and retry recovery remain manual checks.

`tools/check-local-emulator.py` repeats the category/condition/detail/navigation
smoke checks after onboarding, with the mock adapter running. It uses the local
SDK's ADB and the emulator named `emulator-5554`.
