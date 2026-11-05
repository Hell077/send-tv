import { describe, expect, it } from "vitest";
import { pagesConfig } from "../../scripts/pages-config.mjs";

describe("GitHub Pages URL configuration", () => {
    it("preserves project bases and handles owner sites and custom domains", () => {
        expect(pagesConfig({ GITHUB_REPOSITORY: "example/send-tv" })).toEqual({
            site: "https://example.github.io",
            base: "/send-tv/",
            github: "https://github.com/example/send-tv",
        });
        expect(
            pagesConfig({ GITHUB_REPOSITORY: "example/example.github.io" })
                .base,
        ).toBe("/");
        expect(
            pagesConfig({
                GITHUB_REPOSITORY: "example/send-tv",
                SITE_URL: "https://tv.example.test/docs-root/",
            }),
        ).toEqual({
            site: "https://tv.example.test",
            base: "/docs-root/",
            github: "https://github.com/example/send-tv",
        });
    });
    it("never invents an owner and rejects insecure or credential-bearing URLs", () => {
        expect(pagesConfig({})).toEqual({
            site: undefined,
            base: "/",
            github: undefined,
        });
        for (const SITE_URL of [
            "http://example.test",
            "https://user:secret@example.test",
            "https://example.test/?x=1",
            "https://example.test/#fragment",
        ]) {
            expect(() => pagesConfig({ SITE_URL })).toThrow();
        }
    });
});
