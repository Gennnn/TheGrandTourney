package me.genn.thegrandtourney.skills;

import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.grid.Direction;
import me.genn.thegrandtourney.skills.tailoring.Table;
import me.genn.thegrandtourney.xp.XpType;
import org.bukkit.Location;

public class HoldingTable extends Table {
        public void spawn(Location loc, String name, XpType type, TGT plugin) {
                this.register(loc,name,type, plugin);
                if (type == XpType.TAILORING) {
                        this.tableTest3(loc);
                } else if (type == XpType.BLACKSMITHING) {
                        this.blacksmithHolding(loc);
                }

                plugin.tableHandler.allHoldingTables.add(this);
        }
        public void spawn(Location loc, String name, XpType type, TGT plugin, Direction dir) {
                this.register(loc,name,type, plugin);
                if (type == XpType.TAILORING) {
                        this.tableTest3(loc, dir);
                } else if (type == XpType.BLACKSMITHING) {
                        this.blacksmithHolding(loc, dir);
                }
                plugin.tableHandler.allHoldingTables.add(this);
        }


}
