package me.genn.thegrandtourney.xp;

import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.item.MMOItem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

public class RewardTable {
    XpType type;
    Map<Integer, LevelReward> rewardMap;

    public RewardTable() {
        this.rewardMap = new HashMap<>();
    }



    public static RewardTable create(ConfigurationSection config, XpType type) throws IOException {
        RewardTable table = new RewardTable();
        table.rewardMap = new HashMap<>();
        table.type = type;
        Iterator var4 = config.getKeys(false).iterator();
        while(var4.hasNext()) {
            String key = (String)var4.next();
            try {
                int levelNum = Integer.parseInt(key);
                LevelReward reward = LevelReward.create(config.getConfigurationSection(key));
                if (reward != null) {
                    table.rewardMap.put(levelNum, reward);
                    JavaPlugin.getPlugin(TGT.class).getLogger().log(Level.INFO, "Adding rewards for level " + levelNum);
                }
            } catch (NumberFormatException e) {
                continue;
            }

        }
        if (table.rewardMap.size() < 1) {
            return null;
        }
        return table;

    }

    public XpType getType() {
        return this.type;
    }

    public LevelReward getRewardsForLevel(int lvl) {
        return rewardMap.get(lvl);
    }
}
