# TV receiver app visual and interaction specification

[Русский](../../docs/ru/design/tv/tv_app_design.md)

Source: `tv_app_design.pen` (1920x1080 receiver home and approval state). Shared colors: `../design-system.md`.

## Screen geometry
Ten-foot UI: safe content margins 96px on 1920x1080, minimum 28px explanatory text, hero device name 54px, clearly differentiated focusable actions. Dark background to reduce glare. Never rely on mouse hover.

## TV home
App name top-left, network status top-right; central `Ready to receive`, large QR card, device name and local address, concise instructions `Scan with your phone to connect`; bottom shortcuts Devices / Settings. QR must be dynamically generated from pairing challenge, not a baked-in design asset.

## Pair request
Full-screen dialog contains sender's proposed name, connection intent, optional verification phrase, `Allow` and `Deny`; D-pad starts focus on Deny or neutral, not default Allow. Timeout and failure recover to Home.

## Browser launch
Use Android `ACTION_VIEW`; if no suitable handler found show `No compatible browser installed` with remediation. Do not pretend full web page loaded on launch callback.

## TV settings
Rename receiver, revoke linked devices, show network reachability and privacy warning; large focus rings and remote-friendly navigation.

## QA states
QR active/expired, paired, disabled network, pending pairing, revoked device, missing browser, long device name. Test real TV at 1080p and 4K scaling; screenshots in docs after actual build.
