package io.lama06.zombies.system.zombie.break_window;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import io.lama06.zombies.Window;
import io.lama06.zombies.ZombiesPlayer;
import io.lama06.zombies.ZombiesPlugin;
import io.lama06.zombies.data.Component;
import io.lama06.zombies.event.zombie.BreakWindowTickEvent;
import io.lama06.zombies.zombie.BreakWindowData;
import io.lama06.zombies.zombie.Zombie;
import io.papermc.paper.math.BlockPosition;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import java.util.Comparator;

public final class TickWindowBreakingSystem implements Listener {
    @EventHandler
    private void onServerTick(final ServerTickEndEvent event) {
        for (final Zombie zombie : ZombiesPlugin.INSTANCE.getZombies()) {
            tickZombie(zombie);
        }
    }

    private void tickZombie(final Zombie zombie) {
        final Component breakWindowComponent = zombie.getComponent(Zombie.BREAK_WINDOW);
        if (breakWindowComponent == null) {
            return;
        }
        final Integer remainingTime = breakWindowComponent.get(BreakWindowData.REMAINING_TIME);
        final BlockPosition block = breakWindowComponent.get(BreakWindowData.BLOCK);
        if (remainingTime == null || block == null) {
            return;
        }
        if (remainingTime == 1) {
            breakWindowComponent.remove(BreakWindowData.REMAINING_TIME);
            breakWindowComponent.remove(BreakWindowData.BLOCK);
            final Block bukkitBlock = block.toLocation(zombie.getWorld().getBukkit()).getBlock();
            bukkitBlock.setType(Material.AIR);

            // Wenn das Fenster komplett offen ist: Zombie durchhelfen (Slabs)
            helpThroughWindow(zombie, block);

            if (zombie.getEntity() instanceof final Mob mob) {
                final ZombiesPlayer nearest = zombie.getWorld().getAlivePlayers().stream()
                        .min(Comparator.comparingDouble(p ->
                                p.getBukkit().getLocation().distanceSquared(mob.getLocation())))
                        .orElse(null);
                if (nearest != null) {
                    mob.setTarget(nearest.getBukkit());
                    // leichter Schubs Richtung Spieler (ueber Slab-Kante)
                    final Vector dir = nearest.getBukkit().getLocation().toVector()
                            .subtract(mob.getLocation().toVector())
                            .setY(0.25)
                            .normalize()
                            .multiply(0.35);
                    mob.setVelocity(dir);
                }
            }
            return;
        }
        breakWindowComponent.set(BreakWindowData.REMAINING_TIME, remainingTime - 1);
        if (!new BreakWindowTickEvent(zombie, remainingTime - 1).callEvent()) {
            breakWindowComponent.remove(BreakWindowData.REMAINING_TIME);
            breakWindowComponent.remove(BreakWindowData.BLOCK);
        }
    }

    /** Wenn alle Bretter weg sind: restliche Slabs im Fenster weg + leichte Teleportation nach vorne. */
    private void helpThroughWindow(final Zombie zombie, final BlockPosition broken) {
        final var world = zombie.getWorld();
        Window matched = null;
        for (final Window window : world.getConfig().windows) {
            if (window.blocks == null) {
                continue;
            }
            for (final BlockPosition bp : window.blocks.getBlocks()) {
                if (bp.blockX() == broken.blockX() && bp.blockY() == broken.blockY() && bp.blockZ() == broken.blockZ()) {
                    matched = window;
                    break;
                }
            }
            if (matched != null) {
                break;
            }
        }
        if (matched == null) {
            return;
        }
        boolean anyLeft = false;
        for (final BlockPosition bp : matched.blocks.getBlocks()) {
            final Block b = bp.toLocation(world.getBukkit()).getBlock();
            if (b.getType() != Material.AIR) {
                anyLeft = true;
                // Nachbar-Slabs mit entfernen wenn Zombie nah genug (verhindert Haengenbleiben)
                if (zombie.getEntity().getLocation().distance(b.getLocation().add(0.5, 0.5, 0.5)) < 2.2) {
                    b.setType(Material.AIR);
                }
            }
        }
        // Wenn Fenster frei: kurz auf die Spieler-Seite schieben
        if (!anyLeft && zombie.getEntity() instanceof final Mob mob) {
            final ZombiesPlayer nearest = world.getAlivePlayers().stream()
                    .min(Comparator.comparingDouble(p ->
                            p.getBukkit().getLocation().distanceSquared(mob.getLocation())))
                    .orElse(null);
            if (nearest != null) {
                final Location from = mob.getLocation();
                final Location toPlayer = nearest.getBukkit().getLocation();
                final Vector push = toPlayer.toVector().subtract(from.toVector()).normalize().multiply(0.8);
                final Location dest = from.clone().add(push);
                dest.setY(from.getY());
                // nur wenn Ziel frei
                if (dest.getBlock().getType().isAir() && dest.clone().add(0, 1, 0).getBlock().getType().isAir()) {
                    mob.teleport(dest);
                }
            }
        }
    }
}
