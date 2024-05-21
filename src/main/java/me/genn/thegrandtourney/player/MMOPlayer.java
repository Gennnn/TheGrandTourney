package me.genn.thegrandtourney.player;

import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.mobs.MMOMob;
import me.genn.thegrandtourney.skills.Craft;
import me.genn.thegrandtourney.skills.Recipe;
import me.genn.thegrandtourney.skills.Station;
import me.genn.thegrandtourney.util.IntMap;
import me.genn.thegrandtourney.xp.XpType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class MMOPlayer {
    public TGT plugin;

    public Objective trackedObjective;
    private float health;
    private float maxHealth;
    private float defense;
    private float strength;
    private float critDamage;
    private float speed;
    private float critChance;
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

    public ItemStack[] getQuiverContents() {
        return quiverContents;
    }

    public void setQuiverContents(ItemStack[] quiverContents) {
        this.quiverContents = quiverContents;
    }

    private ItemStack[] quiverContents = new ItemStack[27];

    public MMOPlayer() {
        this.slayerMap = new HashMap<>();
        this.accessoryBagContents = new ArrayList<>();
        this.storageContents = new HashMap<>();
        this.objectives = new ArrayList<>();
        this.completedObjectives = new ArrayList<>();
        this.recipeBook = new ArrayList<>();
        for (int i = 0; i < quiverContents.length; i++) {
            quiverContents[i] = new ItemStack(Material.AIR);
        }
    }
    public List<BankTransaction> transactionHistory = new ArrayList<>();
    public HashSet<StatBuff> buffs = new HashSet<>();
    public HashSet<PotionEffect> potionEffects = new HashSet<>();
    public boolean isCrafting = false;
    public Location craftStart;
    public String currentCraft = "none";
    public Craft currentCraftObj = null;
    public Station currentStation;
    public List<StatBuff> absorptionLayers = new ArrayList<>();
    public int mobileBankLvl = 0;
    public float getTotalHealth() {
        return health + absorptionHealth;
    }

    public void takeDamage(float damage) {
        if (absorptionLayers.size() > 0) {
            this.sortLayers();

            do {
                StatBuff layer = absorptionLayers.get(0);
                if (layer.amount > damage) {
                    layer.amount = layer.amount - damage;
                    damage = 0;
                    absorptionLayers.set(0, layer);
                } else if (layer.amount == damage) {
                    damage = 0;
                    absorptionLayers.remove(layer);
                } else if (layer.amount < damage) {
                    damage = damage - layer.amount;
                    absorptionLayers.remove(layer);
                }
            } while (damage > 0 && absorptionLayers.size() > 0);
        }
        if (potionEffects.size() > 0 && potionEffects.stream().anyMatch(o -> o.statsImpacted.stream().anyMatch(o2 -> o2.statName.equalsIgnoreCase("absorption")))) {
            for (PotionEffect effect : potionEffects) {
                StatBuff buff = effect.statsImpacted.stream().filter(o3 -> o3.statName.equalsIgnoreCase("absorption")).findFirst().orElse(null);
                if (buff != null && buff.amount > 0) {
                    if (buff.amount > damage) {
                        buff.amount = buff.amount - damage;
                        damage = 0;
                    } else if (buff.amount == damage) {
                        damage = 0;
                    } else if (buff.amount < damage) {
                        damage = damage - buff.amount;
                    }
                }
            }
        }
        if (potionEffects.size() > 0 || absorptionLayers.size() > 0) {
            float absorptionHealth = 0.0f;
            for (PotionEffect effect : potionEffects) {
                StatBuff buff = effect.statsImpacted.stream().filter(o3 -> o3.statName.equalsIgnoreCase("absorption")).findFirst().orElse(null);
                if (buff != null) {
                    absorptionHealth = absorptionHealth + buff.amount;
                }
            }
            for (StatBuff buff : absorptionLayers) {
                absorptionHealth = absorptionHealth + buff.amount;
            }
            this.absorptionHealth = absorptionHealth;
        }

        //plugin.updatePlayerHealth(this,-damage);
    }

    public boolean addPotionEffect(String effectName, int lvl, int timeInSecs, ItemStack activatingItem) {
        PotionEffect effect = new PotionEffect(effectName,System.currentTimeMillis() + (1000L * timeInSecs),lvl, activatingItem);
        if(potionEffects.stream().anyMatch(o -> o.name.equalsIgnoreCase(effectName))) {
            PotionEffect compEffect = potionEffects.stream().filter(o -> o.name.equalsIgnoreCase(effectName)).findFirst().orElse(null);
            if (compEffect!=null) {
                StatBuff oldAbsorption = compEffect.statsImpacted.stream().filter(o2 -> o2.statName.equalsIgnoreCase("absorption")).findFirst().orElse(null);
                StatBuff newAbsorption = effect.statsImpacted.stream().filter(o3 -> o3.statName.equalsIgnoreCase("absorption")).findFirst().orElse(null);

                if (lvl < compEffect.lvl) {
                    if (oldAbsorption != null && newAbsorption != null) {
                        if (oldAbsorption.amount < newAbsorption.amount) {
                            oldAbsorption.amount = newAbsorption.amount;
                            return true;
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                } else if (lvl == compEffect.lvl) {
                    if (effect.expiryTime <= compEffect.expiryTime) {
                        return false;
                    } else {
                        this.potionEffects.remove(compEffect);
                    }
                } else {
                    this.potionEffects.remove(compEffect);
                }
            }
        }
        effect.addStatsForName(this);
        this.potionEffects.add(effect);
        takeDamage(0);
        return true;
    }

    public float getEvasiveness() {
        return this.evasiveness;
    }

    public void setEvasiveness(float value) {
        this.evasiveness = value;
    }

    private float evasiveness = 0.0f;


    public void heal(float amount) {
        plugin.updatePlayerHealth(this, amount);
    }

    public float getAbsorptionHealth() {
        return absorptionHealth;
    }

    public void setAbsorptionHealth(float absorptionHealth) {
        this.absorptionHealth = absorptionHealth;
    }

    public void sortLayers() {
        absorptionLayers.sort(Comparator.comparingLong(StatBuff::getExpiryTime));
    }


    private float absorptionHealth = 0.0f;

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
    private float baseMaxMana;
    private float baseVendorPrice;
    private float baseDialogueSpeed;
    private int combatLvl;
    private int miningLvl;
    private int loggingLvl;
    private int fishingLvl;
    private int farmingLvl;
    private int smithingLvl;
    private int cookingLvl;

    public int getAlchemyLvl() {
        return alchemyLvl;
    }

    public void setAlchemyLvl(int alchemyLvl) {
        this.alchemyLvl = alchemyLvl;
    }

    public int getCarpentryLvl() {
        return carpentryLvl;
    }

    public void setCarpentryLvl(int carpentryLvl) {
        this.carpentryLvl = carpentryLvl;
    }

    private int alchemyLvl;
    private int carpentryLvl;
    private float combatProg;
    private float miningProg;
    private float loggingProg;
    private float fishingProg;
    private float farmingProg;
    private float smithingProg;

    public float getAlchemyProg() {
        return alchemyProg;
    }

    public void setAlchemyProg(float alchemyProg) {
        this.alchemyProg = alchemyProg;
    }

    public float getCarpentryProg() {
        return carpentryProg;
    }

    public void setCarpentryProg(float carpentryProg) {
        this.carpentryProg = carpentryProg;
    }

    private float alchemyProg;
    private float carpentryProg;


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

    public float getCombatFortune() {
        return combatFortune;
    }

    public void setCombatFortune(float combatFortune) {
        this.combatFortune = combatFortune;
    }

    public float getBaseCombatFortune() {
        return baseCombatFortune;
    }

    public void setBaseCombatFortune(float baseCombatFortune) {
        this.baseCombatFortune = baseCombatFortune;
    }

    public float getFishingFortune() {
        return fishingFortune;
    }

    public void setFishingFortune(float fishingFortune) {
        this.fishingFortune = fishingFortune;
    }

    public float getBaseFishingFortune() {
        return baseFishingFortune;
    }

    public void setBaseFishingFortune(float baseFishingFortune) {
        this.baseFishingFortune = baseFishingFortune;
    }

    private float combatFortune;
    private float baseCombatFortune;
    private float fishingFortune;
    private float baseFishingFortune;

    public float getSeaCreatureChance() {
        return seaCreatureChance;
    }

    public void setSeaCreatureChance(float seaCreatureChance) {
        this.seaCreatureChance = seaCreatureChance;
    }

    public float getBaseSeaCreatureChance() {
        return baseSeaCreatureChance;
    }

    public void setBaseSeaCreatureChance(float baseSeaCreatureChance) {
        this.baseSeaCreatureChance = baseSeaCreatureChance;
    }

    private float seaCreatureChance = 0.0f;
    private float baseSeaCreatureChance = 20.0f;

    public Location getRespawnLocation() {
        return respawnLocation;
    }

    public void setRespawnLocation(Location respawnLocation) {
        this.respawnLocation = respawnLocation;
    }

    private Location respawnLocation;

    public float getLuck() {
        return luck;
    }

    public void setLuck(float luck) {
        this.luck = luck;
    }

    private float luck = 0.0f;

    public float getBaseLuck() {
        return baseLuck;
    }

    public void setBaseLuck(float baseLuck) {
        this.baseLuck = baseLuck;
    }

    private float baseLuck = 0.0f;



    public float getAttackSpeed() {
        return attackSpeed;
    }

    public void setAttackSpeed(float attackSpeed) {
        this.attackSpeed = attackSpeed;
    }

    private float attackSpeed;

    private UUID minecraftUUID;
    double bankBalance = 0.0d;

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
            plugin.econ.withdrawPlayer(Bukkit.getOfflinePlayer(minecraftUUID), plugin.econ.getBalance(Bukkit.getOfflinePlayer(minecraftUUID)));
            return false;
        }
    }
    public double getBankGold() {

        return this.bankBalance;
    }
    public void addBankGold(double amount) {
        this.bankBalance += amount;
    }
    public boolean removeBankGold(double amount) {
        if (this.bankBalance >= amount) {
            this.bankBalance -= amount;
            return true;
        } else {
            this.bankBalance = 0;
            return false;
        }
    }

    public void addBankTransaction(double amount, long time, String actor) {
        BankTransaction transaction = new BankTransaction((float)amount,time,actor);
        this.transactionHistory.add(0,transaction);
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
    public List<ItemStack> soldItems = new ArrayList<>();

    public float getVigor() {
        return vigor;
    }

    public void setVigor(float vigor) {
        this.vigor = vigor;
    }

    public float getBaseVigor() {
        return baseVigor;
    }

    public void setBaseVigor(float baseVigor) {
        this.baseVigor = baseVigor;
    }

    public float vigor = 0.0f;
    public float baseVigor = 0.0f;

    public void setMana(float mana) {
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
    public long lastMobileBankUse = 0L;



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
        } else if (type == XpType.ALCHEMY) {
            return getAlchemyProg();
        } else if (type == XpType.CARPENTRY) {
            return getCarpentryProg();
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
        } else if (type == XpType.CARPENTRY) {
            setCarpentryProg((float)amount);
        } else if (type == XpType.ALCHEMY) {
            setAlchemyProg((float)amount);
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
        } else if (type == XpType.ALCHEMY) {
            return getAlchemyLvl();
        } else if (type == XpType.CARPENTRY) {
            return getCarpentryLvl();
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
        } else if (type == XpType.CARPENTRY) {
            setCarpentryLvl(lvl);
        } else if (type == XpType.ALCHEMY) {
            setAlchemyLvl(lvl);
        }
    }

}
