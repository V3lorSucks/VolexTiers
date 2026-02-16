package com.volextierlist.tiertagger;

import com.volextierlist.tiertagger.cache.OverallCache;
import com.volextierlist.tiertagger.client.gui.PlayerSearchScreen;
import com.volextierlist.tiertagger.config.ModConfig;
import com.volextierlist.tiertagger.version.ModMenuSupport;
import com.volextierlist.tiertagger.version.VersionSupport;
import com.volextierlist.tiertagger.version.compat.CompatBridgeFactory;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class VolexTierTaggerClient implements ClientModInitializer {
    private static KeyBinding gamemodeKeybind;
    private static KeyBinding searchKeybind;
    
    @Override
    public void onInitializeClient() {
        VersionSupport.requireSupportedOrThrow();
        ModMenuSupport.requireCompatibleOrThrow();
        VolexTierTagger.init();

        // Initialize config
        ModConfig.init(FabricLoader.getInstance().getConfigDir());
        
        // Initialize overall cache (downloads /rankings/overall on startup)
        OverallCache.init(FabricLoader.getInstance().getConfigDir());
        
        // Register gamemode keybind
        gamemodeKeybind = KeyBindingHelper.registerKeyBinding(
            CompatBridgeFactory.client().createKeyBinding(
                "key.volex-tiertagger.cycle_gamemode",
                GLFW.GLFW_KEY_G,
                "category.volex-tiertagger.controls"
            )
        );
        
        // Register search keybind
        searchKeybind = KeyBindingHelper.registerKeyBinding(
            CompatBridgeFactory.client().createKeyBinding(
                "key.volex-tiertagger.search_player",
                GLFW.GLFW_KEY_Y,
                "category.volex-tiertagger.controls"
            )
        );
        
        // Register keybind handlers
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Gamemode cycle
            while (gamemodeKeybind.wasPressed()) {
                ModConfig.cycleGamemode();
                if (client.player != null) {
                    String gamemode = ModConfig.getSelectedGamemode();
                    String icon = getGamemodeIcon(gamemode);
                    String message = icon + " §7Current Gamemode: §f" + gamemode;
                    client.player.sendMessage(Text.literal(message), true);
                }
            }
            
            // Open search screen
            while (searchKeybind.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new PlayerSearchScreen(null));
                }
            }
        });
        
        VolexTierTagger.LOGGER.info("Volex TierTagger Client initialized!");
        VolexTierTagger.LOGGER.info("Nametag rendering enabled - tiers will show above players");
    }
    
    private static String getGamemodeIcon(String gamemode) {
        return switch (gamemode.toLowerCase()) {
            case "sword" -> "\uE801";
            case "crystal" -> "\uE800";
            case "netherite" -> "\uE803";
            case "potion" -> "\uE802";
            case "mace" -> "\uE807";
            case "uhc" -> "\uE804";
            case "axe" -> "\uE805";
            case "smp" -> "\uE806";
            case "diasmp" -> "\uE808";
            default -> "";
        };
    }
}
