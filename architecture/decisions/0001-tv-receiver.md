# ADR 0001 — Native Android TV receiver

[Русский](../../docs/ru/architecture/decisions/0001-tv-receiver.md)

Status: Accepted for MVP.

## Decision
Use native Kotlin Android TV app with embedded local HTTP server and static web sender, initially only `open URL`. TV installation is a prerequisite.

## Rationale
Arbitrary smart TVs cannot universally be forced to open websites using a standard discovery protocol. Android TV supports Android intents and own receiver UI. Ktor embedded or equivalent must be evaluated for binary size, lifecycle, threading and security.

## Consequences
Pros: explicit behavior, no external infrastructure, testable pairing and status. Cons: installation friction, Android system background limits, browser handler variability; other TV OS require new receivers.
