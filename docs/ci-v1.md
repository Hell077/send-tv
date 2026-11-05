# V1 CI verification

[Русский](ru/docs/ci-v1.md)

The first hosted verification run failed because Android's setup action requested the retired `tools` SDK package. The workflow requests `platform-tools` explicitly instead.

The next run rejected an npm lockfile containing temporary-directory package keys. A lockfile generated with a symlinked temporary prefix was not portable to the Linux runner. The keys were corrected to repository-relative `node_modules/` paths, and a clean `npm ci --ignore-scripts --no-audit --no-fund` installation with the exact committed manifest/lock was verified in a separate canonical temporary directory.

`node scripts/check-lockfile.mjs` now fails early if package paths escape `node_modules/`, declared packages are absent, or root dependencies differ from `package.json`. Its regression tests run with `node --test scripts/check-lockfile.test.mjs` before dependency installation in both CI jobs. These checks supplement npm's own validation; they do not certify dependency security.

Hosted CI outcomes must still be inspected for each pushed revision. Local success is not reported as a passing hosted run.
