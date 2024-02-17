package me.genn.thegrandtourney.skills.mining;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import me.genn.thegrandtourney.TGT;
import org.bukkit.configuration.ConfigurationSection;

public class OreHandler {

    public List<OreTemplate> allOres;



    public OreHandler() {
    }
    public void registerOreTemplates(TGT plugin, ConfigurationSection config) throws IOException {
        this.allOres = new ArrayList<>();
        Iterator var4 = config.getKeys(false).iterator();
        while(var4.hasNext()) {
            String key = (String)var4.next();
            OreTemplate ore = OreTemplate.create(config.getConfigurationSection(key));
            if (ore != null) {
                this.allOres.add(ore);
            } else {
                plugin.getLogger().severe("Ore Template " + key + " was empty!");
            }
        }

    }
    public boolean containsName(final List<OreTemplate> list, final String name){
        return list.stream().map(OreTemplate::getName).filter(name::equals).findFirst().isPresent();
    }
}
