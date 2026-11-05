# Git, коммиты и ветки

[English](../../../governance/git.md)

## Conventional Commits
`<type>(<scope>)!: <imperative description>`, где `!` — только для несовместимого изменения.
Типы: `feat`, `fix`, `docs`, `design`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`, `revert`. Области: `tv`, `web`, `cli`, `protocol`, `security`, `site`, `docs`, `design`, `repo`.

Примеры:
- `feat(tv): add URL launch dispatcher`
- `feat(web): add pairing status polling`
- `fix(security): reject replayed pairing nonce`
- `docs(site): add pairing troubleshooting`
- `design(tv): refine ten-foot pairing screen`
- `test(protocol): cover invalid URL schemes`
- `ci(repo): validate commit messages`

Тема не длиннее 72 символов, повелительная форма, без точки. Тело объясняет причину, поведение, риски и тесты; при необходимости footer `BREAKING CHANGE:`.

## Ветки / PR
`main` защищена, прямые push запрещены. Ветки функций `feat/<short-task>`, исправлений `fix/<short-task>`, документации `docs/<short-task>`, дизайна `design/<short-task>`. Предпочтителен squash merge, заголовок PR по Conventional Commits. По возможности одна задача/функция на PR. В релизных ветках нет незавершённых функций; для них используйте feature flags.

## Обязательные проверки
Форматирование/lint, модульные и интеграционные тесты, OpenAPI, сборка сайта, проверка Markdown-ссылок где возможно, тесты безопасности. Шаблон PR включает поведение, тесты, UI-скриншоты, безопасность и документацию.

## Hooks
Локальный `pre-commit`: форматирование/быстрый lint, запрет секретов и случайных больших бинарников. `commit-msg`: Conventional Commits. `pre-push`: целевые тесты; CI — источник истины, hooks не являются границей безопасности. Husky — только при необходимости JS workspace; для полиглотного монорепозитория используйте `scripts/hooks/` и `core.hooksPath`.

## Релизы
SemVer `v0.x.y` для экспериментальных версий; changelog из коммитов или вручную. GitHub Release с артефактами и контрольными суммами. Не заявляйте о подписанных релизах до настройки подписи. Не публикуйте промышленное приложение автоматически из каждого feature PR.
