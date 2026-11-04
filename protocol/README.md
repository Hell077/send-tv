# API specification notes

`openapi.yaml` currently formalizes info/open/operation endpoints. Pairing endpoints are deliberately left as implementation-gated until challenge transport, TV approval, scoped polling auth, and threat model are proved in a spike; see `architecture/api.md` and `architecture/security.md`. Agent must extend OpenAPI *before* coding pairing API and contract tests.
