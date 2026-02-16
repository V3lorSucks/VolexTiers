# GUI Docs (1.21.6)

## Main config screen
- Entry UI class: `ConfigScreen`.
- Categories:
  - `General`
  - `Colours`
- General controls include:
  - Mod enabled toggle
  - Gamemode icon toggle
  - Gamemode selector
  - Highest-tier mode toggle
  - Debug toggle
  - Player search button
  - Reset settings button

## Colours tab
- Per-tier rows for `HT1/LT1` through `HT5/LT5`.
- Each row supports:
  - Primary color picker
  - Gradient toggle
  - Optional gradient color picker
- Includes scroll handling and viewport clipping.

## Search/player flow
- `PlayerSearchScreen` handles player input and async data loading.
- Uses `SkinLoader.loadSkinAndCreateWidget(...)` + `TierListAPI.fetchPlayerTier(...)`.
- On success opens `PlayerInfoScreen`; on failure shows inline error text.

## UX details
- Search supports Enter/Numpad Enter and ESC to close.
- `shouldPause()` returns false for non-pausing overlay behavior.
- `resize(...)` preserves text input when window size changes.

## Multi-version notes (1.21.6)
- Keep screen feature logic version-agnostic and isolate API signature changes in small wrappers.
- Re-test widget constructors and draw calls after each patch bump in 1.21.x.
