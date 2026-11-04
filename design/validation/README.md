# Pencil reference validation — 2026-11-04

These are screenshots returned by the installed Pencil (`Pen.app`) MCP server. They show **design references**, not screenshots of a running SendTV app or website. No implementation fidelity or device compatibility is implied.

## Connection and schema

Local Codex MCP configuration enables `pencil`, using the bundled `mcp-server-darwin-arm64` with `--app desktop --agent codexCLI`. The server was accessed over MCP stdio because its tools were not directly exposed to the conversation. No computer-use permissions or Codex configuration were changed.

The server's `read_skill` response and `pen-schema.md` declare document version `2.21`. The original references declared `2.22`; opening the TV reference with `get_app_state` reported an empty document. A temporary copy differing only in the version header loaded both original TV frames. All three repository references now use `2.21`. A structural comparison against Git confirmed that every node, label, ID, color, and coordinate on disk is otherwise unchanged.

`get_app_state`, `execute`/`Get`, and `TakeScreenshot` were used to inspect the reference frames. The TV document, initially cached as empty by the editor, was rehydrated through MCP from the original editable nodes. The sender and website documents loaded directly after their header corrections. Screenshots are the MCP tool's preview resolution, not full-resolution app captures.

## Reviewed frames

| Reference | Design size | MCP screenshot |
| --- | --- | --- |
| TV receiver home | 1920 × 1080 | [Home](tv-home.png) |
| TV approval | 1920 × 1080 | [Approval](tv-approval.png) |
| Sender connected | 390 × 844 | [Connected](sender-connected.png) |
| Sender pending approval | 390 × 844 | [Pending](sender-pending.png) |
| Website desktop | 1440 × 900 | [Desktop](website-desktop.png) |
| Website mobile | 390 × 844 | [Mobile](website-mobile.png) |

Screenshots show the expected dark palette, editable labels and shapes, non-scannable illustrative QR blocks, approval actions, sender form and waiting state, and landing-page hierarchy. `Get` visitors reported no partially/fully clipped nodes. These checks do not establish comprehensive accessibility, focus behavior, or pixel-perfect implementation.

## Implementation follow-up

- TV reference has a 1px Deny outline; runtime focus must be at least 3px and begin on Deny or a neutral action. Some TV explanatory labels are below the visual spec's 28px minimum.
- Sender reference includes body/status text at 15px; runtime body copy must meet the 16px minimum. The desktop centered form, 320px behavior, errors, expiry, and revoked states need implementation and browser tests.
- Website mobile menu illustration is 42px; runtime hit area must be at least 44px. The landing art is an illustration, not a live receiver status. Public copy must reflect actual release availability.
- QR placeholders are design illustrations. Runtime QR must encode a fresh expiring pairing challenge, never a permanent credential.
- Check plaintext-LAN warnings, actual contrast, keyboard/D-pad focus, long names, offline and failure states in the implemented UI. Recheck the edited designs in Pencil when those reference states are refined.

This review removes the Pencil-access blocker. It does not complete any end-to-end V1 acceptance criterion.
