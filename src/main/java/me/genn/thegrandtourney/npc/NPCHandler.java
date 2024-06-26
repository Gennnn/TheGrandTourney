package me.genn.thegrandtourney.npc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import io.lumine.mythic.core.skills.stats.types.ParryChanceStat;
import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.util.IHandler;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class NPCHandler implements IHandler {
    public static File skinAndSigFolder;
    public static File skinAndSigDirectory;
    public List<TGTNpc> allNpcs = new ArrayList<>();
    public List<TGTNpc> allSpawnedNpcs = new ArrayList<>();
    TGT plugin;

    public NPCHandler(TGT plugin) {
        this.plugin = plugin;
        this.skinAndSigFolder = new File(plugin.getDataFolder(), "npc-skins/");
        this.skinAndSigDirectory = new File(plugin.getDataFolder(), "npc-skins");
    }

    @Override
    public void register(YamlConfiguration configuration) throws IOException {
        this.registerNPCs(configuration.getConfigurationSection("npcs"));
        this.registerSubNPCs();
    }

    public void registerNPCs(ConfigurationSection config) throws IOException {
        Iterator var4 = config.getKeys(false).iterator();
        while(var4.hasNext()) {
            String key = (String)var4.next();
            TGTNpc npc = TGTNpc.create(config.getConfigurationSection(key));
            if (npc != null) {
                this.allNpcs.add(npc);
            } else {
                plugin.getLogger().severe("NPC " + key + " was empty!");
            }
        }

    }

    public void registerSubNPCs() throws IOException {
        for (TGTNpc npc : allNpcs ) {
            if (npc != null) {
            TGTNpc.createStep2(npc);
            if (!npc.childNpcs.isEmpty() && npc.childNpcs.size() > 0) {
                for (String child : npc.childNpcs) {
                    allNpcs.stream().filter(o -> o.getName().equals(child)).forEach(
                            o -> {
                                o.parentNpc = npc;
                                npc.childNpcList.add(o);
                            }
                    );
                }

            }
            } else {
                plugin.getLogger().severe("NPC " + npc.getName() + " was empty!");
            }
        }
    }

    public boolean containsName(final List<TGTNpc> list, final String name){
        return list.stream().map(TGTNpc::getName).filter(name::equals).findFirst().isPresent();
    }

    private List<TGTNpc> listOfNpcsWithName(final List<TGTNpc> list, final String name){
        return list.stream().filter(o -> o.getName().equals(name)).toList();
    }

    public TGTNpc getNpcWithName(final String name){
        return this.allSpawnedNpcs.stream().filter(o -> o.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    private TGTNpc getClosestNpc(List<TGTNpc> npcs, Location originLoc) {
        TGTNpc npc = npcs.get(0);
        Location minLoc = npc.pasteLocation;
        for (int i = 1; i<npcs.size(); i++) {
            if (npcs.get(i).pasteLocation.distanceSquared(originLoc) < minLoc.distanceSquared(originLoc)) {
                minLoc = npcs.get(i).pasteLocation;
                npc = npcs.get(i);
            }
        }
        return npc;
    }

    public TGTNpc getNpcForObj(String name, Location originLoc) {
        List<TGTNpc> npcs = listOfNpcsWithName(allSpawnedNpcs, name);
        if (npcs.size() == 0) {
            return null;
        } else if (npcs.size() == 1) {
            return npcs.get(0);
        } else {
            TGTNpc npc = getClosestNpc(npcs, originLoc);
            return npc;
        }
    }



}
