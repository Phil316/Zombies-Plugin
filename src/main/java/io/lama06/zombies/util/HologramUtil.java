package io.lama06.zombies.util;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

/** Einfache schwebende Text-Hologramme (ArmorStands), Hypixel-aehnlich. */
public final class HologramUtil {
    public static final String HOLOGRAM_TAG = "zombies_hologram";

    private HologramUtil() { }

    public static List<ArmorStand> spawnLines(final Location base, final List<Component> lines) {
        final List<ArmorStand> stands = new ArrayList<>();
        final World world = base.getWorld();
        if (world == null) {
            return stands;
        }
        // Von oben nach unten
        double y = base.getY() + 0.3 * (lines.size() - 1);
        for (final Component line : lines) {
            final Location loc = base.clone();
            loc.setY(y);
            final ArmorStand stand = (ArmorStand) world.spawnEntity(loc, EntityType.ARMOR_STAND);
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setMarker(true);
            stand.setInvulnerable(true);
            stand.setCustomNameVisible(true);
            stand.customName(line);
            stand.setCollidable(false);
            stand.setSilent(true);
            stand.addScoreboardTag(HOLOGRAM_TAG);
            stands.add(stand);
            y -= 0.28;
        }
        return stands;
    }

    public static void removeAll(final World world) {
        for (final Entity entity : world.getEntities()) {
            if (entity.getScoreboardTags().contains(HOLOGRAM_TAG)) {
                entity.remove();
            }
        }
    }
}
