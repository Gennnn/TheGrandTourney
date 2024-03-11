package me.genn.thegrandtourney.item;


import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;


import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;

import me.genn.thegrandtourney.TGT;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;


import net.md_5.bungee.api.ChatColor;

import org.bukkit.profile.PlayerTextures;
import org.checkerframework.checker.units.qual.N;

public class MMOItem {
    public String internalName;
    public ItemStack bukkitItem;
    public ItemMeta bukkitItemMeta;
    public Rarity rarity;
    public String displayName;
    public List<String> lore;
    public int durability;
    public boolean unique;

    public String colorStr;

    public boolean enchantGlint;
    public String textureStr;
    public boolean isCharm;
    public Spell spell;
    public Spell lSpell;
    public Spell activateSpell;



    public static MMOItem create(ConfigurationSection config) throws IOException {
        MMOItem item = new MMOItem();
        TGT plugin = JavaPlugin.getPlugin(TGT.class);
        if (config.getString("item") == null) {
            return null;
        }
        item.bukkitItem = new ItemStack(Material.matchMaterial("minecraft:" + config.getString("item", "apple")));
        item.bukkitItemMeta = item.bukkitItem.getItemMeta();
        if (item.bukkitItem.getType().getMaxDurability() > 0) {
            item.bukkitItemMeta.setUnbreakable(true);
        }

        if (item.bukkitItemMeta.hasAttributeModifiers() && item.bukkitItemMeta.getAttributeModifiers(Attribute.GENERIC_ATTACK_SPEED) != null) {
            item.bukkitItemMeta.removeAttributeModifier(Attribute.GENERIC_ATTACK_SPEED);
        }
        item.bukkitItemMeta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, new AttributeModifier("generic.attackSpeed", config.getDouble("attack-speed", 1), AttributeModifier.Operation.ADD_NUMBER));

