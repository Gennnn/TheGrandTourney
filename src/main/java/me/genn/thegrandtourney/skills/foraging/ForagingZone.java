package me.genn.thegrandtourney.skills.foraging;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.shaded.effectlib.util.ParticleOptions;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.SessionManager;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.world.World;
import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.player.CritDamageIndicator;
import me.genn.thegrandtourney.player.NormalDamageIndicator;
import me.genn.thegrandtourney.skills.Station;
import me.genn.thegrandtourney.skills.TournamentZone;
import me.genn.thegrandtourney.skills.mining.Ore;
import net.minecraft.server.level.WorldServer;

import net.minecraft.world.entity.decoration.EntityArmorStand;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ForagingZone implements Listener, TournamentZone {
    public Location minLoc;
    public Location maxLoc;
    public ForagingZoneTemplate template;

    TGT plugin;
    List<Block> currentlyLoggedBlocks; //ha
    List<Tree> treesInZone;
    Map<UUID, ArmorStand> healthStands;
    public Location centerLoc;
    public String name;

    public ForagingZone(TGT plugin, ForagingZoneTemplate template) {
        this.plugin = plugin;
        this.template = template;

        this.currentlyLoggedBlocks = new ArrayList<>();
        this.treesInZone = new ArrayList<>();
        this.healthStands = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    @Override
    public void spawn(Player p) {
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
        this.minLoc = new Location(p.getWorld(), min.getBlockX(), min.getBlockY(), min.getBlockZ()).toCenterLocation();
        this.maxLoc = new Location(p.getWorld(), max.getBlockX(), max.getBlockY(), max.getBlockZ()).toCenterLocation();
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
        this.centerLoc = new Location(this.minLoc.getWorld(), this.minLoc.getX()+(this.maxLoc.getX()- this.minLoc.getX())*0.5,this.minLoc.getY()+(this.maxLoc.getY()- this.minLoc.getY())*0.5,this.minLoc.getZ()+(this.maxLoc.getZ()- this.minLoc.getZ())*0.5);
        ForagingZone zone = new ForagingZone(this.plugin, this.template);
        zone.name = template.name + "." + (plugin.foragingZoneLocList.size() + 1);
        zone.paste(minLoc, maxLoc);
    }
    @Override
    public void paste(Location minLoc, Location maxLoc) {
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
        this.centerLoc = new Location(this.minLoc.getWorld(), this.minLoc.getX()+(this.maxLoc.getX()- this.minLoc.getX())*0.5,this.minLoc.getY()+(this.maxLoc.getY()- this.minLoc.getY())*0.5,this.minLoc.getZ()+(this.maxLoc.getZ()- this.minLoc.getZ())*0.5);
        plugin.foragingZoneLocList.put(this.centerLoc, this);
        for (int x = (int)minLoc.getX(); x <= maxLoc.getX(); x++) {
            for (int y = (int)minLoc.getY(); y <= maxLoc.getY(); y++) {
                for (int z = (int)minLoc.getZ(); z <= maxLoc.getZ(); z++) {
                    Location loc = new Location(minLoc.getWorld(), x, y, z);
                    loc = loc.toBlockLocation();
                    if (isLogBlock(loc)) {
                        if (!currentlyLoggedBlocks.contains(loc.getBlock())) {
                            Tree tree = new Tree(this, plugin);
                            tree.create(loc.getBlock());
                            treesInZone.add(tree);
                        }
                    }
                }
            }
        }
    }
    @Override
    public void remove() {
        plugin.foragingZoneHandler.allSpawnedZones.remove(this);
        HandlerList.unregisterAll(this);
        this.maxLoc = null;
        this.minLoc = null;
        for (Tree tree : this.treesInZone) {
            tree.unregister();
        }
        this.treesInZone.clear();
        this.name = null;
        this.healthStands.clear();
        this.currentlyLoggedBlocks.clear();
    }
    @EventHandler
    public void onLogBreak(BlockBreakEvent e) {



            if (template.logBlocks.contains(e.getBlock().getType())) {
                e.setCancelled(true);
                for (Tree tree : treesInZone) {
                    if (tree.isBlockOnTree(e.getBlock()) && tree.health > 0) {
                        tree.lastActionTime = System.currentTimeMillis();
                        boolean crit = false;
                        if (tree.criticalBlock != null) {
                            crit = tree.criticalBlock.getLocation().toBlockLocation().distance(e.getBlock().getLocation().toBlockLocation()) < 0.25;
                        }
                        if (plugin.calculateDamageAxe(plugin.players.get(e.getPlayer().getUniqueId()), e.getPlayer().getItemInHand(), false) > tree.maxHealth) {
                            crit = true;
                        }
                        double dam = plugin.calculateDamageAxe(plugin.players.get(e.getPlayer().getUniqueId()), e.getPlayer().getItemInHand(), crit);
                        if (tree.health - dam <= 0) {
                            tree.health = 0;
                        } else {
                            tree.health = tree.health - dam;
                        }
                        if (crit) {
                            e.getBlock().getLocation().getWorld().playSound(e.getBlock().getLocation(), "entity.vex.hurt", 0.75f, 2f);
                            e.getBlock().getLocation().getWorld().playSound(e.getBlock().getLocation(), "entity.shulker.open", 2f, 0f);
                            plugin.listener.spectralDamage.spawnDamageIndicator(e.getPlayer(), e.getBlock().getLocation(), new CritDamageIndicator(), (int)dam);
                        } else {
                            plugin.listener.spectralDamage.spawnDamageIndicator(e.getPlayer(), e.getBlock().getLocation(), new NormalDamageIndicator(), (int)dam);
                        }
                        template.chopDrops.calculateDrops(e.getPlayer(), plugin.players.get(e.getPlayer().getUniqueId()).getLoggingFortune());
                        tree.dropFruit();
                        if (tree.health <= 0) {
                            if (crit) {
                                template.fellDrops.calculateDrops(e.getPlayer(), plugin.players.get(e.getPlayer().getUniqueId()).getLoggingFortune());
                                tree.dropAllFruits();
                                e.getBlock().getLocation().getWorld().playSound(e.getBlock().getLocation(), "entity.iron_golem.damage", 0.75f, 0f);
                            }
                            e.getBlock().getLocation().getWorld().playSound(e.getBlock().getLocation(), "entity.iron_golem.damage", 0.75f, 0f);
                            e.getBlock().getLocation().getWorld().playSound(e.getBlock().getLocation(), "block.chest.close", 1.2f, 0.5f);
                            tree.criticalBlock = null;
                            for (Block block : tree.logBlocks) {
                                Material restoreMat = block.getType();
                                block.setType(Material.AIR);
                                ParticleOptions po = new ParticleOptions((float) 0.2, (float) 0.3, (float) 0.2, 0.2F,5, 1F, (Color) null,restoreMat, (byte) 0);
                                List<Player> targetP = new ArrayList<>(Bukkit.getOnlinePlayers());
                                MagicSpells.getEffectManager().display(Particle.BLOCK_CRACK, po, block.getLocation(), 32.0D, targetP);
                                block.getLocation().getWorld().playSound(block.getLocation(), "block.wood.break", 0.5f, 0.5f);
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        block.setType(restoreMat);
                                        tree.health = tree.maxHealth;
                                        ParticleOptions po = new ParticleOptions((float) 0.2, (float) 0.3, (float) 0.2, 0.2F,5, 1F, (Color) null,null, (byte) 0);
                                        List<Player> targetP = new ArrayList<>(Bukkit.getOnlinePlayers());
                                        MagicSpells.getEffectManager().display(Particle.VILLAGER_HAPPY, po, block.getLocation(), 32.0D, targetP);
                                        block.getLocation().getWorld().playSound(block.getLocation(), "block.grass.break", 0.75f, 0.0f);
                                    }
                                }.runTaskLater(plugin, this.template.regenTime*20L);
                            }
                        } else {
                            tree.cycleCriticalBlock();
                        }
                    }
                }
            }


    }
    @EventHandler
    public void onLogHit(PlayerInteractEvent e) {

            if (e.getClickedBlock() != null && template.logBlocks.contains(e.getClickedBlock().getType())) {
                for (Tree tree : treesInZone) {
                    if (tree.isBlockOnTree(e.getClickedBlock())) {
                        double[] midPointVals = lerp3D(0.5, e.getPlayer().getLocation().getX(),e.getPlayer().getLocation().getY(),e.getPlayer().getLocation().getZ(),e.getClickedBlock().getX(),e.getClickedBlock().getY(),e.getClickedBlock().getZ());
                        Location midPoint = new Location(e.getPlayer().getWorld(), midPointVals[0], midPointVals[1] , midPointVals[2]);

                        if (!this.healthStands.containsKey(e.getPlayer().getUniqueId())) {
                            spawnHealthbar(midPoint, tree, e.getPlayer());
                        }
                        ArmorStand as = this.healthStands.get(e.getPlayer().getUniqueId());
                        as.teleport(midPoint);
                    }
                }
            }


    }
    public void spawnHealthbar(Location midPoint, Tree tree, Player player) {
        if (player.getLocation().getPitch() >= 0) {
            midPoint.add(0, 0.5, 0);
        } else {
            midPoint.add(0, -0.5, 0);
        }
        ArmorStand as = (ArmorStand) midPoint.getWorld().spawn(midPoint, ArmorStand.class);
        as.setGravity(false);
        as.setVisible(false);
        as.setVisibleByDefault(false);
        as.setCustomName(logName(tree, template.displayName));
        as.setCustomNameVisible(true);
        as.setMarker(true);

        player.showEntity(plugin, as);

        this.healthStands.put(player.getUniqueId(), as);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (ForagingZone.this.treesInZone.size() < 1) {
                    this.cancel();
                    return;
                }
                if (!(tree.health > 0) || player.getLocation().distance(as.getLocation()) > 9) {
                    cancel();
                    as.remove();
                    ForagingZone.this.healthStands.remove(player.getUniqueId());
                    return;
                }
                as.setCustomName(logName(tree, template.displayName));
            }
        }.runTaskTimer(plugin, 0L, 1L);

    }

    public boolean withinZone(Location loc) {
        return loc.getX() <= maxLoc.getX() &&
                loc.getX() >= minLoc.getX() &&
                loc.getY() <= maxLoc.getY() &&
                loc.getY() >= minLoc.getY() &&
                loc.getZ() <= maxLoc.getZ() &&
                loc.getZ() >= minLoc.getZ();
    }

    public boolean isLogBlock(Location loc) {
        for (Material mat : template.logBlocks) {
            if (loc.getBlock().getType() == mat) {
                return true;
            }
        }
        return false;
    }

    public static double[] lerp3D(double amount, double x1, double y1, double z1, double x2, double y2, double z2)
    {
        return new double[]{ x1+(x2-x1)*amount, y1+(y2-y1)*amount, z1+(z2-z1)*amount };
    }
    public String logName(Tree tree, String name) {
        String str = ChatColor.RED.toString() + name;
        if (tree.health == tree.maxHealth) {
            str = str + " " + ChatColor.GREEN.toString();
        } else {
            str = str + " " + ChatColor.YELLOW.toString();
        }
        str = str + (int)tree.health + ChatColor.WHITE.toString() + "/" + ChatColor.GREEN.toString() + (int)tree.maxHealth + ChatColor.RED.toString() + "❤";
        return str;
    }

    public String getName() {
        return template.name;
    }
}
