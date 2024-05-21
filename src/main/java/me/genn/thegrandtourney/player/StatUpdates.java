package me.genn.thegrandtourney.player;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.item.MMOItem;
import me.genn.thegrandtourney.xp.Xp;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class StatUpdates {
    TGT plugin;

    public StatUpdates(TGT plugin) {
        this.plugin = plugin;
    }

    public static void updateStatsFromItem(ItemStack item, Map<String, Map<String, Float>> fullChangeList) {
        float change;
        Map<String, Float> changes = new HashMap<>();
        if (!item.hasItemMeta() || !item.getItemMeta().hasLore()) {
            return;
        }
        Iterator<String> loreIter = item.getItemMeta().getLore().iterator();
        while (loreIter.hasNext()) {
            String line = ChatColor.stripColor((String) loreIter.next());
            if (ChatColor.stripColor(line).startsWith("Strength: +")) {
                line = line.replaceFirst("(Strength: +\\+)", "");
                change = Float.parseFloat(line);
                changes.put("strength", change);
            } else if (ChatColor.stripColor(line).startsWith("Strength: -")) {
                line = line.replaceFirst("(Strength: +\\-)", "");
                change = -Float.parseFloat(line);
                changes.put("strength", change);
            } else if (ChatColor.stripColor(line).startsWith("Crit Damage: +")) {
                line = line.replaceFirst("(Crit Damage: +\\+)", "");
                line = line.replaceFirst("(%)", "");
                change = Float.parseFloat(line);
                changes.put("critDamage", change);
            } else if (ChatColor.stripColor(line).startsWith("Crit Damage: -")) {
                line = line.replaceFirst("(Crit Damage: +\\-)", "");
                line = line.replaceFirst("(%)", "");
                change = -Float.parseFloat(line);
                changes.put("critDamage", change);
            } else if (ChatColor.stripColor(line).startsWith("Speed: +")) {
                line = line.replaceFirst("(Speed: +\\+)", "");
                change = Float.parseFloat(line);
                changes.put("speed", change);
            } else if (ChatColor.stripColor(line).startsWith("Speed: -")) {
                line = line.replaceFirst("(Speed: +\\-)", "");
                change = -Float.parseFloat(line);
                changes.put("speed", change);
            } else if (ChatColor.stripColor(line).startsWith("Crit Chance: +")) {
                line = line.replaceFirst("(Crit Chance: +\\+)", "");
                line = line.replaceFirst("(%)", "");
                change = Float.parseFloat(line);
                changes.put("critChance", change);
            } else if (ChatColor.stripColor(line).startsWith("Crit Chance: -")) {
                line = line.replaceFirst("(Crit Chance: +\\-)", "");
                line = line.replaceFirst("(%)", "");
                change = -Float.parseFloat(line);
                changes.put("critChance", change);
            } else if (ChatColor.stripColor(line).startsWith("Health: +")) {
                line = line.replaceFirst("(Health: +\\+)", "");
                change = Float.parseFloat(line);
                changes.put("health", change);
            } else if (ChatColor.stripColor(line).startsWith("Health: -")) {
                line = line.replaceFirst("(Health: +\\-)", "");
                change = -Float.parseFloat(line);
                changes.put("health", change);
            } else if (ChatColor.stripColor(line).startsWith("Defense: +")) {
                line = line.replaceFirst("(Defense: +\\+)", "");
                change = Float.parseFloat(line);
                changes.put("defense", change);
            } else if (ChatColor.stripColor(line).startsWith("Defense: -")) {
                line = line.replaceFirst("(Defense: +\\-)", "");
                change = -Float.parseFloat(line);
                changes.put("defense", change);
            } else if (ChatColor.stripColor(line).startsWith("Stamina: +")) {
                line = line.replaceFirst("(Stamina: +\\+)", "");
                change = Float.parseFloat(line);
                changes.put("mana", change);
            } else if (ChatColor.stripColor(line).startsWith("Stamina: -")) {
                line = line.replaceFirst("(Stamina: +\\-)", "");
                change = -Float.parseFloat(line);
                changes.put("mana", change);
            } else if (ChatColor.stripColor(line).startsWith("Ability Damage: +")) {
                line = line.replaceFirst("(Ability Damage: +\\+)", "");
                line = line.replaceFirst("(%)", "");
                change = Float.parseFloat(line);
                changes.put("abilityPower", change);
            } else if (ChatColor.stripColor(line).startsWith("Ability Damage: -")) {
                line = line.replaceFirst("(Ability Damage: +\\-)", "");
                line = line.replaceFirst("(%)", "");
                change = -Float.parseFloat(line);
                changes.put("abilityPower", change);
            } else if (ChatColor.stripColor(line).startsWith("Shop Discount: +")) {
                line = line.replaceFirst("(Shop Discount: +\\+)", "");
                line = line.replaceFirst("(%)", "");
                change = Float.parseFloat(line);
                changes.put("discount", change);
            } else if (ChatColor.stripColor(line).startsWith("Shop Discount: -")) {
                line = line.replaceFirst("(Shop Discount: +\\-)", "");
                line = line.replaceFirst("(%)", "");
                change = -Float.parseFloat(line);
                changes.put("discount", change);
            } else if (ChatColor.stripColor(line).startsWith("Dialogue Speed: +")) {
                line = line.replaceFirst("(Dialogue Speed: +\\+)", "");
                change = Float.parseFloat(line);
                changes.put("talkSpeed", change);
            } else if (ChatColor.stripColor(line).startsWith("Dialogue Speed: -")) {
                line = line.replaceFirst("(Dialogue Speed: +\\-)", "");
                change = -Float.parseFloat(line);
                changes.put("talkSpeed", change);
            } else if (ChatColor.stripColor(line).startsWith("Health Regen: +")) {
                line = line.replaceFirst("(Health Regen: +\\+)", "");
                change = Float.parseFloat(line);
                changes.put("healthRegen", change);
            } else if (ChatColor.stripColor(line).startsWith("Health Regen: -")) {
                line = line.replaceFirst("(Health Regen: +\\-)", "");
                change = -Float.parseFloat(line);
                changes.put("healthRegen", change);
            } else if (ChatColor.stripColor(line).startsWith("Mana Regen: +")) {
                line = line.replaceFirst("(Mana Regen: +\\+)", "");
                change = Float.parseFloat(line);
                changes.put("manaRegen", change);
            } else if (ChatColor.stripColor(line).startsWith("Mana Regen: -")) {
                line = line.replaceFirst("(Mana Regen: +\\-)", "");
                change = -Float.parseFloat(line);
                changes.put("manaRegen", change);
            } else if (ChatColor.stripColor(line).startsWith("Attack Speed: +")) {
                line = line.replaceFirst("(Attack Speed: +\\+)", "");
                change = Float.parseFloat(line);
                changes.put("attackSpeed", change);
            } else if (ChatColor.stripColor(line).startsWith("Attack Speed: -")) {
                line = line.replaceFirst("(Attack Speed: +\\-)", "");
                change = -Float.parseFloat(line);
                changes.put("attackSpeed", change);
            } else if (ChatColor.stripColor(line).startsWith("Fishing Speed: +")) {
                line = line.replaceFirst("(Fishing Speed: +\\+)", "");
                change = Float.parseFloat(line);
                changes.put("lure", change);
            } else if (ChatColor.stripColor(line).startsWith("Fishing Speed: -")) {
                line = line.replaceFirst("(Fishing Speed: +\\-)", "");
                change = -Float.parseFloat(line);
                changes.put("lure", change);
            } else if (ChatColor.stripColor(line).startsWith("Lure: +")) {
                line = line.replaceFirst("(Lure: +\\+)", "");
                change = Float.parseFloat(line);
                changes.put("flash", change);
            } else if (ChatColor.stripColor(line).startsWith("Lure: -")) {
                line = line.replaceFirst("(Lure: +\\-)", "");
                change = -Float.parseFloat(line);
                changes.put("flash", change);
            } else if (ChatColor.stripColor(line).startsWith("Focus: +")) {
                line = line.replaceFirst("(Focus: +\\+)", "");
                change = Float.parseFloat(line);
                changes.put("focus", change);
            } else if (ChatColor.stripColor(line).startsWith("Focus: -")) {
                line = line.replaceFirst("(Focus: +\\-)", "");
                change = -Float.parseFloat(line);
                changes.put("focus", change);
            } else if (ChatColor.stripColor(line).startsWith("Vigor: +")) {
                line = line.replaceFirst("(Vigor: +\\+)", "");
                change = Float.parseFloat(line);
                changes.put("vigor", change);
            } else if (ChatColor.stripColor(line).startsWith("Vigor: -")) {
                line = line.replaceFirst("(Vigor: +\\-)", "");
                change = -Float.parseFloat(line);
                changes.put("vigor", change);
            } else if (ChatColor.stripColor(line).startsWith("Sea Creature Chance: +")) {
                line = line.replaceFirst("(Sea Creature Chance: +\\+)", "");
                line = line.replaceFirst("(%)", "");
                change = Float.parseFloat(line);
                changes.put("seaCreatureChance", change);
            } else if (ChatColor.stripColor(line).startsWith("Sea Creature Chance: -")) {
                line = line.replaceFirst("(Sea Creature Chance: +\\-)", "");
                line = line.replaceFirst("(%)", "");
                change = -Float.parseFloat(line);
                changes.put("seaCreatureChance", change);
            } else if (ChatColor.stripColor(line).startsWith("Farming Fortune: +")) {
                line = line.replaceFirst("(Farming Fortune: +\\+)", "");
                change = Float.parseFloat(line);
                changes.put("farmingFortune", change);
            } else if (ChatColor.stripColor(line).startsWith("Farming Fortune: -")) {
                line = line.replaceFirst("(Farming Fortune: +\\-)", "");
                change = -Float.parseFloat(line);
                changes.put("farmingFortune", change);
            } else if (ChatColor.stripColor(line).startsWith("Foraging Fortune: +")) {
                line = line.replaceFirst("(Foraging Fortune: +\\+)", "");
                change = Float.parseFloat(line);
                changes.put("foragingFortune", change);
            } else if (ChatColor.stripColor(line).startsWith("Foraging Fortune: -")) {
                line = line.replaceFirst("(Foraging Fortune: +\\-)", "");
                change = -Float.parseFloat(line);
                changes.put("foragingFortune", change);
            } else if (ChatColor.stripColor(line).startsWith("Mining Fortune: +")) {
                line = line.replaceFirst("(Mining Fortune: +\\+)", "");
                change = Float.parseFloat(line);
                changes.put("miningFortune", change);
            } else if (ChatColor.stripColor(line).startsWith("Mining Fortune: -")) {
                line = line.replaceFirst("(Mining Fortune: +\\-)", "");
                change = -Float.parseFloat(line);
                changes.put("miningFortune", change);
            } else if (ChatColor.stripColor(line).startsWith("Luck: +")) {
                line = line.replaceFirst("(Luck: +\\+)", "");
                change = Float.parseFloat(line);
                changes.put("luck", change);
            } else if (ChatColor.stripColor(line).startsWith("Luck: -")) {
                line = line.replaceFirst("(Luck: +\\-)", "");
                change = -Float.parseFloat(line);
                changes.put("luck", change);
            }

        }
        fullChangeList.put(item.getItemMeta().getDisplayName(), changes);
    }

    public void updateAbilityText(ItemStack item, Player player) {
        if (item == null || item.getType()  == Material.AIR) {
            return;
        }
        NBTItem nbtI = new NBTItem(item);
        if (!nbtI.hasTag("ExtraAttributes")) {
            return;
        }
        NBTCompound comp = nbtI.getCompound("ExtraAttributes");
        if (!comp.hasTag("id")) {
            return;
        }
        String id = comp.getString("id");
        if (plugin.itemHandler.getMMOItemFromString(id) == null) {
            return;
        }
        MMOItem mmoItem = plugin.itemHandler.getMMOItemFromString(id);
        List<String> statBlock = new ArrayList<>(mmoItem.statBlock);
        List<String> abilityBlock = new ArrayList<>(mmoItem.abilityBlock);
        if (mmoItem.abilityBlock.size() < 1) {
            return;
        }
        Iterator<String> abIter = abilityBlock.iterator();
        boolean pass = false;
        while (abIter.hasNext()) {
            String str = abIter.next();
            if (str.contains("%ad:")) {
                pass = true;
            }
        }
        if (!pass) {
            return;
        }
        for (int i = 0 ; i  < abilityBlock.size(); i++) {
            String[] words = abilityBlock.get(i).split(" ");
            for (String word : words) {
                String newWord = ChatColor.stripColor(word);
                if (newWord.startsWith("%ad:")) {
                    newWord = newWord.replace("%ad:" , "");
                    try {
                        double num = Double.parseDouble(newWord);
                        MMOPlayer mmoPlayer = plugin.players.get(player.getUniqueId());
                        num = calculateAbilityDamage((float)num, mmoPlayer, item);
                        String numString = String.format("%,.1f",num);
                        abilityBlock.set(i,abilityBlock.get(i).replaceFirst(word, ChatColor.RED + numString));
                        String fullString = "";
                        Iterator<String> iter = Objects.requireNonNull(item.getItemMeta().getLore()).iterator();
                        while (iter.hasNext()) {
                            String str = iter.next();
                            fullString = fullString.concat(ChatColor.stripColor(str) + " ");
                        }
                        if (fullString.contains(ChatColor.stripColor(numString + " damage")) || fullString.contains(ChatColor.stripColor(numString + " Damage"))) {
                            return;
                        }
                    } catch (NumberFormatException e) {
                        return;
                    }
                }
            }
        }

        if (nbtI.hasTag("statBoost")) {
            statBlock = StatUpdates.getUpdatedStatBlock(statBlock, nbtI.getFloat("statBoost"));
        }
        List<String> lore = new ArrayList<>();
        if (statBlock.size() > 0) {
            lore.addAll(statBlock);
            lore.add("");
        }
        if (abilityBlock.size() > 0) {
            lore.addAll(abilityBlock);
            lore.add("");
        }
        String finalString = MMOItem.getRarityColor(mmoItem.rarity) + ChatColor.BOLD.toString() + mmoItem.rarity.toString().toUpperCase();
        if (mmoItem.categoryString != null) {
            finalString += " " + mmoItem.categoryString.toUpperCase();
        }
        lore.add(finalString);
        ItemMeta meta = item.getItemMeta();
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    public void updateFullInventory(Player player, MMOPlayer mmoPlayer) {
        Map<String, Map<String, Float>> changeMap = new HashMap<>();
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
        float focusChange = 0;
        float vigorChange = 0;
        float seaCreatureChance = 0;
        float miningFortuneChange = 0;
        float farmingFortuneChange = 0;
        float foragingFortuneChange = 0;
        float evasivenessChange = 0;
        float luckChange = 0;
        if (plugin.listener.checkWieldingItemForLvlRequirements(player)) {
            updateStatsFromItem(player.getInventory().getItemInMainHand(), changeMap);
        }

        if (player.getInventory().getHelmet() != null ) {
            if (plugin.listener.checkItemForRequirements(player, player.getInventory().getHelmet())) {
                updateStatsFromItem(player.getInventory().getHelmet(), changeMap);
            }
        }
        if (player.getInventory().getChestplate() != null) {
            if (plugin.listener.checkItemForRequirements(player, player.getInventory().getChestplate())) {
                updateStatsFromItem(player.getInventory().getChestplate(), changeMap);
            }
        }
        if (player.getInventory().getLeggings() != null) {
            if (plugin.listener.checkItemForRequirements(player, player.getInventory().getLeggings())) {
                updateStatsFromItem(player.getInventory().getLeggings(), changeMap);
            }
        }
        if (player.getInventory().getBoots() != null) {
            if (plugin.listener.checkItemForRequirements(player, player.getInventory().getBoots())) {
                updateStatsFromItem(player.getInventory().getBoots(), changeMap);
            }
        }
        if (mmoPlayer.buffs.size() > 0) {
            for (StatBuff buff : mmoPlayer.buffs) {
                if (buff.expiryTime < System.currentTimeMillis()) {
                    mmoPlayer.buffs.remove(buff);
                    if (buff.statName.equalsIgnoreCase("absorption")) {
                        if (buff.amount > 0 && buff.added) {
                            mmoPlayer.setAbsorptionHealth(mmoPlayer.getAbsorptionHealth() - buff.amount);
                            if (mmoPlayer.getAbsorptionHealth() < 0) {
                                mmoPlayer.setAbsorptionHealth(0);
                            }
                        }
                        mmoPlayer.absorptionLayers.stream().filter(o -> o.expiryTime == buff.expiryTime).findFirst().ifPresent(removal -> mmoPlayer.absorptionLayers.remove(removal));
                        mmoPlayer.sortLayers();
                    }
                    if (buff.statName.equalsIgnoreCase("absorption") || buff.statName.equalsIgnoreCase("health")) {
                        plugin.updatePlayerHealth(mmoPlayer,0);
                    }

                    continue;
                }
                String statName = buff.statName;
                float amount = buff.amount;
                if (statName.equalsIgnoreCase("strength")) {
                    strChange = strChange + amount;
                } else if (statName.equalsIgnoreCase("defense")) {
                    defenseChange = defenseChange + amount;
                } else if (statName.equalsIgnoreCase("health")) {
                    healthChange = healthChange + amount;
                } else if (statName.equalsIgnoreCase("crit-damage")) {
                    critDamChange = critDamChange + amount;
                } else if (statName.equalsIgnoreCase("crit-chance")) {
                    critChanceChange = critChanceChange + amount;
                } else if (statName.equalsIgnoreCase("speed")) {
                    speedChange = speedChange + amount;
                } else if (statName.equalsIgnoreCase("vigor")) {
                    vigorChange = vigorChange + amount;
                } else if (statName.equalsIgnoreCase("stamina-regen")) {
                    manaRegenChange = manaRegenChange + amount;
                } else if (statName.equalsIgnoreCase("health-regen")) {
                    healthRegenChange = healthRegenChange + amount;
                } else if (statName.equalsIgnoreCase("ability-damage")) {
                    abilityPowerChange = abilityPowerChange + amount;
                } else if (statName.equalsIgnoreCase("mining-fortune")) {
                    miningFortuneChange = miningFortuneChange + amount;
                } else if (statName.equalsIgnoreCase("farming-fortune")) {
                    farmingFortuneChange = farmingFortuneChange + amount;
                } else if (statName.equalsIgnoreCase("foraging-fortune")) {
                    foragingFortuneChange = foragingFortuneChange + amount;
                } else if (statName.equalsIgnoreCase("fishing-speed")) {
                    lureChange = lureChange + amount;
                } else if (statName.equalsIgnoreCase("lure")) {
                    flashChange = flashChange + amount;
                } else if (statName.equalsIgnoreCase("dialogue-speed")) {
                    talkSpeedChange = talkSpeedChange + amount;
                } else if (statName.equalsIgnoreCase("shop-discount")) {
                    discountChange = discountChange + amount;
                } else if (statName.equalsIgnoreCase("focus")) {
                    focusChange = focusChange + amount;
                } else if (statName.equalsIgnoreCase("stamina")) {
                    manaChange = manaChange + amount;
                } else if (statName.equalsIgnoreCase("sea-creature-chance")) {
                    seaCreatureChance = seaCreatureChance + amount;
                } else if (statName.equalsIgnoreCase("absorption")) {
                    if (!buff.added) {
                        mmoPlayer.setAbsorptionHealth(mmoPlayer.getAbsorptionHealth() + amount);
                        buff.added = true;
                    }
                } else if (statName.equalsIgnoreCase("evasiveness")) {
                    evasivenessChange = evasivenessChange + amount;
                } else if (statName.equalsIgnoreCase("luck")) {
                    luckChange = luckChange + amount;
                }
            }
        }
        if (mmoPlayer.potionEffects.size() > 0) {
            for (PotionEffect buff : mmoPlayer.potionEffects) {
                if (buff.expiryTime < System.currentTimeMillis()) {
                    mmoPlayer.potionEffects.remove(buff);
                    for (StatBuff stat : buff.statsImpacted) {
                        String name = stat.statName;
                        if (name.equalsIgnoreCase("absorption")) {
                            if (stat.amount > 0 && buff.added) {
                                mmoPlayer.setAbsorptionHealth(mmoPlayer.getAbsorptionHealth() - stat.amount);
                                if (mmoPlayer.getAbsorptionHealth() < 0) {
                                    mmoPlayer.setAbsorptionHealth(0);
                                }
                            }
                        }
                        if (name.equalsIgnoreCase("absorption") || name.equalsIgnoreCase("health")) {
                            plugin.updatePlayerHealth(mmoPlayer,0);
                        }
                    }
                    continue;
                }
                for (StatBuff stat : buff.statsImpacted) {
                    String statName = stat.statName;
                    float amount = stat.amount;
                    if (statName.equalsIgnoreCase("strength")) {
                        strChange = strChange + amount;
                    } else if (statName.equalsIgnoreCase("defense")) {
                        defenseChange = defenseChange + amount;
                    } else if (statName.equalsIgnoreCase("health")) {
                        healthChange = healthChange + amount;
                    } else if (statName.equalsIgnoreCase("crit-damage")) {
                        critDamChange = critDamChange + amount;
                    } else if (statName.equalsIgnoreCase("crit-chance")) {
                        critChanceChange = critChanceChange + amount;
                    } else if (statName.equalsIgnoreCase("speed")) {
                        speedChange = speedChange + amount;
                    } else if (statName.equalsIgnoreCase("vigor")) {
                        vigorChange = vigorChange + amount;
                    } else if (statName.equalsIgnoreCase("stamina-regen")) {
                        manaRegenChange = manaRegenChange + amount;
                    } else if (statName.equalsIgnoreCase("health-regen")) {
                        healthRegenChange = healthRegenChange + amount;
                    } else if (statName.equalsIgnoreCase("ability-damage")) {
                        abilityPowerChange = abilityPowerChange + amount;
                    } else if (statName.equalsIgnoreCase("mining-fortune")) {
                        miningFortuneChange = miningFortuneChange + amount;
                    } else if (statName.equalsIgnoreCase("farming-fortune")) {
                        farmingFortuneChange = farmingFortuneChange + amount;
                    } else if (statName.equalsIgnoreCase("foraging-fortune")) {
                        foragingFortuneChange = foragingFortuneChange + amount;
                    } else if (statName.equalsIgnoreCase("fishing-speed")) {
                        lureChange = lureChange + amount;
                    } else if (statName.equalsIgnoreCase("lure")) {
                        flashChange = flashChange + amount;
                    } else if (statName.equalsIgnoreCase("dialogue-speed")) {
                        talkSpeedChange = talkSpeedChange + amount;
                    } else if (statName.equalsIgnoreCase("shop-discount")) {
                        discountChange = discountChange + amount;
                    } else if (statName.equalsIgnoreCase("focus")) {
                        focusChange = focusChange + amount;
                    } else if (statName.equalsIgnoreCase("stamina")) {
                        manaChange = manaChange + amount;
                    } else if (statName.equalsIgnoreCase("sea-creature-chance")) {
                        seaCreatureChance = seaCreatureChance + amount;
                    } else if (statName.equalsIgnoreCase("absorption")) {
                        if (!buff.added) {
                            mmoPlayer.setAbsorptionHealth(mmoPlayer.getAbsorptionHealth() + amount);
                            buff.added = true;
                        }
                    } else if (statName.equalsIgnoreCase("evasiveness")) {
                        evasivenessChange = evasivenessChange + amount;
                    }
                }
            }
        }
        /*Iterator<ItemStack> invIter = player.getInventory().iterator();
        while (invIter.hasNext()) {
            ItemStack item = (ItemStack) invIter.next();
            if (item != null) {
                NBTItem nbtI = new NBTItem(item);
                if (nbtI.hasTag("ExtraAttributes")) {
                    NBTCompound comp = nbtI.getCompound("ExtraAttributes");
                   if (comp.hasTag("accessory")) {
                        updateStatsFromItem(item, changeMap);
                    }
                }
            }
        }*/
        Iterator<ItemStack> accessoryIter = mmoPlayer.getAccessoryBagContents().iterator();
        while (accessoryIter.hasNext()) {
            ItemStack item = (ItemStack) accessoryIter.next();
            if (item != null) {
                updateStatsFromItem(item, changeMap);
            }
        }
        Iterator<String> statsIter = changeMap.keySet().iterator();
        while (statsIter.hasNext()) {
            Map<String, Float> map = changeMap.get(statsIter.next());
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
                } else if (itr.getKey().equalsIgnoreCase("focus")) {
                    focusChange = focusChange + itr.getValue();
                } else if (itr.getKey().equalsIgnoreCase("vigor")) {
                    vigorChange = vigorChange + itr.getValue();
                } else if (itr.getKey().equalsIgnoreCase("seaCreatureChance")) {
                    seaCreatureChance = seaCreatureChance + itr.getValue();
                } else if (itr.getKey().equalsIgnoreCase("miningFortune")) {
                    miningFortuneChange = miningFortuneChange + itr.getValue();
                } else if (itr.getKey().equalsIgnoreCase("foragingFortune")) {
                    foragingFortuneChange = foragingFortuneChange + itr.getValue();
                } else if (itr.getKey().equalsIgnoreCase("farmingFortune")) {
                    farmingFortuneChange = farmingFortuneChange + itr.getValue();
                } else if (itr.getKey().equalsIgnoreCase("luck")) {
                    luckChange = luckChange + itr.getValue();
                }
            }
        }
        mmoPlayer.setStrength(mmoPlayer.getBaseStrength() + strChange);
        mmoPlayer.setCritDamage(mmoPlayer.getBaseCritDamage() + critDamChange);
        mmoPlayer.setSpeed(mmoPlayer.getBaseSpeed() + speedChange);
        mmoPlayer.setCritChance(mmoPlayer.getBaseCritChance() + critChanceChange);
        mmoPlayer.setMaxHealth(mmoPlayer.getBaseMaxHealth() + healthChange);
        mmoPlayer.setDefense(mmoPlayer.getBaseDefense() + defenseChange);
        mmoPlayer.setVigor(mmoPlayer.getBaseVigor() + vigorChange);
        mmoPlayer.setMaxMana(mmoPlayer.getBaseMaxMana() + manaChange);
        mmoPlayer.setAbilityDamage(mmoPlayer.getBaseAbilityDamage() + abilityPowerChange);
        mmoPlayer.setVendorPrice(mmoPlayer.getBaseVendorPrice() + discountChange);
        mmoPlayer.setDialogueSpeed(mmoPlayer.getBaseDialogueSpeed() + talkSpeedChange);
        mmoPlayer.setManaRegen(mmoPlayer.getBaseManaRegen() + manaRegenChange);
        mmoPlayer.setHealthRegen(mmoPlayer.getBaseHealthRegen() + healthRegenChange);
        mmoPlayer.setAttackSpeed(mmoPlayer.getAttackSpeed() + attackSpeedChange);
        mmoPlayer.setEvasiveness(0.0f + evasivenessChange);
        mmoPlayer.setFlash(mmoPlayer.getBaseFlash() + flashChange);
        mmoPlayer.setLure(mmoPlayer.getBaseLure() + lureChange);
        mmoPlayer.setFocus(mmoPlayer.getBaseFocus() + focusChange);
        mmoPlayer.setSeaCreatureChance(mmoPlayer.getBaseSeaCreatureChance() + seaCreatureChance);
        mmoPlayer.setMiningFortune(mmoPlayer.getBaseMiningFortune() + miningFortuneChange);
        mmoPlayer.setLoggingFortune(mmoPlayer.getBaseLoggingFortune() + foragingFortuneChange);
        mmoPlayer.setFarmingFortune(mmoPlayer.getBaseFarmingFortune() + farmingFortuneChange);
        mmoPlayer.setLuck(mmoPlayer.getBaseLuck() + luckChange);
        Iterator<ItemStack> iter = player.getInventory().iterator();
        while (iter.hasNext()) {
            ItemStack item = (ItemStack) iter.next();
            if (item != null && item.hasItemMeta()) {
                NBTItem nbtI = new NBTItem(item);
                if (nbtI.hasTag("ExtraAttributes")) {
                    NBTCompound comp = nbtI.getCompound("ExtraAttributes");
                    if (comp.hasTag("id")) {
                        String id = nbtI.getCompound("ExtraAttributes").getString("id");
                        updateAbilityText(item, player);
                        MMOItem mmoItem = plugin.itemHandler.getMMOItemFromString(id);

                        if (mmoItem != null && mmoItem.typeRequirement != null) {

                            if (mmoPlayer.getLvlForType(mmoItem.typeRequirement) < mmoItem.lvlRequirement) {
                                if (itemLoreContainsLevelRequirement(item)) {
                                } else {
                                    List<String> lore = item.getItemMeta().getLore();
                                    lore.add(lore.size()-1, (ChatColor.DARK_RED + "✖ " + ChatColor.RED + "Requires " + ChatColor.GREEN + mmoItem.typeRequirement.getName() + " Level " + Xp.intToRoman(mmoItem.lvlRequirement) + ChatColor.RED + " to use."));
                                    ItemMeta meta = item.getItemMeta();
                                    meta.setLore(lore);
                                    item.setItemMeta(meta);
                                }
                            } else {
                                if (itemLoreContainsLevelRequirement(item)) {
                                    int num = lineNumberOfRequirementString(item);
                                    if (num == -1) {
                                        continue;
                                    }
                                    List<String> lore = item.getItemMeta().getLore();
                                    lore.remove(num);
                                    ItemMeta meta = item.getItemMeta();
                                    meta.setLore(lore);
                                    item.setItemMeta(meta);
                                } else {
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean itemLoreContainsLevelRequirement(ItemStack item) {
        Iterator<String> iter = item.getItemMeta().getLore().iterator();
        while (iter.hasNext()) {
            String line = (String) iter.next();
            if (ChatColor.stripColor(line).startsWith("✖ Requires")) {
                return true;
            }
        }
        return false;
    }
    public int lineNumberOfRequirementString(ItemStack item) {
        Iterator<String> iter = item.getItemMeta().getLore().iterator();
        int num = 0;
        while (iter.hasNext()) {
            String line = (String) iter.next();
            if (ChatColor.stripColor(line).startsWith("✖ Requires")) {
                return num;
            }
            num++;
        }
        return -1;
    }

    public static List<String> getUpdatedStatBlock(List<String> statBlock, float multiplier) {
        float change;
        List<String> lore = new ArrayList<>(statBlock);
        for (int i = 0; i < lore.size(); i++) {
            String line = ChatColor.stripColor(lore.get(i));
            if (line.startsWith("Damage: +")) {
                line = line.replaceFirst("(Damage: +\\+)", "");
                change = Float.parseFloat(line);
                change = change * multiplier;
                lore.set(i, ChatColor.GRAY + "Damage: " + ChatColor.GREEN + "+" + formatDouble(change));
            } else if (line.startsWith("Damage: -")) {
                line = line.replaceFirst("(Damage: +\\-)", "");
                change = -Float.parseFloat(line);
                change = change - (change * (multiplier-1));
                lore.set(i, ChatColor.GRAY + "Damage: " + ChatColor.RED + "-" + formatDouble(change));
            } else if (line.startsWith("Mining Damage: +")) {
                line = line.replaceFirst("(Mining Damage: +\\+)", "");
                change = Float.parseFloat(line);
                change = change * multiplier;
                lore.set(i, ChatColor.GRAY + "Mining Damage: " + ChatColor.GREEN + "+" + formatDouble(change));
            } else if (line.startsWith("Mining Damage: -")) {
                line = line.replaceFirst("(Mining Damage: +\\-)", "");
                change = -Float.parseFloat(line);
                change = change - (change * (multiplier-1));
                lore.set(i, ChatColor.GRAY + "Mining Damage: " + ChatColor.RED + "-" + formatDouble(change));
            } else if (line.startsWith("Chopping Damage: +")) {
                line = line.replaceFirst("(Chopping Damage: +\\+)", "");
                change = Float.parseFloat(line);
                change = change * multiplier;
                lore.set(i, ChatColor.GRAY + "Chopping Damage: " + ChatColor.GREEN + "+" + formatDouble(change));
            } else if (line.startsWith("Chopping Damage: -")) {
                line = line.replaceFirst("(Chopping Damage: +\\-)", "");
                change = -Float.parseFloat(line);
                change = change - (change * (multiplier-1));
                lore.set(i, ChatColor.GRAY + "Chopping Damage: " + ChatColor.RED + "-" + formatDouble(change));
            } else if (line.startsWith("Strength: +")) {
                line = line.replaceFirst("(Strength: +\\+)", "");
                change = Float.parseFloat(line);
                change = change * multiplier;
                lore.set(i, ChatColor.GRAY + "Strength: " + ChatColor.GREEN + "+" + formatDouble(change));
            } else if (line.startsWith("Strength: -")) {
                line = line.replaceFirst("(Strength: +\\-)", "");
                change = -Float.parseFloat(line);
                change = change - (change * (multiplier-1));
                lore.set(i, ChatColor.GRAY + "Strength: " + ChatColor.RED + "-" + formatDouble(change));
            } else if (line.startsWith("Crit Damage: +")) {
                line = line.replaceFirst("(Crit Damage: +\\+)", "");
                line = line.replaceFirst("(%)", "");
                change = Float.parseFloat(line);
                change = change * multiplier;
                lore.set(i, ChatColor.GRAY + "Crit Damage: " + ChatColor.GREEN + "+" + formatDouble(change) + "%");
            } else if (line.startsWith("Crit Damage: -")) {
                line = line.replaceFirst("(Crit Damage: +\\-)", "");
                line = line.replaceFirst("(%)", "");
                change = -Float.parseFloat(line);
                change = change - (change * (multiplier-1));
                lore.set(i, ChatColor.GRAY + "Crit Damage: " + ChatColor.RED + "-" + formatDouble(change) + "%");
            } else if (line.startsWith("Speed: +")) {
                line = line.replaceFirst("(Speed: +\\+)", "");
                change = Float.parseFloat(line);
                change = change * multiplier;
                lore.set(i, ChatColor.GRAY + "Speed: " + ChatColor.GREEN + "+" + formatDouble(change));
            } else if (line.startsWith("Speed: -")) {
                line = line.replaceFirst("(Speed: +\\-)", "");
                change = -Float.parseFloat(line);
                change = change - (change * (multiplier-1));
                lore.set(i, ChatColor.GRAY + "Speed: " + ChatColor.RED + "-" + formatDouble(change));
            } else if (line.startsWith("Crit Chance: +")) {
                line = line.replaceFirst("(Crit Chance: +\\+)", "");
                line = line.replaceFirst("(%)", "");
                change = Float.parseFloat(line);
                change = change * multiplier;
                lore.set(i, ChatColor.GRAY + "Crit Chance: " + ChatColor.GREEN + "+" + formatDouble(change) + "%");
            } else if (line.startsWith("Crit Chance: -")) {
                line = line.replaceFirst("(Crit Chance: +\\-)", "");
                line = line.replaceFirst("(%)", "");
                change = -Float.parseFloat(line);
                change = change - (change * (multiplier-1));
                lore.set(i, ChatColor.GRAY + "Crit Chance: " + ChatColor.RED + "-" + formatDouble(change) + "%");
            } else if (line.startsWith("Health: +")) {
                line = line.replaceFirst("(Health: +\\+)", "");
                change = Float.parseFloat(line);
                change = change * multiplier;
                lore.set(i, ChatColor.GRAY + "Health: " + ChatColor.GREEN + "+" + formatDouble(change));
            } else if (line.startsWith("Health: -")) {
                line = line.replaceFirst("(Health: +\\-)", "");
                change = -Float.parseFloat(line);
                change = change - (change * (multiplier-1));
                lore.set(i, ChatColor.GRAY + "Health: " + ChatColor.RED + "-" + formatDouble(change));
            } else if (line.startsWith("Defense: +")) {
                line = line.replaceFirst("(Defense: +\\+)", "");
                change = Float.parseFloat(line);
                change = change * multiplier;
                lore.set(i, ChatColor.GRAY + "Defense: " + ChatColor.GREEN + "+" + formatDouble(change));
            } else if (line.startsWith("Defense: -")) {
                line = line.replaceFirst("(Defense: +\\-)", "");
                change = -Float.parseFloat(line);
                change = change - (change * (multiplier-1));
                lore.set(i, ChatColor.GRAY + "Defense: " + ChatColor.RED + "-" + formatDouble(change));
            } else if (line.startsWith("Stamina: +")) {
                line = line.replaceFirst("(Stamina: +\\+)", "");
                change = Float.parseFloat(line);
                change = change * multiplier;
                lore.set(i, ChatColor.GRAY + "Stamina: " + ChatColor.GREEN + "+" + formatDouble(change));
            } else if (line.startsWith("Stamina: -")) {
                line = line.replaceFirst("(Stamina: +\\-)", "");
                change = -Float.parseFloat(line);
                change = change - (change * (multiplier-1));
                lore.set(i, ChatColor.GRAY + "Stamina: " + ChatColor.RED + "-" + formatDouble(change));
            } else if (line.startsWith("Ability Damage: +")) {
                line = line.replaceFirst("(Ability Damage: +\\+)", "");
                line = line.replaceFirst("(%)", "");
                change = Float.parseFloat(line);
                change = change * multiplier;
                lore.set(i, ChatColor.GRAY + "Ability Damage: " + ChatColor.GREEN + "+" + formatDouble(change) + "%");
            } else if (line.startsWith("Ability Damage: -")) {
                line = line.replaceFirst("(Ability Damage: +\\-)", "");
                line = line.replaceFirst("(%)", "");
                change = -Float.parseFloat(line);
                change = change - (change * (multiplier-1));
                lore.set(i, ChatColor.GRAY + "Ability Damage: " + ChatColor.RED + "-" + formatDouble(change) + "%");
            } else if (line.startsWith("Shop Discount: +")) {
                line = line.replaceFirst("(Shop Discount: +\\+)", "");
                line = line.replaceFirst("(%)", "");
                change = Float.parseFloat(line);
                change = change * multiplier;
                lore.set(i, ChatColor.GRAY + "Shop Discount: " + ChatColor.GREEN + "+" + formatDouble(change) + "%");
            } else if (line.startsWith("Shop Discount: -")) {
                line = line.replaceFirst("(Shop Discount: +\\-)", "");
                line = line.replaceFirst("(%)", "");
                change = -Float.parseFloat(line);
                change = change - (change * (multiplier-1));
                lore.set(i, ChatColor.GRAY + "Shop Discount: " + ChatColor.RED + "-" + formatDouble(change) + "%");
            }else if (line.startsWith("Dialogue Speed: +")) {
                line = line.replaceFirst("(Dialogue Speed: +\\+)", "");
                change = Float.parseFloat(line);
                change = change * multiplier;
                lore.set(i, ChatColor.GRAY + "Dialogue Speed: " + ChatColor.GREEN + "+" + formatDouble(change));
            } else if (line.startsWith("Dialogue Speed: -")) {
                line = line.replaceFirst("(Dialogue Speed: +\\-)", "");
                change = -Float.parseFloat(line);
                change = change - (change * (multiplier-1));
                lore.set(i, ChatColor.GRAY + "Dialogue Speed: " + ChatColor.RED + "-" + formatDouble(change));
            } else if (line.startsWith("Health Regen: +")) {
                line = line.replaceFirst("(Health Regen: +\\+)", "");
                change = Float.parseFloat(line);
                change = change * multiplier;
                lore.set(i, ChatColor.GRAY + "Health Regen: " + ChatColor.GREEN + "+" + formatDouble(change));
            } else if (line.startsWith("Health Regen: -")) {
                line = line.replaceFirst("(Health Regen: +\\-)", "");
                change = -Float.parseFloat(line);
                change = change - (change * (multiplier-1));
                lore.set(i, ChatColor.GRAY + "Health Regen: " + ChatColor.RED + "-" + formatDouble(change));
            } else if (line.startsWith("Stamina Regen: +")) {
                line = line.replaceFirst("(Stamina Regen: +\\+)", "");
                change = Float.parseFloat(line);
                change = change * multiplier;
                lore.set(i, ChatColor.GRAY + "Stamina Regen: " + ChatColor.GREEN + "+" + formatDouble(change));
            } else if (line.startsWith("Stamina Regen: -")) {
                line = line.replaceFirst("(Stamina Regen: +\\-)", "");
                change = -Float.parseFloat(line);
                change = change - (change * (multiplier-1));
                lore.set(i, ChatColor.GRAY + "Stamina Regen: " + ChatColor.RED + "-" + formatDouble(change));
            } else if (line.startsWith("Fishing Speed: +")) {
                line = line.replaceFirst("(Fishing Speed: +\\+)", "");
                change = Float.parseFloat(line);
                change = change * multiplier;
                lore.set(i, ChatColor.GRAY + "Fishing Speed: " + ChatColor.GREEN + "+" + formatDouble(change));
            } else if (line.startsWith("Fishing Speed: -")) {
                line = line.replaceFirst("(Fishing Speed: +\\-)", "");
                change = -Float.parseFloat(line);
                change = change - (change * (multiplier-1));
                lore.set(i, ChatColor.GRAY + "Fishing Speed: " + ChatColor.RED + "-" + formatDouble(change));
            } else if (line.startsWith("Lure: +")) {
                line = line.replaceFirst("(Lure: +\\+)", "");
                change = Float.parseFloat(line);
                change = change * multiplier;
                lore.set(i, ChatColor.GRAY + "Lure: " + ChatColor.GREEN + "+" + formatDouble(change));
            } else if (line.startsWith("Lure: -")) {
                line = line.replaceFirst("(Lure: +\\-)", "");
                change = -Float.parseFloat(line);
                change = change - (change * (multiplier-1));
                lore.set(i, ChatColor.GRAY + "Lure: " + ChatColor.RED + "-" + formatDouble(change));
            } else if (line.startsWith("Focus: +")) {
                line = line.replaceFirst("(Focus: +\\+)", "");
                change = Float.parseFloat(line);
                change = change * multiplier;
                lore.set(i, ChatColor.GRAY + "Focus: " + ChatColor.GREEN + "+" + formatDouble(change));
            } else if (line.startsWith("Focus: -")) {
                line = line.replaceFirst("(Focus: +\\-)", "");
                change = -Float.parseFloat(line);
                change = change - (change * (multiplier-1));
                lore.set(i, ChatColor.GRAY + "Focus: " + ChatColor.RED + "-" + formatDouble(change));
            } else if (line.startsWith("Vigor: +")) {
                line = line.replaceFirst("(Vigor: +\\+)", "");
                change = Float.parseFloat(line);
                change = change * multiplier;
                lore.set(i, ChatColor.GRAY + "Vigor: " + ChatColor.GREEN + "+" + formatDouble(change));
            } else if (line.startsWith("Vigor: -")) {
                line = line.replaceFirst("(Vigor: +\\-)", "");
                change = -Float.parseFloat(line);
                change = change - (change * (multiplier-1));
                lore.set(i, ChatColor.GRAY + "Vigor: " + ChatColor.RED + "-" + formatDouble(change));
            } else if (line.startsWith("Sea Creature Chance: +")) {
                line = line.replaceFirst("(Sea Creature Chance: +\\+)", "");
                line = line.replaceFirst("(%)", "");
                change = Float.parseFloat(line);
                change = change * multiplier;
                lore.set(i, ChatColor.GRAY + "Sea Creature Chance: " + ChatColor.GREEN + "+" + formatDouble(change) + "%");
            } else if (line.startsWith("Sea Creature Chance: -")) {
                line = line.replaceFirst("(Sea Creature Chance: +\\-)", "");
                line = line.replaceFirst("(%)", "");
                change = -Float.parseFloat(line);
                change = change - (change * (multiplier-1));
                lore.set(i, ChatColor.GRAY + "Sea Creature Chance: " + ChatColor.RED + "-" + formatDouble(change) + "%");
            } else if (line.startsWith("Farming Fortune: +")) {
                line = line.replaceFirst("(Farming Fortune: +\\+)", "");
                change = Float.parseFloat(line);
                change = change * multiplier;
                lore.set(i, ChatColor.GRAY + "Farming Fortune: " + ChatColor.GREEN + "+" + formatDouble(change));
            } else if (line.startsWith("Farming Fortune: -")) {
                line = line.replaceFirst("(Farming Fortune: +\\-)", "");
                change = -Float.parseFloat(line);
                change = change - (change * (multiplier-1));
                lore.set(i, ChatColor.GRAY + "Farming Fortune: " + ChatColor.RED + "-" + formatDouble(change));
            } else if (line.startsWith("Foraging Fortune: +")) {
                line = line.replaceFirst("(Foraging Fortune: +\\+)", "");
                change = Float.parseFloat(line);
                change = change * multiplier;
                lore.set(i, ChatColor.GRAY + "Foraging Fortune: " + ChatColor.GREEN + "+" + formatDouble(change));
            } else if (line.startsWith("Foraging Fortune: -")) {
                line = line.replaceFirst("(Foraging Fortune: +\\-)", "");
                change = -Float.parseFloat(line);
                change = change - (change * (multiplier-1));
                lore.set(i, ChatColor.GRAY + "Foraging Fortune: " + ChatColor.RED + "-" + formatDouble(change));
            } else if (line.startsWith("Mining Fortune: +")) {
                line = line.replaceFirst("(Mining Fortune: +\\+)", "");
                change = Float.parseFloat(line);
                change = change * multiplier;
                lore.set(i, ChatColor.GRAY + "Mining Fortune: " + ChatColor.GREEN + "+" + formatDouble(change));
            } else if (line.startsWith("Mining Fortune: -")) {
                line = line.replaceFirst("(Mining Fortune: +\\-)", "");
                change = -Float.parseFloat(line);
                change = change - (change * (multiplier-1));
                lore.set(i, ChatColor.GRAY + "Mining Fortune: " + ChatColor.RED + "-" + formatDouble(change));
            } else if (line.startsWith("Luck: +")) {
                line = line.replaceFirst("(Luck: +\\+)", "");
                change = Float.parseFloat(line);
                change = change * multiplier;
                lore.set(i, ChatColor.GRAY + "Luck: " + ChatColor.GREEN + "+" + formatDouble(change));
            } else if (line.startsWith("Luck: -")) {
                line = line.replaceFirst("(Luck: +\\-)", "");
                change = -Float.parseFloat(line);
                change = change - (change * (multiplier-1));
                lore.set(i, ChatColor.GRAY + "Luck: " + ChatColor.RED + "-" + formatDouble(change));
            }
        }
        return lore;
    }
    private static String formatDouble(double amount) {
        if ((int)amount == amount) {
            return String.format("%,d",(int)amount);
        } else {
            return String.format("%,.1f",amount);
        }
    }

    public float calculateNormalDamage(float num, MMOPlayer mmoPlayer, Entity target, ItemStack item, boolean crit) {
        num = (5+num) * getNormalDamageModifier(mmoPlayer, target, item);
        if (crit) {
            num = num * (1+(mmoPlayer.getCritDamage()/100));
        }
        return num;
    }
    private float getNormalDamageModifier(MMOPlayer mmoPlayer, Entity target, ItemStack item) {
        MMOItem mmoItem = plugin.itemHandler.getMMOItem(item);
        return (1+(mmoPlayer.getStrength()/100)) * normalDamageAdditiveMults(mmoPlayer, target, mmoItem) * normalDamageMultiplicativeMults(mmoPlayer ,target,mmoItem);
    }
    public float calculateAbilityDamage(float num, MMOPlayer mmoPlayer, Entity target, ItemStack item) {
        MMOItem mmoItem = plugin.itemHandler.getMMOItem(item);
        num = num * getAbilityDamageModifier(mmoPlayer, target, item);
        return num;
    }
    public float calculateAbilityDamage(float num, MMOPlayer mmoPlayer, ItemStack item) {
        MMOItem mmoItem = plugin.itemHandler.getMMOItem(item);
        num = num * getAbilityDamageModifier(mmoPlayer, null, item);
        return num;
    }
    public float getAbilityDamageModifier(MMOPlayer mmoPlayer, Entity target, ItemStack item) {
        MMOItem mmoItem = plugin.itemHandler.getMMOItem(item);
        return (1 + (mmoPlayer.getVigor()/100) * mmoItem.abilityScaling) * abilityDamageAdditiveMults(mmoPlayer, target, mmoItem) * abilityDamageMultiplicativeMults(mmoPlayer, target, mmoItem);
    }
    private float abilityDamageAdditiveMults(MMOPlayer mmoPlayer, Entity target, MMOItem mmoItem) {
        float multiplier = 1;
        double combatLvlMult = mmoPlayer.getCombatLvl() * 0.04;
        if (plugin.tournament) {
            combatLvlMult = combatLvlMult * 2;
        }
        multiplier = (float) (multiplier + combatLvlMult);
        return multiplier;
    }

    private float abilityDamageMultiplicativeMults(MMOPlayer mmoPlayer, Entity target, MMOItem mmoItem) {
        float multiplier = 1;
        multiplier = multiplier * (1 + (mmoPlayer.getAbilityDamage()/100));
        return multiplier;
    }
    private float normalDamageAdditiveMults(MMOPlayer mmoPlayer, Entity target, MMOItem mmoItem) {
        float multiplier = 1;
        double combatLvlMult = mmoPlayer.getCombatLvl() * 0.04;
        if (plugin.tournament) {
            combatLvlMult = combatLvlMult * 2;
        }
        multiplier = (float) (multiplier + combatLvlMult);
        return multiplier;
    }
    private float normalDamageMultiplicativeMults(MMOPlayer mmoPlayer, Entity target, MMOItem mmoItem) {
        return 1;
    }

    public void temporaryStatUpdate(MMOPlayer mmoPlayer, String statName, float amount, int timeInSecs) {
        StatBuff buff = new StatBuff(statName, amount, System.currentTimeMillis() + (1000L*timeInSecs));
        mmoPlayer.buffs.add(buff);
        if (statName.equalsIgnoreCase("absorption")) {
            mmoPlayer.absorptionLayers.add(buff);
            plugin.updatePlayerHealth(mmoPlayer,0);
            mmoPlayer.sortLayers();
        }
    }


}
