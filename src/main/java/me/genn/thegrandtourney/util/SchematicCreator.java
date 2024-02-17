package me.genn.thegrandtourney.util;

import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.internal.annotation.Selection;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.session.SessionManager;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.world.World;
import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.skills.fishing.FishingZone;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;

public class SchematicCreator {
    TGT plugin;
    WorldEditPlugin we;
    File schematicDirectory;
    File schematicDetailsDirectory;
    public SchematicCreator(TGT plugin) {
        this.plugin = plugin;
        this.we = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        this.schematicDirectory = plugin.schematicHandler.schematicFolder;
        this.schematicDetailsDirectory = new File(plugin.getDataFolder(), "schematic-contents");
    }
    public void writeSchematic(org.bukkit.entity.Player p, String schematicName) throws FileNotFoundException, EmptyClipboardException {
        SessionManager manager = WorldEdit.getInstance().getSessionManager();
        Player actor = BukkitAdapter.adapt(p);
        LocalSession session = manager.get(actor);
        Region region;
        World selectionWorld = session.getSelectionWorld();
        try {
            if (selectionWorld == null) throw new IncompleteRegionException();
            region = session.getSelection(selectionWorld);
        } catch (IncompleteRegionException e) {
            actor.printError(TextComponent.of("Please make a region selection first."));
            return;
        }
        ClipboardHolder clipboard;
        try {
            clipboard = session.getClipboard();
            Clipboard cc = clipboard.getClipboard();

            try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(new File(plugin.schematicHandler.schematicDirectory, schematicName + ".schematic")))) {
                writer.write(cc);
                p.sendMessage("Saved schematic " + schematicName + " with the following objects:");
                int objectCounter = 1;
                File configFile = new File(this.schematicDetailsDirectory, schematicName + ".yml");
                if (!configFile.exists()) {
                    configFile.createNewFile();
                }
                FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
                for (int x = region.getMinimumPoint().getX(); x <= region.getMaximumPoint().getX(); x++) {
                    for (int y = region.getMinimumPoint().getY(); y <= region.getMaximumPoint().getY(); y++) {
                        for (int z = region.getMinimumPoint().getZ(); z <= region.getMaximumPoint().getZ(); z++) {
                            Location loc = new Location(p.getWorld(), x, y, z);
                            loc = loc.toCenterLocation();
                            Iterator iter = plugin.oreLocationList.keySet().iterator();
                            while (iter.hasNext()) {
                                Location targetLoc = (Location) iter.next();
                                double targetX = targetLoc.getX();
                                double targetY = targetLoc.getY();
                                double targetZ = targetLoc.getZ();
                                if (loc.getX() == targetX && loc.getY() == targetY && loc.getZ() == targetZ) {
                                    config.set(schematicName + ".ores." + objectCounter + ".x", x - clipboard.getClipboard().getOrigin().getX());
                                    config.set(schematicName + ".ores." + objectCounter + ".y", y - clipboard.getClipboard().getOrigin().getY());
                                    config.set(schematicName + ".ores." + objectCounter + ".z", z - clipboard.getClipboard().getOrigin().getZ());
                                    config.set(schematicName + ".ores." + objectCounter + ".template-name", (plugin.oreLocationList.get(targetLoc)).getName());
                                    p.sendMessage(plugin.oreLocationList.get(targetLoc).getName() + " at " + (x - clipboard.getClipboard().getOrigin().getX()) + "," + (y - clipboard.getClipboard().getOrigin().getY()) + "," + (z - clipboard.getClipboard().getOrigin().getZ()) );
                                    objectCounter++;
                                }
                            }
                            iter = plugin.npcLocationList.keySet().iterator();
                            while (iter.hasNext()) {
                                Location targetLoc = (Location) iter.next();
                                double targetX = targetLoc.getX();
                                double targetY = targetLoc.getY();
                                double targetZ = targetLoc.getZ();
                                if (loc.getX() == targetX && loc.getY() == targetY && loc.getZ() == targetZ) {
                                    config.set(schematicName + ".npcs." + objectCounter + ".x", x - clipboard.getClipboard().getOrigin().getX());
                                    config.set(schematicName + ".npcs." + objectCounter + ".y", y - clipboard.getClipboard().getOrigin().getY());
                                    config.set(schematicName + ".npcs." + objectCounter + ".z", z - clipboard.getClipboard().getOrigin().getZ());
                                    config.set(schematicName + ".npcs." + objectCounter + ".npc-name", plugin.npcLocationList.get(targetLoc).internalName);
                                    p.sendMessage(plugin.npcLocationList.get(targetLoc).getName() + " at " + (x - clipboard.getClipboard().getOrigin().getX()) + "," + (y - clipboard.getClipboard().getOrigin().getY()) + "," + (z - clipboard.getClipboard().getOrigin().getZ()) );
                                    objectCounter++;
                                }
                            }
                            iter = plugin.spawnerLocationList.keySet().iterator();
                            while (iter.hasNext()) {
                                Location targetLoc = (Location) iter.next();
                                double targetX = targetLoc.getX();
                                double targetY = targetLoc.getY();
                                double targetZ = targetLoc.getZ();
                                if (loc.getX() == targetX && loc.getY() == targetY && loc.getZ() == targetZ) {
                                    config.set(schematicName + ".spawners." + objectCounter + ".x", x - clipboard.getClipboard().getOrigin().getX());
                                    config.set(schematicName + ".spawners." + objectCounter + ".y", y - clipboard.getClipboard().getOrigin().getY());
                                    config.set(schematicName + ".spawners." + objectCounter + ".z", z - clipboard.getClipboard().getOrigin().getZ());
                                    config.set(schematicName + ".spawners." + objectCounter + ".spawner-name", plugin.spawnerLocationList.get(targetLoc).getName());
                                    p.sendMessage(plugin.spawnerLocationList.get(targetLoc).getName() + " at " + (x - clipboard.getClipboard().getOrigin().getX()) + "," + (y - clipboard.getClipboard().getOrigin().getY()) + "," + (z - clipboard.getClipboard().getOrigin().getZ()) );
                                    objectCounter++;
                                }
                            }
                            iter = plugin.cropLocationList.keySet().iterator();
                            while (iter.hasNext()) {
                                Location targetLoc = (Location) iter.next();
                                double targetX = targetLoc.getX();
                                double targetY = targetLoc.getY();
                                double targetZ = targetLoc.getZ();
                                if (loc.getX() == targetX && loc.getY() == targetY && loc.getZ() == targetZ) {
                                    config.set(schematicName + ".crops." + objectCounter + ".x", x - clipboard.getClipboard().getOrigin().getX());
                                    config.set(schematicName + ".crops." + objectCounter + ".y", y - clipboard.getClipboard().getOrigin().getY());
                                    config.set(schematicName + ".crops." + objectCounter + ".z", z - clipboard.getClipboard().getOrigin().getZ());
                                    config.set(schematicName + ".crops." + objectCounter + ".template-name", plugin.cropLocationList.get(targetLoc).getName());
                                    p.sendMessage(plugin.cropLocationList.get(targetLoc).getName() + " at " + (x - clipboard.getClipboard().getOrigin().getX()) + "," + (y - clipboard.getClipboard().getOrigin().getY()) + "," + (z - clipboard.getClipboard().getOrigin().getZ()) );
                                    objectCounter++;
                                }
                            }


                        }
                    }
                }
                Iterator iter = plugin.fishingZoneList.iterator();
                Location regionMin = new Location(p.getWorld(), region.getMinimumPoint().getX(), region.getMinimumPoint().getY(), region.getMinimumPoint().getZ()).toCenterLocation();
                Location regionMax = new Location(p.getWorld(), region.getMaximumPoint().getX(), region.getMaximumPoint().getY(), region.getMaximumPoint().getZ()).toCenterLocation();
                while (iter.hasNext()) {
                    FishingZone zone = (FishingZone) iter.next();
                    if (zone.minLoc.getX() >= regionMin.getX() &&
                    zone.minLoc.getY() >= regionMin.getY() &&
                    zone.minLoc.getZ() >= regionMin.getZ() &&
                    zone.minLoc.getX() <= regionMax.getX() &&
                    zone.minLoc.getY() <= regionMax.getY() &&
                    zone.minLoc.getZ() <= regionMax.getZ() &&
                    zone.maxLoc.getX() >= regionMin.getX() &&
                    zone.maxLoc.getY() >= regionMin.getY() &&
                    zone.maxLoc.getZ() >= regionMin.getZ() &&
                    zone.maxLoc.getX() <= regionMax.getX() &&
                    zone.maxLoc.getY() <= regionMax.getY() &&
                    zone.maxLoc.getZ() <= regionMax.getZ()) {
                        config.set(schematicName + ".fishing-zones." + objectCounter + ".min-x", zone.minLoc.getX() - clipboard.getClipboard().getOrigin().getX());
                        config.set(schematicName + ".fishing-zones." + objectCounter + ".min-y", zone.minLoc.getY() - clipboard.getClipboard().getOrigin().getY());
                        config.set(schematicName + ".fishing-zones." + objectCounter + ".min-z", zone.minLoc.getZ() - clipboard.getClipboard().getOrigin().getZ());
                        config.set(schematicName + ".fishing-zones." + objectCounter + ".max-x", zone.maxLoc.getX() - clipboard.getClipboard().getOrigin().getX());
                        config.set(schematicName + ".fishing-zones." + objectCounter + ".max-y", zone.maxLoc.getY() - clipboard.getClipboard().getOrigin().getY());
                        config.set(schematicName + ".fishing-zones." + objectCounter + ".max-z", zone.maxLoc.getZ() - clipboard.getClipboard().getOrigin().getZ());
                        config.set(schematicName + ".fishing-zones." + objectCounter + ".template-name", zone.template.getName());
                        objectCounter++;
                        p.sendMessage(zone.getName() + " with bounds min " + (zone.minLoc.getX() - clipboard.getClipboard().getOrigin().getX()) + "," + (zone.minLoc.getY() - clipboard.getClipboard().getOrigin().getY()) + "," + (zone.minLoc.getZ() - clipboard.getClipboard().getOrigin().getZ()) + " and max " + (zone.maxLoc.getX() - clipboard.getClipboard().getOrigin().getX()) + "," + (zone.maxLoc.getY() - clipboard.getClipboard().getOrigin().getY()) + "," + (zone.maxLoc.getZ() - clipboard.getClipboard().getOrigin().getZ()));
                    }

                }
                config.save(configFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (EmptyClipboardException e) {
            actor.printError(TextComponent.of("Your clipboard is empty!"));
        }


    }
}
