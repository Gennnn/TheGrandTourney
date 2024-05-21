package me.genn.thegrandtourney.listener;

import java.util.*;

import com.destroystokyo.paper.event.player.PlayerRecipeBookClickEvent;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.events.*;
import de.tr7zw.nbtapi.NBTItem;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import io.lumine.mythic.bukkit.events.MythicMobSpawnEvent;
import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.conditions.*;
import me.genn.thegrandtourney.dungeons.Room;
import me.genn.thegrandtourney.dungeons.RoomGoal;
import me.genn.thegrandtourney.effects.BetterArmorStandEffect;
import me.genn.thegrandtourney.item.MMOItem;
import me.genn.thegrandtourney.mobs.MMOMob;
import me.genn.thegrandtourney.npc.SlayerQuest;
import me.genn.thegrandtourney.player.*;
import me.genn.thegrandtourney.skills.fishing.Fish;
import me.genn.thegrandtourney.skills.fishing.FishingZoneTemplate;
import me.genn.thegrandtourney.skills.mining.Ore;
import me.genn.thegrandtourney.tournament.MiniGame;
import me.genn.thegrandtourney.util.IntMap;
import me.genn.thegrandtourney.util.TabList;
import org.bukkit.*;

import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;


import ch.twidev.spectraldamage.api.SpectralDamage;
import de.tr7zw.nbtapi.NBTCompound;


import net.citizensnpcs.api.npc.NPC;


public class EventListener implements Listener {
    TGT plugin;
    Random r;
    public SpectralDamage spectralDamage;
    public Map<Entity, MMOMob> mobs;
    public Map<Entity, Ore> ores;
    public Map<MMOMob, IntMap<Player>> slayerTracker;
    public Map<UUID, Map<EquipmentSlot, Long>> cantUseMsgCd;
    Map<Player, Fish> queuedFish;
    public HashMap<Entity, ArrowDamage> arrowsAndDamage = new HashMap<>();

    public EventListener(TGT plugin) {
        this.plugin = plugin;
        this.r = new Random();
        this.spectralDamage = SpectralDamage.getInstance();
        this.mobs = new HashMap<>();
        this.ores = new HashMap<>();
        this.slayerTracker = new HashMap<>();
        this.cantUseMsgCd = new HashMap<>();
        this.queuedFish = new HashMap<>();
    }


