package io.lama06.zombies;

/**
 * Skaliert Zombie-Anzahl und Leben nach Spielerzahl der aktuellen Runde.
 * 1 Spieler = Basis, mehr Spieler = mehr Zombies / etwas mehr HP.
 * Wird zu Beginn jeder Runde neu berechnet.
 */
public final class PlayerCountScale {
    private PlayerCountScale() {}

    public static int countActivePlayers(final ZombiesWorld world) {
        final int alive = world.getAlivePlayers().size();
        if (alive > 0) {
            return alive;
        }
        return Math.max(1, world.getPlayers().size());
    }

    /**
     * Anzahl-Multiplikator: 1p=1.0, 2p=1.5, 3p=2.0, 4p=2.5
     */
    public static double countMultiplier(final int players) {
        final int p = Math.max(1, Math.min(4, players));
        return 0.5 + 0.5 * p;
    }

    /**
     * Leben-Multiplikator: mild, 1p=1.0 … 4p=1.36
     */
    public static double healthMultiplier(final int players) {
        final int p = Math.max(1, Math.min(4, players));
        return 1.0 + 0.12 * (p - 1);
    }

    public static int scaleZombieCount(final int base, final int players, final GameDifficulty difficulty) {
        final int afterDiff = difficulty.scaleCount(base);
        return Math.max(1, (int) Math.round(afterDiff * countMultiplier(players)));
    }

    public static double scaleHealth(final double base, final int players, final GameDifficulty difficulty) {
        return Math.max(1.0, difficulty.scaleHealth(base) * healthMultiplier(players));
    }
}
