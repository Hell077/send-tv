import { spawnSync } from "node:child_process";

// Fresh receiver state per browser preserves the production pairing rate limits.
for (const browser of ["chromium", "webkit"]) {
    const result = spawnSync(
        process.execPath,
        [
            "node_modules/@playwright/test/cli.js",
            "test",
            `--project=${browser}`,
            "--reporter=line",
        ],
        { stdio: "inherit", env: process.env },
    );
    if (result.status !== 0) process.exit(result.status ?? 1);
}
