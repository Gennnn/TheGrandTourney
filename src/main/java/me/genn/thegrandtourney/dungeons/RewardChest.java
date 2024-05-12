package me.genn.thegrandtourney.dungeons;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.shaded.effectlib.EffectManager;
import com.nisovin.magicspells.shaded.effectlib.effect.ParticleEffect;
import com.nisovin.magicspells.shaded.effectlib.effect.SmokeEffect;
import com.nisovin.magicspells.shaded.effectlib.effect.SphereEffect;
import com.nisovin.magicspells.shaded.effectlib.util.DynamicLocation;
import com.nisovin.magicspells.shaded.effectlib.util.ParticleOptions;
import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.grid.Direction;
import me.genn.thegrandtourney.item.DropTable;
import me.genn.thegrandtourney.item.MMOItem;
import me.genn.thegrandtourney.skills.TournamentObject;
import me.genn.thegrandtourney.skills.foraging.Tree;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.*;

public class RewardChest implements TournamentObject, Listener {
    TGT plugin;
    public Material middleMaterial;
    public Material baseMaterial;
    public String chestTexture;
    public DropTable drops;
    public List<ArmorStand> baseArmorStands;
    public Map<UUID, ArmorStand> chestArmorStands;
    public List<UUID> openedPlayers;
    public Map<Item, UUID> droppedItems;
    public Direction direction;
    public Location loc;
    public BukkitTask chestDisplayTask;
    public Interaction hitbox;
    public Particle chestParticle;
    public int particleCount = 1;
    Random r;

    public RewardChest(TGT plugin, Material baseMat, Material midMat, String texture64, DropTable rewardTable, Particle chestParticle, int particleCount) {
        this.plugin = plugin;
        this.baseArmorStands = new ArrayList<>();
        this.openedPlayers = new ArrayList<>();
        this.chestArmorStands = new HashMap<>();
        this.droppedItems = new HashMap<>();
        this.baseMaterial = baseMat;
        this.middleMaterial = midMat;
        this.chestTexture = texture64;
        this.drops = rewardTable;
        this.chestParticle = chestParticle;
        this.particleCount = particleCount;
        this.r = new Random();
    }

