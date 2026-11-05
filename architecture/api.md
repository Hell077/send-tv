# Protocol API contract

[Русский](../docs/ru/architecture/api.md)

V1 HTTP JSON; base path `/v1`. The `protocol/openapi.yaml` file is canonical for request/response shape.

## Resources
- `GET /v1/info`: receiver identity, protocol version, capabilities (no secrets).
- Pairing challenge generation is TV-local only; no HTTP challenge endpoint is exposed. The QR carries the short-lived challenge in a fragment.
- `POST /v1/pair`: challenge and proposed device name; TV confirmation required; returns pending approval or access token only after approval.
- `GET /v1/pair/{requestId}`: pairing status polling with a separate scoped 256-bit bearer polling credential and expiration; request ID alone never authorizes delivery.
- `DELETE /v1/pair/{requestId}`: cancel a pending request using its polling credential.
- `POST /v1/open`: `Authorization: Bearer <token>` and `{url, requestId}`; respond with `{operationId,status:accepted}` if queued.
- `GET /v1/operations/{id}`: accepted/launching/launched/failed; avoid implying webpage loaded.
- Revocation and device enumeration are TV-local methods, not HTTP endpoints.

## Error shape
`{code: "INVALID_URL", message: "Only http(s) links are supported", requestId: "..."}`. Never put secrets in messages.

## Semantics
- Status `launched` means Android accepted ACTION_VIEW, not remote page fully rendered.
- Receiver limits URL size and rejects scheme bypass, embedded credentials and oversized JSON.
- Pairing status endpoint must not reveal token to any caller with only a guessable ID.
- `open` should be protected from rapid duplicates; treat `requestId` as idempotency key with bounded TTL.

## Versioning
Additive changes to v1 only; breaking change -> `/v2`. Client checks capability list, never infers capability from platform string alone.
