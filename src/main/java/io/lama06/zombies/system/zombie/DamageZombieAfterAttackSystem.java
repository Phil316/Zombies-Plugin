package io.lama06.zombies.system.zombie;

import io.lama06.zombies.event.player.PlayerAttackZombieEvent;
import io.lama06.zombies.zombie.Zombie;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class DamageZombieAfterAttackSystem implements Listener {
    @EventHandler
    private void onPlayerAttackZombie(final PlayerAttackZombieEvent event) {
        final Zombie zombie = event.getZombie();
        if (!(zombie.getEntity() instanceof final LivingEntity living)) {
            return;
        }
        if (living.getHealth() - event.getDamage() <= 0) {
            living.setKiller(event.getPlayer().getBukkit());
            living.setHealth(0);
            return;
        }
        living.setNoDamageTicks(0);
        living.damage(event.getDamage(), event.getPlayer().getBukkit());
        if (event.isFreeze()) {
            living.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 7 * 20, 2));
        }
    }
}
