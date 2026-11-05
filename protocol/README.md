# API specification notes

[Русский](../docs/ru/protocol/README.md)

`openapi.yaml` formalizes info, pairing request/poll/cancel, open and operation endpoints. Pairing core negative tests establish challenge expiry/single use, local TV approval, separately authenticated polling and hashed token persistence before transport integration. The QR carries its nonce in a URL fragment, removed by the sender before making requests; it is never a permanent credential.

Challenges, approval decisions, device enumeration and revocation are TV-local methods, not remotely accessible HTTP endpoints. Poll credentials expire with their request after 60 seconds. The sender uses same-origin JSON requests, never cookies or wildcard CORS. See [pairing security](../docs/pairing-v1.md) and [API architecture](../architecture/api.md).
