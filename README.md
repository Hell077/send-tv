# SendTV — implementation blueprint

An offline-first, local-network protocol and product for sending URLs from phones, tablets, browsers, and computers to a television. **V1 is implemented for development verification; it is not a verified public release.**

## Development status

The native Kotlin receiver now includes explicit start/stop, LAN binding, expiring QR challenges, TV approval, token-hash persistence and revocation, authenticated HTTP URL handoff and operation status, plus a bundled TypeScript sender. Automated security, socket, Android resource/intent and Chromium/WebKit sender tests are present. Controlled physical TV/browser flows have been verified; the remaining browser/network matrix and public site deployment remain open; no V2/V3 work has started.

Build instructions, verification evidence, and remaining work: [V1 development](docs/development-v1.md). The `.pen` references have been [inspected through Pencil MCP](design/validation/README.md); runtime captures and their limits are recorded in [V1 verification](docs/verification-v1.md). Do not publish the experimental build as a verified V1 release.

## Agent entry point
1. Read `AGENTS.md` and `governance/git.md`.
2. Read `product/requirements.md`, `architecture/system.md`, and `architecture/security.md`.
3. Implement `roadmap/mvp/version-1.md` end-to-end before touching V2.
4. Inspect editable Pencil documents in `design/` before writing UI. Match designs and accessibility requirements; refine via Pencil as necessary.
5. Follow `website/website-spec.md` for GitHub Pages site; deployment must not require a backend.

## Structure
- `roadmap/mvp/`: sequential versions, acceptance criteria, tests.
- `roadmap/expansion/`: post-MVP file transfer and future casting; not MVP promises.
- `architecture/`: API, network/security, technical decisions, ADRs.
- `design/`: native editable `.pen` frames + matching design specs.
- `website/`: landing, documentation, publishing, SEO, content.
- `governance/`: Conventional Commits, PR, hooks, tests, release process.

## Key constraints
- Android TV receiver is required for V1. **Do not claim arbitrary unmodified TVs can open arbitrary URLs.**
- Users send URLs through a receiver-hosted web interface reached by TV QR code. Native discovery comes later.
- No accounts, cloud, ADB, tracking or invasive access.
- Receiver can open an HTTP(S) URL only after pairing and permission check. Never execute remote shell commands.
- Names, app/package identifiers, domains, and GitHub owner are placeholders until finalized.

## Implementation sequence
V1: URL -> TV; V2: discovery, multi-device, CLI; V3: viewing files + remote control; V4+: alternative TV OS + optional casting.

Documentation language: English for repository artifacts; UI copy: English. This specification was prepared on 2026-11-04.
