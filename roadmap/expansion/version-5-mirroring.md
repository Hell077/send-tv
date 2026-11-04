# V5 — Optional screen / window mirroring (separate epic)

Research capture APIs on macOS (ScreenCaptureKit), Windows (Windows Graphics Capture), Linux (PipeWire portals), Android (MediaProjection), iOS (ReplayKit), TV video decoder and audio routes. Negotiate session via authenticated signaling, use WebRTC where practical, evaluate hardware H.264/H.265/AV1 support by device. Measure latency, packet loss, battery and CPU with reproducible tests; no fabricated benchmark numbers.

## Security and constraints
Screen capture requires explicit OS permission. Secure/DRM-protected content may be blacked out. Respect user privacy: never record without consent and do not persist frames. The initial release does not promise mirroring.
