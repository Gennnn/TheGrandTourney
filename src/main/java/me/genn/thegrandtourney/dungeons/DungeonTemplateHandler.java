package me.genn.thegrandtourney.dungeons;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.skills.farming.Crop;
import me.genn.thegrandtourney.skills.fishing.FishingZone;
import me.genn.thegrandtourney.skills.fishing.FishingZoneTemplate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class DungeonTemplateHandler {

    public List<DungeonTemplate> allTemplates;

    public List<Dungeon> allDungeons;



    public DungeonTemplateHandler() {
    }
    public void registerTemplates(TGT plugin, ConfigurationSection config) throws IOException {
        this.allDungeons = new ArrayList<>();
        this.allTemplates = new ArrayList<>();
        Iterator var4 = config.getKeys(false).iterator();
        while(var4.hasNext()) {
            String key = (String)var4.next();
            DungeonTemplate template = DungeonTemplate.create(config.getConfigurationSection(key), plugin);
            if (template != null) {
                this.allTemplates.add(template);
                plugin.getLogger().log(Level.INFO, "Register DT " + template.name);
            } else {
                plugin.getLogger().severe("DT " + key + " was empty!");
            }
        }

    }


    private List<Dungeon> listOfDungeonsWithTemplateName(final List<Dungeon> list, final String name){
        return list.stream().filter(o -> o.getName().equals(name)).toList();
    }

    private Dungeon getClosestDungeon(List<Dungeon> dungeons, Location originLoc) {
        Dungeon dungeon = dungeons.get(0);
        Location minLoc = dungeon.centerLoc;
        for (int i = 1; i<dungeons.size(); i++) {
            if (dungeons.get(i).centerLoc.distanceSquared(originLoc) < minLoc.distanceSquared(originLoc)) {
                minLoc = dungeons.get(i).centerLoc;
                dungeon = dungeons.get(i);
            }
        }
        return dungeon;
    }

    public Dungeon getDungeonForObj(String name, Location originLoc) {
        List<Dungeon> zones = listOfDungeonsWithTemplateName(allDungeons, name);
        if (zones.size() == 0) {
            return null;
        } else if (zones.size() == 1) {
            return zones.get(0);
        } else {
            Dungeon zone = getClosestDungeon(zones, originLoc);
            return zone;
        }
    }

    public Dungeon getDungeonForRoom(Location location) {
        return this.allDungeons.stream().filter(o -> o.withinBounds(location)).findFirst().orElse(null);
    }
}
