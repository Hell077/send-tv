# Product requirements

[Русский](../docs/ru/product/requirements.md)

## Problem
Sending a URL to a TV is inconvenient: retyping via remote control, incompatible cast protocols, login friction, unreliable screen mirroring. The product is a LAN-first URL handoff tool.

## Personas
- Household user with Android/Google TV and phone.
- Developer sending a current browser tab from a laptop using CLI.
- Guest sending a presentation URL with explicit TV-owner approval.

## V1 primary journey
TV app installed -> opens pairing screen -> displays QR containing local receiver address and short-lived pairing nonce -> sender scans with default phone camera -> sender opens web UI hosted by receiver -> TV prompts to allow connection -> sender pastes HTTPS URL -> TV opens it in available browser -> UI shows meaningful status.

## Functional requirements
- Receiver has stable device identity and human-readable display name.
- QR rotates pairing nonce and expires it; no permanent credential in QR.
- TV confirmation on first pairing; trusted devices can be revoked.
- Validate scheme, URL length and syntax; accept only `http` and `https`.
- Opening uses Android `ACTION_VIEW` with suitable browser/handler; show instructions for installing compatible browser if absent.
- On send failure, show specific error and retry, not fabricated success.
- Support Android TV remote / D-pad navigation for TV UI.
- Support English and Russian; language changes do not alter authorization, device names or URL input.
- Keep sender UI responsive from 320px to desktop size.

## Non-functional requirements
- Works offline within LAN except destination site's own Internet requirements.
- Basic send flow needs no account, subscription, or third-party server.
- No analytics, ads or data collection by default.
- Pairing and open requests rate-limited; store token securely where practical.
- Support accessibility semantics, 4.5:1 body-text contrast, focus indicators, keyboard and D-pad use.

## V1 out of scope
- No generic Samsung Tizen / LG webOS support, Chromecast mirroring, HDMI-CEC, TV power controls, iOS native app, cross-VLAN promises, file transfer, notifications, browser tab mirroring or remote keyboard injection.

## User success criteria
- A new sender can pair on a normal home LAN by scanning QR and approving on TV.
- Trusted sender can open a supported URL in at most 2 actions after launching sender interface.
- Unauthorized sender is rejected and cannot silently open content.
- Browser missing/URL invalid/network missing has documented recovery UI.
