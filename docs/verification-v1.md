# V1 verification record

[Русский](ru/docs/verification-v1.md)

Development checks on 2026-11-04; this is not a release certification. Core physical flows used commit `9a68c2f`; the subsequent API-30-minimum candidate installed successfully and displayed a live QR on the same TV.

| Environment | Observed result |
| --- | --- |
| MiTV-MOOQ1, Android 11/API 30 | Install, start/stop, LAN info, QR, TV approval, Deny-first D-pad interaction |
| Installed TV browser | `https://example.com/` handed off and public example page rendered |
| Actual receiver process restart | Previously approved verification credential remained usable after explicit restart/start |
| Single-device revocation | Verification credential rejected with HTTP 401; existing phone pairing preserved |
| Physical LAN negative cases | Unsupported scheme rejected, expired unused QR rejected after more than 65 seconds with the same running process, reused QR rejected, missing authorization rejected, foreign Origin rejected |
| Background receiver | Sender displayed `RETURN_TO_RECEIVER` |
| Android 16 phone, Chrome 156 | Receiver-hosted sender reported successful handoff |
| API 34 TV emulator | APK installed/launched; native QR and LAN info exercised |
| Automated sender, Chromium/WebKit | Pairing, denial, fresh QR navigation, open, errors, forget and accessibility checks |
| Automated static site, Chromium/WebKit | 390/768/1440 layouts, documentation navigation/filter, FAQ and accessibility checks |

The controlled Deny flow was repeated with the remote idle after an earlier approval had uncertain input attribution; only the repeat is counted as denial evidence. A native regression test separately dispatches D-pad Center on the default Deny action.

Missing-browser behavior is covered by controlled Android tests and browser fixtures; no installed browser was removed from the household TV. WebKit automation is not physical Safari certification. Physical iOS/Windows, no-Internet LAN, guest-network isolation and 4K runtime checks remain unverified. No household network settings were changed for these tests. The headless emulator ignored a requested 3840×2160 window-manager size override and remained 1920×1080; its density was restored, and no 4K result is claimed.

HTTP LAN traffic exposes links and credentials to malicious network observers. Pairing requires TV confirmation; this does not encrypt transport. Successful Android intent dispatch does not generally prove page loading or compatibility.

Local final-candidate checks: 37 JVM/resource/socket tests; 12 sender browser checks; 10 site browser checks on each local-root and configured project-base build; three site unit/build checks; Kotlin formatting, Android lint (0 errors, 13 unsuppressed warnings), TypeScript, ESLint, Prettier, OpenAPI, lockfile and guardrail checks passed.

## English/Russian increment, 2026-11-05

45 JVM/resource/socket test cases passed on the configured API 30/34 matrix, including Russian resources, English override, unsupported stored language and D-pad reachability. All 16 sender browser checks and 12 site browser checks passed in Chromium/WebKit; site checks also passed under `/send-tv/`. Three site unit/build checks and six Python guardrail checks passed. TypeScript, ESLint, Prettier, OpenAPI, Kotlin format, Android lint and APK assembly passed. Lint reports 0 errors and 13 warnings, without suppression.

The API 34 emulator installed the bilingual APK and D-pad selection applied Russian; Home/settings screenshots were visually reviewed. Russian mobile landing and direct-visit sender screenshots were reviewed. Pencil MCP reopened all three original English reference documents. Physical household TV/phone were not connected for this increment; prior physical results are not re-attributed to the localized artifact. See [language behavior](localization.md).
