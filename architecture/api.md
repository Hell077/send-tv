# Protocol API contract

V1 HTTP JSON; base path `/v1`. The `protocol/openapi.yaml` file is canonical for request/response shape.

## Resources
- `GET /v1/info`: receiver identity, protocol version, capabilities (no secrets).
- `GET /v1/pair/challenge`: only from TV-local UI or controlled pairing flow; TV generates short-lived challenge.
- `POST /v1/pair`: challenge and proposed device name; TV confirmation required; returns pending approval or access token only after approval.
- `GET /v1/pair/{requestId}`: pairing status polling with scoped unguessable request ID and expiration.
- `POST /v1/open`: `Authorization: Bearer <token>` and `{url, requestId}`; respond with `{operationId,status:accepted}` if queued.
- `GET /v1/operations/{id}`: accepted/launching/launched/failed; avoid implying webpage loaded.
- `DELETE /v1/devices/{id}`: owner-authorized revoke via TV interface; do not expose anonymously.

## Error shape
`{code: "INVALID_URL", message: "Only http(s) links are supported", requestId: "..."}`. Never put secrets in messages.

## Semantics
- Status `launched` means Android accepted ACTION_VIEW, not remote page fully rendered.
- Receiver limits URL size and rejects scheme bypass, embedded credentials and oversized JSON.
- Pairing status endpoint must not reveal token to any caller with only a guessable ID.
- `open` should be protected from rapid duplicates; treat `requestId` as idempotency key with bounded TTL.

## Versioning
Additive changes to v1 only; breaking change -> `/v2`. Client checks capability list, never infers capability from platform string alone.
