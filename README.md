# Volex TierTagger

A Minecraft Fabric mod that displays player PvP tiers from [Volex TierList](https://volextierlist.vercel.app) directly in their nametags.

## Features

- 🏷️ **HUD Tier Display**: Shows player tiers **above their nametags** (similar to Tiers mod)
- 🎮 **Actual Gamemode Icons**: Real PNG texture icons for each gamemode (from Tiers mod)
- 🖼️ **Custom Font System**: Uses Minecraft's bitmap font provider for crisp icons
- 🌍 **Multi-Region Support**: Displays player regions (NA, EU, AS/AU)
- 🎮 **Multiple Game Modes**: Supports Sword, Crystal, Netherite, Potion, Mace, UHC, Axe, and SMP rankings
- ⚡ **Smart Caching**: Reduces API calls with intelligent caching system (30 min default)
- 🎨 **Color-Coded Tiers**: Different colors for each tier level
- ⚙️ **Configurable**: Customize what information to display
- 📊 **Automatic Detection**: Displays highest tier across all gamemodes
- 🎨 **Resource Pack Compatible**: Icons can be customized via resource packs

## Requirements

- Minecraft 1.21.1 through 1.21.10 (All supported 1.21.x versions)
- Fabric Loader 0.16.0+
- Fabric API
- Java 21+

## Installation

1. Download the latest release from the [releases page](#)
2. Place the `.jar` file in your `.minecraft/mods` folder
3. Launch Minecraft with Fabric

## Configuration

The mod creates a config file at `.minecraft/config/volex-tiertagger.json`:

```json
{
  "enabled": true,
  "showRegion": true,
  "showGamemode": false,
  "cacheTimeMinutes": 30,
  "debugMode": false
}
```

### Config Options:

- **enabled**: Enable/disable the mod
- **showRegion**: Show player region (NA/EU/AS/AU)
- **showGamemode**: Show the gamemode for the displayed tier
- **cacheTimeMinutes**: How long to cache player data (default: 30 minutes)
- **debugMode**: Enable debug logging

## Tier Colors

- **Red**: Tier 1 (HT1/LT1) - Highest skill
- **Orange**: Tier 2 (HT2/LT2)
- **Gold**: Tier 3 (HT3/LT3)
- **Green**: Tier 4 (HT4/LT4)
- **Cyan**: Tier 5 (HT5/LT5)

## API Integration

This mod integrates with the Volex TierList API at `https://volextierlist-api-b2s8.vercel.app` to fetch real-time player rankings.

## Building from Source

```bash
./gradlew build
```

The built jar will be in `build/libs/`.

## License

MIT License - See [LICENSE](LICENSE) file for details.

## Credits

- Volex TierList Team
- [Tiers mod](https://github.com/Flavio6561/Tiers) - Gamemode texture icons
- [TierTagger](https://github.com/mctiers-dev/TierTagger) - Search UI functionality
- Fabric API
- All contributors

## Links

- [Volex TierList Website](https://volextierlist.vercel.app)
- [Discord](https://discord.gg/1-21-central-tierlist-1207931735485186130)
