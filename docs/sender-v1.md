# V1 browser sender

[Русский](ru/docs/sender-v1.md)

The mobile client is the receiver-hosted TypeScript website, not a separate native mobile APK. Open it by scanning the TV QR. The public GitHub Pages website is documentation and never connects to a receiver.

The sender removes the QR fragment from the address bar immediately. A new challenge arriving in the same already-open tab is processed through `hashchange` as well as first navigation. This matters when phone browsers reuse a tab during rescanning. An in-flight older pairing poll cannot overwrite the new connection state; obsolete pending requests are cancelled on a best-effort basis and otherwise expire on the TV.

Token storage is per receiver identity and browser origin. Session storage is the default; permanent local storage requires the explicit Remember checkbox. Storage failure permits the current in-memory session but does not imply that a credential was saved. Forgetting a browser only deletes its local credential; revoke on the TV to disable other copies.

The interface announces status/errors, uses at least 16px body copy and 44px touch targets, keeps a single sender layout from 320px to desktop, and exposes cancel/reconnect/retry actions. Clipboard access can be unavailable over local HTTP; normal browser paste remains supported. Request IDs are reused across ambiguous network failures to avoid duplicate launches, and renewed after a definitive launch failure or success.

Chromium and WebKit tests run the actual Kotlin HTTP/auth implementation with test-only TV decision and Android launch adapters. Eight flows per engine cover direct visits, rescans in an open tab, approval/open/reload/forget at 320px, invalid URL and missing-browser/background recovery, denial, automated accessibility, Russian language selection during pairing, persistence and unsupported-language fallback. This is engine-level evidence; physical Android/iOS and TV browser results are recorded separately. No browser trace or credential-bearing request dump is retained.

English and Russian follow the browser preference initially; the explicit selector stores only `sendtv.language`. It does not reload the page, cancel pairing, change verification codes or edit names/URLs. Language storage is independent of credentials.
