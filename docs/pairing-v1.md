# V1 pairing security state machine

The Kotlin pairing core uses a TV-issued 256-bit random challenge, valid for 60 seconds and consumed when a request is submitted. Approval is a local method available only to the TV owner UI; no approval HTTP endpoint exists. Denying, cancelling, timing out, or restarting a session cannot approve a request. Pair requests have a separate 256-bit polling credential; knowing a request ID alone grants no access.

Approved bearer tokens are random 256-bit values. Only SHA-256 hashes are passed to persistent device storage. The plaintext token is retained in the scoped polling response in memory until the original 60-second request expiry, permitting delivery retries; restarting a session erases that delivery state. Revocation disables authorization and erases any pending delivery of that device's token immediately. Persistence failures do not grant new authorization.

State is bounded to 8 pairing requests and 32 trusted devices. Pair submissions are limited to 5 per source per minute and 20 globally per minute, with bounded source buckets. Replayed and expired challenges fail. These limits reduce abuse but cannot make a hostile LAN safe. Plain HTTP exposes challenges, polling credentials, tokens and destination URLs to observers; pairing always requires TV confirmation.

The URL validator accepts only syntactically valid HTTP(S) URIs with a host, no credentials or whitespace/control characters, valid ports, and a 4096-character cap. Validation never fetches the destination. Android dispatch and transport integration are separate subsequent increments.
