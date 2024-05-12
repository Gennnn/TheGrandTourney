package me.genn.thegrandtourney.dungeons;

import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.item.MMOItem;
import me.genn.thegrandtourney.mobs.MMOMob;
import me.genn.thegrandtourney.util.IntMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class Room implements Listener {
    public String name;
    public Location minLoc;
    public Location maxLoc;
    public RoomGoal goal;

    public MMOMob mobToKill;
    public MMOItem itemToCollect;
    public int quantity;
    public boolean doorClosedByDefault = true;

    public boolean preventAbilities = false;
    public Door door;
    public IntMap<UUID> playerProgress;
    public List<UUID> completedPlayers;
    TGT plugin;
    BukkitTask boundsTask;
    public RewardChest rewardChest;

    public Room(TGT plugin) {
        this.playerProgress = new IntMap<>();
        this.completedPlayers = new ArrayList<>();
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }



    public void initializeRoom(Location minLoc, Location maxLoc) {
        this.minLoc = minLoc;
        this.maxLoc = maxLoc;
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
        this.boundsTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (withinBounds(player.getLocation())) {
                        plugin.playerAndDungeonRoom.put(player.getUniqueId(), Room.this);
                        if (door != null && door.playersAndProgressStands != null && !(door.playersAndProgressStands.containsKey(player.getUniqueId()))) {
                            door.summonProgressStand(player);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 3L);

    }

    public void unregister() {
        HandlerList.unregisterAll(this);
        if (this.boundsTask != null) {
            this.boundsTask.cancel();
        }
        if (this.door != null) {
            this.door.unregister();
        }
        if (this.rewardChest != null) {
            this.rewardChest.remove();
        }
        this.minLoc = null;
        this.maxLoc = null;
        this.name = null;

    }
    public boolean withinBounds(Location loc) {
        return loc.getX() <= maxLoc.getX() &&
                loc.getX() >= minLoc.getX() &&
                loc.getY() <= maxLoc.getY() &&
                loc.getY() >= minLoc.getY() &&
                loc.getZ() <= maxLoc.getZ() &&
                loc.getZ() >= minLoc.getZ();
    }


    public void incrementPlayerProgress(Player player) {
        if (completedPlayers.contains(player.getUniqueId())) {
            return;
        }
        if (!playerProgress.containsKey(player.getUniqueId())) {
            playerProgress.put(player.getUniqueId(), 0);
        }
        playerProgress.increment(player.getUniqueId());
        if (playerProgress.get(player.getUniqueId()) >= quantity) {
            completedPlayers.add(player.getUniqueId());
            this.door.doorLower(player);
        }
    }

    public void openDoorForThrough(Player player) {
        completedPlayers.add(player.getUniqueId());
        this.door.doorLower(player);
    }

    @EventHandler
    public void mobTarget (EntityTargetEvent e) {
        if (withinBounds(e.getEntity().getLocation())) {
            if (e.getTarget() instanceof Player) {
                if (!withinBounds(e.getTarget().getLocation())) {
                    e.setCancelled(true);
                }
            }
        }
    }


}
