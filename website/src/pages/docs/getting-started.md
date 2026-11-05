---
layout: "../../layouts/Docs.astro"
title: Getting started
description: Build and test the unreleased SendTV V1 receiver on an Android TV development device.
toc:
    - { id: before-you-start, label: Before you start }
    - { id: install, label: Install }
    - { id: pair, label: Pair }
    - { id: send, label: Send }
---

## Before you start

**V1 is unreleased and still under device verification.** No verified public APK download is available. The current build is for development testing, not a finished consumer release.

You need an Android / Google TV development device, a compatible installed browser or HTTP(S) URL handler, and a phone or computer on the same trusted Wi-Fi/Ethernet LAN. Samsung Tizen and LG webOS are not supported. Client isolation and VLAN firewalls can prevent pairing.

The initial supported floor is **Android 11/API 30**, selected after physical MiTV-MOOQ1 testing. API 34 is exercised in an Android TV emulator. Older TVs and other models are outside the initial tested compatibility scope. The API-30-minimum development candidate was reinstalled successfully on that physical TV; this does not certify every API 30+ model.

## Install

For developers: use JDK 21, Node 24, and Android SDK platform 36/build-tools 36.0.0. Clone the repository, install locked development dependencies, and build:

```sh
npm ci --ignore-scripts --no-audit --no-fund
./gradlew :tv:assembleDebug
```

The APK is at `tv/build/outputs/apk/debug/tv-debug.apk`. It uses a local development signing key. Install it through your developer-authorized Android tooling:

```sh
adb install -r tv/build/outputs/apk/debug/tv-debug.apk
adb shell am start -n org.sendtv.receiver/.ReceiverActivity
```

ADB is used here only to install and inspect a development build. The product's pairing and send flow does not use ADB, shell commands, or elevated Android permissions.

<figure>
<img src="../../screenshots/tv-settings-api30.png" width="1920" height="1080" loading="lazy" alt="SendTV settings on a physical Android 11 TV, with receiver name, port 8765, plaintext HTTP warning and focused Back to Home button" />
<figcaption>Actual development build on MiTV-MOOQ1, 4 November 2026. Settings are controlled locally on the TV.</figcaption>
</figure>

## Pair

1. Open SendTV on the TV. Choose **Start receiver** and read the plaintext-HTTP warning. Keep SendTV visible.
2. Scan the current QR with your phone's camera. The sender loads from the TV, not from this public website. The QR expires after 60 seconds and can be submitted once.
3. Enter a device name. Optionally choose **Remember this browser** only on a trusted device/network. Otherwise the credential stays within the browser tab session.
4. Choose **Request TV approval**. Compare the verification code on both screens, then explicitly choose **Allow** on the TV. The default focus is Deny.

TV Settings offers system language, English and Russian. The sender initially follows the browser’s language and has an English/Russian selector. Changing language does not approve a pairing request or translate device names and entered links.

HTTP does not encrypt challenges, bearer tokens, or links. A malicious LAN observer may steal them. Use trusted networks and avoid sensitive links on shared/untrusted Wi-Fi.

## Send

Paste an HTTP(S) address into **Website address**, then choose **Open on TV**. Embedded credentials, unsafe schemes, malformed addresses and URLs longer than 4096 characters are rejected.

“Android accepted the handoff” means the OS accepted `ACTION_VIEW`; it does not confirm that the destination page loaded. Login, Widevine, cookies, compatible handlers and remote input can affect the result.

Android restricts background app launches. Return to SendTV on the TV before sending another link if the sender asks you to do so. The listener is tied to the app process: Android may stop it while backgrounded. Reopen SendTV and start the receiver after a process restart; trusted-device hashes persist, but pending pairing requests do not.

To remove access, open **Devices** on the TV and revoke that device or all devices. **Forget this browser** only removes its local copy; it cannot revoke a credential copied elsewhere.

For errors, see [troubleshooting](../troubleshooting/). For security tradeoffs, see [architecture](../architecture/).
