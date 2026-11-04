import { defineConfig, devices } from "@playwright/test";
import { readFileSync } from "node:fs";
const html = readFileSync("build/site/index.html", "utf8");
const canonical = html.match(/rel="canonical" href="([^"]+)"/);
const base = canonical ? new URL(canonical[1]).pathname : "/";

export default defineConfig({
    testDir: "./website/browser-tests",
    use: {
        baseURL: `http://127.0.0.1:18767${base}`,
        trace: "off",
        screenshot: "off",
    },
    projects: [
        { name: "site-chromium", use: devices["Desktop Chrome"] },
        { name: "site-webkit", use: devices["iPhone 13"] },
    ],
    webServer: {
        command: "node scripts/preview-site.mjs",
        url: `http://127.0.0.1:18767${base}`,
        reuseExistingServer: false,
    },
});
