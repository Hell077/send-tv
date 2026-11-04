# MVP V1 — Pair and open a URL

## Objective
From phone browser scan QR on Android TV, approve first device on television, paste URL, and reliably hand it off to installed TV browser.

## Scope / implementation order
1. Android TV Compose or Views shell with focusable 10-foot pairing home and settings.
2. Embedded HTTP server with static web assets from TV app; configurable internal port, explicit network lifecycle, bind only permitted interfaces.
3. Pairing challenge state machine, QR rendering, TV approval, trusted token persistence, revocation and rate-limits.
4. `GET /v1/info`, pairing, `POST /v1/open`, operation status. Implement current `protocol/openapi.yaml`.
5. Strict URL validator + Android `ACTION_VIEW`; detect missing handler; no implicit elevated permissions.
6. Sender responsive interface: idle, connecting, waiting for TV approval, connected, sending, launched, invalid URL, inaccessible receiver, missing browser.
7. Error UX, accessibility, privacy / security review, offline LAN testing.
8. Documentation: install APK, network requirements, permissions, browser behavior, troubleshooting, screenshots.

## Acceptance criteria
- [ ] Android TV receiver installable on supported Android API floor selected and documented after testing.
- [ ] TV UI D-pad navigable; QR shows a short-lived challenge URL.
- [ ] New sender pairs only after TV-side approval; expired/reused challenges fail.
- [ ] A valid HTTPS URL opens through compatible installed handler.
- [ ] Unsupported scheme, malformed URL and missing browser show clear errors.
- [ ] Revoke sender immediately disables future commands.
- [ ] Auth + pairing negative tests pass; no logging of full URL secrets.
- [ ] Sender works on mobile Safari/Chrome and desktop current browsers where LAN fetch is permitted.
- [ ] Site has public GitHub Pages landing and getting started docs; no live TV connection required on website.

## Test matrix
Android TV emulator plus at least one real device; Chrome Android, Safari iOS, Chrome macOS/Windows; same Wi-Fi, no Internet, phone on isolated guest network, first pairing, re-pair, revoked device, browser absent, URL with credentials, QR expired, app process restart. Note physical testing that was not performed.

## Do not implement
File transfer, Google Cast, mirroring, cross-platform TV apps, remote mouse injection, mandatory TLS without a trust model, discovery in raw web browser.
