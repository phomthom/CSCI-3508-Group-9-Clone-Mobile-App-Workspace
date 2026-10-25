# Student Exchange mobile web prototype

Buildless HTML/CSS/JavaScript prototype designed for phone browsers. This is the browser-testing companion to the earlier Expo starter, not a compiled native app.

Try: welcome -> demo login or guest browsing -> search/filter -> save an item -> saved page -> post with optional photos -> my items -> refresh. Account settings, marking your listing sold, removing a demo listing, and practice messaging are local interactions. Product illustrations are original SVGs; listings and sellers are fictional examples.

Storage: browser localStorage, with compressed uploaded photos. No passwords stored or transmitted. No real authentication, registration, reset emails, seller messages, payments or original backend integration. Do not interpret the private hosting access check as marketplace authentication. Changes stay in one browser and do not sync between devices. Browser quotas/private mode can limit persistence; the UI reports storage failures. The site uses optional Google Fonts with system fallback.

Run pure model checks: `node --test model.test.mjs`. Check app syntax: `node --check dist/app.mjs`. Static output is `dist/`; no build or package install required.

Verified in the internal browser: welcome navigation, guest entry, search, saving and the saved page, empty-post validation, creating a demo item, persistence after refresh, combined maximum-price/condition filters, listing details, and practice messaging. Inspected layouts at 390 px and 320 px using embedded phone-sized viewports. Six model tests passed. Physical iOS/Android hardware, camera/gallery file picking and compression, and Home Screen installation remain user-device checks.
