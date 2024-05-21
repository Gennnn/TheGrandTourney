package me.genn.thegrandtourney.skills.foraging;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.shaded.effectlib.EffectManager;
import com.nisovin.magicspells.shaded.effectlib.util.DynamicLocation;
import com.nisovin.magicspells.shaded.effectlib.util.ParticleOptions;
import me.genn.thegrandtourney.TGT;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

import com.nisovin.magicspells.shaded.effectlib.effect.CubeEffect;

public class Tree {
    List<Block> logBlocks;
    List<Block> choppableBlocks;
    List<Block> leafBlocks;
    List<Block> fruitableBlocks;
    ForagingZone zone;
    Block startingBlock;
    public double health;
    public double maxHealth;
    public int regenRate;
    Block criticalBlock;
    TGT plugin;
    Random r;
    BukkitTask critIndicatorTask;
    int fruitSpawned = 0;
    Map<Block, ArmorStand> fruits;
    Map<Block, List<BlockFace>> fruitableFaces;
    BukkitTask fruitSpawnTask;

    BukkitTask healthRegenTask;

    long lastActionTime;


    public Tree(ForagingZone zone, TGT plugin) {
        this.logBlocks = new ArrayList<>();
        this.leafBlocks = new ArrayList<>();
        this.choppableBlocks = new ArrayList<>();
        this.fruitableBlocks = new ArrayList<>();
        this.fruits = new HashMap<>();
        this.zone = zone;
        this.health = zone.template.health;
        this.maxHealth = this.health;
        this.plugin = plugin;
        this.regenRate = zone.template.regenRate;
        this.r = new Random();
        this.fruitableFaces = new HashMap<>();
        this.lastActionTime = System.currentTimeMillis();
    }

