# Changelog

All notable changes to CTL TierTagger will be documented in this file.

## [1.0.0] - 2025-10-20

### Added
- Initial release of Volex TierTagger
- HUD-style tier display above player nametags (similar to Tiers mod)
- Real PNG texture icons for gamemodes (copied from Tiers mod)
- Custom Minecraft font system for icon rendering
- Support for all Central TierList gamemodes:
  - Sword PvP
  - Crystal PvP (Vanilla)
  - Netherite PvP
  - Potion PvP
  - Mace PvP
  - UHC
  - Axe PvP
  - SMP Kit
- Multi-region support (NA, EU, AS/AU)
- Color-coded tiers (HT1-HT5, LT1-LT5)
- Smart caching system (30-minute default)
- JSON configuration file
- Client-side only mod
- Compatible with all Minecraft 1.21.x versions

### Features
- **Display Modes**:
  - Gamemode icons (toggle with `showGamemode` config)
  - Region tags (toggle with `showRegion` config)
  - Automatic highest tier detection across all gamemodes
  
- **Rendering**:
  - Positioned 0.6 blocks above player head
  - Semi-transparent background
  - Always faces camera
  - Proper depth rendering
  
- **Configuration**:
  - `enabled`: Toggle mod on/off
  - `showRegion`: Show/hide region tags
  - `showGamemode`: Show/hide gamemode icons
  - `cacheTimeMinutes`: Cache duration (default: 30)
  - `debugMode`: Enable debug logging

### Technical Details
- Built with Fabric Loader 0.16.0+
- Uses Gradle 9.0
- Requires Java 21+
- Requires Fabric API
- Mod size: ~244 KB
- Includes 31 KB of gamemode icon textures

### API Integration
- Connects to Central TierList API: https://ctltierlist-api-b2s8.vercel.app
- Fetches player data asynchronously
- Caches responses to reduce API load
- Handles API errors gracefully

### Credits
- Gamemode icon textures: Tiers mod by Flavio6561 (GPL-3.0)
- Original HUD concept: Tiers mod
- API: Central TierList

### Known Issues
- None reported yet

### Future Plans
- Add keybinds for toggling display
- Add player search screen (`/tiers <player>`)
- Add cycle keybinds for gamemode selection
- Add more icon style presets
- Add TabList tier display
- Add config GUI (ModMenu integration)

---

## Version Format

This project follows [Semantic Versioning](https://semver.org/):
- MAJOR version for incompatible API/breaking changes
- MINOR version for new functionality in a backwards compatible manner
- PATCH version for backwards compatible bug fixes

## Links

- [GitHub Repository](https://github.com/volextierlist/tiertagger)
- [Modrinth Page](https://modrinth.com/mod/volex-tiertagger)
- [Volex TierList](https://volextierlist.vercel.app)
- [API Documentation](https://ctltierlist-api-b2s8.vercel.app)
