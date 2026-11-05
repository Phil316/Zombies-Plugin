package io.lama06.zombies;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import io.lama06.zombies.event.player.PlayerCancelCommandEvent;
import io.lama06.zombies.event.player.PlayerGoldChangeEvent;
import io.lama06.zombies.system.ShopDisplaySystem;
import io.lama06.zombies.weapon.WeaponType;
import io.lama06.zombies.zombie.ZombieType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public final class ZombiesCommandExecutor implements TabExecutor {
    private static final String DEAD_END_TEMPLATE = "templates/dead_end.json";

    @Override
    public boolean onCommand(
            final CommandSender sender,
            final Command command,
            final String label,
            final String[] args
    ) {
        if (args.length == 0) {
            root(sender);
            return true;
        }

        final String[] remainingArgs = Arrays.copyOfRange(args, 1, args.length);
        switch (args[0]) {
            case "config" -> config(sender);
            case "saveConfig" -> saveConfig(sender);
            case "checkConfig" -> checkConfig(sender);
            case "loadTemplate" -> loadTemplate(sender);
            case "start" -> start(sender);
            case "stop" -> stop(sender);
            case "giveGold" -> giveGold(sender, remainingArgs);
            case "giveWeapon" -> giveWeapon(sender, remainingArgs);
            case "spawnZombie" -> spawnZombie(sender, remainingArgs);
            case "cancel" -> cancel(sender);
            case "placeSigns" -> placeSigns(sender, remainingArgs);
            case "dumpWorldConfig" -> dumpWorldConfig(sender);
            case "difficulty", "schwierigkeit" -> difficulty(sender, remainingArgs);
            default -> sender.sendMessage(Component.text("Unbekannter Befehl").color(NamedTextColor.RED));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(
            final CommandSender sender,
            final Command command,
            final String label,
            final String[] args
    ) {
        if (args.length == 1) {
            return List.of(
                    "config", "saveConfig", "checkConfig", "loadTemplate", "start", "stop",
                    "giveGold", "giveWeapon", "spawnZombie", "placeSigns", "difficulty"
            );
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("difficulty")) {
            return List.of("leicht", "normal", "schwer");
        }
        return List.of();
    }

    private void root(final CommandSender sender) {
        final Component equalSigns = Component.text("=".repeat(10)).color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD);
        final Component obfuscatedChar = Component.text("_").decorate(TextDecoration.OBFUSCATED);
        final TextComponent.Builder builder = Component.text();

        builder.append(equalSigns).append(obfuscatedChar).appendSpace();
        builder.append(Component.text("Zombies-Plugin").color(NamedTextColor.LIGHT_PURPLE));
        builder.appendSpace().append(Component.text("(Version %s)".formatted(ZombiesPlugin.INSTANCE.getPluginMeta().getVersion()))
                                             .color(NamedTextColor.GREEN));
        builder.appendSpace().append(obfuscatedChar).append(equalSigns);

        builder.appendNewline();
        builder.append(Component.text("Creator: "));
        builder.append(Component.text("Lama06").color(NamedTextColor.GOLD));

        builder.appendNewline();
        builder.append(Component.text("Befehle: /zombies loadTemplate | start | difficulty leicht|normal|schwer"));

        sender.sendMessage(builder);
    }

    private void difficulty(final CommandSender sender, final String[] args) {
        if (!(sender instanceof final Player player)) {
            sender.sendMessage(Component.text("Nur als Spieler nutzbar.").color(NamedTextColor.RED));
            return;
        }
        final ZombiesWorld world = new ZombiesWorld(player.getWorld());
        if (args.length == 0) {
            final GameDifficulty current = world.getDifficulty();
            sender.sendMessage(Component.text("Aktuelle Schwierigkeit: ").append(current.getDisplayName()));
            sender.sendMessage(Component.text("Aendern: /zombies difficulty leicht|normal|schwer").color(NamedTextColor.GRAY));
            return;
        }
        if (world.isGameRunning()) {
            sender.sendMessage(Component.text("Schwierigkeit nur vor Spielstart aendern.").color(NamedTextColor.RED));
            return;
        }
        final GameDifficulty d = GameDifficulty.fromString(args[0]);
        world.set(ZombiesWorld.DIFFICULTY, d.name());
        sender.sendMessage(Component.text("Schwierigkeit gesetzt: ").append(d.getDisplayName()));
        sender.sendMessage(Component.text("Leicht = weniger/schwächere Zombies, Schwer = mehr/stärkere.").color(NamedTextColor.GRAY));
    }

    private void config(final CommandSender sender) {
        if (!(sender instanceof final Player player)) {
            return;
        }
        final ZombiesWorld world = new ZombiesWorld(player.getWorld());
        if (world.isGameRunning()) {
            sender.sendMessage(Component.text("Config wahrend eines laufenden Spiels andern ist riskant.").color(NamedTextColor.RED));
        }
        WorldConfig config = world.getConfig();
        if (config == null) {
            final ZombiesConfig globalConfig = ZombiesPlugin.INSTANCE.getGlobalConfig();
            config = new WorldConfig();
            globalConfig.worlds.put(world.getBukkit().getName(), config);
        }
        config.openMenu(player, () -> {});
    }

    private void saveConfig(final CommandSender sender) {
        try {
            ZombiesPlugin.INSTANCE.getConfigManager().saveConfig();
        } catch (final IOException e) {
            sender.sendMessage(Component.text("Fehler beim Speichern: " + e.getMessage()).color(NamedTextColor.RED));
            ZombiesPlugin.INSTANCE.getSLF4JLogger().error("failed to save config", e);
            return;
        }
        sender.sendMessage(Component.text("Config gespeichert.").color(NamedTextColor.GREEN));
    }

    private void checkConfig(final CommandSender sender) {
        if (!(sender instanceof final Player player)) {
            return;
        }
        final WorldConfig config = ZombiesPlugin.INSTANCE.getWorldConfig(new ZombiesWorld(player.getWorld()));
        if (config == null) {
            sender.sendMessage(Component.text("Diese Welt ist nicht konfiguriert. Nutze /zombies loadTemplate").color(NamedTextColor.RED));
            return;
        }
        try {
            config.check();
        } catch (final InvalidConfigException e) {
            sender.sendMessage(Component.text("Config-Fehler: " + e.getMessage()).color(NamedTextColor.RED));
            return;
        }
        sender.sendMessage(Component.text("Keine Probleme gefunden.").color(NamedTextColor.GREEN));
    }

    private void loadTemplate(final CommandSender sender) {
        if (!(sender instanceof final Player player)) {
            sender.sendMessage(Component.text("Nur als Spieler nutzbar.").color(NamedTextColor.RED));
            return;
        }
        final ZombiesWorld world = new ZombiesWorld(player.getWorld());
        final ZombiesConfig globalConfig = ZombiesPlugin.INSTANCE.getGlobalConfig();
        final InputStream resource = ZombiesPlugin.INSTANCE.getResource(DEAD_END_TEMPLATE);
        if (resource == null) {
            sender.sendMessage(Component.text("Dead-End-Template fehlt in der Plugin-JAR.").color(NamedTextColor.RED));
            return;
        }
        final WorldConfig config;
        try (resource) {
            config = ConfigManager.createGson().fromJson(new InputStreamReader(resource, StandardCharsets.UTF_8), WorldConfig.class);
        } catch (final JsonParseException e) {
            sender.sendMessage(Component.text("Template kaputt (JSON): " + e.getMessage()).color(NamedTextColor.RED));
            ZombiesPlugin.INSTANCE.getSLF4JLogger().error("dead_end template parse failed", e);
            return;
        } catch (final IOException e) {
            sender.sendMessage(Component.text("Template konnte nicht gelesen werden.").color(NamedTextColor.RED));
            return;
        }
        if (config == null) {
            sender.sendMessage(Component.text("Template ist leer / konnte nicht geladen werden.").color(NamedTextColor.RED));
            return;
        }
        globalConfig.worlds.put(world.getBukkit().getName(), config);

        try {
            ZombiesPlugin.INSTANCE.getConfigManager().saveConfig();
        } catch (final IOException e) {
            sender.sendMessage(Component.text("Geladen, aber Speichern fehlgeschlagen: " + e.getMessage()).color(NamedTextColor.YELLOW));
        }

        placeSigns(player, new String[]{"ok"});

        sender.sendMessage(Component.text("Dead-End-Config geladen und gespeichert.").color(NamedTextColor.GREEN));
        sender.sendMessage(Component.text("> Klicke hier zum Starten <")
                                   .color(NamedTextColor.GOLD)
                                   .decorate(TextDecoration.BOLD)
                                   .clickEvent(ClickEvent.runCommand("/zombies start")));
    }

    private void start(final CommandSender sender) {
        if (!(sender instanceof final Player player)) {
            sender.sendMessage(Component.text("Nur als Spieler nutzbar.").color(NamedTextColor.RED));
            return;
        }
        final ZombiesWorld world = new ZombiesWorld(player.getWorld());
        if (!world.isZombiesWorld()) {
            final Component msg = Component.text("Welt ist nicht konfiguriert.").color(NamedTextColor.RED)
                    .appendNewline()
                    .append(Component.text("> Dead-End-Config laden (empfohlen) <")
                                    .clickEvent(ClickEvent.runCommand("/zombies loadTemplate"))
                                    .color(NamedTextColor.GREEN));
            sender.sendMessage(msg);
            return;
        }
        if (world.isGameRunning()) {
            sender.sendMessage(Component.text("Das Spiel lauft bereits.").color(NamedTextColor.RED));
            return;
        }
        try {
            world.getConfig().check();
        } catch (final InvalidConfigException e) {
            player.sendMessage(Component.text("Config ungueltig: " + e.getMessage()).color(NamedTextColor.RED));
            player.sendMessage(Component.text("Tipp: /zombies loadTemplate erneut ausfuhren.").color(NamedTextColor.YELLOW));
            return;
        }
        world.startGame();
        ShopDisplaySystem.refreshAll(world);
        player.sendMessage(Component.text("Spiel gestartet!").color(NamedTextColor.GREEN));
    }

    private void stop(final CommandSender sender) {
        if (!(sender instanceof final Player player)) {
            return;
        }
        final ZombiesWorld world = new ZombiesWorld(player.getWorld());
        if (!world.isZombiesWorld() || !world.isGameRunning()) {
            sender.sendMessage(Component.text("Es lauft kein Spiel.").color(NamedTextColor.RED));
            return;
        }
        world.endGame();
        sender.sendMessage(Component.text("Spiel gestoppt.").color(NamedTextColor.YELLOW));
    }

    private void giveWeapon(final CommandSender sender, final String[] args) {
        if (args.length == 0) {
            return;
        }
        final WeaponType weaponType;
        try {
            weaponType = WeaponType.valueOf(args[0].toUpperCase());
        } catch (final IllegalArgumentException e) {
            return;
        }
        if (!(sender instanceof final Player player)) {
            return;
        }
        final ZombiesPlayer zombiesPlayer = new ZombiesPlayer(player);
        zombiesPlayer.giveWeapon(player.getInventory().getHeldItemSlot(), weaponType);
    }

    private void giveGold(final CommandSender sender, final String[] args) {
        if (!(sender instanceof final Player player)) {
            return;
        }
        final ZombiesPlayer zombiesPlayer = new ZombiesPlayer(player);
        if (args.length == 0) {
            return;
        }
        final int goldAdd;
        try {
            goldAdd = Integer.parseInt(args[0]);
        } catch (final NumberFormatException e) {
            return;
        }
        final int goldPrevious = zombiesPlayer.get(ZombiesPlayer.GOLD);
        final int golfAfter = goldPrevious + goldAdd;
        zombiesPlayer.set(ZombiesPlayer.GOLD, golfAfter);
        Bukkit.getPluginManager().callEvent(new PlayerGoldChangeEvent(zombiesPlayer, goldPrevious, golfAfter));
    }

    private void spawnZombie(final CommandSender sender, final String[] args) {
        if (!(sender instanceof final Player player)) {
            return;
        }
        final ZombiesWorld world = new ZombiesWorld(player.getWorld());
        if (!world.isGameRunning()) {
            return;
        }
        if (args.length == 0) {
            return;
        }
        final ZombieType zombieType;
        try {
            zombieType = ZombieType.valueOf(args[0].toUpperCase());
        } catch (final IllegalArgumentException e) {
            return;
        }
        world.spawnZombie(player.getLocation(), zombieType);
    }

    private void cancel(final CommandSender sender) {
        if (!(sender instanceof final Player player)) {
            return;
        }
        Bukkit.getPluginManager().callEvent(new PlayerCancelCommandEvent(player));
    }

    private void placeSigns(final CommandSender sender, final String[] args) {
        if (!(sender instanceof final Player player)) {
            return;
        }

        if (args.length == 0 || !args[0].equalsIgnoreCase("ok")) {
            final TextComponent.Builder builder = Component.text();
            builder.append(Component.text("Schilder + Tuer-Hologramme setzen."));
            builder.appendNewline();
            builder.append(Component.text("> Hier klicken zum Bestaetigen <")
                                   .clickEvent(ClickEvent.runCommand("/zombies placeSigns ok"))
                                   .color(NamedTextColor.BLUE));
            sender.sendMessage(builder);
            return;
        }

        final ZombiesWorld world = new ZombiesWorld(player.getWorld());
        if (!world.isZombiesWorld()) {
            sender.sendMessage(Component.text("Zuerst /zombies loadTemplate ausfuhren.").color(NamedTextColor.RED));
            return;
        }
        ShopDisplaySystem.refreshAll(world);
        sender.sendMessage(Component.text("Schilder + Tuer-Hologramme gesetzt.").color(NamedTextColor.GREEN));
    }

    private void dumpWorldConfig(final CommandSender sender) {
        if (!(sender instanceof final Player player)) {
            return;
        }
        final ZombiesWorld world = new ZombiesWorld(player.getWorld());
        if (!world.isZombiesWorld()) {
            return;
        }
        final WorldConfig config = world.getConfig();
        final Gson gson = ConfigManager.createGson();
        final String json = gson.toJson(config);
        sender.sendMessage(Component.text("> Kopieren <").clickEvent(ClickEvent.copyToClipboard(json)).color(NamedTextColor.GREEN));
    }
}
