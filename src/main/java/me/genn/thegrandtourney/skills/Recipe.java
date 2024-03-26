package me.genn.thegrandtourney.skills;

import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.item.MMOItem;
import me.genn.thegrandtourney.xp.Xp;
import me.genn.thegrandtourney.xp.XpType;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Recipe {
    String name;
    public String displayName;
    public long timeLimit;
    double craftingScore;
    public long bronzeThreshold;
    public long silverThreshold;
    public long goldThreshold;
    public Map<MMOItem, Integer> components;
    public MMOItem reward;
    public int levelRequired;
    public XpType type;
    public List<String> compString;
    public double craftingScorePerTask;

    public Recipe(){
        this.components = new HashMap<>();
        this.compString = new ArrayList<>();
    }
    public static Recipe create(ConfigurationSection config, TGT plugin, String name) {
        Recipe recipe = new Recipe();
        recipe.name = name + " recipe";
        recipe.displayName = config.getString("name");
        if (recipe.displayName == null) {
            return null;
        }
        recipe.displayName = ChatColor.translateAlternateColorCodes('&', recipe.displayName);
        recipe.type = Xp.parseXpType(config.getString("type"));
        if (recipe.type == null) {
            return null;
        }
        recipe.timeLimit = config.getLong("time-limit", 60L);
        recipe.craftingScore = config.getDouble("crafting-score", 100);
        recipe.craftingScorePerTask = config.getDouble("crafting-score-per-task", 20);
        recipe.bronzeThreshold = config.getLong("bronze-threshold", 40L);
        recipe.silverThreshold = config.getLong("silver-threshold", 25L);
        recipe.goldThreshold = config.getLong("gold-threshold", 15L);

        recipe.levelRequired = config.getInt("level-requirement", 1);
        recipe.compString = config.getStringList("components");

        return recipe;
    }

    public void createStep2(TGT plugin) {
        String[] parts1 = this.name.split(" ");
        this.reward = plugin.itemHandler.getMMOItemFromString(parts1[0]);
        List<String> comps = this.compString;
        for (String str : comps) {
            String[] parts = str.split(" ");
            MMOItem item =  plugin.itemHandler.getMMOItemFromString(parts[0]);
            try {
                int quantity = Integer.parseInt(parts[1]);
                this.components.put(item, quantity);
            } catch (NumberFormatException e) {
                return;
            }
        }
    }

    public String getDisplayName() {
        return ChatColor.stripColor(this.displayName);
    }

}
