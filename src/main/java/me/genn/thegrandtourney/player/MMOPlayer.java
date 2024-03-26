package me.genn.thegrandtourney.player;

import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.mobs.MMOMob;
import me.genn.thegrandtourney.skills.Recipe;
import me.genn.thegrandtourney.skills.Station;
import me.genn.thegrandtourney.util.IntMap;
import me.genn.thegrandtourney.xp.XpType;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class MMOPlayer {
    public TGT plugin;
    public String currentGoal;

    public Objective trackedObjective;
    private float health;
    private float maxHealth;
    private float defense;
    private float strength;
    private float critDamage;
    private float speed;
    private float critChance;
    private float abilityDam;
    private float mana;
    private float maxMana;
    private float vendorPrice;
    private float dialogueSpeed;

    public float getAbilityDamage() {
        return abilityDamage;
    }

    public void setAbilityDamage(float abilityDamage) {
        this.abilityDamage = abilityDamage;
    }

    private float abilityDamage;
    private float baseAbilityDamage;

    public float getLure() {
        return lure;
    }

    public void setLure(float lure) {
        this.lure = lure;
    }

    public float getBaseLure() {
        return baseLure;
    }

    public void setBaseLure(float baseLure) {
        this.baseLure = baseLure;
    }

    public float getFlash() {
        return flash;
    }

    public void setFlash(float flash) {
        this.flash = flash;
    }

    public float getBaseFlash() {
        return baseFlash;
    }

    public void setBaseFlash(float baseFlash) {
        this.baseFlash = baseFlash;
    }

    private float lure;
    private float baseLure;
    private float flash;
    private float baseFlash;
    public Map<String, IntMap<MMOMob>> slayerMap;

    public MMOPlayer() {
        this.slayerMap = new HashMap<>();
        this.accessoryBagContents = new ArrayList<>();
        this.storageContents = new HashMap<>();
        this.objectives = new ArrayList<>();
        this.completedObjectives = new ArrayList<>();
        this.recipeBook = new ArrayList<>();
    }
    public boolean isCrafting = false;
    public Location craftStart;
    public String currentCraft = "none";
    public Station currentStation;

    public float getStrength() {
        return strength;
    }

    public void setStrength(float strength) {
        this.strength = strength;
    }

    public float getBaseHealth() {
        return baseHealth;
    }

    public void setBaseHealth(float baseHealth) {
        this.baseHealth = baseHealth;
    }

    public float getBaseMaxHealth() {
        return baseMaxHealth;
    }

    public void setBaseMaxHealth(float baseMaxHealth) {
        this.baseMaxHealth = baseMaxHealth;
    }

    public float getBaseDefense() {
        return baseDefense;
    }

    public void setBaseDefense(float baseDefense) {
        this.baseDefense = baseDefense;
    }

    public float getBaseStrength() {
        return baseStrength;
    }

    public void setBaseStrength(float baseStrength) {
        this.baseStrength = baseStrength;
    }

    public float getBaseCritDamage() {
        return baseCritDamage;
    }

    public void setBaseCritDamage(float baseCritDamage) {
        this.baseCritDamage = baseCritDamage;
    }

    public float getBaseSpeed() {
        return baseSpeed;
    }

    public void setBaseSpeed(float baseSpeed) {
        this.baseSpeed = baseSpeed;
    }

    public float getBaseCritChance() {
        return baseCritChance;
    }

    public void setBaseCritChance(float baseCritChance) {
        this.baseCritChance = baseCritChance;
    }

    public float getBaseAbilityDamage() {
        return baseAbilityDamage;
    }

    public void setBaseAbilityDamage(float baseAbilityDamage) {
        this.baseAbilityDamage = baseAbilityDamage;
    }

    public float getBaseMana() {
        return baseMana;
    }

    public void setBaseMana(float baseMana) {
        this.baseMana = baseMana;
    }

    public float getBaseMaxMana() {
        return baseMaxMana;
    }

    public void setBaseMaxMana(float baseMaxMana) {
        this.baseMaxMana = baseMaxMana;
    }

    public float getBaseVendorPrice() {
        return baseVendorPrice;
    }

    public void setBaseVendorPrice(float baseVendorPrice) {
        this.baseVendorPrice = baseVendorPrice;
    }

    public float getBaseDialogueSpeed() {
        return baseDialogueSpeed;
    }

    public void setBaseDialogueSpeed(float baseDialogueSpeed) {
        this.baseDialogueSpeed = baseDialogueSpeed;
    }

    public float getBaseHealthRegen() {
        return baseHealthRegen;
    }

    public void setBaseHealthRegen(float baseHealthRegen) {
        this.baseHealthRegen = baseHealthRegen;
    }

    public float getBaseManaRegen() {
        return baseManaRegen;
    }

    public void setBaseManaRegen(float baseManaRegen) {
        this.baseManaRegen = baseManaRegen;
    }

    private float baseHealth;
    private float baseMaxHealth;
    private float baseDefense;
    private float baseStrength;
    private float baseCritDamage;
    private float baseSpeed;
    private float baseCritChance;
    private float baseMana;
    private float baseMaxMana;
    private float baseVendorPrice;
    private float baseDialogueSpeed;
    private double gold;
    private int combatLvl;
    private int miningLvl;
    private int loggingLvl;
    private int fishingLvl;
    private int farmingLvl;
    private int smithingLvl;
    private int cookingLvl;
    private float combatProg;
    private float miningProg;
    private float loggingProg;
    private float fishingProg;
    private float farmingProg;
    private float smithingProg;


    public float getTailoringProg() {
        return tailoringProg;
    }

    public void setTailoringProg(float tailoringProg) {
        this.tailoringProg = tailoringProg;
    }

    public float getTinkeringProg() {
        return tinkeringProg;
    }

    public void setTinkeringProg(float tinkeringProg) {
        this.tinkeringProg = tinkeringProg;
    }

    public int getTailoringLvl() {
        return tailoringLvl;
    }

    public void setTailoringLvl(int tailoringLvl) {
        this.tailoringLvl = tailoringLvl;
    }

    public int getTinkeringLvl() {
        return tinkeringLvl;
    }

    public void setTinkeringLvl(int tinkeringLvl) {
        this.tinkeringLvl = tinkeringLvl;
    }

    private float tailoringProg;
    private float tinkeringProg;
    private int tailoringLvl;
    private int tinkeringLvl;

    public int getAccessoryBagSlots() {
        if (accessoryBagSlots >= 45) {
            return 45;
        } else {
            return accessoryBagSlots;
        }
    }

    public void setAccessoryBagSlots(int accessoryBagSlots) {
        this.accessoryBagSlots = accessoryBagSlots;
    }

    private int accessoryBagSlots = 3;

    public int getStorageSlots() {
        return storageSlots;
    }

    public void setStorageSlots(int storageSlots) {
        this.storageSlots = storageSlots;
    }

    private int storageSlots = 9;

    public List<ItemStack> getAccessoryBagContents() {
        return accessoryBagContents;
    }

    public void setAccessoryBagContents(List<ItemStack> accessoryBagContents) {
        this.accessoryBagContents = accessoryBagContents;
    }

    private List<ItemStack> accessoryBagContents;



    public Map<Integer, ItemStack> storageContents;
    public List<Objective> objectives;
    public List<Objective> completedObjectives;
    public List<Recipe> recipeBook;

    private float cookingProg;
    private float healthRegen;
    private float manaRegen;
    private float baseHealthRegen;
    private float baseManaRegen;

    public float getBaseFocus() {
        return baseFocus;
    }

    public void setBaseFocus(float baseFocus) {
        this.baseFocus = baseFocus;
    }

    public float getFocus() {
        return focus;
    }

    public void setFocus(float focus) {
        this.focus = focus;
    }

    private float baseFocus = 0f;
    private float focus = 0f;

    public float getMiningFortune() {
        return miningFortune;
    }

    public void setMiningFortune(float miningFortune) {
        this.miningFortune = miningFortune;
    }

    public float getBaseMiningFortune() {
        return baseMiningFortune;
    }

    public void setBaseMiningFortune(float baseMiningFortune) {
        this.baseMiningFortune = baseMiningFortune;
    }

    public float getFarmingFortune() {
        return farmingFortune;
    }

    public void setFarmingFortune(float farmingFortune) {
        this.farmingFortune = farmingFortune;
    }

    public float getBaseFarmingFortune() {
        return baseFarmingFortune;
    }

    public void setBaseFarmingFortune(float baseFarmingFortune) {
        this.baseFarmingFortune = baseFarmingFortune;
    }

    public float getLoggingFortune() {
        return loggingFortune;
    }

    public void setLoggingFortune(float loggingFortune) {
        this.loggingFortune = loggingFortune;
    }

    public float getBaseLoggingFortune() {
        return baseLoggingFortune;
    }

    public void setBaseLoggingFortune(float baseLoggingFortune) {
        this.baseLoggingFortune = baseLoggingFortune;
    }

    private float miningFortune;
    private float baseMiningFortune;
    private float farmingFortune;
    private float baseFarmingFortune;
    private float loggingFortune;
    private float baseLoggingFortune;

    public Location getRespawnLocation() {
        return respawnLocation;
    }

    public void setRespawnLocation(Location respawnLocation) {
        this.respawnLocation = respawnLocation;
    }

    private Location respawnLocation;


    public float getAttackSpeed() {
        return attackSpeed;
    }

    public void setAttackSpeed(float attackSpeed) {
        this.attackSpeed = attackSpeed;
    }

    private float attackSpeed;

    public float getBaseAttackSpeed() {
        return baseAttackSpeed;
    }

    public void setBaseAttackSpeed(float baseAttackSpeed) {
        this.baseAttackSpeed = baseAttackSpeed;
    }

    private float baseAttackSpeed;
    private UUID minecraftUUID;


    public double getPurseGold() {
        return plugin.econ.getBalance(Bukkit.getOfflinePlayer(minecraftUUID));
    }

    public void addPurseGold(double amount) {
        plugin.econ.depositPlayer(Bukkit.getOfflinePlayer(minecraftUUID), amount);
    }
    public boolean removePurseGold(double amount) {
        if (plugin.econ.has(Bukkit.getOfflinePlayer(minecraftUUID), amount)) {
            plugin.econ.withdrawPlayer(Bukkit.getOfflinePlayer(minecraftUUID), amount);
            return true;
        } else {
            return false;
        }
    }
    public double getBankGold() {
        return plugin.econ.bankBalance("Bank." + Bukkit.getPlayer(minecraftUUID).getName()).balance;
    }
    public void addBankGold(double amount) {
        plugin.econ.bankDeposit("Bank." + Bukkit.getPlayer(minecraftUUID).getName(), amount);
    }
    public boolean removeBankGold(double amount) {
        if (plugin.econ.bankHas("Bank." + Bukkit.getPlayer(minecraftUUID).getName(), amount).type == EconomyResponse.ResponseType.SUCCESS) {
            plugin.econ.bankWithdraw("Bank." + Bukkit.getPlayer(minecraftUUID).getName(), amount);
            return true;
        } else {
            return false;
        }
    }
    public float getHealth() {
        return health;
    }
    public void setHealth(float health) {
        this.health = health;
    }
    public float getMaxHealth() {
        return maxHealth;
    }
    public void setMaxHealth(float maxHealth) {
        this.maxHealth = maxHealth;
    }
    public float getDefense() {
        return defense;
    }
    public void setDefense(float defense) {
        this.defense = defense;
    }
    public float getDamage() {
        return strength;
    }
    public void setDamage(float damage) {
        this.strength = damage;
    }
    public float getCritDamage() {
        return critDamage;
    }
    public void setCritDamage(float critDamage) {
        this.critDamage = critDamage;
    }
    public float getSpeed() {
        return speed;
    }
    public void setSpeed(float speed) {
        this.speed = speed;
    }
    public float getCritChance() {
        return critChance;
    }
    public void setCritChance(float critChance) {
        this.critChance = critChance;
    }
    public float getAbilityDam() {
        return abilityDam;
    }
    public void setAbilityDam(float abilityDam) {
        this.abilityDam = abilityDam;
    }
    public float getMana() {
        return mana;
    }
    public void setMana(float mana) {
        this.mana = mana;
    }
    public float getMaxMana() {
        return maxMana;
    }
    public void setMaxMana(float maxMana) {
        this.maxMana = maxMana;
    }
    public float getVendorPrice() {
        return vendorPrice;
    }
    public void setVendorPrice(float vendorPrice) {
        this.vendorPrice = vendorPrice;
    }
    public float getDialogueSpeed() {
        return dialogueSpeed;
    }
    public void setDialogueSpeed(float dialogueSpeed) {
        this.dialogueSpeed = dialogueSpeed;
    }
    public int getCombatLvl() {
        return combatLvl;
    }
    public void setCombatLvl(int combatLvl) {
        this.combatLvl = combatLvl;
    }
    public int getMiningLvl() {
        return miningLvl;
    }
    public void setMiningLvl(int miningLvl) {
        this.miningLvl = miningLvl;
    }
    public int getLoggingLvl() {
        return loggingLvl;
    }
    public void setLoggingLvl(int loggingLvl) {
        this.loggingLvl = loggingLvl;
    }
    public int getFishingLvl() {
        return fishingLvl;
    }
    public void setFishingLvl(int fishingLvl) {
        this.fishingLvl = fishingLvl;
    }
    public int getFarmingLvl() {
        return farmingLvl;
    }
    public void setFarmingLvl(int farmingLvl) {
        this.farmingLvl = farmingLvl;
    }
    public int getSmithingLvl() {
        return smithingLvl;
    }
    public void setSmithingLvl(int smithingLvl) {
        this.smithingLvl = smithingLvl;
    }

    public int getCookingLvl() {
        return cookingLvl;
    }
    public void setCookingLvl(int cookingLvl) {
        this.cookingLvl = cookingLvl;
    }
    public float getCombatProg() {
        return combatProg;
    }
    public void setCombatProg(float combatProg) {
        this.combatProg = combatProg;
    }
    public float getMiningProg() {
        return miningProg;
    }
    public void setMiningProg(float miningProg) {
        this.miningProg = miningProg;
    }
    public float getLoggingProg() {
        return loggingProg;
    }
    public void setLoggingProg(float loggingProg) {
        this.loggingProg = loggingProg;
    }
    public float getFishingProg() {
        return fishingProg;
    }
    public void setFishingProg(float fishingProg) {
        this.fishingProg = fishingProg;
    }
    public float getFarmingProg() {
        return farmingProg;
    }
    public void setFarmingProg(float farmingProg) {
        this.farmingProg = farmingProg;
    }
    public float getSmithingProg() {
        return smithingProg;
    }
    public void setSmithingProg(float smithingProg) {
        this.smithingProg = smithingProg;
    }

    public float getCookingProg() {
        return cookingProg;
    }
    public void setCookingProg(float cookingProg) {
        this.cookingProg = cookingProg;
    }
    public UUID getMinecraftUUID() {
        return minecraftUUID;
    }
    public void setMinecraftUUID(UUID minecraftUUID) {
        this.minecraftUUID = minecraftUUID;
    }
    public float getHealthRegen() {
        return healthRegen;
    }
    public void setHealthRegen(float healthRegen) {
        this.healthRegen = healthRegen;
    }
    public float getManaRegen() {
        return manaRegen;
    }
    public void setManaRegen(float manaRegen) {
        this.manaRegen = manaRegen;
    }




    public float getXpForType(XpType type) {
        if (type == XpType.BLACKSMITHING) {
            return getSmithingProg();
        } else if (type == XpType.TAILORING) {
            return getTailoringProg();
        } else if (type == XpType.COOKING) {
            return getCookingProg();
        } else if (type == XpType.TINKERING) {
            return getTinkeringProg();
        } else if (type == XpType.COMBAT) {
            return getCombatProg();
        } else if (type == XpType.FARMING) {
            return getFarmingProg();
        } else if (type == XpType.LOGGING) {
            return getLoggingProg();
        } else if (type == XpType.FISHING) {
            return getFishingProg();
        } else if (type == XpType.MINING) {
            return getMiningProg();
        }
        return 0.0F;
    }
    public void setXpForType(XpType type, double amount) {
        if (type == XpType.BLACKSMITHING) {
             setSmithingProg((float)amount);
        } else if (type == XpType.TAILORING) {
             setTailoringProg((float)amount);
        } else if (type == XpType.COOKING) {
             setCookingProg((float)amount);
        } else if (type == XpType.TINKERING) {
             setTinkeringProg((float)amount);
        } else if (type == XpType.COMBAT) {
             setCombatProg((float)amount);
        } else if (type == XpType.FARMING) {
             setFarmingProg((float)amount);
        } else if (type == XpType.LOGGING) {
             setLoggingProg((float)amount);
        } else if (type == XpType.FISHING) {
             setFishingProg((float)amount);
        } else if (type == XpType.MINING) {
             setMiningProg((float)amount);
        }
    }

    public int getLvlForType(XpType type) {
        if (type == XpType.BLACKSMITHING) {
            return getSmithingLvl();
        } else if (type == XpType.TAILORING) {
            return getTailoringLvl();
        } else if (type == XpType.COOKING) {
            return getCookingLvl();
        } else if (type == XpType.TINKERING) {
            return getTinkeringLvl();
        } else if (type == XpType.COMBAT) {
            return getCombatLvl();
        } else if (type == XpType.FARMING) {
            return getFarmingLvl();
        } else if (type == XpType.LOGGING) {
            return getLoggingLvl();
        } else if (type == XpType.FISHING) {
            return getFishingLvl();
        } else if (type == XpType.MINING) {
            return getMiningLvl();
        }
        return 0;
    }

    public void setLvlForType(XpType type, int lvl) {
        if (type == XpType.BLACKSMITHING) {
             setSmithingLvl(lvl);
        } else if (type == XpType.TAILORING) {
             setTailoringLvl(lvl);
        } else if (type == XpType.COOKING) {
             setCookingLvl(lvl);
        } else if (type == XpType.TINKERING) {
             setTinkeringLvl(lvl);
        } else if (type == XpType.COMBAT) {
             setCombatLvl(lvl);
        } else if (type == XpType.FARMING) {
             setFarmingLvl(lvl);
        } else if (type == XpType.LOGGING) {
             setLoggingLvl(lvl);
        } else if (type == XpType.FISHING) {
             setFishingLvl(lvl);
        } else if (type == XpType.MINING) {
             setMiningLvl(lvl);
        }
    }

}
