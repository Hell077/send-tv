# Landing page design specification

Source: `website.pen` (editable Pencil frames for desktop landing + mobile). Design system: `../design-system.md`.

## Desktop 1440x900
Top navigation with SendTV logo, Features, How it works, Docs, GitHub. Hero section: compact eyebrow `LOCAL-FIRST TV HANDOFF`; H1 `A link on your phone. A page on your TV.`; two CTAs; decorative paired phone/TV interface with connecting line (no false live status). Second section: three steps and mini feature cards; footer with accurate compatibility and privacy information.

## Mobile 390x844
Single-column stacked hero; menu button, title around 36px; CTAs wrap/stack; illustrated TV preview scales without horizontal scroll; FAQ/docs easy access.

## Components and states
- CTA labels: Get started, Read docs, View GitHub. View GitHub URL configured from repo, never invented.
- Support matrix: Android TV supported (when V1 released), Tizen, webOS, Mirror planned; labels truthful.
- Animated lines optional; respect reduced motion.
- Docs template: left sidebar >=240px, header search, central readable max-width 760px, right TOC on wide screens.

## Design QA
Screenshots at 390, 768, 1440 px; verify focus order, contrast, no clipping, copy matches `website/content.md`; image alt text describes hero illustration as illustration.
