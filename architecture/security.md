# Threat model and pairing

[Русский](../docs/ru/architecture/security.md)

## Threats
Malicious device on shared Wi-Fi; replayed QR nonce; stolen bearer token; drive-by browser requests; fake receiver; accidental URL with unsafe scheme; LAN packet inspection; guest network isolation; denial-of-service and abuse.

## V1 controls
1. TV displays unpredictable, short-lived, single-use pairing challenge; regenerate after use / expiry.
2. First-time pairing is explicitly confirmed on TV: identify device name and one-time verification phrase where possible.
3. Pair endpoint is heavily rate-limited; returned long-term scoped token is never embedded in QR code.
4. Token required for `open`; token hashes at rest, revoke per device, rotate if exposed.
5. Require strict `Origin` policy for browser requests, `SameSite` cookies if cookies are used, CSRF mitigation and appropriate CORS; do not expose wildcard CORS.
6. Accept `http://` and `https://` only; length cap e.g. 4096 chars; reject embedded credentials; cap payload sizes. Never accept `javascript:`, `file:`, `intent:`, `content:`, or shell commands.
7. HTTPS-only destination URLs preferred, but HTTP supported with explicit tradeoff.
8. Bound requests, connections and timeouts; do not log URLs containing query secrets by default.
9. Bind to appropriate LAN interfaces, do not UPnP-port-forward, never make public server.
10. Show meaningful state to owner and support immediate revoke-all.

## Security limitations
Local HTTP is visible on hostile networks; do not market encrypted/secure transport before implementing authenticated TLS. Consider TV-issued self-signed certificate plus fingerprint pinning in native clients; browsers require trusted cert strategies. Do not claim a secure web origin on plain HTTP LAN addresses.

## Verification
- Unknown token: 401; revoked token: 401; wrong origin/CSRF: denied; expired challenge: denied.
- Unauthorized open never launches an Android Intent.
- QR cannot be used a second time after successful pairing.
- Fuzz/malformed JSON and 10x long URL fail safely.
- Device discovery never equals authentication.
