package me.genn.thegrandtourney.grid;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.skills.Station;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

public class SchematicHandler {
    public File schematicFolder;
    File linkedSchematicFolder;
    public static File schematicDirectory;
    public static File linkedSchematicDirectory;
    List<String> allSchematicsFiles;
    List<String> linkedSchematicsFiles;
    List<Schematic> farmSchematics;
    List<Schematic> aristocracySchematics;
    List<Schematic> slumsSchematics;
    List<Schematic> portSchematics;
    List<Schematic> outskirtsSchematics;
    List<Schematic> farmSchematicsRepeatable;
    List<Schematic> aristocracySchematicsRepeatable;
    List<Schematic> slumsSchematicsRepeatable;
    List<Schematic> portSchematicsRepeatable;
    List<Schematic> outskirtsSchematicsRepeatable;
    List<Schematic> farmSchematicsOmni;
    List<Schematic> aristocracySchematicsOmni;
    List<Schematic> slumsSchematicsOmni;
    List<Schematic> portSchematicsOmni;
    List<Schematic> outskirtsSchematicsOmni;
    List<Schematic> allSchematics;
    List<Schematic> noneSchematics;
    List<Schematic> linkedSchematics;
    List<String> pastedSchematics;
    public File schematicDetailsDirectory;
    public List<Station> allCraftingStations;


    public SchematicHandler(File schematicFolder, File linkedSchematicFolder, File schematicDirectory, File linkedSchematicDirectory) {
        this.linkedSchematicFolder = linkedSchematicFolder;
        this.schematicFolder = schematicFolder;
        this.schematicDirectory = schematicDirectory;
        this.linkedSchematicDirectory = linkedSchematicDirectory;
        this.allCraftingStations = new ArrayList<>();

    }

