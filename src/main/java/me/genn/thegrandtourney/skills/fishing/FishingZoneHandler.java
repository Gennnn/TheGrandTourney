package me.genn.thegrandtourney.skills.fishing;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.skills.fishing.FishingZone;
import me.genn.thegrandtourney.skills.fishing.FishingZoneTemplate;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class FishingZoneHandler {

    public List<FishingZoneTemplate> allZones;



    public FishingZoneHandler() {
    }
    public void registerZones(TGT plugin, ConfigurationSection config) throws IOException {
        this.allZones = new ArrayList<>();
        Iterator var4 = config.getKeys(false).iterator();
        while(var4.hasNext()) {
            String key = (String)var4.next();
            FishingZoneTemplate zone = FishingZoneTemplate.create(config.getConfigurationSection(key));
            if (zone != null) {
                this.allZones.add(zone);
                plugin.getLogger().log(Level.INFO, "Registered fishing zone " + zone.name);
            } else {
                plugin.getLogger().severe("Fishing Zone Tempalte " + key + " was empty!");
            }
        }

    }
    public boolean containsName(final List<FishingZoneTemplate> list, final String name){
        return list.stream().map(FishingZoneTemplate::getName).filter(name::equals).findFirst().isPresent();
    }


}
