package io.lama06.zombies.system;

import io.lama06.zombies.WorldConfig;
import io.lama06.zombies.ZombiesPlayer;
import io.lama06.zombies.ZombiesWorld;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.time.Duration;

public final class EnablePowerSwitchSystem implements Listener {
    @EventHandler
    private void onPlayerInteract(final PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) {
            return;
        }
        final Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }
        final ZombiesPlayer player = new ZombiesPlayer(event.getPlayer());
        final ZombiesWorld world = player.getWorld();
        if (!world.isGameRunning()) {
            return;
        }
        final WorldConfig config = world.getConfig();
        if (config.powerSwitch == null) {
            return;
        }
        if (!clickedBlock.getLocation().toBlock().equals(config.powerSwitch.position)) {
            return;
        }
        final boolean powerSwitchOn = world.get(ZombiesWorld.POWER_SWITCH);
        if (powerSwitchOn) {
            player.sendMessage(Component.text("Strom ist bereits an.").color(NamedTextColor.RED));
            return;
        }
        if (!player.requireGold(config.powerSwitch.gold)) {
            return;
        }
        player.pay(config.powerSwitch.gold);
        world.set(ZombiesWorld.POWER_SWITCH, true);
        world.showTitle(Title.title(
                Component.text("STROM AN").color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD),
                Component.text(player.getBukkit().getName() + " hat den Power Switch aktiviert").color(NamedTextColor.GRAY),
                Title.Times.times(Duration.ofMillis(200), Duration.ofSeconds(3), Duration.ofMillis(400))
        ));
        // Perk-Schilder auf Preise umstellen
        ShopDisplaySystem.refreshAll(world);
    }
}
