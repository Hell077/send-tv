# GitHub Pages publication checklist

1. Determine GitHub `<owner>/<repo>` and Pages URL; configure Astro `site` and `base` accordingly.
2. Create docs content and 404; run internal-link validator.
3. CI: install, lint, typecheck, test, static build; deploy only from protected main.
4. GitHub repo Settings -> Pages -> GitHub Actions as source; least-privilege deployment permissions.
5. Verify `/`, `/docs/getting-started`, `/docs/troubleshooting`, asset paths under project base, mobile layout and canonical URLs.
6. Link to actual releases only, and show download as unavailable until a real signed/verified build exists.
7. Include `README` badges only if verified pipeline actually exists. Keep changelog updated at each release.
