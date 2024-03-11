package me.genn.thegrandtourney.mobs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.npc.TGTNpc;
import me.genn.thegrandtourney.skills.fishing.FishingZone;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.inventory.ItemStack;

public class SpawnerHandler {

    public List<SpawnerTemplate> allSpawners;

    public List<Spawner> allSpawnedSpawners;



    public SpawnerHandler() {
    }
    public void registerSpawners(TGT plugin, ConfigurationSection config) throws IOException {
        this.allSpawners = new ArrayList<>();
        this.allSpawnedSpawners = new ArrayList<>();
        Iterator var4 = config.getKeys(false).iterator();
        while(var4.hasNext()) {
            String key = (String)var4.next();
            SpawnerTemplate spawner = SpawnerTemplate.create(config.getConfigurationSection(key));
            if (spawner != null) {
                this.allSpawners.add(spawner);
            } else {
                plugin.getLogger().severe("Ore Template " + key + " was empty!");
            }
        }

    }
    public boolean containsName(final List<SpawnerTemplate> list, final String name){
        return list.stream().map(SpawnerTemplate::getName).filter(name::equals).findFirst().isPresent();
    }

    private List<Spawner> listOfSpawnersWithMobName(final List<Spawner> list, final String name){
        return list.stream().filter(o -> o.getMobName().equals(name)).toList();
    }

    private Spawner getClosestSpawner(List<Spawner> spawners, Location originLoc) {
        Spawner spawner = spawners.get(0);
        Location minLoc = spawner.loc;
        for (int i = 1; i<spawners.size(); i++) {
            if (spawners.get(i).loc.distanceSquared(originLoc) < minLoc.distanceSquared(originLoc)) {
                minLoc = spawners.get(i).loc;
                spawner = spawners.get(i);
            }
        }
        return spawner;
    }

    public Spawner getSpawnerForObj(String name, Location originLoc) {
        List<Spawner> spawners = listOfSpawnersWithMobName(allSpawnedSpawners, name);
        if (spawners.size() == 0) {
            return null;
        } else if (spawners.size() == 1) {
            return spawners.get(0);
        } else {
            Spawner spawner = getClosestSpawner(spawners, originLoc);
            return spawner;
        }
    }
}
