package me.genn.thegrandtourney.item;

import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.xp.XpType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class DropTable {
    public List<Drop> drops;
    public boolean calculateDropsIndividually = false;
    public boolean overflowDrops = false;
    public int moneyDropMin;
    public int moneyDropMax;
    public XpType xpDropType;
    public double xpDropMin;
    public double xpDropMax;
    TGT plugin;

    public DropTable(TGT plugin, boolean calculateDropsIndividually, boolean overflowDrops) {
        this.plugin = plugin;
        this.drops = new ArrayList<>();
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
        return drop;
    }

    public void addDrop (Drop drop) {
        drops.add(drop);
    }

    public void calculateDrops(Player p) {
        if (calculateDropsIndividually) {
            calculateDropsIndividual(p);
        } else {
            calculateDropsNonIndividual(p);
        }

    }
    public void calculateDropsNonIndividual(Player p) {
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
        ItemStack bIteam = plugin.itemHandler.getItem(drop.drop).asQuantity(r.nextInt((int)drop.minQuantity, (int)drop.maxQuantity+1));
        p.getInventory().addItem(bIteam);
    }
    public void calculateDropsIndividual(Player p) {
        if (drops.size() < 1) {
            return;
        }
        Iterator iter = this.drops.iterator();
        Random r = new Random();
        while (iter.hasNext()) {
            Drop drop = (Drop) iter.next();
            if (drop.chance >= (r.nextDouble() * 100)) {
                ItemStack bIteam = plugin.itemHandler.getItem(drop.drop).asQuantity(r.nextInt((int)drop.minQuantity, (int)drop.maxQuantity+1));
                p.getInventory().addItem(bIteam);

            }
        }
    }
}
