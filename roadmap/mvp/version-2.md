# MVP V2 — Discovery, multiple devices, CLI

## Dependencies
V1 signed off with security and end-to-end tests. API capabilities stable.

## Work packages
1. Android TV native DNS-SD advertising `_sendtv._tcp` with stable instance ID and sanitized TXT metadata.
2. Go CLI `sendtv discover`, `sendtv pair`, `sendtv devices`, `sendtv open URL [--device id]`, `sendtv status`; no OS shell expansion of URL arguments.
3. Device selection UI for multiple trusted receivers; friendly names and last-seen timestamps.
4. Browser sender reconnect and re-pair flow, deterministic receiver address QR fallback; manual IP entry in native CLI.
5. Retain trusted device keys/tokens in OS keychain/credential storage for native sender; do not write token to CLI history or logs.
6. CLI exit codes, machine-readable `--json` output, cancellations, timeouts and stable error messages.
7. Improve GitHub Pages: CLI install, commands, discovery troubleshooting, capability docs.

## Acceptance criteria
- [ ] Multiple receiver announcements discovered on a typical single-subnet home LAN.
- [ ] CLI can select by ID and send URL to correct TV, including duplicate display names.
- [ ] QR/direct address fallback works if multicast blocked but unicast LAN allowed.
- [ ] Discovery events never grant authorization.
- [ ] CLI stores no plain-text token except with explicitly documented opt-in fallback.
- [ ] Tests cover discovery records parsing, duplicate IDs, stale IP, timeouts, auth expiry, name collisions.

## Not promised
Cross-subnet discovery without network setup; browser mDNS access; arbitrary URL support on TV without receiver.
