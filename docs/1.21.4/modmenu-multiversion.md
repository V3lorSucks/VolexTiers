# Mod Menu Multi-Version Docs (1.21.4)

## Integration point
- `ModMenuIntegration` implements `com.terraformersmc.modmenu.api.ModMenuApi`.
- `getModConfigScreenFactory()` returns `ConfigScreen::new`.
- Registered under `entrypoints.modmenu` in `fabric.mod.json`.

## Dependency and compatibility
- Build uses `modCompileOnly "com.terraformersmc:modmenu:11.0.3"`.
- Runtime dependency in `fabric.mod.json` is currently `"modmenu": "*"`.
- Minecraft range in `fabric.mod.json` is `"minecraft": "~1.21"`.

## Recommended multi-version handling
- Keep Mod Menu binding minimal (factory-only class) so version drift is localized.
- If Mod Menu API changes for a patch, patch only `ModMenuIntegration` and keep `ConfigScreen` unchanged.
- Validate config screen open path from Mod Menu on each supported patch version.

## Regression checklist (1.21.4)
- Mod appears in Mod Menu list.
- Config button opens `ConfigScreen` without crashes.
- All category tabs and widgets render and respond.
- Closing returns correctly to Mod Menu parent screen.
