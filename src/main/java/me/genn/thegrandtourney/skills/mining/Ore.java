package me.genn.thegrandtourney.skills.mining;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.mana.ManaChangeReason;
import com.nisovin.magicspells.shaded.effectlib.util.ParticleOptions;
import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.item.MMOItem;
import me.genn.thegrandtourney.player.CritDamageIndicator;
import me.genn.thegrandtourney.player.MMOPlayer;
import me.genn.thegrandtourney.player.NormalDamageIndicator;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class Ore implements Listener {
    Interaction entity;
    OreTemplate template;
    List<ArmorStand> resourceStands;
    float width;
    float height;
    float health;
    TGT plugin;
    ItemStack resource;
    MMOItem drop;
    int regenTime;
    int regenRate;
    int staminaCost;
    float maxHealth;
    public double criticalAngle;
    Random r;
    long lastCritPointRefresh;

    public Ore(TGT plugin, OreTemplate template) {
        this.resourceStands = new ArrayList<>();
        this.width = template.width;
        this.height = template.height;
        this.health = template.health;
        this.maxHealth = template.health;
        this.plugin = plugin;
        this.resource = template.resource;
        this.drop = template.drop;
        this.template = template;
        this.regenTime = template.regenTime;
        this.staminaCost = template.staminaCost;
        this.regenRate = template.regenRate;
        this.r = new Random();
    }

    public void spawn(Location loc) {
        Interaction in = loc.getWorld().spawn(loc, Interaction.class);
        in.setInteractionWidth(this.width);
        in.setInteractionHeight(this.height);
        this.entity = in;
        this.criticalAngle = r.nextInt(12);
        this.lastCritPointRefresh = System.currentTimeMillis();
        spawnHealthbar();
        oreTest(loc);
        plugin.listener.ores.put(this.entity, this);
        new BukkitRunnable() {

            @Override
            public void run() {
                if (Ore.this.health <= 0) {
                    this.cancel();
                    return;
                }
                if (Ore.this.health == Ore.this.maxHealth) {
                    return;
                }
                if (Ore.this.health + Ore.this.regenRate >= Ore.this.maxHealth) {
                    Ore.this.health = Ore.this.maxHealth;
                    return;
                }
                Ore.this.health = Ore.this.health + Ore.this.regenRate;

            }
        }.runTaskTimer(plugin, 0L, 40L);
    }
    public void spawnHealthbar() {
        ArmorStand as = (ArmorStand) entity.getLocation().getWorld().spawn(new Location(entity.getLocation().getWorld(), entity.getLocation().getX(), entity.getLocation().getY() - 0.5D, entity.getLocation().getZ()), ArmorStand.class);
        as.setGravity(false);
        as.setCustomName(template.displayName);
        as.setCustomNameVisible(true);
        as.setVisible(false);
        as.setMarker(true);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!Ore.this.entity.isValid() || !(Ore.this.health > 0)) {
                    cancel();
                    as.remove();
                    return;
                }
                as.teleport(Ore.this.entity.getLocation().clone().add(0, height, 0));
                as.setCustomName(oreName(Ore.this, template.displayName));
            }
        }.runTaskTimer(plugin, 0L, 1L);

    }
    public String oreName(Ore ore, String name) {
        String str = ChatColor.RED.toString() + name;
        if (ore.health == ore.maxHealth) {
            str = str + " " + ChatColor.GREEN.toString();
        } else {
            str = str + " " + ChatColor.YELLOW.toString();
        }
        str = str + (int)ore.health + ChatColor.WHITE.toString() + "/" + ChatColor.GREEN.toString() + (int)ore.maxHealth + ChatColor.RED.toString() + "❤";
        return str;
    }
    public void oreTest(Location loc) {
        //summonArmorStandHead(-0.318964 ,-4.77656 ,-0.09264800000000001, -15, -60, 0, false, base, loc);
        //summonArmorStandHead(0.293257,-4.68843,0.056036,-15,30,20, false, base, loc);
        //summonArmorStandHead(0.08071999999999999 ,-4.79242 ,0.48845,20,-30,25, false, base, loc);
        /*loc.getBlock().setType(base.getType());
        summonArmorStandHead(0.35 ,-4.62883 ,-0.35,0,135,0,false,base,loc);
        summonArmorStandHead(0.0 ,-4.44133 ,-0.5,0,0,0,false,base,loc);
        summonArmorStandHead(-0.35 ,-4.62883 ,-0.35,0,-45,0,false,base,loc);
        summonArmorStandHead(-0.5 ,-4.44133 ,0.0,0,0,0,false,base,loc);
        summonArmorStandHead(0.0 ,-4.44133 ,0.5,0,0,0,false,base,loc);
        summonArmorStandHead(0.5 ,-4.44133 ,0.0,0,0,0,false,base,loc);
        summonArmorStandHead(-0.35 ,-4.62883 ,0.35,0,45,0,false,base,loc);
        summonArmorStandHead(0.35 ,-4.62883 ,0.35,0,-135,0,false,base,loc);*/

        summonArmorStandHead(0.31475 ,-3.63666 ,-0.384673,-25,60,0,false,resource,loc);
        summonArmorStandHead(-0.573 ,-3.245 ,-0.432,20,125,0,false,resource,loc);
        summonArmorStandHead(-0.395951 ,-3.48041 ,0.137903,25,80,0,false,resource,loc);
        summonArmorStandHead(-0.106653 ,-3.63666 ,0.458451,25,10,0,false,resource,loc);
        summonArmorStandHead(0.413788 ,-3.1 ,0.246999,160,120,0,false,resource,loc);
        summonArmorStandHead(0.013456000000000003 ,-3.05 ,0.070042,0,200,35,false,resource,loc);
        summonArmorStandHead(-0.125 ,-2.02 ,0.336527,35,35,0,true,resource,loc);
        summonArmorStandHead(0.2 ,-2.15 ,-0.237,185,155,0,true,resource,loc);
        summonArmorStandHead(-0.123 ,-2.935 ,-0.328,315,175,0,true,resource,loc);
        //summonArmorStandHead(-0.097452 ,-3.87968 ,0.589221,25,-25,-45,true,base,loc);
        //summonArmorStandHead(0.592344 ,-4.03487 ,0.293562,35,-60,-20,true,base,loc);
        //summonArmorStandHead(0.020471 ,-3.49715 ,-0.076709,-45,-50,-135,true,base,loc);
        //summonArmorStandHead(-0.807573 ,-3.82628 ,0.238473,-35,35,0,true,base,loc);
        //summonArmorStandHead(0.151554 ,-3.30168 ,0.0,0,-45,-60,true,resource,loc);
        //summonArmorStandHead(0.059854 ,-3.53488 ,0.53125,-80,-90,60,true,resource,loc);
        //summonArmorStandHead(0.301348 ,-3.53577 ,0.22500599999999998,30,-50,-20,true,resource,loc);
        //summonArmorStandHead(-0.476797 ,-3.44012 ,0.364796,45,115,95,true,resource,loc);
        //summonArmorStandHead(0.030388 ,-3.26152 ,0.3125,0,15,-10,true,resource,loc);
    }



    public void summonArmorStandHead(double x, double y, double z, double xAngle, double yAngle, double zAngle, boolean small, ItemStack item, Location loc) {
        loc = loc.toCenterLocation();
        loc.setPitch(45);
        float yaw = loc.getYaw();
        if ((yaw > -45) && (yaw <= 45)) {

        } else if (((yaw > 135) && (yaw <= 180) ) || ((yaw <= -135)&&yaw >= -180 )) {
            double newX = -x;
            double newZ = -z;
            double newXangle = -xAngle;
            double newZAngle = -zAngle;
            x = newX;
            z = newZ;
            xAngle = newXangle;
            zAngle = newZAngle;
        } else if ((yaw > 45) && (yaw <=135)) {
            double newX = -z;
            double newZ = x;
            double newXangle = -zAngle;
            double newZAngle = xAngle;
            x = newX;
            z = newZ;
            xAngle = newXangle;
            zAngle = newZAngle;
        } else if ((yaw > -135) && (yaw <= -45)) {
            double newX = z;
            double newZ = -x;
            double newXangle = zAngle;
            double newZAngle = -xAngle;
            x = newX;
            z = newZ;
            //xAngle = newXangle;
            //zAngle = newZAngle;
        }

        ArmorStand as = (ArmorStand) loc.getWorld().spawn(loc, ArmorStand.class);
        as.setGravity(false);
        as.setMarker(true);
        as.setVisible(false);
        as.setCollidable(true);
        as.setSmall(small);
        as.teleport(as.getLocation().add(x,y+1.5,z));
        as.setHeadPose(new EulerAngle(xAngle, yAngle, zAngle));
        as.setHelmet(item);
        if (item.getType() == resource.getType()) {
            this.resourceStands.add(as);
        }
    }


    @EventHandler (priority = EventPriority.MONITOR)
    public void onDamageEntity(EntityDamageByEntityEvent e) {
        if (e.getEntity() == this.entity) {
            if (this.health <= 0) {
                e.setCancelled(true);
                return;
            }

            if (e.getDamager() instanceof Player) {
                Player p = (Player) e.getDamager();
                MMOPlayer player = plugin.players.get(p.getUniqueId());
                Location target = e.getEntity().getLocation();

                target.add(1.35 * r.nextDouble() * (1 - (r.nextDouble()*2)), 0.8 * r.nextDouble(), 1.35 * r.nextDouble() * (1 - (r.nextDouble()*2)));
                if (p.getAttackCooldown() != 1.0) {
                    e.setCancelled(true);
                    return;
                }
                ParticleOptions po = new ParticleOptions((float) 0.25, (float) 0.3, (float) 0.25, 0.2F,8, 1F, (Color) null,resource.getType(), (byte) 0);
                List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
                if (!MagicSpells.getManaHandler().hasMana(p, staminaCost)) {
                    MagicSpells.getEffectManager().display(Particle.BLOCK_CRACK, po, entity.getLocation(), 32.0D, players);
                    p.playSound(this.entity.getLocation(), "block.anvil.place",0.5F, 2F);
                    e.setCancelled(true);
                    return;
                }
                double angle = testForPlayerEyes(p);
                boolean crit = false;
                if (!e.isCancelled()) {
                    p.playSound(this.entity.getLocation(), "block.anvil.place",0.75F, 1.25F);
                    p.playSound(this.entity.getLocation(), "block.stone.hit",1.0F, 0.5F);
                }

                if ((30*(this.criticalAngle+1)) > angle && angle >= (30*this.criticalAngle)) {
                    crit = true;
                }
                double damage = plugin.calculateDamagePickaxe(player, p.getItemInHand(), crit);
                if (template.defense != 0) {
                    damage = plugin.calculateDefenseDamage((float)template.defense, (float)damage);
                }
                if (this.lastCritPointRefresh + TimeUnit.SECONDS.toMillis(this.template.critPointRegenTime) <= System.currentTimeMillis() && crit) {
                    this.lastCritPointRefresh = System.currentTimeMillis();
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Iterator iter = Bukkit.getOnlinePlayers().iterator();
                            boolean pass = false;
                            while (iter.hasNext()) {
                                Player p = (Player) iter.next();
                                if ((p.getLocation().distanceSquared(Ore.this.entity.getLocation()) <= 40) && Ore.this.health < Ore.this.maxHealth) {
                                    pass = true;
                                }
                            }
                            if (pass) {
                                Ore.this.criticalAngle = Ore.this.r.nextInt(12);
                            }
                        }
                    }.runTaskLater(plugin, this.template.critPointRegenTime * 20L);
                    final int[] counter = {0};
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (health <= 0) {
                                this.cancel();
                                return;
                            }
                            counter[0]++;
                            if (counter[0] > 4) {
                                this.cancel();
                                return;
                            }
                            MagicSpells.getEffectManager().display(Particle.REDSTONE, entity.getLocation(),1.0F - ((counter[0]+1)*0.15F),0.3F,1.0F - ((counter[0]+1)*0.15F),0.1F,10 * (counter[0]+1), 1.2F,Color.fromRGB(6,90+(counter[0]*17),204),(Material) null, (byte)0, 20.0D, players);
                            Ore.this.entity.getLocation().getWorld().playSound(Ore.this.entity.getLocation(), "entity.guardian.ambient", 0.5F + ((counter[0]+1)*0.2F), 1.4F + ((counter[0]+1)*0.1F));
                        }
                    }.runTaskTimer(plugin, (this.template.critPointRegenTime*20L)-50L,10L);
                }

                po.amount = 25;
                po.speed = 0.35F;
                MagicSpells.getEffectManager().display(Particle.BLOCK_CRACK, po, entity.getLocation(), 32.0D, players);
                double coords[] = lerp3D(0.5, p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ(), entity.getLocation().getX(), entity.getLocation().getY(), entity.getLocation().getZ());
                Location middlePoint = new Location(p.getWorld(), coords[0], coords[1] + 0.2, coords[2]);
                if (!e.isCancelled()) {
                    if (crit) {
                        MagicSpells.getEffectManager().display(Particle.CRIT_MAGIC, middlePoint, 0.1F, 0F, 0.1F, 0.25F, 10, 8F, (Color) null, (Material) null, (byte) 0, 20.0D, players);
                        Ore.this.entity.getLocation().getWorld().playSound(p, "entity.vex.hurt", 1.5F, 2F);
                        p.playSound(p, "block.anvil.place", 0.25F, 0.75F);
                        plugin.listener.spectralDamage.spawnDamageIndicator(p, target, new CritDamageIndicator(), (int)damage);
                    } else {
                        MagicSpells.getEffectManager().display(Particle.CRIT, middlePoint, 0.1F, 0F, 0.1F, 0.25F, 10, 8F, (Color) null, (Material) null, (byte) 0, 20.0D, players);
                        plugin.listener.spectralDamage.spawnDamageIndicator(p, target, new NormalDamageIndicator(), (int)damage);
                    }
                    MagicSpells.getManaHandler().removeMana(p, staminaCost, ManaChangeReason.OTHER);
                    e.setDamage((int)damage);
                }
                this.health = (float) (this.health - e.getDamage());

                if (this.health <= 0) {
                    po.amount = 50;
                    po.speed = 0.55F;

                    MagicSpells.getEffectManager().display(Particle.BLOCK_CRACK, po, entity.getLocation(), 32.0D, players);
                    Iterator asIter = this.resourceStands.iterator();
                    do {
                        ArmorStand as = (ArmorStand) asIter.next();
                        as.setHelmet(new ItemStack(Material.AIR));
                    } while (asIter.hasNext());
                    p.getInventory().addItem(plugin.itemHandler.getItem(drop));
                    Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                        @Override
                        public void run() {
                            Iterator asIter = Ore.this.resourceStands.iterator();
                            do {
                                ArmorStand as = (ArmorStand) asIter.next();
                                as.setHelmet(resource);

                            } while (asIter.hasNext());
                            MagicSpells.getEffectManager().display(Particle.VILLAGER_HAPPY, entity.getLocation(),0.4F,0.2F,0.4F,0.25F,50,8F,(Color)null,(Material) null, (byte)0, 20.0D, players);
                            MagicSpells.getEffectManager().display(Particle.BLOCK_CRACK, po, entity.getLocation(), 32.0D, players);
                            Ore.this.health = Ore.this.maxHealth;
                            Ore.this.entity.getLocation().getWorld().playSound(Ore.this.entity.getLocation(), "entity.player.attack.crit", 1.5F, 2.0F);
                            Ore.this.entity.getLocation().getWorld().playSound(Ore.this.entity.getLocation(), "block.stone.break", 1F, 1F);
                            Ore.this.entity.getLocation().getWorld().playSound(Ore.this.entity.getLocation(), "block.stone.place", 2F, 0F);
                            spawnHealthbar();
                        }
                    }, 20L * regenTime);
                    new BukkitRunnable() {

                        @Override
                        public void run() {
                            if (Ore.this.health <= 0) {
                                this.cancel();
                                return;
                            }
                            if (Ore.this.health == Ore.this.maxHealth) {
                                return;
                            }
                            if (Ore.this.health + Ore.this.regenRate >= Ore.this.maxHealth) {
                                Ore.this.health = Ore.this.maxHealth;
                                return;
                            }
                            Ore.this.health = Ore.this.health + Ore.this.regenRate;

                        }
                    }.runTaskTimer(plugin, 20L * regenTime, 40L);
                    new BukkitRunnable() {
                        int counter = 0;
                        @Override
                        public void run() {
                            counter++;
                            if (counter > 10) {
                                this.cancel();
                                return;
                            }
                            Ore.this.entity.getLocation().getWorld().playSound(Ore.this.entity.getLocation(), "block.stone.hit", 2F, 0.5F);
                            MagicSpells.getEffectManager().display(Particle.BLOCK_CRACK, entity.getLocation().add(0D, -0.25D, 0.0D), 0.25F, 0.00F, 0.25F, 0.2F, 15, 1F, (Color)null, resource.getType(), (byte)0, 20.0D, players);

                        }
                    }.runTaskTimer(plugin, (20L * regenTime) - 47L, 5L);
                }
            }
        }
    }

    public String getName() {
        return template.name;
    }
    public static double[] lerp3D(double amount, double x1, double y1, double z1, double x2, double y2, double z2)
    {
        return new double[]{ x1+(x2-x1)*amount, y1+(y2-y1)*amount, z1+(z2-z1)*amount };
    }

    public double testForPlayerEyes(Player player) {
        Location pEyes = player.getLocation();
        Location oreLoc = this.entity.getLocation();
        double dx = pEyes.getX() - oreLoc.getX();
        double dz = pEyes.getZ() - oreLoc.getZ();
        double angle = Math.atan2(dz,dx);
        if (angle < 0) {
            angle += Math.PI*2;
        }

        return angle*(180/Math.PI);
    }
}
