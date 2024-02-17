package me.genn.thegrandtourney.npc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import me.genn.thegrandtourney.TGT;
import org.bukkit.configuration.ConfigurationSection;

public class NPCHandler {
    public static File skinAndSigFolder;
    public static File skinAndSigDirectory;
    public List<String> skinsAndSigs;
    public List<TGTNpc> allNpcs;
    public List<TGTNpc> subNpcs;



    public NPCHandler(File skinAndSigFolder, File skinAndSigDirectory) {
        this.skinAndSigFolder = skinAndSigFolder;
        this.skinAndSigDirectory = skinAndSigDirectory;
    }

    public void generate() {
        this.allNpcs = new ArrayList<TGTNpc>();
        this.skinsAndSigs = new ArrayList<String>();
        String contents[] = skinAndSigFolder.list();
        for (int i=0; i < contents.length; i++) {
            skinsAndSigs.add(contents[i]);
        }
    }

    public void registerNPCs(TGT plugin, ConfigurationSection config) throws IOException {
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

    public void registerSubNPCs(TGT plugin) throws IOException {
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




}
