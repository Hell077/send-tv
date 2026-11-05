# ADR 0002 — Initial V1 Android floor

[Русский](../../docs/ru/architecture/decisions/0002-v1-android-floor.md)

Status: Accepted for the initial V1 compatibility scope.

## Decision

V1 targets Android TV/Google TV running Android 11 (API 30) or newer. The APK declares `minSdk = 30`; compile/target SDK remains 36. Framework/resource tests cover API 30 and 34.

## Evidence

The development receiver installed and ran on a physical MiTV-MOOQ1 using Android 11/API 30, and on an Android TV API 34 emulator. The physical flow exercised LAN serving, TV approval, HTTPS handoff to the installed browser, process-restart credential persistence, per-device revocation and Deny-first D-pad interaction. See the V1 verification record for revision and limits.

## Tradeoff

The original API 26 value was provisional. Initial support is narrowed to the oldest real TV platform exercised rather than presented as certification of Android 8–10 devices. Older-TV support can be evaluated through a separate compatibility change and device testing. This does not guarantee every API 30+ model or destination website: vendor behavior, handlers, DRM, cookies and remote input still matter.

The final API-30-minimum debug APK was reinstalled successfully on the physical TV on 2026-11-04; the receiver started and displayed a live pairing QR. This remains development evidence, not a signed release or certification.
