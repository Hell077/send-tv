# Shared design system

Brand: SendTV (working name). Personality: practical, calm, crisp, device-focused. Visual metaphor: one clean blue path between screens, not a cluttered Wi-Fi icon field.

## Colors
- Canvas `#080D19`, surface `#101A2C`, raised `#17243A`, border `#29405F`.
- Accent `#60A5FA`, hover `#93C5FD`, accent dark `#1D4ED8`.
- Primary text `#F8FAFC`, secondary `#A8BCD6`, success `#4ADE80`, danger `#FB7185`.
- Use saturated accent for meaningful calls to action; no glossy neon.

## Typography / geometry
- Website: Inter or system fallback; headings 56/40/30/24px, body 16–18px, mono for protocol code.
- Mobile: system UI, min 16px body and 44px touch hit area.
- TV: high-legibility system font, 10-foot UI with 28–48px text depending on 1080p screen, focus ring >=3px; deliberate D-pad focus order.
- Radius cards 16–24px, input 12px; 8px spacing scale; clean 1px borders.

## Components
Primary/secondary buttons, URL field, TV device row, status dot, error banner, QR pairing block, confirmation dialog, empty/loading states, docs tab/sidebar and code snippet. Every component has hover, focus, pressed, disabled and error state where applicable.

## Accessibility
WCAG 2.2 AA aspiration for website/web sender; check actual text contrast, reduced-motion media queries, semantic labels, error semantics, D-pad focus navigation on TV. Never rely on color alone.

## Pen source
`.pen` files use Pencil JSON format with editable shapes, labels and frames. Validate by opening in Pencil; if schema changes, migrate with editor/CLI and commit updated files. SVG may be exported for previews but is not source of truth.
