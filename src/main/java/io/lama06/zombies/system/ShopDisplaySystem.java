package io.lama06.zombies.system;

import io.lama06.zombies.ArmorShop;
import io.lama06.zombies.Door;
import io.lama06.zombies.WeaponShop;
import io.lama06.zombies.WorldConfig;
import io.lama06.zombies.ZombiesWorld;
import io.lama06.zombies.perk.PerkMachine;
import io.lama06.zombies.util.HologramUtil;
import io.papermc.paper.math.BlockPosition;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Schwebende Hologramme wie bei Hypixel:
 * Tuer: Zielgebiet + Gold
 * Shop/Perk: Name + Preis (+ Strom-Hinweis)
 */
public final class ShopDisplaySystem {
    private ShopDisplaySystem() { }

    public static void refreshAll(final ZombiesWorld zombiesWorld) {
        final WorldConfig config = zombiesWorld.getConfig();
        if (config == null) {
            return;
        }
        final World world = zombiesWorld.getBukkit();
        final Boolean powerAttr = zombiesWorld.get(ZombiesWorld.POWER_SWITCH);
        final boolean powerOn = Boolean.TRUE.equals(powerAttr);

        HologramUtil.removeAll(world);

        // Tueren: nur Ziel-Gebiet + Preis (kein Weg-Text)
        final List<String> reachable = zombiesWorld.get(ZombiesWorld.REACHABLE_AREAS);
        final List<String> areas = reachable != null ? reachable : List.of();
        for (final Door door : config.doors) {
            if (door.position == null) {
                continue;
            }
            final String target;
            if (areas.contains(door.area1) && !areas.contains(door.area2)) {
                target = door.area2;
            } else if (areas.contains(door.area2) && !areas.contains(door.area1)) {
                target = door.area1;
            } else {
                // Fallback: zweites Gebiet
                target = door.area2 != null && !door.area2.isEmpty() ? door.area2 : door.area1;
            }
            final Location mid = doorCenter(world, door);
            mid.add(0.5, 1.4, 0.5);
            HologramUtil.spawnLines(mid, List.of(
                    Component.text(target).color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD),
                    Component.text(door.gold + " Gold").color(NamedTextColor.GOLD)
            ));
        }

        // Waffen-Shops
        for (final WeaponShop shop : config.weaponShops) {
            if (shop.position == null || shop.weaponType == null) {
                continue;
            }
            final Location loc = shop.position.toLocation(world).add(0.5, 1.3, 0.5);
            HologramUtil.spawnLines(loc, List.of(
                    shop.weaponType.getDisplayName().color(NamedTextColor.WHITE),
                    Component.text(shop.purchasePrice + " Gold").color(NamedTextColor.GOLD)
            ));
        }

        // Ruestung
        for (final ArmorShop shop : config.armorShops) {
            if (shop.position == null || shop.quality == null || shop.part == null) {
                continue;
            }
            final Location loc = shop.position.toLocation(world).add(0.5, 1.3, 0.5);
            HologramUtil.spawnLines(loc, List.of(
                    shop.quality.getDisplayName().append(Component.text(" ")).append(shop.part.getDisplayName()),
                    Component.text(shop.price + " Gold").color(NamedTextColor.GOLD)
            ));
        }

        // Perk-Maschinen
        for (final PerkMachine machine : config.perkMachines) {
            if (machine.position == null || machine.perk == null) {
                continue;
            }
            final Location loc = machine.position.toLocation(world).add(0.5, 1.5, 0.5);
            final List<Component> lines = new ArrayList<>();
            lines.add(machine.perk.getDisplayName().color(NamedTextColor.LIGHT_PURPLE));
            if (powerOn) {
                lines.add(Component.text(machine.gold + " Gold").color(NamedTextColor.GOLD));
            } else {
                lines.add(Component.text("BRAUCHT STROM").color(NamedTextColor.RED).decorate(TextDecoration.BOLD));
                lines.add(Component.text(machine.gold + " Gold").color(NamedTextColor.DARK_GRAY));
            }
            HologramUtil.spawnLines(loc, lines);
        }

        // Power Switch
        if (config.powerSwitch != null && config.powerSwitch.position != null) {
            final Location loc = config.powerSwitch.position.toLocation(world).add(0.5, 1.4, 0.5);
            if (powerOn) {
                HologramUtil.spawnLines(loc, List.of(
                        Component.text("STROM").color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD),
                        Component.text("AKTIV").color(NamedTextColor.GREEN)
                ));
            } else {
                HologramUtil.spawnLines(loc, List.of(
                        Component.text("STROM").color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD),
                        Component.text(config.powerSwitch.gold + " Gold").color(NamedTextColor.GOLD)
                ));
            }
        }
    }

    private static Location doorCenter(final World world, final Door door) {
        final BlockPosition p1 = door.position.position1();
        final BlockPosition p2 = door.position.position2();
        final double x = (p1.blockX() + p2.blockX()) / 2.0;
        final double y = Math.max(p1.blockY(), p2.blockY());
        final double z = (p1.blockZ() + p2.blockZ()) / 2.0;
        return new Location(world, x, y, z);
    }
}
