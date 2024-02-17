package me.genn.thegrandtourney.mobs;

import me.genn.thegrandtourney.TGT;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public class SpawnerTemplate {
    public String mobName;
    public int radius;
    public int radiusY;
    public boolean useTimer;
    public int maxMobs;
    public int mobsPerSpawn;
    public int cooldown;
    public int warmup;
    public boolean checkForPlayers;
    public int leashRange;
    public String name;
    public double activationRange;

    public SpawnerTemplate() {
    }

    public static SpawnerTemplate create(ConfigurationSection config) throws IOException {
        SpawnerTemplate spawner = new SpawnerTemplate();
        TGT plugin = JavaPlugin.getPlugin(TGT.class);
        spawner.mobName = config.getString("mob-name");
        if ( plugin.mobHandler.getMobFromString(spawner.mobName) == null) {
            return null;
        }
        spawner.radius = config.getInt("radius", 5);
        spawner.radiusY = config.getInt("y-radius", 2);
        spawner.useTimer = config.getBoolean("use-timer", true);
        spawner.maxMobs = config.getInt("max-mobs", 2);
        spawner.mobsPerSpawn = config.getInt("mobs-per-spawn", 1);
        spawner.cooldown = config.getInt("cooldown", 15);
        spawner.warmup = config.getInt("warmup", 0);
        spawner.checkForPlayers = config.getBoolean("check-for-players", true);
        spawner.leashRange = config.getInt("leash-range", 30);
        spawner.name = config.getName();
        spawner.activationRange = config.getDouble("activation-range", 40);

        return spawner;
    }

    public String getName() {
        return this.name;
    }

}
