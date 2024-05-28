package me.genn.thegrandtourney.grid;

public class Cell {
    public boolean isOccupied = false;
    public boolean isRoad;
    public District district;
    public int x;
    public int z;
    public String schematicAtCell;
    public PasteDetails pasteDetails;
    public RoadTier roadTier = RoadTier.NOT;
    public boolean isRiver = false;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Cell)) {
            return false;
        }
        Cell cell = (Cell)o;
        if (cell.x == this.x && cell.z == this.z) {
            return true;
        } else {
            return false;
        }

    }



}
