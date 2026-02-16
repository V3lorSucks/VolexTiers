# Nametag Display Docs (1.21.4)

## Rendering flow
- `PlayerNametagMixin` modifies `PlayerEntity#getDisplayName()` to format: `[ICON] TIER | PlayerName`.
- `EntityRendererMixin` injects into `PlayerEntityRenderer#renderLabelIfPresent(...)` and calls `TierHudRenderer.renderTierAboveNametag(...)`.
- Tier source is `TierCache.getTierData(playerName)` with fallback to original nametag when data is unavailable.

## Display logic
- Feature is gated by `ModConfig.isEnabled()`.
- Tier mode:
  - Highest tier mode: `ModConfig.shouldShowHighestTier()`
  - Selected gamemode mode: `ModConfig.getSelectedGamemode()`
- Retired tiers are prefixed with `R` (example: `RHT3`).
- Gamemode icon visibility is controlled by `ModConfig.shouldShowGamemode()`.

## Icon mapping
- Uses custom font codepoints (``..``) defined via `assets/minecraft/font/default.json`.
- Supported mappings include sword, crystal/cpvp, netherite, pot, mace, uhc, axe, smp.

## Color/format behavior
- Tier color comes from `ModConfig.getTierColor(displayTier)`.
- Gradient path in nametag text uses `createGradientText(...)` in `PlayerNametagMixin`.
- Extra above-nametag rendering uses `TextRenderer.draw(..., TextLayerType.SEE_THROUGH, ...)`.

## Multi-version notes (1.21.4)
- Keep all version-sensitive rendering changes behind mixin targets and helper methods.
- If mappings/signatures drift in 1.21 patches, update only mixin signatures + adapter paths.
