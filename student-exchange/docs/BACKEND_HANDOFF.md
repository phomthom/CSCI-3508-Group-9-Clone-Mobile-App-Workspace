# Backend integration handoff

## Current implementation

`web/dist/app.mjs` manages interface state and local browser persistence. `web/dist/model.mjs` exports sample data, filtering, and validation. The `mobile` folder contains a separate welcome/login starter. No original backend source or live API integration is included.

## Verify these contracts with the backend team

- Authentication: actual login route, request fields, sessions/tokens, expiry, logout, and recovery.
- Listings: schema, search/pagination, creation, ownership, editing, sold state, and deletion.
- Schools: real response schema and identifiers; current campus options are static examples.
- Saved items: authenticated ownership and shared storage.
- Photos: permitted types/sizes, upload route, storage references, and access rules.
- Messaging: recipients, authorization, thread history, delivery, and any real-time connection.
- Errors: validation rules and stable response/error formats.

## Replacement points

Replace the public sample credential comparison with verified backend authentication. Replace browser-only persistence with API adapters where shared records are needed. Replace seed listings and campus examples with verified data. Upload photos to server storage and retain references. Connect real messaging only after recipient and authorization rules are established.

The browser `session` flag and public demo password are not production authentication. Local user text is escaped before HTML insertion, but production security and authorization still require independent review. Never store raw passwords; determine session handling from the real backend contract.

## Original references

- https://studentexchange.onrender.com/
- https://github.com/University-of-Colorado-Anschutz-Denver/CSCI-3508-Group-9/tree/main/documentation/Frontend/Website
- https://github.com/triscroa/CSCI-3508-Group-9-Clone/blob/main/documentation/Backend/readme.md
- https://studentexchangebck-fpdnfpcqe4bje2eh.westus-01.azurewebsites.net/Schools/

These sources were inaccessible during the original build. The group should inspect the actual documentation before integration; request fields and routes have not been inferred from those URLs.
