package io.lama06.zombies.system.player;

import io.lama06.zombies.ZombiesPlugin;
import io.lama06.zombies.ZombiesWorld;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.time.Duration;

/**
 * Startet das Spiel automatisch, wenn der erste Spieler joint
 * und noch kein Spiel läuft. Kann in config deaktiviert werden.
 */
public final class AutoStartGameSystem implements Listener {
    private static final int DELAY_SECONDS = 8;
    private boolean scheduled;

    @EventHandler
    private void onJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final World bukkitWorld = player.getWorld();
        final ZombiesWorld world = new ZombiesWorld(bukkitWorld);

        if (world.isGameRunning()) {
            return;
        }
        if (scheduled) {
            return;
        }
        scheduled = true;

        player.sendMessage(Component.text("Spiel startet in " + DELAY_SECONDS + " Sekunden...")
                .color(NamedTextColor.GOLD));
        player.showTitle(Title.title(
                Component.text("ZOMBIES")
                        .color(NamedTextColor.RED)
                        .decorate(TextDecoration.BOLD),
                Component.text("Start in " + DELAY_SECONDS + "s")
                        .color(NamedTextColor.YELLOW),
                Title.Times.times(Duration.ofMillis(200), Duration.ofSeconds(3), Duration.ofMillis(300))
        ));

        Bukkit.getScheduler().runTaskLater(ZombiesPlugin.INSTANCE, () -> {
            scheduled = false;
            if (world.isGameRunning()) {
                return;
            }
            if (bukkitWorld.getPlayers().isEmpty()) {
                return;
            }
            try {
                // loadTemplate nur wenn noch nicht konfiguriert – Fehler ignorieren
                try {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "zombies loadTemplate");
                } catch (final Exception ignored) { }

                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "zombies start");
                for (final Player p : bukkitWorld.getPlayers()) {
                    p.sendMessage(Component.text("Spiel gestartet!")
                            .color(NamedTextColor.GREEN)
                            .decorate(TextDecoration.BOLD));
                }
            } catch (final Exception e) {
                ZombiesPlugin.INSTANCE.getLogger().warning("Auto-Start fehlgeschlagen: " + e.getMessage());
                for (final Player p : bukkitWorld.getPlayers()) {
                    p.sendMessage(Component.text("Auto-Start fehlgeschlagen. Nutze /zombies start")
                            .color(NamedTextColor.RED));
                }
            }
        }, DELAY_SECONDS * 20L);
    }
}
