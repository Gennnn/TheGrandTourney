package me.genn.thegrandtourney.grid;

import java.util.ArrayList;
import java.util.List;

public class PasteDetails {
    public String schematicName;
    public Direction schematicFacing;
    public long pasteTime;
    public List<Cell> cellsOccupied = new ArrayList<>();
    public boolean isOmni = false;

    public PasteDetails(String schemName, Direction schemFacing, long time, List<Cell> cells, boolean isOmni) {
        this.schematicName = schemName;
        this.schematicFacing = schemFacing;
        this.pasteTime = time;
        this.cellsOccupied = cells;
        this.isOmni = isOmni;
    }
}
