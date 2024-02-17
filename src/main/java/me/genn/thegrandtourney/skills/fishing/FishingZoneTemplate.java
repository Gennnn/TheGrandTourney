package me.genn.thegrandtourney.skills.fishing;

import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.item.MMOItem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.*;

public class FishingZoneTemplate {
    List<Fish> drops;
    public String name;
    int minTimeModifier;
    int maxTimeModifier;
    public static FishingZoneTemplate create(ConfigurationSection config) throws IOException {
        FishingZoneTemplate zone = new FishingZoneTemplate();
        zone.drops = new ArrayList<>();
        TGT plugin = JavaPlugin.getPlugin(TGT.class);
        ConfigurationSection fishSection = config.getConfigurationSection("drops");
        Iterator iter = fishSection.getKeys(false).iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            ConfigurationSection fish = fishSection.getConfigurationSection(key);
            Fish fishObj = new Fish();
            if (fish.getString("item") != null) {
                fishObj.drop = plugin.itemHandler.getMMOItemFromString(fish.getString("item"));
            }
            if (fish.getString("mob") != null) {
                fishObj.mob = plugin.mobHandler.getMobFromString(fish.getString("mob"));
            }
            if (fishObj.drop == null && fishObj.mob == null) {
                continue;
            }
            fishObj.name = key;
            if (fish.getString("quantity").contains("-")) {
                String[] parts = fish.getString("quantity").split("-");
                fishObj.minQuantity = Integer.parseInt(parts[0]);
                fishObj.maxQuantity = Integer.parseInt(parts[1]);
            } else {
                fishObj.minQuantity = fish.getInt("quantity", 1);
                fishObj.maxQuantity = fish.getInt("quantity", 1);
            }

            fishObj.chance = fish.getInt("chance", 100);
            fishObj.minTime = fish.getInt("min-time", 1);
            fishObj.maxTime = fish.getInt("max-time", 4);
            zone.drops.add(fishObj);
        }

        zone.name = config.getName();
        zone.minTimeModifier = config.getInt("min-time-modifier", 0);
        zone.maxTimeModifier = config.getInt("max-time-modifier", 0);
        return zone;
    }

    public String getName() {
        return this.name;
    }

    public Fish selectDrop(Random r) {
        List<Fish> weightedList = new ArrayList<>();
        Iterator iter = drops.iterator();
        while (iter.hasNext()) {
            Fish fish = (Fish) iter.next();
            for (int i = 0; i < fish.chance; i++) {
                weightedList.add(fish);
            }
        }
        return weightedList.get(r.nextInt(weightedList.size()));
    }

}
