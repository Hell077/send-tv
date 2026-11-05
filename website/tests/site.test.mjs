import { expect, it } from "vitest";
import { readFile, readdir, stat } from "node:fs/promises";
import { join, relative } from "node:path";

const root = "build/site";
async function htmlFiles(folder) {
    const files = [];
    for (const entry of await readdir(folder, { withFileTypes: true })) {
        const path = join(folder, entry.name);
        if (entry.isDirectory()) files.push(...(await htmlFiles(path)));
        else if (entry.name.endsWith(".html")) files.push(path);
    }
    return files;
}

it("builds real static guides with valid internal links and assets under the project base", async () => {
    const home = await readFile(join(root, "index.html"), "utf8");
    const canonical = home.match(/rel="canonical" href="([^"]+)"/);
    const siteUrl = new URL(canonical?.[1] ?? "http://localhost/");
    const base = siteUrl.pathname;
    const files = await htmlFiles(root);
    expect(files.length).toBeGreaterThanOrEqual(10);
    for (const file of files) {
        const html = await readFile(file, "utf8");
        expect(html).toContain(file.includes("/ru/") ? 'lang="ru"' : 'lang="en"');
        expect(html).toContain('<meta name="description"');
        expect(html).toContain('id="main"');
        const current = new URL(
            base + relative(root, file).replace(/index\.html$/, ""),
            siteUrl,
        );
        for (const match of html.matchAll(/(?:href|src)="([^"]+)"/g)) {
            const raw = match[1];
            if (/^(https?:|mailto:|data:)/.test(raw)) continue;
            const url = new URL(raw, current);
            expect(
                url.pathname.startsWith(base),
                `${file}: asset/link escaped configured base`,
            ).toBe(true);
            let target = join(
                root,
                decodeURIComponent(url.pathname.slice(base.length)),
            );
            if ((await stat(target)).isDirectory())
                target = join(target, "index.html");
            const content = await readFile(target, "utf8");
            if (url.hash)
                expect(
                    content,
                    `${file}: missing link target ${url.hash}`,
                ).toContain(`id="${decodeURIComponent(url.hash.slice(1))}"`);
        }
        expect(html).not.toMatch(/<script[^>]+src="https?:/);
        expect(html).not.toMatch(/href="[^"]*releases\/download/);
    }
    expect(await readFile(join(root, "protocol/openapi.yaml"), "utf8")).toBe(
        await readFile("protocol/openapi.yaml", "utf8"),
    );
    const robots = await readFile(join(root, "robots.txt"), "utf8");
    if (canonical)
        expect(robots).toContain(`${siteUrl.origin}${base}sitemap-index.xml`);
    else expect(robots).not.toContain("Sitemap:");
});
