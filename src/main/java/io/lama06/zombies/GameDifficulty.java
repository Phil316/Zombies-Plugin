package io.lama06.zombies;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Einfache Schwierigkeit (nicht 1:1 Hypixel RIP).
 * Beeinflusst Zombie-Anzahl und Leben.
 */
public enum GameDifficulty {
    LEICHT(0.75, 0.85, Component.text("Leicht").color(NamedTextColor.GREEN)),
    NORMAL(1.0, 1.0, Component.text("Normal").color(NamedTextColor.YELLOW)),
    SCHWER(1.35, 1.4, Component.text("Schwer").color(NamedTextColor.RED));

    private final double countMultiplier;
    private final double healthMultiplier;
    private final Component displayName;

    GameDifficulty(final double countMultiplier, final double healthMultiplier, final Component displayName) {
        this.countMultiplier = countMultiplier;
        this.healthMultiplier = healthMultiplier;
        this.displayName = displayName;
    }

    public double getCountMultiplier() {
        return countMultiplier;
    }

    public double getHealthMultiplier() {
        return healthMultiplier;
    }

    public Component getDisplayName() {
        return displayName;
    }

    public int scaleCount(final int base) {
        return Math.max(1, (int) Math.round(base * countMultiplier));
    }

    public double scaleHealth(final double base) {
        return Math.max(1.0, base * healthMultiplier);
    }

    public static GameDifficulty fromString(final String raw) {
        if (raw == null) {
            return NORMAL;
        }
        return switch (raw.toLowerCase()) {
            case "leicht", "easy", "l" -> LEICHT;
            case "schwer", "hard", "s", "h" -> SCHWER;
            default -> NORMAL;
        };
    }
}
