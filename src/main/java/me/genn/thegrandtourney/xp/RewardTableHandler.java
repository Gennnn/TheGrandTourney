package me.genn.thegrandtourney.xp;

import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.skills.mining.Ore;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

public class RewardTableHandler {
    public List<RewardTable> allRewardTables;

    public void registerRewardTables(ConfigurationSection config) throws IOException {
        this.allRewardTables = new ArrayList<>();
        Iterator var4 = config.getKeys(false).iterator();
        while(var4.hasNext()) {
            String key = (String)var4.next();
            if (Xp.parseXpType(key) == null) {
                continue;
            }
            XpType type = Xp.parseXpType(key);
            RewardTable table = RewardTable.create(config.getConfigurationSection(key), type);
            if (table != null) {
                allRewardTables.add(table);
            }
        }

    }

    public RewardTable getTableForType(final XpType type){
        return this.allRewardTables.stream().filter(o -> o.getType().equals(type)).findFirst().orElse(null);
    }
}
