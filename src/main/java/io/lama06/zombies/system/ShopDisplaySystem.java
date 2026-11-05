package io.lama06.zombies.system;

import io.lama06.zombies.ArmorShop;
import io.lama06.zombies.Door;
import io.lama06.zombies.WeaponShop;
import io.lama06.zombies.WorldConfig;
import io.lama06.zombies.ZombiesWorld;
import io.lama06.zombies.perk.PerkMachine;
import io.lama06.zombies.util.HologramUtil;
import io.lama06.zombies.util.PositionUtil;
import io.papermc.paper.math.BlockPosition;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.Switch;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Setzt Schilder + schwebende Tuer-Hologramme (wie Hypixel-Nametags).
 * Perk-Maschinen: ohne Strom "BRAUCHT STROM", mit Strom Preis.
 */
public final class ShopDisplaySystem {
    private ShopDisplaySystem() { }

    public static void refreshAll(final ZombiesWorld zombiesWorld) {
        final WorldConfig config = zombiesWorld.getConfig();
        if (config == null) {
            return;
        }
        final World world = zombiesWorld.getBukkit();
        final boolean powerOn = Boolean.TRUE.equals(zombiesWorld.get(ZombiesWorld.POWER_SWITCH));

        HologramUtil.removeAll(world);

        // Tuer-Hologramme (fliegende Nametags)
        for (final Door door : config.doors) {
            if (door.position == null) {
                continue;
            }
            final Location mid = doorCenter(world, door);
            mid.add(0.5, 1.6, 0.5);
            HologramUtil.spawnLines(mid, List.of(
                    Component.text("Tuer").color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD),
                    Component.text(door.area1 + " <-> " + door.area2).color(NamedTextColor.WHITE),
                    Component.text(door.gold + " Gold").color(NamedTextColor.GOLD)
            ));
        }

        // Perk-Maschinen Schilder
        for (final PerkMachine perkMachine : config.perkMachines) {
            if (perkMachine.position == null || perkMachine.perk == null) {
                continue;
            }
            final List<Component> lines;
            if (powerOn) {
                lines = List.of(
                        perkMachine.perk.getDisplayName(),
                        Component.text(perkMachine.gold + " Gold").color(NamedTextColor.GOLD),
                        Component.text("Bereit").color(NamedTextColor.GREEN)
                );
            } else {
                lines = List.of(
                        perkMachine.perk.getDisplayName(),
                        Component.text("BRAUCHT STROM").color(NamedTextColor.RED).decorate(TextDecoration.BOLD),
                        Component.text(perkMachine.gold + " Gold").color(NamedTextColor.DARK_GRAY)
                );
            }
            placePerkSign(world, perkMachine.position, lines);
        }

        // Power Switch
        if (config.powerSwitch != null && config.powerSwitch.position != null) {
            final List<Component> lines = powerOn
                    ? List.of(
                        Component.text("STROM").color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD),
                        Component.text("AKTIV").color(NamedTextColor.GREEN),
                        Component.text("")
                    )
                    : List.of(
                        Component.text("STROM").color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD),
                        Component.text(config.powerSwitch.gold + " Gold").color(NamedTextColor.GOLD),
                        Component.text("Rechtsklick")
                    );
            placeShopSign(world, config.powerSwitch.position, lines);
        }

        // Waffen-Shops
        for (final WeaponShop weaponShop : config.weaponShops) {
            if (weaponShop.position == null || weaponShop.weaponType == null) {
                continue;
            }
            placeShopSign(world, weaponShop.position, List.of(
                    weaponShop.weaponType.getDisplayName(),
                    Component.text(weaponShop.purchasePrice + " Gold").color(NamedTextColor.GOLD),
                    Component.text("Nachfullen: " + weaponShop.refillPrice).color(NamedTextColor.GRAY)
            ));
        }

        // Ruestung
        for (final ArmorShop armorShop : config.armorShops) {
            if (armorShop.position == null || armorShop.quality == null || armorShop.part == null) {
                continue;
            }
            placeShopSign(world, armorShop.position, List.of(
                    armorShop.quality.getDisplayName().append(Component.text(" Ruestung")),
                    armorShop.part.getDisplayName(),
                    Component.text(armorShop.price + " Gold").color(NamedTextColor.GOLD)
            ));
        }
    }

    private static Location doorCenter(final World world, final Door door) {
        // Mittelpunkt der Tuer-Area
        final var p1 = door.position.position1;
        final var p2 = door.position.position2;
        final double x = (p1.blockX() + p2.blockX()) / 2.0;
        final double y = Math.max(p1.blockY(), p2.blockY());
        final double z = (p1.blockZ() + p2.blockZ()) / 2.0;
        return new Location(world, x, y, z);
    }

    private static void placeShopSign(final World world, final BlockPosition position, final List<Component> lines) {
        final Optional<SignPos> pos = getShopSignPosition(world, position);
        if (pos.isEmpty()) {
            return;
        }
        writeSign(pos.get(), lines);
    }

    private static void placePerkSign(final World world, final BlockPosition position, final List<Component> lines) {
        final Optional<SignPos> pos = getPerkSignPosition(world, position);
        if (pos.isEmpty()) {
            // Fallback: Shop-Logik
            placeShopSign(world, position, lines);
            return;
        }
        writeSign(pos.get(), lines);
    }

    private record SignPos(Block block, BlockFace direction) { }

    private static Optional<SignPos> getShopSignPosition(final World world, final BlockPosition position) {
        final Block signBlock = position.toLocation(world).getBlock();
        Block neighbour = null;
        int modX = 0, modZ = 0;
        search:
        for (modX = -1; modX <= 1; modX++) {
            for (modZ = -1; modZ <= 1; modZ++) {
                if ((modX == 0) == (modZ == 0)) {
                    continue;
                }
                final Block candidate = signBlock.getRelative(modX, 0, modZ);
                if (candidate.getType().isEmpty()) {
                    continue;
                }
                neighbour = candidate;
                break search;
            }
        }
        if (neighbour == null) {
            return Optional.empty();
        }
        final int fx = modX;
        final int fz = modZ;
        final BlockFace toNeighbour = Arrays.stream(BlockFace.values())
                .filter(f -> f.getModX() == fx && f.getModZ() == fz)
                .findAny().orElse(BlockFace.NORTH);
        return Optional.of(new SignPos(signBlock, toNeighbour.getOppositeFace()));
    }

    private static Optional<SignPos> getPerkSignPosition(final World world, final BlockPosition position) {
        final Block buttonBlock = position.toLocation(world).getBlock();
        if (!(buttonBlock.getBlockData() instanceof final Switch buttonData)) {
            return Optional.empty();
        }
        final BlockFace signDirection = buttonData.getFacing();
        final Block signBlock = buttonBlock.getRelative(BlockFace.UP);
        return Optional.of(new SignPos(signBlock, signDirection));
    }

    private static void writeSign(final SignPos pos, final List<Component> lines) {
        final Block signBlock = pos.block();
        signBlock.setType(Material.OAK_WALL_SIGN);
        final WallSign data = (WallSign) signBlock.getBlockData();
        data.setFacing(pos.direction());
        signBlock.setBlockData(data);
        final Sign state = (Sign) signBlock.getState();
        final SignSide front = state.getSide(Side.FRONT);
        front.setGlowingText(true);
        for (int i = 0; i < Math.min(4, lines.size()); i++) {
            front.line(i, lines.get(i));
        }
        state.update();
    }
}
