package me.genn.thegrandtourney.grid;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import me.genn.thegrandtourney.mobs.Spawner;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;


public class Schematic implements Comparable<Schematic> {
    public String fileName;
    public String name;
    public int xLength;
    public int zHeight;
    public District district;
    public String linkedName;
    public int area;
    public Schematic linkedSchematic;
    public boolean repeatable;
    public boolean omnidirectional;
    public List<String> npcNames;
    public List<Spawner> spawners;

    public Schematic() {

    }

    public static Schematic create(ConfigurationSection config, List<String> schematicList, List<Schematic> linkedSchematicList) {
        Schematic schem = new Schematic();

        schem.fileName = config.getString("schematic-name", (String)null);
        if (schem.fileName == null) {
            return null;
        }

        if (!schematicList.contains(schem.fileName + ".schematic") && !schematicList.contains(schem.fileName)) {
            return null;
        }

        schem.linkedName = config.getString("linked-schematic", null);
        if (schem.linkedName != null && !containsName(linkedSchematicList, schem.linkedName)) {
            return null;
        }
        schem.linkedSchematic = getSchematicWithName(schem.linkedName, linkedSchematicList);
        if (schem.linkedName != null && schem.linkedSchematic == null) {
            return null;
        }
        schem.name = config.getString("name", schem.fileName);
        schem.xLength = config.getInt("x-size", 1);
        schem.zHeight = config.getInt("z-size", 1);
        String districtToParse = config.getString("district", "none");
        if (districtToParse.equalsIgnoreCase("a") || districtToParse.equalsIgnoreCase("aristocracy")) {
            schem.district = District.ARISTOCRACY;
        } else if (districtToParse.equalsIgnoreCase("f") || districtToParse.equalsIgnoreCase("farm")) {
            schem.district = District.FARM;
        } else if (districtToParse.equalsIgnoreCase("p") || districtToParse.equalsIgnoreCase("port")) {
            schem.district = District.PORT;
        } else if (districtToParse.equalsIgnoreCase("s") || districtToParse.equalsIgnoreCase("slums")) {
            schem.district = District.SLUMS;
        } else if (districtToParse.equalsIgnoreCase("o") || districtToParse.equalsIgnoreCase("outskirts")) {
            schem.district = District.OUTSKIRTS;
        } else {
            schem.district = District.NONE;
        }
        schem.area = schem.xLength * schem.zHeight;
        schem.repeatable = config.getBoolean("repeatable", false);
        schem.omnidirectional = config.getBoolean("omnidirectional", false);
        schem.npcNames = config.getStringList("npcs");


        return schem;
    }

    public static Schematic createLinked(ConfigurationSection config, List<String> schematicList) {
        Schematic schem = new Schematic();
        schem.fileName = config.getString("schematic-name", (String)null);
        if (schem.fileName == null) {
            return null;

        }

        if (!schematicList.contains(schem.fileName + ".schematic") && !schematicList.contains(schem.fileName)) {

            for (String schemname : schematicList) {
                System.out.print(schemname);
            }
            return null;

        }
        schem.name = config.getString("name", schem.fileName);
        schem.xLength = config.getInt("x-size", 1);
        schem.zHeight = config.getInt("z-size", 1);
        String districtToParse = config.getString("district", "none");
        if (districtToParse.equalsIgnoreCase("a") || districtToParse.equalsIgnoreCase("aristocracy")) {
            schem.district = District.ARISTOCRACY;
        } else if (districtToParse.equalsIgnoreCase("f") || districtToParse.equalsIgnoreCase("farm")) {
            schem.district = District.FARM;
        } else if (districtToParse.equalsIgnoreCase("p") || districtToParse.equalsIgnoreCase("port")) {
            schem.district = District.PORT;
        } else if (districtToParse.equalsIgnoreCase("s") || districtToParse.equalsIgnoreCase("slums")) {
            schem.district = District.SLUMS;
        } else if (districtToParse.equalsIgnoreCase("o") || districtToParse.equalsIgnoreCase("outskirts")) {
            schem.district = District.OUTSKIRTS;
        } else {
            schem.district = District.NONE;
        }
        schem.area = schem.xLength * schem.zHeight;
        return schem;
    }

    public static String getName(Schematic schematic) {
        return schematic.name;
    }



    public static boolean containsName(final List<Schematic> list, final String name){
        return list.stream().map(Schematic::getName).filter(name::equals).findFirst().isPresent();
    }

    public static Schematic getSchematicWithName(String name, List<Schematic> schemList) {
        for (Schematic schem : schemList) {
            if (getName(schem).equalsIgnoreCase(name)) {
                return schem;
            }
        }
        return null;
    }

    @Override
    public int compareTo(Schematic compareSchem) {
        int compareArea = compareSchem.area;
        return compareArea - this.area;
    }
}
