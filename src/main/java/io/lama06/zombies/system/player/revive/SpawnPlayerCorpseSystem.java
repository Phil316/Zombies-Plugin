package io.lama06.zombies.system.player.revive;

import io.lama06.zombies.ZombiesPlayer;
import io.lama06.zombies.ZombiesWorld;
import io.lama06.zombies.util.pdc.UuidDataType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.time.Duration;

public final class SpawnPlayerCorpseSystem implements Listener {
    @EventHandler
    private void onPlayerDeath(final PlayerDeathEvent event) {
        final ZombiesPlayer player = new ZombiesPlayer(event.getPlayer());
        final ZombiesWorld world = player.getWorld();
        if (!world.isGameRunning()) {
            return;
        }

        final int seconds = PlayerCorpse.TIME / 20;
        world.sendMessage(
                Component.text(seconds + " Sekunden übrig, um ")
                        .append(player.getBukkit().displayName())
                        .append(Component.text(" wiederzubeleben"))
                        .color(NamedTextColor.RED)
        );

        // Team sieht großen Titel in der Mitte
        for (final Player online : world.getBukkit().getPlayers()) {
            if (online.equals(player.getBukkit())) {
                continue;
            }
            online.showTitle(Title.title(
                    Component.text("TEAMMITGLIED GESTORBEN")
                            .color(NamedTextColor.RED)
                            .decorate(TextDecoration.BOLD),
                    player.getBukkit().displayName()
                            .append(Component.text(" – " + seconds + "s zum Wiederbeleben").color(NamedTextColor.GRAY)),
                    Title.Times.times(
                            Duration.ofMillis(200),
                            Duration.ofSeconds(2),
                            Duration.ofMillis(400)
                    )
            ));
        }

        final ArmorStand corpse = world.getBukkit().spawn(player.getBukkit().getLocation(), ArmorStand.class);
        corpse.setCanMove(false);
        final EntityEquipment corpseEquipment = corpse.getEquipment();
        final ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        final SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        headMeta.setOwningPlayer(player.getBukkit());
        head.setItemMeta(headMeta);
        corpseEquipment.setHelmet(head);
        corpseEquipment.setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
        corpseEquipment.setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
        corpseEquipment.setBoots(new ItemStack(Material.LEATHER_BOOTS));
        final PersistentDataContainer corpsPdc = corpse.getPersistentDataContainer();
        corpsPdc.set(PlayerCorpse.getPlayerKey(), UuidDataType.INSTANCE, player.getBukkit().getUniqueId());
        corpsPdc.set(PlayerCorpse.getRemainingTimeKey(), PersistentDataType.INTEGER, PlayerCorpse.TIME);
        corpsPdc.set(PlayerCorpse.getReliveTimeKey(), PersistentDataType.INTEGER, PlayerCorpse.RELIVE_TIME);
    }
}
