package me.genn.thegrandtourney.player;

import me.genn.thegrandtourney.TGT;
import org.bukkit.plugin.java.JavaPlugin;

public class ActionBarMessage {
    public int maxDuration;
    public String message;
    public int priority = 0;
    public int duration = 0;
    TGT plugin;

    public ActionBarMessage() {
        plugin = JavaPlugin.getPlugin(TGT.class);
    }
}
