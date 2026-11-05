# V3 — Files and remote control (post-initial MVP)

[Русский](../../docs/ru/roadmap/mvp/version-3.md)

## Features
- Image and PDF transfer with explicit user acceptance, safe MIME detection, max file size and bounded on-TV storage.
- Receiver gallery with delete/expiry controls, secure temporary URLs and rejection of ZIP bombs/malformed payloads.
- Media files: negotiate codec/container capabilities, direct play when supported, no automatic cloud upload.
- Browser control for receiver-owned WebView only: back, forward, reload, scroll; no arbitrary input into external apps.
- WebSocket event channel with authenticated origin and replay protection.

## Acceptance
Images and small PDFs render on compatible TV without third-party cloud; no path traversal; storage quota enforced; transfer abort frees resources; remote actions require authenticated trust; docs reflect codec limitations.
