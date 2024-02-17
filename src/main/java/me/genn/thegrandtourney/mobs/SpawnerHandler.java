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
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.inventory.ItemStack;

public class SpawnerHandler {

    public List<SpawnerTemplate> allSpawners;



    public SpawnerHandler() {
    }
    public void registerSpawners(TGT plugin, ConfigurationSection config) throws IOException {
        this.allSpawners = new ArrayList<>();
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
}
