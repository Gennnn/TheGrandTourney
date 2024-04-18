package me.genn.thegrandtourney.skills.fishing;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.SessionManager;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.world.World;
import io.lumine.mythic.api.mobs.entities.SpawnReason;
import io.lumine.mythic.bukkit.MythicBukkit;
import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.item.MMOItem;
import me.genn.thegrandtourney.skills.TournamentObject;
import me.genn.thegrandtourney.skills.TournamentZone;
import me.genn.thegrandtourney.skills.farming.CropTemplate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;


public class FishingZone implements Listener, TournamentZone {
    public Location minLoc;
    public Location maxLoc;
    public FishingZoneTemplate template;
    public String name;
    TGT plugin;
    Random r;
    Map<Player, Fish> queuedFish;
    public Location centerLocation;

    public FishingZone(FishingZoneTemplate template, TGT plugin) {
        this.template = template;
        this.plugin = plugin;
        this.r = new Random();
        this.queuedFish = new HashMap<>();

    }
    @Override
    public void spawn(org.bukkit.entity.Player p) {
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
        FishingZone zone = new FishingZone(template, plugin);
        zone.name = template.name + (plugin.fishingZoneList.size()+1);
        p.sendMessage("Defining fishing zone " + zone.template.name + " with coords " + min.getX() + "," + min.getY() + "," + min.getZ() + " minimum and " + max.getX() + "," + max.getY() + "," + max.getZ() + " maximum");
        this.minLoc = new Location(p.getWorld(), min.getX(), min.getY(), min.getZ()).toCenterLocation();
        this.maxLoc = new Location(p.getWorld(), max.getX(), max.getY(), max.getZ()).toCenterLocation();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        plugin.fishingZoneList.add(this);
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
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.centerLocation = new Location(this.minLoc.getWorld(), this.minLoc.getX()+(this.maxLoc.getX()- this.minLoc.getX())*0.5,this.minLoc.getY()+(this.maxLoc.getY()- this.minLoc.getY())*0.5,this.minLoc.getZ()+(this.maxLoc.getZ()- this.minLoc.getZ())*0.5);
        plugin.fishingZoneHandler.allSpawnedZones.add(this);

    }
    @Override
    public void remove() {
        this.plugin.fishingZoneList.remove(this);
        this.minLoc = null;
        this.maxLoc = null;
        this.centerLocation = null;
        HandlerList.unregisterAll(this);
        this.name = null;
        this.queuedFish.clear();
    }
    @EventHandler
    public void onFish(PlayerFishEvent e) {
        Location hookLoc = e.getHook().getLocation().toCenterLocation();
        if (e.getState() == PlayerFishEvent.State.FISHING && withinZone(hookLoc)) {
            FishHook hook = e.getHook();
            hook.setApplyLure(false);
            hook.setRainInfluenced(false);
            hook.setSkyInfluenced(false);
            int min = (int)calculateMinWaitModifiers(e.getPlayer());
            int max = (int)calculateMaxWaitModifiers(e.getPlayer());
            if (min < 0) {
                min = 0;
            }
            if (max < 0 || max < min) {
                max = min + 1;
            }
            hook.setWaitTime(min, max);
            Fish fish = template.selectDrop(e.getPlayer());
            this.queuedFish.put(e.getPlayer(), fish);
            min = (int)calculateMinLureModifiers(e.getPlayer(), fish);
            max = (int)calculateMaxLureModifiers(e.getPlayer(), fish);
            if (min < 5) {
                min = 5;
            }
            if (max < 5 || max < min) {
                max = min + 1;
            }
            hook.setLureTime(min, max);
        } else if (e.getState() == PlayerFishEvent.State.CAUGHT_FISH && withinZone(hookLoc) && this.queuedFish.containsKey(e.getPlayer())) {
            Fish fish = this.queuedFish.get(e.getPlayer());
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

    public boolean withinZone(Location loc) {
        return loc.getX() <= maxLoc.getX() &&
                loc.getX() >= minLoc.getX() &&
                loc.getY() <= maxLoc.getY() &&
                loc.getY() >= minLoc.getY() &&
                loc.getZ() <= maxLoc.getZ() &&
                loc.getZ() >= minLoc.getZ();
    }

    public float calculateMinWaitModifiers(Player player) {
        return 100 + (template.minTimeModifier*20L) - (plugin.players.get(player.getUniqueId()).getLure() * 5L);
    }
    public float calculateMaxWaitModifiers(Player player) {
        return 600 + (template.minTimeModifier*20L) - (plugin.players.get(player.getUniqueId()).getLure() * 5L);
    }

    public float calculateMinLureModifiers(Player player, Fish fish) {
        return (fish.minTime*20L) - (plugin.players.get(player.getUniqueId()).getFlash() * 1L);
    }
    public float calculateMaxLureModifiers(Player player, Fish fish) {
        return (fish.maxTime*20L) - (plugin.players.get(player.getUniqueId()).getFlash() * 1L);
    }

    public String getName() {
        return template.getName();
    }
}
