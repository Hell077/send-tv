# English and Russian

[Русский](ru/docs/localization.md)

The TV receiver, bundled sender and static documentation site support English and Russian. English originals remain in place; Russian engineering counterparts mirror their paths under `docs/ru/`. Update both documents when changing a requirement. Machine-readable OpenAPI, source identifiers, protocol fields/error codes, commands and URLs retain their original spelling.

## TV

Settings → Language offers System language, English and Русский. The selection is private local app state; it does not change the TV's system language or grant access. Unsupported stored values fall back to the system configuration, whose unsupported languages use English resources. Recreating the Activity applies the selection and returns to Home; unsaved name/port edits are not committed by language selection. Pairing approval still defaults to Deny. User-saved device names are preserved. The default receiver name is resolved in the application system locale, so the TV and `/v1/info` expose the same name even when the Activity has an explicit language override.

Network status uses Android string resources rather than stored English sentences so the selected Activity locale also applies to asynchronous network updates. Strings and format arguments are checked for English/Russian coverage.

## Sender

A supported browser language initializes the selection; otherwise English is used. Explicit choice is saved as `sendtv.language` when local storage is available, independently of credentials. Switching updates labels, status and errors without reloading, cancelling an in-flight pairing request, changing verification digits, or altering input. Failure to save a language preference does not prevent the current tab from working. Receiver names and entered URLs are not translated.

Plain HTTP limitations, TV approval, missing-browser and background-launch instructions are translated. Stable protocol codes remain untranslated and are mapped to user-facing messages.

## Public site and documents

The static site provides `/ru/` counterparts with Russian HTML language/metadata, navigation, guides, privacy and changelog. English/Русский links preserve the current guide and project Pages base. The site does not contact a TV. Engineering translations under `docs/ru/` have reciprocal English links and keep future-version plans labelled as future work.

## Verification

Run the normal Android, sender and site checks plus `python3 -m unittest discover -s scripts -p 'test_*.py'`. The localization guard checks Android key/format parity, engineering counterpart presence and local translated-document links. Browser regressions cover Russian initialization, explicit override/reload, live language changes during pairing, unchanged input/code, errors and fallback. Test protocol values do not change with the UI language.

Runtime review on 2026-11-05 used the API 34 emulator and Chromium/WebKit; the household physical TV/phone were unavailable during this localization check. Earlier physical English V1 evidence does not certify Russian layout on that hardware. Native settings/Home and Russian site screenshots are recorded separately. Original editable English design references were reopened through Pencil MCP; Russian text expansion was assessed in the actual runtime. No pixel-perfect Russian design match is claimed.
