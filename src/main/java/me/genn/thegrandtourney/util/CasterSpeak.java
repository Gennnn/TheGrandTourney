package me.genn.thegrandtourney.util;

import me.genn.thegrandtourney.TGT;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class CasterSpeak {
    TGT plugin;

    public CasterSpeak(TGT plugin) {
        this.plugin = plugin;
    }

    public void speak(int delaySeconds, String text, String speaker) {
        if (speaker.equalsIgnoreCase("king")) {
            speakKing(text, delaySeconds);
        } else {

        }
    }
    public void speakKing(String text, int delaySeconds) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.broadcastMessage(ChatColor.WHITE + "<" + ChatColor.GOLD + "King Posh" + ChatColor.WHITE + "> " + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', text));
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.playSound(player.getLocation(), "entity.villager.celebrate", 5.0f, 0.75f);
                }
            }
        }.runTaskLater(plugin, delaySeconds * 20L);


    }
    public void speakHerald(String text, int delaySeconds) {
        new BukkitRunnable() {

            @Override
            public void run() {
                Bukkit.broadcastMessage(ChatColor.WHITE + "<" + ChatColor.YELLOW + "Herald" + ChatColor.WHITE + "> " + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', text));
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.playSound(player.getLocation(), "entity.villager.trade", 5.0f, 1.25f);
                }
            }
        }.runTaskLater(plugin, delaySeconds * 20L);


    }
}
