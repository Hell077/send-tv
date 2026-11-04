# Git, commits and branch policy

## Commit format — Conventional Commits
`<type>(<scope>)!: <imperative description>` where `!` only for breaking change.
Types: `feat`, `fix`, `docs`, `design`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`, `revert`. Scopes: `tv`, `web`, `cli`, `protocol`, `security`, `site`, `docs`, `design`, `repo`.

Examples:
- `feat(tv): add URL launch dispatcher`
- `feat(web): add pairing status polling`
- `fix(security): reject replayed pairing nonce`
- `docs(site): add pairing troubleshooting`
- `design(tv): refine ten-foot pairing screen`
- `test(protocol): cover invalid URL schemes`
- `ci(repo): validate commit messages`

Subject <= 72 characters, present tense, no trailing period. Body explains why, behavior, risks, testing; `BREAKING CHANGE:` footer where needed.

## Branches / PRs
`main` protected, no direct pushes. Feature branches `feat/<short-task>`, fixes `fix/<short-task>`, docs `docs/<short-task>`, design `design/<short-task>`. Squash merge preferred, PR title Conventional Commit compliant. 1 issue/feature per PR when practical. No unfinished functionality in release branches; feature flags for incomplete work.

## Required checks
Format/lint, unit + integration tests, OpenAPI validation, website build, markdown link check where feasible, security-sensitive tests. PR template includes behavior, tests, screenshots for UI, security changes, docs changes.

## Hooks
Local `pre-commit`: formatting/fast lint, forbidden secrets and accidental large binaries. `commit-msg`: commitlint Conventional Commits. `pre-push`: targeted tests; CI is source of truth, hooks are not security gates. Husky for JS workspaces only if needed; use simple repo-level `scripts/hooks/` + `core.hooksPath` for polyglot monorepo.

## Releases
SemVer `v0.x.y` while experimental, changelog generated from commits or curated, GitHub Release artifacts and checksums. Never claim signed releases until signing is configured. No automatic deployment of production app on every feature PR.
