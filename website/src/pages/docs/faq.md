---
layout: "../../layouts/Docs.astro"
title: FAQ
description: Honest answers about SendTV's Android TV requirements, offline operation, browser compatibility and scope.
---

## Does SendTV work on Samsung or LG TVs?

Not on Tizen or webOS in V1. The first receiver is native Kotlin for Android / Google TV. A TV needs an installed receiver app and a compatible HTTP(S) handler.

## Do I need Internet?

Pairing and control are designed to stay on your LAN. The TV needs Internet if the destination page does. Guest Wi-Fi isolation can block local connections even when Internet works.

## Is it mirroring or Google Cast?

No. The TV opens the URL itself. V1 has no screen mirroring, file transfer, remote mouse, TV power control, or casting protocol.

## Can it open every page?

No. Some pages require Widevine, cookies, browser capabilities, login, or input that is hard with a remote. Log in separately on the TV if the destination requires it.

## Is local HTTP encrypted?

No. A malicious LAN observer can see links and credentials. Explicit TV approval is required but does not provide encryption. Use a trusted network.

## Is there an APK release or CLI?

No verified public APK release is available yet. Developer build instructions are in [getting started](../getting-started/). CLI and native discovery are [planned for V2](../cli/); they are not implemented in V1.

## How do I choose a language?

TV Settings offers system language, English and Russian. The sender initially follows the browser and provides a two-language selector. English/Русский links on the public site open the corresponding page. Device names and entered links are preserved.
