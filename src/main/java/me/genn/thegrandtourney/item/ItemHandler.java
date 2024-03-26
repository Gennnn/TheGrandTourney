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
import me.genn.thegrandtourney.skills.Recipe;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.inventory.ItemStack;

public class ItemHandler {

    public List<MMOItem> allItems;
    public List<Recipe> allRecipes;



    public ItemHandler() {
    }
    public void registerItems(TGT plugin, ConfigurationSection config) throws IOException {
        this.allItems = new ArrayList<>();
        this.allRecipes = new ArrayList<>();
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
        registerRecipes(plugin);

    }
    public void registerRecipes(TGT plugin) throws IOException {
        Iterator var4 = this.allRecipes.iterator();
        while(var4.hasNext()) {
            Recipe recipe = (Recipe) var4.next();
            recipe.createStep2(plugin);
        }

    }
    public Recipe getRecipeFromDisplayName(String name) {
        return allRecipes.stream().filter(obj -> obj.getDisplayName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }
    public boolean recipesContainsDisplayName(String name) {
        return allRecipes.stream().anyMatch(obj -> obj.getDisplayName().equalsIgnoreCase(name));
    }
    public boolean containsName(final List<MMOItem> list, final String name){
        return list.stream().map(MMOItem::getName).anyMatch(name::equalsIgnoreCase);
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
    public Recipe getRecipeFromString(String itemId) {
        MMOItem item = getMMOItemFromString(itemId);
        if (item == null) {
            return null;
        }
        Recipe recipe = allRecipes.stream().filter(obj -> obj.reward.internalName.equalsIgnoreCase(item.internalName)).findFirst().orElse(null);
        return recipe;
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

    public boolean itemIsMMOItemOfName(ItemStack item, String name) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }
        NBTItem nbtItem = new NBTItem(item);
        if (nbtItem.hasTag("ExtraAttributes")) {
            NBTCompound comp = nbtItem.getCompound("ExtraAttributes");
            if (comp.hasTag("id") && comp.getString("id").equalsIgnoreCase(name)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }







}
