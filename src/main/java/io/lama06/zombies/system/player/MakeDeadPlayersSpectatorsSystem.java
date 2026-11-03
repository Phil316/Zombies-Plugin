package io.lama06.zombies.system.player;

import io.lama06.zombies.ZombiesWorld;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.time.Duration;

public final class MakeDeadPlayersSpectatorsSystem implements Listener {
    @EventHandler
    private void onPlayerDies(final PlayerDeathEvent event) {
        final Player bukkitPlayer = event.getPlayer();
        final ZombiesWorld world = new ZombiesWorld(bukkitPlayer.getWorld());
        if (!world.isGameRunning()) {
            return;
        }

        event.setKeepInventory(true);
        event.setShouldDropExperience(false);
        event.setShowDeathScreen(false);
        event.deathMessage(
                bukkitPlayer.displayName()
                        .append(Component.text(" ist gestorben").color(NamedTextColor.RED))
        );

        bukkitPlayer.setGameMode(GameMode.SPECTATOR);

        // Hypixel-Style großer Text in der Mitte
        bukkitPlayer.showTitle(Title.title(
                Component.text("DU BIST GESTORBEN")
                        .color(NamedTextColor.RED)
                        .decorate(TextDecoration.BOLD),
                Component.text("Warte auf Wiederbelebung...")
                        .color(NamedTextColor.GRAY),
                Title.Times.times(
                        Duration.ofMillis(250),
                        Duration.ofSeconds(4),
                        Duration.ofMillis(500)
                )
        ));
    }
}
