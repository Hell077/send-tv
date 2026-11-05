# GitHub Pages publication checklist

[Русский](../docs/ru/website/publishing.md)

1. Determine GitHub `<owner>/<repo>` and Pages URL; configure Astro `site` and `base` accordingly.
2. Create docs content and 404; run internal-link validator.
3. CI: install, lint, typecheck, test, static build; deploy only from protected main.
4. GitHub repo Settings -> Pages -> GitHub Actions as source; least-privilege deployment permissions.
5. Verify `/`, `/docs/getting-started`, `/docs/troubleshooting`, asset paths under project base, mobile layout and canonical URLs.
6. Link to actual releases only, and show download as unavailable until a real signed/verified build exists.
7. Include `README` badges only if verified pipeline actually exists. Keep changelog updated at each release.

## Implemented deployment

The Astro site builds to `build/site`. `GITHUB_REPOSITORY=Hell077/send-tv npm run build:site` verifies the `/send-tv/` project base locally; `SITE_URL` can override it with an HTTPS custom-domain URL. Without deployment configuration the local build omits canonical/sitemap claims. Tests cover internal links, fragments, assets and OpenAPI parity.

`.github/workflows/pages.yml` builds and deploys only `main`, using the GitHub Pages environment and scoped deployment permissions. Feature branches run verification without publishing. The repository must enable Pages with GitHub Actions as its source. Public deployment and live URL verification are still pending; no public availability is claimed.
