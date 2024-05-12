package me.genn.thegrandtourney.item;

import java.io.File;
import java.io.IOException;
import java.util.*;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.npc.TGTNpc;
import me.genn.thegrandtourney.player.StatUpdates;
import me.genn.thegrandtourney.skills.Recipe;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemHandler {

    public List<MMOItem> allItems;
    public List<Recipe> allRecipes;
    TGT plugin;



    public ItemHandler(TGT plugin) {
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
    public ItemStack getItemWithQuality(MMOItem mmoItem, String quality) {
        if (mmoItem == null) {
            return new ItemStack(Material.AIR);
        }
        ItemStack returnItem = mmoItem.bukkitItem;
        returnItem = applyItemQuality(returnItem,mmoItem,quality);
        NBTItem nbtI = new NBTItem(returnItem);
        NBTCompound comp = nbtI.getCompound("ExtraAttributes");
        if (mmoItem.unique) {
            comp.setString("uuid", UUID.randomUUID().toString());
        }
        returnItem = nbtI.getItem();
        return returnItem;
    }
    private ItemStack applyItemQuality(ItemStack item, MMOItem mmoItem, String quality) {
        if (quality.equalsIgnoreCase("good")) {
            item = updateStatsFromItem(item, mmoItem.qualityScaling[0], mmoItem);
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();
            lore.add(0, ChatColor.GRAY + "Quality: " + ChatColor.RED + "✯" );
            meta.setLore(lore);
            item.setItemMeta(meta);
            NBTItem nbtI = new NBTItem(item);
            Objects.requireNonNull(nbtI.getCompound("ExtraAttributes")).setFloat("statBoost", mmoItem.qualityScaling[0]);
            item = nbtI.getItem();
            return item;
        } else if (quality.equalsIgnoreCase("great")) {
            item = updateStatsFromItem(item, mmoItem.qualityScaling[1], mmoItem);
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();
            lore.add(0, ChatColor.GRAY + "Quality: " + ChatColor.WHITE + "✯✯" );
            meta.setLore(lore);
            item.setItemMeta(meta);
            NBTItem nbtI = new NBTItem(item);
            Objects.requireNonNull(nbtI.getCompound("ExtraAttributes")).setFloat("statBoost", mmoItem.qualityScaling[1]);
            item = nbtI.getItem();
            return item;
        } else if (quality.equalsIgnoreCase("superb")) {
            item = updateStatsFromItem(item, mmoItem.qualityScaling[2], mmoItem);
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();
            lore.add(0, ChatColor.GRAY + "Quality: " + ChatColor.GOLD + "✯✯✯" );
            meta.setLore(lore);
            item.setItemMeta(meta);
            NBTItem nbtI = new NBTItem(item);
            Objects.requireNonNull(nbtI.getCompound("ExtraAttributes")).setFloat("statBoost", mmoItem.qualityScaling[2]);
            item = nbtI.getItem();
            return item;
        } else {
            return item;
        }
    }
    public ItemStack updateStatsFromItem(ItemStack item, float multiplier, MMOItem mmoItem) {
        if (!item.hasItemMeta() || !item.getItemMeta().hasLore()) {
            return item;
        }
        List<String> statBlock = StatUpdates.getUpdatedStatBlock(new ArrayList<>(mmoItem.statBlock), multiplier);
        ItemMeta meta = item.getItemMeta();
        meta.setLore(MMOItem.assembleFullLore(mmoItem,statBlock,mmoItem.abilityBlock));
        item.setItemMeta(meta);
        return item;
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
