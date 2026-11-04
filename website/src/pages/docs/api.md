---
layout: "../../layouts/Docs.astro"
title: Receiver API
description: SendTV V1 receiver protocol, TV approval boundaries and redacted handoff examples.
---

The [canonical OpenAPI document](../../protocol/openapi.yaml) defines request and response shapes. V1 is under verification. Base path: `/v1`; default receiver port: `8765`, configurable on the TV.

| Method / path                 | Authorization                          | Meaning                                     |
| ----------------------------- | -------------------------------------- | ------------------------------------------- |
| `GET /v1/info`                | Public                                 | Identity and capabilities; no secrets       |
| `POST /v1/pair`               | Fresh QR challenge + exact Origin      | Request TV approval                         |
| `GET /v1/pair/{requestId}`    | Separate polling bearer                | Pending, denied, or approved token delivery |
| `DELETE /v1/pair/{requestId}` | Separate polling bearer + exact Origin | Cancel pending approval                     |
| `POST /v1/open`               | Trusted bearer + exact Origin          | Queue a URL handoff                         |
| `GET /v1/operations/{id}`     | Same trusted device                    | Private handoff status                      |

Challenge generation, approval, trusted-device enumeration and revocation are TV-local only. No HTTP proxy or arbitrary asset path exists.

Example request body, with an idempotency key scoped to the trusted device:

```json
{ "url": "https://example.com/", "requestId": "redacted-example-0001" }
```

Headers include `Content-Type: application/json`, `Authorization: Bearer <redacted>` and the exact receiver `Origin`, for example `http://192.168.1.42:8765`. The Host must match that receiver address and port. The sender supplies these automatically; a random website cannot substitute its own Origin.

```json
{ "operationId": "opaque-operation-id", "status": "accepted" }
```

Poll the returned operation. Status is `accepted`, `launching`, `launched` or `failed`; `launched` means Android accepted the handoff, not that the page loaded. Reusing an idempotency key with a different URL fails with 409.

Failure envelopes contain `code` and `message` without a destination URL or credential. Examples include `INVALID_URL`, `UNAUTHORIZED`, `INVALID_ORIGIN`, `INVALID_CHALLENGE`, `RATE_LIMITED`, `NO_BROWSER`, and `RETURN_TO_RECEIVER`.

The server accepts a constrained HTTP/1.1 subset: one request per connection, Content-Length for POST, no chunking or duplicate headers, 8 KiB headers and 16 KiB bodies. All responses are no-store. See [architecture](../architecture/) for plaintext-LAN limitations.
