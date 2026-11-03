package io.lama06.zombies.system.player.revive;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import io.lama06.zombies.ZombiesPlayer;
import io.lama06.zombies.ZombiesPlugin;
import io.lama06.zombies.ZombiesWorld;
import io.lama06.zombies.perk.PlayerPerk;
import io.lama06.zombies.util.pdc.UuidDataType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public final class TickPlayerCorpseSystem implements Listener {
    private static final double REVIVE_RADIUS = 4;

    @EventHandler
    private void onTick(final ServerTickEndEvent event) {
        for (final ZombiesWorld world : ZombiesPlugin.INSTANCE.getWorlds()) {
            final Collection<ArmorStand> armorStands = world.getBukkit().getEntitiesByClass(ArmorStand.class);
            for (final ArmorStand corpse : armorStands) {
                final PersistentDataContainer pdc = corpse.getPersistentDataContainer();
                final UUID playerUuid = pdc.get(PlayerCorpse.getPlayerKey(), UuidDataType.INSTANCE);
                if (playerUuid == null) {
                    continue;
                }
                final Player corpsePlayer = Bukkit.getPlayer(playerUuid);
                if (!world.isGameRunning() || corpsePlayer == null || !corpsePlayer.getWorld().equals(world.getBukkit())) {
                    corpse.remove();
                    continue;
                }
                final Integer remainingTime = pdc.get(PlayerCorpse.getRemainingTimeKey(), PersistentDataType.INTEGER);
                final Integer reliveTime = pdc.get(PlayerCorpse.getReliveTimeKey(), PersistentDataType.INTEGER);
                if (remainingTime == null || remainingTime <= 0 || reliveTime == null) {
                    // Timer abgelaufen → bleibt tot bis nächste Runde
                    if (remainingTime != null && remainingTime <= 0 && corpsePlayer.getGameMode() == GameMode.SPECTATOR) {
                        corpsePlayer.showTitle(Title.title(
                                Component.text("ZEIT ABGELAUFEN")
                                        .color(NamedTextColor.DARK_RED)
                                        .decorate(TextDecoration.BOLD),
                                Component.text("Du wirst in der nächsten Runde wiederbelebt")
                                        .color(NamedTextColor.GRAY),
                                Title.Times.times(
                                        Duration.ofMillis(200),
                                        Duration.ofSeconds(3),
                                        Duration.ofMillis(500)
                                )
                        ));
                    }
                    corpse.remove();
                    continue;
                }
                if (reliveTime <= 0) {
                    corpse.remove();
                    corpsePlayer.setGameMode(GameMode.ADVENTURE);
                    corpsePlayer.teleport(corpse.getLocation());
                    corpsePlayer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 6 * 20, 2));
                    world.sendMessage(
                            corpsePlayer.displayName()
                                    .append(Component.text(" wurde wiederbelebt").color(NamedTextColor.GREEN))
                    );
                    corpsePlayer.showTitle(Title.title(
                            Component.text("WIEDERBELEBT")
                                    .color(NamedTextColor.GREEN)
                                    .decorate(TextDecoration.BOLD),
                            Component.text("Zurück im Kampf!")
                                    .color(NamedTextColor.YELLOW),
                            Title.Times.times(
                                    Duration.ofMillis(150),
                                    Duration.ofSeconds(2),
                                    Duration.ofMillis(300)
                            )
                    ));
                    continue;
                }

                // Toter Spieler: Countdown als Actionbar
                final float secondsLeft = remainingTime / 20f;
                corpsePlayer.sendActionBar(
                        Component.text("Wiederbelebung: ")
                                .color(NamedTextColor.RED)
                                .append(Component.text(String.format("%.1fs", secondsLeft)).color(NamedTextColor.YELLOW))
                                .append(Component.text("  |  Sonst nächste Runde").color(NamedTextColor.GRAY))
                );

                final List<Player> nearbyPlayers = corpse.getNearbyEntities(REVIVE_RADIUS, REVIVE_RADIUS, REVIVE_RADIUS)
                        .stream().filter(e -> e instanceof Player).map(e -> (Player) e).toList();
                boolean reviving = false;
                for (final Player nearbyPlayer : nearbyPlayers) {
                    if (nearbyPlayer.equals(corpsePlayer)) {
                        continue;
                    }
                    nearbyPlayer.sendActionBar(
                            Component.text("Sneaken zum Wiederbeleben: ")
                                    .color(NamedTextColor.YELLOW)
                                    .append(Component.text(String.format("%.1fs", reliveTime / 20f)).color(NamedTextColor.GOLD))
                    );
                    if (nearbyPlayer.isSneaking()) {
                        reviving = true;
                        final int reviveSpeed = new ZombiesPlayer(nearbyPlayer).hasPerk(PlayerPerk.FAST_REVIVE) ? 3 : 1;
                        pdc.set(PlayerCorpse.getReliveTimeKey(), PersistentDataType.INTEGER, reliveTime - reviveSpeed);
                    }
                }
                if (!reviving) {
                    pdc.set(PlayerCorpse.getRemainingTimeKey(), PersistentDataType.INTEGER, remainingTime - 1);
                }
            }
        }
    }
}
