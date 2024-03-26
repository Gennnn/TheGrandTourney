package me.genn.thegrandtourney.xp;

import me.genn.thegrandtourney.TGT;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class LevelReward {
    public List<String> rewardText;
    public List<String> rewardCommands;

    public LevelReward() {
        this.rewardText = new ArrayList<>();
        this.rewardCommands = new ArrayList<>();
    }

    public static LevelReward create(ConfigurationSection section) {
        LevelReward reward = new LevelReward();
        List<String> list = section.getStringList("reward-text");
        if (list.size() > 0) {
            for (String str : list) {
                reward.rewardText.add(ChatColor.translateAlternateColorCodes('&', str));
            }
        } else {
            return null;
        }
        list = section.getStringList("reward-commands");
        if (list.size() > 0) {
            reward.rewardCommands.addAll(list);

        } else {
            return null;
        }
        return reward;
    }
}
