//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package me.genn.thegrandtourney.shops;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.util.Util;

import java.lang.reflect.Array;
import java.util.*;
import java.util.logging.Level;

import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.item.MMOItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class ShopItem {
    String name;
    MMOItem item;
    public int cost;
    Map<MMOItem, Integer> costItems = new HashMap<>();
    String spell;
    boolean giveItem;
    boolean confirm;
    boolean closeAfter;
    TGT plugin;
    boolean quantifiable = false;
    String quality = "normal";

    public ShopItem() {
        this.plugin = JavaPlugin.getPlugin(TGT.class);
    }

    public static ShopItem create(ConfigurationSection config) {
        ShopItem item = new ShopItem();

        item.item = item.plugin.itemHandler.getMMOItemFromString(config.getString("item"));
        if (item.item == null) {
            return null;
        } else {
            item.name = item.item.displayName;
            item.cost = config.getInt("cost", 0);
            if (config.contains("cost-items")) {
                List<String> costItems = config.getStringList("cost-items");
                for (String costItem : costItems) {
                    String[] parts = costItem.split(" ");
                    if (parts.length < 2) {
                        Bukkit.getLogger().log(Level.SEVERE, "Cost items for " + item.name + " are invalid!");
                        return null;
                    }
                    MMOItem mmoItem = item.plugin.itemHandler.getMMOItemFromString(parts[0]);
                    if (mmoItem == null) {
                        Bukkit.getLogger().log(Level.SEVERE, "Cost items for " + item.name + " are invalid!");
                        return null;
                    }
                    try {
                        item.costItems.put(mmoItem, Integer.parseInt(parts[1]));
                    } catch (NumberFormatException e) {
                        Bukkit.getLogger().log(Level.SEVERE, "Cost items for " + item.name + " are invalid!");
                        return null;
                    }
                }
            }
            item.spell = config.getString("spell", "");
            item.giveItem = config.getBoolean("give-item", false);
            item.confirm = config.getBoolean("confirm", false);
            item.closeAfter = config.getBoolean("close-after", true);
            item.quantifiable = config.getBoolean("quantity-options", false);
            if (config.contains("quality")) {
                item.quality = config.getString("quality");
            }
            return item;
        }
    }

    public List<String> getCostLore() {
        List<String> costLore = new ArrayList<>();
        costLore.add(" ");
        costLore.add(ChatColor.GRAY + "Cost:");
        if (cost == 0 && costItems.size() < 1) {
            costLore.add(ChatColor.GOLD + "Free!");
        } else {
            if (cost > 0) {
                costLore.add(ChatColor.GOLD.toString() + cost + " Dosh");
            }
            if (costItems.size() > 0) {
                for (MMOItem costItem : costItems.keySet()) {
                    String costStr = costItem.displayName;
                    if (costItems.get(costItem) > 1) {
                        costStr = costStr + " " + ChatColor.DARK_GRAY + "x" + costItems.get(costItem);
                    }
                    costLore.add(costStr);
                }
            }
        }
        return costLore;
    }
    public List<String> getCostLore(int quantityMult) {
        List<String> costLore = new ArrayList<>();
        costLore.add(" ");
        costLore.add(ChatColor.GRAY + "Cost:");
        if (cost == 0 && costItems.size() < 1) {
            costLore.add(ChatColor.GOLD + "Free!");
        } else {
            if (cost > 0) {
                costLore.add(ChatColor.GOLD.toString() + (cost * quantityMult) + " Dosh");
            }
            if (costItems.size() > 0) {
                for (MMOItem costItem : costItems.keySet()) {
                    String costStr = costItem.displayName;
                    if (costItems.get(costItem) > 1) {
                        costStr = costStr + " " + ChatColor.DARK_GRAY + "x" + (costItems.get(costItem) * quantityMult);
                    }
                    costLore.add(costStr);
                }
            }
        }
        return costLore;
    }
    public ItemStack getShopItem() {
        ItemStack item = null;
        if (!this.quality.equalsIgnoreCase("normal")) {
            item = plugin.itemHandler.getItemWithQuality(this.item, this.quality).asQuantity(1);
        } else {
            item = plugin.itemHandler.getItem(this.item).asQuantity(1);
        }
        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<>(meta.getLore());
        lore.addAll(getCostLore());
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
        item.setItemMeta(meta);
        return item;
    }
    public ItemStack getShopItem(int quantity) {
        ItemStack item = null;
        if (!this.quality.equalsIgnoreCase("normal")) {
            item = plugin.itemHandler.getItemWithQuality(this.item, this.quality).asQuantity(quantity);
        } else {
            item = plugin.itemHandler.getItem(this.item).asQuantity(quantity);
        }
        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<>(meta.getLore());
        lore.addAll(getCostLore(quantity));
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
        item.setItemMeta(meta);
        return item;
    }
}
