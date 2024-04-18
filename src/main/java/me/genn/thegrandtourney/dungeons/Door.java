package me.genn.thegrandtourney.dungeons;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.shaded.effectlib.util.ParticleOptions;
import com.sk89q.worldedit.math.BlockVector3;
import me.genn.thegrandtourney.TGT;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class Door {
    List<Block> blocks;
    public Material blockType;
    TGT plugin;
    List<UUID> openedPlayers;
    Location asLocation;
    Room parentRoom;
    Map<UUID, ArmorStand> playersAndProgressStands;
    List<ArmorStand> allArmorStands;
    String goalText;
    BukkitTask blockChangeTask;


    public Door(TGT plugin, Material mat, Room room, Location location, String goalText) {
        this.blocks = new ArrayList<>();
        this.plugin = plugin;
        this.blockType = mat;
        this.parentRoom = room;
        this.asLocation = location.add(0,1.5,0);
        this.playersAndProgressStands = new HashMap<>();
        this.allArmorStands = new ArrayList<>();
        this.openedPlayers = new ArrayList<>();
        this.goalText = goalText;
        this.blockChangeTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!openedPlayers.contains(player.getUniqueId())) {
                        for (Block block : blocks) {
                            player.sendBlockChange(block.getLocation(), Door.this.blockType, (byte)0);

                        }
                    }
                }
                for (Block block : blocks) {
                    for (Entity entity : block.getLocation().getNearbyEntities(5,5,5)) {
                        if (entity.getLocation().toCenterLocation().distance(block.getLocation().toCenterLocation()) < 0.9 && !(entity instanceof Player)) {
                            entity.setVelocity(entity.getVelocity().multiply(-2));
                            entity.setVelocity(entity.getVelocity().setY(0));

                        }
                    }
                }

            }
        }.runTaskTimer(plugin, 0L, 4L);
    }




    public List<String> generateList(BlockVector3 origin) {
        List<String> doorBlocks = new ArrayList<>();
        for (Block block : blocks) {
            String str = (block.getLocation().toCenterLocation().getX() - origin.getX()) + "," + (block.getLocation().toCenterLocation().getY() - origin.getY()) + "," + (block.getLocation().toCenterLocation().getZ() - origin.getZ());
            doorBlocks.add(str);
        }
        return doorBlocks;
    }
    public void unregister() {
        if (this.blockChangeTask != null) {
            this.blockChangeTask.cancel();
        }
        for (ArmorStand stand : allArmorStands) {
            stand.remove();
        }
        for (Block block : blocks) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendBlockChange(block.getLocation(), block.getType(), (byte)0);
            }
        }

    }

    public void doorLower(Player player) {
        ParticleOptions po = new ParticleOptions((float) 0.2, (float) 0.3, (float) 0.2, 0.2F,5, 1F, (Color) null,blockType, (byte) 0);
        List<Player> targetP = new ArrayList<>();
        targetP.add(player);

        for (Block block : blocks) {
            this.openedPlayers.add(player.getUniqueId());
            player.sendBlockChange(block.getLocation(), block.getType(), (byte)0);
            MagicSpells.getEffectManager().display(Particle.BLOCK_CRACK, po, block.getLocation(), 32.0D, targetP);
            block.getLocation().getWorld().playSound(block.getLocation(), block.getBlockSoundGroup().getBreakSound(), 0.75f, 1.0f);
            //player.sendBlockChange(block.getLocation(), Material.AIR.createBlockData().merge(block.getBlockData()));
        }
    }

    public void summonEntranceStand(Player player) {
        ArmorStand as = player.getWorld().spawn(this.asLocation.clone(), ArmorStand.class);
        as.setVisible(false);
        as.setVisibleByDefault(false);
        as.setGravity(false);
        as.setCustomNameVisible(true);
        as.setMarker(true);
        as.setCustomName(this.goalText);
        player.showEntity(plugin, as);
        playersAndProgressStands.put(player.getUniqueId(), as);
        this.allArmorStands.add(as);
    }
    public void hideEntranceStand(Player player) {
        if (playersAndProgressStands.containsKey(player.getUniqueId())) {
            ArmorStand armorStand = playersAndProgressStands.get(player.getUniqueId());
            armorStand.remove();
            playersAndProgressStands.remove(player.getUniqueId());
            this.openedPlayers.add(player.getUniqueId());
        }
    }

    public void summonProgressStand(Player player) {
        if (!(this.playersAndProgressStands.containsKey(player.getUniqueId()))) {
            ArmorStand as = player.getWorld().spawn(this.asLocation.clone(), ArmorStand.class);
            as.setVisible(false);
            as.setVisibleByDefault(false);
            as.setGravity(false);
            as.setCustomNameVisible(true);
            as.setMarker(true);

            String nameString = ChatColor.YELLOW + ChatColor.BOLD.toString() + "GOAL: " + ChatColor.RESET;
            if (parentRoom.goal == RoomGoal.SLAYER) {
                nameString = nameString + ChatColor.GRAY + "Slay " + parentRoom.quantity + " " + parentRoom.mobToKill.nameplateName + ChatColor.GRAY + " to continue.";
                ArmorStand as2 = player.getWorld().spawn(this.asLocation.clone().add(0,-0.5,0), ArmorStand.class);
                as2.setVisible(false);
                as2.setCustomNameVisible(true);
                as2.setVisibleByDefault(false);
                as2.setGravity(false);
                as2.setMarker(true);
                as2.setCustomName(ChatColor.RED + "Progress: 0 /" + parentRoom.quantity);
                player.showEntity(plugin, as2);
                this.allArmorStands.add(as2);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!as2.isValid()) {
                            this.cancel();
                            return;
                        }
                        if (parentRoom.completedPlayers.contains(player.getUniqueId())) {
                            as2.setCustomName(ChatColor.GREEN + "Progress: Completed!");
                            this.cancel();
                            return;
                        }
                        as2.setCustomName(ChatColor.RED + "Progress: " + parentRoom.playerProgress.get(player.getUniqueId()) + " / " + parentRoom.quantity);
                    }
                }.runTaskTimer(plugin, 0L, 10L);
            } else if (parentRoom.goal == RoomGoal.COLLECTION) {
                nameString += ChatColor.GRAY + "Collect " + parentRoom.quantity + " " + parentRoom.itemToCollect.displayName + ChatColor.GRAY +" to continue.";
                ArmorStand as2 = player.getWorld().spawn(this.asLocation.clone().add(0,-0.5,0), ArmorStand.class);
                as2.setCustomNameVisible(true);
                as2.setVisible(false);
                as2.setVisibleByDefault(false);
                as2.setGravity(false);
                as2.setMarker(true);
                as2.setCustomName(ChatColor.RED + "Progress: 0 /" + parentRoom.quantity);
                player.showEntity(plugin, as2);
                this.allArmorStands.add(as2);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!as2.isValid()) {
                            this.cancel();
                            return;
                        }
                        if (parentRoom.completedPlayers.contains(player.getUniqueId())) {
                            as2.setCustomName(ChatColor.GREEN + "Progress: Completed!");
                            this.cancel();
                            return;
                        }
                        as2.setCustomName(ChatColor.RED + "Progress: " + parentRoom.playerProgress.get(player.getUniqueId()) + " / " + parentRoom.quantity);
                    }
                }.runTaskTimer(plugin, 0L, 10L);
            } else {
                nameString += ChatColor.GRAY + "Reach the end of the room.";
            }
            if (this.goalText != null) {
                nameString = ChatColor.YELLOW + ChatColor.BOLD.toString() + "GOAL: " + ChatColor.RESET + this.goalText;
            }
            as.setCustomName(nameString);
            player.showEntity(plugin, as);
            playersAndProgressStands.put(player.getUniqueId(), as);
            this.allArmorStands.add(as);
        }
    }

}
