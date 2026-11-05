import { spawnSync } from "node:child_process";
import { resolve } from "node:path";
const args = process.argv.slice(2);
const root = args.indexOf("--root");
if (root >= 0 && args[root + 1]) args[root + 1] = resolve(args[root + 1]);
const result = spawnSync(
    process.execPath,
    ["node_modules/astro/bin/astro.mjs", ...args],
    {
        stdio: "inherit",
        env: { ...process.env, ASTRO_TELEMETRY_DISABLED: "1" },
    },
);
process.exit(result.status ?? 1);
