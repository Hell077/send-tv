---
layout: "../layouts/Docs.astro"
title: Development privacy statement
description: What the SendTV development implementation stores and how its local HTTP security limits affect privacy.
---

The app has no remote telemetry, ads, mandatory account, or cloud relay. Links are sent directly to the receiver, which asks Android to open them in an installed handler. Destination websites and their handlers have their own network behavior and policies.

The receiver stores a local display name, stable random device identity and trusted-device token hashes. Operation records are held in memory for up to five minutes, with a digest rather than the full URL. Pending token delivery expires with the pairing request. App backup/transfer exclusions are configured.

Language preference is stored separately from authorization and does not grant access.

The browser keeps credentials for its tab session by default. Choosing “Remember this browser” stores the bearer token in browser local storage for that receiver. This is not hardware-backed or encrypted storage. Forgetting the browser's token does not revoke other copies; revoke access on the TV.

**The LAN HTTP channel is not encrypted.** Malicious network observers can inspect links and credentials. Use trusted networks and avoid sensitive URLs on untrusted Wi-Fi. No universal browser or TV compatibility is promised.

This static public site does not connect to TVs and contains no application analytics or third-party fonts/scripts. GitHub Pages hosting and destination services have their own policies. This is a statement about an unreleased development implementation, not a claim of a completed security audit.
