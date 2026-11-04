# V1 development

Active roadmap: [MVP V1](../roadmap/mvp/version-1.md), step 1 (Android TV shell and settings). This increment advances that step without satisfying the end-to-end V1 acceptance criteria.

## Available behavior

- Native Kotlin Android Views app with a Leanback launcher and no touchscreen requirement.
- Home reports that pairing, URL handoff, and networking are unavailable.
- Settings lets the TV owner rename the receiver. Names must contain 1–48 UTF-16 code units, without surrounding whitespace, control characters, or Unicode format characters. Invalid input shows an error without changing the saved name.
- Settings starts focus on Back; focus styling and navigation targets are present. This is a provisional development screen, not the final Pencil design.
- Private local preferences store only the receiver name. No network permission, remote endpoint, analytics, or tokens exist in this increment. Backup and transfer exclusions are explicit, following [Android backup documentation](https://developer.android.com/identity/data/autobackup).

The provisional application ID `org.sendtv.receiver` remains a placeholder. Do not publish an application under it until project ownership and naming are finalized.

## Build and checks

Requirements: JDK 21, Android SDK platform 36 and build-tools 36.0.0, Node 24, npm, Python 3. Point `ANDROID_HOME` to your SDK and `JAVA_HOME` to JDK 21. Android Studio's bundled JBR 21 works locally. The Gradle 9.4.1 wrapper includes the distribution checksum; AGP is pinned to 9.2.0. AGP provides built-in Kotlin support. Runtime app code uses platform APIs without a Go runtime or third-party runtime UI libraries.

```sh
npm ci --ignore-scripts --no-audit --no-fund
./gradlew ktlintCheck :tv:testDebugUnitTest :tv:lintDebug :tv:assembleDebug --console=plain
npm run check:protocol
python3 -m unittest discover -s scripts -p 'test_*.py'
git diff --check
```

Use `./gradlew ktlintFormat` for Kotlin formatting. Debug APK: `tv/build/outputs/apk/debug/tv-debug.apk`. It is signed with a local development key only; there is no release signing or download release. Build outputs, keystores, IDE files, and local SDK paths are ignored.

To install on a developer-authorized Android TV test device:

```sh
adb install -r tv/build/outputs/apk/debug/tv-debug.apk
adb shell am start -n org.sendtv.receiver/.ReceiverActivity
```

ADB is a development installation tool here, not a product dependency or sender transport. Normal V1 operation must not require it. Do not expose ADB on untrusted networks.

Provisional minimum API is 26. JVM resource/activity tests cover API 26 and 34; this does **not** establish an installable or supported Android TV floor. Select the supported floor only after emulator and physical TV tests.

## CI and hooks

`.github/workflows/verify.yml` runs Kotlin formatting, JVM unit/resource integration tests, Android lint, debug APK build, OpenAPI validation, Python guardrail tests, whitespace checks, and PR commit-subject validation. The workflow has been added but has not run on a remote host: this checkout has no configured Git remote. Website build and browser tests must be added with those implementations; no passing website build is claimed.

Enable local hooks explicitly for your checkout:

```sh
git config core.hooksPath scripts/hooks
```

Hooks check staged whitespace, common credential patterns and files over 2 MiB; commit subjects follow the project's allowed types/scopes and 72-character limit; pre-push runs targeted checks. These guardrails do not detect all secrets and do not replace review or CI. Hooks are not silently enabled by the repository.

## Remaining validation and implementation

Local verification on 2026-11-04: debug APK assembled; ktlint passed; 10 Android JVM/resource tests passed (8 activity tests across API 26/34 plus 2 name-validator tests); 4 Python guardrail tests passed; OpenAPI validation passed without warnings. Android lint completed with 0 errors and 7 warnings: dependency/SDK version notices, heading semantics ignored below API 28, and the provisional vector banner size. No warning was suppressed. These results are local evidence, not a hosted CI result or device compatibility certification.

- Pencil access is available through its configured local MCP server. The original `2.22` schema header caused the TV document to appear empty; the installed editor publishes schema `2.21`. All three headers were migrated without changing node content or geometry, and all six reference frames were inspected with MCP screenshots and layout queries. See [design validation](../design/validation/README.md). This validates editable references; the development shell is still provisional and has not been visually matched to them.
- No Android TV emulator is configured and no physical device is attached. No installation, TV screenshots, remote input, 1080p/4K rendering, or physical accessibility tests have been performed. The only existing emulator profile is a phone profile.
- Next implementation steps remain receiver lifecycle/LAN binding, pairing state machine and approval, secure token handling, API transport, URL dispatch, sender, static public site, and the full acceptance matrix. Extend canonical OpenAPI before implementing the pairing endpoints.
- HTTP on a LAN exposes links and credentials to malicious observers; it must never be described as encrypted. New-device pairing must require TV confirmation. No receiver endpoint may fetch arbitrary URLs or proxy LAN/localhost resources.
- No claim of universal TV or website compatibility is made. Android TV needs an installed receiver and compatible URL handler; login, Widevine, cookies, and remote input can still limit destinations.

No V1 acceptance box should be checked based solely on this increment.
