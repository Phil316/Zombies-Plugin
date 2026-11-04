package io.lama06.zombies.system.zombie.explosion_attack;

import io.lama06.zombies.ZombiesPlayer;
import io.lama06.zombies.zombie.Zombie;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public final class ZombieExplosion {
    private static final int PARTICLE_COUNT = 3;

    public static void explode(final Zombie zombie, final double radius, final double damage) {
        final Entity entity = zombie.getEntity();
        final World worldBukkit = entity.getWorld();
        // Paper 1.20.4: EXPLOSION_NORMAL (nicht EXPLOSION)
        worldBukkit.spawnParticle(Particle.EXPLOSION_NORMAL, zombie.getEntity().getLocation(), PARTICLE_COUNT);
        for (final Player player : worldBukkit.getPlayers()) {
            if (player.getLocation().distance(entity.getLocation()) > radius) {
                continue;
            }
            final ZombiesPlayer zombiesPlayer = new ZombiesPlayer(player);
            if (!zombiesPlayer.isAlive()) {
                continue;
            }
            player.damage(damage, entity);
        }
    }

    private ZombieExplosion() { }
}
