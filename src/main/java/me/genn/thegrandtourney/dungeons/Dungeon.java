package me.genn.thegrandtourney.dungeons;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.shaded.effectlib.EffectManager;
import com.nisovin.magicspells.shaded.effectlib.effect.CircleEffect;
import com.nisovin.magicspells.shaded.effectlib.effect.CubeEffect;
import com.nisovin.magicspells.shaded.effectlib.effect.HelixEffect;
import com.nisovin.magicspells.shaded.effectlib.util.DynamicLocation;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.SessionManager;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.world.World;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.item.MMOItem;
import me.genn.thegrandtourney.mobs.MMOMob;
import me.genn.thegrandtourney.skills.Station;
import me.genn.thegrandtourney.skills.TournamentZone;
import me.genn.thegrandtourney.skills.fishing.FishingZone;
import me.genn.thegrandtourney.skills.foraging.Tree;
import me.genn.thegrandtourney.xp.XpType;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;

public class Dungeon implements Listener, TournamentZone {
    public DungeonTemplate template;
    String name;
    public LinkedList<Room> rooms;
    public Location centerLoc;
    public Location minLoc;
    public Location maxLoc;
    public Door entranceDoor;
    public Map<UUID, ArmorStand> playersAndEntranceStands;
    BukkitTask entranceStandTask;
    BukkitTask doorOpenTask;
    BukkitTask exitWarpTask;
    BukkitTask exitWarpEffectTask;
    public Location exitWarpLocation;
    public Location exitWarpTarget;
    ArmorStand exitLabelArmorStand;


    TGT plugin;
    public Dungeon(DungeonTemplate dungeonTemplate, TGT plugin) {
        this.plugin = plugin;
        this.template = dungeonTemplate;
        this.rooms = new LinkedList<>();
        this.playersAndEntranceStands = new HashMap<>();
    }

    @Override
    public void spawn(org.bukkit.entity.Player p) {
        SessionManager manager = WorldEdit.getInstance().getSessionManager();
        com.sk89q.worldedit.entity.Player actor = BukkitAdapter.adapt(p);
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
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();
        p.sendMessage("Defining dungeon " + template.name + " with coords " + min.getX() + "," + min.getY() + "," + min.getZ() + " minimum and " + max.getX() + "," + max.getY() + "," + max.getZ() + " maximum");

        this.minLoc = new Location(p.getWorld(), min.getX(), min.getY(), min.getZ()).toCenterLocation();
        this.maxLoc = new Location(p.getWorld(), max.getX(), max.getY(), max.getZ()).toCenterLocation();
        Location centerLoc = new Location(p.getWorld(), region.getCenter().getX(), region.getCenter().getY(), region.getCenter().getZ());
        if (this.maxLoc.getX() < this.minLoc.getX()) {
            double minX = this.maxLoc.getX();
            double maxX = this.minLoc.getX();
            this.maxLoc.setX(maxX);
            this.minLoc.setX(minX);
        }
        if (this.maxLoc.getY() < this.minLoc.getY()) {
            double minY = this.maxLoc.getY();
            double maxY = this.minLoc.getY();
            this.maxLoc.setY(maxY);
            this.minLoc.setY(minY);
        }
        if (this.maxLoc.getZ() < this.minLoc.getZ()) {
            double minZ = this.maxLoc.getZ();
            double maxZ = this.minLoc.getZ();
            this.maxLoc.setZ(maxZ);
            this.minLoc.setZ(minZ);
        }
        this.centerLoc = centerLoc.toCenterLocation();
        plugin.dungeonLocList.put(this.centerLoc,this);
        this.name = template.name + "." + (plugin.dungeonLocList.size()+1);
        Bukkit.getPluginManager().registerEvents(this, plugin);
        plugin.dungeonHandler.allDungeons.add(this);
    }
    public void createRoom(org.bukkit.entity.Player p, RoomGoal goal, String name, int quantity, String collectName, boolean preventAbilities, boolean doorClosed) {

        SessionManager manager = WorldEdit.getInstance().getSessionManager();
        com.sk89q.worldedit.entity.Player actor = BukkitAdapter.adapt(p);
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
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();
        p.sendMessage("Defining room " + name + " with coords " + min.getX() + "," + min.getY() + "," + min.getZ() + " minimum and " + max.getX() + "," + max.getY() + "," + max.getZ() + " maximum");

        Location minLoc = new Location(p.getWorld(), min.getX(), min.getY(), min.getZ()).toCenterLocation();
        Location maxLoc = new Location(p.getWorld(), max.getX(), max.getY(), max.getZ()).toCenterLocation();
        if (goal == RoomGoal.SLAYER) {
            addSlayerRoom(p, name, minLoc, maxLoc, quantity, collectName,preventAbilities);
        } else if (goal == RoomGoal.COLLECTION) {
            addCollectionRoom(p, name, minLoc, maxLoc, quantity, collectName,preventAbilities);
        } else if (goal == RoomGoal.THRU) {
            addThroughRoom(name, minLoc, maxLoc, doorClosed, preventAbilities);
        }

    }

