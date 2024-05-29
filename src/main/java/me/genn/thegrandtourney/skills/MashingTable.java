package me.genn.thegrandtourney.skills;

import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.grid.Direction;
import me.genn.thegrandtourney.xp.XpType;
import org.bukkit.Location;

public class MashingTable extends Table {
    public void spawn(Location loc, String name, XpType type, TGT plugin) {
        this.register(loc,name,type, plugin);
        if (type == XpType.TAILORING) {
            this.tableTest(loc);
        } else if (type == XpType.BLACKSMITHING) {
            this.blacksmithMashing(loc);
        } else if (type == XpType.ALCHEMY) {
            this.alchemyMashing(loc);
        }
        plugin.tableHandler.allMashingTables.add(this);
    }
    public void spawn(Location loc, String name, XpType type, TGT plugin, Direction dir) {
        this.register(loc,name,type, plugin);
        if (type == XpType.TAILORING) {
            this.tableTest(loc,dir);
        } else if (type == XpType.BLACKSMITHING) {
            this.blacksmithMashing(loc,dir);
        }else if (type == XpType.ALCHEMY) {
            this.alchemyMashing(loc,dir);
        }
        plugin.tableHandler.allMashingTables.add(this);
    }
}
