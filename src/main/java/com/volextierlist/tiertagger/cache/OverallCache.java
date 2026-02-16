package com.volextierlist.tiertagger.cache;

import com.volextierlist.tiertagger.VolexTierTagger;
import com.volextierlist.tiertagger.api.TierListAPI;
import com.volextierlist.tiertagger.util.GamemodeUtil;
import com.google.gson.*;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.*;

public class OverallCache {
    private static final String API_URL = "https://volextl-api.vercel.app/api/players";
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    private static final long REFRESH_INTERVAL_MS = 60 * 60 * 1000; // 1 hour
    private static final long STALE_THRESHOLD_MS = 24 * 60 * 60 * 1000; // 24 hours
    private static final int MAX_RETRIES = 3;
    private static final long[] RETRY_DELAYS_MS = {5_000, 15_000, 30_000};

    private static volatile Map<String, TierListAPI.PlayerTierData> playerCache = new ConcurrentHashMap<>();
    private static Path cacheFilePath;
    private static ScheduledExecutorService scheduler;
    private static volatile boolean initialized = false;
    private static volatile long lastRefreshTime = 0;

    public static void init(Path configDir) {
        cacheFilePath = configDir.resolve("ctl-tiertagger").resolve("cache.json");

        loadFromDisk();

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Volex-TierTagger-CacheRefresh");
            t.setDaemon(true);
            return t;
        });

        scheduler.execute(OverallCache::refreshWithRetry);

        scheduler.scheduleAtFixedRate(
            OverallCache::refreshFromAPI,
            REFRESH_INTERVAL_MS,
            REFRESH_INTERVAL_MS,
            TimeUnit.MILLISECONDS
        );

        initialized = true;
        VolexTierTagger.LOGGER.info("OverallCache initialized with {} players from disk cache, API refresh started", playerCache.size());
    }

    public static TierListAPI.PlayerTierData getPlayer(String playerName) {
        if (playerName == null || playerName.isEmpty()) return null;
        return playerCache.get(playerName.toLowerCase());
    }

    public static boolean hasPlayer(String playerName) {
        if (playerName == null || playerName.isEmpty()) return false;
        return playerCache.containsKey(playerName.toLowerCase());
    }

    public static int getCacheSize() {
        return playerCache.size();
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static long getLastRefreshTime() {
        return lastRefreshTime;
    }

    private static void refreshWithRetry() {
        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            if (refreshFromAPIInternal()) return;
            if (attempt < MAX_RETRIES) {
                long delay = RETRY_DELAYS_MS[attempt];
                VolexTierTagger.LOGGER.warn("Cache refresh failed, retrying in {}s (attempt {}/{}", delay / 1000, attempt + 1, MAX_RETRIES);
                try { Thread.sleep(delay); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
            }
        }
        VolexTierTagger.LOGGER.error("Cache refresh failed after {} retries", MAX_RETRIES);
    }

    public static void refreshFromAPI() {
        refreshFromAPIInternal();
    }

    private static boolean refreshFromAPIInternal() {
        long startTime = System.currentTimeMillis();
        try {
            VolexTierTagger.LOGGER.info("Refreshing overall cache from API...");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .timeout(Duration.ofSeconds(60))
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String json = response.body();
                Map<String, TierListAPI.PlayerTierData> newCache = parseLeaderboard(json);
                if (newCache != null && !newCache.isEmpty()) {
                    playerCache = newCache; // atomic swap
                    saveToDisk(json);
                    lastRefreshTime = System.currentTimeMillis();
                    long elapsed = lastRefreshTime - startTime;
                    VolexTierTagger.LOGGER.info("Overall cache refreshed: {} players loaded in {}ms", newCache.size(), elapsed);
                    return true;
                }
                VolexTierTagger.LOGGER.warn("API returned 200 but parsed 0 players");
            } else {
                String body = response.body();
                String truncated = body.length() > 200 ? body.substring(0, 200) + "..." : body;
                VolexTierTagger.LOGGER.warn("Failed to fetch overall rankings: HTTP {} - {}", response.statusCode(), truncated);
            }
        } catch (Exception e) {
            VolexTierTagger.LOGGER.error("Error refreshing overall cache: {}", e.getMessage());
        }
        return false;
    }

    private static Map<String, TierListAPI.PlayerTierData> parseLeaderboard(String json) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            JsonArray leaderboard = root.getAsJsonArray("leaderboard");
            if (leaderboard == null) return null;

            Map<String, TierListAPI.PlayerTierData> newCache = new ConcurrentHashMap<>();

            for (JsonElement element : leaderboard) {
                JsonObject player = element.getAsJsonObject();

                String ingameName = player.get("ingameName").getAsString();
                String region = player.has("region") && !player.get("region").isJsonNull() ? player.get("region").getAsString() : "Unknown";
                int totalPoints = player.has("totalPoints") ? player.get("totalPoints").getAsInt() : 0;
                String title = player.has("title") ? player.get("title").getAsString() : "Rookie";
                int rank = player.has("rank") ? player.get("rank").getAsInt() : 0;

                TierListAPI.PlayerTierData tierData = new TierListAPI.PlayerTierData(
                    ingameName, region, "", "", totalPoints, title, rank
                );

                if (player.has("ranks") && player.get("ranks").isJsonObject()) {
                    JsonObject ranks = player.getAsJsonObject("ranks");
                    for (String gamemode : ranks.keySet()) {
                        JsonObject rankData = ranks.getAsJsonObject(gamemode);
                        if (rankData.has("rank")) {
                            String tierRank = rankData.get("rank").getAsString();
                            boolean retired = rankData.has("retired") && rankData.get("retired").getAsBoolean();
                            tierData.setTierForGamemode(GamemodeUtil.normalize(gamemode), tierRank, retired);
                        }
                    }
                }

                newCache.put(ingameName.toLowerCase(), tierData);
            }

            return newCache;
        } catch (Exception e) {
            VolexTierTagger.LOGGER.error("Error parsing leaderboard JSON: {}", e.getMessage());
            return null;
        }
    }

    private static void saveToDisk(String json) {
        try {
            Files.createDirectories(cacheFilePath.getParent());
            Files.writeString(cacheFilePath, json);
        } catch (Exception e) {
            VolexTierTagger.LOGGER.error("Failed to save cache to disk: {}", e.getMessage());
        }
    }

    private static void loadFromDisk() {
        try {
            if (Files.exists(cacheFilePath)) {
                long fileAge = System.currentTimeMillis() - Files.getLastModifiedTime(cacheFilePath).toMillis();
                String json = Files.readString(cacheFilePath);
                Map<String, TierListAPI.PlayerTierData> loaded = parseLeaderboard(json);
                if (loaded != null && !loaded.isEmpty()) {
                    playerCache = loaded;
                    if (fileAge > STALE_THRESHOLD_MS) {
                        VolexTierTagger.LOGGER.warn("Disk cache is {}h old, will refresh from API", fileAge / (60 * 60 * 1000));
                    }
                    VolexTierTagger.LOGGER.info("Loaded {} players from disk cache", loaded.size());
                }
            }
        } catch (Exception e) {
            VolexTierTagger.LOGGER.warn("Failed to load cache from disk: {}", e.getMessage());
        }
    }

    public static void shutdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    public static void forceRefresh() {
        CompletableFuture.runAsync(OverallCache::refreshWithRetry);
    }
}
