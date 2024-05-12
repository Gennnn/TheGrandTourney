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
import me.genn.thegrandtourney.dungeons.Dungeon;
import me.genn.thegrandtourney.dungeons.Room;
import me.genn.thegrandtourney.dungeons.RoomGoal;
import me.genn.thegrandtourney.skills.Station;
import me.genn.thegrandtourney.skills.fishing.FishingZone;
import me.genn.thegrandtourney.skills.foraging.ForagingZone;
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
                            iter = plugin.stationList.keySet().iterator();
                            while (iter.hasNext()) {
                                Location targetLoc = (Location) iter.next();
                                double targetX = targetLoc.getX();
                                double targetY = targetLoc.getY();
                                double targetZ = targetLoc.getZ();

                                if (loc.getX() == targetX && loc.getY() == targetY && loc.getZ() == targetZ) {
                                    Station station = plugin.stationList.get(targetLoc);
                                    config.set(schematicName + ".stations." + objectCounter + ".min-x", station.minLoc.getX() - clipboard.getClipboard().getOrigin().getX());
                                    config.set(schematicName + ".stations." + objectCounter + ".min-y", station.minLoc.getY() - clipboard.getClipboard().getOrigin().getY());
                                    config.set(schematicName + ".stations." + objectCounter + ".min-z", station.minLoc.getZ() - clipboard.getClipboard().getOrigin().getZ());
                                    config.set(schematicName + ".stations." + objectCounter + ".max-x", station.maxLoc.getX() - clipboard.getClipboard().getOrigin().getX());
                                    config.set(schematicName + ".stations." + objectCounter + ".max-y", station.maxLoc.getY() - clipboard.getClipboard().getOrigin().getY());
                                    config.set(schematicName + ".stations." + objectCounter + ".max-z", station.maxLoc.getZ() - clipboard.getClipboard().getOrigin().getZ());
                                    config.set(schematicName + ".stations." + objectCounter + ".type", station.type.toString());
                                    config.set(schematicName + ".stations." + objectCounter + ".mashing-table.x", station.mashingTable.loc.getX() - clipboard.getClipboard().getOrigin().getX());
                                    config.set(schematicName + ".stations." + objectCounter + ".mashing-table.y", station.mashingTable.loc.getY() - clipboard.getClipboard().getOrigin().getY());
                                    config.set(schematicName + ".stations." + objectCounter + ".mashing-table.z", station.mashingTable.loc.getZ() - clipboard.getClipboard().getOrigin().getZ());
                                    config.set(schematicName + ".stations." + objectCounter + ".mashing-table.dir", station.mashingTable.dir.toString());
                                    config.set(schematicName + ".stations." + objectCounter + ".holding-table.x", station.holdingTable.loc.getX() - clipboard.getClipboard().getOrigin().getX());
                                    config.set(schematicName + ".stations." + objectCounter + ".holding-table.y", station.holdingTable.loc.getY() - clipboard.getClipboard().getOrigin().getY());
                                    config.set(schematicName + ".stations." + objectCounter + ".holding-table.z", station.holdingTable.loc.getZ() - clipboard.getClipboard().getOrigin().getZ());
                                    config.set(schematicName + ".stations." + objectCounter + ".holding-table.dir", station.holdingTable.dir.toString());
                                    config.set(schematicName + ".stations." + objectCounter + ".timing-table.x", station.timingTable.loc.getX() - clipboard.getClipboard().getOrigin().getX());
                                    config.set(schematicName + ".stations." + objectCounter + ".timing-table.y", station.timingTable.loc.getY() - clipboard.getClipboard().getOrigin().getY());
                                    config.set(schematicName + ".stations." + objectCounter + ".timing-table.z", station.timingTable.loc.getZ() - clipboard.getClipboard().getOrigin().getZ());
                                    config.set(schematicName + ".stations." + objectCounter + ".timing-table.dir", station.timingTable.dir.toString());
                                    config.set(schematicName + ".stations." + objectCounter + ".spawn-x", station.spawnLocation.getX() - clipboard.getClipboard().getOrigin().getX());
                                    config.set(schematicName + ".stations." + objectCounter + ".spawn-y", station.spawnLocation.getY() - clipboard.getClipboard().getOrigin().getY());
                                    config.set(schematicName + ".stations." + objectCounter + ".spawn-z", station.spawnLocation.getZ() - clipboard.getClipboard().getOrigin().getZ());
                                    config.set(schematicName + ".stations." + objectCounter + ".spawn-yaw", station.spawnLocation.getYaw());
                                    p.sendMessage(station.type + " with spawn location " + station.spawnLocation.getX() + "," + station.spawnLocation.getY() + "," + station.spawnLocation.getZ() + " with yaw " + station.spawnLocation.getYaw());
                                    objectCounter++;
                                }
                            }
                            iter = plugin.foragingZoneLocList.keySet().iterator();
                            while (iter.hasNext()) {
                                Location originalLoc = (Location) iter.next();
                                Location targetLoc = originalLoc.clone().toCenterLocation();
                                double targetX = targetLoc.getX();
                                double targetY = targetLoc.getY();
                                double targetZ = targetLoc.getZ();
                                if (loc.getX() == targetX && loc.getY() == targetY && loc.getZ() == targetZ) {
                                    ForagingZone zone = plugin.foragingZoneLocList.get(originalLoc);
                                    config.set(schematicName + ".foraging-zones." + objectCounter + ".min-x", zone.minLoc.getX() - clipboard.getClipboard().getOrigin().getX());
                                    config.set(schematicName + ".foraging-zones." + objectCounter + ".min-y", zone.minLoc.getY() - clipboard.getClipboard().getOrigin().getY());
                                    config.set(schematicName + ".foraging-zones." + objectCounter + ".min-z", zone.minLoc.getZ() - clipboard.getClipboard().getOrigin().getZ());
                                    config.set(schematicName + ".foraging-zones." + objectCounter + ".max-x", zone.maxLoc.getX() - clipboard.getClipboard().getOrigin().getX());
                                    config.set(schematicName + ".foraging-zones." + objectCounter + ".max-y", zone.maxLoc.getY() - clipboard.getClipboard().getOrigin().getY());
                                    config.set(schematicName + ".foraging-zones." + objectCounter + ".max-z", zone.maxLoc.getZ() - clipboard.getClipboard().getOrigin().getZ());
                                    config.set(schematicName + ".template-name." + objectCounter + ".template-name", zone.template.getName());
                                    p.sendMessage(plugin.cropLocationList.get(targetLoc).getName() + " at " + (x - clipboard.getClipboard().getOrigin().getX()) + "," + (y - clipboard.getClipboard().getOrigin().getY()) + "," + (z - clipboard.getClipboard().getOrigin().getZ()) );
                                    objectCounter++;
                                }
                            }
                            iter = plugin.dungeonLocList.keySet().iterator();
                            while (iter.hasNext()) {
                                Location targetLoc = (Location) iter.next();
                                double targetX = targetLoc.getX();
                                double targetY = targetLoc.getY();
                                double targetZ = targetLoc.getZ();
                                if (loc.getX() == targetX && loc.getY() == targetY && loc.getZ() == targetZ) {
                                    Dungeon dungeon = plugin.dungeonLocList.get(targetLoc);
                                    config.set(schematicName + ".dungeons." + objectCounter + ".min-x", dungeon.minLoc.getX() - clipboard.getClipboard().getOrigin().getX());
                                    config.set(schematicName + ".dungeons." + objectCounter + ".min-y", dungeon.minLoc.getY() - clipboard.getClipboard().getOrigin().getY());
                                    config.set(schematicName + ".dungeons." + objectCounter + ".min-z", dungeon.minLoc.getZ() - clipboard.getClipboard().getOrigin().getZ());
                                    config.set(schematicName + ".dungeons." + objectCounter + ".max-x", dungeon.maxLoc.getX() - clipboard.getClipboard().getOrigin().getX());
                                    config.set(schematicName + ".dungeons." + objectCounter + ".max-y", dungeon.maxLoc.getY() - clipboard.getClipboard().getOrigin().getY());
                                    config.set(schematicName + ".dungeons." + objectCounter + ".max-z", dungeon.maxLoc.getZ() - clipboard.getClipboard().getOrigin().getZ());
                                    config.set(schematicName + ".dungeons." + objectCounter + ".template-name", dungeon.template.getName());
                                    if (dungeon.entranceDoor != null) {
                                        config.set(schematicName + ".dungeons." + objectCounter + ".entrance-door-min-loc", (dungeon.entranceDoor.minLoc.getX() - clipboard.getClipboard().getOrigin().getX()) + "," + (dungeon.entranceDoor.minLoc.getY() - clipboard.getClipboard().getOrigin().getY()) + "," + (dungeon.entranceDoor.minLoc.getZ() - clipboard.getClipboard().getOrigin().getZ()));
                                        config.set(schematicName + ".dungeons." + objectCounter + ".entrance-door-max-loc", (dungeon.entranceDoor.maxLoc.getX() - clipboard.getClipboard().getOrigin().getX()) + "," + (dungeon.entranceDoor.maxLoc.getY() - clipboard.getClipboard().getOrigin().getY()) + "," + (dungeon.entranceDoor.maxLoc.getZ() - clipboard.getClipboard().getOrigin().getZ()));
                                        config.set(schematicName + ".dungeons." + objectCounter + ".entrance-door-armorstand-loc", (dungeon.entranceDoor.asLocation.getX() - clipboard.getClipboard().getOrigin().getX()) + "," + (dungeon.entranceDoor.asLocation.getY() - clipboard.getClipboard().getOrigin().getY()) + "," + (dungeon.entranceDoor.asLocation.getZ() - clipboard.getClipboard().getOrigin().getZ()) + "," + (dungeon.entranceDoor.asLocation.getYaw()));
                                    }
                                    if (dungeon.exitWarpTarget != null && dungeon.exitWarpLocation != null) {
                                        config.set(schematicName + ".dungeons." + objectCounter + ".exit-warp-entrance", (dungeon.exitWarpLocation.getX() - clipboard.getClipboard().getOrigin().getX()) + "," + (dungeon.exitWarpLocation.getY() - clipboard.getClipboard().getOrigin().getY()) + "," + (dungeon.exitWarpLocation.getZ() - clipboard.getClipboard().getOrigin().getZ()) + "," + (dungeon.exitWarpLocation.getYaw()));
                                        config.set(schematicName + ".dungeons." + objectCounter + ".exit-warp-target", (dungeon.exitWarpTarget.getX() - clipboard.getClipboard().getOrigin().getX()) + "," + (dungeon.exitWarpTarget.getY() - clipboard.getClipboard().getOrigin().getY()) + "," + (dungeon.exitWarpTarget.getZ() - clipboard.getClipboard().getOrigin().getZ()) + "," + (dungeon.exitWarpTarget.getYaw()));
                                    }
                                    for (Room room : dungeon.rooms) {
                                        config.set(schematicName + ".dungeons." + objectCounter + ".room." + dungeon.rooms.indexOf(room) + ".name", room.name);
                                        config.set(schematicName + ".dungeons." + objectCounter + ".room." + dungeon.rooms.indexOf(room) + ".min-x", room.minLoc.getX() - clipboard.getClipboard().getOrigin().getX());
                                        config.set(schematicName + ".dungeons." + objectCounter + ".room." + dungeon.rooms.indexOf(room) + ".min-y", room.minLoc.getY() - clipboard.getClipboard().getOrigin().getY());
                                        config.set(schematicName + ".dungeons." + objectCounter + ".room." + dungeon.rooms.indexOf(room) + ".min-z", room.minLoc.getZ() - clipboard.getClipboard().getOrigin().getZ());
                                        config.set(schematicName + ".dungeons." + objectCounter + ".room." + dungeon.rooms.indexOf(room) + ".max-x", room.maxLoc.getX() - clipboard.getClipboard().getOrigin().getX());
                                        config.set(schematicName + ".dungeons." + objectCounter + ".room." + dungeon.rooms.indexOf(room) + ".max-y", room.maxLoc.getY() - clipboard.getClipboard().getOrigin().getY());
                                        config.set(schematicName + ".dungeons." + objectCounter + ".room." + dungeon.rooms.indexOf(room) + ".max-z", room.maxLoc.getZ() - clipboard.getClipboard().getOrigin().getZ());
                                        if (room.door != null) {
                                            config.set(schematicName + ".dungeons." + objectCounter + ".room." + dungeon.rooms.indexOf(room) + ".door-min-loc", (room.door.minLoc.getX() - clipboard.getClipboard().getOrigin().getX()) + "," + (room.door.minLoc.getY() - clipboard.getClipboard().getOrigin().getY()) + "," + (room.door.minLoc.getZ() - clipboard.getClipboard().getOrigin().getZ()));
                                            config.set(schematicName + ".dungeons." + objectCounter + ".room." + dungeon.rooms.indexOf(room) + ".door-max-loc", (room.door.maxLoc.getX() - clipboard.getClipboard().getOrigin().getX()) + "," + (room.door.maxLoc.getX() - clipboard.getClipboard().getOrigin().getY()) + "," + (room.door.maxLoc.getZ() - clipboard.getClipboard().getOrigin().getZ()));
                                            config.set(schematicName + ".dungeons." + objectCounter + ".room." + dungeon.rooms.indexOf(room) + ".door-armorstand-loc", (room.door.asLocation.getX() - clipboard.getClipboard().getOrigin().getX()) + "," + (room.door.asLocation.getY() - clipboard.getClipboard().getOrigin().getY()) + "," + (room.door.asLocation.getZ() - clipboard.getClipboard().getOrigin().getZ()) + "," + room.door.asLocation.getYaw());
                                        }
                                        if (room.rewardChest != null) {
                                            config.set(schematicName + ".dungeons." + objectCounter + ".room." + dungeon.rooms.indexOf(room) + ".reward-chest-loc", (room.rewardChest.loc.getX() - clipboard.getClipboard().getOrigin().getX()) + "," + (room.rewardChest.loc.getY() - clipboard.getClipboard().getOrigin().getY()) + "," + (room.rewardChest.loc.getZ() - clipboard.getClipboard().getOrigin().getZ()));
                                            config.set(schematicName + ".dungeons." + objectCounter + ".room." + dungeon.rooms.indexOf(room) + ".reward-chest-dir", room.rewardChest.direction.toString());
                                        }
                                    }
                                    p.sendMessage(plugin.dungeonLocList.get(targetLoc).getName() + " at " + (x - clipboard.getClipboard().getOrigin().getX()) + "," + (y - clipboard.getClipboard().getOrigin().getY()) + "," + (z - clipboard.getClipboard().getOrigin().getZ()) );
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
