package io.lama06.zombies.system.zombie;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import io.lama06.zombies.ZombiesPlayer;
import io.lama06.zombies.ZombiesPlugin;
import io.lama06.zombies.event.zombie.ZombieSpawnEvent;
import io.lama06.zombies.zombie.Zombie;
import org.bukkit.DyeColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Mob;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Comparator;

/**
 * Hypixel-aehnlich: Zombies laufen immer zum naechsten Spieler
 * (nach dem Fenster-Durchbruch / sobald sie gespawnt sind).
 */
public final class AngerZombiesSystem implements Listener {
    private static final double FOLLOW_RANGE = 64.0;

    @EventHandler
    private void onZombieSpawn(final ZombieSpawnEvent event) {
        final Zombie zombie = event.getZombie();
        setupAttributes(zombie);
        retarget(zombie);
    }

    /** Alle 10 Ticks Ziele aktualisieren – verhindert zielloses Herumlaufen. */
    @EventHandler
    private void onTick(final ServerTickEndEvent event) {
        if (event.getTickNumber() % 10 != 0) {
            return;
        }
        for (final Zombie zombie : ZombiesPlugin.INSTANCE.getZombies()) {
            retarget(zombie);
        }
    }

    private void setupAttributes(final Zombie zombie) {
        if (!(zombie.getEntity() instanceof final LivingEntity living)) {
            return;
        }
        final AttributeInstance follow = living.getAttribute(Attribute.GENERIC_FOLLOW_RANGE);
        if (follow != null) {
            follow.setBaseValue(FOLLOW_RANGE);
        }
        // Nicht despawnen
        living.setRemoveWhenFarAway(false);
        if (living instanceof final Mob mob) {
            mob.setAware(true);
        }
    }

    private void retarget(final Zombie zombie) {
        if (!(zombie.getEntity() instanceof final Mob mob)) {
            return;
        }
        // Waehrend Fenster-Brechen nicht umleiten
        if (zombie.getComponent(Zombie.BREAK_WINDOW) != null
                && zombie.getComponent(Zombie.BREAK_WINDOW).get(io.lama06.zombies.zombie.BreakWindowData.REMAINING_TIME) != null) {
            return;
        }
        final ZombiesPlayer nearest = getNearestPlayer(zombie);
        if (nearest == null) {
            return;
        }
        // Nur setzen wenn kein gueltiges Ziel oder Ziel tot/spectator
        if (mob.getTarget() == null
                || !(mob.getTarget() instanceof org.bukkit.entity.Player)
                || mob.getTarget().isDead()
                || !nearest.getBukkit().equals(mob.getTarget())) {
            mob.setTarget(nearest.getBukkit());
        }
        if (mob instanceof final PigZombie pigZombie) {
            pigZombie.setAngry(true);
        } else if (mob instanceof final Wolf wolf) {
            wolf.setAngry(true);
            wolf.setCollarColor(DyeColor.RED);
        }
    }

    private ZombiesPlayer getNearestPlayer(final Zombie zombie) {
        return zombie.getWorld().getAlivePlayers().stream()
                .min(Comparator.comparingDouble(player ->
                        player.getBukkit().getLocation().distanceSquared(zombie.getEntity().getLocation())))
                .orElse(null);
    }
}