        item.internalName = config.getName();
        item.rarity = Rarity.valueOf((config.getString("rarity", "common")).toUpperCase());
        item.displayName = getRarityColor(item.rarity) + config.getString("name", item.bukkitItemMeta.getDisplayName());
        item.durability = config.getInt("durability", 10);
        item.lore = config.getStringList("lore");
        for (int i = 0; i < item.lore.size() ; i++) {
            item.lore.set(i, ChatColor.translateAlternateColorCodes('&',item.lore.get(i)));
        }
        String spellName = config.getString("spell");
        if (spellName != null) {
            item.spell = MagicSpells.getSpellByInternalName(spellName);
        }
        String leftSpellName = config.getString("lclick-spell");
        if (leftSpellName != null) {
            item.lSpell = MagicSpells.getSpellByInternalName(leftSpellName);
        }
        String activateSpellName = config.getString("activate-spell");
        if (activateSpellName != null) {
            item.activateSpell = MagicSpells.getSpellByInternalName(activateSpellName);
        }
        item.bukkitItemMeta.setLore(item.lore);
        item.unique = config.getBoolean("unique", false);
        item.enchantGlint = config.getBoolean("enchanted", false);
        if (item.enchantGlint) {
            addFakeEnchantment(item.bukkitItemMeta);
        }
        if (item.bukkitItemMeta.hasAttributeModifiers() && item.bukkitItemMeta.getAttributeModifiers(Attribute.GENERIC_ATTACK_SPEED) != null) {
            item.bukkitItemMeta.removeAttributeModifier(Attribute.GENERIC_ATTACK_SPEED);
        }
        item.bukkitItemMeta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, new AttributeModifier("generic.attackSpeed", 0.0F, AttributeModifier.Operation.ADD_NUMBER));
        Iterator loreIter = item.lore.iterator();
        while (loreIter.hasNext()) {
            String line = org.bukkit.ChatColor.stripColor((String) loreIter.next());
            if (org.bukkit.ChatColor.stripColor(line).startsWith("Swing Speed: +")) {
                line = line.replaceFirst("(Swing Speed: +\\+)", "");

                float attackSpeedChange = Float.parseFloat(line);
                if (item.bukkitItemMeta.hasAttributeModifiers() && item.bukkitItemMeta.getAttributeModifiers(Attribute.GENERIC_ATTACK_SPEED) != null) {
                    item.bukkitItemMeta.removeAttributeModifier(Attribute.GENERIC_ATTACK_SPEED);
                }
                attackSpeedChange = (float) (0.0F + (attackSpeedChange * (3.5/10)));
                item.bukkitItemMeta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, new AttributeModifier("generic.attackSpeed", attackSpeedChange, AttributeModifier.Operation.ADD_NUMBER));
            } else if (org.bukkit.ChatColor.stripColor(line).startsWith("Swing Speed: -")) {
                line = line.replaceFirst("(Swing Speed: +\\-)", "");
                float attackSpeedChange = -Float.parseFloat(line);
                if (item.bukkitItemMeta.hasAttributeModifiers() && item.bukkitItemMeta.getAttributeModifiers(Attribute.GENERIC_ATTACK_SPEED) != null) {
                    item.bukkitItemMeta.removeAttributeModifier(Attribute.GENERIC_ATTACK_SPEED);
                }
                attackSpeedChange = (float) (0.0F + (attackSpeedChange * (3.5/10)));
                item.bukkitItemMeta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, new AttributeModifier("generic.attackSpeed", attackSpeedChange, AttributeModifier.Operation.ADD_NUMBER));
            }

        }
        item.bukkitItemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.bukkitItemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        item.bukkitItemMeta.setDisplayName(item.displayName);
        item.bukkitItem.setItemMeta(item.bukkitItemMeta);
        item.colorStr = config.getString("color");
        if (item.colorStr != null) {
            if (item.bukkitItem.getType() == Material.LEATHER_HELMET || item.bukkitItem.getType() == Material.LEATHER_CHESTPLATE || item.bukkitItem.getType() == Material.LEATHER_LEGGINGS || item.bukkitItem.getType() == Material.LEATHER_BOOTS) {
                LeatherArmorMeta meta = (LeatherArmorMeta) item.bukkitItem.getItemMeta();
                int r = Integer.valueOf(item.colorStr.substring(0, 2), 16);
                int g = Integer.valueOf(item.colorStr.substring(2, 4), 16);
                int b = Integer.valueOf(item.colorStr.substring(4, 6), 16);
                Color color = Color.fromRGB(r,g,b);
                meta.setColor(color);
                item.bukkitItem.setItemMeta(meta);
            }

        }
        item.textureStr = config.getString("head-texture");
        if (item.textureStr != null && item.bukkitItem.getType() == Material.PLAYER_HEAD) {
            getHeadFrom64(item.textureStr, item.bukkitItem);
        }
        NBTItem nbtI = new NBTItem(item.bukkitItem);
        nbtI.addCompound("ExtraAttributes");
        NBTCompound comp = nbtI.getCompound("ExtraAttributes");
        comp.setString("id", item.internalName.toUpperCase());
        item.isCharm = config.getBoolean("charm");
        if (item.isCharm) {
            comp.setBoolean("charm", true);
        }
        item.bukkitItem = nbtI.getItem();
        return item;
    }



    public String getName() {
        return this.internalName;
    }

    public enum Rarity {
        COMMON,UNCOMMON,RARE,EPIC,LEGENDARY
    }

    public static ChatColor getRarityColor(Rarity rarity) {
        switch (rarity) {
            case UNCOMMON -> {
                return ChatColor.GREEN;
            }
            case RARE -> {
                return ChatColor.BLUE;

            }
            case EPIC -> {
                return ChatColor.DARK_PURPLE;
            }
            case LEGENDARY -> {
                return ChatColor.GOLD;
            }
            default -> {
                return ChatColor.WHITE;
            }
        }

    }

    public static void addFakeEnchantment(ItemMeta meta) {
        if (meta != null) {
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.addEnchant(Enchantment.FROST_WALKER, -1, true);
        }
    }

    public static void getHeadFrom64(String value, ItemStack skull) {
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
        profile.setProperty(new ProfileProperty("textures", value));
        meta.setPlayerProfile(profile);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        skull.setItemMeta(meta);
    }

    public static void removeItem(Player player, MMOItem item, int quantity) {
        if (player.getInventory().getContents().length <= 0) {
            return;
        }
        ItemStack[] inv = player.getInventory().getContents();
        for (ItemStack i : inv) {
            if (i != null) {
                if (i.getType() == item.bukkitItem.getType() && i.hasItemMeta()) {
                    NBTItem nbtI = new NBTItem(i);
                    if (nbtI.hasTag("ExtraAttributes")) {
                        if (nbtI.getCompound("ExtraAttributes").hasTag("id") && nbtI.getCompound("ExtraAttributes").getString("id").equalsIgnoreCase(item.internalName)) {
                            player.getInventory().removeItem(i.asQuantity(quantity));
                        }
                    }
                }
            }
        }
    }


}
