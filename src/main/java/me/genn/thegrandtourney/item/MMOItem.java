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

import me.genn.thegrandtourney.skills.Recipe;
import me.genn.thegrandtourney.xp.Xp;
import me.genn.thegrandtourney.xp.XpType;
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
import org.bukkit.inventory.meta.PotionMeta;
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
    public List<String> statBlock;
    public List<String> abilityBlock;
    public String categoryString;
    public int durability;
    public boolean unique;

    public String colorStr;

    public boolean enchantGlint;
    public String textureStr;
    public boolean isCharm;
    public String spellName;
    public String lSpellName;
    public String activateSpellName;
    public XpType typeRequirement;
    public int lvlRequirement;
    public float abilityScaling;
    public float[] qualityScaling;
    public boolean consumable;
    public Color color;
    public float sellPrice;



    public static MMOItem create(ConfigurationSection config) throws IOException {
        MMOItem item = new MMOItem();
        TGT plugin = JavaPlugin.getPlugin(TGT.class);
        if (config.getString("item") == null) {
            return null;
        }
        item.bukkitItem = new ItemStack(Material.matchMaterial("minecraft:" + config.getString("item", "apple")));
        item.bukkitItemMeta = item.bukkitItem.getItemMeta();
        item.qualityScaling = new float[]{1.1f,1.2f,1.3f};
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

        item.statBlock = config.getStringList("stat-block");
        if (item.statBlock != null) {
            item.statBlock.replaceAll(textToTranslate -> ChatColor.translateAlternateColorCodes('&', textToTranslate));

        }

        item.abilityBlock = config.getStringList("ability-block");
        if (item.abilityBlock != null) {
            item.abilityBlock.replaceAll(textToTranslate -> ChatColor.translateAlternateColorCodes('&', textToTranslate));
        }

        item.consumable = config.getBoolean("consumable", false);

        item.categoryString = config.getString("category");
        item.spellName = config.getString("spell");

        item.lSpellName = config.getString("lclick-spell");

        item.activateSpellName = config.getString("activate-spell");
        item.sellPrice = (float) config.getDouble("sell-price", 1.0);
        item.bukkitItemMeta.setLore(MMOItem.assembleFullLore(item, item.statBlock, item.abilityBlock));
        item.unique = config.getBoolean("unique", false);
        item.enchantGlint = config.getBoolean("enchanted", false);
        if (item.enchantGlint) {
            addFakeEnchantment(item.bukkitItemMeta);
        }
        if (item.bukkitItemMeta.hasAttributeModifiers() && item.bukkitItemMeta.getAttributeModifiers(Attribute.GENERIC_ATTACK_SPEED) != null) {
            item.bukkitItemMeta.removeAttributeModifier(Attribute.GENERIC_ATTACK_SPEED);
        }
        item.abilityScaling = (float) config.getDouble("ability-scaling", 0.2);
        item.bukkitItemMeta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, new AttributeModifier("generic.attackSpeed", 0.0F, AttributeModifier.Operation.ADD_NUMBER));
        Iterator loreIter = item.statBlock.iterator();
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
        item.bukkitItemMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        item.bukkitItemMeta.setDisplayName(item.displayName);
        item.bukkitItem.setItemMeta(item.bukkitItemMeta);
        if (config.contains("type-requirement")) {
            item.typeRequirement = Xp.parseXpType(config.getString("type-requirement"));
            item.lvlRequirement = config.getInt("level-requirement", 1);
        }


        item.colorStr = config.getString("color");
        if (item.colorStr != null) {
            item.color = Color.fromRGB(Integer.parseInt(item.colorStr, 16));
            if (item.bukkitItem.getType() == Material.LEATHER_HELMET || item.bukkitItem.getType() == Material.LEATHER_CHESTPLATE || item.bukkitItem.getType() == Material.LEATHER_LEGGINGS || item.bukkitItem.getType() == Material.LEATHER_BOOTS) {
                LeatherArmorMeta meta = (LeatherArmorMeta) item.bukkitItem.getItemMeta();
                meta.setColor(item.color);
                item.bukkitItem.setItemMeta(meta);
            } else if (item.bukkitItem.getType() == Material.POTION) {
                PotionMeta meta = (PotionMeta) item.bukkitItem.getItemMeta();
                meta.setColor(item.color);
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
        item.isCharm = config.getBoolean("accessory");
        if (item.isCharm) {
            comp.setBoolean("accessory", true);
        }
        item.bukkitItem = nbtI.getItem();
        if (config.contains("recipe")) {
            ConfigurationSection section = config.getConfigurationSection("recipe");
            Recipe recipe = Recipe.create(section, plugin, item.internalName);
            plugin.itemHandler.allRecipes.add(recipe);
        }
        if (config.contains("bronze-scaling")) {
            item.qualityScaling[0] = (float) config.getDouble("bronze-scaling");
        }
        if (config.contains("silver-scaling")) {
            item.qualityScaling[1] = (float) config.getDouble("silver-scaling");
        }
        if (config.contains("gold-scaling")) {
            item.qualityScaling[2] = (float) config.getDouble("gold-scaling");
        }
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
        if (player.getInventory().getContents().length == 0) {
            return;
        }

        ItemStack[] inv = player.getInventory().getContents();
        for (ItemStack i : inv) {
            if (quantity <= 0) {
                return;
            }
            if (i != null) {
                if (i.getType() == item.bukkitItem.getType() && i.hasItemMeta()) {
                    NBTItem nbtI = new NBTItem(i);
                    if (nbtI.hasTag("ExtraAttributes")) {
                        if (nbtI.getCompound("ExtraAttributes").hasTag("id") && nbtI.getCompound("ExtraAttributes").getString("id").equalsIgnoreCase(item.internalName)) {
                            int stack = i.getAmount();
                            if (stack >= quantity) {
                                player.getInventory().removeItem(i.asQuantity(quantity));
                                quantity = 0;
                            } else {
                                quantity = quantity - stack;
                                player.getInventory().removeItem(i.asQuantity(stack));
                            }
                        }
                    }
                }
            }
        }
    }
    public static List<String> assembleFullLore(MMOItem item, List<String> masterStatBlock, List<String> masterAbilityBlock) {
        List<String> lore = new ArrayList<>();
        List<String> statBlock = new ArrayList<>(masterStatBlock);
        List<String> abilityBlock = new ArrayList<>(masterAbilityBlock);
        if (statBlock.size() > 0) {
            lore.addAll(statBlock);
            lore.add("");
        }
        if (abilityBlock.size() > 0) {
            for (int i = 0; i < abilityBlock.size(); ++i) {
                if (abilityBlock.get(i).contains("%ad:")) {
                    abilityBlock.set(i, abilityBlock.get(i).replaceAll("%ad:", ""));
                }
            }
            lore.addAll(abilityBlock);
            lore.add("");
        }
        String finalString = getRarityColor(item.rarity) + ChatColor.BOLD.toString() + item.rarity.toString().toUpperCase();
        if (item.categoryString != null) {
            finalString += " " + item.categoryString.toUpperCase();
        }
        lore.add(finalString);
        return lore;
    }



}
