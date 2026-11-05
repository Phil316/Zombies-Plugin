package io.lama06.zombies.system.zombie.break_window;

import io.lama06.zombies.event.zombie.BreakWindowTickEvent;
import io.lama06.zombies.zombie.Zombie;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class PlaySoundDuringWindowBreakingSystem implements Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onBreakWindowTick(final BreakWindowTickEvent event) {
        final Zombie zombie = event.getZombie();

        // Schlag-Animation alle 5 Ticks (0.25s)
        if (event.getNewRemainingTime() % 5 == 0
                && zombie.getEntity() instanceof final LivingEntity living) {
            living.swingMainHand();
        }

        // Sound etwas oefter als vorher
        final int SOUND_DELAY = 15;
        if (event.getNewRemainingTime() % SOUND_DELAY != 0) {
            return;
        }
        zombie.getWorld().getBukkit().playSound(
                zombie.getEntity(),
                Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR,
                SoundCategory.HOSTILE,
                1,
                0.9f + (float) (Math.random() * 0.2)
        );
    }
}
