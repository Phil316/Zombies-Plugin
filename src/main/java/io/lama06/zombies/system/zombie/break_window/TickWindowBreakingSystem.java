package io.lama06.zombies.system.zombie.break_window;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import io.lama06.zombies.ZombiesPlayer;
import io.lama06.zombies.ZombiesPlugin;
import io.lama06.zombies.data.Component;
import io.lama06.zombies.event.zombie.BreakWindowTickEvent;
import io.lama06.zombies.zombie.BreakWindowData;
import io.lama06.zombies.zombie.Zombie;
import io.papermc.paper.math.BlockPosition;
import org.bukkit.Material;
import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

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
            block.toLocation(zombie.getWorld().getBukkit()).getBlock().setType(Material.AIR);
            // Nach Durchbruch: sofort Spieler anvisieren
            if (zombie.getEntity() instanceof final Mob mob) {
                final ZombiesPlayer nearest = zombie.getWorld().getAlivePlayers().stream()
                        .min(Comparator.comparingDouble(p ->
                                p.getBukkit().getLocation().distanceSquared(mob.getLocation())))
                        .orElse(null);
                if (nearest != null) {
                    mob.setTarget(nearest.getBukkit());
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
}
