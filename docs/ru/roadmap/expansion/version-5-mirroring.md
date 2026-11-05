# V5: необязательная трансляция экрана/окна, отдельный эпик

[English](../../../../roadmap/expansion/version-5-mirroring.md)

Изучите capture API macOS (ScreenCaptureKit), Windows (Windows Graphics Capture), Linux (PipeWire portals), Android (MediaProjection), iOS (ReplayKit), декодер ТВ и аудиомаршруты. Сеанс согласуется через аутентифицированный signaling; WebRTC где практично; аппаратные H.264/H.265/AV1 по устройствам. Задержка, потери, батарея и CPU измеряются воспроизводимо, без выдуманных чисел.

## Безопасность и ограничения
Захват требует явного разрешения ОС. DRM/защищённый контент может быть чёрным. Не записывайте без согласия и не сохраняйте кадры. Начальный релиз не обещает mirroring.
