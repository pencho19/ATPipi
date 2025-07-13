package com.example.atpipi;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class ATPipiPlugin extends JavaPlugin implements CommandExecutor {

    private boolean isLegacyVersion;
    private boolean enabled = true; // ← Флаг включения/отключения плагина

    @Override
    public void onEnable() {
        isLegacyVersion = !Bukkit.getVersion().contains("1.13") &&
                          !Bukkit.getVersion().contains("1.14") &&
                          !Bukkit.getVersion().contains("1.15") &&
                          !Bukkit.getVersion().contains("1.16") &&
                          !Bukkit.getVersion().contains("1.17") &&
                          !Bukkit.getVersion().contains("1.18") &&
                          !Bukkit.getVersion().contains("1.19") &&
                          !Bukkit.getVersion().contains("1.20") &&
                          !Bukkit.getVersion().contains("1.21");

        this.getCommand("atpipi").setExecutor(this);
        this.getCommand("atpipi-toggle").setExecutor(this); // ← Регистрируем вторую команду
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (command.getName().equalsIgnoreCase("atpipi-toggle")) {
            if (!sender.hasPermission("atpipi.toggle")) {
                sender.sendMessage("§cУ тебя нет прав на использование этой команды.");
                return true;
            }

            enabled = !enabled;
            sender.sendMessage("§eПлагин ATPipi теперь " + (enabled ? "§aвключён" : "§cвыключен"));
            return true;
        }

        if (command.getName().equalsIgnoreCase("atpipi")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Эту команду может использовать только игрок.");
                return true;
            }

            if (!enabled) {
                sender.sendMessage("§cПлагин ATPipi сейчас отключён админом.");
                return true;
            }

            Player player = (Player) sender;

            if (!player.hasPermission("atpipi.use")) {
                player.sendMessage("У вас нет прав для использования этой команды.");
                return true;
            }

            startPipiAction(player);
            return true;
        }

        return false;
    }

    private void startPipiAction(Player player) {
        Set<Player> hitPlayers = new HashSet<>();
        final boolean[] hasNotified = {false};

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 100) {
                    this.cancel();
                    return;
                }
                dropYellowConcrete(player, hitPlayers, hasNotified);
                ticks++;
            }
        }.runTaskTimer(this, 0L, 1L);
    }

    private void dropYellowConcrete(Player player, Set<Player> hitPlayers, boolean[] hasNotified) {
        Material concreteMaterial = isLegacyVersion ? Material.valueOf("CONCRETE") : Material.YELLOW_CONCRETE;

        final org.bukkit.entity.Item item = player.getWorld().dropItemNaturally(
            player.getLocation(),
            new org.bukkit.inventory.ItemStack(concreteMaterial, 1)
        );

        item.setPickupDelay(Integer.MAX_VALUE);
        item.setVelocity(player.getLocation().getDirection().normalize().multiply(0.5));

        new BukkitRunnable() {
            @Override
            public void run() {
                item.remove();
            }
        }.runTaskLater(this, 20L);

        for (Entity entity : player.getNearbyEntities(1, 1, 1)) {
            if (entity instanceof Player target && !hitPlayers.contains(target)) {
                hitPlayers.add(target);
                player.sendMessage("Я попал на " + target.getName());

                if (!hasNotified[0]) {
                    target.sendMessage("На вас попал " + player.getName());
                    hasNotified[0] = true;
                }
            }
        }
    }
}
