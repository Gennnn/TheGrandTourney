package me.genn.thegrandtourney.item;

import me.genn.thegrandtourney.TGT;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class DropTable {
    public List<Drop> drops;
    public boolean calculateDropsIndividually;
    TGT plugin;

    public DropTable(TGT plugin) {
        this.plugin = plugin;
        this.drops = new ArrayList<>();

    }
    public void addDropsFromSection(ConfigurationSection section) {
        Iterator iter = section.getKeys(false).iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            ConfigurationSection drop = section.getConfigurationSection(key);
            Drop dropObj = new Drop();
            if (drop.getString("item") != null) {
                dropObj.drop = plugin.itemHandler.getMMOItemFromString(drop.getString("item"));
            }
            if (dropObj.drop == null) {
                continue;
            }
            dropObj.name = key;
            if (drop.getString("quantity").contains("-")) {
                String[] parts = drop.getString("quantity").split("-");
                dropObj.minQuantity = Integer.parseInt(parts[0]);
                dropObj.maxQuantity = Integer.parseInt(parts[1]);
            } else {
                dropObj.minQuantity = drop.getInt("quantity", 1);
                dropObj.maxQuantity = drop.getInt("quantity", 1);
            }

            dropObj.chance = drop.getDouble("chance", 100);

            dropObj.weight = drop.getInt("weight", 1);
            addDrop(dropObj);
        }
    }
    public Drop itemDrop(ConfigurationSection section) {
        Drop drop = new Drop();
        drop.type = DropType.ITEM;
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
        ItemStack bIteam = plugin.itemHandler.getItem(drop.drop).asQuantity(r.nextInt(drop.minQuantity, drop.maxQuantity+1));
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
                ItemStack bIteam = plugin.itemHandler.getItem(drop.drop).asQuantity(r.nextInt(drop.minQuantity, drop.maxQuantity+1));
                p.getInventory().addItem(bIteam);

            }
        }
    }
}
