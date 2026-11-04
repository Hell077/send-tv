# SendTV — implementation blueprint

An offline-first, local-network protocol and product for sending URLs from phones, tablets, browsers, and computers to a television. **V1 implementation has started; URL handoff is not available yet.**

## Development status

The first increment adds a native Kotlin Android TV development shell, local receiver-name settings, JVM tests, and build/CI configuration. The shell explicitly reports that its network listener is disabled. It has no network permission, pairing, QR, or URL dispatch yet. V1 acceptance criteria remain unchecked; no V2/V3 work has started.

Build instructions, verification evidence, and remaining blockers: [V1 development](docs/development-v1.md). The UI is provisional until the `.pen` files can be opened and validated in Pencil. Do not publish this shell as a usable SendTV receiver.

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