    public void generate() {
        this.allSchematicsFiles = new ArrayList<String>();
        this.linkedSchematicsFiles = new ArrayList<String>();
        this.farmSchematics = new ArrayList<Schematic>();
        this.portSchematics = new ArrayList<Schematic>();
        this.outskirtsSchematics = new ArrayList<Schematic>();
        this.aristocracySchematics = new ArrayList<Schematic>();
        this.slumsSchematics = new ArrayList<Schematic>();
        this.farmSchematicsRepeatable = new ArrayList<Schematic>();
        this.portSchematicsRepeatable = new ArrayList<Schematic>();
        this.outskirtsSchematicsRepeatable = new ArrayList<Schematic>();
        this.aristocracySchematicsRepeatable = new ArrayList<Schematic>();
        this.slumsSchematicsRepeatable = new ArrayList<Schematic>();
        this.farmSchematicsOmni = new ArrayList<Schematic>();
        this.portSchematicsOmni = new ArrayList<Schematic>();
        this.outskirtsSchematicsOmni = new ArrayList<Schematic>();
        this.aristocracySchematicsOmni = new ArrayList<Schematic>();
        this.slumsSchematicsOmni = new ArrayList<Schematic>();
        this.linkedSchematics = new ArrayList<Schematic>();
        this.allSchematics = new ArrayList<Schematic>();
        this.noneSchematics = new ArrayList<Schematic>();
        String[] contents = schematicFolder.list();
        for (int i=0; i < contents.length; i++) {
            allSchematicsFiles.add(contents[i]);
        }
        contents = linkedSchematicFolder.list();
        for (int i=0; i < contents.length; i++) {
            linkedSchematicsFiles.add(contents[i]);
        }
    }
    public void registerLinkedSchematics(TGT plugin, ConfigurationSection config) {
        Iterator var4 = config.getKeys(false).iterator();
        while(var4.hasNext()) {
            String key = (String)var4.next();
            Schematic schem = Schematic.createLinked(config.getConfigurationSection(key), this.linkedSchematicsFiles);
            if (schem != null) {
                this.linkedSchematics.add(schem);
            } else {
                plugin.getLogger().severe("Linked Schematic " + key + " was empty!");
            }
        }

    }
    public void registerSchematics(TGT plugin, ConfigurationSection config) {
        Iterator var4 = config.getKeys(false).iterator();
        while(var4.hasNext()) {
            String key = (String)var4.next();
            Schematic schem = Schematic.create(config.getConfigurationSection(key), allSchematicsFiles, this.linkedSchematics);
            if (schem != null) {
                this.allSchematics.add(schem);
                this.sortSchematic(schem);
            } else {
                plugin.getLogger().severe("Schematic " + key + " was empty!");
            }
        }
        Collections.sort(this.outskirtsSchematics);
        Collections.sort(this.farmSchematics);
        Collections.sort(this.slumsSchematics);
        Collections.sort(this.aristocracySchematics);
        Collections.sort(this.portSchematics);
        for (Schematic schem : this.farmSchematics) {
            plugin.getLogger().log(Level.INFO, schem.name + " loaded with size " + schem.xLength + "," + schem.zHeight + " R: " + schem.repeatable + " O: " + schem.omnidirectional);        }
        for (Schematic schem : this.slumsSchematics) {
            plugin.getLogger().log(Level.INFO, schem.name + " loaded with size " + schem.xLength + "," + schem.zHeight + " R: " + schem.repeatable + " O: " + schem.omnidirectional);        }
        for (Schematic schem : this.portSchematics) {
            plugin.getLogger().log(Level.INFO, schem.name + " loaded with size " + schem.xLength + "," + schem.zHeight + " R: " + schem.repeatable + " O: " + schem.omnidirectional);        }
        for (Schematic schem : this.aristocracySchematics) {
            plugin.getLogger().log(Level.INFO, schem.name + " loaded with size " + schem.xLength + "," + schem.zHeight + " R: " + schem.repeatable + " O: " + schem.omnidirectional);        }
        for (Schematic schem : this.outskirtsSchematics) {
            plugin.getLogger().log(Level.INFO, schem.name + " loaded with size " + schem.xLength + "," + schem.zHeight + " R: " + schem.repeatable + " O: " + schem.omnidirectional);
        }


    }
    public void sortSchematic(Schematic schematic) {
        if (schematic.district == District.PORT) {
            if (schematic.repeatable) {
                this.portSchematicsRepeatable.add(schematic);
                if (schematic.omnidirectional) {
                    this.portSchematicsOmni.add(schematic);
                }
            } else {
                this.portSchematics.add(schematic);
            }

        } else if (schematic.district == District.SLUMS) {
            if (schematic.repeatable) {
                this.slumsSchematicsRepeatable.add(schematic);
                if (schematic.omnidirectional) {
                    this.slumsSchematicsOmni.add(schematic);
                }
            } else {
                this.slumsSchematics.add(schematic);
            }

        } else if (schematic.district == District.ARISTOCRACY) {
            if (schematic.repeatable) {
                this.aristocracySchematicsRepeatable.add(schematic);
                if (schematic.omnidirectional) {
                    this.aristocracySchematicsOmni.add(schematic);
                }
            } else {
                this.aristocracySchematics.add(schematic);
            }

        } else if (schematic.district == District.FARM) {
            if (schematic.repeatable) {
                this.farmSchematicsRepeatable.add(schematic);
                if (schematic.omnidirectional) {
                    this.farmSchematicsOmni.add(schematic);
                }
            } else {
                this.farmSchematics.add(schematic);
            }

        } else if (schematic.district == District.OUTSKIRTS) {
            if (schematic.repeatable) {
                this.outskirtsSchematicsRepeatable.add(schematic);
                if (schematic.omnidirectional) {
                    this.outskirtsSchematicsOmni.add(schematic);
                }
            } else {
                this.outskirtsSchematics.add(schematic);
            }

        } else {
            this.noneSchematics.add(schematic);
        }
    }



}
