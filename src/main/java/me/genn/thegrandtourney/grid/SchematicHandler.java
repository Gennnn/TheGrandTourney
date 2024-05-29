package me.genn.thegrandtourney.grid;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.skills.Station;
import me.genn.thegrandtourney.util.IHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class SchematicHandler implements IHandler {
    public File schematicFolder;
    File linkedSchematicFolder;
    public static File schematicDirectory;
    List<String> allSchematicsFiles = new ArrayList<>();
    List<String> linkedSchematicsFiles = new ArrayList<>();
    List<Schematic> farmSchematics = new ArrayList<>();
    List<Schematic> aristocracySchematics = new ArrayList<>();
    List<Schematic> slumsSchematics = new ArrayList<>();
    List<Schematic> portSchematics = new ArrayList<>();
    List<Schematic> outskirtsSchematics = new ArrayList<>();
    List<Schematic> farmSchematicsRepeatable = new ArrayList<>();
    List<Schematic> aristocracySchematicsRepeatable = new ArrayList<>();
    List<Schematic> slumsSchematicsRepeatable = new ArrayList<>();
    List<Schematic> portSchematicsRepeatable = new ArrayList<>();
    List<Schematic> outskirtsSchematicsRepeatable = new ArrayList<>();
    List<Schematic> farmSchematicsOmni = new ArrayList<>();
    List<Schematic> aristocracySchematicsOmni = new ArrayList<>();
    List<Schematic> slumsSchematicsOmni = new ArrayList<>();
    List<Schematic> portSchematicsOmni = new ArrayList<>();
    List<Schematic> outskirtsSchematicsOmni = new ArrayList<>();
    List<Schematic> allSchematics = new ArrayList<>();
    List<Schematic> noneSchematics = new ArrayList<>();
    List<Schematic> linkedSchematics = new ArrayList<>();
    public List<Station> allCraftingStations = new ArrayList<>();
    List<Schematic> portRoads = new ArrayList<>();
    List<Schematic> farmRoads = new ArrayList<>();
    List<Schematic> aristocracyRoads = new ArrayList<>();
    List<Schematic> slumsRoads = new ArrayList<>();
    List<Schematic> outskirtsRoads = new ArrayList<>();

    List<Schematic> portRivers = new ArrayList<>();
    List<Schematic> farmRivers = new ArrayList<>();
    List<Schematic> aristocracyRivers = new ArrayList<>();
    List<Schematic> slumsRivers = new ArrayList<>();
    List<Schematic> outskirtsRivers = new ArrayList<>();
    TGT plugin;


    public SchematicHandler(TGT plugin) {
        this.plugin = plugin;
        this.linkedSchematicFolder = new File(plugin.getDataFolder(), "linked_schematics/");
        this.schematicFolder = new File(plugin.getDataFolder(), "schematics/");
        this.schematicDirectory = new File(plugin.getDataFolder(), "schematics");
        /*this.linkedSchematicDirectory = linkedSchematicDirectory;*/
    }

    @Override
    public void register(YamlConfiguration config) throws IOException {
        String[] contents = schematicFolder.list();
        allSchematicsFiles.addAll(Arrays.asList(contents));
        this.registerLinkedSchematics(config.getConfigurationSection("schematics.linked-schematics"));
        this.registerSchematics(config.getConfigurationSection("schematics.main-schematics"));
        this.registerRiverSchematics(config.getConfigurationSection("schematics.river-schematics"));
        this.registerRoadSchematics(config.getConfigurationSection("schematics.road-schematics"));
    }
    public void registerLinkedSchematics(ConfigurationSection config) {
        Iterator var4 = config.getKeys(false).iterator();
        while(var4.hasNext()) {
            String key = (String)var4.next();
            Schematic schem = Schematic.createLinked(config.getConfigurationSection(key), allSchematicsFiles);
            if (schem != null) {
                this.linkedSchematics.add(schem);
            } else {
                plugin.getLogger().severe("Linked Schematic " + key + " was empty!");
            }
        }

    }
    public void registerRoadSchematics(ConfigurationSection config) {
        Iterator var4 = config.getKeys(false).iterator();
        while(var4.hasNext()) {
            String key = (String)var4.next();
            Schematic schem = Schematic.createRoad(config.getConfigurationSection(key), allSchematicsFiles);
            if (schem != null) {
                this.sortSchematic(schem);
            } else {
                plugin.getLogger().severe("Road Schematic " + key + " was empty!");
            }
        }

    }
    public void registerRiverSchematics(ConfigurationSection config) {
        Iterator var4 = config.getKeys(false).iterator();
        while(var4.hasNext()) {
            String key = (String)var4.next();
            Schematic schem = Schematic.createRiver(config.getConfigurationSection(key), allSchematicsFiles);
            if (schem != null) {
                this.sortSchematic(schem);
            } else {
                plugin.getLogger().severe("River Schematic " + key + " was empty!");
            }
        }

    }
    public void registerSchematics(ConfigurationSection config) {
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
            if (schematic.isRiver) {
                this.portRivers.add(schematic);
                return;
            }
            if (schematic.roadType != RoadType.NOT && schematic.roadTier != RoadTier.NOT) {
                this.portRoads.add(schematic);
                return;
            }
            if (schematic.repeatable) {
                this.portSchematicsRepeatable.add(schematic);
                if (schematic.omnidirectional) {
                    this.portSchematicsOmni.add(schematic);
                }
            } else {
                this.portSchematics.add(schematic);
            }

        } else if (schematic.district == District.SLUMS) {
            if (schematic.isRiver) {
                this.slumsRivers.add(schematic);
                return;
            }
            if (schematic.roadType != RoadType.NOT && schematic.roadTier != RoadTier.NOT) {
                this.slumsRoads.add(schematic);
                return;
            }
            if (schematic.repeatable) {
                this.slumsSchematicsRepeatable.add(schematic);
                if (schematic.omnidirectional) {
                    this.slumsSchematicsOmni.add(schematic);
                }
            } else {
                this.slumsSchematics.add(schematic);
            }

        } else if (schematic.district == District.ARISTOCRACY) {
            if (schematic.isRiver) {
                this.aristocracyRivers.add(schematic);
                return;
            }
            if (schematic.roadType != RoadType.NOT && schematic.roadTier != RoadTier.NOT) {
                this.aristocracyRoads.add(schematic);
                return;
            }
            if (schematic.repeatable) {
                this.aristocracySchematicsRepeatable.add(schematic);
                if (schematic.omnidirectional) {
                    this.aristocracySchematicsOmni.add(schematic);
                }
            } else {
                this.aristocracySchematics.add(schematic);
            }

        } else if (schematic.district == District.FARM) {
            if (schematic.isRiver) {
                this.farmRivers.add(schematic);
                return;
            }
            if (schematic.roadType != RoadType.NOT && schematic.roadTier != RoadTier.NOT) {
                this.farmRoads.add(schematic);
                return;
            }
            if (schematic.repeatable) {
                this.farmSchematicsRepeatable.add(schematic);
                if (schematic.omnidirectional) {
                    this.farmSchematicsOmni.add(schematic);
                }
            } else {
                this.farmSchematics.add(schematic);
            }

        } else if (schematic.district == District.OUTSKIRTS) {
            if (schematic.isRiver) {
                this.outskirtsRivers.add(schematic);
                return;
            }
            if (schematic.roadType != RoadType.NOT && schematic.roadTier != RoadTier.NOT) {
                this.outskirtsRoads.add(schematic);
                return;
            }
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



    public List<Schematic> getRoadsWithQualities(District district, RoadTier tier, RoadType type) {
        List<Schematic> roads =  new ArrayList<>();
        if (district == District.ARISTOCRACY) {
            roads.addAll(aristocracyRoads.stream().filter(o -> (o.roadTier == tier && o.roadType == type)).toList());
        } else if (district == District.OUTSKIRTS) {
            roads.addAll(outskirtsRoads.stream().filter(o -> (o.roadTier == tier && o.roadType == type)).toList());
        } else if (district == District.PORT) {
            roads.addAll(portRoads.stream().filter(o -> (o.roadTier == tier && o.roadType == type)).toList());
        } else if (district == District.FARM) {
            roads.addAll(farmRoads.stream().filter(o -> (o.roadTier == tier && o.roadType == type)).toList());
        } else if (district == District.SLUMS) {
            roads.addAll(slumsRoads.stream().filter(o -> (o.roadTier == tier && o.roadType == type)).toList());
        }
        return roads;
    }

    public List<Schematic> getRiversWithQualities(District district, RoadType type) {
        List<Schematic> rivers =  new ArrayList<>();
        if (district == District.ARISTOCRACY) {
            rivers.addAll(aristocracyRivers.stream().filter(o -> (o.roadType == type)).toList());
        } else if (district == District.OUTSKIRTS) {
            rivers.addAll(outskirtsRivers.stream().filter(o -> (o.roadType == type)).toList());
        } else if (district == District.PORT) {
            rivers.addAll(portRivers.stream().filter(o -> (o.roadType == type)).toList());
        } else if (district == District.FARM) {
            rivers.addAll(farmRivers.stream().filter(o -> (o.roadType == type)).toList());
        } else if (district == District.SLUMS) {
            rivers.addAll(slumsRivers.stream().filter(o -> (o.roadType == type)).toList());
        }
        return rivers;
    }



}
