---
layout: "../../layouts/Docs.astro"
title: Troubleshooting
description: Recover from LAN, QR, authorization and TV browser errors in SendTV V1 development builds.
toc:
    - { id: receiver-unreachable, label: Receiver unreachable }
    - { id: qr-expired-or-used, label: QR expired or used }
    - { id: access-revoked, label: Access revoked }
    - { id: no-compatible-browser, label: Missing browser }
---

## Receiver unreachable

Keep both devices on the same trusted LAN. Disable guest/client isolation through your network administrator, or use a network that allows device-to-device traffic. Being on the same Wi-Fi name does not guarantee reachability. Cross-VLAN operation is not promised.

Open SendTV and choose Start receiver. The TV needs a private IPv4 address on Wi-Fi or Ethernet. IPv6-only LANs, public-interface binding, and browser mDNS discovery are not implemented in V1. A destination site's Internet requirement is separate from LAN pairing, which does not need Internet.

If the configured port is in use, choose a different port from 1024–65535 in Settings, then rescan the new QR. A port change or network change invalidates the old address and pending pairing sessions. The receiver never creates router port-forwarding rules.

The listener lives in the Android app process. If Android stopped the background process, reopen the app and start it again. No permanent foreground/background launch permission is requested.

## QR expired or used

Scan the current TV QR. Challenges expire after 60 seconds and are consumed at submission, even when the TV later denies the request. A screenshot of an old QR cannot be reused.

If cancellation cannot reach the TV, its pending request expires within 60 seconds. Wait or deny it on the TV, then rescan.

## Access revoked

Scan a fresh QR and ask the TV owner to approve again. Reinstallation, cleared browser storage, and a process restart during token delivery may also require pairing again. The public website never stores a receiver credential.

Remembered browser credentials are stored on that browser's origin. Plain HTTP and browser-local storage are not an encrypted or hardware-backed credential vault. Revoke the device on the TV if a token may have been copied.

## No compatible browser

Install a browser or compatible HTTP(S) handler using the TV's normal installation process, return to SendTV, and retry. SendTV does not install software or override platform restrictions for you.

If the sender asks you to return to SendTV, make the receiver visible before retrying. Android may deny launches from background apps. An accepted handoff does not establish that a page rendered.

## Invalid URL

Use a complete HTTP or HTTPS URL with a host. Remove embedded username/password credentials, spaces and control characters. Maximum length is 4096 characters. Schemes such as `javascript:`, `file:`, `intent:`, and `content:` are rejected. Some Unicode hostnames may need their ASCII/Punycode form.

## Too many requests

Wait a minute. Pairing is limited to five attempts per source per minute and twenty globally; pending requests and trusted devices are bounded. Revoke old devices on the TV if its device limit is reached. Do not disable these limits to get a test passing.
