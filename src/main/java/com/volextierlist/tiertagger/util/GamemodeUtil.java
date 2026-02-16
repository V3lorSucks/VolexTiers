package com.volextierlist.tiertagger.util;

public final class GamemodeUtil {
    private GamemodeUtil() {}

    public static String normalize(String gamemode) {
        return switch (gamemode.toLowerCase()) {
            case "sword", "swd" -> "Sword";
            case "crystal", "cpvp" -> "Crystal";
            case "netherite", "nethpot" -> "Netherite";
            case "pot", "potion" -> "Potion";
            case "mace", "macepvp" -> "Mace";
            case "uhc" -> "UHC";
            case "axe", "axepvp" -> "Axe";
            case "smp", "smpkit" -> "SMP";
            case "diasmp" -> "DiaSMP";
            default -> gamemode;
        };
    }
}
