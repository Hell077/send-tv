# V1 development

Active roadmap: [MVP V1](../roadmap/mvp/version-1.md). The native receiver, security core, bounded HTTP adapter, and bundled sender are implemented for development verification. End-to-end physical-device acceptance remains open.

## Available behavior

- Native Kotlin Android Views app with a Leanback launcher and no touchscreen requirement.
- Home starts stopped; the owner explicitly starts the receiver after reading the LAN warning. A rotating 60-second challenge QR is displayed only while a private IPv4 Wi-Fi/Ethernet listener is available.
- Settings lets the TV owner rename the receiver. Names must contain 1–48 UTF-16 code units, without surrounding whitespace, control characters, or Unicode format characters. Invalid input shows an error without changing the saved name.
- Settings starts focus on Back; pairing approval starts on Deny. Devices can be revoked individually or all at once. The UI follows the validated Pencil reference hierarchy; actual TV rendering remains under verification.
- Private preferences store receiver identity, name, port, and token hashes. Only INTERNET and ACCESS_NETWORK_STATE permissions are requested. Backup and transfer exclusions follow [Android backup documentation](https://developer.android.com/identity/data/autobackup). No analytics, cloud relay or remote proxy exists.
- The bundled TypeScript sender supports pairing, verification codes, cancel, open, status polling, malformed URL/missing-browser/revoked/unreachable errors, and retry idempotency. Browser credentials use session storage by default; remembering them in local storage is explicit.

## Lifecycle and platform constraints

The app does not request a foreground service, overlay, background-start exemption, or elevated permissions. The listener lives in the Android process and survives an ordinary activity pause long enough to poll a handoff, but Android may terminate a background process. After process restart the receiver starts stopped; TV owners reopen it and start it again. Persistent trusted-device hashes survive, while pending requests and operations do not.

`ACTION_VIEW` runs only with a visible receiver Activity. If SendTV is backgrounded, the command returns `RETURN_TO_RECEIVER`; the sender asks the owner to return to the receiver before retrying. `launched` means Android accepted the intent, not that the destination loaded. Missing handlers are reported as `NO_BROWSER`. This explicitly respects Android restrictions rather than claiming unrestricted remote control.

Network callbacks select only private IPv4 Wi-Fi/Ethernet addresses. Loss/address/port changes close the listener, cancel queued dispatch on that session, and invalidate challenges and pending token delivery. Stop disables dispatch immediately while the lifecycle worker closes sockets. Trusted-device hashes persist until explicit revocation. IPv6-only LANs are not currently supported.

See [pairing](pairing-v1.md) and [HTTP boundary](http-v1.md) for limits and threat-model details.

The provisional application ID `org.sendtv.receiver` remains a placeholder. Do not publish an application under it until project ownership and naming are finalized.

## Build and checks

Requirements: JDK 21, Android SDK platform 36 and build-tools 36.0.0, Node 24, npm, Python 3. Point `ANDROID_HOME` to your SDK and `JAVA_HOME` to JDK 21. Android Studio's bundled JBR 21 works locally. The Gradle 9.4.1 wrapper includes the distribution checksum; AGP is pinned to 9.2.0. AGP provides built-in Kotlin support. Runtime app code uses platform APIs without a Go runtime or third-party runtime UI libraries.

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

Use `./gradlew ktlintFormat` for Kotlin formatting. The Android asset task invokes esbuild to bundle the sender from TypeScript. Run `npx playwright install chromium webkit` once before browser tests. Browser projects run with a fresh test-only receiver process per engine to preserve production rate limits; the actual HTTP/auth core runs, while TV approval and Android launch are local test adapters. This is not a physical Safari or TV browser test. Browser traces/screenshots are disabled during these credential-bearing flows.

Debug APK: `tv/build/outputs/apk/debug/tv-debug.apk`. It is signed with a local development key only; there is no release signing or download release. Build outputs, keystores, IDE files, and local SDK paths are ignored.

To install on a developer-authorized Android TV test device:

```sh
adb install -r tv/build/outputs/apk/debug/tv-debug.apk
adb shell am start -n org.sendtv.receiver/.ReceiverActivity
```

ADB is a development installation tool here, not a product dependency or sender transport. Normal V1 operation must not require it. Do not expose ADB on untrusted networks.

The initial supported floor is Android 11/API 30, selected after physical MiTV-MOOQ1 testing; API 34 is also exercised by the emulator. Framework/resource tests now cover API 30 and 34. See [ADR 0002](../architecture/decisions/0002-v1-android-floor.md) for scope. The final API-30-minimum candidate was reinstalled successfully on the physical TV.

## CI and hooks

`.github/workflows/verify.yml` runs Kotlin formatting, JVM unit/resource integration tests, Android lint, debug APK build, sender browser flows, OpenAPI validation, strict TypeScript, ESLint/Prettier, Python guardrail tests, whitespace checks, and PR commit-subject validation. Commits are pushed to the feature branch on the configured origin. The first hosted run exposed an obsolete SDK `tools` package in the setup action; the workflow now requests `platform-tools` explicitly. Hosted outcomes must be checked for each subsequent commit.

Enable local hooks explicitly for your checkout:

```sh
git config core.hooksPath scripts/hooks
```

Hooks check staged whitespace, common credential patterns and files over 2 MiB; commit subjects follow the project's allowed types/scopes and 72-character limit; pre-push runs targeted checks. These guardrails do not detect all secrets and do not replace review or CI. Hooks are not silently enabled by the repository.

## Remaining validation and implementation

Local verification on 2026-11-04: debug APK assembled; ktlint passed; 37 JVM/resource/real-socket tests passed, including native approval focus and intent dispatch; 6 sender browser flows passed in Chromium and 6 in WebKit; strict TypeScript, ESLint and OpenAPI validation passed. Android lint completed with 0 errors and 16 warnings. No warning was suppressed. These results are local evidence, not device compatibility certification.

- Pencil access is available through its configured local MCP server. The original `2.22` schema header caused the TV document to appear empty; the installed editor publishes schema `2.21`. All three headers were migrated without changing node content or geometry, and all six reference frames were inspected with MCP screenshots and layout queries. See [design validation](../design/validation/README.md). This validates editable references; the development shell is still provisional and has not been visually matched to them.
- The APK installed and launched on an API 34 Android TV emulator and a physical MiTV-MOOQ1 running Android 11/API 30. D-pad start/stop and real LAN info requests were observed. The connected Android 16 phone runs Chrome 156 and its sender reported a handoff. Controlled approval, denial, HTTPS handoff, restart persistence and single-device revocation were also exercised; see the [verification record](verification-v1.md). 4K scaling, offline/isolation and physical iOS tests remain open.
- The static site is implemented and tested locally. Public Pages deployment, offline/isolated LAN scenarios, remaining physical browser/device tests, and full acceptance remain open.
- HTTP on a LAN exposes links and credentials to malicious observers; it must never be described as encrypted. New-device pairing must require TV confirmation. No receiver endpoint may fetch arbitrary URLs or proxy LAN/localhost resources.
- No claim of universal TV or website compatibility is made. Android TV needs an installed receiver and compatible URL handler; login, Widevine, cookies, and remote input can still limit destinations.

No V1 acceptance box should be checked based solely on this increment.

## Runtime UI regression fixes

Physical-TV inspection exposed repeated accessibility text changes on each one-second poll even when labels were unchanged. Labels now update only when their content changes; approval expiry updates in five-second display increments while expiration is still enforced on every tick. A regression test verifies unchanged text does not generate repeated updates. D-pad navigation now reaches the configurable port, and invalid ports cannot save a different receiver name. Missing-browser failures show remediation on both the TV and sender. Text colors explicitly follow the validated design palette.

## Static website

Run `npm run check:site`, `npm run format:site`, `npm run build:site`, `npm run test:site` and `npm run test:site:browser`. `npm run dev:site` starts a loopback-only preview. The build derives project paths from `GITHUB_REPOSITORY`; an optional HTTPS `SITE_URL` sets the canonical deployment URL. No TV connection is required. See [publication](../website/publishing.md).
