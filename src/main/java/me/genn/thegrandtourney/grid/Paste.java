package me.genn.thegrandtourney.grid;

public class Paste {
    public Schematic schematic;
    int x;
    int z;
    Direction direction;
    int targetCellX;
    int targetCellZ;

    public Paste(Schematic schematic, int x, int z, Direction direction, int targetCellX, int targetCellZ) {
        this.schematic = schematic;
        this.x = x;
        this.z = z;
        this.direction = direction;
        this.targetCellX = targetCellX;
        this.targetCellZ = targetCellZ;
    }
}
