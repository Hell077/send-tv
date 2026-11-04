---
layout: "../../layouts/Docs.astro"
title: Architecture & privacy
description: Understand SendTV's local receiver, authorization boundary, URL handoff and plaintext-LAN limitations.
toc:
    - { id: the-path-of-a-link, label: Link flow }
    - { id: authorization, label: Authorization }
    - { id: known-limitations, label: Known limitations }
---

## The path of a link

Phone camera → receiver-hosted sender → local HTTP JSON → authorization → Android `ACTION_VIEW` → installed TV browser or handler.

The Kotlin TV app serves a bundled TypeScript web sender and the versioned protocol. It does not fetch destination URLs, act as a proxy, execute shell commands, or expose arbitrary localhost/LAN resources. There is no cloud relay, account, mandatory hosted service, tracking, or remote telemetry in the app.

This public documentation website is a static GitHub Pages build. It is not a live sender and never tries to connect to your TV. Serving the sender from the receiver avoids relying on a public HTTPS page fetching a private HTTP service, but it does **not** encrypt the LAN traffic.

## Authorization

The TV generates a random 256-bit, short-lived, single-use challenge. The QR places it in a URL fragment, which the sender removes from the address bar before sending protocol requests. A separate random polling credential protects the pending response; knowing the request ID does not deliver a token.

Only a local TV-side decision grants a long-term scoped token. The receiver stores SHA-256 token hashes and explicit backup exclusions. Revocation disables subsequent commands, including a command queued before revocation but not yet dispatched. The receiver never exposes approval or revocation as public endpoints.

HTTP uses exact Host and Origin checks, strict JSON, bounded requests/connections, rate limits and device-scoped idempotency. These defenses do not replace authenticated transport encryption.

## Known limitations

- **Local HTTP is visible to malicious LAN observers.** An attacker may inspect links or steal credentials. Do not use sensitive links over untrusted Wi-Fi.
- Android TV installation is required; unmodified arbitrary TVs cannot be controlled this way.
- Android background launch and process-lifecycle restrictions still apply. Keep or return the receiver to the foreground for commands.
- A successful handoff does not prove the page loaded. Browser, login, cookies, DRM and remote input can affect compatibility.
- IPv4 LAN reachability is required for the current implementation; no cross-VLAN, IPv6-only, discovery, casting or mirroring guarantee is made.

Authenticated local TLS with a device trust model is future security work. V1 must not be marketed as an encrypted control channel.