    public void create(Block startingBlock) {
        this.startingBlock = startingBlock;
        Location startingLoc = startingBlock.getLocation();
        Block block = startingLoc.clone().add(1,0,0).getBlock();
        if (zone.template.logBlocks.contains(block.getType()) && !logBlocks.contains(block) && !zone.currentlyLoggedBlocks.contains(block)) {
            logBlocks.add(block);
            zone.currentlyLoggedBlocks.add(block);
            branch(block, startingLoc);
            if (startingLoc.distance(block.getLocation()) < 6 && checkIfBlockHasExposedAir(block)) {
                choppableBlocks.add(block);
            }
        }
        block = startingLoc.clone().add(-1,0,0).getBlock();
        if (zone.template.logBlocks.contains(block.getType()) && !logBlocks.contains(block) && !zone.currentlyLoggedBlocks.contains(block)) {
            logBlocks.add(block);
            zone.currentlyLoggedBlocks.add(block);
            branch(block, startingLoc);
            if (startingLoc.distance(block.getLocation()) < 6&& checkIfBlockHasExposedAir(block)) {
                choppableBlocks.add(block);
            }
        }
        block = startingLoc.clone().add(0,1,0).getBlock();
        if (zone.template.logBlocks.contains(block.getType()) && !logBlocks.contains(block) && !zone.currentlyLoggedBlocks.contains(block)) {
            logBlocks.add(block);
            zone.currentlyLoggedBlocks.add(block);
            branch(block, startingLoc);
            if (startingLoc.distance(block.getLocation()) < 6&& checkIfBlockHasExposedAir(block)) {
                choppableBlocks.add(block);
            }
        }
        block = startingLoc.clone().add(0,-1,0).getBlock();
        if (zone.template.logBlocks.contains(block.getType()) && !logBlocks.contains(block) && !zone.currentlyLoggedBlocks.contains(block)) {
            logBlocks.add(block);
            zone.currentlyLoggedBlocks.add(block);
            branch(block, startingLoc);
            if (startingLoc.distance(block.getLocation()) < 6&& checkIfBlockHasExposedAir(block)) {
                choppableBlocks.add(block);
            }
        }
        block = startingLoc.clone().add(0,0,1).getBlock();
        if (zone.template.logBlocks.contains(block.getType()) && !logBlocks.contains(block) && !zone.currentlyLoggedBlocks.contains(block)) {
            logBlocks.add(block);
            zone.currentlyLoggedBlocks.add(block);
            branch(block, startingLoc);
            if (startingLoc.distance(block.getLocation()) < 6&& checkIfBlockHasExposedAir(block)) {
                choppableBlocks.add(block);
            }
        }
        block = startingLoc.clone().add(0,0,-1).getBlock();
        if (zone.template.logBlocks.contains(block.getType()) && !logBlocks.contains(block) && !zone.currentlyLoggedBlocks.contains(block)) {
            logBlocks.add(block);
            zone.currentlyLoggedBlocks.add(block);
            branch(block, startingLoc);
            if (startingLoc.distance(block.getLocation()) < 6 && checkIfBlockHasExposedAir(block)) {
                choppableBlocks.add(block);
            }
        }
        leavesMarking();
        this.fruitSpawnTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (r.nextInt(2) == 0) {
                    if (Tree.this.fruitSpawned >= (Tree.this.fruitableBlocks.size() * 0.5) || Tree.this.health <= 0 || Tree.this.fruitableBlocks.size() < 1) {
                        return;
                    }
                    Block leafBlock = Tree.this.fruitableBlocks.get(r.nextInt(Tree.this.fruitableBlocks.size()));
                    if (Tree.this.fruits.containsKey(leafBlock)) {
                        return;
                    }
                    Location offsetLoc = getOffsetLoc(leafBlock);
                    if (offsetLoc == null) {
                        return;
                    }
                    offsetLoc = offsetLoc.add(0,-0.5,0);
                    ArmorStand as = leafBlock.getLocation().getWorld().spawn(offsetLoc, ArmorStand.class);
                    as.setVisible(false);
                    as.setMarker(true);
                    as.setGravity(false);
                    as.setCollidable(false);
                    as.setSmall(true);
                    as.setHelmet(zone.template.fruitItem);
                    as.setBodyYaw(r.nextInt(360));
                    Tree.this.fruits.put(leafBlock, as);
                    ParticleOptions po = new ParticleOptions((float) 0.2, (float) 0.3, (float) 0.2, 0.2F,5, 1F, (Color) null,null, (byte) 0);
                    List<Player> targetP = new ArrayList<>(Bukkit.getOnlinePlayers());
                    MagicSpells.getEffectManager().display(Particle.VILLAGER_HAPPY, po, as.getLocation(), 32.0D, targetP);
                    as.getLocation().getWorld().playSound(as.getLocation(), "block.grass.break", 1.0f, 2.0f);
                    fruitSpawned++;
                }
            }
        }.runTaskTimer(plugin, 0L, 104L);
        this.healthRegenTask = new BukkitRunnable() {

            @Override
            public void run() {
                if (Tree.this.health <= 0) {
                    return;
                }
                if (Tree.this.health == Tree.this.maxHealth) {
                    return;
                }
                if (Tree.this.health + Tree.this.regenRate >= Tree.this.maxHealth) {
                    Tree.this.health = Tree.this.maxHealth;
                    return;
                }
                Tree.this.health = Tree.this.health + Tree.this.regenRate;

            }
        }.runTaskTimer(plugin, 0L, 40L);
        Bukkit.broadcastMessage("Registered tree with base at " + startingLoc);
    }

    public Location getOffsetLoc(Block block) {
        if (!this.fruitableFaces.keySet().contains(block)) {
            return null;
        }
        List<BlockFace> faces = this.fruitableFaces.get(block);
        BlockFace face;
        if (faces.size() > 0) {
            face = faces.get(r.nextInt(faces.size()));
        } else {
            face = faces.get(0);
        }
        if (face == BlockFace.NORTH) {
            return block.getLocation().toCenterLocation().clone().add(-0.3 + r.nextDouble(0,0.3), -0.3 + r.nextDouble(0,0.3),-r.nextDouble(0.1,0.45));
        } else if (face == BlockFace.SOUTH) {
            return block.getLocation().toCenterLocation().clone().add(-0.3 + r.nextDouble(0,0.3), -0.3 + r.nextDouble(0,0.3),r.nextDouble(0.1,0.45));
        } else if (face == BlockFace.EAST) {
            return block.getLocation().toCenterLocation().clone().add(r.nextDouble(0.1,0.45), -0.3 + r.nextDouble(0,0.3),-0.3 + r.nextDouble(0.3));
        } else if (face == BlockFace.WEST) {
            return block.getLocation().toCenterLocation().clone().add(-r.nextDouble(0.1,0.45), -0.3 + r.nextDouble(0,0.3),-0.3 + r.nextDouble(0.3));
        } else if (face == BlockFace.UP) {
            return block.getLocation().toCenterLocation().clone().add(-0.3 + r.nextDouble(0,0.3), r.nextDouble(0.1,0.45),-0.3 + r.nextDouble(0.3));
        } else if (face == BlockFace.DOWN) {
            return block.getLocation().toCenterLocation().clone().add(-0.3 + r.nextDouble(0,0.3), -r.nextDouble(0.1,0.45),-0.3 + r.nextDouble(0.3));
        }
        else return null;
    }

    public void branch(Block branchedBlock, Location startingLoc) {
        Location branchLoc = branchedBlock.getLocation();
        Block block = branchLoc.clone().add(1,0,0).getBlock();
        if (zone.template.logBlocks.contains(block.getType()) && !logBlocks.contains(block) && !zone.currentlyLoggedBlocks.contains(block)) {
            logBlocks.add(block);
            zone.currentlyLoggedBlocks.add(block);
            branch(block, startingLoc);
            if (startingLoc.distance(block.getLocation()) < 5 && checkIfBlockHasExposedAir(block)) {
                choppableBlocks.add(block);
            }
        }
        block = branchLoc.clone().add(-1,0,0).getBlock();
        if (zone.template.logBlocks.contains(block.getType()) && !logBlocks.contains(block) && !zone.currentlyLoggedBlocks.contains(block)) {
            logBlocks.add(block);
            zone.currentlyLoggedBlocks.add(block);
            branch(block, startingLoc);
            if (startingLoc.distance(block.getLocation()) < 5 && checkIfBlockHasExposedAir(block)) {
                choppableBlocks.add(block);
            }
        }
        block = branchLoc.clone().add(0,1,0).getBlock();
        if (zone.template.logBlocks.contains(block.getType()) && !logBlocks.contains(block) && !zone.currentlyLoggedBlocks.contains(block)) {
            logBlocks.add(block);
            zone.currentlyLoggedBlocks.add(block);
            branch(block, startingLoc);
            if (startingLoc.distance(block.getLocation()) < 5 && checkIfBlockHasExposedAir(block)) {
                choppableBlocks.add(block);
            }
        }
        block = branchLoc.clone().add(0,-1,0).getBlock();
        if (zone.template.logBlocks.contains(block.getType()) && !logBlocks.contains(block) && !zone.currentlyLoggedBlocks.contains(block)) {
            logBlocks.add(block);
            zone.currentlyLoggedBlocks.add(block);
            branch(block, startingLoc);
            if (startingLoc.distance(block.getLocation()) < 5 && checkIfBlockHasExposedAir(block)) {
                choppableBlocks.add(block);
            }
        }
        block = branchLoc.clone().add(0,0,1).getBlock();
        if (zone.template.logBlocks.contains(block.getType()) && !logBlocks.contains(block) && !zone.currentlyLoggedBlocks.contains(block)) {
            logBlocks.add(block);
            zone.currentlyLoggedBlocks.add(block);
            branch(block, startingLoc);
            if (startingLoc.distance(block.getLocation()) < 5 && checkIfBlockHasExposedAir(block)) {
                choppableBlocks.add(block);
            }
        }
        block = branchLoc.clone().add(0,0,-1).getBlock();
        if (zone.template.logBlocks.contains(block.getType()) && !logBlocks.contains(block) && !zone.currentlyLoggedBlocks.contains(block)) {
            logBlocks.add(block);
            zone.currentlyLoggedBlocks.add(block);
            branch(block, startingLoc);
            if (startingLoc.distance(block.getLocation()) < 5 && checkIfBlockHasExposedAir(block)) {
                choppableBlocks.add(block);
            }
        }
    }

    public void leavesMarking() {
        for (Block startBlock : logBlocks) {
            Location startBlockLoc = startBlock.getLocation();
            Block block = startBlockLoc.clone().add(1,0,0).getBlock();
            if (zone.template.leafBlocks.contains(block.getType()) && !leafBlocks.contains(block) && !zone.currentlyLoggedBlocks.contains(block)) {

                if (startBlockLoc.distance(block.getLocation()) < 5) {
                    doFaceChecking(block);
                    zone.currentlyLoggedBlocks.add(block);
                    leavesBranch(block, startBlockLoc);
                    leafBlocks.add(block);
                }
            }
            block = startBlockLoc.clone().add(-1,0,0).getBlock();
            if (zone.template.leafBlocks.contains(block.getType()) && !leafBlocks.contains(block) && !zone.currentlyLoggedBlocks.contains(block)) {

                if (startBlockLoc.distance(block.getLocation()) < 5) {
                    doFaceChecking(block);
                    zone.currentlyLoggedBlocks.add(block);
                    leavesBranch(block, startBlockLoc);
                    leafBlocks.add(block);
                }
            }
            block = startBlockLoc.clone().add(0,1,0).getBlock();
            if (zone.template.leafBlocks.contains(block.getType()) && !leafBlocks.contains(block) && !zone.currentlyLoggedBlocks.contains(block)) {

                if (startBlockLoc.distance(block.getLocation()) < 5) {
                    doFaceChecking(block);
                    zone.currentlyLoggedBlocks.add(block);
                    leavesBranch(block, startBlockLoc);
                    leafBlocks.add(block);
                }
            }
            block = startBlockLoc.clone().add(0,-1,0).getBlock();
            if (zone.template.leafBlocks.contains(block.getType()) && !leafBlocks.contains(block) && !zone.currentlyLoggedBlocks.contains(block)) {

                if (startBlockLoc.distance(block.getLocation()) < 5) {
                    doFaceChecking(block);
                    zone.currentlyLoggedBlocks.add(block);
                    leavesBranch(block, startBlockLoc);
                    leafBlocks.add(block);
                }
            }
            block = startBlockLoc.clone().add(0,0,1).getBlock();
            if (zone.template.leafBlocks.contains(block.getType()) && !leafBlocks.contains(block) && !zone.currentlyLoggedBlocks.contains(block)) {

                if (startBlockLoc.distance(block.getLocation()) < 5) {
                    doFaceChecking(block);
                    zone.currentlyLoggedBlocks.add(block);
                    leavesBranch(block, startBlockLoc);
                    leafBlocks.add(block);
                }
            }
            block = startBlockLoc.clone().add(0,0,-1).getBlock();
            if (zone.template.leafBlocks.contains(block.getType()) && !leafBlocks.contains(block) && !zone.currentlyLoggedBlocks.contains(block)) {

                if (startBlockLoc.distance(block.getLocation()) < 5) {
                    doFaceChecking(block);
                    zone.currentlyLoggedBlocks.add(block);
                    leavesBranch(block, startBlockLoc);
                    leafBlocks.add(block);
                }
            }
        }
    }
    public void doFaceChecking(Block block) {
        List<BlockFace> faceList = new ArrayList();
        if (block.getLocation().clone().add(1,0,0).getBlock().getType() == Material.AIR) {
            faceList.add(BlockFace.EAST);
        } else if (block.getLocation().clone().add(-1,0,0).getBlock().getType() == Material.AIR) {
            faceList.add(BlockFace.WEST);
        } else if (block.getLocation().clone().add(0,-1,0).getBlock().getType() == Material.AIR) {
            faceList.add(BlockFace.DOWN);
        } else if (block.getLocation().clone().add(0,0,1).getBlock().getType() == Material.AIR) {
            faceList.add(BlockFace.SOUTH);
        } else if (block.getLocation().clone().add(0,0,-1).getBlock().getType() == Material.AIR) {
            faceList.add(BlockFace.NORTH);
        }
        if (faceList.size() > 0) {
            this.fruitableFaces.put(block, faceList);
            this.fruitableBlocks.add(block);
        }

    }
    public void leavesBranch(Block startBlock, Location startBlockLoc) {
        Location branchBlockLoc = startBlock.getLocation();
        Block block = branchBlockLoc.clone().add(1,0,0).getBlock();
        if (zone.template.leafBlocks.contains(block.getType()) && !leafBlocks.contains(block) && !zone.currentlyLoggedBlocks.contains(block)) {

            if (startBlockLoc.distance(block.getLocation()) < 5) {
                doFaceChecking(block);
                zone.currentlyLoggedBlocks.add(block);
                leavesBranch(block, startBlockLoc);
                leafBlocks.add(block);
            }
        }
        block = branchBlockLoc.clone().add(-1,0,0).getBlock();
        if (zone.template.leafBlocks.contains(block.getType()) && !leafBlocks.contains(block) && !zone.currentlyLoggedBlocks.contains(block)) {

            if (startBlockLoc.distance(block.getLocation()) < 5) {
                doFaceChecking(block);
                zone.currentlyLoggedBlocks.add(block);
                leavesBranch(block, startBlockLoc);
                leafBlocks.add(block);
            }
        }
        block = branchBlockLoc.clone().add(0,1,0).getBlock();
        if (zone.template.leafBlocks.contains(block.getType()) && !leafBlocks.contains(block) && !zone.currentlyLoggedBlocks.contains(block)) {

            if (startBlockLoc.distance(block.getLocation()) < 5) {
                doFaceChecking(block);
                zone.currentlyLoggedBlocks.add(block);
                leavesBranch(block, startBlockLoc);
                leafBlocks.add(block);
            }
        }
        block = branchBlockLoc.clone().add(0,-1,0).getBlock();
        if (zone.template.leafBlocks.contains(block.getType()) && !leafBlocks.contains(block) && !zone.currentlyLoggedBlocks.contains(block)) {

            if (startBlockLoc.distance(block.getLocation()) < 5) {
                doFaceChecking(block);
                zone.currentlyLoggedBlocks.add(block);
                leavesBranch(block, startBlockLoc);
                leafBlocks.add(block);
            }
        }
        block = branchBlockLoc.clone().add(0,0,1).getBlock();
        if (zone.template.leafBlocks.contains(block.getType()) && !leafBlocks.contains(block) && !zone.currentlyLoggedBlocks.contains(block)) {

            if (startBlockLoc.distance(block.getLocation()) < 5) {
                doFaceChecking(block);
                zone.currentlyLoggedBlocks.add(block);
                leavesBranch(block, startBlockLoc);
                leafBlocks.add(block);
            }
        }
        block = branchBlockLoc.clone().add(0,0,-1).getBlock();
        if (zone.template.leafBlocks.contains(block.getType()) && !leafBlocks.contains(block) && !zone.currentlyLoggedBlocks.contains(block)) {

            if (startBlockLoc.distance(block.getLocation()) < 5) {
                doFaceChecking(block);
                zone.currentlyLoggedBlocks.add(block);
                leavesBranch(block, startBlockLoc);
                leafBlocks.add(block);
            }
        }
    }

    public boolean isBlockOnTree(Block block) {
        return this.choppableBlocks.contains(block);
    }

    public void cycleCriticalBlock() {
        if (this.critIndicatorTask != null) {
            this.critIndicatorTask.cancel();
        }

        this.criticalBlock = this.choppableBlocks.get(r.nextInt(this.choppableBlocks.size()));
        this.critIndicatorTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!(Tree.this.health > 0)) {
                    cancel();
                    return;
                }
                if (System.currentTimeMillis() > Tree.this.lastActionTime + 15000L) {
                    cancel();
                    Tree.this.criticalBlock = null;
                    return;
                }
                EffectManager em = MagicSpells.getEffectManager();
                CubeEffect effect = new CubeEffect(em);
                effect.edgeLength = 1;
                effect.enableRotation = false;
                effect.outlineOnly = true;
                effect.yaw = 0f;
                effect.particle = Particle.CRIT;
                effect.particles = 5;
                effect.particleCount = 1;

                effect.iterations = 1;
                effect.period = 1;
                effect.setDynamicOrigin(new DynamicLocation(Tree.this.criticalBlock.getLocation().toCenterLocation()));
                em.start(effect);
            }
        }.runTaskTimer(plugin, 0L, 3L);
    }

    private void dropFruitTask() {
        List<Block> keysAsArray = new ArrayList<>(this.fruits.keySet());
        Block dropBlock = keysAsArray.get(r.nextInt(keysAsArray.size()));
        ArmorStand as = this.fruits.get(dropBlock);
        double[] acceleration = new double[]{0};
        new BukkitRunnable() {
            @Override
            public void run() {
                if (Tree.this.zone == null) {
                    this.cancel();
                    return;
                }
                Location nextTpLocation = as.getLocation().add(0,-acceleration[0],0);
                if (nextTpLocation.getBlock().getType() != Material.AIR && !zone.template.leafBlocks.contains(nextTpLocation.getBlock().getType()) && !zone.template.logBlocks.contains(nextTpLocation.getBlock().getType())) {
                    as.remove();
                    if (getItemSpawnLocation(nextTpLocation) != null) {
                        Location loc = getItemSpawnLocation(nextTpLocation);
                        loc.getWorld().dropItem(loc, zone.template.fruitItem);
                        loc.getWorld().playSound(loc, "block.grass.break", 1.0f, 0.0f);
                    }
                    this.cancel();
                } else {
                    as.teleport(nextTpLocation);
                    acceleration[0] = acceleration[0] + 0.14;
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
    public void unregister() {
        this.zone = null;
        if (this.critIndicatorTask != null) {
            this.critIndicatorTask.cancel();
        }
        if (this.fruitSpawnTask != null) {
            this.fruitSpawnTask.cancel();
        }
        if (this.healthRegenTask != null) {
            this.healthRegenTask.cancel();
        }
        HandlerList.unregisterAll();
    }

    public void dropFruit() {
        if (this.fruits.size() > 0) {
            if (r.nextInt(18) == 0) {
                dropFruitTask();
                fruitSpawned--;
            }
        }
    }
    public void dropAllFruits() {
        if (this.fruits.size() > 0) {
            Iterator<Block> iter = this.fruits.keySet().iterator();
            while (iter.hasNext()) {
                Block block = iter.next();
                ArmorStand as = this.fruits.get(block);
                double[] acceleration = new double[]{0};
                fruitSpawned--;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (Tree.this.zone == null) {
                            this.cancel();
                            return;
                        }
                        Location nextTpLocation = as.getLocation().add(0,-acceleration[0],0);
                        if (nextTpLocation.getBlock().getType() != Material.AIR && !zone.template.leafBlocks.contains(nextTpLocation.getBlock().getType()) && !zone.template.logBlocks.contains(nextTpLocation.getBlock().getType())) {
                            as.remove();
                            if (getItemSpawnLocation(nextTpLocation) != null) {
                                Location loc = getItemSpawnLocation(nextTpLocation);
                                loc.getWorld().dropItem(loc, zone.template.fruitItem);
                                loc.getWorld().playSound(loc, "block.grass.break", 1.0f, 0.0f);
                            }
                            this.cancel();
                        } else {
                            as.teleport(nextTpLocation);
                            acceleration[0] = acceleration[0] + 0.14;
                        }
                    }
                }.runTaskTimer(plugin, 0L, 2L);
            }
        }
    }

    public Location getItemSpawnLocation(Location loc) {
        do {
            loc = loc.add(0,1,0);
            Block block = loc.getBlock();
            if (block.getType() == Material.AIR) {
                return loc;
            }
        } while (loc.getBlock().getType() != Material.AIR);
        return loc;
    }

    public boolean checkIfBlockHasExposedAir(Block block) {
        Block testBlock = block.getLocation().toBlockLocation().clone().add(1,0,0).getBlock();
        if (testBlock.getType() == Material.AIR) {
            return true;
        }
        testBlock = block.getLocation().toBlockLocation().clone().add(-1,0,0).getBlock();
        if (testBlock.getType() == Material.AIR) {
            return true;
        }
        testBlock = block.getLocation().toBlockLocation().clone().add(0,-1,0).getBlock();
        if (testBlock.getType() == Material.AIR) {
            return true;
        }
        testBlock = block.getLocation().toBlockLocation().clone().add(0,0,1).getBlock();
        if (testBlock.getType() == Material.AIR) {
            return true;
        }
        testBlock = block.getLocation().toBlockLocation().clone().add(0,0,-1).getBlock();
        return testBlock.getType() == Material.AIR;
    }
}
