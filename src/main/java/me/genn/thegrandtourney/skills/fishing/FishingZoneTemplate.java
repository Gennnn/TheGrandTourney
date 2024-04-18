package me.genn.thegrandtourney.skills.fishing;

import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.item.DropTable;
import me.genn.thegrandtourney.item.MMOItem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.*;

public class FishingZoneTemplate {

    public DropTable drops;
    public String name;
    int minTimeModifier;
    int maxTimeModifier;

    public static FishingZoneTemplate create(ConfigurationSection config) throws IOException {
        FishingZoneTemplate zone = new FishingZoneTemplate();

        TGT plugin = JavaPlugin.getPlugin(TGT.class);
        boolean calculateDropsIndividually = config.getBoolean("calculate-drops-individually", false);
        boolean overflowDrops = config.getBoolean("overflow-drops", false);
        zone.drops = new DropTable(plugin, calculateDropsIndividually, overflowDrops);
        if (config.contains("drops")) {
            ConfigurationSection section = config.getConfigurationSection("drops");
            zone.drops.addFishFromSection(section);
        }

        zone.name = config.getName();
        zone.minTimeModifier = config.getInt("min-time-modifier", 0);
        zone.maxTimeModifier = config.getInt("max-time-modifier", 0);
        return zone;
    }

    public String getName() {
        return this.name;
    }

    public Fish selectDrop(Player player) {
        return this.drops.getDropFish(player);
    }

}
