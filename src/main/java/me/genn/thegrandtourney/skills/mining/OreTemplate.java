package me.genn.thegrandtourney.skills.mining;


import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.item.MMOItem;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public class OreTemplate {
    public ItemStack resource;
    public VeinType type;
    public MMOItem drop;
    public float health;
    public int regenTime;
    public String name;
    public float width;
    public float height;
    public int staminaCost;
    public int regenRate;
    public String displayName;
    public int critPointRegenTime;
    public int defense;

    public static OreTemplate create(ConfigurationSection config) throws IOException {
        OreTemplate template = new OreTemplate();
        TGT plugin = JavaPlugin.getPlugin(TGT.class);
        if (config.getString("resource") == null) {
            return null;
        }
        template.resource = new ItemStack(Material.matchMaterial("minecraft:" + config.getString("resource", "apple")));
        if (template.resource == null) {
            return null;
        }
        template.type = VeinType.valueOf(config.getString("type", "small").toUpperCase());
        template.drop = plugin.itemHandler.getMMOItemFromString(config.getString("drop", "coal"));
        template.health = (float) config.getDouble("health", 100);
        template.regenTime = config.getInt("regen-time", 30);
        template.width = (float) config.getDouble("width", 1.5);
        template.height = (float) config.getDouble("height", 1.5);
        template.staminaCost = config.getInt("stamina-cost", 10);
        template.regenRate = config.getInt("regen-rate", 2);
        template.defense = config.getInt("defense", 10);
        template.name = config.getName();
        template.displayName = ChatColor.translateAlternateColorCodes('&', config.getString("display-name"));
        template.critPointRegenTime = config.getInt("crit-point-regen-time", 15);
        return template;
    }

    public String getName() {
        return this.name;
    }
}

