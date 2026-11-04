# Sender application design

Sources: `app_design.pen` (mobile sender screen + pairing/feedback); shared design system in `../design-system.md`.

## V1 mobile sender
390x844 screen, header `SendTV`, selected receiver card `Living Room TV`, status `Connected`, URL field with paste affordance, primary `Open on TV` CTA, small explanatory message, recent links deferred until future versions.

## State machine
`unpaired -> pairing-requested -> waiting-for-tv-confirmation -> paired -> sending -> launched | failed`. Receiver status `launched` means Android accepted handoff, not full webpage load. At every state user can retry/back; timeout is explicit.

## Required screen states
1. Connect: QR deep-link arrived; TV details and trust warning.
2. Await approval: loading and TV instructions; cancel button.
3. Connected: URL field, paste, selected TV, open CTA.
4. Success: `Sent to TV` / `Opened with TV browser` only if launch confirmed; return to input.
5. Error: invalid URL, TV unreachable, token revoked, missing browser.
6. Device switcher (V2 only) must clearly distinguish ID/name.

## Interaction & accessibility
URL field uses URL input mode and accepts paste; sanitize only with explicit feedback; never silently alter user URL. Button touch targets >=44px. Loading disables duplicate submit but supports cancel where possible. All statuses announced to screen readers.

## Desktop sender
Same UI within 440px card centered on desktop, optionally expandable list of devices in V2; no separate desktop design language.
