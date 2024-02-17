package me.genn.thegrandtourney.skills.farming;

import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.item.MMOItem;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import scala.concurrent.impl.FutureConvertersImpl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class CropTemplate {
    Material block;
    String blockName;
    public String name;
    Map<MMOItem, Double> drops;
    int grownAge;
    int regeneratingAge;
    String regeneratingBlockName;
    Material regeneratingBlock;
    int regenerationTime;
    boolean agedCrop;
    public String grownBase64String;
    public String regeneratingBase64String;

    public static CropTemplate create(ConfigurationSection config) throws IOException {
        CropTemplate template = new CropTemplate();
        template.drops = new HashMap<>();
        TGT plugin = JavaPlugin.getPlugin(TGT.class);
        if (config.getString("block-name") == null) {
            return null;
        }
        template.blockName = config.getString("block-name");
        if (template.blockName.contains(":")) {
            String[] parts = template.blockName.split(":");
            template.blockName = parts[0];
            template.grownAge = Integer.parseInt(parts[1]);
            plugin.getLogger().log(Level.INFO, "Logged crop " + config.getName() + " with block name " + template.blockName + " and age " + template.grownAge);

        } else {
            template.regeneratingAge = 2;
        }

        template.regeneratingBlockName = config.getString("regenerating-block-name");
        if (template.regeneratingBlockName.contains(":")) {
            String[] parts = template.regeneratingBlockName.split(":");
            template.regeneratingBlockName = parts[0];
            template.regeneratingAge = Integer.parseInt(parts[1]);
            plugin.getLogger().log(Level.INFO, "Logged crop " + config.getName() + " with regenerating block name " + template.regeneratingBlockName + " and age " + template.regeneratingAge);
        } else {
            template.regeneratingAge = 2;
        }

        template.block = Material.matchMaterial("minecraft:" + template.blockName);

        template.regeneratingBlock = Material.matchMaterial("minecraft:" + template.regeneratingBlockName);
        List<String> dropsList = config.getStringList("drops");
        for (String str : dropsList) {
            String[] parts = str.split(" ");
            template.drops.put(plugin.itemHandler.getMMOItemFromString(parts[0]), Double.valueOf(parts[1]));
        }
        template.regenerationTime = config.getInt("regeneration-time", 10);
        template.name = config.getName();
        if (template.block == template.regeneratingBlock) {
            template.agedCrop = true;
        } else {
            template.agedCrop = false;
        }
        String base64Grown = config.getString("grown-base64");
        if (base64Grown != null) {
            template.grownBase64String = base64Grown;
        }
        String regeneratingBase64 = config.getString("regenerating-base64");
        if (regeneratingBase64 != null) {
            template.regeneratingBase64String = regeneratingBase64;
        }
        return template;
    }

    public String getName() {
        return this.name;
    }
}
