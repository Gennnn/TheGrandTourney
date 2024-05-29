package me.genn.thegrandtourney.skills;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.shaded.effectlib.EffectManager;
import com.nisovin.magicspells.shaded.effectlib.effect.FlameEffect;
import com.nisovin.magicspells.shaded.effectlib.effect.ParticleEffect;
import com.nisovin.magicspells.shaded.effectlib.util.DynamicLocation;
import com.nisovin.magicspells.shaded.effectlib.util.ParticleOptions;
import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.grid.Direction;
import me.genn.thegrandtourney.item.MMOItem;
import me.genn.thegrandtourney.skills.HoldingTable;
import me.genn.thegrandtourney.skills.MashingTable;
import me.genn.thegrandtourney.skills.TimingTable;
import me.genn.thegrandtourney.xp.XpType;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Table {
    public Location loc;
    public String name;
    public XpType type;
    public Direction dir;
    TGT plugin;
    public Interaction entity;
    List<ArmorStand> displayStands;

    public Table() {
    }

    public void register(Location loc, String name, XpType type, TGT plugin) {
        this.loc = loc;
        this.name = name;
        this.type = type;
        this.plugin = plugin;
        this.displayStands = new ArrayList<>();
    }
    public void unregister() {
        this.loc = null;
        this.entity.remove();
        this.name = null;
        if (this instanceof TimingTable) {
            plugin.tableHandler.allTimingTables.remove(this);
        } else if (this instanceof HoldingTable) {
            plugin.tableHandler.allHoldingTables.remove(this);
        } else if (this instanceof MashingTable) {
            plugin.tableHandler.allMashingTables.remove(this);
        }
        for (ArmorStand displayStand : displayStands) {
            displayStand.remove();
        }
        displayStands.clear();
    }
    public void blacksmithMashing(Location loc) {
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
        Interaction in = loc.getWorld().spawn(loc, Interaction.class);
        in.setInteractionWidth(2);
        in.setInteractionHeight(2);
        this.entity = in;
        summonArmorStand(0.0 ,-3.56633 ,0.0,0f,-90f,0f,new ItemStack(Material.ANVIL),loc,dir);

        summonArmorStand(0.0 ,-4.41008 ,0.0,0,0,0,new ItemStack(Material.POLISHED_ANDESITE),loc,dir);
        summonSmallArmorStand(0.0 ,-3.29543 ,0.0,0,0,0,new ItemStack(Material.POLISHED_ANDESITE),loc,dir);

        summonSmallArmorStand(0.28125 ,-3.7425 ,0.28125,0f,-45f,0f,new ItemStack(Material.SMOOTH_STONE_SLAB),loc,dir);
        summonSmallArmorStand(-0.28125 ,-3.7425 ,0.28125,0f,45f,0f,new ItemStack(Material.SMOOTH_STONE_SLAB),loc,dir);
        summonSmallArmorStand(-0.28125 ,-3.7425 ,-0.28125,0f,-135f,0f,new ItemStack(Material.SMOOTH_STONE_SLAB),loc,dir);
        summonSmallArmorStand(0.28125 ,-3.7425 ,-0.28125,-0f,135f,0f,new ItemStack(Material.SMOOTH_STONE_SLAB),loc,dir);

        summonArmorStand(-0.453945 ,-4.02684 ,0.0,90f,-90f,0f,new ItemStack(Material.TRIPWIRE_HOOK),loc,dir);
        summonArmorStand(0.453945 ,-3.50402 ,0.0,-90f,-90f,0f,new ItemStack(Material.TRIPWIRE_HOOK),loc,dir);

        summonSmallArmorStand(0.0 ,-3.29543 ,0.25,0,0,0,new ItemStack(Material.SMOOTH_STONE_SLAB),loc,dir);

        summonSmallArmorStand(0.34375 ,-2.22668 ,0.0,-180f,-180f,0f,new ItemStack(Material.ANVIL),loc,dir);
        summonSmallArmorStand(-0.40625 ,-2.32043 ,0.0,-180f,-90f,0f,new ItemStack(Material.ANVIL),loc,dir);


        this.dir = dir;
    }
    public void blacksmithMashing(Location loc, Direction dir) {

        Interaction in = loc.getWorld().spawn(loc, Interaction.class);
        in.setInteractionWidth(2);
        in.setInteractionHeight(2);
        this.entity = in;
        summonArmorStand(0.0 ,-3.56633 ,0.0,0f,-90f,0f,new ItemStack(Material.ANVIL),loc,dir);

        summonArmorStand(0.0 ,-4.41008 ,0.0,0,0,0,new ItemStack(Material.POLISHED_ANDESITE),loc,dir);
        summonSmallArmorStand(0.0 ,-3.29543 ,0.0,0,0,0,new ItemStack(Material.POLISHED_ANDESITE),loc,dir);

        summonSmallArmorStand(0.28125 ,-3.7425 ,0.28125,0f,-45f,0f,new ItemStack(Material.SMOOTH_STONE_SLAB),loc,dir);
        summonSmallArmorStand(-0.28125 ,-3.7425 ,0.28125,0f,45f,0f,new ItemStack(Material.SMOOTH_STONE_SLAB),loc,dir);
        summonSmallArmorStand(-0.28125 ,-3.7425 ,-0.28125,0f,-135f,0f,new ItemStack(Material.SMOOTH_STONE_SLAB),loc,dir);
        summonSmallArmorStand(0.28125 ,-3.7425 ,-0.28125,-0f,135f,0f,new ItemStack(Material.SMOOTH_STONE_SLAB),loc,dir);

        summonArmorStand(-0.453945 ,-4.02684 ,0.0,90f,-90f,0f,new ItemStack(Material.TRIPWIRE_HOOK),loc,dir);
        summonArmorStand(0.453945 ,-3.50402 ,0.0,-90f,-90f,0f,new ItemStack(Material.TRIPWIRE_HOOK),loc,dir);

        summonSmallArmorStand(0.0 ,-3.29543 ,0.25,0,0,0,new ItemStack(Material.SMOOTH_STONE_SLAB),loc,dir);

        summonSmallArmorStand(0.34375 ,-2.22668 ,0.0,-180f,-180f,0f,new ItemStack(Material.ANVIL),loc,dir);
        summonSmallArmorStand(-0.40625 ,-2.32043 ,0.0,-180f,-90f,0f,new ItemStack(Material.ANVIL),loc,dir);


        this.dir = dir;
    }

    public void blacksmithHolding(Location loc) {
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
        if (dir == Direction.N) {
            dir = Direction.E;
        } else if (dir == Direction.E) {
            dir = Direction.S;
        } else if (dir == Direction.S) {
            dir = Direction.W;
        } else {
            dir = Direction.N;
        }
        Interaction in = loc.getWorld().spawn(loc, Interaction.class);
        in.setInteractionWidth(2);
        in.setInteractionHeight(2);
        this.entity = in;

        summonArmorStand(0.875 ,-3.75383 ,-0.34375,-90f,-90f,0f,new ItemStack(Material.OAK_SLAB), loc, dir);
        summonArmorStand(0.3125 ,-3.75383 ,-1.53125,-90f,-180f,0f,new ItemStack(Material.OAK_SLAB), loc, dir);
        summonArmorStand(0.875 ,-3.75383 ,-0.96875,-90f,-90f,0f,new ItemStack(Material.OAK_SLAB), loc, dir);
        summonArmorStand(-0.3125 ,-3.75383 ,-1.53125,-90f,-180f,0f,new ItemStack(Material.OAK_SLAB), loc, dir);
        summonArmorStand(-0.3125 ,-3.75383 ,0.21875,-90f,0f,0f,new ItemStack(Material.OAK_SLAB), loc, dir);
        summonArmorStand(0.3125 ,-3.75383 ,0.21875,-90f,0f,0f,new ItemStack(Material.OAK_SLAB), loc, dir);
        summonArmorStand(-0.875 ,-3.75383 ,-0.34375,-90f,90f,0f,new ItemStack(Material.OAK_SLAB), loc, dir);
        summonArmorStand(-0.875 ,-3.75383 ,-0.96875,-90f,90f,0f,new ItemStack(Material.OAK_SLAB), loc, dir);

        summonSmallArmorStand(0.4375 ,-3.55793 ,-1.27832,90f,0,0f,new ItemStack(Material.SPRUCE_STAIRS), loc, dir);
        summonSmallArmorStand(0.4375 ,-3.55793 ,-0.03417999999999999,90f,180f,0f,new ItemStack(Material.SPRUCE_STAIRS), loc, dir);
        summonSmallArmorStand(-0.4375 ,-3.55793 ,-1.27832,90f,0,0f,new ItemStack(Material.SPRUCE_STAIRS), loc, dir);
        summonSmallArmorStand(-0.4375 ,-3.55793 ,-0.03417999999999999,90f,180f,0f,new ItemStack(Material.SPRUCE_STAIRS), loc, dir);

        summonSmallArmorStand(0.0 ,-3.52375 ,-0.65625,0,0,0,new ItemStack(Material.SPRUCE_SLAB), loc, dir);
        summonSmallArmorStand(-0.4375 ,-3.52375 ,-0.65625,0,0,0,new ItemStack(Material.SPRUCE_SLAB), loc, dir);
        summonSmallArmorStand(0.0 ,-3.52375 ,-1.09375,0,0,0,new ItemStack(Material.SPRUCE_SLAB), loc, dir);
        summonSmallArmorStand(0.0 ,-3.52375 ,-0.21875,0,0,0,new ItemStack(Material.SPRUCE_SLAB), loc, dir);
        summonSmallArmorStand(0.4375 ,-3.52375 ,-0.65625,0,0,0,new ItemStack(Material.SPRUCE_SLAB), loc, dir);

        summonArmorStand(-0.3125 ,-4.44133 ,1.3125,0,-90,0,new ItemStack(Material.SMOOTH_STONE_SLAB), loc, dir);
        summonArmorStand(-0.3125 ,-4.44133 ,0.6875,0,0,0,new ItemStack(Material.SMOOTH_STONE_SLAB), loc, dir);
        summonArmorStand(0.3125 ,-4.44133 ,1.3125,0,0,0,new ItemStack(Material.SMOOTH_STONE_SLAB), loc, dir);
        summonArmorStand(0.3125 ,-4.44133 ,0.6875,0,-90,0,new ItemStack(Material.SMOOTH_STONE_SLAB), loc, dir);

        summonArmorStand(0.38323300000000005 ,-3.88664 ,1.7582330000000002,-90f,-45f,0f,new ItemStack(Material.SUGAR), loc, dir);
        summonSmallArmorStand(-0.932957 ,-3.3318 ,1.110169,-80f,95f,0f,new ItemStack(Material.SUGAR), loc, dir);
        summonSmallArmorStand(0.05815899999999999 ,-3.37826 ,0.46552499999999997,-75f,-160f,0f,new ItemStack(Material.SUGAR), loc, dir);

        summonSmallArmorStand(0.293945 ,-3.24152 ,0.6875,-90f,-90f,0f,new ItemStack(Material.MAGMA_CREAM), loc, dir);
        summonSmallArmorStand(0.380174 ,-3.39717 ,0.96112,-70f,-60f,0f,new ItemStack(Material.MAGMA_CREAM), loc, dir);

        summonArmorStand(1.0625 ,-3.88664 ,0.96875,-90f,-90f,0f,new ItemStack(Material.PAPER), loc, dir);
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        MMOItem.getHeadFrom64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzRmY2JjMjU2ZDBiZTdlNjgzYWY4NGUzOGM0YmNkYjcxYWZiOTM5ODUzOGEyOWFhOTZjYmZhMzE4YjJlYSJ9fX0=", item);
        summonArmorStand(-0.28125 ,-4.12125 ,-0.34375,0,0,0,item,loc,dir);
        summonArmorStand(-0.28125 ,-4.12125 ,-0.96875,0,-90,0,item,loc,dir);
        summonArmorStand(0.3125 ,-4.12125 ,-0.34375,0,180,0,item,loc,dir);
        summonArmorStand(0.3125 ,-4.12125 ,-0.96875,0,90,0,item,loc,dir);

        this.dir = dir;
    }
    public void blacksmithHolding(Location loc, Direction dir) {

        /*if (dir == Direction.N) {
            dir = Direction.E;
        } else if (dir == Direction.E) {
            dir = Direction.S;
        } else if (dir == Direction.S) {
            dir = Direction.W;
        } else {
            dir = Direction.N;
        }*/
        Interaction in = loc.getWorld().spawn(loc, Interaction.class);
        in.setInteractionWidth(2);
        in.setInteractionHeight(2);
        this.entity = in;

        summonArmorStand(0.875 ,-3.75383 ,-0.34375,-90f,-90f,0f,new ItemStack(Material.OAK_SLAB), loc, dir);
        summonArmorStand(0.3125 ,-3.75383 ,-1.53125,-90f,-180f,0f,new ItemStack(Material.OAK_SLAB), loc, dir);
        summonArmorStand(0.875 ,-3.75383 ,-0.96875,-90f,-90f,0f,new ItemStack(Material.OAK_SLAB), loc, dir);
        summonArmorStand(-0.3125 ,-3.75383 ,-1.53125,-90f,-180f,0f,new ItemStack(Material.OAK_SLAB), loc, dir);
        summonArmorStand(-0.3125 ,-3.75383 ,0.21875,-90f,0f,0f,new ItemStack(Material.OAK_SLAB), loc, dir);
        summonArmorStand(0.3125 ,-3.75383 ,0.21875,-90f,0f,0f,new ItemStack(Material.OAK_SLAB), loc, dir);
        summonArmorStand(-0.875 ,-3.75383 ,-0.34375,-90f,90f,0f,new ItemStack(Material.OAK_SLAB), loc, dir);
        summonArmorStand(-0.875 ,-3.75383 ,-0.96875,-90f,90f,0f,new ItemStack(Material.OAK_SLAB), loc, dir);

        summonSmallArmorStand(0.4375 ,-3.55793 ,-1.27832,90f,0,0f,new ItemStack(Material.SPRUCE_STAIRS), loc, dir);
        summonSmallArmorStand(0.4375 ,-3.55793 ,-0.03417999999999999,90f,180f,0f,new ItemStack(Material.SPRUCE_STAIRS), loc, dir);
        summonSmallArmorStand(-0.4375 ,-3.55793 ,-1.27832,90f,0,0f,new ItemStack(Material.SPRUCE_STAIRS), loc, dir);
        summonSmallArmorStand(-0.4375 ,-3.55793 ,-0.03417999999999999,90f,180f,0f,new ItemStack(Material.SPRUCE_STAIRS), loc, dir);

        summonSmallArmorStand(0.0 ,-3.52375 ,-0.65625,0,0,0,new ItemStack(Material.SPRUCE_SLAB), loc, dir);
        summonSmallArmorStand(-0.4375 ,-3.52375 ,-0.65625,0,0,0,new ItemStack(Material.SPRUCE_SLAB), loc, dir);
        summonSmallArmorStand(0.0 ,-3.52375 ,-1.09375,0,0,0,new ItemStack(Material.SPRUCE_SLAB), loc, dir);
        summonSmallArmorStand(0.0 ,-3.52375 ,-0.21875,0,0,0,new ItemStack(Material.SPRUCE_SLAB), loc, dir);
        summonSmallArmorStand(0.4375 ,-3.52375 ,-0.65625,0,0,0,new ItemStack(Material.SPRUCE_SLAB), loc, dir);

        summonArmorStand(-0.3125 ,-4.44133 ,1.3125,0,-90,0,new ItemStack(Material.SMOOTH_STONE_SLAB), loc, dir);
        summonArmorStand(-0.3125 ,-4.44133 ,0.6875,0,0,0,new ItemStack(Material.SMOOTH_STONE_SLAB), loc, dir);
        summonArmorStand(0.3125 ,-4.44133 ,1.3125,0,0,0,new ItemStack(Material.SMOOTH_STONE_SLAB), loc, dir);
        summonArmorStand(0.3125 ,-4.44133 ,0.6875,0,-90,0,new ItemStack(Material.SMOOTH_STONE_SLAB), loc, dir);

        summonArmorStand(0.38323300000000005 ,-3.88664 ,1.7582330000000002,-90f,-45f,0f,new ItemStack(Material.SUGAR), loc, dir);
        summonSmallArmorStand(-0.932957 ,-3.3318 ,1.110169,-80f,95f,0f,new ItemStack(Material.SUGAR), loc, dir);
        summonSmallArmorStand(0.05815899999999999 ,-3.37826 ,0.46552499999999997,-75f,-160f,0f,new ItemStack(Material.SUGAR), loc, dir);

        summonSmallArmorStand(0.293945 ,-3.24152 ,0.6875,-90f,-90f,0f,new ItemStack(Material.MAGMA_CREAM), loc, dir);
        summonSmallArmorStand(0.380174 ,-3.39717 ,0.96112,-70f,-60f,0f,new ItemStack(Material.MAGMA_CREAM), loc, dir);

        summonArmorStand(1.0625 ,-3.88664 ,0.96875,-90f,-90f,0f,new ItemStack(Material.PAPER), loc, dir);
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        MMOItem.getHeadFrom64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzRmY2JjMjU2ZDBiZTdlNjgzYWY4NGUzOGM0YmNkYjcxYWZiOTM5ODUzOGEyOWFhOTZjYmZhMzE4YjJlYSJ9fX0=", item);
        summonArmorStand(-0.28125 ,-4.12125 ,-0.34375,0,0,0,item,loc,dir);
        summonArmorStand(-0.28125 ,-4.12125 ,-0.96875,0,-90,0,item,loc,dir);
        summonArmorStand(0.3125 ,-4.12125 ,-0.34375,0,180,0,item,loc,dir);
        summonArmorStand(0.3125 ,-4.12125 ,-0.96875,0,90,0,item,loc,dir);

        this.dir = dir;
    }
    public void alchemyHolding(Location loc, Direction dir) {

        Interaction in = loc.getWorld().spawn(loc, Interaction.class);
        in.setInteractionWidth(2);
        in.setInteractionHeight(2);
        this.entity = in;
        ItemStack item = new ItemStack(Material.NETHER_BRICK_SLAB);
        summonArmorStand(-0.44822300000000004 ,-4.19133 ,0.44822300000000004,45f,0f,-90f,item,loc,dir);
        summonArmorStand(0.44822300000000004 ,-4.19133 ,0.44822300000000004,135f,0f,-90f,item,loc,dir);
        summonArmorStand(0.44822300000000004 ,-4.19133 ,-0.44822300000000004,-135f,0f,-90f,item,loc,dir);
        summonArmorStand(-0.44822300000000004 ,-4.19133 ,-0.44822300000000004,-45f,0f,-90f,item,loc,dir);
        summonArmorStand(0.65625 ,-3.81633 ,0.65625,0f,-45f,0f,item,loc,dir);
        summonArmorStand(0.0 ,-3.81633 ,-0.4375,0f,-45f,0f,item,loc,dir);
        summonArmorStand(-0.65625 ,-3.81633 ,0.65625,0f,-45f,0f,item,loc,dir);
        summonArmorStand(-0.65625 ,-3.81633 ,-0.65625,0f,-45f,0f,item,loc,dir);
        summonArmorStand(0.65625 ,-3.81633 ,-0.65625,0f,-45f,0f,item,loc,dir);
        summonArmorStand(0.4375 ,-3.81633 ,0.0,0f,-45f,0f,item,loc,dir);
        summonArmorStand(-0.4375 ,-3.81633 ,0.0,0f,-45f,0f,item,loc,dir);
        summonArmorStand(0.0 ,-3.81633 ,0.4375,0f,-45f,0f,item,loc,dir);

        summonArmorStand(0.366465 ,-3.69708 ,0.472453,-20f,20f,-50f,new ItemStack(Material.BLAZE_ROD),loc,dir);
        Direction finalDir = dir;
        new BukkitRunnable() {

            @Override
            public void run() {
                EffectManager em = MagicSpells.getEffectManager();
                ParticleEffect effect = new ParticleEffect(em);
                effect.particleCount = 2;
                effect.speed = 0.00001f;
                effect.particle = Particle.SMOKE_NORMAL;
                effect.iterations = 1;
                effect.period = 1;
                effect.particleOffsetX = 0;
                effect.particleOffsetY = 0;
                effect.particleOffsetZ = 0;
                effect.setDynamicOrigin(new DynamicLocation(getLocationForDir(loc.clone(), new Vector(0 ,1.1 ,0), finalDir)));
                em.start(effect);
            }
        }.runTaskTimer(plugin, 0L, 10L);

        summonArmorStand(-0.3125 ,-3.50383 ,0.3125,0f,0f,0f,new ItemStack(Material.RED_CARPET),loc,dir);
        summonArmorStand(0.3125 ,-3.50383 ,-0.3125,0f,0f,0f,new ItemStack(Material.RED_CARPET),loc,dir);
        summonArmorStand(0.3125 ,-3.50383 ,0.3125,0f,0f,0f,new ItemStack(Material.ORANGE_CARPET),loc,dir);
        summonArmorStand(-0.3125 ,-3.50383 ,-0.3125,0f,0f,0f,new ItemStack(Material.ORANGE_CARPET),loc,dir);

        summonArmorStand(0.0 ,-3.77258 ,0.15625,0f,90f,0f,new ItemStack(Material.STONE_BUTTON),loc,dir);
        summonArmorStand(-0.125 ,-3.77258 ,-0.03125,0f,180f,0f,new ItemStack(Material.STONE_BUTTON),loc,dir);
        summonArmorStand(0.125 ,-3.77258 ,-0.03125,0f,0f,0f,new ItemStack(Material.STONE_BUTTON),loc,dir);
        summonArmorStand(0.0 ,-3.77258 ,-0.21875,0f,90f,0f,new ItemStack(Material.STONE_BUTTON),loc,dir);

        summonSmallArmorStand(0.0 ,-2.65559 ,-0.34687500000000004,90f,0f,0f,new ItemStack(Material.LEVER),loc,dir);
        summonSmallArmorStand(0.28437500000000004 ,-2.27277 ,-0.03125,-90f,-90f,0f,new ItemStack(Material.LEVER),loc,dir);
        summonSmallArmorStand(0.0 ,-2.65559 ,0.25312500000000004,90f,180f,0f,new ItemStack(Material.LEVER),loc,dir);
        summonSmallArmorStand(-0.31562500000000004 ,-2.27277 ,-0.03125,-90f,90f,0f,new ItemStack(Material.LEVER),loc,dir);

        item = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        meta.setColor(Color.RED);
        item.setItemMeta(meta);
        summonSmallArmorStand(0.0 ,-3.06105 ,0.37890599999999997,0f,0f,0f,item,loc,dir);
        meta.setColor(Color.GREEN);
        item.setItemMeta(meta);
        summonSmallArmorStand(0.0 ,-3.06105 ,-0.47265599999999997,0f,180f,0f,item,loc,dir);
        meta.setColor(Color.BLUE);
        item.setItemMeta(meta);
        summonSmallArmorStand(0.41015599999999997 ,-3.06105 ,-0.03125,0f,-90f,0f,item,loc,dir);
        meta.setColor(Color.YELLOW);
        item.setItemMeta(meta);
        summonSmallArmorStand(-0.44140599999999997 ,-3.06105 ,-0.03125,0f,90f,0f,item,loc,dir);

        item = new ItemStack(Material.PLAYER_HEAD);
        MMOItem.getHeadFrom64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTFlN2ViMmU0NjFlOTZlNjMxY2JhMGMwY2RhYTU0NDg4MDYzMDJlZGFlOTFiNjFkYWZjMjgxYWU1ODRkOCJ9fX0=", item);
        summonSmallArmorStand(0.65625 ,-2.51418 ,-0.6875,0f,-60f,0f,item,loc,dir);

        item = new ItemStack(Material.PLAYER_HEAD);
        MMOItem.getHeadFrom64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTYyOWEzZDNmNTE5ZGNkMzg1YjU5OTJlZjM0YmZhOGE2OGMzNzQ0NDc1MGI5NGVkZTVjMmY1MjFlMTczNDIifX19", item);
        summonSmallArmorStand(-0.65625 ,-2.51418 ,-0.6875,0f,-20f,0f,item,loc,dir);

        item = new ItemStack(Material.NETHER_BRICK_SLAB);
        summonArmorStand(-0.65625 ,-3.50383 ,-0.65625,0f,-45f,0f,item,loc,dir);
        summonArmorStand(0.39552699999999996 ,-3.31633 ,-0.916973,-90f,45f,180f,item,loc,dir);
        summonArmorStand(0.65625 ,-3.31633 ,-0.65625,0f,-45f,180f,item,loc,dir);
        summonArmorStand(-0.39552699999999996 ,-3.31633 ,-0.916973,-90f,-45f,180f,item,loc,dir);
        this.dir = dir;
    }
    public void alchemyHolding(Location loc) {
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
        Interaction in = loc.getWorld().spawn(loc, Interaction.class);
        in.setInteractionWidth(2);
        in.setInteractionHeight(2);
        this.entity = in;
        ItemStack item = new ItemStack(Material.NETHER_BRICK_SLAB);
        summonArmorStand(-0.44822300000000004 ,-4.19133 ,0.44822300000000004,45f,0f,-90f,item,loc,dir);
        summonArmorStand(0.44822300000000004 ,-4.19133 ,0.44822300000000004,135f,0f,-90f,item,loc,dir);
        summonArmorStand(0.44822300000000004 ,-4.19133 ,-0.44822300000000004,-135f,0f,-90f,item,loc,dir);
        summonArmorStand(-0.44822300000000004 ,-4.19133 ,-0.44822300000000004,-45f,0f,-90f,item,loc,dir);
        summonArmorStand(0.65625 ,-3.81633 ,0.65625,0f,-45f,0f,item,loc,dir);
        summonArmorStand(0.0 ,-3.81633 ,-0.4375,0f,-45f,0f,item,loc,dir);
        summonArmorStand(-0.65625 ,-3.81633 ,0.65625,0f,-45f,0f,item,loc,dir);
        summonArmorStand(-0.65625 ,-3.81633 ,-0.65625,0f,-45f,0f,item,loc,dir);
        summonArmorStand(0.65625 ,-3.81633 ,-0.65625,0f,-45f,0f,item,loc,dir);
        summonArmorStand(0.4375 ,-3.81633 ,0.0,0f,-45f,0f,item,loc,dir);
        summonArmorStand(-0.4375 ,-3.81633 ,0.0,0f,-45f,0f,item,loc,dir);
        summonArmorStand(0.0 ,-3.81633 ,0.4375,0f,-45f,0f,item,loc,dir);

        summonArmorStand(0.366465 ,-3.69708 ,0.472453,-20f,20f,-50f,new ItemStack(Material.BLAZE_ROD),loc,dir);
        Direction finalDir = dir;
        new BukkitRunnable() {

            @Override
            public void run() {
                EffectManager em = MagicSpells.getEffectManager();
                ParticleEffect effect = new ParticleEffect(em);
                effect.particleCount = 2;
                effect.speed = 0.00001f;
                effect.particle = Particle.SMOKE_NORMAL;
                effect.iterations = 1;
                effect.period = 1;
                effect.particleOffsetX = 0;
                effect.particleOffsetY = 0;
                effect.particleOffsetZ = 0;
                effect.setDynamicOrigin(new DynamicLocation(getLocationForDir(loc.clone(), new Vector(0 ,1.1 ,0), finalDir)));
                em.start(effect);
            }
        }.runTaskTimer(plugin, 0L, 10L);

        summonArmorStand(-0.3125 ,-3.50383 ,0.3125,0f,0f,0f,new ItemStack(Material.RED_CARPET),loc,dir);
        summonArmorStand(0.3125 ,-3.50383 ,-0.3125,0f,0f,0f,new ItemStack(Material.RED_CARPET),loc,dir);
        summonArmorStand(0.3125 ,-3.50383 ,0.3125,0f,0f,0f,new ItemStack(Material.ORANGE_CARPET),loc,dir);
        summonArmorStand(-0.3125 ,-3.50383 ,-0.3125,0f,0f,0f,new ItemStack(Material.ORANGE_CARPET),loc,dir);

        summonArmorStand(0.0 ,-3.77258 ,0.15625,0f,90f,0f,new ItemStack(Material.STONE_BUTTON),loc,dir);
        summonArmorStand(-0.125 ,-3.77258 ,-0.03125,0f,180f,0f,new ItemStack(Material.STONE_BUTTON),loc,dir);
        summonArmorStand(0.125 ,-3.77258 ,-0.03125,0f,0f,0f,new ItemStack(Material.STONE_BUTTON),loc,dir);
        summonArmorStand(0.0 ,-3.77258 ,-0.21875,0f,90f,0f,new ItemStack(Material.STONE_BUTTON),loc,dir);

        summonSmallArmorStand(0.0 ,-2.65559 ,-0.34687500000000004,90f,0f,0f,new ItemStack(Material.LEVER),loc,dir);
        summonSmallArmorStand(0.28437500000000004 ,-2.27277 ,-0.03125,-90f,-90f,0f,new ItemStack(Material.LEVER),loc,dir);
        summonSmallArmorStand(0.0 ,-2.65559 ,0.25312500000000004,90f,180f,0f,new ItemStack(Material.LEVER),loc,dir);
        summonSmallArmorStand(-0.31562500000000004 ,-2.27277 ,-0.03125,-90f,90f,0f,new ItemStack(Material.LEVER),loc,dir);

        item = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        meta.setColor(Color.RED);
        item.setItemMeta(meta);
        summonSmallArmorStand(0.0 ,-3.06105 ,0.37890599999999997,0f,0f,0f,item,loc,dir);
        meta.setColor(Color.GREEN);
        item.setItemMeta(meta);
        summonSmallArmorStand(0.0 ,-3.06105 ,-0.47265599999999997,0f,180f,0f,item,loc,dir);
        meta.setColor(Color.BLUE);
        item.setItemMeta(meta);
        summonSmallArmorStand(0.41015599999999997 ,-3.06105 ,-0.03125,0f,-90f,0f,item,loc,dir);
        meta.setColor(Color.YELLOW);
        item.setItemMeta(meta);
        summonSmallArmorStand(-0.44140599999999997 ,-3.06105 ,-0.03125,0f,90f,0f,item,loc,dir);

        item = new ItemStack(Material.PLAYER_HEAD);
        MMOItem.getHeadFrom64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTFlN2ViMmU0NjFlOTZlNjMxY2JhMGMwY2RhYTU0NDg4MDYzMDJlZGFlOTFiNjFkYWZjMjgxYWU1ODRkOCJ9fX0=", item);
        summonSmallArmorStand(0.65625 ,-2.51418 ,-0.6875,0f,-60f,0f,item,loc,dir);

        item = new ItemStack(Material.PLAYER_HEAD);
        MMOItem.getHeadFrom64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTYyOWEzZDNmNTE5ZGNkMzg1YjU5OTJlZjM0YmZhOGE2OGMzNzQ0NDc1MGI5NGVkZTVjMmY1MjFlMTczNDIifX19", item);
        summonSmallArmorStand(-0.65625 ,-2.51418 ,-0.6875,0f,-20f,0f,item,loc,dir);

        item = new ItemStack(Material.NETHER_BRICK_SLAB);
        summonArmorStand(-0.65625 ,-3.50383 ,-0.65625,0f,-45f,0f,item,loc,dir);
        summonArmorStand(0.39552699999999996 ,-3.31633 ,-0.916973,-90f,45f,180f,item,loc,dir);
        summonArmorStand(0.65625 ,-3.31633 ,-0.65625,0f,-45f,180f,item,loc,dir);
        summonArmorStand(-0.39552699999999996 ,-3.31633 ,-0.916973,-90f,-45f,180f,item,loc,dir);
        this.dir = dir;
    }

    public void alchemyTiming(Location loc, Direction dir) {

        Interaction in = loc.getWorld().spawn(loc, Interaction.class);
        in.setInteractionWidth(2);
        in.setInteractionHeight(2);
        this.entity = in;
        ItemStack item = new ItemStack(Material.NETHER_BRICK_SLAB);
        summonArmorStand(-0.44822300000000004 ,-4.19133 ,0.44822300000000004,45f,0f,-90f,item,loc,dir);
        summonArmorStand(0.44822300000000004 ,-4.19133 ,0.44822300000000004,135f,0f,-90f,item,loc,dir);
        summonArmorStand(0.44822300000000004 ,-4.19133 ,-0.44822300000000004,-135f,0f,-90f,item,loc,dir);
        summonArmorStand(-0.44822300000000004 ,-4.19133 ,-0.44822300000000004,-45f,0f,-90f,item,loc,dir);
        summonArmorStand(0.65625 ,-3.81633 ,0.65625,0f,-45f,0f,item,loc,dir);
        summonArmorStand(0.0 ,-3.81633 ,-0.4375,0f,-45f,0f,item,loc,dir);
        summonArmorStand(-0.65625 ,-3.81633 ,0.65625,0f,-45f,0f,item,loc,dir);
        summonArmorStand(-0.65625 ,-3.81633 ,-0.65625,0f,-45f,0f,item,loc,dir);
        summonArmorStand(0.65625 ,-3.81633 ,-0.65625,0f,-45f,0f,item,loc,dir);
        summonArmorStand(0.4375 ,-3.81633 ,0.0,0f,-45f,0f,item,loc,dir);
        summonArmorStand(-0.4375 ,-3.81633 ,0.0,0f,-45f,0f,item,loc,dir);
        summonArmorStand(0.0 ,-3.81633 ,0.4375,0f,-45f,0f,item,loc,dir);

        summonArmorStand(-0.3125 ,-3.50383 ,0.3125,0f,0f,0f,new ItemStack(Material.RED_CARPET),loc,dir);
        summonArmorStand(0.3125 ,-3.50383 ,-0.3125,0f,0f,0f,new ItemStack(Material.RED_CARPET),loc,dir);
        summonArmorStand(0.3125 ,-3.50383 ,0.3125,0f,0f,0f,new ItemStack(Material.ORANGE_CARPET),loc,dir);
        summonArmorStand(-0.3125 ,-3.50383 ,-0.3125,0f,0f,0f,new ItemStack(Material.ORANGE_CARPET),loc,dir);

        item = new ItemStack(Material.NETHER_BRICK_SLAB);
        summonArmorStand(-0.65625 ,-3.50383 ,-0.65625,0f,-45f,0f,item,loc,dir);
        summonArmorStand(0.39552699999999996 ,-3.31633 ,-0.916973,-90f,45f,180f,item,loc,dir);
        summonArmorStand(0.65625 ,-3.31633 ,-0.65625,0f,-45f,180f,item,loc,dir);
        summonArmorStand(-0.39552699999999996 ,-3.31633 ,-0.916973,-90f,-45f,180f,item,loc,dir);

        item = new ItemStack(Material.PLAYER_HEAD);
        MMOItem.getHeadFrom64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTNhNzI4YWQ4ZDMxNDg2YTdmOWFhZDIwMGVkYjM3M2VhODAzZDFmYzVmZDQzMjFiMmUyYTk3MTM0ODIzNDQ0MyJ9fX0=", item);
        summonArmorStand(0.0 ,-3.69258 ,0.0,0f,0f,0f,item,loc,dir);

        item = new ItemStack(Material.STICK);
        summonArmorStand(0.577666 ,-3.69121 ,0.10055399999999999,0f,-30f,-25f,item,loc,dir);

        item = new ItemStack(Material.GOLDEN_SHOVEL);
        summonSmallArmorStand(0.10644500000000001 ,-2.58527 ,0.40625,-90f,-90f,0f,item,loc,dir);

        item = new ItemStack(Material.GLASS_BOTTLE);
        summonSmallArmorStand(0.31457599999999997 ,-2.61652 ,0.9646600000000001,-90f,25f,0f,item,loc,dir);
        summonSmallArmorStand(1.137961 ,-2.77217 ,0.9767600000000001,-70f,-50f,0f,item,loc,dir);
        summonSmallArmorStand(0.701782 ,-2.67721 ,-0.23855700000000002,-80f,-165f,0f,item,loc,dir);

        item = new ItemStack(Material.PLAYER_HEAD);
        MMOItem.getHeadFrom64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDI3ZDY0ZTE0M2UwNThhZmQ5MWFhYTY1MTFiZTVlNDc0OTg3OWZlNDU2NGI0YTVjNTVhMzI0MzkzMTU2NDVhZSJ9fX0=", item);
        summonSmallArmorStand(-0.65625 ,-2.61418 ,-0.65625,0f,-60f,0f,item,loc,dir);

        summonSmallArmorStand(0.63625 ,-2.31418 ,-0.65625,90f,-60f,0f,item,loc,dir);
        this.dir = dir;
    }

    public void alchemyTiming(Location loc) {
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
        Interaction in = loc.getWorld().spawn(loc, Interaction.class);
        in.setInteractionWidth(2);
        in.setInteractionHeight(2);
        this.entity = in;
        ItemStack item = new ItemStack(Material.NETHER_BRICK_SLAB);
        summonArmorStand(-0.44822300000000004 ,-4.19133 ,0.44822300000000004,45f,0f,-90f,item,loc,dir);
        summonArmorStand(0.44822300000000004 ,-4.19133 ,0.44822300000000004,135f,0f,-90f,item,loc,dir);
        summonArmorStand(0.44822300000000004 ,-4.19133 ,-0.44822300000000004,-135f,0f,-90f,item,loc,dir);
        summonArmorStand(-0.44822300000000004 ,-4.19133 ,-0.44822300000000004,-45f,0f,-90f,item,loc,dir);
        summonArmorStand(0.65625 ,-3.81633 ,0.65625,0f,-45f,0f,item,loc,dir);
        summonArmorStand(0.0 ,-3.81633 ,-0.4375,0f,-45f,0f,item,loc,dir);
        summonArmorStand(-0.65625 ,-3.81633 ,0.65625,0f,-45f,0f,item,loc,dir);
        summonArmorStand(-0.65625 ,-3.81633 ,-0.65625,0f,-45f,0f,item,loc,dir);
        summonArmorStand(0.65625 ,-3.81633 ,-0.65625,0f,-45f,0f,item,loc,dir);
        summonArmorStand(0.4375 ,-3.81633 ,0.0,0f,-45f,0f,item,loc,dir);
        summonArmorStand(-0.4375 ,-3.81633 ,0.0,0f,-45f,0f,item,loc,dir);
        summonArmorStand(0.0 ,-3.81633 ,0.4375,0f,-45f,0f,item,loc,dir);

        summonArmorStand(-0.3125 ,-3.50383 ,0.3125,0f,0f,0f,new ItemStack(Material.RED_CARPET),loc,dir);
        summonArmorStand(0.3125 ,-3.50383 ,-0.3125,0f,0f,0f,new ItemStack(Material.RED_CARPET),loc,dir);
        summonArmorStand(0.3125 ,-3.50383 ,0.3125,0f,0f,0f,new ItemStack(Material.ORANGE_CARPET),loc,dir);
        summonArmorStand(-0.3125 ,-3.50383 ,-0.3125,0f,0f,0f,new ItemStack(Material.ORANGE_CARPET),loc,dir);

        item = new ItemStack(Material.NETHER_BRICK_SLAB);
        summonArmorStand(-0.65625 ,-3.50383 ,-0.65625,0f,-45f,0f,item,loc,dir);
        summonArmorStand(0.39552699999999996 ,-3.31633 ,-0.916973,-90f,45f,180f,item,loc,dir);
        summonArmorStand(0.65625 ,-3.31633 ,-0.65625,0f,-45f,180f,item,loc,dir);
        summonArmorStand(-0.39552699999999996 ,-3.31633 ,-0.916973,-90f,-45f,180f,item,loc,dir);

        item = new ItemStack(Material.PLAYER_HEAD);
        MMOItem.getHeadFrom64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTNhNzI4YWQ4ZDMxNDg2YTdmOWFhZDIwMGVkYjM3M2VhODAzZDFmYzVmZDQzMjFiMmUyYTk3MTM0ODIzNDQ0MyJ9fX0=", item);
        summonArmorStand(0.0 ,-3.69258 ,0.0,0f,0f,0f,item,loc,dir);

        item = new ItemStack(Material.STICK);
        summonArmorStand(0.577666 ,-3.69121 ,0.10055399999999999,0f,-30f,-25f,item,loc,dir);

        item = new ItemStack(Material.GOLDEN_SHOVEL);
        summonSmallArmorStand(0.10644500000000001 ,-2.58527 ,0.40625,-90f,-90f,0f,item,loc,dir);

        item = new ItemStack(Material.GLASS_BOTTLE);
        summonSmallArmorStand(0.31457599999999997 ,-2.61652 ,0.9646600000000001,-90f,25f,0f,item,loc,dir);
        summonSmallArmorStand(1.137961 ,-2.77217 ,0.9767600000000001,-70f,-50f,0f,item,loc,dir);
        summonSmallArmorStand(0.701782 ,-2.67721 ,-0.23855700000000002,-80f,-165f,0f,item,loc,dir);

        item = new ItemStack(Material.PLAYER_HEAD);
        MMOItem.getHeadFrom64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDI3ZDY0ZTE0M2UwNThhZmQ5MWFhYTY1MTFiZTVlNDc0OTg3OWZlNDU2NGI0YTVjNTVhMzI0MzkzMTU2NDVhZSJ9fX0=", item);
        summonSmallArmorStand(-0.65625 ,-2.61418 ,-0.65625,0f,-60f,0f,item,loc,dir);

        summonSmallArmorStand(0.63625 ,-2.31418 ,-0.65625,90f,-60f,0f,item,loc,dir);
        this.dir = dir;
    }

    public void alchemyMashing(Location loc) {
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
        Interaction in = loc.getWorld().spawn(loc, Interaction.class);
        in.setInteractionWidth(2);
        in.setInteractionHeight(2);
        this.entity = in;

        ItemStack item = new ItemStack(Material.NETHER_BRICK_SLAB);
        summonArmorStand(-0.44822300000000004 ,-4.19133 ,0.44822300000000004,45f,0f,-90f,item,loc,dir);
        summonArmorStand(0.44822300000000004 ,-4.19133 ,0.44822300000000004,135f,0f,-90f,item,loc,dir);
        summonArmorStand(0.44822300000000004 ,-4.19133 ,-0.44822300000000004,-135f,0f,-90f,item,loc,dir);
        summonArmorStand(-0.44822300000000004 ,-4.19133 ,-0.44822300000000004,-45f,0f,-90f,item,loc,dir);
        summonArmorStand(0.65625 ,-3.81633 ,0.65625,0f,-45f,0f,item,loc,dir);
        summonArmorStand(0.0 ,-3.81633 ,-0.4375,0f,-45f,0f,item,loc,dir);
        summonArmorStand(-0.65625 ,-3.81633 ,0.65625,0f,-45f,0f,item,loc,dir);
        summonArmorStand(-0.65625 ,-3.81633 ,-0.65625,0f,-45f,0f,item,loc,dir);
        summonArmorStand(0.65625 ,-3.81633 ,-0.65625,0f,-45f,0f,item,loc,dir);
        summonArmorStand(0.4375 ,-3.81633 ,0.0,0f,-45f,0f,item,loc,dir);
        summonArmorStand(-0.4375 ,-3.81633 ,0.0,0f,-45f,0f,item,loc,dir);
        summonArmorStand(0.0 ,-3.81633 ,0.4375,0f,-45f,0f,item,loc,dir);

        summonArmorStand(-0.3125 ,-3.50383 ,0.3125,0f,0f,0f,new ItemStack(Material.RED_CARPET),loc,dir);
        summonArmorStand(0.3125 ,-3.50383 ,-0.3125,0f,0f,0f,new ItemStack(Material.RED_CARPET),loc,dir);
        summonArmorStand(0.3125 ,-3.50383 ,0.3125,0f,0f,0f,new ItemStack(Material.ORANGE_CARPET),loc,dir);
        summonArmorStand(-0.3125 ,-3.50383 ,-0.3125,0f,0f,0f,new ItemStack(Material.ORANGE_CARPET),loc,dir);

        item = new ItemStack(Material.NETHER_BRICK_SLAB);
        summonArmorStand(-0.65625 ,-3.50383 ,-0.65625,0f,-45f,0f,item,loc,dir);
        summonArmorStand(0.39552699999999996 ,-3.31633 ,-0.916973,-90f,45f,180f,item,loc,dir);
        summonArmorStand(0.65625 ,-3.31633 ,-0.65625,0f,-45f,180f,item,loc,dir);
        summonArmorStand(-0.39552699999999996 ,-3.31633 ,-0.916973,-90f,-45f,180f,item,loc,dir);

        item = new ItemStack(Material.PLAYER_HEAD);
        MMOItem.getHeadFrom64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWQzMGM1YjkzNTgxNzlkMDk4Nzc0MGQ3NDc4YzBlZWI2YjljN2ZhMDdjZTQ4OGRkNjk4NTE4MWFmNjFmYjhhMiJ9fX0=", item);
        summonSmallArmorStand(0.6875 ,-2.5425 ,-0.59375, 0f,20f,0f,item,loc,dir);
        Direction finalDir = dir;
        new BukkitRunnable() {

            @Override
            public void run() {
                EffectManager em = MagicSpells.getEffectManager();
                ParticleEffect effect = new ParticleEffect(em);
                effect.particleCount = 1;
                effect.yaw = 0f;
                effect.speed = 0.00001f;
                effect.particle = Particle.FLAME;
                effect.particleCount = 1;
                effect.iterations = 1;

                effect.period = 1;
                effect.particleOffsetX=0;
                effect.particleOffsetY=0;
                effect.particleOffsetZ=0;
                effect.setDynamicOrigin(new DynamicLocation(getLocationForDir(loc.clone(), new Vector(0.6875 ,1.1 ,-0.59375), finalDir)));
                em.start(effect);
            }
        }.runTaskTimer(plugin, 0L, 10L);

        summonSmallArmorStand(-0.6875 ,-2.5425 ,-0.59375, 0f,-70f,0f,item,loc,dir);
        new BukkitRunnable() {

            @Override
            public void run() {
                EffectManager em = MagicSpells.getEffectManager();
                ParticleEffect effect = new ParticleEffect(em);
                effect.particleCount = 1;
                effect.yaw = 0f;
                effect.speed = 0.00001f;
                effect.particle = Particle.FLAME;
                effect.particleCount = 1;
                effect.iterations = 1;
                effect.period = 1;
                effect.particleOffsetX=0;
                effect.particleOffsetY=0;
                effect.particleOffsetZ=0;
                effect.setDynamicOrigin(new DynamicLocation(getLocationForDir(loc.clone(), new Vector(-0.6875 ,1.1 ,-0.59375), finalDir)));
                em.start(effect);
            }
        }.runTaskTimer(plugin, 1L, 10L);

        MMOItem.getHeadFrom64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWEwYzA0OWVhNGU5NDRmOWE5MDJmYzM1ZWM3YWFkMjg4NDAzMmQ4YmJlZTlkYTM1NzAyOGQ0NjEwN2JjZGRhZiJ9fX0=",item);
        summonSmallArmorStand(-0.3125 ,-3.12375 ,-0.09375, 0f,-95f,0f,item,loc,dir);
        summonSmallArmorStand(0.3125 ,-3.12375 ,0.21875, 0f,-125f,0f,item,loc,dir);
        summonSmallArmorStand(0.1875 ,-3.12375 ,-0.34375, 0f,-75f,0f,item,loc,dir);

        item = new ItemStack(Material.NETHER_WART);
        summonSmallArmorStand(-0.18366700000000002 ,-2.62096 ,0.09707100000000002, -80f,35f,0f,item,loc,dir);
        summonSmallArmorStand(0.6289830000000001 ,-2.63451 ,0.031725999999999976, -75f,-50f,0f,item,loc,dir);
        summonSmallArmorStand(0.599349 ,-2.6814 ,-0.755599, -70f,-135f,0f,item,loc,dir);

        item = new ItemStack(Material.BOWL);
        summonSmallArmorStand(0.797886 ,-2.80342 ,1.153594, -70f,-15f,0f,item,loc,dir);
        summonSmallArmorStand(1.286424 ,-2.80342 ,0.89862, -70f,-60f,0f,item,loc,dir);

        item = new ItemStack(Material.BLAZE_POWDER);
        summonSmallArmorStand(-0.14661999999999997 ,-2.48507 ,0.434531, -95f,60f,0f,item,loc,dir);
        summonSmallArmorStand(0.799056 ,-2.48507 ,0.070457, -95f,-100f,0f,item,loc,dir);
        summonSmallArmorStand(0.334883 ,-2.48221 ,0.710932, -95f,-5f,-5f,item,loc,dir);

        item = new ItemStack(Material.SPIDER_EYE);
        summonSmallArmorStand(0.29515800000000003 ,-2.71592 ,-0.014804999999999999, -70f,-85f,0f,item,loc,dir);
        summonSmallArmorStand(-0.40625 ,-2.71592 ,-0.703491, -70f,-180f,0f,item,loc,dir);
        summonSmallArmorStand(-0.30822 ,-2.22192 ,-0.101351, 35f,-15f,180f,item,loc,dir);
        summonSmallArmorStand(0.245969 ,-2.71592 ,-0.418774, -70f,-115f,0f,item,loc,dir);
        this.dir = dir;
    }
    public void alchemyMashing(Location loc, Direction dir) {
        Interaction in = loc.getWorld().spawn(loc, Interaction.class);
        in.setInteractionWidth(2);
        in.setInteractionHeight(2);
        this.entity = in;

        ItemStack item = new ItemStack(Material.NETHER_BRICK_SLAB);
        summonArmorStand(-0.44822300000000004 ,-4.19133 ,0.44822300000000004,45f,0f,-90f,item,loc,dir);
        summonArmorStand(0.44822300000000004 ,-4.19133 ,0.44822300000000004,135f,0f,-90f,item,loc,dir);
        summonArmorStand(0.44822300000000004 ,-4.19133 ,-0.44822300000000004,-135f,0f,-90f,item,loc,dir);
        summonArmorStand(-0.44822300000000004 ,-4.19133 ,-0.44822300000000004,-45f,0f,-90f,item,loc,dir);
        summonArmorStand(0.65625 ,-3.81633 ,0.65625,0f,-45f,0f,item,loc,dir);
        summonArmorStand(0.0 ,-3.81633 ,-0.4375,0f,-45f,0f,item,loc,dir);
        summonArmorStand(-0.65625 ,-3.81633 ,0.65625,0f,-45f,0f,item,loc,dir);
        summonArmorStand(-0.65625 ,-3.81633 ,-0.65625,0f,-45f,0f,item,loc,dir);
        summonArmorStand(0.65625 ,-3.81633 ,-0.65625,0f,-45f,0f,item,loc,dir);
        summonArmorStand(0.4375 ,-3.81633 ,0.0,0f,-45f,0f,item,loc,dir);
        summonArmorStand(-0.4375 ,-3.81633 ,0.0,0f,-45f,0f,item,loc,dir);
        summonArmorStand(0.0 ,-3.81633 ,0.4375,0f,-45f,0f,item,loc,dir);

        summonArmorStand(-0.3125 ,-3.50383 ,0.3125,0f,0f,0f,new ItemStack(Material.RED_CARPET),loc,dir);
        summonArmorStand(0.3125 ,-3.50383 ,-0.3125,0f,0f,0f,new ItemStack(Material.RED_CARPET),loc,dir);
        summonArmorStand(0.3125 ,-3.50383 ,0.3125,0f,0f,0f,new ItemStack(Material.ORANGE_CARPET),loc,dir);
        summonArmorStand(-0.3125 ,-3.50383 ,-0.3125,0f,0f,0f,new ItemStack(Material.ORANGE_CARPET),loc,dir);

        item = new ItemStack(Material.NETHER_BRICK_SLAB);
        summonArmorStand(-0.65625 ,-3.50383 ,-0.65625,0f,-45f,0f,item,loc,dir);
        summonArmorStand(0.39552699999999996 ,-3.31633 ,-0.916973,-90f,45f,180f,item,loc,dir);
        summonArmorStand(0.65625 ,-3.31633 ,-0.65625,0f,-45f,180f,item,loc,dir);
        summonArmorStand(-0.39552699999999996 ,-3.31633 ,-0.916973,-90f,-45f,180f,item,loc,dir);

        item = new ItemStack(Material.PLAYER_HEAD);
        MMOItem.getHeadFrom64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWQzMGM1YjkzNTgxNzlkMDk4Nzc0MGQ3NDc4YzBlZWI2YjljN2ZhMDdjZTQ4OGRkNjk4NTE4MWFmNjFmYjhhMiJ9fX0=", item);
        summonSmallArmorStand(0.6875 ,-2.5425 ,-0.59375, 0f,20f,0f,item,loc,dir);
        Direction finalDir = dir;
        new BukkitRunnable() {

            @Override
            public void run() {
                EffectManager em = MagicSpells.getEffectManager();
                ParticleEffect effect = new ParticleEffect(em);
                effect.particleCount = 1;
                effect.yaw = 0f;
                effect.speed = 0.00001f;
                effect.particle = Particle.FLAME;
                effect.particleCount = 1;
                effect.iterations = 1;

                effect.period = 1;
                effect.particleOffsetX=0;
                effect.particleOffsetY=0;
                effect.particleOffsetZ=0;
                effect.setDynamicOrigin(new DynamicLocation(getLocationForDir(loc.clone(), new Vector(0.6875 ,1.1 ,-0.59375), finalDir)));
                em.start(effect);
            }
        }.runTaskTimer(plugin, 0L, 10L);

        summonSmallArmorStand(-0.6875 ,-2.5425 ,-0.59375, 0f,-70f,0f,item,loc,dir);
        new BukkitRunnable() {

            @Override
            public void run() {
                EffectManager em = MagicSpells.getEffectManager();
                ParticleEffect effect = new ParticleEffect(em);
                effect.particleCount = 1;
                effect.yaw = 0f;
                effect.speed = 0.00001f;
                effect.particle = Particle.FLAME;
                effect.particleCount = 1;
                effect.iterations = 1;
                effect.period = 1;
                effect.particleOffsetX=0;
                effect.particleOffsetY=0;
                effect.particleOffsetZ=0;
                effect.setDynamicOrigin(new DynamicLocation(getLocationForDir(loc.clone(), new Vector(-0.6875 ,1.1 ,-0.59375), finalDir)));
                em.start(effect);
            }
        }.runTaskTimer(plugin, 1L, 10L);

        MMOItem.getHeadFrom64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWEwYzA0OWVhNGU5NDRmOWE5MDJmYzM1ZWM3YWFkMjg4NDAzMmQ4YmJlZTlkYTM1NzAyOGQ0NjEwN2JjZGRhZiJ9fX0=",item);
        summonSmallArmorStand(-0.3125 ,-3.12375 ,-0.09375, 0f,-95f,0f,item,loc,dir);
        summonSmallArmorStand(0.3125 ,-3.12375 ,0.21875, 0f,-125f,0f,item,loc,dir);
        summonSmallArmorStand(0.1875 ,-3.12375 ,-0.34375, 0f,-75f,0f,item,loc,dir);

        item = new ItemStack(Material.NETHER_WART);
        summonSmallArmorStand(-0.18366700000000002 ,-2.62096 ,0.09707100000000002, -80f,35f,0f,item,loc,dir);
        summonSmallArmorStand(0.6289830000000001 ,-2.63451 ,0.031725999999999976, -75f,-50f,0f,item,loc,dir);
        summonSmallArmorStand(0.599349 ,-2.6814 ,-0.755599, -70f,-135f,0f,item,loc,dir);

        item = new ItemStack(Material.BOWL);
        summonSmallArmorStand(0.797886 ,-2.80342 ,1.153594, -70f,-15f,0f,item,loc,dir);
        summonSmallArmorStand(1.286424 ,-2.80342 ,0.89862, -70f,-60f,0f,item,loc,dir);

        item = new ItemStack(Material.BLAZE_POWDER);
        summonSmallArmorStand(-0.14661999999999997 ,-2.48507 ,0.434531, -95f,60f,0f,item,loc,dir);
        summonSmallArmorStand(0.799056 ,-2.48507 ,0.070457, -95f,-100f,0f,item,loc,dir);
        summonSmallArmorStand(0.334883 ,-2.48221 ,0.710932, -95f,-5f,-5f,item,loc,dir);

        item = new ItemStack(Material.SPIDER_EYE);
        summonSmallArmorStand(0.29515800000000003 ,-2.71592 ,-0.014804999999999999, -70f,-85f,0f,item,loc,dir);
        summonSmallArmorStand(-0.40625 ,-2.71592 ,-0.703491, -70f,-180f,0f,item,loc,dir);
        summonSmallArmorStand(-0.30822 ,-2.22192 ,-0.101351, 35f,-15f,180f,item,loc,dir);
        summonSmallArmorStand(0.245969 ,-2.71592 ,-0.418774, -70f,-115f,0f,item,loc,dir);
        this.dir = dir;
    }

    public void tableTest(Location loc) {
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
        Interaction in = loc.getWorld().spawn(loc, Interaction.class);
        in.setInteractionWidth(2);
        in.setInteractionHeight(2);
        this.entity = in;
        ItemStack item = new ItemStack(Material.DARK_OAK_SLAB);
        summonArmorStand(1.0625,-4.19133,0.0,-90,-90,0,item,loc,dir);
        summonArmorStand(-1.0625,-4.19133,0.0,90,-90,0,item,loc,dir);
        summonArmorStand(-0.9375 ,-3.81633 ,0.3125,0,-90,0,item,loc,dir);
        summonArmorStand(-0.9375 ,-3.81633 ,-0.3125,0,-90,0,item,loc,dir);
        summonArmorStand(-0.3125 ,-3.81633 ,0.3125,0,-90,0,item,loc,dir);
        summonArmorStand(0.3125 ,-3.81633 ,0.3125,0,-90,0,item,loc,dir);
        summonArmorStand(0.9375 ,-3.81633 ,0.3125,0,-90,0,item,loc,dir);
        summonArmorStand(0.9375 ,-3.81633 ,-0.3125,0,-90,0,item,loc,dir);
        summonArmorStand(0.3125 ,-3.81633 ,-0.3125,0,-90,0,item,loc,dir);
        summonArmorStand(-0.3125 ,-3.81633 ,-0.3125,0,-90,0,item,loc,dir);

        summonSmallArmorStand(0.0 ,-2.805 ,0.0,0f,-45f,0f,new ItemStack(Material.MAGENTA_CARPET),loc,dir);
        summonSmallArmorStand(-0.625 ,-2.805 ,0.0,0f,-45f,0f,new ItemStack(Material.MAGENTA_CARPET),loc,dir);
        summonSmallArmorStand(0.625 ,-2.805 ,0.0,0f,-45f,0f,new ItemStack(Material.MAGENTA_CARPET),loc,dir);

        summonSmallArmorStand(0.9375 ,-2.805 ,-0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);
        summonSmallArmorStand(0.9375 ,-2.805 ,0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);
        summonSmallArmorStand(-0.9375 ,-2.805 ,0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);
        summonSmallArmorStand(-0.9375 ,-2.805 ,-0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);
        summonSmallArmorStand(-0.3125 ,-2.805 ,0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);
        summonSmallArmorStand(0.3125 ,-2.805 ,0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);
        summonSmallArmorStand(0.3125 ,-2.805 ,-0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);
        summonSmallArmorStand(-0.3125 ,-2.805 ,-0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);

        summonSmallArmorStand(-0.78125 ,-2.77375 ,-0.15625,0f,-30f,0f,new ItemStack(Material.RED_CARPET),loc,dir);
        summonSmallArmorStand(-0.78125 ,-2.7425 ,-0.1875,0f,10f,0f,new ItemStack(Material.GREEN_CARPET),loc,dir);
        summonSmallArmorStand(-0.71875 ,-2.71125 ,-0.1875,0f,-125f,0f,new ItemStack(Material.BLUE_CARPET),loc,dir);
        summonSmallArmorStand(-0.78125 ,-2.68 ,-0.21875,0f,40f,0f,new ItemStack(Material.YELLOW_CARPET),loc,dir);
        summonSmallArmorStand(-0.78125 ,-2.64875 ,-0.21875,0f,-5f,0f,new ItemStack(Material.MAGENTA_CARPET),loc,dir);
        summonSmallArmorStand(-0.84375 ,-2.6175 ,-0.21875,0f,25f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);
        summonSmallArmorStand(-0.8125 ,-2.58625 ,-0.25,0f,10f,0f,new ItemStack(Material.ORANGE_CARPET),loc,dir);
        summonSmallArmorStand(-0.78125 ,-2.555 ,-0.28125,0f,-35f,0f,new ItemStack(Material.CYAN_CARPET),loc,dir);
        summonSmallArmorStand(-0.8125 ,-2.52375 ,-0.21875,0f,35f,0f,new ItemStack(Material.LIGHT_BLUE_CARPET),loc,dir);
        summonSmallArmorStand(-0.8125 ,-2.4925 ,-0.28125,0f,-175f,0f,new ItemStack(Material.LIME_CARPET),loc,dir);
        summonSmallArmorStand(-0.466197 ,-2.61896 ,-0.102745,-60f,130f,0f,new ItemStack(Material.PINK_CARPET),loc,dir);
        summonArmorStand(0.434458 ,-3.30533 ,0.820228,-85f,20f,0f,new ItemStack(Material.SHEARS),loc,dir);
        this.dir = dir;
    }
    public void tableTest(Location loc, Direction dir) {
        Interaction in = loc.getWorld().spawn(loc, Interaction.class);
        in.setInteractionWidth(2);
        in.setInteractionHeight(2);
        this.entity = in;
        ItemStack item = new ItemStack(Material.DARK_OAK_SLAB);
        summonArmorStand(1.0625,-4.19133,0.0,-90,-90,0,item,loc,dir);
        summonArmorStand(-1.0625,-4.19133,0.0,90,-90,0,item,loc,dir);
        summonArmorStand(-0.9375 ,-3.81633 ,0.3125,0,-90,0,item,loc,dir);
        summonArmorStand(-0.9375 ,-3.81633 ,-0.3125,0,-90,0,item,loc,dir);
        summonArmorStand(-0.3125 ,-3.81633 ,0.3125,0,-90,0,item,loc,dir);
        summonArmorStand(0.3125 ,-3.81633 ,0.3125,0,-90,0,item,loc,dir);
        summonArmorStand(0.9375 ,-3.81633 ,0.3125,0,-90,0,item,loc,dir);
        summonArmorStand(0.9375 ,-3.81633 ,-0.3125,0,-90,0,item,loc,dir);
        summonArmorStand(0.3125 ,-3.81633 ,-0.3125,0,-90,0,item,loc,dir);
        summonArmorStand(-0.3125 ,-3.81633 ,-0.3125,0,-90,0,item,loc,dir);

        summonSmallArmorStand(0.0 ,-2.805 ,0.0,0f,-45f,0f,new ItemStack(Material.MAGENTA_CARPET),loc,dir);
        summonSmallArmorStand(-0.625 ,-2.805 ,0.0,0f,-45f,0f,new ItemStack(Material.MAGENTA_CARPET),loc,dir);
        summonSmallArmorStand(0.625 ,-2.805 ,0.0,0f,-45f,0f,new ItemStack(Material.MAGENTA_CARPET),loc,dir);

        summonSmallArmorStand(0.9375 ,-2.805 ,-0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);
        summonSmallArmorStand(0.9375 ,-2.805 ,0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);
        summonSmallArmorStand(-0.9375 ,-2.805 ,0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);
        summonSmallArmorStand(-0.9375 ,-2.805 ,-0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);
        summonSmallArmorStand(-0.3125 ,-2.805 ,0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);
        summonSmallArmorStand(0.3125 ,-2.805 ,0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);
        summonSmallArmorStand(0.3125 ,-2.805 ,-0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);
        summonSmallArmorStand(-0.3125 ,-2.805 ,-0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);

        summonSmallArmorStand(-0.78125 ,-2.77375 ,-0.15625,0f,-30f,0f,new ItemStack(Material.RED_CARPET),loc,dir);
        summonSmallArmorStand(-0.78125 ,-2.7425 ,-0.1875,0f,10f,0f,new ItemStack(Material.GREEN_CARPET),loc,dir);
        summonSmallArmorStand(-0.71875 ,-2.71125 ,-0.1875,0f,-125f,0f,new ItemStack(Material.BLUE_CARPET),loc,dir);
        summonSmallArmorStand(-0.78125 ,-2.68 ,-0.21875,0f,40f,0f,new ItemStack(Material.YELLOW_CARPET),loc,dir);
        summonSmallArmorStand(-0.78125 ,-2.64875 ,-0.21875,0f,-5f,0f,new ItemStack(Material.MAGENTA_CARPET),loc,dir);
        summonSmallArmorStand(-0.84375 ,-2.6175 ,-0.21875,0f,25f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);
        summonSmallArmorStand(-0.8125 ,-2.58625 ,-0.25,0f,10f,0f,new ItemStack(Material.ORANGE_CARPET),loc,dir);
        summonSmallArmorStand(-0.78125 ,-2.555 ,-0.28125,0f,-35f,0f,new ItemStack(Material.CYAN_CARPET),loc,dir);
        summonSmallArmorStand(-0.8125 ,-2.52375 ,-0.21875,0f,35f,0f,new ItemStack(Material.LIGHT_BLUE_CARPET),loc,dir);
        summonSmallArmorStand(-0.8125 ,-2.4925 ,-0.28125,0f,-175f,0f,new ItemStack(Material.LIME_CARPET),loc,dir);
        summonSmallArmorStand(-0.466197 ,-2.61896 ,-0.102745,-60f,130f,0f,new ItemStack(Material.PINK_CARPET),loc,dir);
        summonArmorStand(0.434458 ,-3.30533 ,0.820228,-85f,20f,0f,new ItemStack(Material.SHEARS),loc,dir);
        this.dir = dir;
    }
    public void blacksmithTiming(Location loc) {
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
        Interaction in = loc.getWorld().spawn(loc, Interaction.class);
        in.setInteractionWidth(2);
        in.setInteractionHeight(2);
        this.entity = in;
        summonArmorStand(-0.6875 ,-4.19133 ,0.0,-90f,-90f,0f,new ItemStack(Material.STONE_BRICK_SLAB), loc, dir);
        summonArmorStand(0.0 ,-3.81633 ,-0.625,0,0,0,new ItemStack(Material.STONE_BRICK_SLAB), loc, dir);
        summonArmorStand(0.625 ,-3.81633 ,-0.625,0,0,0,new ItemStack(Material.STONE_BRICK_SLAB), loc, dir);
        summonArmorStand(-0.625 ,-3.81633 ,-0.625,0,0,0,new ItemStack(Material.STONE_BRICK_SLAB), loc, dir);
        summonArmorStand(-0.625 ,-3.81633 ,0.625,0,0,0,new ItemStack(Material.STONE_BRICK_SLAB), loc, dir);
        summonArmorStand(0.0 ,-3.81633 ,0.625,0,0,0,new ItemStack(Material.STONE_BRICK_SLAB), loc, dir);
        summonArmorStand(0.625 ,-3.81633 ,0.625,0,0,0,new ItemStack(Material.STONE_BRICK_SLAB), loc, dir);
        summonArmorStand(0.6875 ,-4.19133 ,0.0,-90f,90f,0f,new ItemStack(Material.STONE_BRICK_SLAB), loc, dir);

        summonSmallArmorStand(-0.6875 ,-3.29543 ,0.1875,0f,-90f,0f, new ItemStack(Material.MAGMA_BLOCK), loc, dir);
        summonSmallArmorStand(-0.65625 ,-3.23293 ,-0.09375,0,0,0, new ItemStack(Material.MAGMA_BLOCK), loc, dir);
        summonSmallArmorStand(-0.21875 ,-3.13918 ,-0.09375,0f,-90f,0f, new ItemStack(Material.MAGMA_BLOCK), loc, dir);
        summonSmallArmorStand(-0.25 ,-3.20168 ,0.125,0,0,0, new ItemStack(Material.MAGMA_BLOCK), loc, dir);
        summonSmallArmorStand(0.1875 ,-3.17043 ,0.125,0,0,0, new ItemStack(Material.MAGMA_BLOCK), loc, dir);
        summonSmallArmorStand(0.625 ,-3.26418 ,0.125,0f,-90f,0f, new ItemStack(Material.MAGMA_BLOCK), loc, dir);
        summonSmallArmorStand(0.65625 ,-3.29543 ,-0.25,0f,-90f,0f, new ItemStack(Material.MAGMA_BLOCK), loc, dir);
        summonSmallArmorStand(0.21875 ,-3.20168 ,-0.09375,0,0,0, new ItemStack(Material.MAGMA_BLOCK), loc, dir);
        summonSmallArmorStand(0.6875 ,-3.38918 ,0.125,0f,-90f,0f, new ItemStack(Material.MAGMA_BLOCK), loc, dir);
        summonSmallArmorStand(-0.6875 ,-3.35793 ,-0.09375,0,0,0, new ItemStack(Material.MAGMA_BLOCK), loc, dir);

        summonArmorStand(0.03125 ,-4.19133 ,-0.03125,0,0,0, new ItemStack(Material.MAGMA_BLOCK), loc, dir);

        summonArmorStand(-1 ,-4.19133 ,0.0,-90f,-90f,0f,new ItemStack(Material.SMOOTH_STONE_SLAB), loc, dir);
        summonArmorStand(-0.625 ,-4.19133 ,0.5625,-90f,0f,0f,new ItemStack(Material.SMOOTH_STONE_SLAB), loc, dir);
        summonArmorStand(0.0 ,-4.19133 ,0.5625,-90f,0f,0f,new ItemStack(Material.SMOOTH_STONE_SLAB), loc, dir);
        summonArmorStand(0.625 ,-4.19133 ,0.5625,-90f,0f,0f,new ItemStack(Material.SMOOTH_STONE_SLAB), loc, dir);
        summonArmorStand(1.0 ,-4.19133 ,0.0,-90f,90f,0f,new ItemStack(Material.SMOOTH_STONE_SLAB), loc, dir);
        summonArmorStand(0.625 ,-4.19133 ,-0.5625,-90f,-180f,0f,new ItemStack(Material.SMOOTH_STONE_SLAB), loc, dir);
        summonArmorStand(0.0 ,-4.19133 ,-0.5625,-90f,-180f,0f,new ItemStack(Material.SMOOTH_STONE_SLAB), loc, dir);
        summonArmorStand(-0.625 ,-4.19133 ,-0.5625,-90f,-180f,0f,new ItemStack(Material.SMOOTH_STONE_SLAB), loc, dir);

        summonArmorStand(-1.21875 ,-3.81633 ,0.0,0,0,0, new ItemStack(Material.STONE_BRICK_SLAB), loc, dir);
        summonArmorStand(1.1875 ,-3.81633 ,0.0,0,0,0, new ItemStack(Material.STONE_BRICK_SLAB), loc, dir);

        summonSmallArmorStand(-1.125 ,-2.79543 ,-0.09375,0,0,0,new ItemStack(Material.STONE_BRICK_SLAB), loc, dir);
        summonSmallArmorStand(1.09375 ,-2.79543 ,-0.09375,0,0,0,new ItemStack(Material.STONE_BRICK_SLAB), loc, dir);
        summonSmallArmorStand(0.71875 ,-2.79543 ,-0.53125,0,0,0,new ItemStack(Material.STONE_BRICK_SLAB), loc, dir);
        summonSmallArmorStand(0.0 ,-2.57668 ,-0.53125,0,0,0,new ItemStack(Material.STONE_BRICK_SLAB), loc, dir);
        summonSmallArmorStand(-0.6875 ,-2.79543 ,-0.53125,0,0,0,new ItemStack(Material.STONE_BRICK_SLAB), loc, dir);
        summonSmallArmorStand(-0.4375 ,-2.57668 ,-0.53125,0,0,0,new ItemStack(Material.STONE_BRICK_SLAB), loc, dir);
        summonSmallArmorStand(0.4375 ,-2.57668 ,-0.53125,0,0,0,new ItemStack(Material.STONE_BRICK_SLAB), loc, dir);

        ParticleOptions po = new ParticleOptions((float) 0.2, (float) 0.2, (float) 0.2, 0.1F,2, 1F, (Color) null,null, (byte) 0);
        Location particleLoc = new Location(loc.getWorld(), loc.getX(), loc.getY() + 0.5, loc.getZ());
        new BukkitRunnable() {
            @Override
            public void run() {
                List<Player> players = new ArrayList<>();
                players.addAll(Bukkit.getOnlinePlayers());
                MagicSpells.getEffectManager().display(Particle.LAVA, po, particleLoc, 32.0D, players);
                loc.getWorld().playSound(loc, "block.fire.ambient", 1.0f, 0.0f);
            }
        }.runTaskTimer(plugin, 0L, 40L);


        this.dir = dir;
    }
    public void blacksmithTiming(Location loc, Direction dir) {

        Interaction in = loc.getWorld().spawn(loc, Interaction.class);
        in.setInteractionWidth(2);
        in.setInteractionHeight(2);
        this.entity = in;
        summonArmorStand(-0.6875 ,-4.19133 ,0.0,-90f,-90f,0f,new ItemStack(Material.STONE_BRICK_SLAB), loc, dir);
        summonArmorStand(0.0 ,-3.81633 ,-0.625,0,0,0,new ItemStack(Material.STONE_BRICK_SLAB), loc, dir);
        summonArmorStand(0.625 ,-3.81633 ,-0.625,0,0,0,new ItemStack(Material.STONE_BRICK_SLAB), loc, dir);
        summonArmorStand(-0.625 ,-3.81633 ,-0.625,0,0,0,new ItemStack(Material.STONE_BRICK_SLAB), loc, dir);
        summonArmorStand(-0.625 ,-3.81633 ,0.625,0,0,0,new ItemStack(Material.STONE_BRICK_SLAB), loc, dir);
        summonArmorStand(0.0 ,-3.81633 ,0.625,0,0,0,new ItemStack(Material.STONE_BRICK_SLAB), loc, dir);
        summonArmorStand(0.625 ,-3.81633 ,0.625,0,0,0,new ItemStack(Material.STONE_BRICK_SLAB), loc, dir);
        summonArmorStand(0.6875 ,-4.19133 ,0.0,-90f,90f,0f,new ItemStack(Material.STONE_BRICK_SLAB), loc, dir);

        summonSmallArmorStand(-0.6875 ,-3.29543 ,0.1875,0f,-90f,0f, new ItemStack(Material.MAGMA_BLOCK), loc, dir);
        summonSmallArmorStand(-0.65625 ,-3.23293 ,-0.09375,0,0,0, new ItemStack(Material.MAGMA_BLOCK), loc, dir);
        summonSmallArmorStand(-0.21875 ,-3.13918 ,-0.09375,0f,-90f,0f, new ItemStack(Material.MAGMA_BLOCK), loc, dir);
        summonSmallArmorStand(-0.25 ,-3.20168 ,0.125,0,0,0, new ItemStack(Material.MAGMA_BLOCK), loc, dir);
        summonSmallArmorStand(0.1875 ,-3.17043 ,0.125,0,0,0, new ItemStack(Material.MAGMA_BLOCK), loc, dir);
        summonSmallArmorStand(0.625 ,-3.26418 ,0.125,0f,-90f,0f, new ItemStack(Material.MAGMA_BLOCK), loc, dir);
        summonSmallArmorStand(0.65625 ,-3.29543 ,-0.25,0f,-90f,0f, new ItemStack(Material.MAGMA_BLOCK), loc, dir);
        summonSmallArmorStand(0.21875 ,-3.20168 ,-0.09375,0,0,0, new ItemStack(Material.MAGMA_BLOCK), loc, dir);
        summonSmallArmorStand(0.6875 ,-3.38918 ,0.125,0f,-90f,0f, new ItemStack(Material.MAGMA_BLOCK), loc, dir);
        summonSmallArmorStand(-0.6875 ,-3.35793 ,-0.09375,0,0,0, new ItemStack(Material.MAGMA_BLOCK), loc, dir);

        summonArmorStand(0.03125 ,-4.19133 ,-0.03125,0,0,0, new ItemStack(Material.MAGMA_BLOCK), loc, dir);

        summonArmorStand(-1 ,-4.19133 ,0.0,-90f,-90f,0f,new ItemStack(Material.SMOOTH_STONE_SLAB), loc, dir);
        summonArmorStand(-0.625 ,-4.19133 ,0.5625,-90f,0f,0f,new ItemStack(Material.SMOOTH_STONE_SLAB), loc, dir);
        summonArmorStand(0.0 ,-4.19133 ,0.5625,-90f,0f,0f,new ItemStack(Material.SMOOTH_STONE_SLAB), loc, dir);
        summonArmorStand(0.625 ,-4.19133 ,0.5625,-90f,0f,0f,new ItemStack(Material.SMOOTH_STONE_SLAB), loc, dir);
        summonArmorStand(1.0 ,-4.19133 ,0.0,-90f,90f,0f,new ItemStack(Material.SMOOTH_STONE_SLAB), loc, dir);
        summonArmorStand(0.625 ,-4.19133 ,-0.5625,-90f,-180f,0f,new ItemStack(Material.SMOOTH_STONE_SLAB), loc, dir);
        summonArmorStand(0.0 ,-4.19133 ,-0.5625,-90f,-180f,0f,new ItemStack(Material.SMOOTH_STONE_SLAB), loc, dir);
        summonArmorStand(-0.625 ,-4.19133 ,-0.5625,-90f,-180f,0f,new ItemStack(Material.SMOOTH_STONE_SLAB), loc, dir);

        summonArmorStand(-1.21875 ,-3.81633 ,0.0,0,0,0, new ItemStack(Material.STONE_BRICK_SLAB), loc, dir);
        summonArmorStand(1.1875 ,-3.81633 ,0.0,0,0,0, new ItemStack(Material.STONE_BRICK_SLAB), loc, dir);

        summonSmallArmorStand(-1.125 ,-2.79543 ,-0.09375,0,0,0,new ItemStack(Material.STONE_BRICK_SLAB), loc, dir);
        summonSmallArmorStand(1.09375 ,-2.79543 ,-0.09375,0,0,0,new ItemStack(Material.STONE_BRICK_SLAB), loc, dir);
        summonSmallArmorStand(0.71875 ,-2.79543 ,-0.53125,0,0,0,new ItemStack(Material.STONE_BRICK_SLAB), loc, dir);
        summonSmallArmorStand(0.0 ,-2.57668 ,-0.53125,0,0,0,new ItemStack(Material.STONE_BRICK_SLAB), loc, dir);
        summonSmallArmorStand(-0.6875 ,-2.79543 ,-0.53125,0,0,0,new ItemStack(Material.STONE_BRICK_SLAB), loc, dir);
        summonSmallArmorStand(-0.4375 ,-2.57668 ,-0.53125,0,0,0,new ItemStack(Material.STONE_BRICK_SLAB), loc, dir);
        summonSmallArmorStand(0.4375 ,-2.57668 ,-0.53125,0,0,0,new ItemStack(Material.STONE_BRICK_SLAB), loc, dir);

        ParticleOptions po = new ParticleOptions((float) 0.2, (float) 0.2, (float) 0.2, 0.1F,2, 1F, (Color) null,null, (byte) 0);
        Location particleLoc = new Location(loc.getWorld(), loc.getX(), loc.getY() + 0.5, loc.getZ());
        new BukkitRunnable() {
            @Override
            public void run() {
                List<Player> players = new ArrayList<>();
                players.addAll(Bukkit.getOnlinePlayers());
                MagicSpells.getEffectManager().display(Particle.LAVA, po, particleLoc, 32.0D, players);
                loc.getWorld().playSound(loc, "block.fire.ambient", 1.0f, 0.0f);
            }
        }.runTaskTimer(plugin, 0L, 40L);

        this.dir = dir;
    }
    public void tableTest2(Location loc) {
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
        Interaction in = loc.getWorld().spawn(loc, Interaction.class);
        in.setInteractionWidth(2);
        in.setInteractionHeight(2);
        this.entity = in;
        ItemStack item = new ItemStack(Material.DARK_OAK_SLAB);
        summonArmorStand(1.0625,-4.19133,0.0,-90,-90,0,item,loc,dir);
        summonArmorStand(-1.0625,-4.19133,0.0,90,-90,0,item,loc,dir);
        summonArmorStand(-0.9375 ,-3.81633 ,0.3125,0,-90,0,item,loc,dir);
        summonArmorStand(-0.9375 ,-3.81633 ,-0.3125,0,-90,0,item,loc,dir);
        summonArmorStand(-0.3125 ,-3.81633 ,0.3125,0,-90,0,item,loc,dir);
        summonArmorStand(0.3125 ,-3.81633 ,0.3125,0,-90,0,item,loc,dir);
        summonArmorStand(0.9375 ,-3.81633 ,0.3125,0,-90,0,item,loc,dir);
        summonArmorStand(0.9375 ,-3.81633 ,-0.3125,0,-90,0,item,loc,dir);
        summonArmorStand(0.3125 ,-3.81633 ,-0.3125,0,-90,0,item,loc,dir);
        summonArmorStand(-0.3125 ,-3.81633 ,-0.3125,0,-90,0,item,loc,dir);

        summonSmallArmorStand(0.0 ,-2.805 ,0.0,0f,-45f,0f,new ItemStack(Material.MAGENTA_CARPET),loc,dir);
        summonSmallArmorStand(-0.625 ,-2.805 ,0.0,0f,-45f,0f,new ItemStack(Material.MAGENTA_CARPET),loc,dir);
        summonSmallArmorStand(0.625 ,-2.805 ,0.0,0f,-45f,0f,new ItemStack(Material.MAGENTA_CARPET),loc,dir);

        summonSmallArmorStand(0.9375 ,-2.805 ,-0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);
        summonSmallArmorStand(0.9375 ,-2.805 ,0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);
        summonSmallArmorStand(-0.9375 ,-2.805 ,0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);
        summonSmallArmorStand(-0.9375 ,-2.805 ,-0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);
        summonSmallArmorStand(-0.3125 ,-2.805 ,0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);
        summonSmallArmorStand(0.3125 ,-2.805 ,0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);
        summonSmallArmorStand(0.3125 ,-2.805 ,-0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);
        summonSmallArmorStand(-0.3125 ,-2.805 ,-0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);

        item = new ItemStack(Material.PLAYER_HEAD);
        MMOItem.getHeadFrom64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmVjOTk4ZDc1NTVkY2ZkZWE2OWJkNTQ2NDUwMTIyYjY3MDM3ZjhkOGI5ZmM4MWUwNjAzYWRiYmVlZTUifX19", item);

        summonSmallArmorStand(0.5625 ,-2.79543 ,-0.21875,0f,-35f,0f,item,loc,dir);
        MMOItem.getHeadFrom64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmUxZjQ5MDFhYjc4ZmQxNjk5NmM0ZjgzMjgxMmE5M2RiZjI1ZTVkNDI4ODdlOWRkYTgzODQ3ODIxZWQ1OTcifX19", item);

        summonSmallArmorStand(0.5625 ,-2.79543 ,0.0625,0f,-130f,0f,item,loc,dir);
        MMOItem.getHeadFrom64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWFlNDI3ZDBhOGI0ODAxMzk3MmExZmRkOTE2ZGVlZWM1Mjc4MjIwODQ5NzJlMTEyNzUxMzliZmE1YzI1NCJ9fX0", item);

        summonSmallArmorStand(0.84375 ,-2.79543 ,-0.03125,0f,-35f,0f,item,loc,dir);
        MMOItem.getHeadFrom64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjM2NzZmMzYyYjM5MTQwODljMzkxN2U2YjJiODJhZTg0Zjk5MWNlMzMyMjljMWIyYjUyZTNhYzMyMmVjIn19fQ", item);

        summonSmallArmorStand(-0.28125 ,-2.79543 ,-0.15625,0f,-80f,0f,item,loc,dir);
        MMOItem.getHeadFrom64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTY0NmE2MmZlYzJkZDk3ZjVlNGFhMzI5YWU0NDZjNjAxMWE4ZThlMmQ4MTdhZWViOGZkYzExY2M1NGVjMmViZSJ9fX0=", item);

        summonSmallArmorStand(0.6875 ,-2.3598 ,-0.125,0f,-35f,0f,item,loc,dir);
        MMOItem.getHeadFrom64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTY3YzhiYzMzZjdiZTYyNmM0OGU3NzZmNjViNzFjM2E5NjI4ODQ1ODMzN2VmZjQwNTFlNjVkMzdmNzRhMyJ9fX0", item);

        summonSmallArmorStand(0.279994 ,-2.62043 ,-0.12625599999999998,-90f,-45f,0f,item,loc,dir);


        summonArmorStand(-0.441673 ,-3.20789 ,0.675404,-90f,-20f,0f,new ItemStack(Material.TRIPWIRE_HOOK),loc,dir);
        this.dir = dir;
    }
    public void tableTest2(Location loc, Direction dir) {
        Interaction in = loc.getWorld().spawn(loc, Interaction.class);
        in.setInteractionWidth(2);
        in.setInteractionHeight(2);
        this.entity = in;
        ItemStack item = new ItemStack(Material.DARK_OAK_SLAB);
        summonArmorStand(1.0625,-4.19133,0.0,-90,-90,0,item,loc,dir);
        summonArmorStand(-1.0625,-4.19133,0.0,90,-90,0,item,loc,dir);
        summonArmorStand(-0.9375 ,-3.81633 ,0.3125,0,-90,0,item,loc,dir);
        summonArmorStand(-0.9375 ,-3.81633 ,-0.3125,0,-90,0,item,loc,dir);
        summonArmorStand(-0.3125 ,-3.81633 ,0.3125,0,-90,0,item,loc,dir);
        summonArmorStand(0.3125 ,-3.81633 ,0.3125,0,-90,0,item,loc,dir);
        summonArmorStand(0.9375 ,-3.81633 ,0.3125,0,-90,0,item,loc,dir);
        summonArmorStand(0.9375 ,-3.81633 ,-0.3125,0,-90,0,item,loc,dir);
        summonArmorStand(0.3125 ,-3.81633 ,-0.3125,0,-90,0,item,loc,dir);
        summonArmorStand(-0.3125 ,-3.81633 ,-0.3125,0,-90,0,item,loc,dir);

        summonSmallArmorStand(0.0 ,-2.805 ,0.0,0f,-45f,0f,new ItemStack(Material.MAGENTA_CARPET),loc,dir);
        summonSmallArmorStand(-0.625 ,-2.805 ,0.0,0f,-45f,0f,new ItemStack(Material.MAGENTA_CARPET),loc,dir);
        summonSmallArmorStand(0.625 ,-2.805 ,0.0,0f,-45f,0f,new ItemStack(Material.MAGENTA_CARPET),loc,dir);

        summonSmallArmorStand(0.9375 ,-2.805 ,-0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);
        summonSmallArmorStand(0.9375 ,-2.805 ,0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);
        summonSmallArmorStand(-0.9375 ,-2.805 ,0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);
        summonSmallArmorStand(-0.9375 ,-2.805 ,-0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);
        summonSmallArmorStand(-0.3125 ,-2.805 ,0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);
        summonSmallArmorStand(0.3125 ,-2.805 ,0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);
        summonSmallArmorStand(0.3125 ,-2.805 ,-0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);
        summonSmallArmorStand(-0.3125 ,-2.805 ,-0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);

        item = new ItemStack(Material.PLAYER_HEAD);
        MMOItem.getHeadFrom64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmVjOTk4ZDc1NTVkY2ZkZWE2OWJkNTQ2NDUwMTIyYjY3MDM3ZjhkOGI5ZmM4MWUwNjAzYWRiYmVlZTUifX19", item);

        summonSmallArmorStand(0.5625 ,-2.79543 ,-0.21875,0f,-35f,0f,item,loc,dir);
        MMOItem.getHeadFrom64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmUxZjQ5MDFhYjc4ZmQxNjk5NmM0ZjgzMjgxMmE5M2RiZjI1ZTVkNDI4ODdlOWRkYTgzODQ3ODIxZWQ1OTcifX19", item);

        summonSmallArmorStand(0.5625 ,-2.79543 ,0.0625,0f,-130f,0f,item,loc,dir);
        MMOItem.getHeadFrom64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWFlNDI3ZDBhOGI0ODAxMzk3MmExZmRkOTE2ZGVlZWM1Mjc4MjIwODQ5NzJlMTEyNzUxMzliZmE1YzI1NCJ9fX0", item);

        summonSmallArmorStand(0.84375 ,-2.79543 ,-0.03125,0f,-35f,0f,item,loc,dir);
        MMOItem.getHeadFrom64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjM2NzZmMzYyYjM5MTQwODljMzkxN2U2YjJiODJhZTg0Zjk5MWNlMzMyMjljMWIyYjUyZTNhYzMyMmVjIn19fQ", item);

        summonSmallArmorStand(-0.28125 ,-2.79543 ,-0.15625,0f,-80f,0f,item,loc,dir);
        MMOItem.getHeadFrom64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTY0NmE2MmZlYzJkZDk3ZjVlNGFhMzI5YWU0NDZjNjAxMWE4ZThlMmQ4MTdhZWViOGZkYzExY2M1NGVjMmViZSJ9fX0=", item);

        summonSmallArmorStand(0.6875 ,-2.3598 ,-0.125,0f,-35f,0f,item,loc,dir);
        MMOItem.getHeadFrom64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTY3YzhiYzMzZjdiZTYyNmM0OGU3NzZmNjViNzFjM2E5NjI4ODQ1ODMzN2VmZjQwNTFlNjVkMzdmNzRhMyJ9fX0", item);

        summonSmallArmorStand(0.279994 ,-2.62043 ,-0.12625599999999998,-90f,-45f,0f,item,loc,dir);


        summonArmorStand(-0.441673 ,-3.20789 ,0.675404,-90f,-20f,0f,new ItemStack(Material.TRIPWIRE_HOOK),loc,dir);
        this.dir = dir;
    }
    public void tableTest3(Location loc) {
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
        Interaction in = loc.getWorld().spawn(loc, Interaction.class);
        in.setInteractionWidth(2);
        in.setInteractionHeight(2);
        this.entity = in;
        ItemStack item = new ItemStack(Material.DARK_OAK_SLAB);
        summonArmorStand(1.0625,-4.19133,0.0,-90,-90,0,item,loc,dir);
        summonArmorStand(-1.0625,-4.19133,0.0,90,-90,0,item,loc,dir);
        summonArmorStand(-0.9375 ,-3.81633 ,0.3125,0,-90,0,item,loc,dir);
        summonArmorStand(-0.9375 ,-3.81633 ,-0.3125,0,-90,0,item,loc,dir);
        summonArmorStand(-0.3125 ,-3.81633 ,0.3125,0,-90,0,item,loc,dir);
        summonArmorStand(0.3125 ,-3.81633 ,0.3125,0,-90,0,item,loc,dir);
        summonArmorStand(0.9375 ,-3.81633 ,0.3125,0,-90,0,item,loc,dir);
        summonArmorStand(0.9375 ,-3.81633 ,-0.3125,0,-90,0,item,loc,dir);
        summonArmorStand(0.3125 ,-3.81633 ,-0.3125,0,-90,0,item,loc,dir);
        summonArmorStand(-0.3125 ,-3.81633 ,-0.3125,0,-90,0,item,loc,dir);

        summonSmallArmorStand(0.0 ,-2.805 ,0.0,0f,-45f,0f,new ItemStack(Material.MAGENTA_CARPET),loc,dir);
        summonSmallArmorStand(-0.625 ,-2.805 ,0.0,0f,-45f,0f,new ItemStack(Material.MAGENTA_CARPET),loc,dir);
        summonSmallArmorStand(0.625 ,-2.805 ,0.0,0f,-45f,0f,new ItemStack(Material.MAGENTA_CARPET),loc,dir);

        summonSmallArmorStand(0.9375 ,-2.805 ,-0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);
        summonSmallArmorStand(0.9375 ,-2.805 ,0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);
        summonSmallArmorStand(-0.9375 ,-2.805 ,0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);
        summonSmallArmorStand(-0.9375 ,-2.805 ,-0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);
        summonSmallArmorStand(-0.3125 ,-2.805 ,0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);
        summonSmallArmorStand(0.3125 ,-2.805 ,0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);
        summonSmallArmorStand(0.3125 ,-2.805 ,-0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);
        summonSmallArmorStand(-0.3125 ,-2.805 ,-0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);

        summonSmallArmorStand(-0.25 ,-2.8675 ,0.0,0,0,0,new ItemStack(Material.QUARTZ_SLAB), loc,dir);
        summonSmallArmorStand(0.1875 ,-2.14875 ,0.0,0,0,0,new ItemStack(Material.QUARTZ_SLAB), loc,dir);
        summonSmallArmorStand(0.59375 ,-2.805 ,0.0,0,0,0,new ItemStack(Material.QUARTZ_BLOCK), loc,dir);
        summonSmallArmorStand(0.59375 ,-2.3675,0.0,0,-90f,0,new ItemStack(Material.QUARTZ_STAIRS), loc,dir);
        summonSmallArmorStand(0.1875 ,-2.89875 ,0.0,0,0,0,new ItemStack(Material.QUARTZ_SLAB), loc,dir);
        summonSmallArmorStand(-0.06542999999999999 ,-2.18293,0.0,90f,90f,0,new ItemStack(Material.QUARTZ_STAIRS), loc,dir);
        summonSmallArmorStand(0.03515599999999999 ,-2.91438 ,0.0,0,-90,0,new ItemStack(Material.IRON_BARS), loc,dir);
        summonSmallArmorStand(0.59375 ,-2.58918 ,0.21582,-90,0,0,new ItemStack(Material.STONE_BUTTON), loc,dir);
        summonSmallArmorStand(0.59375 ,-2.58918 ,-0.21582,90,0,0,new ItemStack(Material.STONE_BUTTON), loc,dir);
        this.dir = dir;
    }
    public void tableTest3(Location loc, Direction dir) {
        Interaction in = loc.getWorld().spawn(loc, Interaction.class);
        in.setInteractionWidth(2);
        in.setInteractionHeight(2);
        this.entity = in;
        ItemStack item = new ItemStack(Material.DARK_OAK_SLAB);
        summonArmorStand(1.0625,-4.19133,0.0,-90,-90,0,item,loc,dir);
        summonArmorStand(-1.0625,-4.19133,0.0,90,-90,0,item,loc,dir);
        summonArmorStand(-0.9375 ,-3.81633 ,0.3125,0,-90,0,item,loc,dir);
        summonArmorStand(-0.9375 ,-3.81633 ,-0.3125,0,-90,0,item,loc,dir);
        summonArmorStand(-0.3125 ,-3.81633 ,0.3125,0,-90,0,item,loc,dir);
        summonArmorStand(0.3125 ,-3.81633 ,0.3125,0,-90,0,item,loc,dir);
        summonArmorStand(0.9375 ,-3.81633 ,0.3125,0,-90,0,item,loc,dir);
        summonArmorStand(0.9375 ,-3.81633 ,-0.3125,0,-90,0,item,loc,dir);
        summonArmorStand(0.3125 ,-3.81633 ,-0.3125,0,-90,0,item,loc,dir);
        summonArmorStand(-0.3125 ,-3.81633 ,-0.3125,0,-90,0,item,loc,dir);

        summonSmallArmorStand(0.0 ,-2.805 ,0.0,0f,-45f,0f,new ItemStack(Material.MAGENTA_CARPET),loc,dir);
        summonSmallArmorStand(-0.625 ,-2.805 ,0.0,0f,-45f,0f,new ItemStack(Material.MAGENTA_CARPET),loc,dir);
        summonSmallArmorStand(0.625 ,-2.805 ,0.0,0f,-45f,0f,new ItemStack(Material.MAGENTA_CARPET),loc,dir);

        summonSmallArmorStand(0.9375 ,-2.805 ,-0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);
        summonSmallArmorStand(0.9375 ,-2.805 ,0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);
        summonSmallArmorStand(-0.9375 ,-2.805 ,0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);
        summonSmallArmorStand(-0.9375 ,-2.805 ,-0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);
        summonSmallArmorStand(-0.3125 ,-2.805 ,0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);
        summonSmallArmorStand(0.3125 ,-2.805 ,0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);
        summonSmallArmorStand(0.3125 ,-2.805 ,-0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);
        summonSmallArmorStand(-0.3125 ,-2.805 ,-0.3125,0f,-45f,0f,new ItemStack(Material.PURPLE_CARPET),loc,dir);

        summonSmallArmorStand(-0.25 ,-2.8675 ,0.0,0,0,0,new ItemStack(Material.QUARTZ_SLAB), loc,dir);
        summonSmallArmorStand(0.1875 ,-2.14875 ,0.0,0,0,0,new ItemStack(Material.QUARTZ_SLAB), loc,dir);
        summonSmallArmorStand(0.59375 ,-2.805 ,0.0,0,0,0,new ItemStack(Material.QUARTZ_BLOCK), loc,dir);
        summonSmallArmorStand(0.59375 ,-2.3675,0.0,0,-90f,0,new ItemStack(Material.QUARTZ_STAIRS), loc,dir);
        summonSmallArmorStand(0.1875 ,-2.89875 ,0.0,0,0,0,new ItemStack(Material.QUARTZ_SLAB), loc,dir);
        summonSmallArmorStand(-0.06542999999999999 ,-2.18293,0.0,90f,90f,0,new ItemStack(Material.QUARTZ_STAIRS), loc,dir);
        summonSmallArmorStand(0.03515599999999999 ,-2.91438 ,0.0,0,-90,0,new ItemStack(Material.IRON_BARS), loc,dir);
        summonSmallArmorStand(0.59375 ,-2.58918 ,0.21582,-90,0,0,new ItemStack(Material.STONE_BUTTON), loc,dir);
        summonSmallArmorStand(0.59375 ,-2.58918 ,-0.21582,90,0,0,new ItemStack(Material.STONE_BUTTON), loc,dir);
        this.dir = dir;
    }
    public void summonSmallArmorStand(double x, double y, double z, double xAngle, double yAngle, double zAngle, ItemStack item, Location loc, Direction dir) {
        summonArmorStandHead(x,y,z,xAngle,yAngle,zAngle,true,item,loc,dir);
    }
    public void summonArmorStand(double x, double y, double z, double xAngle, double yAngle, double zAngle, ItemStack item, Location loc, Direction dir) {
        summonArmorStandHead(x,y,z,xAngle,yAngle,zAngle,false,item,loc,dir);
    }
    public void summonArmorStandHead(double x, double y, double z, double xAngle, double yAngle, double zAngle, boolean small, ItemStack item, Location loc, Direction direction) {


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
            //W
            double newX = -z;
            double newZ = x;
            /*double newXangle = -zAngle;
            double newZAngle = xAngle;*/
            x = newX;
            z = newZ;
            /*xAngle = newXangle;
            zAngle = newZAngle;*/
            loc.setYaw(90);
        } else if (direction == Direction.E) {
            //E
            double newX = z;
            double newZ = -x;
            /*double newXangle = zAngle;
            double newZAngle = -xAngle;*/
            x = newX;
            z = newZ;
            /*xAngle = newXangle;
            zAngle = newZAngle;*/
            loc.setYaw(-90);
        }

        ArmorStand as = (ArmorStand) loc.getWorld().spawn(loc, ArmorStand.class);

        as.setGravity(false);
        as.setMarker(true);
        as.setVisible(false);
        as.setCollidable(true);
        as.setSmall(small);
        as.teleport(as.getLocation().add(x,y+2.5,z));
        as.setHeadPose(new EulerAngle(xAngle, yAngle, zAngle));
        as.setHelmet(item);
        this.displayStands.add(as);
    }

    private Location getLocationForDir(Location loc, Vector offset, Direction dir) {
        double newX = offset.getX();
        double newZ = offset.getZ();
        if (dir == Direction.S) {
            loc.setYaw(0);
        } else if (dir == Direction.N) {
            newX = -offset.getX();
            newZ = -offset.getZ();
            loc.setYaw(180);
        } else if (dir == Direction.W) {
            newX = -offset.getZ();
            newZ = offset.getX();
            loc.setYaw(90);
        } else if (dir == Direction.E) {
            //E
            newX = offset.getZ();
            newZ = -offset.getX();
            loc.setYaw(-90);
        }
        Vector newOffset = new Vector(newX, offset.getY(), newZ);
        return loc.clone().add(newOffset);
    }

    public String getName() {
        return this.name;
    }
    public XpType getType() {
        return this.type;
    }
}
