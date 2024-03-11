package me.genn.thegrandtourney.mobs;

import io.lumine.mythic.api.skills.placeholders.PlaceholderInt;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.spawning.spawners.MythicSpawner;
import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.grid.Paste;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.logging.Level;

public class Spawner {
    TGT plugin;
    SpawnerTemplate template;
    public Location loc;
    public Spawner(TGT plugin, SpawnerTemplate template) {
        this.plugin = plugin;
        this.template = template;
    }
    public void spawn(Location loc) {
        String name = template.name + "" + (plugin.spawnerLocationList.size() + 1);
        MythicBukkit.inst().getSpawnerManager().createSpawner(name, loc, template.mobName);
        MythicSpawner mythicSpawner = MythicBukkit.inst().getSpawnerManager().getSpawnerByName(name);
        if (mythicSpawner == null) {
            Bukkit.getLogger().log(Level.SEVERE, "SPAWNER " + name + " COULD NOT BE LOADED!");
            return;
        }
        mythicSpawner.setSpawnRadius(template.radius);
        mythicSpawner.setSpawnRadiusY(template.radiusY);
        mythicSpawner.setUseTimer(template.useTimer);
        mythicSpawner.setMaxMobs(PlaceholderInt.of(String.valueOf(template.maxMobs)));
        mythicSpawner.setMobsPerSpawn(template.mobsPerSpawn);
        mythicSpawner.setCooldownSeconds(template.cooldown);
        mythicSpawner.setWarmupSeconds(template.warmup);
        mythicSpawner.setCheckForPlayers(template.checkForPlayers);
        mythicSpawner.setLeashRange(template.leashRange);
        mythicSpawner.Enable();
        mythicSpawner.ActivateSpawner();
        this.loc = loc;
        plugin.spawnerHandler.allSpawnedSpawners.add(this);
    }
    public void paste(Location loc, Paste paste, int counter) {
        String name = template.name + "." + paste.schematic.name + "." + counter;
        MythicBukkit.inst().getSpawnerManager().createSpawner(name, loc, template.mobName);
        MythicSpawner mythicSpawner = MythicBukkit.inst().getSpawnerManager().getSpawnerByName(name);
        if (mythicSpawner == null) {
            Bukkit.getLogger().log(Level.SEVERE, "SPAWNER " + name + " COULD NOT BE LOADED!");
            return;
        }
        mythicSpawner.setSpawnRadius(template.radius);
        mythicSpawner.setSpawnRadiusY(template.radiusY);
        mythicSpawner.setUseTimer(template.useTimer);
        mythicSpawner.setMaxMobs(PlaceholderInt.of(String.valueOf(template.maxMobs)));
        mythicSpawner.setMobsPerSpawn(template.mobsPerSpawn);
        mythicSpawner.setCooldownSeconds(template.cooldown);
        mythicSpawner.setWarmupSeconds(template.warmup);
        mythicSpawner.setCheckForPlayers(template.checkForPlayers);
        mythicSpawner.setLeashRange(template.leashRange);
        mythicSpawner.setActivationRange(template.activationRange);
        mythicSpawner.Enable();
        mythicSpawner.ActivateSpawner();
        this.loc = loc;
        plugin.spawnerHandler.allSpawnedSpawners.add(this);
    }

    public String getName() {
        return template.name;
    }
    public String getMobName() {
        return template.mobName;
    }
}
