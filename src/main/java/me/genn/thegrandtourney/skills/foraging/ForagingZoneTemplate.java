package me.genn.thegrandtourney.skills.foraging;

import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.item.DropTable;
import me.genn.thegrandtourney.item.MMOItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.*;

public class ForagingZoneTemplate {
    public DropTable chopDrops;
    public DropTable fellDrops;
    public String name;
    public List<Material> logBlocks;
    public List<Material> leafBlocks;
    public double health;
    public int staminaCost;
    public int regenTime;
    public int defense;
    public int regenRate;
    public String displayName;
    public ItemStack fruitItem;



    public static ForagingZoneTemplate create(ConfigurationSection config) throws IOException {
        ForagingZoneTemplate zone = new ForagingZoneTemplate();

        TGT plugin = JavaPlugin.getPlugin(TGT.class);
        boolean calculateDropsIndividually = config.getBoolean("calculate-drops-individually", false);
        boolean overflowDrops = config.getBoolean("overflow-drops", false);
        zone.chopDrops = new DropTable(plugin, calculateDropsIndividually, overflowDrops);
        if (config.contains("drops")) {
            ConfigurationSection section = config.getConfigurationSection("drops");
            zone.chopDrops.addDropsFromSection(section);
        }

        calculateDropsIndividually = config.getBoolean("fell-calculate-drops-individually", false);
        overflowDrops = config.getBoolean("fell-overflow-drops", false);
        zone.fellDrops = new DropTable(plugin, calculateDropsIndividually, overflowDrops);
        if (config.contains("fell-drops")) {
            ConfigurationSection section = config.getConfigurationSection("fell-drops");
            zone.fellDrops.addDropsFromSection(section);
        }
        zone.name = config.getName();
        List<String> list = new ArrayList<>();
        zone.logBlocks = new ArrayList<>();
        zone.leafBlocks = new ArrayList<>();
        list.addAll(config.getStringList("log-blocks"));
        for (String str : list) {
            zone.logBlocks.add(Material.matchMaterial("minecraft:" + str));
        }
        list.clear();
        list.addAll(config.getStringList("leaf-blocks"));
        for (String str : list) {
            zone.leafBlocks.add(Material.matchMaterial("minecraft:" + str));
        }
        zone.regenRate = config.getInt("regen-rate", 3);
        zone.regenTime = config.getInt("regen-time", 30);
        zone.defense = config.getInt("defense", 5);
        zone.staminaCost = config.getInt("stamina-cost", 3);
        zone.health = config.getInt("health", 250);
        zone.displayName = ChatColor.translateAlternateColorCodes('&', config.getString("display-name", "Tree"));
        zone.fruitItem = plugin.itemHandler.getItemFromString(config.getString("fruit-item", "apple"));
        return zone;
    }

    public String getName() {
        return this.name;
    }



}
