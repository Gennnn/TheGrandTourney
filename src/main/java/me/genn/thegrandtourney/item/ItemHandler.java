package me.genn.thegrandtourney.item;

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

public class ItemHandler {

    public List<MMOItem> allItems;



    public ItemHandler() {
    }
    public void registerItems(TGT plugin, ConfigurationSection config) throws IOException {
        this.allItems = new ArrayList<>();
        Iterator var4 = config.getKeys(false).iterator();
        while(var4.hasNext()) {
            String key = (String)var4.next();
            MMOItem item = MMOItem.create(config.getConfigurationSection(key));
            if (item != null) {
                this.allItems.add(item);
            } else {
                plugin.getLogger().severe("Item " + key + " was empty!");
            }
        }

    }
    public boolean containsName(final List<MMOItem> list, final String name){
        return list.stream().map(MMOItem::getName).filter(name::equals).findFirst().isPresent();
    }
    public ItemStack getItemFromString(String string) {
        if (containsName(allItems, string)) {
            MMOItem item = allItems.stream().filter(obj -> obj.internalName.equalsIgnoreCase(string)).findFirst().orElse(null);
            return getItem(item);
        }
        return new ItemStack(Material.AIR);
    }

    public MMOItem getMMOItemFromString(String string) {
        if (containsName(allItems, string)) {
            MMOItem item = allItems.stream().filter(obj -> obj.internalName.equalsIgnoreCase(string)).findFirst().orElse(null);
            return item;
        }
        return null;
    }
    public ItemStack getItem(MMOItem mmoItem) {
        if (mmoItem == null) {
            return new ItemStack(Material.AIR);
        }
        ItemStack returnItem = mmoItem.bukkitItem;
        NBTItem nbtI = new NBTItem(mmoItem.bukkitItem);
        NBTCompound comp = nbtI.getCompound("ExtraAttributes");
        if (mmoItem.unique) {
            comp.setString("uuid", UUID.randomUUID().toString());
        }
        returnItem = nbtI.getItem();
        return returnItem;
    }







}
