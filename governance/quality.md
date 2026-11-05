# Engineering quality gates

[Русский](../docs/ru/governance/quality.md)

- Kotlin: ktlint or detekt; JVM unit tests for URL validator, pairing FSM, authorization; Android instrumentation test for Intent dispatch where available.
- Go: `gofmt`, `go vet`, `go test -race ./...` on supported CI runners, discovery/parser contract tests.
- TypeScript: strict TS, ESLint, Prettier, Vitest, Playwright for sender and website critical paths.
- Every bug fix carries regression test; flaky tests are tracked, not silently ignored.
- Minimum goal: all security and protocol branches covered by targeted tests, not arbitrary 100% overall coverage claim.
- No real credentials in fixtures. Check logs for secrets and auth token redaction.
- Run Accessibility checks: keyboard, visible focus, color contrast, screen reader labels; TV D-pad traversal.
- Build artifacts reproducibly where practical; lock dependency versions; dependency review in CI.
- Merge requires passing CI, updated docs, verified acceptance criteria of active version.
