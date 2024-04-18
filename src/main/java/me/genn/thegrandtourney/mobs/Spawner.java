package me.genn.thegrandtourney.mobs;

import io.lumine.mythic.api.skills.placeholders.PlaceholderInt;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.spawning.spawners.MythicSpawner;
import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.grid.Paste;
import me.genn.thegrandtourney.skills.TournamentObject;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.File;
import java.util.Objects;
import java.util.logging.Level;

public class Spawner implements TournamentObject {
    TGT plugin;
    public SpawnerTemplate template;
    public Location loc;
    public String name;
    public Spawner(TGT plugin, SpawnerTemplate template) {
        this.plugin = plugin;
        this.template = template;
    }
    @Override
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
    @Override
    public void paste(Location loc) {

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

    @Override
    public void remove() {
        plugin.spawnerHandler.allSpawnedSpawners.remove(this);
        MythicBukkit.inst().getSpawnerManager().removeSpawner(MythicBukkit.inst().getSpawnerManager().getSpawnerByName(name));
        for (File file : Objects.requireNonNull(MythicBukkit.inst().getSpawnerManager().getSpawnerFolder().listFiles())) {
            if (file.getName().equalsIgnoreCase(name + ".yml")) {
                file.delete();
            }
        }
        this.loc = null;
        this.name = null;
    }

    public String getName() {
        return template.name;
    }
    public String getMobName() {
        return template.mobName;
    }
}