    @Override
    public void spawn(Location loc) {
        Direction dir = Direction.S;
        float yaw = loc.getYaw();
        if ((yaw > -45) && (yaw <= 45)) {
            loc.setYaw(0);
        } else if (((yaw > 135) && (yaw <= 180) ) || ((yaw <= -135)&&yaw >= -180 )) {
            dir = Direction.N;
            loc.setYaw(180);
        } else if ((yaw > 45) && (yaw <=135)) {
            //W
            dir = Direction.W;
            loc.setYaw(90);
        } else if ((yaw > -135) && (yaw <= -45)) {
            //E
            dir = Direction.E;
            loc.setYaw(-90);
        }
        this.loc = loc.toCenterLocation();
        this.direction = dir;
        createBaseStands(this.loc);
        chestDisplayTask();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void createBaseStands(Location loc) {
        baseArmorStands.add(summonArmorStand(-0.3125 ,-4.44133 ,-0.3125, 0,0,0, new ItemStack(baseMaterial), loc, direction));
        baseArmorStands.add(summonArmorStand(0.3125 ,-4.44133 ,-0.3125, 0,0,0, new ItemStack(baseMaterial), loc, direction));
        baseArmorStands.add(summonArmorStand(0.3125 ,-4.44133 ,0.3125, 0,0,0, new ItemStack(baseMaterial), loc, direction));
        baseArmorStands.add(summonArmorStand(-0.3125 ,-4.44133 ,0.3125, 0,0,0, new ItemStack(baseMaterial), loc, direction));

        baseArmorStands.add(summonArmorStand(0.3125 ,-3.97258 ,-0.3125, 0,0,0, new ItemStack(baseMaterial), loc, direction));
        baseArmorStands.add(summonArmorStand(-0.3125 ,-3.97258 ,-0.3125, 0,0,0, new ItemStack(baseMaterial), loc, direction));
        baseArmorStands.add(summonArmorStand(-0.3125 ,-3.97258 ,0.3125, 0,0,0, new ItemStack(baseMaterial), loc, direction));
        baseArmorStands.add(summonArmorStand(0.3125 ,-3.97258 ,0.3125, 0,0,0, new ItemStack(baseMaterial), loc, direction));

        baseArmorStands.add(summonSmallArmorStand(0.21875 ,-3.63918 ,-0.21875, 0,0,0, new ItemStack(middleMaterial), loc, direction));
        baseArmorStands.add(summonSmallArmorStand(0.21875 ,-3.63918 ,0.21875, 0f,0,0f, new ItemStack(middleMaterial), loc, direction));
        baseArmorStands.add(summonSmallArmorStand(-0.21875 ,-3.63918 ,0.21875, 0f,0,0f, new ItemStack(middleMaterial), loc, direction));
        baseArmorStands.add(summonSmallArmorStand(-0.21875 ,-3.63918 ,-0.21875, 0f,0,0f, new ItemStack(middleMaterial), loc, direction));

        this.hitbox = loc.getWorld().spawn(loc, Interaction.class);
        this.hitbox.setInteractionWidth(2f);
        this.hitbox.setInteractionHeight(3f);
    }

    public void chestDisplayTask() {
        this.chestDisplayTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getLocation().distance(RewardChest.this.loc) > 40) {
                        if (chestArmorStands.containsKey(player.getUniqueId())) {
                            ArmorStand armorStand = chestArmorStands.get(player.getUniqueId());
                            chestArmorStands.remove(player.getUniqueId());
                            armorStand.remove();
                        }
                    } else {
                        if (!chestArmorStands.containsKey(player.getUniqueId()) && !openedPlayers.contains(player.getUniqueId())) {
                            ItemStack item = new ItemStack(Material.PLAYER_HEAD);
                            MMOItem.getHeadFrom64(RewardChest.this.chestTexture, item);
                            ArmorStand armorStand = summonPrivateArmorStand(0.0 ,-2.9 ,0.0, 0, 0, 0, item, RewardChest.this.loc, RewardChest.this.direction, player);
                            chestArmorStands.put(player.getUniqueId(), armorStand);
                            chestAnimation(armorStand);
                        }
                    }

                }
            }
        }.runTaskTimer(plugin, 0L, 100L);
    }

    @EventHandler
    public void interactWithChest(EntityDamageByEntityEvent e) {
        if (e.getEntity() == this.hitbox && e.getDamager() instanceof Player && !this.openedPlayers.contains(e.getDamager().getUniqueId())) {
            Player player = (Player) e.getDamager();
            List<ItemStack> items = this.drops.calculateDungeonChestDrops(player);
            this.openedPlayers.add(player.getUniqueId());
            int[] counter = new int[]{0};
            player.playSound(chestArmorStands.get(player.getUniqueId()).getLocation(), Sound.BLOCK_CHEST_OPEN, 2.0f, 0.75f);
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (counter[0] == items.size()) {
                        ParticleOptions po = new ParticleOptions((float) 0.25, (float) 0.3, (float) 0.25, 0.2F,15, 1F, (Color) null, RewardChest.this.middleMaterial, (byte) 0);
                        List<Player> players = new ArrayList<>();
                        players.add(player);
                        MagicSpells.getEffectManager().display(Particle.BLOCK_CRACK, po, chestArmorStands.get(player.getUniqueId()).getLocation().clone().add(0, 1, 0), 32.0D, players);
                        player.playSound(chestArmorStands.get(player.getUniqueId()).getLocation(), Sound.BLOCK_CHEST_CLOSE, 2.0f, 0.75f);
                        player.playSound(chestArmorStands.get(player.getUniqueId()).getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 1.75f);
                        chestArmorStands.get(player.getUniqueId()).remove();
                        this.cancel();
                        return;
                    }
                    dropLoot(items.get(counter[0]), player);
                    counter[0]++;
                }
            }.runTaskTimer(plugin, 16L, 10L);

         }
    }

    public void dropLoot(ItemStack item, Player player) {

        Item droppedItem = this.loc.getWorld().dropItem(this.loc, item);
        droppedItem.setVisibleByDefault(false);
        player.showEntity(plugin, droppedItem);
        droppedItem.setVelocity(new Vector(((r.nextDouble() * 2) - 1)*0.2, 0.4, ((r.nextDouble() * 2) - 1)*0.2));
        droppedItem.setOwner(player.getUniqueId());
        this.droppedItems.put(droppedItem, player.getUniqueId());
        droppedItem.setPickupDelay(40);
        player.playSound(this.loc, Sound.ENTITY_ITEM_PICKUP, 2.0f, 1.0f);
        EffectManager em = MagicSpells.getEffectManager();
        SphereEffect effect = new SphereEffect(em);
        effect.iterations = 1;
        effect.radius = 0.1f;
        effect.particle = chestParticle;
        effect.particleCount =(int)((particleCount/2)+1);
        effect.period = 1;
        effect.speed = 0.05f;
        effect.setDynamicOrigin(new DynamicLocation(chestArmorStands.get(player.getUniqueId()).getLocation().clone().add(0, 1, 0)));
        em.start(effect);
        if (!(ChatColor.stripColor(droppedItem.getItemStack().getItemMeta().getDisplayName())).contains("XP x") && !(ChatColor.stripColor(droppedItem.getItemStack().getItemMeta().getDisplayName())).contains("Dosh x") && droppedItem.getItemStack().getAmount() > 1) {
            droppedItem.setCustomName(item.getItemMeta().getDisplayName() + " " + ChatColor.GRAY + item.getAmount() + "x");
        } else {
            droppedItem.setCustomName(item.getItemMeta().getDisplayName());
        }
        droppedItem.setCustomNameVisible(true);

    }

    @EventHandler
    public void itemPickup(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player)) {
            e.setCancelled(true);
            return;
        }
        Player player = (Player) e.getEntity();
        Item item = e.getItem();
        if (  !(ChatColor.stripColor(item.getItemStack().getItemMeta().getDisplayName())).contains("XP x") && !(ChatColor.stripColor(item.getItemStack().getItemMeta().getDisplayName())).contains("Dosh x") ) {
            if (this.droppedItems.containsKey(item)) {
                this.droppedItems.remove(item);
            }
            return;
        }
        if ((ChatColor.stripColor(item.getItemStack().getItemMeta().getDisplayName())).contains("XP x")) {
            item.remove();
            e.setCancelled(true);
            player.playSound(this.loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.25f, 1.0f);
            plugin.xpHandler.grantXp(this.drops.xpDropType, player, this.drops.xpDropMax);
            if (this.droppedItems.containsKey(item)) {
                this.droppedItems.remove(item);
            }
        } else if ((ChatColor.stripColor(item.getItemStack().getItemMeta().getDisplayName())).contains("Dosh x")) {
            item.remove();
            e.setCancelled(true);
            player.playSound(this.loc, Sound.ENTITY_ITEM_PICKUP, 0.25f, 1.0f);
            plugin.players.get(player.getUniqueId()).addPurseGold(this.drops.moneyDropMax);
            if (this.droppedItems.containsKey(item)) {
                this.droppedItems.remove(item);
            }
        }
    }

    public void chestAnimation(ArmorStand armorStand) {
        double initialHeight = armorStand.getLocation().getY();
        int[] counter = new int[]{0, 0};
        final boolean[] up = {true};
        EffectManager em = MagicSpells.getEffectManager();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!armorStand.isValid()) {
                    this.cancel();
                    return;
                }
                if (armorStand.getLocation().getY() >= initialHeight + 0.25) {
                    if (counter[0] != 5) {
                        counter[0]++;
                    } else {
                        armorStand.teleport(armorStand.getLocation().clone().add(0,-0.025,0));
                        counter[0] = 0;
                        up[0] = false;
                    }
                } else if (armorStand.getLocation().getY() <= initialHeight - 0.25) {
                    if (counter[0] != 5) {
                        counter[0]++;
                    } else {
                        armorStand.teleport(armorStand.getLocation().clone().add(0,0.025,0));
                        counter[0] = 0;
                        up[0] = true;
                    }
                } else if (up[0]) {
                    armorStand.teleport(armorStand.getLocation().clone().add(0,0.025,0));
                } else {
                    armorStand.teleport(armorStand.getLocation().clone().add(0,-0.025,0));
                }
                if (counter[1] == 5) {
                    ParticleEffect effect = new ParticleEffect(em);
                    effect.iterations = 1;
                    effect.particle = chestParticle;
                    effect.particleCount = particleCount;
                    effect.period = 1;
                    effect.speed = 0.05f;
                    effect.setDynamicOrigin(new DynamicLocation(armorStand.getLocation().clone().add(0, 1, 0)));
                    em.start(effect);
                    counter[1] = 0;
                } else {
                    counter[1]++;
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    public ArmorStand summonSmallArmorStand(double x, double y, double z, double xAngle, double yAngle, double zAngle, ItemStack item, Location loc, Direction dir) {
        return summonArmorStandHead(x,y,z,xAngle,yAngle,zAngle,true,item,loc,dir);
    }
    public ArmorStand summonArmorStand(double x, double y, double z, double xAngle, double yAngle, double zAngle, ItemStack item, Location loc, Direction dir) {
        return summonArmorStandHead(x,y,z,xAngle,yAngle,zAngle,false,item,loc,dir);
    }
    public ArmorStand summonPrivateArmorStand(double x, double y, double z, double xAngle, double yAngle, double zAngle, ItemStack item, Location loc, Direction dir, Player player) {
        return summonPrivateArmorStandHead(x,y,z,xAngle,yAngle,zAngle,false,item,loc,dir,player);
    }
    public ArmorStand summonArmorStandHead(double x, double y, double z, double xAngle, double yAngle, double zAngle, boolean small, ItemStack item, Location loc, Direction direction) {
        xAngle = Math.toRadians(xAngle);
        yAngle = Math.toRadians(yAngle);
        zAngle = Math.toRadians(zAngle);
        loc = loc.toCenterLocation();
        loc.setPitch(45);
        float yaw = loc.getYaw();
        if (direction == Direction.S) {
            loc.setYaw(0);
        } else if (direction == Direction.N) {
            double newX = -x;
            double newZ = -z;
            double newXangle = xAngle;
            double newZAngle = zAngle;
            x = newX;
            z = newZ;
            xAngle = newXangle;
            zAngle = newZAngle;
            loc.setYaw(180);
        } else if (direction == Direction.W) {
            double newX = -z;
            double newZ = x;
            x = newX;
            z = newZ;
            loc.setYaw(90);
        } else if (direction == Direction.E) {
            double newX = z;
            double newZ = -x;
            x = newX;
            z = newZ;
            loc.setYaw(-90);
        }

        ArmorStand as = (ArmorStand) loc.getWorld().spawn(loc, ArmorStand.class);

        as.setGravity(false);
        as.setMarker(true);
        as.setVisible(false);
        as.setCollidable(false);
        as.setSmall(small);
        as.teleport(as.getLocation().add(x,y+2.5,z));
        as.setHeadPose(new EulerAngle(xAngle, yAngle, zAngle));
        as.setHelmet(item);
        return as;
    }
    public ArmorStand summonPrivateArmorStandHead(double x, double y, double z, double xAngle, double yAngle, double zAngle, boolean small, ItemStack item, Location loc, Direction direction, Player player) {
        xAngle = Math.toRadians(xAngle);
        yAngle = Math.toRadians(yAngle);
        zAngle = Math.toRadians(zAngle);
        loc = loc.toCenterLocation();
        loc.setPitch(45);
        float yaw = loc.getYaw();
        if (direction == Direction.S) {
            loc.setYaw(0);
        } else if (direction == Direction.N) {
            double newX = -x;
            double newZ = -z;
            double newXangle = xAngle;
            double newZAngle = zAngle;
            x = newX;
            z = newZ;
            xAngle = newXangle;
            zAngle = newZAngle;
            loc.setYaw(180);
        } else if (direction == Direction.W) {
            double newX = -z;
            double newZ = x;
            x = newX;
            z = newZ;
            loc.setYaw(90);
        } else if (direction == Direction.E) {
            double newX = z;
            double newZ = -x;
            x = newX;
            z = newZ;
            loc.setYaw(-90);
        }

        ArmorStand as = (ArmorStand) loc.getWorld().spawn(loc, ArmorStand.class);

        as.setGravity(false);
        as.setMarker(true);
        as.setVisible(false);
        as.setCollidable(false);
        as.setSmall(small);
        as.teleport(as.getLocation().add(x,y+2.5,z));
        as.setHeadPose(new EulerAngle(xAngle, yAngle, zAngle));
        as.setHelmet(item);
        as.setVisibleByDefault(false);
        player.showEntity(plugin, as);
        return as;
    }
    @Override
    public void remove() {
        HandlerList.unregisterAll(this);
        for (ArmorStand as : baseArmorStands) {
            as.remove();
        }
        for (ArmorStand as : chestArmorStands.values()) {
            as.remove();
        }
        for (Item item : droppedItems.keySet()) {
            item.remove();
        }
        hitbox.remove();
        loc = null;
    }

    @Override
    public void paste(Location loc) {
        Direction dir = Direction.S;
        float yaw = loc.getYaw();
        if ((yaw > -45) && (yaw <= 45)) {
            loc.setYaw(0);
        } else if (((yaw > 135) && (yaw <= 180) ) || ((yaw <= -135)&&yaw >= -180 )) {
            dir = Direction.N;
            loc.setYaw(180);
        } else if ((yaw > 45) && (yaw <=135)) {
            //W
            dir = Direction.W;
            loc.setYaw(90);
        } else if ((yaw > -135) && (yaw <= -45)) {
            //E
            dir = Direction.E;
            loc.setYaw(-90);
        }
        this.loc = loc;
        this.direction = dir;
        createBaseStands(this.loc);
        chestDisplayTask();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    public void paste(Location loc, Direction dir) {

        this.loc = loc;
        this.direction = dir;
        createBaseStands(this.loc);
        chestDisplayTask();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
}
