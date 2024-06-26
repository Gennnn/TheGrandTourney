package me.genn.thegrandtourney.skills.farming;

import com.nisovin.magicspells.MagicSpells;
import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.item.MMOItem;
import me.genn.thegrandtourney.skills.TournamentObject;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import scala.concurrent.impl.FutureConvertersImpl;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Crop implements Listener, TournamentObject {
    public Location loc;
    TGT plugin;
    CropTemplate template;
    Block block;
    Random r;
    boolean isGrown = true;
    BukkitTask regenerationTask;
    public Crop(TGT plugin, CropTemplate template) {
        this.plugin = plugin;
        this.template = template;
        this.r = new Random();
    }
    @Override
    public void spawn(Location loc) {
        this.loc = loc.toCenterLocation();

        this.block = this.loc.getBlock();
        block.setType(template.block);

        if (block.getBlockData() instanceof Ageable) {
            Ageable a = (Ageable) Crop.this.block.getBlockData();
            if (template.grownAge > a.getMaximumAge()) {
                template.grownAge = a.getMaximumAge();
            }
            a.setAge(template.grownAge);
            Crop.this.block.setBlockData(a);
        }
        if (block.getType() == Material.PLAYER_HEAD && template.grownBase64String != null) {
            Skull skull = (Skull) block;
            plugin.cropHandler.getHeadFrom64(template.grownBase64String, skull);
        }
        plugin.cropHandler.allSpawnedCrops.add(this);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    @Override
    public void paste(Location loc) {
        this.spawn(loc);
    }

    @Override
    public void remove() {
        plugin.cropHandler.allSpawnedCrops.remove(this);
        this.block.setType(Material.AIR);
        if (this.regenerationTask != null) {
            this.regenerationTask.cancel();
        }
        HandlerList.unregisterAll(this);
        this.loc = null;
    }

    @EventHandler
    public void onCropBreak2(PlayerInteractEvent e) {
        if (e.getAction() == Action.LEFT_CLICK_BLOCK) {


            Location blockLoc = e.getClickedBlock().getLocation().toCenterLocation();
            if (e.getClickedBlock().getBlockData() instanceof Ageable && ((Ageable) e.getClickedBlock().getBlockData()).getAge() == this.template.grownAge) {
                if (blockLoc.getX() == this.loc.getX() && blockLoc.getY() == this.loc.getY() && blockLoc.getZ() == this.loc.getZ() && e.getClickedBlock().getType() == this.template.block) {
                    if (e.getPlayer().getAttackCooldown() != 1.0) {
                        e.setCancelled(true);
                        return;
                    }
                    e.setCancelled(true);
                    breakEvents(e.getPlayer());
                    block.getLocation().getWorld().playSound(block.getLocation(), "block.grass.break", 0.5F, 0.75F);
                }
                return;
            } else if (e.getClickedBlock().getBlockData() instanceof Ageable && ((Ageable) e.getClickedBlock().getBlockData()).getAge() == this.template.regeneratingAge) {
                if (blockLoc.getX() == this.loc.getX() && blockLoc.getY() == this.loc.getY() && blockLoc.getZ() == this.loc.getZ() && e.getClickedBlock().getType() == this.template.regeneratingBlock && !e.isCancelled()) {
                    if (e.getPlayer().getAttackCooldown() != 1.0) {
                        e.setCancelled(true);
                        return;
                    }
                    List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
                    MagicSpells.getEffectManager().display(Particle.SMOKE_NORMAL, block.getLocation().toCenterLocation(),0.2F,0.2F,0.2F,0.0F,25,0.5F,(Color)null,(Material) null, (byte)0, 20.0D, players);
                    e.getPlayer().playSound(block.getLocation(), "entity.cat.hiss", 0.5F, 2F);
                    e.setCancelled(true);
                }
            } else if (!(e.getClickedBlock().getBlockData() instanceof Ageable) ) {
                if (blockLoc.getX() == this.loc.getX() && blockLoc.getY() == this.loc.getY() && blockLoc.getZ() == this.loc.getZ() && e.getClickedBlock().getType() == this.template.block) {
                    if (e.getPlayer().getAttackCooldown() != 1.0) {
                        e.setCancelled(true);
                        return;
                    }
                    e.setCancelled(true);
                    breakEvents(e.getPlayer());
                    block.getLocation().getWorld().playSound(block.getLocation(), "block.grass.break", 0.5F, 0.75F);
                } else if (blockLoc.getX() == this.loc.getX() && blockLoc.getY() == this.loc.getY() && blockLoc.getZ() == this.loc.getZ() && e.getClickedBlock().getType() == this.template.regeneratingBlock && !e.isCancelled()) {
                    if (e.getPlayer().getAttackCooldown() != 1.0) {
                        e.setCancelled(true);
                        return;
                    }
                    List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
                    MagicSpells.getEffectManager().display(Particle.SMOKE_NORMAL, block.getLocation().toCenterLocation(),0.2F,0.2F,0.2F,0.0F,25,0.5F,(Color)null,(Material) null, (byte)0, 20.0D, players);
                    e.getPlayer().playSound(block.getLocation(), "entity.cat.hiss", 0.5F, 2F);
                    e.setCancelled(true);
                }
            }
        }
    }

    public void breakEvents(Player player) {
        calculateDrops(player);
        this.isGrown = false;
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        MagicSpells.getEffectManager().display(Particle.BLOCK_CRACK, block.getLocation().toCenterLocation(),0.33F,0.33F,0.33F,0.25F,15,1F,(Color)null,(Material) block.getType(), (byte)0, 20.0D, players);
        Crop.this.block.setType(template.regeneratingBlock);
        if (Crop.this.block.getBlockData() instanceof Ageable) {
            Ageable a = (Ageable) Crop.this.block.getBlockData();
            if (template.regeneratingAge > a.getMaximumAge()) {
                template.regeneratingAge = a.getMaximumAge();
            }

            a.setAge(template.regeneratingAge);
            Crop.this.block.setBlockData(a);
        }
        if (template.regeneratingBlock == Material.PLAYER_HEAD && template.regeneratingBase64String != null) {
            Skull skull = (Skull) Crop.this.block.getState();
            plugin.cropHandler.getHeadFrom64(template.regeneratingBase64String, skull);

        }
        this.regenerationTask = new BukkitRunnable() {

            @Override
            public void run() {
                Crop.this.block.setType(template.block);
                Crop.this.isGrown = true;
                List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
                MagicSpells.getEffectManager().display(Particle.VILLAGER_HAPPY, block.getLocation().toCenterLocation(),0.35F,0.35F,0.35F,0.25F,25,8F,(Color)null,(Material) null, (byte)0, 20.0D, players);
                block.getLocation().getWorld().playSound(block.getLocation(), "block.grass.break", 0.5F, 0.75F);
                block.getLocation().getWorld().playSound(block.getLocation(), "entity.shulker_bullet.hit", 0.5F, 2F);
                block.getLocation().getWorld().playSound(block.getLocation(), "entity.shulker.close", 0.75F, 1F);
                if (Crop.this.block.getBlockData() instanceof Ageable) {
                    Ageable a = (Ageable) Crop.this.block.getBlockData();
                    if (template.grownAge > a.getMaximumAge()) {
                        template.grownAge = a.getMaximumAge();
                    }
                    a.setAge(template.grownAge);
                    Crop.this.block.setBlockData(a);
                }
                if (Crop.this.block.getType() == Material.PLAYER_HEAD && template.grownBase64String != null) {
                    Skull skull = (Skull) Crop.this.block;
                    plugin.cropHandler.getHeadFrom64(template.grownBase64String, skull);
                }
            }
        }.runTaskLater(plugin, 20L * template.regenerationTime);
    }

    public void calculateDrops(Player p) {
        template.drops.calculateDrops(p, plugin.players.get(p.getUniqueId()).getFarmingFortune());
    }
    public String getName() {
        return this.template.name;
    }

}
