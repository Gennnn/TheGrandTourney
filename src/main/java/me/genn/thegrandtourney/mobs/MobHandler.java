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
import me.genn.thegrandtourney.item.MMOItem;
import me.genn.thegrandtourney.mobs.MMOMob;
import me.genn.thegrandtourney.npc.TGTNpc;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.inventory.ItemStack;

public class MobHandler {

    public List<MMOMob> allMobs;



    public MobHandler() {
    }
    public void registerMobs(TGT plugin, ConfigurationSection config) throws IOException {
        this.allMobs = new ArrayList<>();
        Iterator var4 = config.getKeys(false).iterator();
        while(var4.hasNext()) {
            String key = (String)var4.next();
            MMOMob mob = MMOMob.create(config.getConfigurationSection(key));
            if (mob != null) {
                this.allMobs.add(mob);
            } else {
                plugin.getLogger().severe("Mob " + key + " was empty!");
            }
        }

    }
    public boolean containsName(final List<MMOMob> list, final String name){
        return list.stream().map(MMOMob::getName).filter(name::equals).findFirst().isPresent();
    }

    public MMOMob getMobFromString(String string) {
        if (containsName(allMobs, string)) {
            MMOMob mob = allMobs.stream().filter(obj -> obj.internalName.equalsIgnoreCase(string)).findFirst().orElse(null);
            return mob;
        }
        return null;
    }








}
