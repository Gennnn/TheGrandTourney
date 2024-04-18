package me.genn.thegrandtourney.mobs;

import java.io.IOException;
import java.util.Iterator;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.MythicBukkit;
import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.item.DropTable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;


import net.md_5.bungee.api.ChatColor;

public class MMOMob {
    public MythicMob mythicmob;
    public String nameplateName;
    public EntityType entityType;
    public boolean isFakePlayer = false;
    public float defense;
    public String internalName;
    public DropTable dropTable;
    public int level;

    TGT plugin;



    public static MMOMob create(ConfigurationSection config) throws IOException {
        MMOMob mob = new MMOMob();
        mob.plugin = JavaPlugin.getPlugin(TGT.class);
        mob.internalName = config.getName();

        mob.mythicmob = MythicBukkit.inst().getMobManager().getMythicMob(mob.internalName).stream().findFirst().orElse(null);
        if (mob.mythicmob == null) {
            mob.plugin.getLogger().severe("MMMob " + mob.internalName + " had no matching MythicMob!");
            return null;
        }
        mob.nameplateName = ChatColor.translateAlternateColorCodes('&',config.getString("display-name"));
        mob.entityType = EntityType.valueOf(mob.mythicmob.getEntityTypeString().toUpperCase());
        if (mob.entityType == null) {
            mob.entityType = EntityType.ZOMBIE;
        }
        mob.level = config.getInt("level", 1);
        if (mob.mythicmob.isFakePlayer()) {
            mob.isFakePlayer = true;
        }
        String isFake = config.getString("is-player");
        if (isFake != null) {
            mob.isFakePlayer = true;
        }
        mob.defense = config.getInt("defense", 5);
        ConfigurationSection dropsSection = config.getConfigurationSection("drops");
        boolean calculateDropsIndividually = config.getBoolean("calculate-drops-individually", false);
        boolean overflowDrops = config.getBoolean("overflow-drops", false);
        mob.dropTable = new DropTable(mob.plugin, calculateDropsIndividually, overflowDrops);
        if (dropsSection != null) {
            mob.dropTable.addDropsFromSection(dropsSection);
        }


        return mob;
    }

    public void calculateDrops(Player p) {
        dropTable.calculateDrops(p, plugin.players.get(p.getUniqueId()).getCombatFortune());
    }

    public String getName() {
        return this.internalName;
    }




}
