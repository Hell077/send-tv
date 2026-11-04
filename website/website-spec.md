# Public website / docs specification

## Host and architecture
GitHub Pages via GitHub Actions, static build only. Suggested stack: Astro + TypeScript + MDX (or another static generator with clear rationale). Public `/` landing + `/docs/` versioned guides. No dependency on running TV receiver; no server-side API, mandatory accounts or tracking.

## Information architecture
- `/` Product intro, 3-step how-to, design demo, feature matrix, privacy promise, GitHub CTA, docs CTA, download CTA only when actual release exists.
- `/docs/getting-started` install TV receiver, supported TVs, browser requirement, pair and send.
- `/docs/cli` available only after V2 ships; show version and truthful placeholders before.
- `/docs/troubleshooting` no LAN connection, QR invalid, browser missing, guest Wi-Fi isolation, auth errors.
- `/docs/architecture` flow, protocol, privacy/security limitations.
- `/docs/api` OpenAPI link, request/response examples redacted.
- `/docs/faq` does it work on Samsung/LG, need Internet, need installed app, does it mirror?
- `/privacy`, `/changelog`, `/download` release-backed only.

## Design / copy
Follow `design/website/design.md` and `website.pen`: minimalist dark navy + clear blue accents, high contrast, restrained gradients, technical-but-friendly; no fake ratings, device mockups presented as illustrations, no invented support logos. CTA: `Get started`, `Read documentation`, `View on GitHub`. Never claim universal TV support.

## Docs requirements
Left navigation on desktop, collapsible mobile TOC, searchable docs if feasible without external services, code copy buttons, clear platform badges, screenshots + alt text, deep links, last-updated / version label, note about mixed HTTP LAN risk.

## SEO and performance
Semantic headings, OpenGraph metadata, sitemap.xml, canonical URL from actual repository settings, robots.txt, lazy media, appropriate alt, responsive 360–1920px. Target Lighthouse >= 90 performance/accessibility on supported CI measurement, no guarantee until tested.

## Publication
GitHub Actions: install locked deps -> typecheck -> tests -> build -> Pages artifact -> deploy from main only. Use `base` for `<repo>` project Pages vs root custom domain; never hardcode owner or URL. Separate staging PR preview strategy if needed; ensure SPA redirects don't swallow docs URLs. Document Pages configuration.
