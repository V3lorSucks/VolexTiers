# Minecraft 1.21.11 Mod Compatibility Notes

Source: DeepWiki summaries for FabricMC/fabric-loader, FabricMC/fabric, FabricMC/fabric-loom.
Scope: Version Detection + Abstraction Layer strategy for this mod on Minecraft 1.21.11.

## 1) Runtime version detection
- Use Fabric Loader's runtime game version (`FabricLoader.getInstance().getModContainer("minecraft")`) as the authoritative detected version.
- Keep `fabric.mod.json` constraints explicit so Loader can block unsupported ranges before runtime.
- Treat patch updates as compatible by default, but gate any fragile code paths behind detected-version checks.

## 2) Abstraction layer pattern (recommended)
- Keep version-specific logic behind a small adapter interface (example: `VersionBridge`).
- Route all NMS/mixin-sensitive calls through adapters instead of direct scattered version checks.
- Prefer Fabric API module abstractions/events over direct internals wherever possible.
- If a patch changes mappings/signatures, update only adapter + mixin targets, keep feature code unchanged.

## 3) Regression checklist after upgrading to this version
- Client boot test (no crashes, mixins apply cleanly).
- Dedicated server boot test (entrypoints + networking still initialize).
- UI/render smoke test for TierTagger overlays/widgets.
- Data/network test (packet handling, serialization/deserialization).
- Third-party mod coexistence smoke test (common Fabric stack).
- Log scan for mixin target misses, mapping warnings, classloading errors.

## 4) Patch-specific note for 1.21.11
- DeepWiki does not expose unique architecture changes per 1.21.x patch in one place.
- Apply the same Loader detection + adapter abstraction, then validate with the checklist above.
- If a failure appears, isolate it in the version adapter/mixin layer only.
