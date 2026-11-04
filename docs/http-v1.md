# V1 receiver HTTP boundary

The receiver binds one explicitly selected private IPv4 address and configurable unprivileged port. Wildcard/public addresses are rejected; loopback is allowed only for local tests. The Android adapter must select only Wi-Fi/Ethernet link addresses and stop/rebind on network loss or address changes. No discovery, UPnP, arbitrary URL fetches, or proxy forwarding exists.

The small embedded server implements a constrained HTTP/1.1 subset instead of a general application server: 4 workers, at most 8 queued connections, 8 KiB headers, 16 KiB request bodies, read timeouts/deadlines, fixed Content-Length, and one request per connection. It rejects duplicate headers, chunked bodies, absolute request targets, path traversal, malformed UTF-8, unsupported methods, and incomplete bodies before routing. Static assets are an exact allowlist. This limits dependencies and auditable behavior; it is not a claim of general-purpose HTTP compatibility or external security certification.

Host must exactly match the selected address and port, defending against DNS rebinding. Origin, when present, must be the exact receiver HTTP origin; every state-changing request requires it. Cross-site fetch metadata is denied. JSON requests use strict syntax, reject duplicate/unknown fields, and require the declared string fields; there are no cookies or wildcard CORS headers. Responses disable caching, framing, referrer leakage and MIME sniffing, with a self-only content security policy.

Open requests require a trusted bearer token. Operations are private to that device, bounded to 128 entries and five minutes, and use a device-scoped idempotency key. Reusing the key with a different URL fails. The token is rechecked immediately before queued dispatch so revocation prevents a queued launch. Operation state reports Android handoff, never a webpage loading guarantee. Operation records keep a URL digest rather than the full destination. No URL/token/request body or exception text is logged.

The implemented contract is [OpenAPI](../protocol/openapi.yaml). Real-socket tests exercise framing, limits, shutdown and security headers; API integration tests exercise approval/polling, strict JSON, unauthorized access, origin/host rejection, idempotency, operation isolation, revocation and missing-browser state. These local tests do not establish mobile-browser or physical LAN compatibility.

HTTP still exposes the entire control channel to hostile LAN observers. Use a trusted LAN and explicitly approve new devices on the TV. Authentication is not transport encryption.
