# Публикация GitHub Pages

[English](../../../website/publishing.md)

1. Определите `<owner>/<repo>` и Pages URL; настройте Astro `site` и `base`.
2. Создайте материалы и 404, проверьте внутренние ссылки.
3. CI: установка, lint, typecheck, тесты, статическая сборка; deploy только из защищённой main.
4. Settings → Pages → GitHub Actions; минимальные разрешения deploy.
5. Проверьте `/`, `/docs/getting-started`, `/docs/troubleshooting`, пути assets с project base, мобильный UI и canonical. Повторите для `/ru/`.
6. Ссылайтесь только на настоящие релизы. Загрузка недоступна до подписанной/проверенной сборки.
7. README badges — только для реально проверенного pipeline; обновляйте changelog при релизе.

## Реализованное развёртывание
Astro собирается в `build/site`. `GITHUB_REPOSITORY=Hell077/send-tv npm run build:site` проверяет project base `/send-tv/`; HTTPS `SITE_URL` может задать custom domain. Без конфигурации локальная сборка не заявляет canonical/sitemap. Тесты проверяют ссылки, фрагменты, assets и соответствие OpenAPI.

`.github/workflows/pages.yml` собирает и публикует только `main` через environment GitHub Pages с ограниченными правами. Feature-ветки только проверяются. В репозитории нужно включить GitHub Actions как источник Pages. Публичный deploy и проверка живого URL ещё не завершены; публичная доступность не заявляется.
