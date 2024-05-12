package me.genn.thegrandtourney.item;

import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.dungeons.Room;
import me.genn.thegrandtourney.dungeons.RoomGoal;
import me.genn.thegrandtourney.skills.fishing.Fish;
import me.genn.thegrandtourney.xp.XpType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class DropTable {
    public List<Drop> drops;
    public List<Fish> fishDrops;
    public List<Fish> fishCreatures;
    public boolean calculateDropsIndividually = false;
    public boolean overflowDrops = false;
    public int moneyDropMin = -1;
    public int moneyDropMax;
    public XpType xpDropType;
    public double xpDropMin;
    public double xpDropMax;
    TGT plugin;

    public DropTable(TGT plugin, boolean calculateDropsIndividually, boolean overflowDrops) {
        this.plugin = plugin;
        this.drops = new ArrayList<>();
        this.fishCreatures = new ArrayList<>();
        this.calculateDropsIndividually = calculateDropsIndividually;
        this.overflowDrops = overflowDrops;
    }
    public void addDropsFromSection(ConfigurationSection section) {
        Iterator iter = section.getKeys(false).iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();

            ConfigurationSection drop = section.getConfigurationSection(key);
            if (key.equalsIgnoreCase("money")) {
                parseMoneyDrop(drop);
                continue;
            } else if (key.equalsIgnoreCase("xp")) {
                parseXpDrop(drop);
                continue;
            }
            String type = drop.getString("type", "item");
            Drop dropObj = new Drop();
            if (type.equalsIgnoreCase("item")) {
                dropObj = itemDrop(drop);
                addDrop(dropObj);
            }
        }
    }
    public void addFishFromSection(ConfigurationSection section) {
        Iterator iter = section.getKeys(false).iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();

            ConfigurationSection drop = section.getConfigurationSection(key);
            if (key.equalsIgnoreCase("money")) {
                parseMoneyDrop(drop);
                continue;
            } else if (key.equalsIgnoreCase("xp")) {
                parseXpDrop(drop);
                continue;
            }
            String type = drop.getString("type", "item");
            Fish dropObj = new Fish();
            if (type.equalsIgnoreCase("item")) {
                dropObj = fishDrop(drop);
                addDrop(dropObj);
            } else if (type.equalsIgnoreCase("mob")) {
                dropObj = mobDrop(drop);
                addDrop(dropObj);
            }
        }
    }
    public void parseXpDrop(ConfigurationSection section) {
        if (section.getString("type") != null) {
            this.xpDropType = XpType.valueOf((Objects.requireNonNull(section.getString("type"))).toUpperCase());
            if (section.getString("quantity").contains("-")) {
                String[] parts = section.getString("quantity").split("-");
                this.xpDropMin = Double.parseDouble(parts[0]);
                this.xpDropMax = Double.parseDouble(parts[1]);
            } else {
                this.xpDropMin = section.getDouble("quantity", 1);
                this.xpDropMax = section.getDouble("quantity", 1);
            }
        }
    }
    public void parseMoneyDrop(ConfigurationSection section) {
        if (section.getString("quantity").contains("-")) {
            String[] parts = section.getString("quantity").split("-");
            this.moneyDropMin = Integer.parseInt(parts[0]);
            this.moneyDropMax = Integer.parseInt(parts[1]);
        } else {
            this.moneyDropMin = section.getInt("quantity", 1);
            this.moneyDropMax = section.getInt("quantity", 1);
        }
    }
    public Drop itemDrop(ConfigurationSection section) {
        Drop drop = new Drop();
        if (section.getString("item") != null) {
            drop.drop = plugin.itemHandler.getMMOItemFromString(section.getString("item"));
        }
        if (drop.drop == null) {
            return null;
        }
        drop.name = section.getName();
        if (section.getString("quantity").contains("-")) {
            String[] parts = section.getString("quantity").split("-");
            drop.minQuantity = Integer.parseInt(parts[0]);
            drop.maxQuantity = Integer.parseInt(parts[1]);
        } else {
            drop.minQuantity = section.getInt("quantity", 1);
            drop.maxQuantity = section.getInt("quantity", 1);
        }

        drop.chance = section.getDouble("chance", 100);

        drop.weight = section.getInt("weight", 1);
        if (section.contains("quality")){
            drop.quality = section.getString("quality");
        }
        return drop;
    }

    public Fish fishDrop(ConfigurationSection section) {
        Fish drop = new Fish();
        if (section.getString("item") != null) {
            drop.drop = plugin.itemHandler.getMMOItemFromString(section.getString("item"));
        }
        if (drop.drop == null) {
            return null;
        }
        drop.name = section.getName();
        if (section.getString("quantity").contains("-")) {
            String[] parts = section.getString("quantity").split("-");
            drop.minQuantity = Integer.parseInt(parts[0]);
            drop.maxQuantity = Integer.parseInt(parts[1]);
        } else {
            drop.minQuantity = section.getInt("quantity", 1);
            drop.maxQuantity = section.getInt("quantity", 1);
        }

        drop.chance = section.getDouble("chance", 100);

        drop.weight = section.getInt("weight", 1);

        drop.minTime = section.getInt("min-time", 1);
        drop.maxTime = section.getInt("max-time", 4);
        if (section.contains("quality")){
            drop.quality = section.getString("quality");
        }
        return drop;
    }
    public Fish mobDrop(ConfigurationSection section) {
        Fish drop = new Fish();
        if (section.getString("mob") != null) {
            drop.mob = plugin.mobHandler.getMobFromString(section.getString("mob"));
        }
        if (drop.mob == null) {
            return null;
        }
        drop.name = section.getName();
        if (section.getString("quantity").contains("-")) {
            String[] parts = section.getString("quantity").split("-");
            drop.minQuantity = Integer.parseInt(parts[0]);
            drop.maxQuantity = Integer.parseInt(parts[1]);
        } else {
            drop.minQuantity = section.getInt("quantity", 1);
            drop.maxQuantity = section.getInt("quantity", 1);
        }

        drop.chance = section.getDouble("chance", 100);

        drop.weight = section.getInt("weight", 1);

        drop.minTime = section.getInt("min-time", 1);
        drop.maxTime = section.getInt("max-time", 4);
        return drop;
    }

    public void addDrop (Drop drop) {
        drops.add(drop);
    }
    public void mobDrop (Fish drop) {
        fishCreatures.add(drop);
    }

    public void calculateDrops(Player p, float bonus) {
        if (calculateDropsIndividually) {
            calculateDropsIndividual(p, bonus);
        } else {
            calculateDropsNonIndividual(p, bonus);
        }

    }
    public void calculateDropsNonIndividual(Player p, float bonus) {
        if (drops.size() < 1) {
            return;
        }
        Iterator iter = this.drops.iterator();
        List<Drop> dropsWithWeight = new ArrayList<>();
        while (iter.hasNext()) {
            Drop drop = (Drop) iter.next();
            for (int i = 0; i < drop.weight; i++) {
                dropsWithWeight.add(drop);
            }
        }
        Random r = new Random();
        Drop drop = dropsWithWeight.get(r.nextInt(dropsWithWeight.size()));

        int bonusAmount = 0;
        while (bonus > 100) {
            bonusAmount++;
            bonus-=100;
        }
        if (bonus > r.nextInt(100)) {
            bonusAmount++;
        }
        int quantity = r.nextInt((int)drop.minQuantity, (int)drop.maxQuantity+1) + bonusAmount;
        ItemStack bItem;
        if (!drop.quality.equalsIgnoreCase("normal")) {
            bItem = plugin.itemHandler.getItemWithQuality(drop.drop, drop.quality).asQuantity(quantity);
        } else {
            bItem = plugin.itemHandler.getItem(drop.drop).asQuantity(quantity);
        }
        p.getInventory().addItem(bItem);
        this.checkDungeonRoom(p, bItem, quantity);
        grantXpAndGold(p, r);
    }
    public Fish getDropFish(Player p) {
        if (drops.size() < 1 && fishCreatures.size() < 1) {
            return null;
        }
        Random r = new Random();
        List<Fish> dropsWithWeight = new ArrayList<>();
        if (r.nextDouble() * 100 <= plugin.players.get(p.getUniqueId()).getSeaCreatureChance()) {
            for (Fish fishCreature : this.fishCreatures) {
                Fish drop = fishCreature;
                for (int i = 0; i < drop.weight; i++) {
                    dropsWithWeight.add(drop);
                }
            }
        } else {
            for (Drop fishCreature : this.drops) {
                for (int i = 0; i < (fishCreature).weight; i++) {
                    dropsWithWeight.add((Fish)fishCreature);
                }
            }
        }

        return dropsWithWeight.get(r.nextInt(dropsWithWeight.size()));
        /*int quantity = r.nextInt((int)drop.minQuantity, (int)drop.maxQuantity+1);
        ItemStack bItem = plugin.itemHandler.getItem(drop.drop).asQuantity(quantity);
        p.getInventory().addItem(bItem);
        this.checkDungeonRoom(p, bItem, quantity);
        if (this.xpDropType != null) {
            plugin.xpHandler.grantXp(xpDropType, p, r.nextDouble(this.xpDropMin, this.xpDropMax+0.1));
        }
        if (this.moneyDropMin != -1) {
            plugin.players.get(p.getUniqueId()).addPurseGold(r.nextInt(this.moneyDropMin, this.moneyDropMax+1));

        }*/
    }
    public void calculateDropsIndividual(Player p, float bonus) {
        if (drops.size() < 1) {
            return;
        }
        Iterator iter = this.drops.iterator();
        Random r = new Random();
        while (iter.hasNext()) {
            Drop drop = (Drop) iter.next();
            if (drop.chance >= (r.nextDouble() * 100)) {
                int quantity = r.nextInt((int)drop.minQuantity, (int)drop.maxQuantity+1);
                int bonusAmount = 0;
                while (bonus > 100) {
                    bonusAmount++;
                    bonus-=100;
                }
                if (bonus > r.nextInt(100)) {
                    bonusAmount++;
                }
                ItemStack bItem;
                if (!drop.quality.equalsIgnoreCase("normal")) {
                    bItem = plugin.itemHandler.getItemWithQuality(drop.drop, drop.quality).asQuantity(quantity);
                } else {
                    bItem = plugin.itemHandler.getItem(drop.drop).asQuantity(quantity);
                }
                p.getInventory().addItem(bItem);
                this.checkDungeonRoom(p, bItem,quantity+ bonusAmount);
            }
        }
        grantXpAndGold(p, r);

    }

    public List<ItemStack> calculateDungeonChestDrops(Player p) {
        List<ItemStack> retList = new ArrayList<>();
        if (drops.size() < 1) {
            return retList;
        }
        Iterator iter = this.drops.iterator();
        Random r = new Random();
        while (iter.hasNext()) {
            Drop drop = (Drop) iter.next();
            if (drop.chance >= (r.nextDouble() * 100)) {
                int quantity = r.nextInt((int)drop.minQuantity, (int)drop.maxQuantity+1);
                ItemStack bItem;
                if (!drop.quality.equalsIgnoreCase("normal")) {
                    bItem = plugin.itemHandler.getItemWithQuality(drop.drop, drop.quality).asQuantity(quantity);
                } else {
                    bItem = plugin.itemHandler.getItem(drop.drop).asQuantity(quantity);
                }
                retList.add(bItem);
                this.checkDungeonRoom(p, bItem,quantity);
            }
        }
        if (this.xpDropType != null) {
            ItemStack xpItem = new ItemStack(Material.EXPERIENCE_BOTTLE);
            ItemMeta meta = xpItem.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + this.xpDropType.getName() + " XP" + ChatColor.DARK_GREEN + " x" + Math.round(this.xpDropMax));
            xpItem.setItemMeta(meta);
            retList.add(xpItem);
        }
        if (this.moneyDropMax > 0) {
            ItemStack moneyItem = new ItemStack(Material.GOLD_NUGGET);
            ItemMeta meta = moneyItem.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + "Dosh" + ChatColor.GOLD + " x" + this.moneyDropMax);
            moneyItem.setItemMeta(meta);
            retList.add(moneyItem);
        }
        return retList;
    }

    public void grantXpAndGold(Player p, Random r) {
        grantXp(p, r);
        grantGold(p, r);
    }
    public void grantXp(Player p, Random r) {
        if (this.xpDropType != null) {
            if (this.xpDropMin == this.xpDropMax) {
                plugin.xpHandler.grantXp(xpDropType, p, this.xpDropMax);
            } else {
                plugin.xpHandler.grantXp(xpDropType, p, r.nextDouble(this.xpDropMin, this.xpDropMax+0.1));
            }
        }
    }
    public void grantGold(Player p, Random r) {
        if (this.moneyDropMin != -1) {
            if (this.moneyDropMin == this.moneyDropMax) {
                plugin.players.get(p.getUniqueId()).addPurseGold(this.moneyDropMax);
            } else {
                plugin.players.get(p.getUniqueId()).addPurseGold(r.nextInt(this.moneyDropMin, this.moneyDropMax+1));
            }

        }
    }
    public void checkDungeonRoom(Player player, ItemStack item, int quantity) {
        if (plugin.playerAndDungeonRoom.containsKey(player.getUniqueId())) {
            Room room = plugin.playerAndDungeonRoom.get(player.getUniqueId());
            if (room.goal != RoomGoal.COLLECTION) {
                return;
            }
            if (room.itemToCollect != null) {
                if (plugin.itemHandler.itemIsMMOItemOfName(item, room.itemToCollect.internalName)) {
                    if (room.withinBounds(player.getLocation())) {
                        for (int i = 0; i < quantity; i++) {
                            room.incrementPlayerProgress(player);
                        }
                    }
                }
            }

        }
    }
}
