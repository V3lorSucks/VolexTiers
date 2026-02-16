package com.volextierlist.tiertagger.client.render;

import com.volextierlist.tiertagger.VolexTierTagger;
import com.volextierlist.tiertagger.api.TierListAPI;
import com.volextierlist.tiertagger.cache.TierCache;
import com.volextierlist.tiertagger.config.ModConfig;
import com.volextierlist.tiertagger.version.compat.CompatBridgeFactory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.joml.Matrix4f;

public class TierHudRenderer {
    
    private static final MinecraftClient client = MinecraftClient.getInstance();
    
    /**
     * Render tier label above player's nametag
     * Called from renderLabelIfPresent, so positioning is already done
     */
    public static void renderTierAboveNametag(PlayerEntity player, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        VolexTierTagger.LOGGER.info("[DEBUG] renderTierAboveNametag called for player: {}", player.getName().getString());
        
        if (!ModConfig.isEnabled()) {
            VolexTierTagger.LOGGER.info("[DEBUG] Mod is disabled, skipping render");
            return;
        }
        
        // Don't render for local player
        if (player == client.player) {
            VolexTierTagger.LOGGER.info("[DEBUG] Player is local player, skipping render");
            return;
        }
        
        String playerName = player.getName().getString();
        VolexTierTagger.LOGGER.info("[DEBUG] Fetching tier data for: {}", playerName);
        
        // Fetch tier data if not cached
        TierListAPI.PlayerTierData tierData = TierCache.getTierData(playerName);
        
        if (tierData == null) {
            VolexTierTagger.LOGGER.info("[DEBUG] No tier data found for: {}", playerName);
            return;
        }
        
        // Determine which tier to show based on config
        String displayTier;
        String displayGamemode;
        boolean isRetired;
        
        if (ModConfig.shouldShowHighestTier()) {
            // Show highest tier across all gamemodes
            displayTier = tierData.getHighestTier();
            displayGamemode = tierData.getHighestTierGamemode();
            
            if (displayTier.equals("Unranked") || displayGamemode == null) {
                VolexTierTagger.LOGGER.info("[DEBUG] Player {} is unranked, skipping render", playerName);
                return;
            }
            
            isRetired = tierData.isRetired(displayGamemode);
            VolexTierTagger.LOGGER.info("[DEBUG] Showing highest tier: tier={}, gamemode={}, region={}, retired={}", displayTier, displayGamemode, tierData.region, isRetired);
        } else {
            // Filter by selected gamemode
            String selectedGamemode = ModConfig.getSelectedGamemode();
            
            if (!tierData.hasTierForGamemode(selectedGamemode)) {
                VolexTierTagger.LOGGER.info("[DEBUG] Player {} has no tier in gamemode {}, skipping render", playerName, selectedGamemode);
                return;
            }
            
            displayTier = tierData.getTierForGamemode(selectedGamemode);
            displayGamemode = selectedGamemode;
            isRetired = tierData.isRetired(selectedGamemode);
            
            VolexTierTagger.LOGGER.info("[DEBUG] Showing selected gamemode tier: tier={}, gamemode={}, region={}, retired={}", displayTier, displayGamemode, tierData.region, isRetired);
        }
        
        // Add R prefix for retired tiers (e.g., "RHT3")
        if (isRetired) {
            displayTier = "R" + displayTier;
        }
        
        // Build tier text in TierTagger format: [ICON] TIER
        VolexTierTagger.LOGGER.info("[DEBUG] Building tier text, showGamemode={}", 
            ModConfig.shouldShowGamemode());
        
        Text text = Text.empty();
        
        // Add gamemode icon if enabled
        if (ModConfig.shouldShowGamemode()) {
            String icon = getGamemodeIcon(displayGamemode);
            VolexTierTagger.LOGGER.info("[DEBUG] Gamemode icon for {}: '{}'", displayGamemode, icon);
            if (!icon.isEmpty()) {
                text = Text.literal(icon + " ");
            }
        }
        
        // Add tier with color from config
        int tierColor = ModConfig.getTierColor(displayTier);
        text = text.copy().append(Text.literal(displayTier).styled(s -> s.withColor(tierColor)));
        
        VolexTierTagger.LOGGER.info("[DEBUG] Final tier text built with color: 0x{}", Integer.toHexString(tierColor));
        TextRenderer textRenderer = client.textRenderer;
        
        if (textRenderer == null) {
            VolexTierTagger.LOGGER.warn("[DEBUG] TextRenderer is null!");
            return;
        }
        
        VolexTierTagger.LOGGER.info("[DEBUG] Starting render, text width: {}", textRenderer.getWidth(text));
        
        matrices.push();
        
        // Move up above the nametag (nametag is at y=0 in this context)
        matrices.translate(0.0, -10.0, 0.0);
        
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        
        // Center the text
        float x = -textRenderer.getWidth(text) / 2.0f;
        
        // Render with background
        int backgroundColor = (int)(0.25F * 255.0F) << 24;
        
        VolexTierTagger.LOGGER.info("[DEBUG] Calling compatibility draw path with x={}, light={}", x, light);

        CompatBridgeFactory.client().drawSeeThroughText(
            textRenderer,
            text,
            x,
            matrix4f,
            vertexConsumers,
            backgroundColor,
            light
        );

        matrices.pop();
        
        VolexTierTagger.LOGGER.info("[DEBUG] Render complete for {}", playerName);
    }
    

    /**
     * Get gamemode icon (using custom font characters)
     * These map to textures defined in assets/minecraft/font/default.json
     */
    private static String getGamemodeIcon(String gamemode) {
        return switch (gamemode.toLowerCase()) {
            case "sword" -> "\uE801"; // sword.png
            case "cpvp", "crystal" -> "\uE800"; // vanilla.png (crystal)
            case "netherite", "nethpot" -> "\uE803"; // neth_op.png
            case "pot", "potion" -> "\uE802"; // pot.png
            case "mace", "macepvp" -> "\uE807"; // mace.png
            case "uhc" -> "\uE804"; // uhc.png
            case "axe", "axepvp" -> "\uE805"; // axe.png
            case "smp", "smpkit" -> "\uE806"; // smp.png
            default -> "";
        };
    }
    

}
