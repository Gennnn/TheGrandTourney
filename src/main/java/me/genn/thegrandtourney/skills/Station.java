package me.genn.thegrandtourney.skills;

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
import me.genn.thegrandtourney.npc.TGTNpc;
import me.genn.thegrandtourney.skills.fishing.FishingZone;
import me.genn.thegrandtourney.xp.XpType;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

public class Station {
    public HoldingTable holdingTable;
    public MashingTable mashingTable;
    public TimingTable timingTable;
    public String name;
    double boundingRange;
    Location centerLoc;
    TGT plugin;
    public XpType type;
    public Location spawnLocation;
    public Location minLoc;
    public Location maxLoc;

    public Station(TGT plugin, XpType type) {
        this.plugin = plugin;
        this.type = type;
    }

    public void create(org.bukkit.entity.Player p) {
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

        Location loc = new Location(p.getWorld(), region.getCenter().getX(), region.getCenter().getY(), region.getCenter().getZ());
        this.minLoc = new Location(p.getWorld(), region.getMinimumPoint().getX(), region.getMinimumPoint().getY(), region.getMinimumPoint().getZ());
        this.maxLoc = new Location(p.getWorld(), region.getMaximumPoint().getX(), region.getMaximumPoint().getY(), region.getMaximumPoint().getZ());
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
        this.holdingTable = plugin.tableHandler.findClosestHoldingTableOfType(type, loc);
        this.mashingTable = plugin.tableHandler.findClosestMashingTableOfType(type, loc);
        this.timingTable = plugin.tableHandler.findClosestTimingTableOfType(type, loc);
        this.centerLoc = loc.toCenterLocation();
        this.boundingRange = this.centerLoc.distance(new Location(p.getWorld(), region.getMinimumPoint().getX(), region.getMinimumPoint().getY(), region.getMinimumPoint().getZ()));
        this.spawnLocation = p.getLocation().toCenterLocation();
        plugin.stationList.put(this.centerLoc,this);
        p.sendMessage("Created zone of type " + this.type + " with center loc " + loc.getX() + "," + loc.getY() + "," + loc.getZ());
    }



}
