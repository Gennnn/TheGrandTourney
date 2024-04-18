package me.genn.thegrandtourney.skills;

import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.skills.farming.Crop;
import me.genn.thegrandtourney.skills.foraging.ForagingZone;
import me.genn.thegrandtourney.xp.Xp;
import me.genn.thegrandtourney.xp.XpType;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class TableHandler {
    public List<HoldingTable> allHoldingTables;
    public List<MashingTable> allMashingTables;
    public List<TimingTable> allTimingTables;
    public List<Station> allStations;
    TGT plugin;
    
    public TableHandler(TGT plugin) {
        this.plugin = plugin;
        this.allTimingTables = new ArrayList<>();
        this.allMashingTables = new ArrayList<>();
        this.allHoldingTables = new ArrayList<>();
        this.allStations = new ArrayList<>();
    }

    private List<Station> listOfStationsWithType(final XpType type){
        return allStations.stream().filter(o -> o.type == type).toList();
    }

    private Station getClosestStation(List<Station> stations, Location originLoc) {
        Station station = stations.get(0);
        Location minLoc = station.centerLoc;
        for (int i = 1; i<stations.size(); i++) {
            if (stations.get(i).centerLoc.distanceSquared(originLoc) < minLoc.distanceSquared(originLoc)) {
                minLoc = stations.get(i).centerLoc;
                station = stations.get(i);
            }
        }
        return station;
    }

    public Station getStationForCraft(XpType type, Location originLoc) {
        List<Station> stations = listOfStationsWithType(type);
        if (stations.size() == 0) {
            return null;
        } else if (stations.size() == 1) {
            return stations.get(0);
        } else {
            Station station = getClosestStation(stations, originLoc);
            return station;
        }
    }
    public Station getStationForObj(String name, Location originLoc) {
        XpType type = Xp.parseXpType(name);
        if (type == null) {
            return null;
        }
        return getStationForCraft(type, originLoc);
    }
    public HoldingTable findClosestHoldingTableOfType(XpType type, Location loc) {
        List<HoldingTable> tables = listOfHoldingTablesWithType(allHoldingTables, type);
        if (tables.size() == 0) {
            return null;
        } else if (tables.size() == 1) {
            return tables.get(0);
        } else {
            HoldingTable table = getClosestHoldingTable(tables, loc);
            return table;
        }
    }
    private List<HoldingTable> listOfHoldingTablesWithType(final List<HoldingTable> list, final XpType type){
        return list.stream().filter(o -> o.getType().equals(type)).toList();
    }
    private HoldingTable getClosestHoldingTable(List<HoldingTable> tables, Location originLoc) {
        HoldingTable table = tables.get(0);
        Location minLoc = table.loc;
        for (int i = 1; i<tables.size(); i++) {
            if (tables.get(i).loc.distanceSquared(originLoc) < minLoc.distanceSquared(originLoc)) {
                minLoc = tables.get(i).loc;
                table = tables.get(i);
            }
        }
        return table;
    }


    public MashingTable findClosestMashingTableOfType(XpType type, Location loc) {
        List<MashingTable> tables = listOfMashingTablesWithType(allMashingTables, type);
        if (tables.size() == 0) {
            return null;
        } else if (tables.size() == 1) {
            return tables.get(0);
        } else {
            MashingTable table = getClosestMashingTable(tables, loc);
            return table;
        }
    }
    private List<MashingTable> listOfMashingTablesWithType(final List<MashingTable> list, final XpType type){
        return list.stream().filter(o -> o.getType().equals(type)).toList();
    }
    private MashingTable getClosestMashingTable(List<MashingTable> tables, Location originLoc) {
        MashingTable table = tables.get(0);
        Location minLoc = table.loc;
        for (int i = 1; i<tables.size(); i++) {
            if (tables.get(i).loc.distanceSquared(originLoc) < minLoc.distanceSquared(originLoc)) {
                minLoc = tables.get(i).loc;
                table = tables.get(i);
            }
        }
        return table;
    }


    public TimingTable findClosestTimingTableOfType(XpType type, Location loc) {
        List<TimingTable> tables = listOfTimingTablesWithType(allTimingTables, type);
        if (tables.size() == 0) {
            return null;
        } else if (tables.size() == 1) {
            return tables.get(0);
        } else {
            TimingTable table = getClosestTimingTable(tables, loc);
            return table;
        }
    }
    private List<TimingTable> listOfTimingTablesWithType(final List<TimingTable> list, final XpType type){
        return list.stream().filter(o -> o.getType().equals(type)).toList();
    }
    private TimingTable getClosestTimingTable(List<TimingTable> tables, Location originLoc) {
        TimingTable table = tables.get(0);
        Location minLoc = table.loc;
        for (int i = 1; i<tables.size(); i++) {
            if (tables.get(i).loc.distanceSquared(originLoc) < minLoc.distanceSquared(originLoc)) {
                minLoc = tables.get(i).loc;
                table = tables.get(i);
            }
        }
        return table;
    }
}
