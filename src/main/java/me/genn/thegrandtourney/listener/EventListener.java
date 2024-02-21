package me.genn.thegrandtourney.listener;

import java.util.*;

import de.tr7zw.nbtapi.NBTItem;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import io.lumine.mythic.bukkit.events.MythicMobSpawnEvent;
import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.item.MMOItem;
import me.genn.thegrandtourney.mobs.MMOMob;
import me.genn.thegrandtourney.player.CritDamageIndicator;
import me.genn.thegrandtourney.player.MMOPlayer;
import me.genn.thegrandtourney.player.NormalDamageIndicator;
import me.genn.thegrandtourney.player.StatUpdates;
import me.genn.thegrandtourney.skills.mining.Ore;
import me.genn.thegrandtourney.util.IntMap;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;


import ch.twidev.spectraldamage.api.SpectralDamage;
import de.tr7zw.nbtapi.NBTCompound;


import net.citizensnpcs.api.npc.NPC;


public class EventListener implements Listener {
    TGT plugin;
    Random r;
    public SpectralDamage spectralDamage;
    Map<Entity, MMOMob> mobs;
    public Map<Entity, Ore> ores;
    public Map<MMOMob, IntMap<Player>> slayerTracker;

    public EventListener(TGT plugin) {
        this.plugin = plugin;
        this.r = new Random();
        this.spectralDamage = SpectralDamage.getInstance();
        this.mobs = new HashMap();
        this.ores = new HashMap();
        this.slayerTracker = new HashMap<>();
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
            StatUpdates.updateFullInventory(p, plugin.players.get(p.getUniqueId()));

        }
    }
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player p = (Player) event.getWhoClicked();
            if (plugin.players.keySet().contains(p.getUniqueId())) {
                StatUpdates.updateFullInventory(p, plugin.players.get(p.getUniqueId()));

            }
        }

    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player p = (Player) event.getWhoClicked();
            if (plugin.players.keySet().contains(p.getUniqueId())) {
                StatUpdates.updateFullInventory(p, plugin.players.get(p.getUniqueId()));

            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player p = event.getPlayer();
        if (plugin.players.keySet().contains(p.getUniqueId())) {
            StatUpdates.updateFullInventory(p, plugin.players.get(p.getUniqueId()));

        }
    }
    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player p = event.getPlayer();
        if (plugin.players.keySet().contains(p.getUniqueId())) {
            StatUpdates.updateFullInventory(p, plugin.players.get(p.getUniqueId()));

        }
    }
    @EventHandler
    public void onPlayerDoDamage(EntityDamageByEntityEvent e) {
        if (this.ores.containsKey(e.getEntity())) {
            return;
        }
        if (e.getDamager() instanceof Player) {
            Player p = (Player) e.getDamager();
            if (p.getAttackCooldown() != 1.0) {
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

                    e.setDamage((int)damage);
                    if (this.mobs.keySet().contains(e.getEntity()) && damage >= ((Damageable)e.getEntity()).getHealth()) {
                        MMOMob mob = this.mobs.get(e.getEntity());
                        if (plugin.players.containsKey(p.getUniqueId())) {
                            Iterator iter = plugin.players.get(p.getUniqueId()).slayerMap.keySet().iterator();
                            while (iter.hasNext()) {
                                String questName = (String) iter.next();
                                IntMap map = plugin.players.get(p.getUniqueId()).slayerMap.get(questName);
                                if (map.containsKey(mob)) {
                                    map.increment(mob);
                                    plugin.players.get(p.getUniqueId()).slayerMap.put(questName, map);
                                }
                            }
                        }
                        //mob.calculateDrops(p);
                    }

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
                    as.setCustomName(EventListener.this.mobName(entity, mob.nameplateName));
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }
    }

    @EventHandler
    public void onMMMobDeath(MythicMobDeathEvent e) {
        this.mobs.remove(e.getEntity());
        if (e.getEntity().getPassenger() != null) {
            if (e.getEntity().getPassenger().getPassenger() != null) {
                e.getEntity().getPassenger().getPassenger().remove();
            }
            e.getEntity().getPassenger().remove();
        }

    }

    public String mobName(Entity entity, String name) {
        String str = ChatColor.RED.toString() + name;
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
        if (e.getClick() == ClickType.SHIFT_LEFT && e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasLore() && ChatColor.stripColor(e.getCurrentItem().getItemMeta().getLore().get(e.getCurrentItem().getItemMeta().getLore().size() - 1)).contains("HELMET") && ((Player)e.getWhoClicked()).getInventory().getHelmet() == null) {
            ((Player) e.getWhoClicked()).getInventory().setHelmet(e.getCurrentItem());
            e.getWhoClicked().getInventory().remove(e.getCurrentItem());
        }
    }
    @EventHandler
    public void helmetClickAir(PlayerInteractEvent e) {
        if ((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) && (e.hasItem() || e.hasBlock())) {
            ItemStack item = e.getItem();
            if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore() && ChatColor.stripColor(item.getItemMeta().getLore().get(item.getItemMeta().getLore().size() - 1)).contains("HELMET") && ((Player)e.getPlayer()).getInventory().getHelmet() == null) {
                e.setCancelled(true);
                e.getPlayer().getInventory().setItemInMainHand(null);
                e.getPlayer().getInventory().setHelmet(item);
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
            MMOItem item = plugin.itemHandler.getMMOItemFromString(id.toLowerCase());
            if (item != null && item.spell != null) {
                item.spell.cast(e.getPlayer());

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
            MMOItem item = plugin.itemHandler.getMMOItemFromString(id.toLowerCase());
            if (item != null && item.lSpell != null) {
                item.lSpell.cast(e.getPlayer());

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
            MMOItem item = plugin.itemHandler.getMMOItemFromString(id.toLowerCase());
            if (item != null && item.activateSpell != null) {
                item.activateSpell.cast(e.getPlayer());

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
        e.getPlayer().sendMessage(ChatColor.RED + "You died and lost " + (int)(mmoPlayer.getPurseGold()/2) + " Dosh!");
        e.getPlayer().playSound(e.getPlayer(), "entity.zombie.attack_iron_door", 2000.0F, 2.0F);
        e.getPlayer().playSound(e.getPlayer(), "entity.player.death", 1000.0F, 2.0F);
    }












}
