package io.lama06.zombies.weapon;

import io.lama06.zombies.menu.MenuDisplayableEnum;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Particle;

public enum WeaponType implements MenuDisplayableEnum {
    KNIFE(
            new WeaponData()
                    .setDisplayName(Component.text("Messer"))
                    .setMaterial(Material.IRON_SWORD)
                    .setMelee(new MeleeData(5))
                    .setAttack(new AttackData(5, false, 10))
                    .setDelay(new DelayData(10))
    ),
    PISTOL(
            new WeaponData()
                    .setDisplayName(Component.text("Pistole"))
                    .setMaterial(Material.WOODEN_HOE)
                    .setShoot(new ShootData(1, 1))
                    .setShootParticle(new ShootParticleData(Particle.FLAME))
                    .setAttack(new AttackData(5, false, 10))
                    .setAmmo(new AmmoData(300, 10))
                    .setDelay(new DelayData(10))
                    .setReload(new ReloadData(30))
    ),
    RIFLE(
            new WeaponData()
                    .setDisplayName(Component.text("Gewehr"))
                    .setMaterial(Material.STONE_HOE)
                    .setShoot(new ShootData(1, 1))
                    .setShootParticle(new ShootParticleData(Particle.SMOKE_NORMAL))
                    .setAttack(new AttackData(5, false, 7))
                    .setAmmo(new AmmoData(288, 32))
                    .setDelay(new DelayData(4))
                    .setReload(new ReloadData(30))
    ),
    SHOTGUN(
            new WeaponData()
                    .setDisplayName(Component.text("Schrotflinte"))
                    .setMaterial(Material.IRON_HOE)
                    .setShoot(new ShootData(10, 0.85))
                    .setShootParticle(new ShootParticleData(Particle.FLAME))
                    .setAttack(new AttackData(1.5, false, 8))
                    .setAmmo(new AmmoData(65, 5))
                    .setDelay(new DelayData((int) (1.4 * 20)))
                    .setReload(new ReloadData((int) (1.5 * 20)))
    ),
    SNIPER(
            new WeaponData()
                    .setDisplayName(Component.text("Scharfschuetzengewehr"))
                    .setMaterial(Material.WOODEN_SHOVEL)
                    .setShoot(new ShootData(2, 1))
                    .setShootParticle(new ShootParticleData(Particle.FLAME))
                    .setAttack(new AttackData(20, false, 30))
                    .setAmmo(new AmmoData(40, 4))
                    .setDelay(new DelayData(30))
                    .setReload(new ReloadData(40))
                    .includeInLuckyChest()
    ),
    FLAME_THROWER(
            new WeaponData()
                    .setDisplayName(Component.text("Flammenwerfer"))
                    .setMaterial(Material.GOLDEN_HOE)
                    .setShoot(new ShootData(1, 0.95))
                    .setShootParticle(new ShootParticleData(Particle.FLAME))
                    .setAttack(new AttackData(2, true, 4))
                    .setAmmo(new AmmoData(350, 50))
                    .setDelay(new DelayData(2))
                    .setReload(new ReloadData(30))
                    .includeInLuckyChest()
    ),
    GOLD_DIGGER(
            new WeaponData()
                    .setDisplayName(Component.text("Goldgraeber").color(NamedTextColor.GOLD))
                    .setMaterial(Material.GOLDEN_PICKAXE)
                    .setAttack(new AttackData(6, false, 13))
                    .setShoot(new ShootData(1, 1))
                    .setShootParticle(new ShootParticleData(Particle.FLAME))
                    .setDelay(new DelayData(10))
                    .setReload(new ReloadData(30))
                    .setAmmo(new AmmoData(70, 7))
                    .includeInLuckyChest()
    );

    public final WeaponData data;

    WeaponType(final WeaponData data) {
        this.data = data;
    }

    @Override
    public Component getDisplayName() {
        return data.displayName;
    }

    @Override
    public Material getDisplayMaterial() {
        return data.material;
    }
}
