package me.genn.thegrandtourney.listener;

import java.util.*;

import com.destroystokyo.paper.event.player.PlayerRecipeBookClickEvent;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.events.SpellApplyDamageEvent;
import com.nisovin.magicspells.events.SpellTargetEvent;
import de.tr7zw.nbtapi.NBTItem;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import io.lumine.mythic.bukkit.events.MythicMobSpawnEvent;
import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.dungeons.Room;
import me.genn.thegrandtourney.dungeons.RoomGoal;
import me.genn.thegrandtourney.item.MMOItem;
import me.genn.thegrandtourney.mobs.MMOMob;
import me.genn.thegrandtourney.npc.ItemRetrievalQuest;
import me.genn.thegrandtourney.npc.Quest;
import me.genn.thegrandtourney.npc.SlayerQuest;
import me.genn.thegrandtourney.player.CritDamageIndicator;
import me.genn.thegrandtourney.player.MMOPlayer;
import me.genn.thegrandtourney.player.NormalDamageIndicator;
import me.genn.thegrandtourney.player.StatUpdates;
import me.genn.thegrandtourney.skills.mining.Ore;
import me.genn.thegrandtourney.util.IntMap;
import me.genn.thegrandtourney.xp.Xp;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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

    public EventListener(TGT plugin) {
        this.plugin = plugin;
        this.r = new Random();
        this.spectralDamage = SpectralDamage.getInstance();
        this.mobs = new HashMap();
        this.ores = new HashMap();
        this.slayerTracker = new HashMap<>();
        this.cantUseMsgCd = new HashMap<>();
    }


    @EventHandler
    public void registerMMOPlayer(PlayerJoinEvent e) {

        if (!plugin.players.keySet().contains(e.getPlayer().getUniqueId())) {
            plugin.players.put(e.getPlayer().getUniqueId(), plugin.createNewPlayer(e.getPlayer()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTakeDam(EntityDamageEvent e) {
        double damage = e.getDamage();

        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (plugin.players.keySet().contains(p.getUniqueId())) {
                damage = plugin.calculateDefenseDamage((float) plugin.players.get(p.getUniqueId()).getDefense(), (float) damage);
                e.setDamage(0.0D);
                plugin.updatePlayerHealth(plugin.players.get(p.getUniqueId()), (float) -damage);

            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        if (plugin.players.keySet().contains(p.getUniqueId())) {
            plugin.updatePlayerHealth(plugin.players.get(p.getUniqueId()), plugin.players.get(p.getUniqueId()).getMaxHealth());
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
                        if (mmoPlayer.getLvlForType(mmoItem.typeRequirement) < mmoItem.lvlRequirement) {
                            return false;
                        }
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
                        if (mmoPlayer.getLvlForType(mmoItem.typeRequirement) < mmoItem.lvlRequirement) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
    @EventHandler
    public void onPlayerDoDamage(EntityDamageByEntityEvent e) {
        if (this.ores.containsKey(e.getEntity())) {
            return;
        }
        if (! (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
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
                    boolean crit = (player.getCritChance()/100) >= r.nextFloat();
                    double damage = plugin.calculateDamage(player, p.getItemInHand(), crit);
                    if (defense != 0) {
                        damage = plugin.calculateDefenseDamage((float)defense, (float)damage);
                    }
                    if (crit) {
                        spectralDamage.spawnDamageIndicator(p, target, new CritDamageIndicator(), (int)damage);
                    } else {
                        spectralDamage.spawnDamageIndicator(p, target, new NormalDamageIndicator(), (int)damage);
                    }
                    e.setDamage((int) damage);

                    /*if (this.mobs.keySet().contains(e.getEntity()) && (int)damage >= ((Damageable)e.getEntity()).getHealth()) {
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
                        //mob.calculateDrops(p);
                    }*/

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
                    boolean crit = (player.getCritChance() / 100) >= r.nextFloat();
                    double damage = plugin.calculateDamage(player, p.getItemInHand(), crit);
                    if (defense != 0) {
                        damage = plugin.calculateDefenseDamage((float) defense, (float) damage);
                    }
                    if (crit) {
                        spectralDamage.spawnDamageIndicator(p, target, new CritDamageIndicator(), (int) damage);
                    } else {
                        spectralDamage.spawnDamageIndicator(p, target, new NormalDamageIndicator(), (int) damage);
                    }
                    e.setDamage((int) damage);
                }
            }


        }

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
        str = str + ChatColor.RED.toString() + name;
        if (((Damageable) entity).getHealth() == ((Damageable) entity).getMaxHealth()) {
            str = str + " " + ChatColor.GREEN.toString();
        } else {
            str = str + " " + ChatColor.YELLOW.toString();
        }
        str = str + (int)((Damageable) entity).getHealth() + ChatColor.WHITE.toString() + "/" + ChatColor.GREEN.toString() + (int)((Damageable) entity).getMaxHealth() + ChatColor.RED.toString() + "❤";
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
                Spell spell = MagicSpells.getSpellByInternalName(item.spellName);
                spell.cast(e.getPlayer());
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
                spell.cast(e.getPlayer());
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
        if (plugin.itemHandler.itemIsMMOItemOfName(e.getCurrentItem(), "tgt_menu")) {
            e.setCancelled(true);
            e.setCursor(new ItemStack(Material.AIR));
            plugin.menus.openHomeMenu(Bukkit.getPlayer(e.getWhoClicked().getUniqueId()));

        }
    }
    @EventHandler
    public void onMenuClick(InventoryDragEvent e) {

        if (plugin.itemHandler.itemIsMMOItemOfName(e.getCursor(), "tgt_menu") || plugin.itemHandler.itemIsMMOItemOfName(e.getOldCursor(), "tgt_menu")) {
            e.setCursor(new ItemStack(Material.AIR));

            plugin.menus.openHomeMenu(Bukkit.getPlayer(e.getWhoClicked().getUniqueId()));
            e.setCancelled(true);
        }
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
                spell.cast(e.getPlayer());
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
        e.getPlayer().teleport(mmoPlayer.getRespawnLocation());
        mmoPlayer.removePurseGold((int)(mmoPlayer.getPurseGold()/2));
        mmoPlayer.setHealth(mmoPlayer.getMaxHealth());
        e.getPlayer().sendMessage(ChatColor.RED + "You died and lost " + (int)(mmoPlayer.getPurseGold()/2) + " Dosh!");
        e.getPlayer().playSound(e.getPlayer(), "entity.zombie.attack_iron_door", 2000.0F, 2.0F);
        e.getPlayer().playSound(e.getPlayer(), "entity.player.death", 1000.0F, 2.0F);
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

    /*@EventHandler
    public void loggingTest(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (e.getClickedBlock() == null) {
            if (p.hasPotionEffect(PotionEffectType.SLOW_DIGGING)) {
                p.removePotionEffect(PotionEffectType.SLOW_DIGGING);
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 2147479783, -5));
            } else {
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 2147479783, -1));
            }
        }
        if (e.getClickedBlock().getType() == Material.OAK_LOG) {

            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 2147479783, 0));
        } else {
            if (p.hasPotionEffect(PotionEffectType.SLOW_DIGGING)) {
                p.removePotionEffect(PotionEffectType.SLOW_DIGGING);
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 2147479783, -5));
            }
        }
    }
    @EventHandler
    public void loggingTest2(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if (e.getBlock().getType() == Material.OAK_LOG) {
            if (p.hasPotionEffect(PotionEffectType.SLOW_DIGGING)) {
                p.removePotionEffect(PotionEffectType.SLOW_DIGGING);
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 99999999, -5));
            }

        }
    }*/

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
                MMOItem mmoItem = plugin.itemHandler.getMMOItemFromString(nbtI.getCompound("ExtraAttributes").getString("id"));
                float num = plugin.statUpdates.getAbilityDamageModifier(mmoPlayer,mmoItem);
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
    }














}
