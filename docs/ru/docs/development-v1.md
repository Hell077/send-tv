# Разработка V1

[English](../../development-v1.md)

Активный план: [MVP V1](../roadmap/mvp/version-1.md). Приёмник, ядро безопасности, ограниченный HTTP и отправитель реализованы для разработки. Полная физическая сквозная приёмка ещё открыта.

## Доступное поведение
- Нативный Kotlin Android Views с Leanback launcher, без требования touchscreen.
- Главный экран изначально остановлен; владелец явно запускает после предупреждения LAN. Вращающийся QR с challenge на 60 секунд виден только при частном IPv4 Wi-Fi/Ethernet listener.
- Имя: 1–48 UTF-16 code units, без пробелов по краям, управляющих и Unicode format символов. Ошибка не меняет сохранённое имя.
- Фокус настроек на возврате, разрешения — на отказе. Отзыв одного или всех устройств. Иерархия следует проверенному Pencil; фактическая ТВ-отрисовка проверяется отдельно.
- Частные preferences: identity, имя, порт, хеши. Только INTERNET и ACCESS_NETWORK_STATE. Исключения backup/transfer согласно [Android](https://developer.android.com/identity/data/autobackup). Нет аналитики, облака и удалённого прокси.
- Отправитель: сопряжение, код, отмена, open, опрос, ошибки URL/браузера/отзыва/доступности, идемпотентность. Session storage по умолчанию, local storage — явно.
- English/Русский: ТВ следует системе или явному выбору в настройках; отправитель — браузеру или переключателю. Введённые данные и код не меняются. Язык хранится отдельно от авторизации. Русские страницы сайта — `/ru/`.

## Жизненный цикл и платформа
Нет foreground service, overlay, исключения фонового запуска и повышенных прав. Listener живёт в процессе Android и переживает обычный pause для опроса передачи, но Android может завершить фон. После перезапуска приёмник остановлен; откройте и запустите. Доверенные хеши сохраняются, pending и операции — нет.

`ACTION_VIEW` выполняется только с видимой Activity. В фоне — `RETURN_TO_RECEIVER`, пользователь возвращается в приложение. `launched` — Android принял intent, не загрузил сайт. Без обработчика — `NO_BROWSER`. Не обещается неограниченное удалённое управление.

Network callbacks выбирают только частный IPv4 Wi-Fi/Ethernet. Потеря/смена адреса/порта закрывает listener, отменяет очередь данного сеанса, challenge и выдачу pending-токенов. Stop немедленно отключает dispatch, пока worker закрывает сокеты. Хеши сохраняются до отзыва. IPv6-only не поддерживается.

Подробнее: [сопряжение](pairing-v1.md), [HTTP](http-v1.md).

Предварительный ID `org.sendtv.receiver` — placeholder. До окончательного выбора владельца и имени не публикуйте под ним приложение.

## Сборка и проверки
JDK 21, Android SDK platform 36, build-tools 36.0.0, Node 24, npm, Python 3. `ANDROID_HOME` указывает SDK, `JAVA_HOME` — JDK 21; JBR 21 Android Studio подходит. Wrapper Gradle 9.4.1 с checksum, AGP 9.2.0 со встроенным Kotlin. Runtime использует platform API без Go и сторонней UI-библиотеки.

```sh
npm ci --ignore-scripts --no-audit --no-fund
./gradlew ktlintCheck :tv:testDebugUnitTest :tv:lintDebug :tv:assembleDebug --console=plain
npm run check:protocol
npm run typecheck
npm run lint:web
npm run format:web
npm run test:browser
python3 -m unittest discover -s scripts -p 'test_*.py'
git diff --check
```

Формат Kotlin: `./gradlew ktlintFormat`. Asset task собирает TypeScript через esbuild. Сначала установите `npx playwright install chromium webkit`. На каждый движок новый тестовый receiver process, чтобы сохранить production rate limits. HTTP/auth настоящие, решение ТВ и launch — тестовые адаптеры. Это не физический Safari/ТВ. Трассы/скриншоты credential-bearing browser-сценариев отключены.

APK: `tv/build/outputs/apk/debug/tv-debug.apk`, только локальный ключ разработки, без релизной подписи и публичной загрузки. Build outputs, ключи, IDE и локальные SDK-пути игнорируются.

```sh
adb install -r tv/build/outputs/apk/debug/tv-debug.apk
adb shell am start -n org.sendtv.receiver/.ReceiverActivity
```

Устанавливайте только на разрешённое тестовое ТВ. ADB — инструмент разработки, не транспорт продукта. Не открывайте его в недоверенных сетях.

Начальный минимум Android 11/API 30 выбран после MiTV-MOOQ1; API 34 также проверяется эмулятором, resource-тесты — на 30/34. [ADR 0002](../architecture/decisions/0002-v1-android-floor.md). Сборка с минимумом 30 успешно переустановлена на физическом ТВ до локализации; русский UI проверяется отдельно.

## CI и hooks
`.github/workflows/verify.yml`: Kotlin format, JVM/resource integration, Android lint, APK, browser-сценарии, OpenAPI, строгий TS, ESLint/Prettier, Python guardrails, whitespace и темы PR-коммитов. Изменения отправляются в feature-ветку origin. Первая ошибка hosted run — устаревший SDK `tools`; теперь явно `platform-tools`. Смотрите результат каждой новой ревизии.

Включение hooks в своём checkout:

```sh
git config core.hooksPath scripts/hooks
```

Hooks проверяют staged whitespace, типовые секреты и файлы свыше 2 MiB; тип/область/72 символа темы; pre-push целевые тесты. Они не находят все секреты и не заменяют review/CI. Автоматически не включаются.

## Оставшаяся проверка
На 2026-11-04: APK, ktlint, 37 JVM/resource/socket-тестов, native focus/intents; по 6 sender-сценариев Chromium/WebKit; TS, ESLint, OpenAPI. Android lint: 0 ошибок, 13 предупреждений, без подавления. Это локальные результаты, не сертификация.

- Pencil доступен через MCP. Заголовок `2.22` давал пустой ТВ-документ; редактор использует `2.21`. Три заголовка мигрированы без смены узлов/геометрии, шесть фреймов просмотрены через screenshots/Get. [Проверка](../design/validation/README.md). Это эталоны, не доказательство точности runtime.
- APK установлен на API 34 и физический MiTV API 30. D-pad start/stop, LAN info, Android 16 Chrome 156 sender. Разрешение/отказ, HTTPS, persistence, отзыв — в [протоколе](verification-v1.md). 4K, offline/isolation и физический iOS ещё открыты.
- Статический сайт реализован и локально проверен; публичный Pages, оставшаяся матрица и полная приёмка открыты.
- HTTP раскрывает ссылки и токены наблюдателю; нельзя называть его зашифрованным. Требуется подтверждение ТВ. Запрещены произвольная загрузка URL и прокси localhost/LAN.
- Универсальная совместимость ТВ/сайтов не обещается. Нужен обработчик; login, Widevine, cookies и пульт могут ограничивать результат.

В плане отмечены только критерии с совокупными автоматическими и физическими доказательствами. Полная V1 ещё не принята.

## Исправления runtime UI
Реальный ТВ выявил повторные accessibility-события каждую секунду без изменения текста. Теперь подписи обновляются только при изменении; countdown отображается шагом 5 секунд, истечение проверяется каждый tick. Есть регрессия на неизменный текст. D-pad достигает порта, неверный порт не сохраняет новое имя. Отсутствие браузера объясняется на ТВ и отправителе. Цвета явно соответствуют палитре эталона.

## Статический сайт
`npm run check:site`, `npm run format:site`, `npm run build:site`, `npm run test:site`, `npm run test:site:browser`. `npm run dev:site` — loopback preview. Пути выводятся из `GITHUB_REPOSITORY`, необязательный HTTPS `SITE_URL` задаёт canonical. ТВ не нужен. [Публикация](../website/publishing.md).

Выбор языка и проверки переводов: [английский и русский](localization.md).