    @EventHandler
    public void registerMMOPlayer(PlayerJoinEvent e) {

        if (!plugin.players.keySet().contains(e.getPlayer().getUniqueId())) {
            plugin.players.put(e.getPlayer().getUniqueId(), plugin.createNewPlayer(e.getPlayer()));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        if (plugin.players.containsKey(e.getPlayer().getUniqueId())) {
            plugin.players.get(e.getPlayer().getUniqueId()).buffs.clear();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTakeDam(EntityDamageEvent e) {
        double damage = e.getDamage();

        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (plugin.players.containsKey(p.getUniqueId())) {
                MMOPlayer mmoPlayer = plugin.players.get(p.getUniqueId());
                if (mmoPlayer.getEvasiveness() > 0.0) {
                    if (r.nextDouble() < (mmoPlayer.getEvasiveness()/(mmoPlayer.getEvasiveness()+100))) {
                        e.setCancelled(true);
                        Location target = p.getLocation();
                        target.add(1.35 * r.nextDouble() * (1 - (r.nextDouble()*2)), 0.8 * r.nextDouble(), 1.35 * r.nextDouble() * (1 - (r.nextDouble()*2)));
                        Entity damageIndicator = this.spectralDamage.spawnDamageIndicator(target, new DodgeIndicator(), 1, true);
                        new BukkitRunnable() {

                            @Override
                            public void run() {
                                damageIndicator.remove();
                            }
                        }.runTaskLater(plugin, 30L);
                        e.setDamage(0.0D);
                        return;
                    }
                }
                damage = plugin.calculateDefenseDamage((float) plugin.players.get(p.getUniqueId()).getDefense(), (float) damage);
                e.setDamage(0.0D);
                plugin.players.get(p.getUniqueId()).takeDamage((float)damage);

            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        if (!plugin.tournament) {
            if (plugin.players.keySet().contains(p.getUniqueId())) {
                plugin.updatePlayerHealth(plugin.players.get(p.getUniqueId()), plugin.players.get(p.getUniqueId()).getMaxHealth());
            }
        }

    }
    @EventHandler
    public void onInventoryPickupItem(PlayerAttemptPickupItemEvent event) {
        if (event.isCancelled()){
            return;
        }
        Player p = event.getPlayer();
        if (plugin.players.keySet().contains(p.getUniqueId())) {
            plugin.statUpdates.updateFullInventory(p, plugin.players.get(p.getUniqueId()));

        }
    }
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player p = (Player) event.getWhoClicked();
            if (plugin.players.keySet().contains(p.getUniqueId())) {
                plugin.statUpdates.updateFullInventory(p, plugin.players.get(p.getUniqueId()));

            }
        }

    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player p = (Player) event.getWhoClicked();
            if (plugin.players.keySet().contains(p.getUniqueId())) {
                plugin.statUpdates.updateFullInventory(p, plugin.players.get(p.getUniqueId()));

            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player p = event.getPlayer();
        if (plugin.players.keySet().contains(p.getUniqueId())) {
            plugin.statUpdates.updateFullInventory(p, plugin.players.get(p.getUniqueId()));

        }
    }
    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player p = event.getPlayer();
        if (plugin.players.keySet().contains(p.getUniqueId())) {
            plugin.statUpdates.updateFullInventory(p, plugin.players.get(p.getUniqueId()));

        }
    }
    public boolean checkWieldingItemForLvlRequirements(Player player) {
        MMOPlayer mmoPlayer = plugin.players.get(player.getUniqueId());
        ItemStack item = player.getItemInHand();
        if (item != null && item.hasItemMeta()) {
            NBTItem nbtI = new NBTItem(item);
            if (nbtI.hasTag("ExtraAttributes")) {
                NBTCompound comp = nbtI.getCompound("ExtraAttributes");
                if (comp.hasTag("id")) {
                    String id = nbtI.getCompound("ExtraAttributes").getString("id");
                    MMOItem mmoItem = plugin.itemHandler.getMMOItemFromString(id);
                    if (mmoItem != null && mmoItem.typeRequirement != null) {
                        return mmoPlayer.getLvlForType(mmoItem.typeRequirement) >= mmoItem.lvlRequirement;
                    }
                }
            }
        }
        return true;
    }
    public boolean checkItemForRequirements(Player player, ItemStack item) {
        MMOPlayer mmoPlayer = plugin.players.get(player.getUniqueId());
        if (item != null && item.hasItemMeta()) {
            NBTItem nbtI = new NBTItem(item);
            if (nbtI.hasTag("ExtraAttributes")) {
                NBTCompound comp = nbtI.getCompound("ExtraAttributes");
                if (comp.hasTag("id")) {
                    String id = nbtI.getCompound("ExtraAttributes").getString("id");
                    MMOItem mmoItem = plugin.itemHandler.getMMOItemFromString(id);
                    if (mmoItem != null && mmoItem.typeRequirement != null) {
                        return mmoPlayer.getLvlForType(mmoItem.typeRequirement) >= mmoItem.lvlRequirement;
                    }
                }
            }
        }
        return true;
    }
    private boolean isBow(ItemStack item) {
        if (item != null && item.hasItemMeta()) {
            NBTItem nbtItem = new NBTItem(item);
            if (nbtItem.hasTag("ExtraAttributes")) {
                NBTCompound comp = nbtItem.getCompound("ExtraAttributes");
                if (comp.hasTag("id")) {
                    String id = comp.getString("id");
                    MMOItem mmoItem = plugin.itemHandler.getMMOItemFromString(id);
                    return mmoItem.categoryString != null && (mmoItem.categoryString.equalsIgnoreCase("bow") || mmoItem.categoryString.equalsIgnoreCase("shortbow") || mmoItem.categoryString.equalsIgnoreCase("longbow") || mmoItem.categoryString.equalsIgnoreCase("greatbow"));
                }
            }
        }
        return false;
    }
    @EventHandler
    public void onBowShoot(EntityShootBowEvent e) {
        if (e.getEntity() instanceof Player && e.getProjectile() instanceof Projectile && isBow(e.getBow())) {
            Player player = (Player)e.getEntity();
            MMOPlayer mmoPlayer = plugin.players.get(player.getUniqueId());
            boolean crit = (mmoPlayer.getCritChance() * (1+(mmoPlayer.getLuck()/100))) >= (r.nextFloat()*100);
            double damage = plugin.calculateBowDamage(mmoPlayer, e.getBow(), crit, e.getConsumable());
            this.arrowsAndDamage.put(e.getProjectile(), new ArrowDamage(damage, crit));
        }
    }
    private boolean isShortBow(ItemStack item) {
        if (item != null && item.hasItemMeta()) {
            NBTItem nbtItem = new NBTItem(item);
            if (nbtItem.hasTag("ExtraAttributes")) {
                NBTCompound comp = nbtItem.getCompound("ExtraAttributes");
                if (comp.hasTag("id")) {
                    String id = comp.getString("id");
                    MMOItem mmoItem = plugin.itemHandler.getMMOItemFromString(id);
                    return mmoItem.categoryString != null && mmoItem.categoryString.equalsIgnoreCase("shortbow");
                }
            }
        }
        return false;
    }
    @EventHandler
    public void onPlayerDoDamage(EntityDamageByEntityEvent e) {
        if (this.ores.containsKey(e.getEntity())) {
            return;
        }
        if (! (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) && ! (e.getCause() == EntityDamageEvent.DamageCause.PROJECTILE)) {
            return;
        }
        if (e.getDamager() instanceof Player) {
            Player p = (Player) e.getDamager();
            if (p.getAttackCooldown() != 1.0) {
                e.setCancelled(true);
                return;
            }
            if (!checkWieldingItemForLvlRequirements(p)) {
                p.sendMessage(ChatColor.RED + "You don't have the necessary level to use this item!");
                e.setCancelled(true);
                return;
            }
            int defense = 0;
            if (this.mobs.keySet().contains(e.getEntity())) {

                defense = (int)this.mobs.get(e.getEntity()).defense;
                if (plugin.players.keySet().contains(p.getUniqueId())) {
                    MMOPlayer player = plugin.players.get(p.getUniqueId());
                    Location target = e.getEntity().getLocation();
                    if (e.getEntity() instanceof NPC) {
                        return;
                    }
                    target.add(1.35 * r.nextDouble() * (1 - (r.nextDouble()*2)), 0.8 * r.nextDouble(), 1.35 * r.nextDouble() * (1 - (r.nextDouble()*2)));
                    //boolean crit = (player.getCritChance()/100) >= r.nextFloat();
                    boolean crit = (player.getCritChance() * (1+(player.getLuck()/100))) >= (r.nextFloat()*100);
                    double damage = plugin.calculateDamage(player, e.getEntity(), p.getItemInHand(), crit);
                    if (defense != 0) {
                        damage = plugin.calculateDefenseDamage((float)defense, (float)damage);
                    }
                    Entity indicator = null;
                    if (crit) {
                        indicator = spectralDamage.spawnDamageIndicator(p, target, new CritDamageIndicator(), (int)damage);
                    } else {
                        indicator = spectralDamage.spawnDamageIndicator(p, target, new NormalDamageIndicator(), (int)damage);
                    }
                    Entity finalIndicator = indicator;
                    new BukkitRunnable() {

                        @Override
                        public void run() {
                            if (finalIndicator.isValid()) {
                                finalIndicator.remove();
                            }
                        }
                    }.runTaskLater(plugin, 30L);
                    e.setDamage((int) damage);
                }
            } else if (e.getEntity() instanceof Player) {
                if (!plugin.tournament) {
                    e.setCancelled(true);
                }
                if (plugin.currentGame != null && !plugin.currentGame.canPvp()) {
                    e.setCancelled(true);
                }
                defense = (int)this.plugin.players.get(((Player)e.getEntity()).getUniqueId()).getDefense();
                if (plugin.players.keySet().contains(p.getUniqueId())) {
                    MMOPlayer player = plugin.players.get(p.getUniqueId());
                    Location target = e.getEntity().getLocation();

                    target.add(1.35 * r.nextDouble() * (1 - (r.nextDouble() * 2)), 0.8 * r.nextDouble(), 1.35 * r.nextDouble() * (1 - (r.nextDouble() * 2)));
                    boolean crit = (player.getCritChance() * (1+(player.getLuck()/100))) >= (r.nextFloat()*100);
                    double damage = plugin.calculateDamage(player, e.getEntity(), p.getItemInHand(), crit);
                    if (defense != 0) {
                        damage = plugin.calculateDefenseDamage((float) defense, (float) damage);
                    }
                    Entity indicator = null;
                    if (crit) {
                        indicator = spectralDamage.spawnDamageIndicator(p, target, new CritDamageIndicator(), (int) damage);
                    } else {
                        indicator = spectralDamage.spawnDamageIndicator(p, target, new NormalDamageIndicator(), (int) damage);
                    }
                    Entity finalIndicator = indicator;
                    new BukkitRunnable() {

                        @Override
                        public void run() {
                            if (finalIndicator.isValid()) {
                                finalIndicator.remove();
                            }
                        }
                    }.runTaskLater(plugin, 30L);
                    e.setDamage((int) damage);
                }
            }


        } else if (e.getDamager() instanceof Projectile && this.arrowsAndDamage.containsKey(e.getDamager()) ) {
            Projectile projectile = (Projectile) e.getDamager();
            Player player = (Player) projectile.getShooter();
            if (player.getAttackCooldown() != 1.0) {
                e.setCancelled(true);
                return;
            }
            if (!checkWieldingItemForLvlRequirements(player)) {
                player.sendMessage(ChatColor.RED + "You don't have the necessary level to use this item!");
                e.setCancelled(true);
                return;
            }
            if (e.getEntity() instanceof NPC) {
                return;
            }
            e.setDamage((int)doBowDamage(player, e.getEntity(), projectile));

        }

    }

    public double doBowDamage(Player shooter, Entity target, Projectile projectile) {
        int defense = 0;
        if (this.mobs.keySet().contains(target)) {
            defense = (int)this.mobs.get(target).defense;
        } else if (plugin.players.containsKey(target.getUniqueId())) {
            defense = (int)plugin.players.get(target.getUniqueId()).getDefense();
        }

        Location targetLoc = target.getLocation();
        targetLoc.add(1.35 * r.nextDouble() * (1 - (r.nextDouble() * 2)), 0.8 * r.nextDouble(), 1.35 * r.nextDouble() * (1 - (r.nextDouble() * 2)));
        double damage = this.arrowsAndDamage.get(projectile).damage;
        if (defense != 0) {
            damage = plugin.calculateDefenseDamage((float)defense, (float)damage);
        }
        Entity indicator = null;
        if (this.arrowsAndDamage.get(projectile).crit) {
            indicator = spectralDamage.spawnDamageIndicator(shooter, targetLoc, new CritDamageIndicator(), (int)damage);
        } else {
            indicator = spectralDamage.spawnDamageIndicator(shooter, targetLoc, new NormalDamageIndicator(), (int)damage);
        }
        Entity finalIndicator = indicator;
        new BukkitRunnable() {

            @Override
            public void run() {
                if (finalIndicator.isValid()) {
                    finalIndicator.remove();
                }
            }
        }.runTaskLater(plugin, 30L);
        return damage;
    }



    @EventHandler(priority = EventPriority.MONITOR)
    public void onMMMobSpawn(MythicMobSpawnEvent e) {
        if (plugin.mobHandler.containsName(plugin.mobHandler.allMobs, e.getMobType().getInternalName())) {
            MMOMob mob = plugin.mobHandler.getMobFromString(e.getMobType().getInternalName());
            this.mobs.put(e.getEntity(), mob);
            Entity entity = e.getEntity();

            plugin.addMonsterToMobs(entity);
            entity.setCustomNameVisible(false);
            ArmorStand as = (ArmorStand) entity.getLocation().getWorld().spawn(new Location(entity.getLocation().getWorld(), entity.getLocation().getX(), entity.getLocation().getY() - 0.5D, entity.getLocation().getZ()), ArmorStand.class);
            as.setGravity(false);
            as.setCustomName(mob.nameplateName);
            as.setCustomNameVisible(true);
            as.setVisible(false);
            as.setMarker(true);
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!entity.isValid()) {
                        cancel();
                        as.remove();
                        return;
                    }
                    as.teleport(entity.getLocation().clone().add(0, ((LivingEntity) entity).getEyeHeight() + (((LivingEntity) entity).getEyeHeight() * 0.15d), 0));
                    as.setCustomName(EventListener.this.mobName(entity, mob.nameplateName, mob.level));
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }
    }

    @EventHandler
    public void onMMMobDeath(MythicMobDeathEvent e) {
        if (e.getKiller() instanceof Player) {
            Player p = (Player) e.getKiller();
            if (this.mobs.containsKey(e.getEntity())) {
                MMOMob mob = this.mobs.get(e.getEntity());
                if (plugin.players.containsKey(p.getUniqueId())) {
                    Iterator iter = plugin.players.get(p.getUniqueId()).slayerMap.keySet().iterator();
                    while (iter.hasNext()) {
                        String questName = (String) iter.next();
                        IntMap map = plugin.players.get(p.getUniqueId()).slayerMap.get(questName);
                        if (map.containsKey(mob)) {
                            map.increment(mob);
                            plugin.players.get(p.getUniqueId()).slayerMap.put(questName, map);
                            //if (plugin.players.get(p.getUniqueId()).trackedObjective.questName.equalsIgnoreCase(questName)) {
                            if (plugin.questHandler.getQuest(questName) != null) {
                                if (plugin.questHandler.getQuest(questName) instanceof SlayerQuest) {
                                    int amount = ((SlayerQuest)plugin.questHandler.getQuest(questName)).amountToBring;
                                    if (plugin.players.get(p.getUniqueId()).slayerMap.get(questName).get(mob) >= amount) {
                                        if (plugin.questHandler.getQuest(questName).questProgress.containsKey(p.getUniqueId()) && plugin.questHandler.getQuest(questName).questProgress.get(p.getUniqueId()).equalsIgnoreCase(((SlayerQuest) plugin.questHandler.getQuest(questName)).stepToFinish)) {
                                            plugin.questHandler.objectiveUpdater.performStatusUpdates(p, questName, plugin.questHandler.getQuest(questName).tgtNpc.updateOnCompleteCollection);
                                        }
                                    }
                                }
                            }
                            //}
                        }
                    }
                }
                mob.calculateDrops(p);
                if (plugin.playerAndDungeonRoom.containsKey(p.getUniqueId())) {
                    p.sendMessage("In player and dungeon room list");
                    if (plugin.playerAndDungeonRoom.get(p.getUniqueId()).goal == RoomGoal.SLAYER) {
                        p.sendMessage("In room");
                        Room room = plugin.playerAndDungeonRoom.get(p.getUniqueId());
                        if (room.mobToKill.internalName.equalsIgnoreCase(mob.internalName)) {
                            room.incrementPlayerProgress(p);
                        }
                    }
                }

            }


        }
        if (e.getEntity().getPassenger() != null) {
            if (e.getEntity().getPassenger().getPassenger() != null) {
                e.getEntity().getPassenger().getPassenger().remove();
            }
            e.getEntity().getPassenger().remove();
        }
        this.mobs.remove(e.getEntity());


    }

    public String mobName(Entity entity, String name, int lvl) {
        String str = ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + "Lv" + lvl + ChatColor.DARK_GRAY + "] ";
        str = str + ChatColor.RED + name;
        if (((Damageable) entity).getHealth() == ((Damageable) entity).getMaxHealth()) {
            str = str + " " + ChatColor.GREEN;
        } else {
            str = str + " " + ChatColor.YELLOW;
        }
        str = str + (int)((Damageable) entity).getHealth() + ChatColor.WHITE + "/" + ChatColor.GREEN + (int)((Damageable) entity).getMaxHealth() + ChatColor.RED + "❤";
        return str;
    }

    @EventHandler
    public void helmetClick(InventoryClickEvent e) {
        if ((e.getClick() == ClickType.SHIFT_LEFT || e.getClick() == ClickType.SHIFT_RIGHT) && e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasLore() && ChatColor.stripColor(e.getCurrentItem().getItemMeta().getLore().get(e.getCurrentItem().getItemMeta().getLore().size() - 1)).contains("HELMET") && ((Player)e.getWhoClicked()).getInventory().getHelmet() == null) {
            if (plugin.listener.checkItemForRequirements(Bukkit.getPlayer(e.getWhoClicked().getUniqueId()), e.getCurrentItem())) {
                ((Player) e.getWhoClicked()).getInventory().setHelmet(e.getCurrentItem());
                e.getWhoClicked().getInventory().remove(e.getCurrentItem());
                e.setCancelled(true);
            } else {
                e.setCancelled(true);
                e.getWhoClicked().sendMessage(ChatColor.RED + "You don't have the necessary level to use this item!");

            }

        } else if ((e.getClick() == ClickType.LEFT || e.getClick() == ClickType.RIGHT) && e.getCursor() != null && e.getCursor().hasItemMeta() && e.getCursor().getItemMeta().hasLore() && ChatColor.stripColor(e.getCursor().getItemMeta().getLore().get(e.getCursor().getItemMeta().getLore().size() - 1)).contains("HELMET") && e.getRawSlot() == 5) {
            if (plugin.listener.checkItemForRequirements(Bukkit.getPlayer(e.getWhoClicked().getUniqueId()), e.getCursor())) {
                return;
            } else {
                e.setCancelled(true);
                e.getWhoClicked().sendMessage(ChatColor.RED + "You don't have the necessary level to use this item!");
            }

        } else if ((e.getClick() == ClickType.SHIFT_LEFT || e.getClick() == ClickType.SHIFT_RIGHT) && e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasLore() && ChatColor.stripColor(e.getCurrentItem().getItemMeta().getLore().get(e.getCurrentItem().getItemMeta().getLore().size() - 1)).contains("CHESTPLATE") && ((Player)e.getWhoClicked()).getInventory().getChestplate() == null) {
            if (plugin.listener.checkItemForRequirements(Bukkit.getPlayer(e.getWhoClicked().getUniqueId()), e.getCurrentItem())) {
                ((Player) e.getWhoClicked()).getInventory().setChestplate(e.getCurrentItem());
                e.getWhoClicked().getInventory().remove(e.getCurrentItem());
                e.setCancelled(true);
            } else {
                e.setCancelled(true);
                e.getWhoClicked().sendMessage(ChatColor.RED + "You don't have the necessary level to use this item!");
            }

        } else if ((e.getClick() == ClickType.LEFT || e.getClick() == ClickType.RIGHT) && e.getCursor() != null && e.getCursor().hasItemMeta() && e.getCursor().getItemMeta().hasLore() && ChatColor.stripColor(e.getCursor().getItemMeta().getLore().get(e.getCursor().getItemMeta().getLore().size() - 1)).contains("CHESTPLATE") && e.getRawSlot() == 6) {
            if (plugin.listener.checkItemForRequirements(Bukkit.getPlayer(e.getWhoClicked().getUniqueId()), e.getCursor())) {
                return;
            } else {
                e.setCancelled(true);
                e.getWhoClicked().sendMessage(ChatColor.RED + "You don't have the necessary level to use this item!");
            }

        } else if ((e.getClick() == ClickType.SHIFT_LEFT || e.getClick() == ClickType.SHIFT_RIGHT) && e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasLore() && ChatColor.stripColor(e.getCurrentItem().getItemMeta().getLore().get(e.getCurrentItem().getItemMeta().getLore().size() - 1)).contains("LEGGINGS") && ((Player)e.getWhoClicked()).getInventory().getLeggings() == null) {
            if (plugin.listener.checkItemForRequirements(Bukkit.getPlayer(e.getWhoClicked().getUniqueId()), e.getCurrentItem())) {
                ((Player) e.getWhoClicked()).getInventory().setLeggings(e.getCurrentItem());
                e.getWhoClicked().getInventory().remove(e.getCurrentItem());
                e.setCancelled(true);
            } else {
                e.setCancelled(true);
                e.getWhoClicked().sendMessage(ChatColor.RED + "You don't have the necessary level to use this item!");
            }

        } else if ((e.getClick() == ClickType.LEFT || e.getClick() == ClickType.RIGHT) && e.getCursor() != null && e.getCursor().hasItemMeta() && e.getCursor().getItemMeta().hasLore() && ChatColor.stripColor(e.getCursor().getItemMeta().getLore().get(e.getCursor().getItemMeta().getLore().size() - 1)).contains("LEGGINGS") && e.getRawSlot() == 7) {
            if (plugin.listener.checkItemForRequirements(Bukkit.getPlayer(e.getWhoClicked().getUniqueId()), e.getCursor())) {
                return;
            } else {
                e.setCancelled(true);
                e.getWhoClicked().sendMessage(ChatColor.RED + "You don't have the necessary level to use this item!");
            }

        } else if ((e.getClick() == ClickType.SHIFT_LEFT || e.getClick() == ClickType.SHIFT_RIGHT) && e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasLore() && ChatColor.stripColor(e.getCurrentItem().getItemMeta().getLore().get(e.getCurrentItem().getItemMeta().getLore().size() - 1)).contains("BOOTS") && ((Player)e.getWhoClicked()).getInventory().getBoots() == null) {
            if (plugin.listener.checkItemForRequirements(Bukkit.getPlayer(e.getWhoClicked().getUniqueId()), e.getCurrentItem())) {
                ((Player) e.getWhoClicked()).getInventory().setBoots(e.getCurrentItem());
                e.getWhoClicked().getInventory().remove(e.getCurrentItem());
                e.setCancelled(true);
            } else {
                e.setCancelled(true);
                e.getWhoClicked().sendMessage(ChatColor.RED + "You don't have the necessary level to use this item!");
            }

        } else if ((e.getClick() == ClickType.LEFT || e.getClick() == ClickType.RIGHT) && e.getCursor() != null && e.getCursor().hasItemMeta() && e.getCursor().getItemMeta().hasLore() && ChatColor.stripColor(e.getCursor().getItemMeta().getLore().get(e.getCursor().getItemMeta().getLore().size() - 1)).contains("BOOTS") && e.getRawSlot() == 8) {
            if (plugin.listener.checkItemForRequirements(Bukkit.getPlayer(e.getWhoClicked().getUniqueId()), e.getCursor())) {
                return;
            } else {
                e.setCancelled(true);
                e.getWhoClicked().sendMessage(ChatColor.RED + "You don't have the necessary level to use this item!");
            }

        }
    }
    @EventHandler
    public void helmetClickAir(PlayerInteractEvent e) {
        if ((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) && (e.hasItem() || e.hasBlock())) {
            ItemStack item = e.getItem();
            if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore() && ChatColor.stripColor(item.getItemMeta().getLore().get(item.getItemMeta().getLore().size() - 1)).contains("HELMET") && ((Player)e.getPlayer()).getInventory().getHelmet() == null) {

                if (plugin.listener.checkItemForRequirements(e.getPlayer(), item)) {
                    e.getPlayer().getInventory().setItemInMainHand(null);
                    e.getPlayer().getInventory().setHelmet(item);
                    e.setCancelled(true);
                }

            } else if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore() && ChatColor.stripColor(item.getItemMeta().getLore().get(item.getItemMeta().getLore().size() - 1)).contains("CHESTPLATE") && ((Player)e.getPlayer()).getInventory().getHelmet() == null) {

                if (plugin.listener.checkItemForRequirements(e.getPlayer(), item)) {
                    e.getPlayer().getInventory().setItemInMainHand(null);
                    e.getPlayer().getInventory().setChestplate(item);
                    e.setCancelled(true);
                }

            } else if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore() && ChatColor.stripColor(item.getItemMeta().getLore().get(item.getItemMeta().getLore().size() - 1)).contains("LEGGINGS") && ((Player)e.getPlayer()).getInventory().getHelmet() == null) {

                if (plugin.listener.checkItemForRequirements(e.getPlayer(), item)) {
                    e.getPlayer().getInventory().setItemInMainHand(null);
                    e.getPlayer().getInventory().setLeggings(item);
                    e.setCancelled(true);
                }

            } else if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore() && ChatColor.stripColor(item.getItemMeta().getLore().get(item.getItemMeta().getLore().size() - 1)).contains("BOOTS") && ((Player)e.getPlayer()).getInventory().getHelmet() == null) {
                if (plugin.listener.checkItemForRequirements(e.getPlayer(), item)) {
                    e.getPlayer().getInventory().setItemInMainHand(null);
                    e.getPlayer().getInventory().setBoots(item);
                }
                e.setCancelled(true);

            }
        }
    }

    @EventHandler
    public void mmoItemSpellCast(PlayerInteractEvent e) {
        if ((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) && e.hasItem() && e.getItem().hasItemMeta()) {
            NBTItem nbtI = new NBTItem(e.getItem());
            if (!nbtI.hasTag("ExtraAttributes")) {
                return;
            }
            NBTCompound comp = nbtI.getCompound("ExtraAttributes");
            if (!comp.hasTag("id")) {
                return;
            }
            String id = comp.getString("id");
            if (!checkWieldingItemForLvlRequirements(e.getPlayer())) {
                e.getPlayer().sendMessage(ChatColor.RED + "You don't have the necessary level to use this item!");
                e.setCancelled(true);
                return;
            }
            MMOItem item = plugin.itemHandler.getMMOItemFromString(id.toLowerCase());
            if (item != null && item.spellName != null) {
                e.setCancelled(true);
                Spell spell = MagicSpells.getSpellByInternalName(item.spellName);
                Spell.SpellCastResult result = spell.cast(e.getPlayer());
                if (result.success()) {
                    if (result.action != Spell.PostCastAction.ALREADY_HANDLED) {
                        if (spell.getReagents().getMana() > 0) {
                            plugin.actionBarMessenger.queueCastMessage(e.getPlayer(), spell.getName(), spell.getReagents().getMana());
                        }
                        if (item.consumable) {
                            if (e.getPlayer().getItemInHand() != null && e.getPlayer().getItemInHand().getAmount() <= 1) {
                                e.getPlayer().setItemInHand(new ItemStack(Material.AIR));
                            } else {
                                e.getPlayer().setItemInHand(e.getPlayer().getItemInHand().asQuantity(e.getPlayer().getItemInHand().getAmount()-1));
                            }
                        }
                    }
                }
            }
        } else if ((e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) && e.hasItem() && e.getItem().hasItemMeta()) {
            NBTItem nbtI = new NBTItem(e.getItem());
            if (!nbtI.hasTag("ExtraAttributes")) {
                return;
            }
            NBTCompound comp = nbtI.getCompound("ExtraAttributes");
            if (!comp.hasTag("id")) {
                return;
            }
            String id = comp.getString("id");
            if (!checkWieldingItemForLvlRequirements(e.getPlayer())) {
                e.getPlayer().sendMessage(ChatColor.RED + "You don't have the necessary level to use this item!");
                e.setCancelled(true);
                return;
            }
            MMOItem item = plugin.itemHandler.getMMOItemFromString(id.toLowerCase());
            if (item != null && item.lSpellName != null) {
                Spell spell = MagicSpells.getSpellByInternalName(item.lSpellName);
                Spell.SpellCastResult result = spell.cast(e.getPlayer());
                if (result.success()) {
                    if (spell.getReagents().getMana() > 0) {
                        plugin.actionBarMessenger.queueCastMessage(e.getPlayer(), spell.getName(), spell.getReagents().getMana());
                    }
                    if (item.consumable) {
                        MMOItem.removeItem(e.getPlayer(), item, 1);
                    }
                }

            }
        }

    }
    @EventHandler
    public void onClickOffHand(InventoryClickEvent e) {
        if (e.getInventory().getType() == InventoryType.CRAFTING && e.getRawSlot() == 45) {
            e.setCancelled(true);
        }
    }
    @EventHandler
    public void onClickOffHand2(InventoryDragEvent e) {
        if (e.getInventory().getType() == InventoryType.CRAFTING && e.getRawSlots().contains(45)) {
            e.setCancelled(true);
        }
    }
    @EventHandler
    public void onMenuClick2(InventoryClickEvent e) {
        if (e.getInventory().getType() == InventoryType.CRAFTING) {
            if (e.getRawSlot()+2 == e.getWhoClicked().getOpenInventory().countSlots()) {
                e.setCancelled(true);
                e.setCursor(new ItemStack(Material.AIR));
                plugin.menus.openHomeMenu(Bukkit.getPlayer(e.getWhoClicked().getUniqueId()));
            }
        } else if (e.getInventory().getType() == InventoryType.CHEST) {
            if (e.getRawSlot()+6 == e.getWhoClicked().getOpenInventory().countSlots()) {
                e.setCancelled(true);
                e.setCursor(new ItemStack(Material.AIR));
                plugin.menus.openHomeMenu(Bukkit.getPlayer(e.getWhoClicked().getUniqueId()));
            }
        }

    }
    @EventHandler
    public void onMenuClick(InventoryDragEvent e) {
        for (int slot : e.getRawSlots()) {
            if (e.getInventory().getType() == InventoryType.CRAFTING) {
                if (slot+2 == e.getWhoClicked().getOpenInventory().countSlots()) {
                    e.setCancelled(true);
                    e.setCursor(new ItemStack(Material.AIR));
                    plugin.menus.openHomeMenu(Bukkit.getPlayer(e.getWhoClicked().getUniqueId()));
                }
            } else if (e.getInventory().getType() == InventoryType.CHEST) {
                if (slot+6 == e.getWhoClicked().getOpenInventory().countSlots()) {
                    e.setCancelled(true);
                    e.setCursor(new ItemStack(Material.AIR));
                    plugin.menus.openHomeMenu(Bukkit.getPlayer(e.getWhoClicked().getUniqueId()));
                }
            }
        }

    }
    @EventHandler
    public void onArrowInGround(ProjectileHitEvent e) {
        e.getEntity().remove();
    }


    @EventHandler
    public void onInvOpenNoMenuAnymore(InventoryOpenEvent e) {

        if (plugin.itemHandler.itemIsMMOItemOfName(e.getPlayer().getItemOnCursor(), "tgt_menu")) {
            e.getPlayer().setItemOnCursor(new ItemStack(Material.AIR));
        }
    }
    @EventHandler
    public void onOffhandSwap(PlayerSwapHandItemsEvent e) {
        e.setCancelled(true);
        if (e.getMainHandItem() != null && e.getMainHandItem().hasItemMeta()) {
            NBTItem i = new NBTItem(e.getMainHandItem());
            if (!i.hasTag("ExtraAttributes")) {
                return;
            }
            NBTCompound comp = i.getCompound("ExtraAttributes");
            if (!comp.hasTag("id")) {
                return;
            }
            String id = comp.getString("id");
            if (!checkWieldingItemForLvlRequirements(e.getPlayer())) {
                e.getPlayer().sendMessage(ChatColor.RED + "You don't have the necessary level to use this item!");
                e.setCancelled(true);
                return;
            }
            MMOItem item = plugin.itemHandler.getMMOItemFromString(id.toLowerCase());
            if (item != null && item.activateSpellName != null) {
                Spell spell = MagicSpells.getSpellByInternalName(item.activateSpellName);
                Spell.SpellCastResult result = spell.cast(e.getPlayer());
                if (result.success()) {
                    if (spell.getReagents().getMana() > 0) {
                        plugin.actionBarMessenger.queueCastMessage(e.getPlayer(), spell.getName(), spell.getReagents().getMana());
                    }
                    if (item.consumable) {
                        MMOItem.removeItem(e.getPlayer(), item, 1);
                    }
                }

            }
        }
    }

    @EventHandler
    public void coralFade(BlockFadeEvent e) {
        e.setCancelled(true);
    }
    @EventHandler
    public void blockGrow(BlockGrowEvent e) {
        e.setCancelled(true);
    }
    @EventHandler
    public void blockSpread(BlockSpreadEvent e) {
        e.setCancelled(true);
    }
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {

        e.setCancelled(true);
        MMOPlayer mmoPlayer = plugin.players.get(e.getPlayer().getUniqueId());
        mmoPlayer.setHealth(mmoPlayer.getMaxHealth());
        if (!plugin.tournament) {
            e.getPlayer().teleport(mmoPlayer.getRespawnLocation());
            mmoPlayer.removePurseGold((int)(mmoPlayer.getPurseGold()/2));
            e.getPlayer().sendMessage(ChatColor.RED + "You died and lost " + (int)(mmoPlayer.getPurseGold()/2) + " Dosh!");
            e.getPlayer().playSound(e.getPlayer(), "entity.zombie.attack_iron_door", 2000.0F, 2.0F);
            e.getPlayer().playSound(e.getPlayer(), "entity.player.death", 1000.0F, 2.0F);
        } else {
            MiniGame game = this.plugin.getCurrentGame();
            if (game != null) {
                game.playerDeath(e.getEntity());
                final Player player = e.getPlayer();
                player.teleport(game.getInitSpawn());
                e.getPlayer().playSound(e.getPlayer(), "entity.zombie.attack_iron_door", 2000.0F, 2.0F);
                e.getPlayer().playSound(e.getPlayer(), "entity.player.death", 1000.0F, 2.0F);
                Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                    public void run() {
                        game.playerJoin(player);
                    }
                }, 1L);
            }


        }
    }
    private boolean fishingInOcean(Location loc) {
        return loc.getX() < plugin.grid.oceanXMin
                || loc.getX() > plugin.grid.oceanXMax
                || loc.getZ() < plugin.grid.oceanZMin
                || loc.getZ() > plugin.grid.oceanZMax;
    }
    @EventHandler
    public void onFish(PlayerFishEvent e) {
        Location hookLoc = e.getHook().getLocation().toCenterLocation();
        if (e.getState() == PlayerFishEvent.State.FISHING && fishingInOcean(hookLoc)) {
            FishHook hook = e.getHook();
            Player player = e.getPlayer();
            hook.setApplyLure(false);
            hook.setRainInfluenced(false);
            hook.setSkyInfluenced(false);
            if (!plugin.fishingZoneHandler.containsName(plugin.fishingZoneHandler.allZones, "ocean")) {
                e.setCancelled(true);
                return;
            }
            FishingZoneTemplate template = plugin.fishingZoneHandler.getTemplateWithName("ocean");
            int min = (int)((int)100 + (template.minTimeModifier*20L) - (plugin.players.get(player.getUniqueId()).getLure() * 5L));
            int max = (int)(600 + (template.minTimeModifier*20L) - (plugin.players.get(player.getUniqueId()).getLure() * 5L));
            if (min < 0) {
                min = 0;
            }
            if (max < 0 || max < min) {
                max = min + 1;
            }
            hook.setWaitTime(min, max);
            me.genn.thegrandtourney.skills.fishing.Fish fish = template.selectDrop(e.getPlayer());
            this.queuedFish.put(e.getPlayer(), fish);
            min = (int)((fish.minTime*20L) - (plugin.players.get(player.getUniqueId()).getFlash() * 1L));
            max = (int)((fish.maxTime*20L) - (plugin.players.get(player.getUniqueId()).getFlash() * 1L));
            if (min < 5) {
                min = 5;
            }
            if (max < 5 || max < min) {
                max = min + 1;
            }
            hook.setLureTime(min, max);
        } else if (e.getState() == PlayerFishEvent.State.CAUGHT_FISH && fishingInOcean(hookLoc) && this.queuedFish.containsKey(e.getPlayer())) {
            Fish fish = this.queuedFish.get(e.getPlayer());
            if (!plugin.fishingZoneHandler.containsName(plugin.fishingZoneHandler.allZones, "ocean")) {
                e.setCancelled(true);
                return;
            }
            FishingZoneTemplate template = plugin.fishingZoneHandler.getTemplateWithName("ocean");
            if (fish.mob != null) {
                e.setExpToDrop(0);
                e.getCaught().remove();
                e.getHook().setHookedEntity(MythicBukkit.inst().getMobManager().spawnMob(fish.mob.mythicmob.getInternalName(), e.getHook().getLocation()).getEntity().getBukkitEntity());
                template.drops.grantXpAndGold(e.getPlayer(), r);
                e.getHook().pullHookedEntity();
            } else if (fish.drop != null) {
                e.setExpToDrop(0);
                int quantity = r.nextInt((int) fish.minQuantity, (int) (fish.maxQuantity+1));
                float bonus = plugin.players.get(e.getPlayer().getUniqueId()).getFishingFortune();
                int bonusQuantity = 0;
                while (bonus > 100) {
                    bonusQuantity++;
                    bonus-=100;
                }
                if (bonus > r.nextInt(100)) {
                    bonusQuantity++;
                }
                ((Item)e.getCaught()).setItemStack(plugin.itemHandler.getItem(fish.drop).asQuantity(quantity+bonusQuantity));
                e.getHook().pullHookedEntity();
                template.drops.grantXpAndGold(e.getPlayer(), r);
                template.drops.checkDungeonRoom(e.getPlayer(), plugin.itemHandler.getItem(fish.drop), quantity+bonusQuantity);
            }
        }
    }
    @EventHandler
    public void openVanillaRecipeBook(PlayerRecipeBookClickEvent e) {
        e.setCancelled(true);
    }
    @EventHandler
    public void discoverRecipe(PlayerRecipeDiscoverEvent e) {
        e.setCancelled(true);

    }
    @EventHandler
    public void leafDecayCancel(LeavesDecayEvent e) {
        e.setCancelled(true);

    }
    @EventHandler
    public void onSpellCast(SpellCastEvent e) {
        if (!(e.getCaster() instanceof Player)) {
            return;
        }
        if (e.getSpellCastState() == Spell.SpellCastState.MISSING_REAGENTS) {
            plugin.actionBarMessenger.queueOutOfManaMessage((Player)e.getCaster());
        }
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent e) {
        e.setCancelled(true);
    }
    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent e) {
        e.setCancelled(true);
        e.setFoodLevel(20);
    }

    @EventHandler
    public void magicDamageEvent(SpellApplyDamageEvent e) {
        if (e.getCaster() instanceof Player) {
            Player player = (Player) e.getCaster();
            MMOPlayer mmoPlayer = plugin.players.get(player.getUniqueId());
            String name = e.getSpell().getInternalName();

            if (name.startsWith("helmet")) {

            } else if (name.startsWith("chestplate")) {

            } else if (name.startsWith("leggings")) {

            } else if (name.startsWith("boots")) {

            } else {
                ItemStack item = player.getItemInHand();
                NBTItem nbtI = new NBTItem(item);
                if (!nbtI.hasTag("ExtraAttributes")) {
                    return;
                }
                if (!nbtI.getCompound("ExtraAttributes").hasTag("id")) {
                    return;
                }
                float num = plugin.statUpdates.getAbilityDamageModifier(mmoPlayer,e.getTarget(),item);
                Location target = e.getTarget().getLocation();

                target.add(1.35 * r.nextDouble() * (1 - (r.nextDouble()*2)), 0.8 * r.nextDouble(), 1.35 * r.nextDouble() * (1 - (r.nextDouble()*2)));

                e.applyDamageModifier(num);
                float defense = 0;
                if (this.mobs.containsKey((Entity)e.getTarget())) {
                    defense = this.mobs.get(e.getTarget()).defense;
                }
                double targetDamage = plugin.calculateDefenseDamage(defense, (float)e.getFinalDamage());
                e.applyDamageModifier((float) (targetDamage/e.getFinalDamage()));

                spectralDamage.spawnDamageIndicator(player, target, new NormalDamageIndicator(), (int)Math.round(e.getFinalDamage()));

            }

        }
    }
    /*@EventHandler(priority = EventPriority.HIGH)
    public void magicDamageAdjust(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            Player p = (Player) e.getDamager();
            if (e.getCause() == EntityDamageEvent.DamageCause.MAGIC) {
                float defense = 0;
                if (this.mobs.containsKey(e.getEntity())) {
                    MMOMob mmoMob = this.mobs.get(e.getEntity());
                    defense = mmoMob.defense;
                }
                MMOPlayer mmoPlayer = plugin.players.get(p.getUniqueId());
                float damage = e.getDamage() * plugin.statUpdates.getAbilityDamageModifier()
                e.setDamage((int)plugin.calculateDefenseDamage(defense,(float) e.getDamage()));
            }
        }
    }*/
    @EventHandler
    public void onJoin (PlayerJoinEvent e) {
        plugin.connectTime.put(e.getPlayer().getUniqueId(), System.currentTimeMillis());

        if (!plugin.tournament) {
            return;
        }
        MiniGame game = this.plugin.getCurrentGame();
        if (game != null) {
            game.playerJoin(e.getPlayer());
        } else {
            Location loc = new Location((World)Bukkit.getWorlds().get(0), 0.0, 0.0, 0.0);
            loc.setY((double)(loc.getWorld().getHighestBlockYAt(loc) + 1));
            e.getPlayer().teleport(loc);
        }
    }

    @EventHandler
    public void onEffectsLoad(SpellEffectsLoadingEvent e) {
        //e.getSpellEffectManager().removeSpellEffect("armorstand");
        e.getSpellEffectManager().addSpellEffect(BetterArmorStandEffect.class, "b_armorstand");

    }
    @EventHandler
    public void onConditionsLoad(ConditionsLoadingEvent e) {
        e.getConditionManager().addCondition(DayNumberCondition.class, "day-number");
        e.getConditionManager().addCondition(DoshCondition.class, "dosh");
        e.getConditionManager().addCondition(SkillLevelCondition.class, "skill-lvl");
        e.getConditionManager().addCondition(TGTDayCondition.class, "t-day");
        e.getConditionManager().addCondition(TGTNightCondition.class, "t-night");
        e.getConditionManager().addCondition(TournamentCondition.class, "tournament");
        e.getConditionManager().addCondition(HasMMOItem.class, "has-mmo-item");
        e.getConditionManager().addCondition(HasMMOItemAmount.class, "has-mmo-item-amount");
    }














}
