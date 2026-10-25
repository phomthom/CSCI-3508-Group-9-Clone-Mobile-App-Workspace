# Student Exchange prototypes

The working mobile web prototype, the separate earlier Expo/React Native starter, and the technical guide for the project group. The web version includes marketplace browsing, search and filters, saved items, posting, local profile settings, and practice messaging. The native starter covers welcome and demo-login screens; the marketplace has not been ported into it.

## Contents

| Path | Contents |
| --- | --- |
| `web/dist/` | Complete web application with HTML, CSS, JavaScript modules, SVG artwork, and a web manifest. This folder is intentionally committed. |
| `web/model.test.mjs` | Six cases for filtering and form validation. |
| `mobile/` | Expo source and configuration, three auth tests, and the original welcome/login browser preview. |
| `scripts/` | Local web server and repository checks using Node built-in modules. |
| `docs/` | Word technical guide, GitHub instructions, and backend handoff notes. |

## Run the web prototype

Install Node.js 20.19 or later. From the repository root:

```bash
npm run web
```

Open `http://127.0.0.1:8080/`. The web app, tests, and checks require no `npm install`. Serve the app over HTTP because it uses JavaScript modules; do not double-click its HTML file as a `file://` page.

For a phone on the same Wi-Fi:

```bash
npm run web -- --host 0.0.0.0
```

On the phone, open `http://YOUR_COMPUTER_LAN_IP:8080/`, replacing the placeholder with your computer's actual Wi-Fi IP. `0.0.0.0` is the listening address, not the address to type into the phone. The computer firewall must allow the connection.

The existing private hosted demo is https://student-exchange-phone.marzont.chatgpt.site and requires access through the owner's ChatGPT account. Your new GitHub repository does not automatically synchronize with that deployment.

## Test and check

```bash
npm test
npm run check
```

Tests cover six web model cases and three native starter auth cases. Checks cover JavaScript syntax, JSON configuration, local asset references, and relative hosting paths. They do not bundle or run the native JSX application.

## Try the demo

Choose **Explore without logging in**, or **Fill demo account** then **Log in**. The public sample account is `demo@studentexchange.test` / `CampusDemo!23`. It grants no access to the real Student Exchange service. Search, filter, save an item, create a demo listing, and refresh. Photo handling accepts up to four JPG/PNG/WebP images up to 10 MB each and compresses them for local storage.

Listings, saved item IDs, profile edits, and practice messages stay in browser localStorage. Clearing site data removes those changes. Data is not shared between people, browsers, devices, or hosting origins. Passwords are not stored or transmitted. No real login, reset email, seller message, or payment is sent to a backend. The hosted site's access prompt is separate from marketplace authentication.

## Run the earlier native starter

```bash
cd mobile
npm install
npx expo install --fix
npm start
```

Use a compatible Expo Go version or a development build. The starter is pinned to Expo SDK 54 and may need upgrading for the current Expo Go version. See `mobile/README.md`. Native dependencies were not installed or bundled in the creation environment because registry access was blocked. No APK, IPA, or successful native device build is included.

After editing its shared auth module or preview template, run `npm run build:preview` from the repository root. This regenerates `mobile/preview/index.html`, the original welcome/login browser preview. That preview is separate from the current web marketplace.

## Push to GitHub

Read [docs/GITHUB_SETUP.md](docs/GITHUB_SETUP.md). Extract the ZIP before uploading; upload its source files, not just the ZIP. The package has no Git history, hosting credentials, provider project identifiers, environment secrets, or installed dependencies. Public demo credentials are intentionally included in the source.

The guide is at [docs/Student_Exchange_Technical_Guide.docx](docs/Student_Exchange_Technical_Guide.docx).

## Static hosting and backend work

Publish the contents of **`web/dist/`** as the site root. Asset and manifest paths are relative, including support for repository subpaths. Automatic GitHub Pages deployment is not configured. The Home Screen manifest is included; offline support and a service worker are not implemented.

Read [docs/BACKEND_HANDOFF.md](docs/BACKEND_HANDOFF.md) before connecting shared data or real accounts. No backend routes or schemas have been guessed. Original team backend/repository files were not copied because their contents could not be retrieved during development.

## Verification limits

The original browser checks covered welcome navigation, guest entry, search, saving, posting, refresh persistence, filters, listing details, and practice messaging. Layouts were inspected at 320 px and 390 px. Physical phone gallery/camera selection, Home Screen installation, and native Android/iOS execution remain device checks. Export checks verify the local server and portable assets; they do not establish new device testing.

The team should decide repository visibility and licensing. This export does not assign a software license.
