# Mandatory instructions for AI coding agents

1. Read `README.md` and all referenced V1 specifications before producing code.
2. Do not implement V2 or V3 work until V1 acceptance tests pass; avoid speculative abstractions.
3. Make short, reviewable, feature-scoped commits; apply `governance/git.md` and `governance/quality.md`.
4. Do not change or erase the documented security model to make development easier.
5. Never silently claim that local HTTP is secure from malicious LAN observers. Treat plaintext LAN traffic as a known limitation; document deployment tradeoffs and require pairing with TV confirmation.
6. Do not expose arbitrary localhost or LAN resources through receiver endpoints; no arbitrary URL fetching by receiver server, no open proxy.
7. Maintain `protocol/openapi.yaml` and corresponding tests whenever API shapes change.
8. Ensure each new behavior has unit or integration tests, including malformed inputs and unauthorized access.
9. Prefer native Kotlin Android SDK APIs for TV; Go for CLI; TypeScript for web; do not add Go runtime to Android TV.
10. Do not assert full web browsing compatibility: some pages depend on Widevine, cookies, browsers, remote input, etc.
11. Designs (`.pen`) are editable references; agent must validate them in Pencil before treating visual results as pixel-perfect.
12. Add or update docs in the same PR as behavioral changes. No secrets or tokens in Git or logs.
13. Keep privacy-first architecture. No remote telemetry or mandatory hosted services.
14. When stuck, state exact blocker and propose the smallest verifiable workaround; do not fake implementation, tests, or screenshots.
