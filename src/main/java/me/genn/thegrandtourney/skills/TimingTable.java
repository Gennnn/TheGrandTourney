package me.genn.thegrandtourney.skills;

import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.grid.Direction;
import me.genn.thegrandtourney.xp.XpType;
import org.bukkit.Location;

public class TimingTable extends Table {

    public void spawn(Location loc, String name, XpType type, TGT plugin) {
        this.register(loc,name,type, plugin);
        if (type == XpType.TAILORING) {
            this.tableTest2(loc);
        } else if (type == XpType.BLACKSMITHING) {
            this.blacksmithTiming(loc);
        } else if (type == XpType.ALCHEMY) {
            this.alchemyTiming(loc);
        }

        plugin.tableHandler.allTimingTables.add(this);
    }
    public void spawn(Location loc, String name, XpType type, TGT plugin, Direction dir) {
        this.register(loc,name,type, plugin);
        if (type == XpType.TAILORING) {
            this.tableTest2(loc, dir);
        } else if (type == XpType.BLACKSMITHING) {
            this.blacksmithTiming(loc, dir);
        } else if (type == XpType.ALCHEMY) {
            this.alchemyTiming(loc,dir);
        }
        plugin.tableHandler.allTimingTables.add(this);
    }




}
