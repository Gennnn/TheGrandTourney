package me.genn.thegrandtourney.skills.foraging;

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

public class ForagingZoneHandler {

    public List<ForagingZoneTemplate> allZones;

    public List<ForagingZone> allSpawnedZones;



    public ForagingZoneHandler() {
    }
    public void registerZones(TGT plugin, ConfigurationSection config) throws IOException {
        this.allZones = new ArrayList<>();
        this.allSpawnedZones = new ArrayList<>();
        Iterator var4 = config.getKeys(false).iterator();
        while(var4.hasNext()) {
            String key = (String)var4.next();
            ForagingZoneTemplate zone = ForagingZoneTemplate.create(config.getConfigurationSection(key));
            if (zone != null) {
                this.allZones.add(zone);
                plugin.getLogger().log(Level.INFO, "Registered foraging zone " + zone.name);
            } else {
                plugin.getLogger().severe("Foraging Zone Template " + key + " was empty!");
            }
        }

    }
    public boolean containsName(final List<ForagingZoneTemplate> list, final String name){
        return list.stream().map(ForagingZoneTemplate::getName).filter(name::equals).findFirst().isPresent();
    }

    private List<ForagingZone> listOfZonesWithTemplateName(final List<ForagingZone> list, final String name){
        return list.stream().filter(o -> o.getName().equals(name)).toList();
    }

    private ForagingZone getClosestZone(List<ForagingZone> zones, Location originLoc) {
        ForagingZone zone = zones.get(0);
        Location minLoc = zone.centerLoc;
        for (int i = 1; i<zones.size(); i++) {
            if (zones.get(i).centerLoc.distanceSquared(originLoc) < minLoc.distanceSquared(originLoc)) {
                minLoc = zones.get(i).centerLoc;
                zone = zones.get(i);
            }
        }
        return zone;
    }

    public ForagingZone getZoneForObj(String name, Location originLoc) {
        List<ForagingZone> zones = listOfZonesWithTemplateName(allSpawnedZones, name);
        if (zones.size() == 0) {
            return null;
        } else if (zones.size() == 1) {
            return zones.get(0);
        } else {
            ForagingZone zone = getClosestZone(zones, originLoc);
            return zone;
        }
    }
}
