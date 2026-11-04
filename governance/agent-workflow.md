# Agent delivery protocol

For every task:
1. Identify current roadmap version and exact acceptance criterion.
2. Read architecture constraints and visual spec for affected features.
3. Define smallest testable end-to-end slice; write test cases first for critical behavior.
4. Implement, format, test, document, and inspect UI at mobile/desktop/TV sizes.
5. Commit logically (never one mega-commit for entire phase).
6. Report completed criteria, commands actually run, test outcomes, limitations, and commit SHAs.

Never skip failing tests, fabricate screenshot approvals, remove constraints to pass CI, make unrequested network calls or add telemetry.

## Task completion checklist
- [ ] Requirements satisfied
- [ ] Security boundary maintained
- [ ] Unit + integration tests present
- [ ] User-facing errors handled
- [ ] Docs / OpenAPI updated
- [ ] Design checked against `.pen` and spec
- [ ] Commit conforms to `git.md`
