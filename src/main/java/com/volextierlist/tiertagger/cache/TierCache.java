package com.volextierlist.tiertagger.cache;

import com.volextierlist.tiertagger.api.TierListAPI;
import com.volextierlist.tiertagger.cache.OverallCache;

public class TierCache {

    /**
     * Get tier data for a player from OverallCache only.
     * No fallback API calls - only ranked players will show tiers.
     */
    public static TierListAPI.PlayerTierData getTierData(String playerName) {
        if (playerName == null || playerName.isEmpty()) {
            return null;
        }
        return OverallCache.getPlayer(playerName);
    }

    /**
     * Get cache size
     */
    public static int getCacheSize() {
        return OverallCache.getCacheSize();
    }
}
