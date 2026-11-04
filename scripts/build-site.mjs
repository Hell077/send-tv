import { mkdir, copyFile } from "node:fs/promises";
import { spawnSync } from "node:child_process";
import { resolve } from "node:path";

await mkdir("website/public/protocol", { recursive: true });
await copyFile("protocol/openapi.yaml", "website/public/protocol/openapi.yaml");
const result = spawnSync(
    process.execPath,
    ["node_modules/astro/bin/astro.mjs", "build", "--root", resolve("website")],
    {
        stdio: "inherit",
        env: { ...process.env, ASTRO_TELEMETRY_DISABLED: "1" },
    },
);
process.exit(result.status ?? 1);
