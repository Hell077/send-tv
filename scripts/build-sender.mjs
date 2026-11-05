import { build } from "esbuild";
import { mkdir, copyFile } from "node:fs/promises";

const destination = "build/sender/sender";
await mkdir(destination, { recursive: true });
await build({
    entryPoints: ["sender/src/app.ts"],
    bundle: true,
    outfile: `${destination}/app.js`,
    target: ["es2020"],
    format: "iife",
});
await copyFile("sender/index.html", `${destination}/index.html`);
await copyFile("sender/app.css", `${destination}/app.css`);
