package me.genn.thegrandtourney.skills;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.shaded.effectlib.util.ParticleOptions;
import de.tr7zw.nbtapi.NBTItem;
import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.grid.Direction;
import me.genn.thegrandtourney.player.MMOPlayer;
import me.genn.thegrandtourney.skills.mining.Ore;
import me.genn.thegrandtourney.xp.XpType;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.sql.Time;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Craft implements Listener {
    TGT plugin;
    String currentCraft;
    Random r;
    boolean hasProgressStand = false;
    ArmorStand as;
    ArmorStand instructionAs;
    float currentTaskProgress = 0f;
    public float totalProgress = 0f;
    int[] timingCount = {2};
    boolean cancelNextTimingTask = false;
    boolean currentlyTiming = false;
    BukkitTask timingRunnable;
    boolean craftActive = false;
    boolean firstClick = true;
    public double totalCraftScore;


    public Craft(TGT plugin) {
        this.plugin = plugin;
        this.r = new Random();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void startCraft(Player player, Recipe recipe, Station station) {
        this.craftActive = true;
        MMOPlayer mmoPlayer = plugin.players.get(player.getUniqueId());
        mmoPlayer.isCrafting = true;
        mmoPlayer.craftStart = player.getLocation();
        mmoPlayer.currentStation = station;
        this.totalCraftScore = recipe.craftingScore;
        Location tpLocation = station.spawnLocation;
        tpLocation.setY((int)tpLocation.getY());
        player.teleport(tpLocation);
        mmoPlayer.currentCraftObj = this;
        final int[] countdown = {3, 10, 30};
        new BukkitRunnable() {
            @Override
            public void run() {

                if (countdown[2] < 1) {
                    cancel();
                    return;
                } else {
                    player.teleport(tpLocation);
                    countdown[2]--;
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
        new BukkitRunnable() {
            @Override
            public void run() {

                if (countdown[0] < 1) {

                    player.playSound(player.getLocation(), "block.note_block.pling", 1.5f, 1.5f);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (countdown[1] < 1) {
                                player.sendTitle("", ChatColor.GREEN + ChatColor.BOLD.toString() + "GO!", 0, 7, 15);
                                firstCraft(recipe, player, station);
                                cancel();
                                return;
                            }
                            if (countdown[1] % 2 == 0) {
                                player.sendTitle("", ChatColor.GREEN + ChatColor.BOLD.toString() + "GO!", 0, 7, 0);
                            } else {
                                player.sendTitle("", ChatColor.WHITE + ChatColor.BOLD.toString() + "GO!", 0, 7, 0);
                            }
                            countdown[1]--;
                        }
                    }.runTaskTimer(plugin, 0L, 5L);
                    cancel();
                    return;
                }
                if (countdown[0] == 3) {
                    player.sendTitle(ChatColor.GRAY + "Craft starts in...", ChatColor.RED + ChatColor.BOLD.toString() + "3", 2, 22, 0);
                } else if (countdown[0] == 2) {
                    player.sendTitle(ChatColor.GRAY + "Craft starts in...", ChatColor.GOLD + ChatColor.BOLD.toString() + "2", 0, 22, 0);
                } else if (countdown[0] == 1) {
                    player.sendTitle(ChatColor.GRAY + "Craft starts in...", ChatColor.YELLOW + ChatColor.BOLD.toString() + "1", 0, 22, 0);
                }
                player.playSound(player.getLocation(), "block.note_block.pling", 1.5f, 0.75f);
                countdown[0]--;
            }
        }.runTaskTimer(plugin, 0L, 20L);


    }
    public void firstCraft(Recipe recipe, Player player, Station station) {
        selectCraft(player, recipe);
        startLoop(recipe, player, station);
    }
    public void startLoop(Recipe recipe, Player player, Station station) {
        final long[] countdown = {recipe.timeLimit*20, 0L};
        new BukkitRunnable() {
            @Override
            public void run() {

                if (countdown[0] < 1) {
                    craftFailState(player);
                    cancel();
                    return;
                } else {
                    setBossBar(player, recipe, countdown[1]);
                    armorStandSpawn(player, recipe, station);
                    if (Craft.this.currentTaskProgress >= recipe.craftingScorePerTask) {
                        Craft.this.totalProgress += recipe.craftingScorePerTask;
                        if (Craft.this.timingRunnable != null && !Craft.this.timingRunnable.isCancelled()) {
                            Craft.this.timingRunnable.cancel();
                        }
                        ParticleOptions po = new ParticleOptions((float) 0.4, (float) 0.3, (float) 0.4, 0.2F,15, 1F, (Color) null,null, (byte) 0);
                        List<Player> targetP = new ArrayList<>();
                        targetP.add(player);
                        if (plugin.players.get(player.getUniqueId()).currentCraft.equalsIgnoreCase("mashing")) {
                            MagicSpells.getEffectManager().display(Particle.FIREWORKS_SPARK, po, station.mashingTable.loc, 32.0D, targetP);
                            player.playSound(station.mashingTable.loc, "entity.player.levelup", 1.0f, 2.0f);
                        } else if (plugin.players.get(player.getUniqueId()).currentCraft.equalsIgnoreCase("timing")) {
                            MagicSpells.getEffectManager().display(Particle.FIREWORKS_SPARK, po, station.timingTable.loc, 32.0D, targetP);
                            player.playSound(station.timingTable.loc, "entity.player.levelup", 1.0f, 2.0f);
                        } else if (plugin.players.get(player.getUniqueId()).currentCraft.equalsIgnoreCase("holding")) {
                            MagicSpells.getEffectManager().display(Particle.FIREWORKS_SPARK, po, station.holdingTable.loc, 32.0D, targetP);
                            player.playSound(station.holdingTable.loc, "entity.player.levelup", 1.0f, 2.0f);
                        }

                        if (Craft.this.totalProgress >= recipe.craftingScore) {
                            finishCraft(player, recipe, countdown[1]);
                            cancel();
                        } else {
                            selectCraft(player, recipe);
                        }
                        Craft.this.as.remove();
                        Craft.this.instructionAs.remove();
                        Craft.this.hasProgressStand = false;
                    }
                }
                float expProg = Craft.this.totalProgress /  (float) recipe.craftingScore;
                if (expProg > 1) {
                    expProg = 1f;
                }
                if (!boundsCheck(player,station)) {
                    Location tpLocation = station.spawnLocation;
                    tpLocation.setY((int)tpLocation.getY());
                    player.teleport(tpLocation);
                    player.sendMessage(ChatColor.RED + "You can't leave the designated crafting area!");
                }
                player.setExp(expProg);
                player.setLevel(0);
                countdown[1]++;
                countdown[0]--;
            }
        }.runTaskTimer(plugin, 0L, 1L);


    }
    public boolean boundsCheck(Player player, Station station) {
        Location loc = player.getLocation();
        return loc.getX() >= station.minLoc.getX()
                && loc.getY() >= station.minLoc.getY()
                && loc.getZ() >= station.minLoc.getZ()
                && loc.getX() <= station.maxLoc.getX()
                && loc.getY() <= station.maxLoc.getY()
                && loc.getZ() <= station.maxLoc.getZ();
    }
    public void initializeTimingRunnable(Player player) {
        Craft.this.timingRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.players.get(player.getUniqueId()).currentCraft.equalsIgnoreCase("timing")) {
                    Craft.this.timingCount[0] = 1;
                    cancel();
                    return;
                }
                if (Craft.this.timingCount[0] == 2 || Craft.this.timingCount[0] == 1) {
                    player.playSound(player, "block.note_block.hat", 1.0f, 0.5f);
                    Craft.this.timingCount[0]--;
                    if (Craft.this.timingCount[0] == 1) {
                        instructionAs.setCustomName(ChatColor.RED + "Wait...");
                    } else {
                        instructionAs.setCustomName(ChatColor.GOLD + "Wait...");
                    }
                } else if (Craft.this.timingCount[0] == 0) {
                    Craft.this.timingCount[0] = 2;
                    player.playSound(player, "block.note_block.harp", 1.0f, 1.0f);
                    instructionAs.setCustomName(ChatColor.YELLOW + ChatColor.BOLD.toString() + "L-CLICK!");

                }
            }
        }.runTaskTimer(plugin, 10L, 10L);
    }
    public void armorStandSpawn(Player player, Recipe recipe, Station station) {
        if (!Craft.this.hasProgressStand) {
            Interaction entity;
            if (Craft.this.currentCraft.equalsIgnoreCase("mashing")) {
                entity = station.mashingTable.entity;
            } else if (Craft.this.currentCraft.equalsIgnoreCase("timing")) {
                entity = station.timingTable.entity;
                initializeTimingRunnable(player);
            } else {
                entity = station.holdingTable.entity;
            }
            Location loc = entity.getLocation();
            loc = loc.add(0,1.0d,0);
            ArmorStand as = (ArmorStand) entity.getLocation().getWorld().spawn(loc, ArmorStand.class);
            as.setGravity(false);
            as.setCustomName(ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + "||||||||||||" + ChatColor.DARK_GRAY + "]");
            as.setCustomNameVisible(true);
            as.setVisible(false);
            as.setMarker(true);
            as.setVisibleByDefault(false);
            player.showEntity(plugin, as);
            Craft.this.as = as;
            Craft.this.hasProgressStand = true;
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (Craft.this.currentTaskProgress >= recipe.craftingScorePerTask){
                        cancel();
                    } else {
                        Craft.this.as.setCustomName(formatProgressName(recipe));
                    }
                }
            }.runTaskTimer(plugin, 0L, 2L);
            loc = loc.add(0,0.25d,0);
            ArmorStand instructionAs = entity.getLocation().getWorld().spawn(loc, ArmorStand.class);
            instructionAs.setGravity(false);
            if (Craft.this.currentCraft.equalsIgnoreCase("mashing")) {
                instructionAs.setCustomName(ChatColor.YELLOW + ChatColor.BOLD.toString() + "L-CLICK!");
            } else if (Craft.this.currentCraft.equalsIgnoreCase("timing")) {
                instructionAs.setCustomName(ChatColor.RED + "Wait...");
            } else {
                instructionAs.setCustomName(ChatColor.YELLOW + ChatColor.BOLD.toString() + "R-CLICK!");
            }
            instructionAs.setCustomNameVisible(true);
            instructionAs.setVisible(false);
            instructionAs.setMarker(true);
            instructionAs.setVisibleByDefault(false);
            player.showEntity(plugin, instructionAs);
            Craft.this.instructionAs = instructionAs;
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (Craft.this.currentTaskProgress >= recipe.craftingScorePerTask){
                        cancel();
                    } else {
                        if (instructionAs.getCustomName().startsWith(ChatColor.YELLOW.toString())) {
                            instructionAs.setCustomName(ChatColor.WHITE + ChatColor.BOLD.toString() + ChatColor.stripColor(instructionAs.getCustomName()));
                        } else if (instructionAs.getCustomName().startsWith(ChatColor.WHITE.toString())) {
                            instructionAs.setCustomName(ChatColor.YELLOW + ChatColor.BOLD.toString() + ChatColor.stripColor(instructionAs.getCustomName()));
                        }
                    }
                }
            }.runTaskTimer(plugin, 0L, 2L);

        }
    }
    public String formatProgressName(Recipe recipe) {
        String retString = ChatColor.DARK_GRAY + "[";
        int num = 12;
        float percentUnfilled = (int) (12 * ((recipe.craftingScorePerTask-this.currentTaskProgress)/recipe.craftingScorePerTask));
        for (int i = 0; i < (12-percentUnfilled); i++) {
            if (i < 3) {
                retString = retString.concat(ChatColor.RED + "|");
            } else if (i < 6) {
                retString = retString.concat( ChatColor.GOLD + "|");
            } else if (i < 9) {
                retString = retString.concat( ChatColor.YELLOW + "|");
            } else {
                retString = retString.concat( ChatColor.GREEN + "|");
            }
        }
        for (int i = 0; i <percentUnfilled; i++) {
            retString += ChatColor.GRAY + "|";
        }
        retString = retString + ChatColor.DARK_GRAY + "]";
        return retString;
    }
    public void selectCraft(Player player, Recipe recipe) {
        firstClick = true;
        int num = r.nextInt(3);
        if (num == 0) {
            this.currentCraft = "mashing";
        } else if (num == 1) {
            this.currentCraft = "timing";
        } else {
            this.currentCraft = "holding";
        }
        if (this.currentCraft.equalsIgnoreCase("mashing")) {
            if (recipe.type == XpType.TAILORING) {
                player.sendTitle(ChatColor.YELLOW + "CUT!", "", 5, 30, 5);
            } else if (recipe.type == XpType.BLACKSMITHING) {
                player.sendTitle(ChatColor.YELLOW + "HAMMER!", "", 5, 30, 5);
            }
        } else if (this.currentCraft.equalsIgnoreCase("timing")) {
            if (recipe.type == XpType.TAILORING) {
                player.sendTitle(ChatColor.GOLD + "THREAD!", "", 5, 30, 5);
            } else if (recipe.type == XpType.BLACKSMITHING) {
                player.sendTitle(ChatColor.GOLD + "TEMPER!", "", 5, 30, 5);
            }
        } else {
            if (recipe.type == XpType.TAILORING) {
                player.sendTitle(ChatColor.RED + "SEW!", "", 5, 30, 5);
            } else if (recipe.type == XpType.BLACKSMITHING) {
                player.sendTitle(ChatColor.RED + "QUENCH!", "", 5, 30, 5);
            }
        }
        plugin.players.get(player.getUniqueId()).currentCraft = this.currentCraft;
        if (this.timingRunnable != null && !this.timingRunnable.isCancelled()) {
            this.timingRunnable.cancel();
        }
        this.currentTaskProgress = 0;
    }
    public void craftFailState(Player player) {
        player.teleport(plugin.players.get(player.getUniqueId()).craftStart);
        player.hideBossBar(plugin.currentlyDisplayedBossBar.get(player));
        plugin.players.get(player.getUniqueId()).isCrafting = false;
        player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "CRAFT FAILED! " + ChatColor.RESET + ChatColor.GRAY + "You failed to complete the craft in time!");
        player.playSound(player, "entity.ender_dragon.hurt", 2.0f, 0.0f);
        if (this.timingRunnable != null && !this.timingRunnable.isCancelled()) {
            this.timingRunnable.cancel();
        }
        MMOPlayer mmoPlayer = plugin.players.get(player.getUniqueId());
        mmoPlayer.currentCraft = "";
        mmoPlayer.isCrafting = false;
        mmoPlayer.currentStation = null;
        mmoPlayer.currentCraftObj = null;
        this.as.remove();
        this.craftActive = false;
    }

    public void setBossBar(Player player, Recipe recipe, long time) {
        String text = ChatColor.YELLOW + "Time Remaining: " + formatTime(((recipe.timeLimit*20*1000) - (time*1000))/20);
        float progress = ((float)(recipe.timeLimit*20) - (float)time)/((float)recipe.timeLimit*20);
        final BossBar bar = BossBar.bossBar(Component.text(text), progress, BossBar.Color.RED, BossBar.Overlay.NOTCHED_20);
        if (plugin.currentlyDisplayedBossBar.get(player) != null) {
            player.hideBossBar(plugin.currentlyDisplayedBossBar.get(player));
        }
        plugin.currentlyDisplayedBossBar.put(player, bar);
        player.showBossBar(bar);
    }

    public String formatTime(long time) {
        long mins = TimeUnit.MILLISECONDS.toMinutes(time);
        time -= TimeUnit.MINUTES.toMillis(mins);
        long secs = TimeUnit.MILLISECONDS.toSeconds(time);
        time -= TimeUnit.SECONDS.toMillis(secs);
        return String.format("%d:%02d:%02d", mins, secs, time);
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEntityEvent e) {
        if (!this.craftActive) {
            return;
        }
        if (plugin.players.get(e.getPlayer().getUniqueId()).currentStation != null && e.getRightClicked() == plugin.players.get(e.getPlayer().getUniqueId()).currentStation.holdingTable.entity) {
            Player player = e.getPlayer();
            MMOPlayer mmoPlayer = plugin.players.get(player.getUniqueId());
            if ((player.getItemInHand() != null && player.getItemInHand().getType() != Material.AIR)) {
                if (!isItemToolForCraft(player.getItemInHand(), mmoPlayer.currentStation.type)) {
                    player.sendMessage(ChatColor.RED + "You can't use that item for this recipe!");
                    player.playSound(player, "entity.enderman.teleport", 1.0f, 0.0f);
                    e.setCancelled(true);
                    return;
                }
            }
            Entity entity = e.getRightClicked();
            if (!mmoPlayer.isCrafting) {
                player.sendMessage(ChatColor.RED + "You can only use that table while crafting!");
                player.playSound(player, "entity.enderman.teleport", 1.0f, 0.0f);
                return;
            }
            if (!mmoPlayer.currentCraft.equalsIgnoreCase("holding")) {
                player.sendMessage(ChatColor.RED + "That's not the correct table!");
                player.playSound(player, "entity.enderman.teleport", 1.0f, 0.5f);
                return;
            }
            float num = 0;
            if (mmoPlayer.currentStation.type == XpType.TAILORING) {
                num = ((mmoPlayer.getFocus() + 1) + ((mmoPlayer.getCritDamage() - mmoPlayer.getBaseCritDamage())/2));
            } else if (mmoPlayer.currentStation.type == XpType.BLACKSMITHING) {
                num = ((mmoPlayer.getFocus() + 1) + ((mmoPlayer.getStrength() - mmoPlayer.getBaseStrength())/2));
            }
            this.currentTaskProgress = this.currentTaskProgress + num;
            this.holdingTableEffects(player, entity.getLocation());

        } /*else if (plugin.players.get(e.getPlayer().getUniqueId()).currentStation != null && e.getRightClicked() == plugin.players.get(e.getPlayer().getUniqueId()).currentStation.timingTable.entity) {
            Player player = e.getPlayer();
            MMOPlayer mmoPlayer = plugin.players.get(player.getUniqueId());
            if ((player.getItemInHand() != null && player.getItemInHand().getType() != Material.AIR)) {
                if (!isItemToolForCraft(player.getItemInHand(), mmoPlayer.currentStation.type)) {
                    player.sendMessage(ChatColor.RED + "You can't use that item for this recipe!");
                    player.playSound(player, "entity.enderman.teleport", 1.0f, 0.0f);
                    e.setCancelled(true);
                    return;
                }
            }
            Entity entity = e.getRightClicked();
            if (!mmoPlayer.isCrafting) {
                player.sendMessage(ChatColor.RED + "You can only use that table while crafting!");
                player.playSound(player, "entity.enderman.teleport", 1.0f, 0.0f);
                return;
            }
            if (!mmoPlayer.currentCraft.equalsIgnoreCase("timing")) {
                player.sendMessage(ChatColor.RED + "That's not the correct table!");
                player.playSound(player, "entity.enderman.teleport", 1.0f, 0.5f);
                return;
            }
            if (this.timingCount[0] == 2) {
                float num = 0;
                if (mmoPlayer.currentStation.type == XpType.TAILORING) {
                    num = ((mmoPlayer.getFocus() + 1) + ((mmoPlayer.getCritDamage() - mmoPlayer.getBaseCritDamage())/2))*5;
                }
                this.currentTaskProgress = this.currentTaskProgress + num;
                this.timingCount[0] = 1;
                timingTableEffects(player, entity.getLocation());
            } else {
                float num = 0;
                if (mmoPlayer.currentStation.type == XpType.TAILORING) {
                    num = (float)(((mmoPlayer.getFocus() + 1) + ((mmoPlayer.getCritDamage() - mmoPlayer.getBaseCritDamage())/2))*2.5);
                }
                if (this.currentTaskProgress - num < 0) {
                    this.currentTaskProgress = 0;
                } else {
                    this.currentTaskProgress = this.currentTaskProgress - num;
                }
                timingTableFailEffects(player, entity.getLocation());
            }

        }*/
    }
    @EventHandler
    public void onPlayerDoDamage(EntityDamageByEntityEvent e) {
        if (!this.craftActive) {
            return;
        }
        if (e.getDamager() instanceof Player) {
            Player player = (Player) e.getDamager();
            if (plugin.players.get(player.getUniqueId()).currentStation != null && e.getEntity() == plugin.players.get(player.getUniqueId()).currentStation.mashingTable.entity) {
                MMOPlayer mmoPlayer = plugin.players.get(player.getUniqueId());
                if ((player.getItemInHand() != null && player.getItemInHand().getType() != Material.AIR)) {
                    if (!isItemToolForCraft(player.getItemInHand(), mmoPlayer.currentStation.type)) {
                        player.sendMessage(ChatColor.RED + "You can't use that item for this recipe!");
                        player.playSound(player, "entity.enderman.teleport", 1.0f, 0.0f);
                        e.setCancelled(true);
                        return;
                    }
                }

                Entity entity = e.getEntity();
                if (!mmoPlayer.isCrafting) {
                    player.sendMessage(ChatColor.RED + "You can only use that table while crafting!");
                    player.playSound(player, "entity.enderman.teleport", 1.0f, 0.0f);
                    return;
                }
                if (!mmoPlayer.currentCraft.equalsIgnoreCase("mashing")) {
                    player.sendMessage(ChatColor.RED + "That's not the correct table!");
                    player.playSound(player, "entity.enderman.teleport", 1.0f, 0.5f);
                    return;
                }
                float num = 0;
                if (mmoPlayer.currentStation.type == XpType.TAILORING) {
                    num = ((mmoPlayer.getFocus() + 1) + ((mmoPlayer.getCritDamage() - mmoPlayer.getBaseCritDamage())/2));
                } else if (mmoPlayer.currentStation.type == XpType.BLACKSMITHING) {
                    num = ((mmoPlayer.getFocus() + 1) + ((mmoPlayer.getStrength() - mmoPlayer.getBaseStrength())/2));
                }
                this.currentTaskProgress = this.currentTaskProgress + num;
                mashingTableEffects(player, entity.getLocation());
            } else if (plugin.players.get(player.getUniqueId()).currentStation != null && e.getEntity() == plugin.players.get(player.getUniqueId()).currentStation.timingTable.entity) {

                MMOPlayer mmoPlayer = plugin.players.get(player.getUniqueId());

                Entity entity = e.getEntity();
                if (!mmoPlayer.isCrafting) {
                    player.sendMessage(ChatColor.RED + "You can only use that table while crafting!");
                    player.playSound(player, "entity.enderman.teleport", 1.0f, 0.0f);
                    return;
                }
                if (!mmoPlayer.currentCraft.equalsIgnoreCase("timing")) {
                    player.sendMessage(ChatColor.RED + "That's not the correct table!");
                    player.playSound(player, "entity.enderman.teleport", 1.0f, 0.5f);
                    return;
                }
                if ((player.getItemInHand() != null && player.getItemInHand().getType() != Material.AIR)) {
                    if (!isItemToolForCraft(player.getItemInHand(), mmoPlayer.currentStation.type)) {
                        player.sendMessage(ChatColor.RED + "You can't use that item for this recipe!");
                        player.playSound(player, "entity.enderman.teleport", 1.0f, 0.0f);
                        e.setCancelled(true);
                        return;
                    }
                }
                float num = 0;
                if (this.timingCount[0] == 2) {
                    if (mmoPlayer.currentStation.type == XpType.TAILORING) {
                        num = ((mmoPlayer.getFocus() + 1) + ((mmoPlayer.getCritDamage() - mmoPlayer.getBaseCritDamage())/2))*5;
                    } else if (mmoPlayer.currentStation.type == XpType.BLACKSMITHING) {
                        num = ((mmoPlayer.getFocus() + 1) + ((mmoPlayer.getStrength() - mmoPlayer.getBaseStrength())/2))*5;
                    }
                    this.currentTaskProgress = this.currentTaskProgress + num;
                    this.timingCount[0] = 1;
                    timingTableEffects(player, entity.getLocation());
                } else {
                    if (mmoPlayer.currentStation.type == XpType.TAILORING) {
                        num = (float) (((mmoPlayer.getFocus() + 1) + ((mmoPlayer.getCritDamage() - mmoPlayer.getBaseCritDamage())/2))*2.5);
                    } else if (mmoPlayer.currentStation.type == XpType.BLACKSMITHING) {
                        num = (float) (((mmoPlayer.getFocus() + 1) + ((mmoPlayer.getStrength() - mmoPlayer.getBaseStrength())/2))*2.5);
                    }
                    if (this.currentTaskProgress - num < 0) {
                        this.currentTaskProgress = 0;
                    } else {
                        this.currentTaskProgress = this.currentTaskProgress - num;
                    }
                    timingTableFailEffects(player, entity.getLocation());
                }
                e.setCancelled(true);

            }
        }
    }
    public void timingTableEffects(Player player, Location location) {
        List<Player> targetP = new ArrayList<>();
        targetP.add(player);
        MMOPlayer mmoPlayer = plugin.players.get(player.getUniqueId());
        if (mmoPlayer.currentStation.type == XpType.TAILORING) {
            ParticleOptions po = new ParticleOptions((float) 0.2, (float) 0.2, (float) 0.2, 0.2F,8, 1F, (Color) null,null, (byte) 0);
            MagicSpells.getEffectManager().display(Particle.CRIT, po, location.add(0f,0.75f,0f), 32.0D, targetP);
            player.playSound(location, "entity.arrow.shoot", 1.0f, 2.0f);
        } else if (mmoPlayer.currentStation.type == XpType.BLACKSMITHING) {
            ParticleOptions po = new ParticleOptions((float) 0.2, (float) 0.2, (float) 0.2, 0.0001F,8, 1F, (Color) null,null, (byte) 0);
            MagicSpells.getEffectManager().display(Particle.FLAME, po, location.add(0f,0.75f,0f), 32.0D, targetP);
            player.playSound(location, "entity.blaze.shoot", 1.0f, 1.25f);
        }
    }
    public void holdingTableEffects(Player player, Location location) {
        List<Player> targetP = new ArrayList<>();
        targetP.add(player);
        MMOPlayer mmoPlayer = plugin.players.get(player.getUniqueId());
        if (mmoPlayer.currentStation.type == XpType.TAILORING) {
            String sound = "";
            int num = r.nextInt(2);
            if (num == 0) {
                sound = "block.piston.contract";
            } else {
                sound = "block.piston.extend";
            }
            ParticleOptions po = new ParticleOptions((float) 0.2, (float) 0.2, (float) 0.2, 0.03F,8, 1F, (Color) null,null, (byte) 0);
            MagicSpells.getEffectManager().display(Particle.CRIT_MAGIC, po, location.add(0f, 0.75f, 0f), 32.0D, targetP);
            player.playSound(location, sound, 1.0f, 2.0f);
        } else if (mmoPlayer.currentStation.type == XpType.BLACKSMITHING) {
            Direction direction = mmoPlayer.currentStation.holdingTable.dir;
            Location newLocation = location;
            if (direction == Direction.S) {
                newLocation.add(0,0.5,-0.9);
            } else if (direction == Direction.N) {
                newLocation.add(0,0.5,0.9);
            } else if (direction == Direction.W) {
                newLocation.add(-0.9,0.5,0);
            } else {
                newLocation.add(0.9,0.5,0);
            }
            if (firstClick) {
                player.playSound(location, "entity.generic.splash", 2f, 0.75f);
                ParticleOptions po = new ParticleOptions((float) 0.2, (float) 0.2, (float) 0.2, 0.4F,25, 1F, (Color) null,null, (byte) 0);
                MagicSpells.getEffectManager().display(Particle.WATER_SPLASH, po, newLocation, 32.0D, targetP);
                firstClick = false;
            } else {
                player.playSound(location, "block.fire.extinguish", 0.8f, 1.5f);
                ParticleOptions po = new ParticleOptions((float) 0.2, (float) 0.2, (float) 0.2, 0.001F,5, 1F, (Color) null,null, (byte) 0);
                MagicSpells.getEffectManager().display(Particle.SMOKE_NORMAL, po, newLocation, 32.0D, targetP);
            }
        }
    }
    public void timingTableFailEffects(Player player, Location location) {
        List<Player> targetP = new ArrayList<>();
        targetP.add(player);
        MMOPlayer mmoPlayer = plugin.players.get(player.getUniqueId());
        if (mmoPlayer.currentStation.type == XpType.TAILORING) {
            ParticleOptions po = new ParticleOptions((float) 0.2, (float) 0.2, (float) 0.2, 0.05F,8, 1F, (Color) null,null, (byte) 0);
            MagicSpells.getEffectManager().display(Particle.SMOKE_NORMAL, po, location.add(0f,0.75f,0f), 32.0D, targetP);
            player.playSound(location, "entity.bat.hurt", 1.0f, 0.5f);
        } else if (mmoPlayer.currentStation.type == XpType.BLACKSMITHING) {
            ParticleOptions po = new ParticleOptions((float) 0.2, (float) 0.2, (float) 0.2, 0.05F,8, 1F, (Color) null,Material.IRON_SWORD, (byte) 0);
            MagicSpells.getEffectManager().display(Particle.BLOCK_CRACK, po, location.add(0f,0.75f,0f), 32.0D, targetP);
            player.playSound(location, "entity.bat.hurt", 1.0f, 0.5f);
            player.playSound(location, "item.shield.break", 1.0f, 1.0f);
        }
    }
    public void mashingTableEffects(Player player, Location location) {
        List<Player> targetP = new ArrayList<>();
        targetP.add(player);
        MMOPlayer mmoPlayer = plugin.players.get(player.getUniqueId());
        if (mmoPlayer.currentStation.type == XpType.TAILORING) {
            Material mat;
            int rInt = r.nextInt(12);
            if (rInt == 0) {
                mat = Material.WHITE_WOOL;
            } else if (rInt == 1) {
                mat = Material.ORANGE_WOOL;
            } else if (rInt == 2) {
                mat = Material.MAGENTA_WOOL;
            } else if (rInt == 3) {
                mat = Material.LIGHT_BLUE_WOOL;
            } else if (rInt == 4) {
                mat = Material.YELLOW_WOOL;
            } else if (rInt == 5) {
                mat = Material.LIME_WOOL;
            }else if (rInt == 6) {
                mat = Material.PINK_WOOL;
            } else if (rInt == 7) {
                mat = Material.CYAN_WOOL;
            } else if (rInt == 8) {
                mat = Material.PURPLE_WOOL;
            }else if (rInt == 9) {
                mat = Material.BLUE_WOOL;
            }else if (rInt == 10) {
                mat = Material.GREEN_WOOL;
            }else {
                mat = Material.RED_WOOL;
            }
            ParticleOptions po = new ParticleOptions((float) 0.2, (float) 0.2, (float) 0.2, 0.2F,8, 1F, (Color) null,mat, (byte) 0);
            MagicSpells.getEffectManager().display(Particle.BLOCK_CRACK, po, location.add(0f,0.75f,0f), 32.0D, targetP);
            player.playSound(location, "entity.sheep.shear", 1.0f, 1.0f);
        } else if (mmoPlayer.currentStation.type == XpType.BLACKSMITHING) {
            ParticleOptions po = new ParticleOptions((float) 0.2, (float) 0.2, (float) 0.2, 0.1F,1, 1F, (Color) null,null, (byte) 0);
            MagicSpells.getEffectManager().display(Particle.LAVA, po, location.add(0f,0.75f,0f), 32.0D, targetP);
            player.playSound(location, "block.anvil.place", 0.8f, 0.8f);
        }
    }

    public void finishCraft(Player player, Recipe recipe, long time) {
        MMOPlayer mmoPlayer = plugin.players.get(player.getUniqueId());
        time = (time/20)*1000;
        player.teleport(mmoPlayer.craftStart);
        if (this.timingRunnable != null && !this.timingRunnable.isCancelled()) {
            this.timingRunnable.cancel();
        }
        String endString = ChatColor.GREEN + ChatColor.BOLD.toString() + "CRAFT COMPLETED!" + ChatColor.RESET + ChatColor.GRAY + " You successfully crafted ";
        player.playSound(player, "entity.player.levelup", 1.0f, 0.75f);
        if (recipe.goldThreshold * 1000 >= time) {
            rewards(player, recipe, 1.15f, "superb");
            endString += "a " + ChatColor.GOLD + "Superb " + ChatColor.GRAY + "Quality " +  recipe.displayName;
        } else if (recipe.silverThreshold * 1000 >= time) {
            rewards(player, recipe, 1.1f, "great");
            endString += "a " + ChatColor.WHITE + "Great " + ChatColor.GRAY + "Quality " +  recipe.displayName;
        } else if (recipe.bronzeThreshold * 1000 >= time) {
            rewards(player, recipe, 1.05f, "good");
            endString += "a " + ChatColor.RED + "Good " + ChatColor.GRAY + "Quality " +  recipe.displayName;
        } else {
            this.rewards(player, recipe, 1.00f, "normal");
            endString += recipe.displayName;
        }
        endString +=  ChatColor.GRAY + " in " + ChatColor.YELLOW + formatTime(time) + ChatColor.GRAY + ".";
        player.sendMessage(endString);
        mmoPlayer.currentCraft = "";
        mmoPlayer.isCrafting = false;
        mmoPlayer.currentStation = null;
        mmoPlayer.currentCraftObj = null;
        this.as.remove();
        this.craftActive = false;
    }

    public void rewards(Player player, Recipe recipe, float statBoost, String quality) {
        if (quality.equalsIgnoreCase("normal")) {
            player.getInventory().addItem(plugin.itemHandler.getItem(recipe.reward).asQuantity(1));
        } else if (quality.equalsIgnoreCase("good")) {
            ItemStack item = plugin.itemHandler.getItem(recipe.reward);
            updateStatsFromItem(item, statBoost);
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();
            lore.add(0, ChatColor.GRAY + "Quality: " + ChatColor.RED + "✯" );
            meta.setLore(lore);
            item.setItemMeta(meta);
            NBTItem nbtI = new NBTItem(item);
            Objects.requireNonNull(nbtI.getCompound("ExtraAttributes")).setFloat("statBoost", statBoost);
            item = nbtI.getItem();
            player.getInventory().addItem(item);
        } else if (quality.equalsIgnoreCase("great")) {
            ItemStack item = plugin.itemHandler.getItem(recipe.reward);
            updateStatsFromItem(item, statBoost);
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();
            lore.add(0, ChatColor.GRAY + "Quality: " + ChatColor.WHITE + "✯✯" );
            meta.setLore(lore);
            item.setItemMeta(meta);
            NBTItem nbtI = new NBTItem(item);
            Objects.requireNonNull(nbtI.getCompound("ExtraAttributes")).setFloat("statBoost", statBoost);
            item = nbtI.getItem();
            player.getInventory().addItem(item);
        } else if (quality.equalsIgnoreCase("superb")) {
            ItemStack item = plugin.itemHandler.getItem(recipe.reward);
            updateStatsFromItem(item, statBoost);
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();
            lore.add(0, ChatColor.GRAY + "Quality: " + ChatColor.GOLD + "✯✯✯" );
            meta.setLore(lore);
            item.setItemMeta(meta);
            NBTItem nbtI = new NBTItem(item);
            Objects.requireNonNull(nbtI.getCompound("ExtraAttributes")).setFloat("statBoost", statBoost);
            item = nbtI.getItem();
            player.getInventory().addItem(item);
        }

    }

    public static void updateStatsFromItem(ItemStack item, float multiplier) {
        float change;
        if (!item.hasItemMeta() || !item.getItemMeta().hasLore()) {
            return;
        }
        List<String> lore = item.getItemMeta().getLore();
        for (int i = 0; i < lore.size(); i++) {
            String line = ChatColor.stripColor(lore.get(i));
            if (line.startsWith("Damage: +")) {
                line = line.replaceFirst("(Damage: +\\+)", "");
                change = Float.parseFloat(line);
                change = change * multiplier;
                lore.set(i, ChatColor.GRAY + "Damage: " + ChatColor.GREEN + "+" + String.format("%.1f", change));
            } else if (line.startsWith("Damage: -")) {
                line = line.replaceFirst("(Damage: +\\-)", "");
                change = -Float.parseFloat(line);
                change = change - (change * (multiplier-1));
                lore.set(i, ChatColor.GRAY + "Damage: " + ChatColor.RED + "-" + String.format("%.1f", change));
            } else if (line.startsWith("Mining Damage: +")) {
                line = line.replaceFirst("(Mining Damage: +\\+)", "");
                change = Float.parseFloat(line);
                change = change * multiplier;
                lore.set(i, ChatColor.GRAY + "Mining Damage: " + ChatColor.GREEN + "+" + String.format("%.1f", change));
            } else if (line.startsWith("Mining Damage: -")) {
                line = line.replaceFirst("(Mining Damage: +\\-)", "");
                change = -Float.parseFloat(line);
                change = change - (change * (multiplier-1));
                lore.set(i, ChatColor.GRAY + "Mining Damage: " + ChatColor.RED + "-" + String.format("%.1f", change));
            } else if (line.startsWith("Chopping Damage: +")) {
                line = line.replaceFirst("(Chopping Damage: +\\+)", "");
                change = Float.parseFloat(line);
                change = change * multiplier;
                lore.set(i, ChatColor.GRAY + "Chopping Damage: " + ChatColor.GREEN + "+" + String.format("%.1f", change));
            } else if (line.startsWith("Chopping Damage: -")) {
                line = line.replaceFirst("(Chopping Damage: +\\-)", "");
                change = -Float.parseFloat(line);
                change = change - (change * (multiplier-1));
                lore.set(i, ChatColor.GRAY + "Chopping Damage: " + ChatColor.RED + "-" + String.format("%.1f", change));
            } else if (line.startsWith("Strength: +")) {
                line = line.replaceFirst("(Strength: +\\+)", "");
                change = Float.parseFloat(line);
                change = change * multiplier;
                lore.set(i, ChatColor.GRAY + "Strength: " + ChatColor.GREEN + "+" + String.format("%.1f", change));
            } else if (line.startsWith("Strength: -")) {
                line = line.replaceFirst("(Strength: +\\-)", "");
                change = -Float.parseFloat(line);
                change = change - (change * (multiplier-1));
                lore.set(i, ChatColor.GRAY + "Strength: " + ChatColor.RED + "-" + String.format("%.1f", change));
            } else if (line.startsWith("Crit Damage: +")) {
                line = line.replaceFirst("(Crit Damage: +\\+)", "");
                line = line.replaceFirst("(%)", "");
                change = Float.parseFloat(line);
                change = change * multiplier;
                lore.set(i, ChatColor.GRAY + "Crit Damage: " + ChatColor.GREEN + "+" + String.format("%.1f", change) + "%");
            } else if (line.startsWith("Crit Damage: -")) {
                line = line.replaceFirst("(Crit Damage: +\\-)", "");
                line = line.replaceFirst("(%)", "");
                change = -Float.parseFloat(line);
                change = change - (change * (multiplier-1));
                lore.set(i, ChatColor.GRAY + "Crit Damage: " + ChatColor.RED + "-" + String.format("%.1f", change) + "%");
            } else if (line.startsWith("Speed: +")) {
                line = line.replaceFirst("(Speed: +\\+)", "");
                change = Float.parseFloat(line);
                change = change * multiplier;
                lore.set(i, ChatColor.GRAY + "Speed: " + ChatColor.GREEN + "+" + String.format("%.1f", change));
            } else if (line.startsWith("Speed: -")) {
                line = line.replaceFirst("(Speed: +\\-)", "");
                change = -Float.parseFloat(line);
                change = change - (change * (multiplier-1));
                lore.set(i, ChatColor.GRAY + "Speed: " + ChatColor.RED + "-" + String.format("%.1f", change));
            } else if (line.startsWith("Crit Chance: +")) {
                line = line.replaceFirst("(Crit Chance: +\\+)", "");
                line = line.replaceFirst("(%)", "");
                change = Float.parseFloat(line);
                change = change * multiplier;
                lore.set(i, ChatColor.GRAY + "Crit Chance: " + ChatColor.GREEN + "+" + String.format("%.1f", change) + "%");
            } else if (line.startsWith("Crit Chance: -")) {
                line = line.replaceFirst("(Crit Chance: +\\-)", "");
                line = line.replaceFirst("(%)", "");
                change = -Float.parseFloat(line);
                change = change - (change * (multiplier-1));
                lore.set(i, ChatColor.GRAY + "Crit Chance: " + ChatColor.RED + "-" + String.format("%.1f", change) + "%");
            } else if (line.startsWith("Health: +")) {
                line = line.replaceFirst("(Health: +\\+)", "");
                change = Float.parseFloat(line);
                change = change * multiplier;
                lore.set(i, ChatColor.GRAY + "Health: " + ChatColor.GREEN + "+" + String.format("%.1f", change));
            } else if (line.startsWith("Health: -")) {
                line = line.replaceFirst("(Health: +\\-)", "");
                change = -Float.parseFloat(line);
                change = change - (change * (multiplier-1));
                lore.set(i, ChatColor.GRAY + "Health: " + ChatColor.RED + "-" + String.format("%.1f", change));
            } else if (line.startsWith("Defense: +")) {
                line = line.replaceFirst("(Defense: +\\+)", "");
                change = Float.parseFloat(line);
                change = change * multiplier;
                lore.set(i, ChatColor.GRAY + "Defense: " + ChatColor.GREEN + "+" + String.format("%.1f", change));
            } else if (line.startsWith("Defense: -")) {
                line = line.replaceFirst("(Defense: +\\-)", "");
                change = -Float.parseFloat(line);
                change = change - (change * (multiplier-1));
                lore.set(i, ChatColor.GRAY + "Defense: " + ChatColor.RED + "-" + String.format("%.1f", change));
            } else if (line.startsWith("Stamina: +")) {
                line = line.replaceFirst("(Stamina: +\\+)", "");
                change = Float.parseFloat(line);
                change = change * multiplier;
                lore.set(i, ChatColor.GRAY + "Stamina: " + ChatColor.GREEN + "+" + String.format("%.1f", change));
            } else if (line.startsWith("Stamina: -")) {
                line = line.replaceFirst("(Stamina: +\\-)", "");
                change = -Float.parseFloat(line);
                change = change - (change * (multiplier-1));
                lore.set(i, ChatColor.GRAY + "Stamina: " + ChatColor.RED + "-" + String.format("%.1f", change));
            } else if (line.startsWith("Ability Damage: +")) {
                line = line.replaceFirst("(Ability Damage: +\\+)", "");
                line = line.replaceFirst("(%)", "");
                change = Float.parseFloat(line);
                change = change * multiplier;
                lore.set(i, ChatColor.GRAY + "Ability Damage: " + ChatColor.GREEN + "+" + String.format("%.1f", change) + "%");
            } else if (line.startsWith("Ability Damage: -")) {
                line = line.replaceFirst("(Ability Damage: +\\-)", "");
                line = line.replaceFirst("(%)", "");
                change = -Float.parseFloat(line);
                change = change - (change * (multiplier-1));
                lore.set(i, ChatColor.GRAY + "Ability Damage: " + ChatColor.RED + "-" + String.format("%.1f", change) + "%");
            } else if (line.startsWith("Shop Discount: +")) {
                line = line.replaceFirst("(Shop Discount: +\\+)", "");
                line = line.replaceFirst("(%)", "");
                change = Float.parseFloat(line);
                change = change * multiplier;
                lore.set(i, ChatColor.GRAY + "Shop Discount: " + ChatColor.GREEN + "+" + String.format("%.1f", change) + "%");
            } else if (line.startsWith("Shop Discount: -")) {
                line = line.replaceFirst("(Shop Discount: +\\-)", "");
                line = line.replaceFirst("(%)", "");
                change = -Float.parseFloat(line);
                change = change - (change * (multiplier-1));
                lore.set(i, ChatColor.GRAY + "Shop Discount: " + ChatColor.RED + "-" + String.format("%.1f", change) + "%");
            }else if (line.startsWith("Dialogue Speed: +")) {
                line = line.replaceFirst("(Dialogue Speed: +\\+)", "");
                change = Float.parseFloat(line);
                change = change * multiplier;
                lore.set(i, ChatColor.GRAY + "Dialogue Speed: " + ChatColor.GREEN + "+" + String.format("%.1f", change));
            } else if (line.startsWith("Dialogue Speed: -")) {
                line = line.replaceFirst("(Dialogue Speed: +\\-)", "");
                change = -Float.parseFloat(line);
                change = change - (change * (multiplier-1));
                lore.set(i, ChatColor.GRAY + "Dialogue Speed: " + ChatColor.RED + "-" + String.format("%.1f", change));
            } else if (line.startsWith("Health Regen: +")) {
                line = line.replaceFirst("(Health Regen: +\\+)", "");
                change = Float.parseFloat(line);
                change = change * multiplier;
                lore.set(i, ChatColor.GRAY + "Health Regen: " + ChatColor.GREEN + "+" + String.format("%.1f", change));
            } else if (line.startsWith("Health Regen: -")) {
                line = line.replaceFirst("(Health Regen: +\\-)", "");
                change = -Float.parseFloat(line);
                change = change - (change * (multiplier-1));
                lore.set(i, ChatColor.GRAY + "Health Regen: " + ChatColor.RED + "-" + String.format("%.1f", change));
            } else if (line.startsWith("Stamina Regen: +")) {
                line = line.replaceFirst("(Stamina Regen: +\\+)", "");
                change = Float.parseFloat(line);
                change = change * multiplier;
                lore.set(i, ChatColor.GRAY + "Stamina Regen: " + ChatColor.GREEN + "+" + String.format("%.1f", change));
            } else if (line.startsWith("Stamina Regen: -")) {
                line = line.replaceFirst("(Stamina Regen: +\\-)", "");
                change = -Float.parseFloat(line);
                change = change - (change * (multiplier-1));
                lore.set(i, ChatColor.GRAY + "Stamina Regen: " + ChatColor.RED + "-" + String.format("%.1f", change));
            } else if (line.startsWith("Fishing Speed: +")) {
                line = line.replaceFirst("(Fishing Speed: +\\+)", "");
                change = Float.parseFloat(line);
                change = change * multiplier;
                lore.set(i, ChatColor.GRAY + "Fishing Speed: " + ChatColor.GREEN + "+" + String.format("%.1f", change));
            } else if (line.startsWith("Fishing Speed: -")) {
                line = line.replaceFirst("(Fishing Speed: +\\-)", "");
                change = -Float.parseFloat(line);
                change = change - (change * (multiplier-1));
                lore.set(i, ChatColor.GRAY + "Fishing Speed: " + ChatColor.RED + "-" + String.format("%.1f", change));
            } else if (line.startsWith("Lure: +")) {
                line = line.replaceFirst("(Lure: +\\+)", "");
                change = Float.parseFloat(line);
                change = change * multiplier;
                lore.set(i, ChatColor.GRAY + "Lure: " + ChatColor.GREEN + "+" + String.format("%.1f", change));
            } else if (line.startsWith("Lure: -")) {
                line = line.replaceFirst("(Lure: +\\-)", "");
                change = -Float.parseFloat(line);
                change = change - (change * (multiplier-1));
                lore.set(i, ChatColor.GRAY + "Lure: " + ChatColor.RED + "-" + String.format("%.1f", change));
            } else if (line.startsWith("Focus: +")) {
                line = line.replaceFirst("(Focus: +\\+)", "");
                change = Float.parseFloat(line);
                change = change * multiplier;
                lore.set(i, ChatColor.GRAY + "Focus: " + ChatColor.GREEN + "+" + String.format("%.1f", change));
            } else if (line.startsWith("Focus: -")) {
                line = line.replaceFirst("(Focus: +\\-)", "");
                change = -Float.parseFloat(line);
                change = change - (change * (multiplier-1));
                lore.set(i, ChatColor.GRAY + "Focus: " + ChatColor.RED + "-" + String.format("%.1f", change));
            } else if (line.startsWith("Vigor: +")) {
                line = line.replaceFirst("(Vigor: +\\+)", "");
                change = Float.parseFloat(line);
                change = change * multiplier;
                lore.set(i, ChatColor.GRAY + "Vigor: " + ChatColor.GREEN + "+" + String.format("%.1f", change));
            } else if (line.startsWith("Vigor: -")) {
                line = line.replaceFirst("(Vigor: +\\-)", "");
                change = -Float.parseFloat(line);
                change = change - (change * (multiplier-1));
                lore.set(i, ChatColor.GRAY + "Vigor: " + ChatColor.RED + "-" + String.format("%.1f", change));
            } else if (line.startsWith("Sea Creature Chance: +")) {
                line = line.replaceFirst("(Sea Creature Chance: +\\+)", "");
                change = Float.parseFloat(line);
                change = change * multiplier;
                lore.set(i, ChatColor.GRAY + "Sea Creature Chance: " + ChatColor.GREEN + "+" + String.format("%.1f", change));
            } else if (line.startsWith("Vigor: -")) {
                line = line.replaceFirst("(Sea Creature Chance: +\\-)", "");
                change = -Float.parseFloat(line);
                change = change - (change * (multiplier-1));
                lore.set(i, ChatColor.GRAY + "Sea Creature Chance: " + ChatColor.RED + "-" + String.format("%.1f", change));
            }
        }
        ItemMeta meta = item.getItemMeta();
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    public boolean isItemToolForCraft(ItemStack item, XpType type) {
        if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
            List<String> lore = item.getItemMeta().getLore();
            String line = lore.get(lore.size()-1);
            String check = "";
            if (type == XpType.TAILORING) {
                check = " SCISSORS";
            } else if (type == XpType.BLACKSMITHING) {
                check = " HAMMER";
            } else if (type == XpType.COOKING) {
                check = " PAN";
            } else if (type == XpType.ALCHEMY) {
                check = " SPOON";
            } else if (type == XpType.CARPENTRY) {
                check = " SAW";
            }
            return ChatColor.stripColor(line).contains(check);
        }
        return false;
    }
}
