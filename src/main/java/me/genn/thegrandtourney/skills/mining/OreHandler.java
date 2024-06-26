package me.genn.thegrandtourney.skills.mining;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.npc.TGTNpc;
import me.genn.thegrandtourney.util.IHandler;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class OreHandler implements IHandler {

    public List<OreTemplate> allOres = new ArrayList<>();

    public List<Ore> allSpawnedOres = new ArrayList<>();
    TGT plugin;



    public OreHandler(TGT plugin) {
        this.plugin = plugin;
        this.allOres = new ArrayList<>();
        this.allSpawnedOres = new ArrayList<>();
    }
    public void registerOreTemplates(ConfigurationSection config) throws IOException {

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

    private List<Ore> listOfOresWithTemplateName(final List<Ore> list, final String name){
        return list.stream().filter(o -> o.getName().equals(name)).toList();
    }

    private Ore getClosestOre(List<Ore> ores, Location originLoc) {
        Ore ore = ores.get(0);
        Location minLoc = ore.loc;
        for (int i = 1; i<ores.size(); i++) {
            if (ores.get(i).loc.distanceSquared(originLoc) < minLoc.distanceSquared(originLoc)) {
                minLoc = ores.get(i).loc;
                ore = ores.get(i);
            }
        }
        return ore;
    }

    public Ore getOreForObj(String name, Location originLoc) {
        List<Ore> ores = listOfOresWithTemplateName(allSpawnedOres, name);
        if (ores.size() == 0) {
            return null;
        } else if (ores.size() == 1) {
            return ores.get(0);
        } else {
            Ore ore = getClosestOre(ores, originLoc);
            return ore;
        }
    }

    @Override
    public void register(YamlConfiguration configuration) throws IOException {
        this.registerOreTemplates(configuration.getConfigurationSection("ores"));
    }
}
