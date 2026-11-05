import { defineConfig } from "astro/config";
import sitemap from "@astrojs/sitemap";
import { pagesConfig } from "../scripts/pages-config.mjs";

const config = pagesConfig();
export default defineConfig({
    site: config.site,
    base: config.base,
    output: "static",
    trailingSlash: "always",
    outDir: "../build/site",
    integrations: config.site ? [sitemap()] : [],
    build: { inlineStylesheets: "never" },
    vite: { define: { __GITHUB_URL__: JSON.stringify(config.github ?? "") } },
});
