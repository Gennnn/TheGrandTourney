package me.genn.thegrandtourney.player;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StatUpdates {

    public static void updateStatsFromItem(ItemStack item, MMOPlayer mmoPlayer, Map<String, Map<String, Float>> fullChangeList) {
        float strChange = 0;
        float critDamChange = 0;
        float speedChange = 0;
        float critChanceChange = 0;
        float healthChange = 0;
        float defenseChange = 0;
        float manaChange = 0;
        float abilityPowerChange = 0;
        float discountChange = 0;
        float talkSpeedChange = 0;
        float healthRegenChange = 0;
        float manaRegenChange = 0;
        float attackSpeedChange = 0;
        float lureChange = 0;
        float flashChange = 0;
        Map<String, Float> changes = new HashMap<>();
        if (!item.hasItemMeta() || !item.getItemMeta().hasLore()) {
            return;
        }
        Iterator loreIter = item.getItemMeta().getLore().iterator();
        while (loreIter.hasNext()) {
            String line = ChatColor.stripColor((String) loreIter.next());
            if (ChatColor.stripColor(line).startsWith("Strength: +")) {
                line = line.replaceFirst("(Strength: +\\+)", "");
                strChange = Float.parseFloat(line);
                changes.put("strength", strChange);
            } else if (ChatColor.stripColor(line).startsWith("Strength: -")) {
                line = line.replaceFirst("(Strength: +\\-)", "");
                strChange = -Float.parseFloat(line);
                changes.put("strength", strChange);
            } else if (ChatColor.stripColor(line).startsWith("Crit Damage: +")) {
                line = line.replaceFirst("(Crit Damage: +\\+)", "");
                line = line.replaceFirst("(%)", "");
                critDamChange = Float.parseFloat(line);
                changes.put("critDamage", critDamChange);
            } else if (ChatColor.stripColor(line).startsWith("Crit Damage: -")) {
                line = line.replaceFirst("(Crit Damage: +\\-)", "");
                line = line.replaceFirst("(%)", "");
                critDamChange = -Float.parseFloat(line);
                changes.put("critDamage", critDamChange);
            } else if (ChatColor.stripColor(line).startsWith("Speed: +")) {
                line = line.replaceFirst("(Speed: +\\+)", "");
                speedChange = Float.parseFloat(line);
                changes.put("speed", speedChange);
            } else if (ChatColor.stripColor(line).startsWith("Speed: -")) {
                line = line.replaceFirst("(Speed: +\\-)", "");
                speedChange = -Float.parseFloat(line);
                changes.put("speed", speedChange);
            } else if (ChatColor.stripColor(line).startsWith("Crit Chance: +")) {
                line = line.replaceFirst("(Crit Chance: +\\+)", "");
                line = line.replaceFirst("(%)", "");
                critChanceChange = Float.parseFloat(line);
                changes.put("critChance", critChanceChange);
            } else if (ChatColor.stripColor(line).startsWith("Crit Chance: -")) {
                line = line.replaceFirst("(Crit Chance: +\\-)", "");
                line = line.replaceFirst("(%)", "");
                critChanceChange = -Float.parseFloat(line);
                changes.put("critChance", critChanceChange);
            } else if (ChatColor.stripColor(line).startsWith("Health: +")) {
                line = line.replaceFirst("(Health: +\\+)", "");
                healthChange = Float.parseFloat(line);
                changes.put("health", healthChange);
            } else if (ChatColor.stripColor(line).startsWith("Health: -")) {
                line = line.replaceFirst("(Health: +\\-)", "");
                healthChange = -Float.parseFloat(line);
                changes.put("health", healthChange);
            } else if (ChatColor.stripColor(line).startsWith("Defense: +")) {
                line = line.replaceFirst("(Defense: +\\+)", "");
                defenseChange = Float.parseFloat(line);
                changes.put("defense", defenseChange);
            } else if (ChatColor.stripColor(line).startsWith("Defense: -")) {
                line = line.replaceFirst("(Defense: +\\-)", "");
                defenseChange = -Float.parseFloat(line);
                changes.put("defense", defenseChange);
            } else if (ChatColor.stripColor(line).startsWith("Mana: +")) {
                line = line.replaceFirst("(Mana: +\\+)", "");
                manaChange = Float.parseFloat(line);
                changes.put("mana", manaChange);
            } else if (ChatColor.stripColor(line).startsWith("Mana: -")) {
                line = line.replaceFirst("(Mana: +\\-)", "");
                manaChange = -Float.parseFloat(line);
                changes.put("mana", manaChange);
            } else if (ChatColor.stripColor(line).startsWith("Ability Damage: +")) {
                line = line.replaceFirst("(Ability Damage: +\\+)", "");
                line = line.replaceFirst("(%)", "");
                abilityPowerChange = Float.parseFloat(line);
                changes.put("abilityPower", abilityPowerChange);
            } else if (ChatColor.stripColor(line).startsWith("Ability Damage: -")) {
                line = line.replaceFirst("(Ability Damage: +\\-)", "");
                line = line.replaceFirst("(%)", "");
                abilityPowerChange = -Float.parseFloat(line);
                changes.put("abilityPower", abilityPowerChange);
            } else if (ChatColor.stripColor(line).startsWith("Shop Discount: +")) {
                line = line.replaceFirst("(Shop Discount: +\\+)", "");
                line = line.replaceFirst("(%)", "");
                discountChange = Float.parseFloat(line);
                changes.put("discount", discountChange);
            } else if (ChatColor.stripColor(line).startsWith("Shop Discount: -")) {
                line = line.replaceFirst("(Shop Discount: +\\-)", "");
                line = line.replaceFirst("(%)", "");
                discountChange = -Float.parseFloat(line);
                changes.put("discount", discountChange);
            } else if (ChatColor.stripColor(line).startsWith("Dialogue Speed: +")) {
                line = line.replaceFirst("(Dialogue Speed: +\\+)", "");
                talkSpeedChange = Float.parseFloat(line);
                changes.put("talkSpeed", talkSpeedChange);
            } else if (ChatColor.stripColor(line).startsWith("Dialogue Speed: -")) {
                line = line.replaceFirst("(Dialogue Speed: +\\-)", "");
                talkSpeedChange = -Float.parseFloat(line);
                changes.put("talkSpeed", talkSpeedChange);
            } else if (ChatColor.stripColor(line).startsWith("Health Regen: +")) {
                line = line.replaceFirst("(Health Regen: +\\+)", "");
                healthRegenChange = Float.parseFloat(line);
                changes.put("healthRegen", healthRegenChange);
            } else if (ChatColor.stripColor(line).startsWith("Health Regen: -")) {
                line = line.replaceFirst("(Health Regen: +\\-)", "");
                healthRegenChange = -Float.parseFloat(line);
                changes.put("healthRegen", healthRegenChange);
            } else if (ChatColor.stripColor(line).startsWith("Mana Regen: +")) {
                line = line.replaceFirst("(Mana Regen: +\\+)", "");
                manaRegenChange = Float.parseFloat(line);
                changes.put("manaRegen", manaRegenChange);
            } else if (ChatColor.stripColor(line).startsWith("Mana Regen: -")) {
                line = line.replaceFirst("(Mana Regen: +\\-)", "");
                manaRegenChange = -Float.parseFloat(line);
                changes.put("manaRegen", manaRegenChange);
            } else if (ChatColor.stripColor(line).startsWith("Attack Speed: +")) {
                line = line.replaceFirst("(Attack Speed: +\\+)", "");
                attackSpeedChange = Float.parseFloat(line);
                changes.put("attackSpeed", attackSpeedChange);
            } else if (ChatColor.stripColor(line).startsWith("Attack Speed: -")) {
                line = line.replaceFirst("(Attack Speed: +\\-)", "");
                attackSpeedChange = -Float.parseFloat(line);
                changes.put("attackSpeed", attackSpeedChange);
            } else if (ChatColor.stripColor(line).startsWith("Fishing Speed: +")) {
                line = line.replaceFirst("(Fishing Speed: +\\+)", "");
                lureChange = Float.parseFloat(line);
                changes.put("lure", lureChange);
            } else if (ChatColor.stripColor(line).startsWith("Fishing Speed: -")) {
                line = line.replaceFirst("(Fishing Speed: +\\-)", "");
                lureChange = -Float.parseFloat(line);
                changes.put("lure", lureChange);
            } else if (ChatColor.stripColor(line).startsWith("Lure: +")) {
                line = line.replaceFirst("(Lure: +\\+)", "");
                flashChange = Float.parseFloat(line);
                changes.put("flash", flashChange);
            } else if (ChatColor.stripColor(line).startsWith("Lure: -")) {
                line = line.replaceFirst("(Lure: +\\-)", "");
                flashChange = -Float.parseFloat(line);
                changes.put("flash", flashChange);
            }

        }
        fullChangeList.put(item.getItemMeta().getDisplayName(), changes);
    }

    public void updateForArmorSlots(MMOPlayer mmoPlayer) {

    }

    public static void updateFullInventory(Player player, MMOPlayer mmoPlayer) {
        Map<String, Map<String, Float>> changeMap = new HashMap();
        float strChange = 0;
        float critDamChange = 0;
        float speedChange = 0;
        float critChanceChange = 0;
        float healthChange = 0;
        float defenseChange = 0;
        float manaChange = 0;
        float abilityPowerChange = 0;
        float discountChange = 0;
        float talkSpeedChange = 0;
        float healthRegenChange = 0;
        float manaRegenChange = 0;
        float attackSpeedChange = 0;
        float flashChange = 0;
        float lureChange = 0;
        updateStatsFromItem(player.getInventory().getItemInMainHand(), mmoPlayer, changeMap);
        if (player.getInventory().getHelmet() != null ) {
            updateStatsFromItem(player.getInventory().getHelmet(), mmoPlayer, changeMap);
        }
        if (player.getInventory().getChestplate() != null) {
            updateStatsFromItem(player.getInventory().getChestplate(), mmoPlayer, changeMap);
        }
        if (player.getInventory().getLeggings() != null) {
            updateStatsFromItem(player.getInventory().getLeggings(), mmoPlayer, changeMap);
        }
        if (player.getInventory().getBoots() != null) {
            updateStatsFromItem(player.getInventory().getBoots(), mmoPlayer, changeMap);
        }

        Iterator invIter = player.getInventory().iterator();
        while (invIter.hasNext()) {
            ItemStack item = (ItemStack) invIter.next();
            if (item != null) {
                NBTItem nbtI = new NBTItem(item);
                if (nbtI.hasTag("ExtraAttributes")) {
                    NBTCompound comp = nbtI.getCompound("ExtraAttributes");
                   if (comp.hasTag("charm")) {
                        updateStatsFromItem(item, mmoPlayer, changeMap);
                    }
                }
            }
        }
        Iterator accessoryIter = mmoPlayer.getAccessoryBagContents().iterator();
        while (accessoryIter.hasNext()) {
            ItemStack item = (ItemStack) accessoryIter.next();
            if (item != null) {
                updateStatsFromItem(item, mmoPlayer, changeMap);
            }
        }
        Iterator statsIter = changeMap.keySet().iterator();
        while (statsIter.hasNext()) {
            Map<String, Float> map = (Map<String, Float>) changeMap.get(statsIter.next());
            for (Map.Entry<String, Float> itr : map.entrySet()) {
                if (itr.getKey().equalsIgnoreCase("strength")) {
                    strChange = strChange + itr.getValue();
                } else if (itr.getKey().equalsIgnoreCase("critDamage")) {
                    critDamChange = critDamChange + itr.getValue();
                } else if (itr.getKey().equalsIgnoreCase("speed")) {
                    speedChange = speedChange + itr.getValue();
                } else if (itr.getKey().equalsIgnoreCase("critChance")) {
                    critChanceChange = critChanceChange + itr.getValue();
                } else if (itr.getKey().equalsIgnoreCase("health")) {
                    healthChange = healthChange + itr.getValue();
                } else if (itr.getKey().equalsIgnoreCase("defense")) {
                    defenseChange = defenseChange + itr.getValue();
                } else if (itr.getKey().equalsIgnoreCase("mana")) {
                    manaChange = manaChange + itr.getValue();
                } else if (itr.getKey().equalsIgnoreCase("abilityPower")) {
                    abilityPowerChange = abilityPowerChange + itr.getValue();
                } else if (itr.getKey().equalsIgnoreCase("discount")) {
                    discountChange = discountChange + itr.getValue();
                } else if (itr.getKey().equalsIgnoreCase("talkSpeed")) {
                    talkSpeedChange = talkSpeedChange + itr.getValue();
                } else if (itr.getKey().equalsIgnoreCase("healthRegen")) {
                    healthRegenChange = healthRegenChange + itr.getValue();
                } else if (itr.getKey().equalsIgnoreCase("manaRegen")) {
                    manaRegenChange = manaRegenChange + itr.getValue();
                } else if (itr.getKey().equalsIgnoreCase("attackSpeed")) {
                    attackSpeedChange = attackSpeedChange + itr.getValue();
                } else if (itr.getKey().equalsIgnoreCase("lure")) {
                    lureChange = lureChange + itr.getValue();
                } else if (itr.getKey().equalsIgnoreCase("flash")) {
                    flashChange = flashChange + itr.getValue();
                }
            }
        }
        mmoPlayer.setStrength(mmoPlayer.getBaseStrength() + strChange);
        mmoPlayer.setCritDamage(mmoPlayer.getBaseCritDamage() + critDamChange);
        mmoPlayer.setSpeed(mmoPlayer.getBaseSpeed() + speedChange);
        mmoPlayer.setCritChance(mmoPlayer.getBaseCritChance() + critChanceChange);
        mmoPlayer.setMaxHealth(mmoPlayer.getBaseMaxHealth() + healthChange);
        mmoPlayer.setDefense(mmoPlayer.getBaseDefense() + defenseChange);

        mmoPlayer.setMaxMana(mmoPlayer.getBaseMaxMana() + manaChange);
        mmoPlayer.setAbilityDam(mmoPlayer.getBaseAbilityDamage() + abilityPowerChange);
        mmoPlayer.setVendorPrice(mmoPlayer.getBaseVendorPrice() + discountChange);
        mmoPlayer.setDialogueSpeed(mmoPlayer.getBaseDialogueSpeed() + talkSpeedChange);
        mmoPlayer.setManaRegen(mmoPlayer.getBaseManaRegen() + manaRegenChange);
        mmoPlayer.setHealthRegen(mmoPlayer.getBaseHealthRegen() + healthRegenChange);
        mmoPlayer.setAttackSpeed(mmoPlayer.getAttackSpeed() + attackSpeedChange);

        mmoPlayer.setFlash(mmoPlayer.getBaseFlash() + flashChange);
        mmoPlayer.setLure(mmoPlayer.getBaseLure() + lureChange);
    }


}