    public void createDoor(org.bukkit.entity.Player p, String name) {
        if (getRoomWithName(name) == null) {
            p.sendMessage(ChatColor.RED + "You must specify a valid room name!");
            return;
        }
        SessionManager manager = WorldEdit.getInstance().getSessionManager();
        com.sk89q.worldedit.entity.Player actor = BukkitAdapter.adapt(p);
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
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();
        p.sendMessage("Defining door for room " + name + "with coords " + min.getX() + "," + min.getY() + "," + min.getZ() + " minimum and " + max.getX() + "," + max.getY() + "," + max.getZ() + " maximum");

        Location minLoc = new Location(p.getWorld(), min.getX(), min.getY(), min.getZ()).toCenterLocation();
        Location maxLoc = new Location(p.getWorld(), max.getX(), max.getY(), max.getZ()).toCenterLocation();

        setDoorForRoom(p, name, minLoc, maxLoc);
    }

    public void createDungeonDoor(org.bukkit.entity.Player p) {
        SessionManager manager = WorldEdit.getInstance().getSessionManager();
        com.sk89q.worldedit.entity.Player actor = BukkitAdapter.adapt(p);
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
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();
        p.sendMessage("Defining door for dungeon " + name + "with coords " + min.getX() + "," + min.getY() + "," + min.getZ() + " minimum and " + max.getX() + "," + max.getY() + "," + max.getZ() + " maximum");

        Location minLoc = new Location(p.getWorld(), min.getX(), min.getY(), min.getZ()).toCenterLocation();
        Location maxLoc = new Location(p.getWorld(), max.getX(), max.getY(), max.getZ()).toCenterLocation();

        setDoorForDungeon(p.getLocation(), name, minLoc, maxLoc);
    }
    public void createRewardChest(Player player, String name) {
        Room room = getRoomWithName(name);
        RoomData data = this.template.getRoomWithName(name);
        if (room == null) {
            player.sendMessage(ChatColor.RED + "You must specify a valid room name!");
            return;
        }
        if (data.rewardChestDrops == null || data.rewardChestBase64 == null || data.rewardChestBase == null || data.rewardChestMid == null) {
            player.sendMessage(ChatColor.RED + "The configuration for this reward chest isn't set up properly!");
            return;
        }
        RewardChest chest = new RewardChest(plugin, data.rewardChestBase, data.rewardChestMid, data.rewardChestBase64, data.rewardChestDrops, data.chestParticle, data.chestParticleCount);
        room.rewardChest = chest;
        chest.spawn(player.getLocation());
    }
    public void createExitWarp(Player player) {
        this.exitWarpLocation = player.getLocation();
        this.exitLabelArmorStand = player.getWorld().spawn(this.exitWarpLocation.clone().add(0,2.0,0), ArmorStand.class);
        this.exitLabelArmorStand.setVisible(false);
        this.exitLabelArmorStand.setGravity(false);
        this.exitLabelArmorStand.setCustomNameVisible(true);
        this.exitLabelArmorStand.setMarker(true);
        this.exitLabelArmorStand.setCustomName(ChatColor.DARK_PURPLE + "✦ " + ChatColor.LIGHT_PURPLE + "Return to Entrance");
        startExitWarpTask();
    }
    public void createExitWarp(Location location) {
        this.exitWarpLocation = location;
        this.exitLabelArmorStand = location.getWorld().spawn(this.exitWarpLocation.clone().add(0,2.0,0), ArmorStand.class);
        this.exitLabelArmorStand.setVisible(false);
        this.exitLabelArmorStand.setGravity(false);
        this.exitLabelArmorStand.setCustomNameVisible(true);
        this.exitLabelArmorStand.setMarker(true);
        this.exitLabelArmorStand.setCustomName(ChatColor.DARK_PURPLE + "✦ " + ChatColor.LIGHT_PURPLE + "Return to Entrance");
        startExitWarpTask();
    }
    public void createExitTarget(Player player) {
        this.exitWarpTarget = player.getLocation();
        startExitWarpTask();
    }
    public void createExitTarget(Location location) {
        this.exitWarpTarget = location;
        startExitWarpTask();
    }
    public void startExitWarpTask() {
        if (this.exitWarpLocation != null && this.exitWarpTarget != null && this.exitWarpTask == null && this.exitWarpEffectTask == null) {
            this.exitWarpTask = new BukkitRunnable() {

                @Override
                public void run() {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.getLocation().distance(Dungeon.this.exitWarpLocation) < 1) {
                            player.teleport(Dungeon.this.exitWarpTarget);
                            player.playSound(player, "entity.enderman.teleport", 1.0f, 1.5f);
                        }
                    }
                }
            }.runTaskTimer(plugin, 0L, 5L);
            this.exitWarpEffectTask = new BukkitRunnable() {

                @Override
                public void run() {
                    EffectManager em = MagicSpells.getEffectManager();
                    HelixEffect effect = new HelixEffect(em);
                    effect.enableRotation = true;
                    effect.yaw = 0f;
                    effect.particle = Particle.PORTAL;
                    effect.particles = 1;
                    effect.iterations = 30;
                    effect.arrivalTime = 3;
                    effect.speed = 0;

                    effect.radius = 0.8f;
                    effect.period = 2;
                    effect.strands = 2;
                    effect.setDynamicOrigin(new DynamicLocation(Dungeon.this.exitWarpLocation.clone().add(0,0.25,0)));
                    em.start(effect);


                    CircleEffect effect2 = new CircleEffect(em);
                    effect2.arrivalTime = 0;
                    effect2.enableRotation = false;
                    effect2.yaw = 0f;
                    effect2.particle = Particle.SPELL_WITCH;
                    effect2.particles = 20;
                    effect2.iterations = 60;
                    effect2.period = 1;
                    effect2.radius = 1;
                    effect2.setDynamicOrigin(new DynamicLocation(Dungeon.this.exitWarpLocation.clone().add(0,0,0)));
                    em.start(effect2);
                }
            }.runTaskTimer(plugin, 0L, 60L);

        }
    }
    @Override
    public void paste(Location minLoc, Location maxLoc) {

    }

    @Override
    public void remove() {
        plugin.dungeonHandler.allDungeons.remove(this);
        for (Room room : rooms) {
            room.unregister();
        }
        rooms.clear();
        if (this.entranceStandTask != null) {
            this.entranceStandTask.cancel();
        }
        if (this.exitWarpTask != null) {
            this.exitWarpTask.cancel();
        }
        this.exitLabelArmorStand.remove();
        if (this.doorOpenTask != null) {
            this.doorOpenTask.cancel();
        }
        if (entranceDoor != null) {
            entranceDoor.unregister();
        }


    }
    public void addSlayerRoom(String name, Location minLoc, Location maxLoc, int quantity, String mobName, boolean preventAbilities) {
        minLoc = minLoc.toCenterLocation();
        maxLoc = maxLoc.toCenterLocation();
        if (maxLoc.getX() < minLoc.getX()) {
            double minX = maxLoc.getX();
            double maxX = minLoc.getX();
            maxLoc.setX(maxX);
            minLoc.setX(minX);
        }
        if (maxLoc.getY() < minLoc.getY()) {
            double minY = maxLoc.getY();
            double maxY = minLoc.getY();
            maxLoc.setY(maxY);
            minLoc.setY(minY);
        }
        if (maxLoc.getZ() < minLoc.getZ()) {
            double minZ = maxLoc.getZ();
            double maxZ = minLoc.getZ();
            maxLoc.setZ(maxZ);
            minLoc.setZ(minZ);
        }
        MMOMob mob = plugin.mobHandler.getMobFromString(mobName);
        if (mob == null) {
            return;
        }
        Room room = new Room(plugin);
        room.mobToKill = mob;
        room.quantity = quantity;
        room.preventAbilities = preventAbilities;
        room.name = name;
        room.goal = RoomGoal.SLAYER;
        room.initializeRoom(minLoc,maxLoc);
        this.rooms.add(room);
    }
    public void addSlayerRoom(Player p, String name, Location minLoc, Location maxLoc, int quantity, String mobName, boolean preventAbilities) {
        minLoc = minLoc.toCenterLocation();
        maxLoc = maxLoc.toCenterLocation();
        if (maxLoc.getX() < minLoc.getX()) {
            double minX = maxLoc.getX();
            double maxX = minLoc.getX();
            maxLoc.setX(maxX);
            minLoc.setX(minX);
        }
        if (maxLoc.getY() < minLoc.getY()) {
            double minY = maxLoc.getY();
            double maxY = minLoc.getY();
            maxLoc.setY(maxY);
            minLoc.setY(minY);
        }
        if (maxLoc.getZ() < minLoc.getZ()) {
            double minZ = maxLoc.getZ();
            double maxZ = minLoc.getZ();
            maxLoc.setZ(maxZ);
            minLoc.setZ(minZ);
        }
        MMOMob mob = plugin.mobHandler.getMobFromString(mobName);
        if (mob == null) {
            p.sendMessage(ChatColor.RED + "You must enter a valid mob name!");
            return;
        }
        Room room = new Room(plugin);
        room.mobToKill = mob;
        room.quantity = quantity;
        room.preventAbilities = preventAbilities;
        room.name = name;
        room.goal = RoomGoal.SLAYER;
        room.initializeRoom(minLoc,maxLoc);
        this.rooms.add(room);
    }
    public void addCollectionRoom(Player p, String name, Location minLoc, Location maxLoc, int quantity, String itemName, boolean preventAbilities) {
        minLoc = minLoc.toCenterLocation();
        maxLoc = maxLoc.toCenterLocation();
        if (maxLoc.getX() < minLoc.getX()) {
            double minX = maxLoc.getX();
            double maxX = minLoc.getX();
            maxLoc.setX(maxX);
            minLoc.setX(minX);
        }
        if (maxLoc.getY() < minLoc.getY()) {
            double minY = maxLoc.getY();
            double maxY = minLoc.getY();
            maxLoc.setY(maxY);
            minLoc.setY(minY);
        }
        if (maxLoc.getZ() < minLoc.getZ()) {
            double minZ = maxLoc.getZ();
            double maxZ = minLoc.getZ();
            maxLoc.setZ(maxZ);
            minLoc.setZ(minZ);
        }
        MMOItem item = plugin.itemHandler.getMMOItemFromString(itemName);
        if (item == null) {
            p.sendMessage(ChatColor.RED + "You must enter a valid item name!");
        }
        Room room = new Room(plugin);
        room.itemToCollect = item;
        room.quantity = quantity;
        room.preventAbilities = preventAbilities;
        room.name = name;
        room.goal = RoomGoal.COLLECTION;
        room.initializeRoom(minLoc,maxLoc);
        this.rooms.add(room);
    }
    public void addCollectionRoom(String name, Location minLoc, Location maxLoc, int quantity, String itemName, boolean preventAbilities) {
        minLoc = minLoc.toCenterLocation();
        maxLoc = maxLoc.toCenterLocation();
        if (maxLoc.getX() < minLoc.getX()) {
            double minX = maxLoc.getX();
            double maxX = minLoc.getX();
            maxLoc.setX(maxX);
            minLoc.setX(minX);
        }
        if (maxLoc.getY() < minLoc.getY()) {
            double minY = maxLoc.getY();
            double maxY = minLoc.getY();
            maxLoc.setY(maxY);
            minLoc.setY(minY);
        }
        if (maxLoc.getZ() < minLoc.getZ()) {
            double minZ = maxLoc.getZ();
            double maxZ = minLoc.getZ();
            maxLoc.setZ(maxZ);
            minLoc.setZ(minZ);
        }
        MMOItem item = plugin.itemHandler.getMMOItemFromString(itemName);
        if (item == null) {
            return;
        }
        Room room = new Room(plugin);
        room.itemToCollect = item;
        room.quantity = quantity;
        room.preventAbilities = preventAbilities;
        room.name = name;
        room.goal = RoomGoal.COLLECTION;
        room.initializeRoom(minLoc,maxLoc);
        this.rooms.add(room);
    }
    public void addThroughRoom(String name, Location minLoc, Location maxLoc, boolean doorClosed, boolean preventAbilities) {
        minLoc = minLoc.toCenterLocation();
        maxLoc = maxLoc.toCenterLocation();
        if (maxLoc.getX() < minLoc.getX()) {
            double minX = maxLoc.getX();
            double maxX = minLoc.getX();
            maxLoc.setX(maxX);
            minLoc.setX(minX);
        }
        if (maxLoc.getY() < minLoc.getY()) {
            double minY = maxLoc.getY();
            double maxY = minLoc.getY();
            maxLoc.setY(maxY);
            minLoc.setY(minY);
        }
        if (maxLoc.getZ() < minLoc.getZ()) {
            double minZ = maxLoc.getZ();
            double maxZ = minLoc.getZ();
            maxLoc.setZ(maxZ);
            minLoc.setZ(minZ);
        }
        Room room = new Room(plugin);
        room.doorClosedByDefault = doorClosed;
        room.preventAbilities = preventAbilities;
        room.name = name;
        room.goal = RoomGoal.THRU;
        room.initializeRoom(minLoc,maxLoc);
        this.rooms.add(room);
    }
    public void setDoorForRoom(Location asLoc, String name, Location minLoc, Location maxLoc) {
        Room room = getRoomWithName(name);
        RoomData data = this.template.getRoomWithName(name);
        if (room == null) {
            //p.sendMessage(ChatColor.RED + "You must specify a valid room name!");
            return;
        }
        minLoc = minLoc.toCenterLocation();
        maxLoc = maxLoc.toCenterLocation();
        if (maxLoc.getX() < minLoc.getX()) {
            double minX = maxLoc.getX();
            double maxX = minLoc.getX();
            maxLoc.setX(maxX);
            minLoc.setX(minX);
        }
        if (maxLoc.getY() < minLoc.getY()) {
            double minY = maxLoc.getY();
            double maxY = minLoc.getY();
            maxLoc.setY(maxY);
            minLoc.setY(minY);
        }
        if (maxLoc.getZ() < minLoc.getZ()) {
            double minZ = maxLoc.getZ();
            double maxZ = minLoc.getZ();
            maxLoc.setZ(maxZ);
            minLoc.setZ(minZ);
        }
        Door door = new Door(plugin, data.doorMat, room, asLoc, data.goalText, minLoc, maxLoc);
        Vector max = Vector.getMaximum(minLoc.toVector(), maxLoc.toVector());
        Vector min = Vector.getMinimum(minLoc.toVector(), maxLoc.toVector());
        for (int i = min.getBlockX(); i <= max.getBlockX();i++) {
            for (int j = min.getBlockY(); j <= max.getBlockY(); j++) {
                for (int k = min.getBlockZ(); k <= max.getBlockZ();k++) {
                    door.blocks.add(minLoc.getWorld().getBlockAt(i,j,k));

                    //p.sendMessage("Adding door block at" + i +"," + j +"," + k);
                }
            }
        }
        /*for (int x = minLoc.getBlockX(); x < maxLoc.getBlockX(); x++) {
            for (int y = (int)minLoc.getBlockY(); y < (int)maxLoc.getBlockY(); y++) {
                for (int z = (int)minLoc.getBlockZ(); z < (int)maxLoc.getBlockZ(); z++) {

                }
            }
        }*/
        room.door = door;

    }
    public void setDoorForRoom(Player p, String name, Location minLoc, Location maxLoc) {
        Room room = getRoomWithName(name);
        RoomData data = this.template.getRoomWithName(name);
        if (room == null) {
            p.sendMessage(ChatColor.RED + "You must specify a valid room name!");
            return;
        }
        minLoc = minLoc.toCenterLocation();
        maxLoc = maxLoc.toCenterLocation();
        if (maxLoc.getX() < minLoc.getX()) {
            double minX = maxLoc.getX();
            double maxX = minLoc.getX();
            maxLoc.setX(maxX);
            minLoc.setX(minX);
        }
        if (maxLoc.getY() < minLoc.getY()) {
            double minY = maxLoc.getY();
            double maxY = minLoc.getY();
            maxLoc.setY(maxY);
            minLoc.setY(minY);
        }
        if (maxLoc.getZ() < minLoc.getZ()) {
            double minZ = maxLoc.getZ();
            double maxZ = minLoc.getZ();
            maxLoc.setZ(maxZ);
            minLoc.setZ(minZ);
        }
        Door door = new Door(plugin, data.doorMat, room, p.getLocation(), data.goalText, minLoc, maxLoc);
        Vector max = Vector.getMaximum(minLoc.toVector(), maxLoc.toVector());
        Vector min = Vector.getMinimum(minLoc.toVector(), maxLoc.toVector());
        for (int i = min.getBlockX(); i <= max.getBlockX();i++) {
            for (int j = min.getBlockY(); j <= max.getBlockY(); j++) {
                for (int k = min.getBlockZ(); k <= max.getBlockZ();k++) {
                    door.blocks.add(minLoc.getWorld().getBlockAt(i,j,k));

                    //p.sendMessage("Adding door block at" + i +"," + j +"," + k);
                }
            }
        }
        /*for (int x = minLoc.getBlockX(); x < maxLoc.getBlockX(); x++) {
            for (int y = (int)minLoc.getBlockY(); y < (int)maxLoc.getBlockY(); y++) {
                for (int z = (int)minLoc.getBlockZ(); z < (int)maxLoc.getBlockZ(); z++) {

                }
            }
        }*/
        room.door = door;

    }
    public void setDoorForDungeon(Location asLoc, String name, Location minLoc, Location maxLoc) {
        minLoc = minLoc.toCenterLocation();
        maxLoc = maxLoc.toCenterLocation();
        if (maxLoc.getX() < minLoc.getX()) {
            double minX = maxLoc.getX();
            double maxX = minLoc.getX();
            maxLoc.setX(maxX);
            minLoc.setX(minX);
        }
        if (maxLoc.getY() < minLoc.getY()) {
            double minY = maxLoc.getY();
            double maxY = minLoc.getY();
            maxLoc.setY(maxY);
            minLoc.setY(minY);
        }
        if (maxLoc.getZ() < minLoc.getZ()) {
            double minZ = maxLoc.getZ();
            double maxZ = minLoc.getZ();
            maxLoc.setZ(maxZ);
            minLoc.setZ(minZ);
        }
        Door door = new Door(plugin, template.doorMat, null, asLoc, template.enterText, minLoc, maxLoc);
        Vector max = Vector.getMaximum(minLoc.toVector(), maxLoc.toVector());
        Vector min = Vector.getMinimum(minLoc.toVector(), maxLoc.toVector());
        for (int i = min.getBlockX(); i <= max.getBlockX();i++) {
            for (int j = min.getBlockY(); j <= max.getBlockY(); j++) {
                for (int k = min.getBlockZ(); k <= max.getBlockZ();k++) {
                    door.blocks.add(minLoc.getWorld().getBlockAt(i,j,k));
                    //p.sendMessage("Adding door block at" + i +"," + j +"," + k);
                }
            }
        }
        /*for (int x = minLoc.getBlockX(); x < maxLoc.getBlockX(); x++) {
            for (int y = (int)minLoc.getBlockY(); y < (int)maxLoc.getBlockY(); y++) {
                for (int z = (int)minLoc.getBlockZ(); z < (int)maxLoc.getBlockZ(); z++) {

                }
            }
        }*/
        this.entranceDoor = door;
        this.entranceStandTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!Dungeon.this.entranceDoor.openedPlayers.contains(player.getUniqueId()) && !Dungeon.this.entranceDoor.playersAndProgressStands.containsKey(player.getUniqueId())) {
                        Dungeon.this.entranceDoor.summonEntranceStand(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 100L);

        this.doorOpenTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Entity entity : entranceDoor.asLocation.getNearbyEntities(template.openRadius,template.openRadius/2,template.openRadius)) {
                    if (entity instanceof Player) {
                        Player player = (Player) entity;
                        if (hasKey(player) && !entranceDoor.openedPlayers.contains(player.getUniqueId())) {
                            entranceDoor.hideEntranceStand(player);
                            entranceDoor.doorLower(player);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 15L);
    }
    public Room getRoomWithName(String roomName) {
        return this.rooms.stream().filter(o -> o.name.equals(roomName)).findFirst().orElse(null);
    }
    public boolean withinBounds(Location loc) {
        return loc.getX() <= maxLoc.getX() &&
                loc.getX() >= minLoc.getX() &&
                loc.getY() <= maxLoc.getY() &&
                loc.getY() >= minLoc.getY() &&
                loc.getZ() <= maxLoc.getZ() &&
                loc.getZ() >= minLoc.getZ();
    }

    public String getName() {
        return this.template.name;
    }

    public boolean hasKey(Player player)
    {
        PlayerInventory inventory = player.getInventory();
        ItemStack[] items = inventory.getContents();
        int has = 0;
        for (ItemStack item : items)
        {
            if ((item != null) && (item.getAmount() > 0) && (item.hasItemMeta()))
            {
                NBTItem nbtI = new NBTItem(item);
                if (nbtI.hasTag("ExtraAttributes")) {
                    NBTCompound comp = nbtI.getCompound("ExtraAttributes");
                    if (comp.hasTag("id")) {
                        String mmoId = comp.getString("id");
                        if (mmoId.equalsIgnoreCase(this.template.key.internalName)) {
                            return true;
                        }
                    }
                }

            }
        }
        return false;
    }


}
