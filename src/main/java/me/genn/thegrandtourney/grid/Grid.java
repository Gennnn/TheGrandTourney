package me.genn.thegrandtourney.grid;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

import com.sk89q.worldedit.EditSession;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.DataException;
import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.dungeons.*;
import me.genn.thegrandtourney.mobs.Spawner;
import me.genn.thegrandtourney.npc.TGTNpc;
import me.genn.thegrandtourney.skills.HoldingTable;
import me.genn.thegrandtourney.skills.MashingTable;
import me.genn.thegrandtourney.skills.Station;
import me.genn.thegrandtourney.skills.TimingTable;
import me.genn.thegrandtourney.skills.farming.Crop;
import me.genn.thegrandtourney.skills.fishing.FishingZone;
import me.genn.thegrandtourney.skills.foraging.ForagingZone;
import me.genn.thegrandtourney.skills.mining.Ore;
import me.genn.thegrandtourney.xp.Xp;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;


public class Grid {
    public int blocksPerCell;
    public int size = 64;
    public Cell[][] grid;
    public double yLvl = 65.0D;
    public double startX = 0.0D;
    public double startZ = -46.0D;
    public int cellSize = 5;
    public byte ADark = 1;
    public byte ALight = 4;
    public byte SDark = 7;
    public byte SLight = 8;
    public byte FDark = 13;
    public byte FLight = 5;
    public byte PDark = 9;
    public byte PLight = 3;
    public Map<Integer, Cell> highways;
    public List<Cell> cellsForBuildings;
    public boolean districtCheck = false;
    public int numBackRoads = 5;
    SchematicHandler schemHandler;
    List<Direction> potentialDirections = Arrays.asList(Direction.N, Direction.S, Direction.E, Direction.W);
    public List<Cell> portCells;
    public List<Cell> farmCells;
    public List<Cell> aristocracyCells;
    public List<Cell> slumsCells;
    public List<Cell> outskirtsCells;
    TGT plugin;
    Random r;
    public List<Schematic> localFarmRepeatable;
    public List<Schematic> localPortRepeatable;
    public List<Schematic> localOutskirtsRepeatable;
    public List<Schematic> localAristocracyRepeatable;
    public List<Schematic> localSlumsRepeatable;

    public List<Schematic> localFarmOmni;
    public List<Schematic> localPortOmni;
    public List<Schematic> localOutskirtsOmni;
    public List<Schematic> localAristocracyOmni;
    public List<Schematic> localSlumsOmni;
    public int runningDelay;
    public File schematicDetailsDirectory;
    public int oceanXMin;
    public int oceanXMax;
    public int oceanZMin;
    public int oceanZMax;
    long lastGeneration;
    boolean generationCompleted = false;
    List<int[]> blacklistedCells = new ArrayList<>();


    public Grid(SchematicHandler schematicHandler, TGT plugin) {
        this.schemHandler = schematicHandler;
        this.plugin = plugin;
        this.r = new Random();
        this.schematicDetailsDirectory = new File(plugin.getDataFolder(), "schematic-contents");
    }

    public void initialize() throws DataException, WorldEditException, IOException {
        grid = new Cell[size][size + (int)(0.5*size)];
        highways = new HashMap();
        this.portCells = new ArrayList<>();
        this.aristocracyCells = new ArrayList<>();
        this.farmCells = new ArrayList<>();
        this.slumsCells = new ArrayList<>();
        this.outskirtsCells = new ArrayList<>();
        this.localFarmRepeatable = new ArrayList<>();
        this.localPortRepeatable = new ArrayList<>();
        this.localOutskirtsRepeatable = new ArrayList<>();
        this.localAristocracyRepeatable = new ArrayList<>();
        this.localSlumsRepeatable = new ArrayList<>();

        this.localFarmOmni = new ArrayList<>();
        this.localPortOmni = new ArrayList<>();
        this.localOutskirtsOmni = new ArrayList<>();
        this.localAristocracyOmni = new ArrayList<>();
        this.localSlumsOmni = new ArrayList<>();
        cellsForBuildings = new ArrayList();
        int cellNum = 0;
        for (int z = 0; z < size + (int)(0.5*size); z++) {
            for (int x = 0; x<size; x++) {
                Cell cell = new Cell();
                cell.isRoad = false;
                cell.isOccupied = false;
                grid[x][z] = cell;
                cell.x = x;
                cell.z = z;
                cellsForBuildings.add(cell);
                cellNum++;
            }
        }
        this.setHighways(grid);
        this.assignDistricts(grid);
        if (districtCheck) {
            for (int z = 0; z < size; z++) {
                for (int x = 0; x<size; x++) {
                    Cell cell = grid[x][z];
                    Block cellBlock = (new Location(((World)Bukkit.getWorlds().get(0)), startX + (x * cellSize), yLvl, startZ + (z * -cellSize))).getBlock();

                    if (cell.district == District.ARISTOCRACY) {
                        if (x % 2 != 0) {
                            this.fillCellWithData(ALight, x, z);
                        } else {
                            this.fillCellWithData(ADark, x, z);
                        }
                    } else if (cell.district == District.PORT) {
                        if (x % 2 != 0) {
                            this.fillCellWithData(PLight, x, z);
                        } else {
                            this.fillCellWithData(PDark, x, z);
                        }
                    } else if (cell.district == District.FARM) {
                        if (x % 2 != 0) {
                            this.fillCellWithData(FLight, x, z);
                        } else  {
                            this.fillCellWithData(FDark, x, z);
                        }
                    } else if (cell.district == District.SLUMS) {
                        if (x % 2 != 0) {
                            this.fillCellWithData(SLight, x, z);
                        } else {
                            this.fillCellWithData(SDark, x, z);
                        }
                    }
                }
            }
        }
        for (int[] coords : this.blacklistedCells) {
            Cell cell = grid[coords[0]][coords[1]];
            cell.isOccupied = true;
        }
        for (int z = 0; z < size + (int)(0.5*size); z++) {
            for (int x = 0; x<size; x++) {
                Cell cell = grid[x][z];
                if (!cell.isOccupied && !cell.isRoad) {
                    //generate
                } else if (cell.isRoad) {
                    this.fillCellWithBlock(Material.SMOOTH_STONE, x, z );
                    cell.isOccupied = true;
                    highways.put(highways.size(), cell);
                }
            }
        }
        this.generateSideRoads(grid);
        this.generateBackRoads(grid);
        this.generateStructure(getSchematicsForDistrict(District.PORT), grid, getCellsForDistrict(District.PORT), District.PORT);

    }

    public void fillCellWithBlock(Material block, int gridX, int gridZ) {
        Location startLoc = new Location(((World)Bukkit.getWorlds().get(0)), startX + (gridX * cellSize), yLvl, startZ + (gridZ * -cellSize));
        for (int x = 0; x < cellSize; x++) {
            for (int z = 0; z > -cellSize; z--) {
                Location changeLoc = new Location(startLoc.getWorld(), startLoc.getX() + Double.valueOf(x), yLvl, startLoc.getZ() + Double.valueOf(z));
                //Bukkit.broadcastMessage("Attempting to change block at coordinates: " + changeLoc.getX() + ", " + changeLoc.getY() + ", " + changeLoc.getZ());
                Block blockLoc = changeLoc.getBlock();
                blockLoc.setType(block);

            }
        }
    }

    public void fillCellWithData(byte data, int gridX, int gridZ) {
        Location startLoc = new Location(((World)Bukkit.getWorlds().get(0)), startX + (gridX * cellSize), yLvl, startZ + (gridZ * -cellSize));
        for (int x = 0; x < cellSize; x++) {
            for (int z = 0; z > -cellSize; z--) {
                Location changeLoc = new Location(startLoc.getWorld(), startLoc.getX() + Double.valueOf(x), yLvl, startLoc.getZ() + Double.valueOf(z));
                //Bukkit.broadcastMessage("Attempting to change data at coordinates: " + changeLoc.getX() + ", " + changeLoc.getY() + ", " + changeLoc.getZ());
                Block blockLoc = changeLoc.getBlock();


            }
        }
    }

    public void setHighways(Cell[][] grid) {
        for (int x = 0; x < size; x++) {
            for (int z = (size/2)-1; z < (size/2)+1; z++) {
                grid[x][z].isRoad = true;
            }
        }
        for (int x = (size/2)-1; x < (size/2)+1; x++) {
            for (int z = 0; z < size; z++) {
                grid[x][z].isRoad = true;
            }
        }
    }



    public void assignDistricts(Cell[][] grid) {
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                if (x < z && z <= (-x + (size-1))) {
                    grid[x][z].district = District.SLUMS;
                    this.slumsCells.add(grid[x][z]);
                } else if (x >= z && z < (-x+ (size-1))) {
                    grid[x][z].district = District.PORT;
                    this.portCells.add(grid[x][z]);
                } else if (x <= z && z > (-x+ (size-1))) {
                    grid[x][z].district = District.FARM;
                    this.farmCells.add(grid[x][z]);
                } else if (x > z && z >= (-x+ (size-1))) {
                    grid[x][z].district = District.ARISTOCRACY;
                    this.aristocracyCells.add(grid[x][z]);
                }
            }
        }
        for (int x = 0; x < size; x++) {
            for (int z = size; z < size*1.5; z++) {
                grid[x][z].district = District.OUTSKIRTS;
                this.outskirtsCells.add(grid[x][z]);
            }
        }
    }

    public void generateSideRoads(Cell[][] grid) {

        for (int x = 0; x < size; x++) {
            if (x < size/2) {
                if (x % 9 == 0) {
                    for (int z = 0; z < size; z++) {

                        if (!grid[x][z].isOccupied) {
                            grid[x][z].isRoad = true;
                            grid[x][z].isOccupied = true;
                            this.fillCellWithBlock(Material.COBBLESTONE, x , z );
                        }

                    }
                }
            } else {
                if ((x-1) % 9 == 0) {
                    for (int z = 0; z < size; z++) {

                        if (!grid[x][z].isOccupied) {
                            grid[x][z].isRoad = true;
                            grid[x][z].isOccupied = true;
                            this.fillCellWithBlock(Material.COBBLESTONE, x , z );
                        }

                    }
                }
            }
        }
    }

    public void generateBackRoads(Cell[][] grid) {
        Random r = new Random();
        for (int x = 0; x < size; x++) {
            if (x < size/2) {
                if (x % 9 == 0) {
                    for (int z = 0; z < numBackRoads*2; z++) {
                        int cellZ = (z*12) + r.nextInt(10);
                        setBackRoadBlocks(grid, x, cellZ);
                    }
                }
            } else {
                if ((x-1) % 9 == 0) {
                    for (int z = 0; z < numBackRoads*2; z++) {
                        int cellZ = (z*12) + r.nextInt(10);
                        setBackRoadBlocks(grid, x, cellZ);
                    }
                }
            }
        }
    }

    public void setBackRoadBlocks(Cell[][] grid, int startX, int startZ) {
        Random r = new Random();
        int lastX = startX;
        int lastZ = startZ;
        do {
            List<Direction> potentialDirections = new ArrayList(Arrays.asList(new Direction[] {Direction.N, Direction.S, Direction.E, Direction.W}));
            List<Direction> adjacentRoads = this.nearbyRoads(lastX, lastZ);
            if (!adjacentRoads.isEmpty()) {
                if (adjacentRoads.contains(Direction.E)) {
                    return;
                }
                for (Direction adjacentRoad : adjacentRoads) {
                    if (potentialDirections.contains(adjacentRoad)) {
                        potentialDirections.remove(potentialDirections.indexOf(adjacentRoad));
                    }
                }
                if (potentialDirections.contains(Direction.W) && !potentialDirections.contains(Direction.E) && !potentialDirections.contains(Direction.S) && !potentialDirections.contains(Direction.N)) {
                    return;
                }
                if (!potentialDirections.isEmpty()) {
                    Direction nextDirection = Direction.E;
                    int directionToMove = r.nextInt(100);
                    if (directionToMove < 59) {
                        nextDirection = Direction.E;
                    } else if (directionToMove < 80 && !(lastZ > (startZ+4))) {
                        nextDirection = Direction.N;
                    } else if (directionToMove < 100 && !(lastZ < (startZ-4))) {
                        nextDirection = Direction.S;
                    } else {

                    }
                    if (!potentialDirections.contains(nextDirection)) {
                        if (!potentialDirections.contains(Direction.E)) {
                            if (!potentialDirections.contains(Direction.N)) {
                                nextDirection = Direction.S;
                            }
                            nextDirection = Direction.N;
                        }
                        nextDirection = Direction.E;
                    }

                    if (nextDirection == Direction.E) {
                        lastX++;
                    } else if (nextDirection == Direction.W) {
                        lastX--;
                    } else if (nextDirection == Direction.S) {
                        lastZ--;
                    } else {
                        lastZ++;
                    }
                    grid[lastX][lastZ].isRoad = true;
                    grid[lastX][lastZ].isOccupied = true;
                    this.fillCellWithBlock(Material.GRAVEL, lastX , lastZ);


                }
            }
        }while(true);

    }


    public List<Direction> nearbyRoads(int cellX, int cellZ) {
        List<Direction> list = new ArrayList();
        int[][] neighbors = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        int direction = 0;
        for (int[] neighbor : neighbors) {

            int newRow = cellX + neighbor[0];
            int newCol = cellZ + neighbor[1];
            if (newRow >= 0 && newRow < size && newCol >= 0 && newCol < size) {
                Cell neighborCell = grid[newRow][newCol];

                if (neighborCell.isRoad) {
                    if (direction == 0) {
                        list.add(Direction.W);
                    } else if (direction == 1) {
                        list.add(Direction.E);
                    } else if (direction == 2) {
                        list.add(Direction.S);
                    } else if (direction == 3) {
                        list.add(Direction.N);
                    }

                }
            } else {
                if (direction == 0) {
                    list.add(Direction.W);
                } else if (direction == 1) {
                    list.add(Direction.E);
                } else if (direction == 2) {
                    list.add(Direction.S);
                } else if (direction == 3) {
                    list.add(Direction.N);
                }
            }
            direction++;
        }
        return list;
    }
    public List<Direction> nearbyRoadsOnly(int cellX, int cellZ) {
        List<Direction> list = new ArrayList();
        int[][] neighbors = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        int direction = 0;
        for (int[] neighbor : neighbors) {

            int newRow = cellX + neighbor[0];
            int newCol = cellZ + neighbor[1];
            if (newRow >= 0 && newRow < size && newCol >= 0 && newCol < size) {
                Cell neighborCell = grid[newRow][newCol];

                if (neighborCell.isRoad) {
                    if (direction == 0) {
                        list.add(Direction.W);
                    } else if (direction == 1) {
                        list.add(Direction.E);
                    } else if (direction == 2) {
                        list.add(Direction.S);
                    } else if (direction == 3) {
                        list.add(Direction.N);
                    }

                }
            }
            direction++;
        }
        return list;
    }
    public void pasteLinked(Paste paste, Paste parentPaste, List<Cell> cells) throws IOException {
        ClipboardFormat format = ClipboardFormats.findByFile(new File(schemHandler.schematicDirectory, paste.schematic.fileName + ".schematic"));
        ClipboardReader reader = format.getReader(new FileInputStream(new File(schemHandler.schematicDirectory, paste.schematic.fileName + ".schematic")));
        Clipboard clipboard = reader.read();
        final int finalX = paste.x;
        final int finalZ = paste.z;
        final Direction finalDirection = paste.direction;
        if (paste.direction == Direction.S) {
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(Bukkit.getWorlds().get(0)), -1);
                    Operation operation = (new ClipboardHolder(clipboard)).createPaste(editSession).to(BlockVector3.at(startX + (paste.targetCellX * cellSize), yLvl + 1, startZ + (paste.targetCellZ * -cellSize))).ignoreAirBlocks(false).build();
                    try {
                        Operations.complete(operation);
                    } catch (WorldEditException e) {
                        throw new RuntimeException(e);
                    }
                    editSession.close();
                    if (paste.schematic.district != District.OUTSKIRTS) {
                        List<Cell> newCells = markAsOccupied(finalX, finalZ, paste.targetCellX, paste.targetCellZ, grid, finalDirection, paste.schematic.name);
                        getCellsForDistrict(paste.schematic.district).removeAll(newCells);
                        try {
                            paste(parentPaste, getCellsForDistrict(parentPaste.schematic.district));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        List<Cell> newCells = markAsOccupiedOutskirts(finalX, finalZ, paste.targetCellX, paste.targetCellZ, grid, finalDirection, paste.schematic.name);
                        getCellsForDistrict(paste.schematic.district).removeAll(newCells);
                        try {
                            paste(parentPaste, getCellsForDistrict(parentPaste.schematic.district));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                }
            }, paste.schematic.area);
        } else if (paste.direction == Direction.E) {
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(Bukkit.getWorlds().get(0)), -1);
                    ClipboardHolder holder = new ClipboardHolder(clipboard);
                    holder.setTransform(new AffineTransform().rotateY(90));
                    Operation operation = holder.createPaste(editSession).to(BlockVector3.at(startX + (paste.targetCellX * cellSize) + 4, yLvl + 1, (startZ + (paste.targetCellZ * -cellSize)) + 5 - cellSize)).ignoreAirBlocks(false).build();
                    try {
                        Operations.complete(operation);
                    } catch (WorldEditException e) {
                        throw new RuntimeException(e);
                    }
                    editSession.close();
                    if (paste.schematic.district != District.OUTSKIRTS) {
                        List<Cell> newCells = markAsOccupied(finalX, finalZ, paste.targetCellX, paste.targetCellZ, grid, finalDirection, paste.schematic.name);
                        getCellsForDistrict(paste.schematic.district).removeAll(newCells);
                        try {
                            paste(parentPaste, getCellsForDistrict(parentPaste.schematic.district));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        List<Cell> newCells = markAsOccupiedOutskirts(finalX, finalZ, paste.targetCellX, paste.targetCellZ, grid, finalDirection, paste.schematic.name);
                        getCellsForDistrict(paste.schematic.district).removeAll(newCells);
                        try {
                            paste(parentPaste, getCellsForDistrict(parentPaste.schematic.district));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                }
            }, paste.schematic.area);
        } else if (paste.direction == Direction.N) {
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(Bukkit.getWorlds().get(0)), -1);
                    ClipboardHolder holder = new ClipboardHolder(clipboard);
                    holder.setTransform(new AffineTransform().rotateY(180));
                    Operation operation = holder.createPaste(editSession).to(BlockVector3.at((startX + ((paste.targetCellX + 1) * cellSize) - 1), yLvl + 1, (startZ + (paste.targetCellZ * -cellSize)) + 1 - cellSize)).ignoreAirBlocks(false).build();
                    try {
                        Operations.complete(operation);
                    } catch (WorldEditException e) {
                        throw new RuntimeException(e);
                    }
                    editSession.close();
                    if (paste.schematic.district != District.OUTSKIRTS) {
                        List<Cell> newCells = markAsOccupied(finalX, finalZ, paste.targetCellX, paste.targetCellZ, grid, finalDirection, paste.schematic.name);
                        getCellsForDistrict(paste.schematic.district).removeAll(newCells);
                        try {
                            paste(parentPaste, getCellsForDistrict(parentPaste.schematic.district));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        List<Cell> newCells = markAsOccupiedOutskirts(finalX, finalZ, paste.targetCellX, paste.targetCellZ, grid, finalDirection, paste.schematic.name);
                        getCellsForDistrict(paste.schematic.district).removeAll(newCells);
                        try {
                            paste(parentPaste, getCellsForDistrict(parentPaste.schematic.district));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                }
            }, paste.schematic.area);
        } else if (paste.direction == Direction.W) {
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(Bukkit.getWorlds().get(0)), -1);
                    ClipboardHolder holder = new ClipboardHolder(clipboard);
                    holder.setTransform(new AffineTransform().rotateY(270));
                    Operation operation = holder.createPaste(editSession).to(BlockVector3.at((startX + ((paste.targetCellX + 1) * cellSize) - 5), yLvl + 1, (startZ + (paste.targetCellZ * -cellSize) - 4))).ignoreAirBlocks(false).build();
                    try {
                        Operations.complete(operation);
                    } catch (WorldEditException e) {
                        throw new RuntimeException(e);
                    }
                    editSession.close();
                    if (paste.schematic.district != District.OUTSKIRTS) {
                        List<Cell> newCells = markAsOccupied(finalX, finalZ, paste.targetCellX, paste.targetCellZ, grid, finalDirection, paste.schematic.name);
                        getCellsForDistrict(paste.schematic.district).removeAll(newCells);
                        try {
                            paste(parentPaste, getCellsForDistrict(parentPaste.schematic.district));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        List<Cell> newCells = markAsOccupiedOutskirts(finalX, finalZ, paste.targetCellX, paste.targetCellZ, grid, finalDirection, paste.schematic.name);
                        getCellsForDistrict(paste.schematic.district).removeAll(newCells);
                        try {
                            paste(parentPaste, getCellsForDistrict(parentPaste.schematic.district));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                }
            }, paste.schematic.area);
        }
    }

    public void paste(Paste paste, List<Cell> cells) throws IOException {
        ClipboardFormat format = ClipboardFormats.findByFile(new File(schemHandler.schematicDirectory, paste.schematic.fileName + ".schematic"));
        ClipboardReader reader = format.getReader(new FileInputStream(new File(schemHandler.schematicDirectory, paste.schematic.fileName + ".schematic")));
        Clipboard clipboard = reader.read();
        final int finalX = paste.x;
        final int finalZ = paste.z;
        final Direction finalDirection = paste.direction;
        //Bukkit.getLogger().log(Level.INFO, "Performing paste of " + paste.schematic.name + " coords=" + paste.targetCellX + "," + paste.targetCellZ + " dir=" + paste.direction);
        if (paste.direction == Direction.S) {
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(Bukkit.getWorlds().get(0)), -1);
                    Operation operation = (new ClipboardHolder(clipboard)).createPaste(editSession).to(BlockVector3.at(startX + (paste.targetCellX * cellSize), yLvl + 1, startZ + (paste.targetCellZ * -cellSize))).ignoreAirBlocks(false).build();
                    try {
                        Operations.complete(operation);
                    } catch (WorldEditException e) {
                        throw new RuntimeException(e);
                    }
                    editSession.close();
                    File configFile = new File(Grid.this.schematicDetailsDirectory, paste.schematic.fileName + ".yml");
                    if (configFile.exists()) {
                        generateDetails(paste, configFile);
                    }

                    /*if (paste.schematic.spawners != null) {
                        generateSpawnersInPaste(paste);
                    } else {
                        Bukkit.getLogger().log(Level.INFO, "No spawners detected for schematic " + paste.schematic.name);
                    }*/
                    if (paste.schematic.district != District.OUTSKIRTS) {
                        List<Cell> newCells = markAsOccupied(finalX, finalZ, paste.targetCellX, paste.targetCellZ, grid, finalDirection, paste.schematic.name);
                        getCellsForDistrict(paste.schematic.district).removeAll(newCells);
                        getSchematicsForDistrict(paste.schematic.district).remove(paste.schematic);
                        try {
                            generateStructure(getSchematicsForDistrict(cycleToNextDistrict(paste.schematic.district)), grid, getCellsForDistrict(cycleToNextDistrict(paste.schematic.district)), cycleToNextDistrict(paste.schematic.district));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } catch (WorldEditException e) {
                            throw new RuntimeException(e);
                        } catch (DataException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        List<Cell> newCells = markAsOccupiedOutskirts(finalX, finalZ, paste.targetCellX, paste.targetCellZ, grid, finalDirection, paste.schematic.name);
                        getCellsForDistrict(paste.schematic.district).removeAll(newCells);
                        getSchematicsForDistrict(paste.schematic.district).remove(paste.schematic);
                        try {
                            generateStructure(getSchematicsForDistrict(cycleToNextDistrict(paste.schematic.district)), grid, getCellsForDistrict(cycleToNextDistrict(paste.schematic.district)), cycleToNextDistrict(paste.schematic.district));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } catch (WorldEditException e) {
                            throw new RuntimeException(e);
                        } catch (DataException e) {
                            throw new RuntimeException(e);
                        }
                    }

                }
            }, paste.schematic.area);
            return;
        } else if (paste.direction == Direction.E) {
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(Bukkit.getWorlds().get(0)), -1);
                    ClipboardHolder holder = new ClipboardHolder(clipboard);
                    holder.setTransform(new AffineTransform().rotateY(90));
                    Operation operation = holder.createPaste(editSession).to(BlockVector3.at(startX + (paste.targetCellX * cellSize) + 4, yLvl + 1, (startZ + (paste.targetCellZ * -cellSize)) + 5 - cellSize)).ignoreAirBlocks(false).build();
                    try {
                        Operations.complete(operation);
                    } catch (WorldEditException e) {
                        throw new RuntimeException(e);
                    }
                    editSession.close();
                    File configFile = new File(Grid.this.schematicDetailsDirectory, paste.schematic.fileName + ".yml");
                    if (configFile.exists()) {
                        generateDetails(paste, configFile);
                    }
                    /*if (paste.schematic.spawners != null) {
                        generateSpawnersInPaste(paste);
                    } else {
                        Bukkit.getLogger().log(Level.INFO, "No spawners detected for schematic " + paste.schematic.name);
                    }*/
                    if (paste.schematic.district == District.OUTSKIRTS) {
                        List<Cell> newCells = markAsOccupied(finalX, finalZ, paste.targetCellX, paste.targetCellZ, grid, finalDirection, paste.schematic.name);
                        getCellsForDistrict(paste.schematic.district).removeAll(newCells);
                        getSchematicsForDistrict(paste.schematic.district).remove(paste.schematic);
                        try {
                            generateStructure(getSchematicsForDistrict(cycleToNextDistrict(paste.schematic.district)), grid, getCellsForDistrict(cycleToNextDistrict(paste.schematic.district)), cycleToNextDistrict(paste.schematic.district));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } catch (WorldEditException e) {
                            throw new RuntimeException(e);
                        } catch (DataException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        List<Cell> newCells = markAsOccupiedOutskirts(finalX, finalZ, paste.targetCellX, paste.targetCellZ, grid, finalDirection, paste.schematic.name);
                        getCellsForDistrict(paste.schematic.district).removeAll(newCells);
                        getSchematicsForDistrict(paste.schematic.district).remove(paste.schematic);
                        try {
                            generateStructure(getSchematicsForDistrict(cycleToNextDistrict(paste.schematic.district)), grid, getCellsForDistrict(cycleToNextDistrict(paste.schematic.district)), cycleToNextDistrict(paste.schematic.district));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } catch (WorldEditException e) {
                            throw new RuntimeException(e);
                        } catch (DataException e) {
                            throw new RuntimeException(e);
                        }
                    }


                }
            }, paste.schematic.area);
            return;
        } else if (paste.direction == Direction.N) {
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(Bukkit.getWorlds().get(0)), -1);
                    ClipboardHolder holder = new ClipboardHolder(clipboard);
                    holder.setTransform(new AffineTransform().rotateY(180));
                    Operation operation = holder.createPaste(editSession).to(BlockVector3.at((startX + ((paste.targetCellX + 1) * cellSize) - 1), yLvl + 1, (startZ + (paste.targetCellZ * -cellSize)) + 1 - cellSize)).ignoreAirBlocks(false).build();
                    try {
                        Operations.complete(operation);
                    } catch (WorldEditException e) {
                        throw new RuntimeException(e);
                    }
                    editSession.close();
                    File configFile = new File(Grid.this.schematicDetailsDirectory, paste.schematic.fileName + ".yml");
                    if (configFile.exists()) {
                        generateDetails(paste, configFile);
                    }
                    /*if (paste.schematic.spawners != null) {
                        generateSpawnersInPaste(paste);
                    } else {
                        Bukkit.getLogger().log(Level.INFO, "No spawners detected for schematic " + paste.schematic.name);
                    }*/
                    if (paste.schematic.district != District.OUTSKIRTS) {
                        List<Cell> newCells = markAsOccupied(finalX, finalZ, paste.targetCellX, paste.targetCellZ, grid, finalDirection, paste.schematic.name);
                        getCellsForDistrict(paste.schematic.district).removeAll(newCells);
                        getSchematicsForDistrict(paste.schematic.district).remove(paste.schematic);
                        try {
                            generateStructure(getSchematicsForDistrict(cycleToNextDistrict(paste.schematic.district)), grid, getCellsForDistrict(cycleToNextDistrict(paste.schematic.district)), cycleToNextDistrict(paste.schematic.district));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } catch (WorldEditException e) {
                            throw new RuntimeException(e);
                        } catch (DataException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        List<Cell> newCells = markAsOccupiedOutskirts(finalX, finalZ, paste.targetCellX, paste.targetCellZ, grid, finalDirection, paste.schematic.name);
                        getCellsForDistrict(paste.schematic.district).removeAll(newCells);
                        getSchematicsForDistrict(paste.schematic.district).remove(paste.schematic);
                        try {
                            generateStructure(getSchematicsForDistrict(cycleToNextDistrict(paste.schematic.district)), grid, getCellsForDistrict(cycleToNextDistrict(paste.schematic.district)), cycleToNextDistrict(paste.schematic.district));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } catch (WorldEditException e) {
                            throw new RuntimeException(e);
                        } catch (DataException e) {
                            throw new RuntimeException(e);
                        }
                    }

                }
            }, paste.schematic.area);
            return;
        } else if (paste.direction == Direction.W) {
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(Bukkit.getWorlds().get(0)), -1);
                    ClipboardHolder holder = new ClipboardHolder(clipboard);
                    holder.setTransform(new AffineTransform().rotateY(270));
                    Operation operation = holder.createPaste(editSession).to(BlockVector3.at((startX + ((paste.targetCellX + 1) * cellSize) - 5), yLvl + 1, (startZ + (paste.targetCellZ * -cellSize) - 4))).ignoreAirBlocks(false).build();
                    try {
                        Operations.complete(operation);
                    } catch (WorldEditException e) {
                        throw new RuntimeException(e);
                    }
                    editSession.close();
                    File configFile = new File(Grid.this.schematicDetailsDirectory, paste.schematic.fileName + ".yml");
                    if (configFile.exists()) {
                        generateDetails(paste, configFile);
                    }
                    /*if (paste.schematic.spawners != null) {
                        generateSpawnersInPaste(paste);
                    } else {
                        Bukkit.getLogger().log(Level.INFO, "No spawners detected for schematic " + paste.schematic.name);
                    }*/
                    if (paste.schematic.district != District.OUTSKIRTS) {
                        List<Cell> newCells = markAsOccupied(finalX, finalZ, paste.targetCellX, paste.targetCellZ, grid, finalDirection, paste.schematic.name);
                        getCellsForDistrict(paste.schematic.district).removeAll(newCells);
                        getSchematicsForDistrict(paste.schematic.district).remove(paste.schematic);
                        try {
                            generateStructure(getSchematicsForDistrict(cycleToNextDistrict(paste.schematic.district)), grid, getCellsForDistrict(cycleToNextDistrict(paste.schematic.district)), cycleToNextDistrict(paste.schematic.district));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } catch (WorldEditException e) {
                            throw new RuntimeException(e);
                        } catch (DataException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        List<Cell> newCells = markAsOccupiedOutskirts(finalX, finalZ, paste.targetCellX, paste.targetCellZ, grid, finalDirection, paste.schematic.name);
                        getCellsForDistrict(paste.schematic.district).removeAll(newCells);
                        getSchematicsForDistrict(paste.schematic.district).remove(paste.schematic);
                        try {
                            generateStructure(getSchematicsForDistrict(cycleToNextDistrict(paste.schematic.district)), grid, getCellsForDistrict(cycleToNextDistrict(paste.schematic.district)), cycleToNextDistrict(paste.schematic.district));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } catch (WorldEditException e) {
                            throw new RuntimeException(e);
                        } catch (DataException e) {
                            throw new RuntimeException(e);
                        }
                    }

                }
            }, paste.schematic.area);
        }
    }
    public void generateDetails(Paste paste, File configFile) {
        if (!configFile.exists()) {
            Bukkit.getLogger().severe("NO CONFIG FOUND FOR SCHEMATIC " + paste.schematic.name);
            return;
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        ConfigurationSection master = config.getConfigurationSection(paste.schematic.fileName);
        assert master != null;
        if (master.getConfigurationSection("ores") != null) {
            ConfigurationSection ores = master.getConfigurationSection("ores");
            Iterator iter = ores.getKeys(false).iterator();
            while (iter.hasNext()) {
                String key = (String) iter.next();
                ConfigurationSection section = ores.getConfigurationSection(key);
                Ore ore = new Ore(plugin, plugin.oreHandler.allOres.stream().filter(obj -> obj.name.equalsIgnoreCase(section.getString("template-name"))).findFirst().orElse(null));
                if (ore.getName() == null) {
                    continue;
                }
                Location loc = getPasteLocation(paste, section.getDouble("x"), section.getDouble("y"), section.getDouble("z"));
                Bukkit.getPluginManager().registerEvents(ore, plugin);
                ore.spawn(loc);
            }
        }

        if (master.getConfigurationSection("npcs") != null) {
            ConfigurationSection npcs = master.getConfigurationSection("npcs");
            Iterator iter = npcs.getKeys(false).iterator();
            while (iter.hasNext()) {
                String key = (String) iter.next();
                ConfigurationSection section = npcs.getConfigurationSection(key);
                TGTNpc npc = plugin.npcHandler.allNpcs.stream().filter(obj -> obj.internalName.equalsIgnoreCase(section.getString("npc-name"))).findFirst().orElse(null);
                if (npc == null) {
                    continue;
                }
                Location loc = getPasteLocation(paste, section.getDouble("x"), section.getDouble("y"), section.getDouble("z"));
                npc.npc.spawn(loc);
                npc.pasteLocation = loc;
                plugin.npcHandler.allSpawnedNpcs.add(npc);
            }
        }

        if (master.getConfigurationSection("spawners") != null) {
            ConfigurationSection spawners = master.getConfigurationSection("spawners");
            Iterator iter = spawners.getKeys(false).iterator();
            int counter = 1;
            while (iter.hasNext()) {
                String key = (String) iter.next();
                ConfigurationSection section = spawners.getConfigurationSection(key);
                Spawner spawner = new Spawner(plugin, plugin.spawnerHandler.allSpawners.stream().filter(obj -> obj.name.equalsIgnoreCase(section.getString("spawner-name"))).findFirst().orElse(null));
                if (spawner.getName() == null) {
                    continue;
                }
                Location loc = getPasteLocation(paste, section.getDouble("x"), section.getDouble("y"), section.getDouble("z"));
                spawner.name = spawner.template.name + "." + paste.schematic.name + "." + counter;
                spawner.paste(loc);
                counter++;
            }
        }

        if (master.getConfigurationSection("crops") != null) {
            ConfigurationSection crops = master.getConfigurationSection("crops");
            Iterator iter = crops.getKeys(false).iterator();
            while (iter.hasNext()) {
                String key = (String) iter.next();
                ConfigurationSection section = crops.getConfigurationSection(key);
                Crop crop = new Crop(plugin, plugin.cropHandler.allCrops.stream().filter(obj -> obj.name.equalsIgnoreCase(section.getString("template-name"))).findFirst().orElse(null));
                if (crop.getName() == null) {
                    continue;
                }
                Location loc = getPasteLocation(paste, section.getDouble("x"), section.getDouble("y"), section.getDouble("z"));
                crop.spawn(loc);
            }
        }

        if (master.getConfigurationSection("fishing-zones") != null) {
            ConfigurationSection fishingZones = master.getConfigurationSection("fishing-zones");
            Iterator iter = fishingZones.getKeys(false).iterator();
            while (iter.hasNext()) {
                String key = (String) iter.next();
                ConfigurationSection section = fishingZones.getConfigurationSection(key);
                FishingZone zone = new FishingZone(plugin.fishingZoneHandler.allZones.stream().filter(obj -> obj.name.equalsIgnoreCase(section.getString("template-name"))).findFirst().orElse(null), plugin);
                if (zone.getName() == null) {
                    continue;
                }
                Location minLoc = getPasteLocation(paste, section.getDouble("min-x"), section.getDouble("min-y"), section.getDouble("min-z"));
                Location maxLoc = getPasteLocation(paste, section.getDouble("max-x"), section.getDouble("max-y"), section.getDouble("max-z"));
                zone.name = paste.schematic.name + "." + key + "." + zone.template.name;
                zone.paste(minLoc, maxLoc);
                Location objLocation = new Location(minLoc.getWorld(), minLoc.getX()+(maxLoc.getX()- minLoc.getX())*0.5,minLoc.getY()+(maxLoc.getY()- minLoc.getY())*0.5,minLoc.getZ()+(maxLoc.getZ()- minLoc.getZ())*0.5);
            }
        }

        if (master.getConfigurationSection("stations") != null) {
            ConfigurationSection stations = master.getConfigurationSection("stations");
            Iterator iter = stations.getKeys(false).iterator();
            int counter = 1;
            while (iter.hasNext()) {
                String key = (String) iter.next();
                ConfigurationSection section = stations.getConfigurationSection(key);
                if (Xp.parseXpType(section.getString("type")) == null) {
                    Bukkit.getLogger().log(Level.INFO, "Station for schematic " + paste.schematic.name + " had invalid type!");
                    continue;
                }
                HoldingTable holdingTable = new HoldingTable();
                Direction dir = getPasteDirection(paste, Direction.getDirection(section.getString("holding-table.dir")));
                holdingTable.spawn(getPasteLocation(paste,section.getDouble("holding-table.x"),section.getDouble("holding-table.y"),section.getDouble("holding-table.z")),paste.schematic.name + "." + section.getString("type") + "." + counter, Xp.parseXpType(section.getString("type")),plugin, dir);
                MashingTable mashingTable = new MashingTable();
                dir = getPasteDirection(paste, Direction.getDirection(section.getString("mashing-table.dir")));
                mashingTable.spawn(getPasteLocation(paste,section.getDouble("mashing-table.x"),section.getDouble("mashing-table.y"),section.getDouble("mashing-table.z")),paste.schematic.name + "." + section.getString("type") + "." + counter, Xp.parseXpType(section.getString("type")),plugin, dir);
                TimingTable timingTable = new TimingTable();
                dir = getPasteDirection(paste, Direction.getDirection(section.getString("timing-table.dir")));
                timingTable.spawn(getPasteLocation(paste,section.getDouble("timing-table.x"),section.getDouble("timing-table.y"),section.getDouble("timing-table.z")),paste.schematic.name + "." + section.getString("type") + "." + counter, Xp.parseXpType(section.getString("type")),plugin, dir);
                Station station = new Station(plugin, Xp.parseXpType(section.getString("type")));
                station.holdingTable = holdingTable;
                station.mashingTable = mashingTable;
                station.timingTable = timingTable;
                Location spawnLoc = getPasteLocation(paste, section.getDouble("spawn-x"), section.getDouble("spawn-y"), section.getDouble("spawn-z"));
                spawnLoc.setYaw((float) section.getDouble("spawn-yaw"));
                Location minLoc = getPasteLocation(paste, section.getDouble("min-x"), section.getDouble("min-y"), section.getDouble("min-z"));
                Location maxLoc = getPasteLocation(paste, section.getDouble("max-x"), section.getDouble("max-y"), section.getDouble("max-z"));
                if (maxLoc.getX() < minLoc.getX()) {
                    double minX = maxLoc.getX();
                    double maxX = minLoc.getX();
                    maxLoc.setX(maxX);
                    minLoc.setX(minX);
                }
                if (maxLoc.getY() < minLoc.getY()) {
                    double minY = maxLoc.getY();
                    double maxY = minLoc.getY();
                    maxLoc.setY(maxY);
                    minLoc.setY(minY);
                }
                if (maxLoc.getZ() < minLoc.getZ()) {
                    double minZ = maxLoc.getZ();
                    double maxZ = minLoc.getZ();
                    maxLoc.setZ(maxZ);
                    minLoc.setZ(minZ);
                }
                station.minLoc = minLoc;
                station.maxLoc = maxLoc;
                station.spawnLocation = spawnLoc;
                if (paste.direction == Direction.W) {
                    station.spawnLocation.setYaw(station.spawnLocation.getYaw() + 90);
                } else if (paste.direction == Direction.N) {
                    station.spawnLocation.setYaw(station.spawnLocation.getYaw() + 180);
                } else if (paste.direction == Direction.E) {
                    station.spawnLocation.setYaw(station.spawnLocation.getYaw() + 270);
                }
                station.name = paste.schematic.name + "." + counter + "." + Xp.parseXpType(section.getString("type")).toString();
                plugin.tableHandler.allStations.add(station);
                counter++;
            }
        }
        if (master.getConfigurationSection("foraging-zones") != null) {
            ConfigurationSection foragingZones = master.getConfigurationSection("foraging-zones");
            Iterator iter = foragingZones.getKeys(false).iterator();
            while (iter.hasNext()) {
                String key = (String) iter.next();
                ConfigurationSection section = foragingZones.getConfigurationSection(key);
                ForagingZone zone = new ForagingZone(plugin, plugin.foragingZoneHandler.allZones.stream().filter(obj -> obj.name.equalsIgnoreCase(section.getString("template-name"))).findFirst().orElse(null));
                if (zone.getName() == null) {
                    continue;
                }
                Location minLoc = getPasteLocation(paste, section.getDouble("min-x"), section.getDouble("min-y"), section.getDouble("min-z"));
                Location maxLoc = getPasteLocation(paste, section.getDouble("max-x"), section.getDouble("max-y"), section.getDouble("max-z"));
                zone.name = paste.schematic.name + "." + key + "." + zone.template.name;
                zone.paste(minLoc, maxLoc);
            }
        }
        if (master.getConfigurationSection("dungeons") != null) {
            ConfigurationSection dungeons = master.getConfigurationSection("dungeons");
            Iterator<String> iter = dungeons.getKeys(false).iterator();
            while (iter.hasNext()) {
                String key = iter.next();
                ConfigurationSection section = dungeons.getConfigurationSection(key);
                DungeonTemplate template = plugin.dungeonHandler.allTemplates.stream().filter(obj -> obj.name.equalsIgnoreCase(section.getString("template-name"))).findFirst().orElse(null);
                if (template == null) {
                    continue;
                }
                Dungeon dungeon = new Dungeon(template, plugin);
                Location minLoc = getPasteLocation(paste, section.getDouble("min-x"), section.getDouble("min-y"), section.getDouble("min-z"));
                Location maxLoc = getPasteLocation(paste, section.getDouble("max-x"), section.getDouble("max-y"), section.getDouble("max-z"));
                if (maxLoc.getX() < minLoc.getX()) {
                    double minX = maxLoc.getX();
                    double maxX = minLoc.getX();
                    maxLoc.setX(maxX);
                    minLoc.setX(minX);
                }
                if (maxLoc.getY() < minLoc.getY()) {
                    double minY = maxLoc.getY();
                    double maxY = minLoc.getY();
                    maxLoc.setY(maxY);
                    minLoc.setY(minY);
                }
                if (maxLoc.getZ() < minLoc.getZ()) {
                    double minZ = maxLoc.getZ();
                    double maxZ = minLoc.getZ();
                    maxLoc.setZ(maxZ);
                    minLoc.setZ(minZ);
                }
                dungeon.minLoc = minLoc.clone();
                dungeon.maxLoc = maxLoc.clone();
                if (section.contains("entrance-door-min-loc") && section.contains("entrance-door-max-loc") && section.contains("entrance-door-armorstand-loc")) {
                    String[] minString = section.getString("entrance-door-min-loc").split(",");
                    String[] maxString = section.getString("entrance-door-max-loc").split(",");
                    String[] asString = section.getString("entrance-door-armorstand-loc").split(",");
                    Location asLoc = getPasteLocation(paste,Double.parseDouble(asString[0]), Double.parseDouble(asString[1]), Double.parseDouble(asString[2]) );
                    asLoc.setYaw((float)Double.parseDouble(asString[3]));
                    dungeon.setDoorForDungeon(asLoc.toCenterLocation(), dungeon.getName() + ".door", getPasteLocation(paste, Double.parseDouble(minString[0]), Double.parseDouble(minString[1]), Double.parseDouble(minString[2])).toCenterLocation(), getPasteLocation(paste, Double.parseDouble(maxString[0]), Double.parseDouble(maxString[1]), Double.parseDouble(maxString[2])).toCenterLocation());
                }
                if (section.contains("exit-warp-entrance") && section.contains("exit-warp-target")) {
                    String[] entranceStr = section.getString("exit-warp-entrance").split(",");
                    String[] targetStr = section.getString("exit-warp-target").split(",");
                    Location entranceLoc = getPasteLocation(paste, Double.parseDouble(entranceStr[0]), Double.parseDouble(entranceStr[1]), Double.parseDouble(entranceStr[2])).toCenterLocation();
                    entranceLoc.setYaw((float)Double.parseDouble(entranceStr[3]));
                    Location targetLoc = getPasteLocation(paste, Double.parseDouble(targetStr[0]), Double.parseDouble(targetStr[1]), Double.parseDouble(targetStr[2])).toCenterLocation();
                    targetLoc.setYaw((float)Double.parseDouble(targetStr[3]));
                    dungeon.createExitWarp(entranceLoc);
                    dungeon.createExitTarget(targetLoc);
                }
                ConfigurationSection rooms = section.getConfigurationSection("rooms");
                Iterator<String> roomsIter = rooms.getKeys(false).iterator();
                while (roomsIter.hasNext()) {
                    String roomKey = roomsIter.next();
                    ConfigurationSection room = rooms.getConfigurationSection(roomKey);
                    RoomData data = template.getRoomWithName(room.getString("name"));
                    if (data == null) {
                        continue;
                    }
                    if (data.goal == RoomGoal.SLAYER) {
                        dungeon.addSlayerRoom(data.name, getPasteLocation(paste, room.getDouble("min-x"), room.getDouble("min-y"), room.getDouble("min-z")), getPasteLocation(paste, room.getDouble("max-x"), room.getDouble("max-y"), room.getDouble("max-z")), data.quantity, data.toProgress, data.preventAbilities);
                    } else if (data.goal == RoomGoal.COLLECTION) {
                        dungeon.addCollectionRoom(data.name, getPasteLocation(paste, room.getDouble("min-x"), room.getDouble("min-y"), room.getDouble("min-z")), getPasteLocation(paste, room.getDouble("max-x"), room.getDouble("max-y"), room.getDouble("max-z")), data.quantity, data.toProgress, data.preventAbilities);
                    } else {
                        dungeon.addThroughRoom(data.name, getPasteLocation(paste, room.getDouble("min-x"), room.getDouble("min-y"), room.getDouble("min-z")), getPasteLocation(paste, room.getDouble("max-x"), room.getDouble("max-y"), room.getDouble("max-z")), data.doorClosedByDefault, data.preventAbilities);
                    }
                    if (room.contains("door-min-loc") && room.contains("door-max-loc") && room.contains("door-armorstand-loc")) {
                        String[] minString = room.getString("door-min-loc").split(",");
                        String[] maxString = room.getString("door-max-loc").split(",");
                        String[] asString = room.getString("door-armorstand-loc").split(",");
                        Location doorMinLoc = getPasteLocation(paste, Double.parseDouble(minString[0]),Double.parseDouble(minString[1]),Double.parseDouble(minString[2]));
                        Location doorMaxLoc = getPasteLocation(paste, Double.parseDouble(maxString[0]),Double.parseDouble(maxString[1]),Double.parseDouble(maxString[2]));
                        Location asLoc = getPasteLocation(paste, Double.parseDouble(asString[0]), Double.parseDouble(asString[1]), Double.parseDouble(asString[2]));
                        asLoc.setYaw((float)Double.parseDouble(asString[3]));
                        dungeon.setDoorForRoom(asLoc,dungeon.getName()+ "." + data.name + ".door",doorMinLoc,doorMaxLoc );
                    }
                    if (room.contains("reward-chest-loc") && room.contains("reward-chest-dir")) {
                        String[] locString = room.getString("reward-chest-loc").split(",");
                        Location chestLoc = getPasteLocation(paste, Double.parseDouble(locString[0]), Double.parseDouble(locString[1]), Double.parseDouble(locString[2]));
                        Direction dir = getPasteDirection(paste, Direction.getDirection(room.getString("reward-chest-dir")));
                        RewardChest chest = new RewardChest(plugin, data.rewardChestBase, data.rewardChestMid, data.rewardChestBase64, data.rewardChestDrops, data.chestParticle, data.chestParticleCount);
                        dungeon.getRoomWithName(data.name).rewardChest = chest;
                        chest.paste(chestLoc, dir);
                    }

                }

            }
        }

    }
    public Location getPasteLocation(Paste paste, double x, double y, double z) {
        if (paste.direction == Direction.S) {
            Location loc = new Location(Bukkit.getWorlds().get(0), startX + (paste.targetCellX * cellSize), yLvl + 1, startZ + (paste.targetCellZ * -cellSize));
            loc.add(x, y, z);
            return loc;
        } else if (paste.direction == Direction.N) {
            Location loc = new Location(Bukkit.getWorlds().get(0), (startX + ((paste.targetCellX + 1) * cellSize) ), yLvl + 1, (startZ + (paste.targetCellZ * -cellSize)) - cellSize + 2);
            loc.add(-x, y, -z);
            return loc;
        } else if (paste.direction == Direction.E) {
            Location loc = new Location(Bukkit.getWorlds().get(0), startX + (paste.targetCellX * cellSize) + 4, yLvl + 1, (startZ + (paste.targetCellZ * -cellSize)) + 6 - cellSize);
            loc.add(z, y, -x);
            return loc;
        } else if (paste.direction == Direction.W) {
            Location loc = new Location(Bukkit.getWorlds().get(0), (startX + ((paste.targetCellX + 1) * cellSize) - 4), yLvl + 1, (startZ + (paste.targetCellZ * -cellSize) - 4));
            loc.add(-z, y, x);
            return loc;
        }
        return null;
    }
    public Direction getPasteDirection(Paste paste, Direction dir) {
        if (paste.direction == Direction.S) {
            return dir;
        } else if (paste.direction == Direction.N) {
            if (dir == Direction.N) {
                return Direction.S;
            } else if (dir == Direction.S) {
                return Direction.N;
            } else if (dir == Direction.E) {
                return Direction.W;
            } else if (dir==Direction.W) {
                return Direction.E;
            }
        } else if (paste.direction == Direction.W) {
            if (dir == Direction.E) {
                return Direction.S;
            } else if (dir == Direction.S) {
                return Direction.W;
            } else if (dir == Direction.W) {
                return Direction.N;
            } else if (dir==Direction.N) {
                return Direction.E;
            }
        } else if (paste.direction == Direction.E) {
            if (dir == Direction.E) {
                return Direction.N;
            } else if (dir == Direction.N) {
                return Direction.W;
            } else if (dir == Direction.W) {
                return Direction.S;
            } else if (dir == Direction.S) {
                return Direction.E;
            }
        }
        return null;
    }

    /*public void generateSpawnersInPaste(Paste paste) {

        for (Spawner spawner : paste.schematic.spawners) {

            if (spawner != null) {

                if (paste.direction == Direction.S) {
                    Location loc = new Location(Bukkit.getWorlds().get(0), startX + (paste.targetCellX * cellSize), yLvl + 1, startZ + (paste.targetCellZ * -cellSize));
                    loc.add(spawner.x, spawner.y, spawner.z);
                    spawner.loc = loc;
                    MythicBukkit.inst().getSpawnerManager().createSpawner(spawner.spawnerName, loc, spawner.mobName);
                    MythicSpawner mythicSpawner = MythicBukkit.inst().getSpawnerManager().getSpawnerByName(spawner.spawnerName);
                    mythicSpawner.setSpawnRadius((int) spawner.radius);
                    mythicSpawner.setSpawnRadiusY((int) spawner.radiusY);
                    mythicSpawner.setUseTimer(spawner.useTimer);
                    mythicSpawner.setMaxMobs(PlaceholderInt.of(String.valueOf(spawner.maxMobs)));
                    mythicSpawner.setMobsPerSpawn(spawner.mobsPerSpawn);
                    mythicSpawner.setCooldownSeconds((int) spawner.cooldown);
                    mythicSpawner.setWarmupSeconds((int) spawner.warmup);
                    mythicSpawner.setCheckForPlayers(spawner.checkForPlayers);
                    mythicSpawner.setLeashRange((int) spawner.leashRange);
                    mythicSpawner.Enable();
                    mythicSpawner.ActivateSpawner();
                    Bukkit.getLogger().log(Level.INFO, "Attempted to create spawner for: " + paste.schematic + " at " + spawner.loc.getX() + "," + spawner.loc.getY() + "," + spawner.loc.getZ());

                } else if (paste.direction == Direction.N) {
                    Location loc = new Location(Bukkit.getWorlds().get(0), (startX + ((paste.targetCellX + 1) * cellSize) - 1), yLvl + 1, (startZ + (paste.targetCellZ * -cellSize)) + 1 - cellSize);
                    loc.add(-spawner.x, spawner.y, -spawner.z);
                    spawner.loc = loc;
                    MythicBukkit.inst().getSpawnerManager().createSpawner(spawner.spawnerName, loc, spawner.mobName);
                    MythicSpawner mythicSpawner = MythicBukkit.inst().getSpawnerManager().getSpawnerByName(spawner.spawnerName);
                    mythicSpawner.setSpawnRadius((int) spawner.radius);
                    mythicSpawner.setSpawnRadiusY((int) spawner.radiusY);
                    mythicSpawner.setUseTimer(spawner.useTimer);
                    mythicSpawner.setMaxMobs(PlaceholderInt.of(String.valueOf(spawner.maxMobs)));
                    mythicSpawner.setMobsPerSpawn(spawner.mobsPerSpawn);
                    mythicSpawner.setCooldownSeconds((int) spawner.cooldown);
                    mythicSpawner.setWarmupSeconds((int) spawner.warmup);
                    mythicSpawner.setCheckForPlayers(spawner.checkForPlayers);
                    mythicSpawner.setLeashRange((int) spawner.leashRange);
                    mythicSpawner.Enable();
                    mythicSpawner.ActivateSpawner();
                    Bukkit.getLogger().log(Level.INFO, "Attempted to create spawner for: " + paste.schematic + " at " + spawner.loc.getX() + "," + spawner.loc.getY() + "," + spawner.loc.getZ());
                } else if (paste.direction == Direction.E) {
                    Location loc = new Location(Bukkit.getWorlds().get(0), startX + (paste.targetCellX * cellSize) + 4, yLvl + 1, (startZ + (paste.targetCellZ * -cellSize)) + 5 - cellSize);
                    loc.add(-spawner.x, spawner.y, spawner.z);
                    spawner.loc = loc;
                    MythicBukkit.inst().getSpawnerManager().createSpawner(spawner.spawnerName, loc, spawner.mobName);
                    MythicSpawner mythicSpawner = MythicBukkit.inst().getSpawnerManager().getSpawnerByName(spawner.spawnerName);
                    mythicSpawner.setSpawnRadius((int) spawner.radius);
                    mythicSpawner.setSpawnRadiusY((int) spawner.radiusY);
                    mythicSpawner.setUseTimer(spawner.useTimer);
                    mythicSpawner.setMaxMobs(PlaceholderInt.of(String.valueOf(spawner.maxMobs)));
                    mythicSpawner.setMobsPerSpawn(spawner.mobsPerSpawn);
                    mythicSpawner.setCooldownSeconds((int) spawner.cooldown);
                    mythicSpawner.setWarmupSeconds((int) spawner.warmup);
                    mythicSpawner.setCheckForPlayers(spawner.checkForPlayers);
                    mythicSpawner.setLeashRange((int) spawner.leashRange);
                    mythicSpawner.Enable();
                    mythicSpawner.ActivateSpawner();
                    Bukkit.getLogger().log(Level.INFO, "Attempted to create spawner for: " + paste.schematic + " at " + spawner.loc.getX() + "," + spawner.loc.getY() + "," + spawner.loc.getZ());
                } else if (paste.direction == Direction.W) {
                    Location loc = new Location(Bukkit.getWorlds().get(0), (startX + ((paste.targetCellX + 1) * cellSize) - 5), yLvl + 1, (startZ + (paste.targetCellZ * -cellSize) - 4));
                    loc.add(spawner.x, spawner.y, -spawner.z);
                    spawner.loc = loc;
                    MythicBukkit.inst().getSpawnerManager().createSpawner(spawner.spawnerName, loc, spawner.mobName);
                    MythicSpawner mythicSpawner = MythicBukkit.inst().getSpawnerManager().getSpawnerByName(spawner.spawnerName);
                    mythicSpawner.setSpawnRadius(spawner.radius);
                    mythicSpawner.setSpawnRadiusY(spawner.radiusY);
                    mythicSpawner.setUseTimer(spawner.useTimer);
                    mythicSpawner.setMaxMobs(PlaceholderInt.of(String.valueOf(spawner.maxMobs)));
                    mythicSpawner.setMobsPerSpawn(spawner.mobsPerSpawn);
                    mythicSpawner.setCooldownSeconds(spawner.cooldown);
                    mythicSpawner.setWarmupSeconds(spawner.warmup);
                    mythicSpawner.setCheckForPlayers(spawner.checkForPlayers);
                    mythicSpawner.setLeashRange(spawner.leashRange);
                    mythicSpawner.Enable();
                    mythicSpawner.ActivateSpawner();
                    Bukkit.getLogger().log(Level.INFO, "Attempted to create spawner for: " + paste.schematic + " at " + spawner.loc.getX() + "," + spawner.loc.getY() + "," + spawner.loc.getZ());
                }

            } else {
                Bukkit.getLogger().severe("SPAWNER FOR " + paste.schematic + " WAS NULL!");
            }
        }
    }*/
    public void pasteRepeatable(Paste paste, List<Cell> cells, boolean isOmni) throws IOException {
        ClipboardFormat format = ClipboardFormats.findByFile(new File(schemHandler.schematicDirectory, paste.schematic.fileName + ".schematic"));
        ClipboardReader reader = format.getReader(new FileInputStream(new File(schemHandler.schematicDirectory, paste.schematic.fileName + ".schematic")));
        Clipboard clipboard = reader.read();
        final int finalX = paste.x;
        final int finalZ = paste.z;
        final Direction finalDirection = paste.direction;
        final District finalDistrict = paste.schematic.district;
        //Bukkit.getLogger().log(Level.INFO, "Performing repeatable paste of " + paste.schematic.name + " coords=" + paste.targetCellX + "," + paste.targetCellZ + " dir=" + finalDirection);

        if (true/*finalDirection == Direction.S*/) {
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    List<Cell> newCells = new ArrayList<>();
                    if (finalDistrict != District.OUTSKIRTS) {
                        newCells.addAll(markAsOccupied(finalX, finalZ, paste.targetCellX, paste.targetCellZ, grid, finalDirection, paste.schematic.name));
                    } else {
                        newCells.addAll(markAsOccupiedOutskirts(finalX, finalZ, paste.targetCellX, paste.targetCellZ, grid, finalDirection, paste.schematic.name));
                    }
                    for (Cell cell : newCells) {
                        cell.pasteDetails = new PasteDetails(paste.schematic.name, finalDirection, System.currentTimeMillis(), newCells, isOmni);
                    }
                    getCellsForDistrict(finalDistrict).removeAll(newCells);
                    cells.removeAll(newCells);
                    EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(Bukkit.getWorlds().get(0)), -1);
                    Operation operation = null;
                    if (finalDirection == Direction.S) {
                        operation = (new ClipboardHolder(clipboard)).createPaste(editSession).to(BlockVector3.at(startX + (paste.targetCellX * cellSize), yLvl + 1, startZ + (paste.targetCellZ * -cellSize))).ignoreAirBlocks(false).build();
                    } else if (finalDirection == Direction.E) {
                        ClipboardHolder holder = new ClipboardHolder(clipboard);
                        holder.setTransform(new AffineTransform().rotateY(90));
                        operation = holder.createPaste(editSession).to(BlockVector3.at(startX + (paste.targetCellX * cellSize) + 4, yLvl + 1, (startZ + (paste.targetCellZ * -cellSize)) + 5 - cellSize)).ignoreAirBlocks(false).build();
                    } else if (finalDirection == Direction.N) {
                        ClipboardHolder holder = new ClipboardHolder(clipboard);
                        holder.setTransform(new AffineTransform().rotateY(180));
                        operation = holder.createPaste(editSession).to(BlockVector3.at((startX + ((paste.targetCellX + 1) * cellSize) - 1), yLvl + 1, (startZ + (paste.targetCellZ * -cellSize)) + 1 - cellSize)).ignoreAirBlocks(false).build();
                    } else if (finalDirection == Direction.W) {
                        ClipboardHolder holder = new ClipboardHolder(clipboard);
                        holder.setTransform(new AffineTransform().rotateY(270));
                        operation = holder.createPaste(editSession).to(BlockVector3.at((startX + ((paste.targetCellX + 1) * cellSize) - 5), yLvl + 1, (startZ + (paste.targetCellZ * -cellSize) - 4))).ignoreAirBlocks(false).build();

                    }
                    try {
                        if (operation != null) {
                            Operations.complete(operation);
                        }
                    } catch (WorldEditException e) {
                        throw new RuntimeException(e);
                    }
                    editSession.close();

                    //getSchematicsForDistrict(finalDistrict).remove(paste.schematic);
                    //runningDelay = runningDelay + paste.schematic.area;
                    try {
                        //Bukkit.getLogger().log(Level.INFO, "Attempting new paste...");
                        generateRepeatables(grid, cells);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }


                }
            }, paste.schematic.area);
            return;
        }

        /*if (finalDirection == Direction.S) {
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(Bukkit.getWorlds().get(0)), -1);
                    Operation operation = (new ClipboardHolder(clipboard)).createPaste(editSession).to(BlockVector3.at(startX + (paste.targetCellX * cellSize), yLvl + 1, startZ + (paste.targetCellZ * -cellSize))).ignoreAirBlocks(false).build();
                    try {
                        Operations.complete(operation);
                    } catch (WorldEditException e) {
                        throw new RuntimeException(e);
                    }
                    editSession.close();
                    if (paste.schematic.district != District.OUTSKIRTS) {
                        List<Cell> newCells = markAsOccupied(finalX, finalZ, paste.targetCellX, paste.targetCellZ, grid, finalDirection, paste.schematic.name);
                        for (Cell cell : newCells) {
                            cell.pasteDetails = new PasteDetails(paste.schematic.name, finalDirection, System.currentTimeMillis(), newCells, isOmni);
                        }
                        getCellsForDistrict(paste.schematic.district).removeAll(newCells);
                        cells.removeAll(newCells);
                        getSchematicsForDistrict(paste.schematic.district).remove(paste.schematic);
                        runningDelay = runningDelay + paste.schematic.area;
                        try {
                            Bukkit.getLogger().log(Level.INFO, "Attempting new paste...");
                            generateRepeatables(grid, cells);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        List<Cell> newCells = markAsOccupiedOutskirts(finalX, finalZ, paste.targetCellX, paste.targetCellZ, grid, finalDirection, paste.schematic.name);
                        for (Cell cell : newCells) {
                            cell.pasteDetails = new PasteDetails(paste.schematic.name, finalDirection, System.currentTimeMillis(), newCells, isOmni);
                        }
                        getCellsForDistrict(paste.schematic.district).removeAll(newCells);
                        cells.removeAll(newCells);
                        getSchematicsForDistrict(paste.schematic.district).remove(paste.schematic);
                        runningDelay = runningDelay + paste.schematic.area;
                        try {
                            Bukkit.getLogger().log(Level.INFO, "Attempting new paste...");
                            generateRepeatables(grid, cells);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                }
            }, paste.schematic.area);
            return;
        } else if (finalDirection == Direction.E) {
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(Bukkit.getWorlds().get(0)), -1);
                    ClipboardHolder holder = new ClipboardHolder(clipboard);
                    holder.setTransform(new AffineTransform().rotateY(90));
                    Operation operation = holder.createPaste(editSession).to(BlockVector3.at(startX + (paste.targetCellX * cellSize) + 4, yLvl + 1, (startZ + (paste.targetCellZ * -cellSize)) + 5 - cellSize)).ignoreAirBlocks(false).build();
                    try {
                        Operations.complete(operation);
                    } catch (WorldEditException e) {
                        throw new RuntimeException(e);
                    }
                    editSession.close();
                    if (paste.schematic.district != District.OUTSKIRTS) {
                        List<Cell> newCells = markAsOccupied(finalX, finalZ, paste.targetCellX, paste.targetCellZ, grid, finalDirection, paste.schematic.name);
                        for (Cell cell : newCells) {
                            cell.pasteDetails = new PasteDetails(paste.schematic.name, finalDirection, System.currentTimeMillis(), newCells, isOmni);
                        }
                        getCellsForDistrict(paste.schematic.district).removeAll(newCells);
                        cells.removeAll(newCells);
                        getSchematicsForDistrict(paste.schematic.district).remove(paste.schematic);

                        runningDelay = runningDelay + paste.schematic.area;
                        try {
                            Bukkit.getLogger().log(Level.INFO, "Attempting new paste...");
                            generateRepeatables(grid, cells);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        List<Cell> newCells = markAsOccupiedOutskirts(finalX, finalZ, paste.targetCellX, paste.targetCellZ, grid, finalDirection, paste.schematic.name);
                        for (Cell cell : newCells) {
                            cell.pasteDetails = new PasteDetails(paste.schematic.name, finalDirection, System.currentTimeMillis(), newCells, isOmni);
                        }
                        getCellsForDistrict(paste.schematic.district).removeAll(newCells);
                        cells.removeAll(newCells);
                        getSchematicsForDistrict(paste.schematic.district).remove(paste.schematic);

                        runningDelay = runningDelay + paste.schematic.area;
                        try {
                            Bukkit.getLogger().log(Level.INFO, "Attempting new paste...");
                            generateRepeatables(grid, cells);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                }
            }, paste.schematic.area*//*paste.schematic.area + runningDelay*//*);
            return;
        } else if (paste.direction == Direction.N) {
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(Bukkit.getWorlds().get(0)), -1);
                    ClipboardHolder holder = new ClipboardHolder(clipboard);
                    holder.setTransform(new AffineTransform().rotateY(180));
                    Operation operation = holder.createPaste(editSession).to(BlockVector3.at((startX + ((paste.targetCellX + 1) * cellSize) - 1), yLvl + 1, (startZ + (paste.targetCellZ * -cellSize)) + 1 - cellSize)).ignoreAirBlocks(false).build();
                    try {
                        Operations.complete(operation);
                    } catch (WorldEditException e) {
                        throw new RuntimeException(e);
                    }
                    editSession.close();
                    if (paste.schematic.district != District.OUTSKIRTS) {
                        List<Cell> newCells = markAsOccupied(finalX, finalZ, paste.targetCellX, paste.targetCellZ, grid, finalDirection, paste.schematic.name);
                        for (Cell cell : newCells) {
                            cell.pasteDetails = new PasteDetails(paste.schematic.name, finalDirection, System.currentTimeMillis(), newCells, isOmni);
                        }
                        getCellsForDistrict(paste.schematic.district).removeAll(newCells);
                        cells.removeAll(newCells);
                        getSchematicsForDistrict(paste.schematic.district).remove(paste.schematic);
                        runningDelay = runningDelay + paste.schematic.area;
                        try {
                            Bukkit.getLogger().log(Level.INFO, "Attempting new paste...");
                            generateRepeatables(grid, cells);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        List<Cell> newCells = markAsOccupiedOutskirts(finalX, finalZ, paste.targetCellX, paste.targetCellZ, grid, finalDirection, paste.schematic.name);
                        for (Cell cell : newCells) {
                            cell.pasteDetails = new PasteDetails(paste.schematic.name, finalDirection, System.currentTimeMillis(), newCells, isOmni);
                        }
                        getCellsForDistrict(paste.schematic.district).removeAll(newCells);
                        cells.removeAll(newCells);
                        getSchematicsForDistrict(paste.schematic.district).remove(paste.schematic);
                        runningDelay = runningDelay + paste.schematic.area;
                        try {
                            Bukkit.getLogger().log(Level.INFO, "Attempting new paste...");
                            generateRepeatables(grid, cells);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                }
            }, paste.schematic.area*//*paste.schematic.area + runningDelay*//*);
            return;
        } else if (paste.direction == Direction.W) {
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(Bukkit.getWorlds().get(0)), -1);
                    ClipboardHolder holder = new ClipboardHolder(clipboard);
                    holder.setTransform(new AffineTransform().rotateY(270));
                    Operation operation = holder.createPaste(editSession).to(BlockVector3.at((startX + ((paste.targetCellX + 1) * cellSize) - 5), yLvl + 1, (startZ + (paste.targetCellZ * -cellSize) - 4))).ignoreAirBlocks(false).build();
                    try {
                        Operations.complete(operation);
                    } catch (WorldEditException e) {
                        throw new RuntimeException(e);
                    }
                    editSession.close();
                    if (paste.schematic.district != District.OUTSKIRTS) {
                        List<Cell> newCells = markAsOccupied(finalX, finalZ, paste.targetCellX, paste.targetCellZ, grid, finalDirection, paste.schematic.name);
                        for (Cell cell : newCells) {
                            cell.pasteDetails = new PasteDetails(paste.schematic.name, finalDirection, System.currentTimeMillis(), newCells, isOmni);
                        }
                        getCellsForDistrict(paste.schematic.district).removeAll(newCells);
                        cells.removeAll(newCells);
                        getSchematicsForDistrict(paste.schematic.district).remove(paste.schematic);
                        runningDelay = runningDelay + paste.schematic.area;
                        try {
                            Bukkit.getLogger().log(Level.INFO, "Attempting new paste...");
                            generateRepeatables(grid, cells);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        List<Cell> newCells = markAsOccupiedOutskirts(finalX, finalZ, paste.targetCellX, paste.targetCellZ, grid, finalDirection, paste.schematic.name);
                        for (Cell cell : newCells) {
                            cell.pasteDetails = new PasteDetails(paste.schematic.name, finalDirection, System.currentTimeMillis(), newCells, isOmni);
                        }
                        getCellsForDistrict(paste.schematic.district).removeAll(newCells);
                        cells.removeAll(newCells);
                        getSchematicsForDistrict(paste.schematic.district).remove(paste.schematic);
                        runningDelay = runningDelay + paste.schematic.area;
                        try {
                            Bukkit.getLogger().log(Level.INFO, "Attempting new paste...");
                            generateRepeatables(grid, cells);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                }
            }, paste.schematic.area*//*paste.schematic.area + runningDelay*//*);
        }*/
    }

    public District cycleToNextDistrict(District district) {
        if (district == District.FARM) {
            return District.ARISTOCRACY;
        } else if (district == District.PORT) {
            return District.SLUMS;
        } else if (district == District.ARISTOCRACY) {
            return District.PORT;
        } else if (district == District.SLUMS) {
            return  District.OUTSKIRTS;
        } else if (district == District.OUTSKIRTS) {
            return District.FARM;
        } else {
            return District.FARM;
        }
    }
    public void generateStructure(List<Schematic> schematicList, Cell[][] grid, List<Cell> cells, District lastDistrict) throws IOException, WorldEditException, DataException {
        if (schematicList.size() < 1 || schematicList.isEmpty()) {
            if (plugin.schematicHandler.slumsSchematics.size() < 1 && plugin.schematicHandler.portSchematics.size() < 1 && plugin.schematicHandler.farmSchematics.size() < 1 && plugin.schematicHandler.aristocracySchematics.size() < 1) {
                generateRepeatablesInitial(grid);
                return;
            }
            generateStructure(getSchematicsForDistrict(cycleToNextDistrict(lastDistrict)), grid, getCellsForDistrict(cycleToNextDistrict(lastDistrict)), cycleToNextDistrict(lastDistrict));
            return;
        }
        Schematic schematic = schematicList.get(0);
        Collections.shuffle(cells);
        Iterator cellsIter = cells.iterator();
        do {
            Cell cell = (Cell) cellsIter.next();
            List<Direction> nearbyRoads = nearbyRoadsOnly(cell.x, cell.z);
            Direction direction = null;
            if (nearbyRoads.size() > 1) {
                direction = nearbyRoads.get(r.nextInt(nearbyRoads.size()));
            } else if (nearbyRoads.size() == 1) {
                direction = nearbyRoads.get(0);
            }
            if (direction == null) {
                continue;
            }
            int targetZ = cell.z;
            int targetX = cell.x;
            int x = schematic.xLength;
            int z = schematic.zHeight;
            if (direction == Direction.W || direction == Direction.E) {
                x = schematic.zHeight;
                z = schematic.xLength;
            }
            if (cell.district != District.OUTSKIRTS) {
                if (this.schemFitsInBounds(x, z, targetX, targetZ, grid, direction)) {
                    Paste paste = new Paste(schematic, x, z, direction, targetX, targetZ);
                    if (schematic.linkedSchematic == null) {
                        paste(paste, getCellsForDistrict(paste.schematic.district));
                    } else {
                        Schematic linkedSchem = schematic.linkedSchematic;
                        List<Cell> linkedCells = getCellsForDistrict(linkedSchem.district);
                        Collections.shuffle(linkedCells);
                        Iterator linkedIter = linkedCells.iterator();

                        do {
                            Cell linkedCell = (Cell) linkedIter.next();
                            List<Direction> linkedNearbyRoads = nearbyRoadsOnly(linkedCell.x, linkedCell.z);
                            Direction linkedDirection = null;
                            if (linkedNearbyRoads.size() > 1) {
                                linkedDirection = linkedNearbyRoads.get(r.nextInt(linkedNearbyRoads.size()));
                            } else if (linkedNearbyRoads.size() == 1) {
                                linkedDirection = linkedNearbyRoads.get(0);
                            }
                            if (linkedDirection == null) {
                                continue;
                            }
                            int linkedTargetZ = linkedCell.z;
                            int linkedTargetX = linkedCell.x;
                            int linkedX = linkedSchem.xLength;
                            int linkedZ = linkedSchem.zHeight;
                            if (linkedDirection == Direction.W || linkedDirection == Direction.E) {
                                linkedX = linkedSchem.zHeight;
                                linkedZ = linkedSchem.xLength;
                            }
                            if (this.schemFitsInBounds(linkedX, linkedZ, linkedTargetX, linkedTargetZ, grid, linkedDirection) && !containsToBeOccupiedCells(toBeOccupiedCells(paste.x, paste.z, paste.targetCellX, paste.targetCellZ, grid, paste.direction), toBeOccupiedCells(linkedX, linkedZ, linkedTargetX, linkedTargetZ, grid, linkedDirection))) {
                                Paste pasteLinked = new Paste(linkedSchem, linkedX, linkedZ, linkedDirection, linkedTargetX, linkedTargetZ);
                                pasteLinked(pasteLinked, paste, getCellsForDistrict(pasteLinked.schematic.district));
                                return;
                            }
                        } while (linkedIter.hasNext());

                        schematicList.remove(schematic);
                        generateStructure(getSchematicsForDistrict(cycleToNextDistrict(lastDistrict)), grid, getCellsForDistrict(cycleToNextDistrict(lastDistrict)), cycleToNextDistrict(lastDistrict));
                    }
                    return;
                }
            } else {
                if (this.schemFitsInBoundsOutskirts(x, z, targetX, targetZ, grid, direction)) {
                    Paste paste = new Paste(schematic, x, z, direction, targetX, targetZ);
                    if (schematic.linkedSchematic == null) {
                        paste(paste, getCellsForDistrict(paste.schematic.district));
                    } else {
                        Schematic linkedSchem = schematic.linkedSchematic;
                        List<Cell> linkedCells = getCellsForDistrict(linkedSchem.district);
                        Collections.shuffle(linkedCells);
                        Iterator linkedIter = linkedCells.iterator();

                        do {
                            Cell linkedCell = (Cell) linkedIter.next();
                            List<Direction> linkedNearbyRoads = nearbyRoadsOnly(linkedCell.x, linkedCell.z);
                            Direction linkedDirection = null;
                            if (linkedNearbyRoads.size() > 1) {
                                linkedDirection = linkedNearbyRoads.get(r.nextInt(linkedNearbyRoads.size()));
                            } else if (linkedNearbyRoads.size() == 1) {
                                linkedDirection = linkedNearbyRoads.get(0);
                            }
                            if (linkedDirection == null) {
                                continue;
                            }
                            int linkedTargetZ = linkedCell.z;
                            int linkedTargetX = linkedCell.x;
                            int linkedX = linkedSchem.xLength;
                            int linkedZ = linkedSchem.zHeight;
                            if (linkedDirection == Direction.W || linkedDirection == Direction.E) {
                                linkedX = linkedSchem.zHeight;
                                linkedZ = linkedSchem.xLength;
                            }
                            if (this.schemFitsInBoundsOutskirts(linkedX, linkedZ, linkedTargetX, linkedTargetZ, grid, linkedDirection) && !containsToBeOccupiedCells(toBeOccupiedCellsOutskirts(paste.x, paste.z, paste.targetCellX, paste.targetCellZ, grid, paste.direction), toBeOccupiedCellsOutskirts(linkedX, linkedZ, linkedTargetX, linkedTargetZ, grid, linkedDirection))) {
                                Paste pasteLinked = new Paste(linkedSchem, linkedX, linkedZ, linkedDirection, linkedTargetX, linkedTargetZ);
                                pasteLinked(pasteLinked, paste, getCellsForDistrict(pasteLinked.schematic.district));
                                return;
                            }
                        } while (linkedIter.hasNext());

                        schematicList.remove(schematic);
                        generateStructure(getSchematicsForDistrict(cycleToNextDistrict(lastDistrict)), grid, getCellsForDistrict(cycleToNextDistrict(lastDistrict)), cycleToNextDistrict(lastDistrict));
                    }
                    return;
                }
            }

        } while (cellsIter.hasNext());
        schematicList.remove(schematic);
        generateStructure(getSchematicsForDistrict(cycleToNextDistrict(lastDistrict)), grid, getCellsForDistrict(cycleToNextDistrict(lastDistrict)), cycleToNextDistrict(lastDistrict));
        /*Collections.shuffle(cells);
        Iterator cellsIter = cells.iterator();
        do {
            Cell cell = (Cell) cellsIter.next();
            List<Direction> nearbyRoads = nearbyRoadsOnly(cell.x, cell.z);
            Direction direction = null;
            if (nearbyRoads.size() > 1) {
                direction = nearbyRoads.get(r.nextInt(nearbyRoads.size()));
            } else if (nearbyRoads.size() == 1) {
                direction = nearbyRoads.get(0);
            }
            if (schematicList.size() < 1 || schematicList.isEmpty()) {
                if (count < 5) {
                    generateBuildingsHighway(grid, count + 1);
                } else {
                    return;
                }
            }
            if (direction == null) {
                continue;
            }
            int targetZ = cell.z;
            int targetX = cell.x;
            int x = schematic.xLength;
            int z = schematic.zHeight;
            if (direction == Direction.W || direction == Direction.E) {
                x = schematic.zHeight;
                z = schematic.xLength;
            }
            if (this.schemFitsInBounds(x, z, targetX, targetZ, grid, direction)) {
                if (schematic.linkedSchematic == null) {

                    return;
                }
            } else {
                Schematic linkedSchematic = schematic.linkedSchematic;
                List<Cell> linkedCells = getCellsForDistrict(linkedSchematic.district);
                List<Cell> cellsToBeOccupied = toBeOccupiedCells(x, z, targetX, targetZ, grid, direction, schematic.name, cells);
                Iterator linkedCellsIter = linkedCells.iterator();
                    do {
                        Cell linkedCell = (Cell) linkedCellsIter.next();
                        List<Direction> linkedNearbyRoads = nearbyRoadsOnly(linkedCell.x, linkedCell.z);
                        Direction linkedDirection = null;
                        if (linkedNearbyRoads.size() > 1) {
                            linkedDirection = linkedNearbyRoads.get(r.nextInt(linkedNearbyRoads.size()));
                        } else if (linkedNearbyRoads.size() == 1) {
                            linkedDirection = linkedNearbyRoads.get(0);
                        }
                        if (linkedDirection == null) {
                            continue;
                        }
                        int linkedTargetZ = linkedCell.z;
                        int linkedTargetX = linkedCell.x;
                        int linkedX = linkedSchematic.xLength;
                        int linkedZ = linkedSchematic.zHeight;
                        if (linkedDirection == Direction.W || linkedDirection == Direction.E) {
                            linkedX = linkedSchematic.zHeight;
                            linkedZ = linkedSchematic.xLength;
                        }
                        if (this.schemFitsInBounds(linkedX, linkedZ, linkedTargetX, linkedTargetZ, grid, linkedDirection) && !containsToBeOccupiedCells(cellsToBeOccupied, toBeOccupiedCells(linkedX, linkedZ,linkedTargetX, linkedTargetZ, grid, linkedDirection, linkedSchematic.name, linkedCells))) {
                                ClipboardFormat format = ClipboardFormats.findByFile(new File(schemHandler.schematicDirectory, linkedSchematic.fileName + ".schematic"));
                                ClipboardReader reader = format.getReader(new FileInputStream(new File(schemHandler.schematicDirectory, linkedSchematic.fileName + ".schematic")));
                                Clipboard clipboard = reader.read();
                                final int finalLinkedX = linkedX;
                                final int finalLinkedZ = linkedZ;
                                final Direction finalLinkedDirection = linkedDirection;
                                    Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                                        @Override
                                        public void run() {
                                            EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(Bukkit.getWorlds().get(0)), -1);
                                            Operation operation = (new ClipboardHolder(clipboard)).createPaste(editSession).to(BlockVector3.at(startX + (linkedTargetX * cellSize), yLvl + 1, startZ + (linkedTargetZ * -cellSize))).ignoreAirBlocks(false).build();
                                            try {
                                                Operations.complete(operation);
                                            } catch (WorldEditException e) {
                                                throw new RuntimeException(e);
                                            }
                                            editSession.close();
                                            List<Cell> newCells = markAsOccupied(finalLinkedX, finalLinkedZ, linkedTargetX, linkedTargetZ, grid, finalLinkedDirection, linkedSchematic.name, linkedCells);
                                            if (linkedSchematic.district == District.FARM) {
                                                farmCells = newCells;
                                            } else if (linkedSchematic.district == District.ARISTOCRACY) {
                                                aristocracyCells = newCells;
                                            } else if (linkedSchematic.district == District.SLUMS) {
                                                slumsCells = newCells;
                                            } else if (linkedSchematic.district == District.PORT) {
                                                portCells = newCells;
                                            } else if (linkedSchematic.district == District.OUTSKIRTS) {
                                                outskirtsCells = newCells;
                                            }
                                            editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(Bukkit.getWorlds().get(0)), -1);
                                            operation = (new ClipboardHolder(clipboard)).createPaste(editSession).to(BlockVector3.at(startX + (targetX * cellSize), yLvl + 1, startZ + (targetZ * -cellSize))).ignoreAirBlocks(false).build();
                                            try {
                                                Operations.complete(operation);
                                            } catch (WorldEditException e) {
                                                throw new RuntimeException(e);
                                            }
                                            editSession.close();
                                            newCells = markAsOccupied(x, z, targetX, targetZ, grid, direction, schematic.name, cells);
                                            schematicList.remove(schematic);
                                            try {
                                                generateStructure(schematicList, grid, count, newCells, r);
                                            } catch (IOException e) {
                                                throw new RuntimeException(e);
                                            } catch (WorldEditException e) {
                                                throw new RuntimeException(e);
                                            } catch (DataException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }
                                    }, schematic.area);
                                    return;
                                } else if (direction == Direction.E) {
                                    Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                                        @Override
                                        public void run() {
                                            EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(Bukkit.getWorlds().get(0)), -1);
                                            ClipboardHolder holder = new ClipboardHolder(clipboard);
                                            holder.setTransform(new AffineTransform().rotateY(90));
                                            Operation operation = holder.createPaste(editSession).to(BlockVector3.at(startX + (targetX * cellSize) + 4, yLvl + 1, (startZ + (targetZ * -cellSize)) + 5 - cellSize)).ignoreAirBlocks(false).build();
                                            try {
                                                Operations.complete(operation);
                                            } catch (WorldEditException e) {
                                                throw new RuntimeException(e);
                                            }
                                            editSession.close();
                                            List<Cell> newCells = markAsOccupied(finalX, finalZ, targetX, targetZ, grid, finalDirection, schematic.name, cells);
                                            schematicList.remove(schematic);
                                            try {
                                                generateStructure(schematicList, grid, count, newCells, r);
                                            } catch (IOException e) {
                                                throw new RuntimeException(e);
                                            } catch (WorldEditException e) {
                                                throw new RuntimeException(e);
                                            } catch (DataException e) {
                                                throw new RuntimeException(e);
                                            }

                                        }
                                    }, schematic.area);
                                    return;
                                } else if (direction == Direction.N) {
                                    Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                                        @Override
                                        public void run() {
                                            EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(Bukkit.getWorlds().get(0)), -1);
                                            ClipboardHolder holder = new ClipboardHolder(clipboard);
                                            holder.setTransform(new AffineTransform().rotateY(180));
                                            Operation operation = holder.createPaste(editSession).to(BlockVector3.at((startX + ((targetX + 1) * cellSize) - 1), yLvl + 1, (startZ + (targetZ * -cellSize)) + 1 - cellSize)).ignoreAirBlocks(false).build();
                                            try {
                                                Operations.complete(operation);
                                            } catch (WorldEditException e) {
                                                throw new RuntimeException(e);
                                            }
                                            editSession.close();
                                            List<Cell> newCells = markAsOccupied(finalX, finalZ, targetX, targetZ, grid, finalDirection, schematic.name, cells);
                                            schematicList.remove(schematic);
                                            try {
                                                generateStructure(schematicList, grid, count, newCells, r);
                                            } catch (IOException e) {
                                                throw new RuntimeException(e);
                                            } catch (WorldEditException e) {
                                                throw new RuntimeException(e);
                                            } catch (DataException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }
                                    }, schematic.area);
                                    return;
                                } else if (direction == Direction.W) {
                                    Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                                        @Override
                                        public void run() {
                                            EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(Bukkit.getWorlds().get(0)), -1);
                                            ClipboardHolder holder = new ClipboardHolder(clipboard);
                                            holder.setTransform(new AffineTransform().rotateY(270));
                                            Operation operation = holder.createPaste(editSession).to(BlockVector3.at((startX + ((targetX + 1) * cellSize) - 5), yLvl + 1, (startZ + (targetZ * -cellSize) - 4))).ignoreAirBlocks(false).build();
                                            try {
                                                Operations.complete(operation);
                                            } catch (WorldEditException e) {
                                                throw new RuntimeException(e);
                                            }
                                            editSession.close();
                                            List<Cell> newCells = markAsOccupied(finalX, finalZ, targetX, targetZ, grid, finalDirection, schematic.name, cells);
                                            schematicList.remove(schematic);
                                            try {
                                                generateStructure(schematicList, grid, count, newCells, r);
                                            } catch (IOException e) {
                                                throw new RuntimeException(e);
                                            } catch (WorldEditException e) {
                                                throw new RuntimeException(e);
                                            } catch (DataException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }
                                    }, schematic.area);
                                    return;
                                }

                        }

                    } while (cellsIter.hasNext());
                }


        } while (cellsIter.hasNext());
        if (count < 5) {
            generateBuildingsHighway(grid, count + 1);
        }
*/
    }
    public void generateRepeatablesInitial(Cell[][] grid) throws IOException {
        Bukkit.getLogger().log(Level.INFO, "Starting repeatable generation...");
        lastGeneration = System.currentTimeMillis();
        List<Cell> remainingCells = new ArrayList<>();
        remainingCells.addAll(getCellsForDistrict(District.FARM));
        remainingCells.addAll(getCellsForDistrict(District.ARISTOCRACY));
        remainingCells.addAll(getCellsForDistrict(District.SLUMS));
        remainingCells.addAll(getCellsForDistrict(District.PORT));
        remainingCells.addAll(getCellsForDistrict(District.OUTSKIRTS));
        Collections.shuffle(remainingCells);
        Bukkit.getLogger().log(Level.INFO, "Remaining cells to be filled: " + remainingCells.size());
        generateRepeatables(grid, remainingCells);
    }
    public void generateRepeatables(Cell[][] grid, List<Cell> remainingCells) throws IOException {

        if (remainingCells.size() >= 1) {
            lastGeneration = System.currentTimeMillis();
            Cell cell = (Cell) remainingCells.get(0);
            //Bukkit.getLogger().log(Level.INFO, "Got cell " + cell.x + "," + cell.z);
            /*new BukkitRunnable() {
                @Override
                public void run() {
                    if (Grid.this.lastGeneration + 3500L < System.currentTimeMillis() && !generationCompleted) {
                        Bukkit.getLogger().log(Level.INFO, "Generation stalled, attempting a refresh...");
                        remainingCells.remove(0);
                        remainingCells.add(cell);
                        try {
                            generateRepeatables(grid, remainingCells);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }.runTaskLater(plugin, 80L);*/

            if (cell.isOccupied || cell.isRoad) {
                remainingCells.remove(cell);
                generateRepeatables(grid, remainingCells);
                //Bukkit.getLogger().log(Level.INFO, "Cell was occupied moving on...");
                return;
            }
            List<Direction> nearbyRoads = nearbyRoadsOnly(cell.x, cell.z);
            Direction direction = null;
            if (nearbyRoads.size() > 1) {
                direction = nearbyRoads.get(r.nextInt(nearbyRoads.size()));
            } else if (nearbyRoads.size() == 1) {
                direction = nearbyRoads.get(0);
            }
            if (direction == null) {
                if ((getOmniSchematicsForDistrict(cell.district)).size() < 1 || getOmniSchematicsForDistrict(cell.district).isEmpty()) {
                    localOmniRefresh(cell.district);
                }
                //Bukkit.getLogger().log(Level.INFO, "Attempting an omni paste for district " + cell.district + " at cell " + cell.x + "," + cell.z);
                List<Schematic> schematicList = getOmniSchematicsForDistrict(cell.district);
                int targetZ = cell.z;
                int targetX = cell.x;
                Iterator<Schematic> schemIter = schematicList.iterator();
                boolean pasted = false;
                do {
                    Schematic schematic = (Schematic) schemIter.next();
                    int x = schematic.xLength;
                    int z = schematic.zHeight;
                    List<Direction> randomDir = new ArrayList<>(potentialDirections);
                    Collections.shuffle(randomDir);
                    Iterator<Direction> dirIter = randomDir.iterator();
                    //Bukkit.getLogger().log(Level.INFO, "Attempting schematic " + schematic.name + " for cell " + cell.x + ", " + cell.z);
                    do {
                        Direction oDirection = (Direction) dirIter.next();
                        if (oDirection == Direction.W || oDirection == Direction.E) {
                            x = schematic.zHeight;
                            z = schematic.xLength;
                        } else if (oDirection == Direction.N || oDirection == Direction.S) {
                            x = schematic.xLength;
                            z = schematic.zHeight;
                        }
                        if (cell.district != District.OUTSKIRTS) {
                            if (this.schemFitsInBounds(x, z, targetX, targetZ, grid, oDirection)) {

                                Paste paste = new Paste(schematic, x, z, oDirection, targetX, targetZ);

                                pasteRepeatable(paste, remainingCells, true);
                                getOmniSchematicsForDistrict(paste.schematic.district).remove(paste.schematic);
                                pasted = true;
                                //Bukkit.getLogger().log(Level.INFO, "Passing to pasteRepeatable");

                            }
                        } else {
                            if (this.schemFitsInBoundsOutskirts(x, z, targetX, targetZ, grid, oDirection)) {
                                Paste paste = new Paste(schematic, x, z, oDirection, targetX, targetZ);
                                pasteRepeatable(paste, remainingCells, true);
                                getOmniSchematicsForDistrict(paste.schematic.district).remove(paste.schematic);
                                pasted = true;
                                //Bukkit.getLogger().log(Level.INFO, "Passing to pasteRepeatable");
                            }
                        }
                    } while (dirIter.hasNext() && !pasted);

                } while (schemIter.hasNext() && !pasted);
                if (!schemIter.hasNext() && !pasted) {
                    //Bukkit.getLogger().log(Level.INFO, "Schem iterator was exhausted");
                    localOmniRefresh(cell.district);
                    generateRepeatables(grid, remainingCells);
                    return;
                }
            } else {
                if (getRepeatableSchematicsForDistrict(cell.district).size() < 1 || getRepeatableSchematicsForDistrict(cell.district).isEmpty()) {
                    localRepeatableRefresh(cell.district);
                }
                List<Schematic> schematicList = getRepeatableSchematicsForDistrict(cell.district);

                int targetZ = cell.z;
                int targetX = cell.x;
                Iterator<Schematic> schemIter = schematicList.iterator();
                boolean pass = false;
                do {

                    Schematic schematic = (Schematic) schemIter.next();
                    //Bukkit.getLogger().log(Level.INFO, "Attempting schematic " + schematic.name + " for cell " + cell.x + ", " + cell.z);
                    int x = schematic.xLength;
                    int z = schematic.zHeight;
                    if (direction == Direction.W || direction == Direction.E) {
                        x = schematic.zHeight;
                        z = schematic.xLength;
                    }
                    if (cell.district != District.OUTSKIRTS) {
                        if (schemFitsInBounds(x, z, targetX, targetZ, grid, direction)) {
                            Paste paste = new Paste(schematic, x, z, direction, targetX, targetZ);
                            pasteRepeatable(paste, remainingCells, false);
                            //Bukkit.getLogger().log(Level.INFO, "Passing to pasteRepeatable");
                            pass = true;
                        }
                    } else {

                        if (schemFitsInBoundsOutskirts(x, z, targetX, targetZ, grid, direction)) {
                            Paste paste = new Paste(schematic, x, z, direction, targetX, targetZ);
                            pasteRepeatable(paste, remainingCells, false);
                            //Bukkit.getLogger().log(Level.INFO, "Passing to pasteRepeatable");
                            pass = true;
                        }
                    }

                } while (schemIter.hasNext() && !pass);
                if (!schemIter.hasNext() && !pass) {
                    //Bukkit.getLogger().log(Level.INFO, "Schem iterator was exhausted");
                    localRepeatableRefresh(cell.district);
                    generateRepeatables(grid, remainingCells);
                    return;
                }
            }
        } else {
            Bukkit.getLogger().log(Level.INFO, "Finished repeatable generation.");
            generationCompleted = true;
        }

    }


    public boolean schemFitsInBounds(int xSize, int zSize, int gridX, int gridZ, Cell[][] grid, Direction direction) {
        if (direction == Direction.S) {
            for (int x = 0; x < xSize; x++) {
                for (int z = 0; z < zSize; z++) {
                    if (((gridX + x) < this.size ) && ((gridZ + z) < size)) {
                        if (grid[gridX + x][gridZ + z].isOccupied) {
                            int xVal = gridX + x;
                            int zVal = gridZ + z;
                            //Bukkit.broadcastMessage("Schematic won't fit! Cell " + xVal + ", " + zVal + " is already occupied.");
                            return false;
                        }
                    } else {
                        //Bukkit.broadcastMessage("Schematic won't fit! Outside of map bounds!");
                        return false;
                    }
                }
            }
            return true;
        } else if (direction == Direction.W) {
            for (int x = 0; x < xSize; x++) {
                for (int z = 0; z < zSize; z++) {
                    if (((gridX + x) < this.size ) && ((gridZ - z) >= 0)) {
                        if (grid[gridX + x][gridZ - z].isOccupied) {
                            int xVal = gridX + x;
                            int zVal = gridZ - z;
                            //Bukkit.broadcastMessage("Schematic won't fit! Cell " + xVal + ", " + zVal + " is already occupied.");
                            return false;
                        }
                    } else {
                        //Bukkit.broadcastMessage("Schematic won't fit! Outside of map bounds!");
                        return false;
                    }
                }
            }
            return true;
        } else if (direction == Direction.N) {
            for (int x = 0; x < xSize; x++) {
                for (int z = 0; z < zSize; z++) {
                    if (((gridX - x) >= 0 ) && ((gridZ - z) >= 0)) {
                        if (grid[gridX - x][gridZ - z].isOccupied) {
                            int xVal = gridX - x;
                            int zVal = gridZ - z;
                            //Bukkit.broadcastMessage("Schematic won't fit! Cell " + xVal + ", " + zVal + " is already occupied.");
                            return false;
                        }
                    } else {
                        //Bukkit.broadcastMessage("Schematic won't fit! Outside of map bounds!");
                        return false;
                    }
                }
            }
            return true;
        } else if (direction == Direction.E) {
            for (int x = 0; x < xSize; x++) {
                for (int z = 0; z < zSize; z++) {
                    if (((gridX - x) >= 0 ) && ((gridZ + z) < size)) {
                        if (grid[gridX - x][gridZ + z].isOccupied) {
                            int xVal = gridX - x;
                            int zVal = gridZ + z;
                            //Bukkit.broadcastMessage("Schematic won't fit! Cell " + xVal + ", " + zVal + " is already occupied.");
                            return false;
                        }
                    } else {
                        //Bukkit.broadcastMessage("Schematic won't fit! Outside of map bounds!");
                        return false;
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }
    public boolean schemFitsInBoundsOutskirts(int xSize, int zSize, int gridX, int gridZ, Cell[][] grid, Direction direction) {
        if (direction == Direction.S) {
            for (int x = 0; x < xSize; x++) {
                for (int z = 0; z < zSize; z++) {
                    if (((gridX + x) < this.size ) && ((gridZ + z) < (int)size*1.5)) {
                        if (grid[gridX + x][gridZ + z].isOccupied) {
                            int xVal = gridX + x;
                            int zVal = gridZ + z;
                            //Bukkit.broadcastMessage("Schematic won't fit! Cell " + xVal + ", " + zVal + " is already occupied.");
                            return false;
                        }
                    } else {
                        //Bukkit.broadcastMessage("Schematic won't fit! Outside of map bounds!");
                        return false;
                    }
                }
            }
            return true;
        } else if (direction == Direction.W) {
            for (int x = 0; x < xSize; x++) {
                for (int z = 0; z < zSize; z++) {
                    if (((gridX + x) < this.size ) && ((gridZ - z) >= size)) {
                        if (grid[gridX + x][gridZ - z].isOccupied) {
                            int xVal = gridX + x;
                            int zVal = gridZ - z;
                            //Bukkit.broadcastMessage("Schematic won't fit! Cell " + xVal + ", " + zVal + " is already occupied.");
                            return false;
                        }
                    } else {
                        //Bukkit.broadcastMessage("Schematic won't fit! Outside of map bounds!");
                        return false;
                    }
                }
            }
            return true;
        } else if (direction == Direction.N) {
            for (int x = 0; x < xSize; x++) {
                for (int z = 0; z < zSize; z++) {
                    if (((gridX - x) >= 0 ) && ((gridZ - z) >= size)) {
                        if (grid[gridX - x][gridZ - z].isOccupied) {
                            int xVal = gridX - x;
                            int zVal = gridZ - z;
                            //Bukkit.broadcastMessage("Schematic won't fit! Cell " + xVal + ", " + zVal + " is already occupied.");
                            return false;
                        }
                    } else {
                        //Bukkit.broadcastMessage("Schematic won't fit! Outside of map bounds!");
                        return false;
                    }
                }
            }
            return true;
        } else if (direction == Direction.E) {
            for (int x = 0; x < xSize; x++) {
                for (int z = 0; z < zSize; z++) {
                    if (((gridX - x) >= 0 ) && ((gridZ + z) < size*1.5)) {
                        if (grid[gridX - x][gridZ + z].isOccupied) {
                            int xVal = gridX - x;
                            int zVal = gridZ + z;
                            //Bukkit.broadcastMessage("Schematic won't fit! Cell " + xVal + ", " + zVal + " is already occupied.");
                            return false;
                        }
                    } else {
                        //Bukkit.broadcastMessage("Schematic won't fit! Outside of map bounds!");
                        return false;
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public List<Cell> toBeOccupiedCells(int xSize, int zSize, int gridX, int gridZ, Cell[][] grid, Direction direction) {
        List<Cell> returnList = new ArrayList<>();
        if (direction == Direction.S) {
            for (int x = 0; x < xSize; x++) {
                for (int z = 0; z < zSize; z++) {
                    if (((gridX + x) < this.size ) && ((gridZ + z) < size)) {
                        int xVal = gridX + x;
                        int zVal = gridZ + z;
                        //Bukkit.broadcastMessage(ChatColor.AQUA + "Marking cell " + xVal + ", " + zVal + " as occupied");
                        returnList.add(grid[gridX + x][gridZ + z]);

                    } else {
                        Bukkit.broadcastMessage(ChatColor.RED + "This is going to throw an error.");
                    }
                }
            }
        } else if (direction == Direction.W) {
            for (int x = 0; x < xSize; x++) {
                for (int z = 0; z < zSize; z++) {
                    if (((gridX + x) < this.size ) && ((gridZ - z) >= 0 )) {
                        int xVal = gridX + x;
                        int zVal = gridZ - z;
                        returnList.add(grid[xVal][zVal]);
                    } else {
                        Bukkit.broadcastMessage(ChatColor.RED + "This is going to throw an error.");
                    }
                }
            }
        } else if (direction == Direction.E) {
            for (int x = 0; x < xSize; x++) {
                for (int z = 0; z < zSize; z++) {
                    if (((gridX - x) >= 0 ) && ((gridZ + z) < size)) {
                        int xVal = gridX - x;
                        int zVal = gridZ + z;
                        //Bukkit.broadcastMessage(ChatColor.AQUA + "Marking cell " + xVal + ", " + zVal + " as occupied");
                        returnList.add(grid[xVal][zVal]);
                    } else {
                        Bukkit.broadcastMessage(ChatColor.RED + "This is going to throw an error.");
                    }
                }
            }
        } else if (direction == Direction.N) {
            for (int x = 0; x < xSize; x++) {
                for (int z = 0; z < zSize; z++) {
                    if (((gridX - x) >= 0 ) && ((gridZ - z) >= 0)) {
                        returnList.add(grid[gridX - x][gridZ - z]);
                    } else {
                        Bukkit.broadcastMessage(ChatColor.RED + "This is going to throw an error.");
                    }
                }
            }
        }
        return returnList;
    }
    public List<Cell> toBeOccupiedCellsOutskirts(int xSize, int zSize, int gridX, int gridZ, Cell[][] grid, Direction direction) {
        List<Cell> returnList = new ArrayList<>();
        if (direction == Direction.S) {
            for (int x = 0; x < xSize; x++) {
                for (int z = 0; z < zSize; z++) {
                    if (((gridX + x) < this.size ) && ((gridZ + z) < size*1.5)) {
                        int xVal = gridX + x;
                        int zVal = gridZ + z;
                        //Bukkit.broadcastMessage(ChatColor.AQUA + "Marking cell " + xVal + ", " + zVal + " as occupied");
                        returnList.add(grid[gridX + x][gridZ + z]);

                    } else {
                        Bukkit.broadcastMessage(ChatColor.RED + "This is going to throw an error.");
                    }
                }
            }
        } else if (direction == Direction.W) {
            for (int x = 0; x < xSize; x++) {
                for (int z = 0; z < zSize; z++) {
                    if (((gridX + x) < this.size ) && ((gridZ - z) >= size )) {
                        int xVal = gridX + x;
                        int zVal = gridZ - z;
                        returnList.add(grid[xVal][zVal]);
                    } else {
                        Bukkit.broadcastMessage(ChatColor.RED + "This is going to throw an error.");
                    }
                }
            }
        } else if (direction == Direction.E) {
            for (int x = 0; x < xSize; x++) {
                for (int z = 0; z < zSize; z++) {
                    if (((gridX - x) >= 0 ) && ((gridZ + z) < size*1.5)) {
                        int xVal = gridX - x;
                        int zVal = gridZ + z;
                        //Bukkit.broadcastMessage(ChatColor.AQUA + "Marking cell " + xVal + ", " + zVal + " as occupied");
                        returnList.add(grid[xVal][zVal]);
                    } else {
                        Bukkit.broadcastMessage(ChatColor.RED + "This is going to throw an error.");
                    }
                }
            }
        } else if (direction == Direction.N) {
            for (int x = 0; x < xSize; x++) {
                for (int z = 0; z < zSize; z++) {
                    if (((gridX - x) >= 0 ) && ((gridZ - z) >= size)) {
                        returnList.add(grid[gridX - x][gridZ - z]);
                    } else {
                        Bukkit.broadcastMessage(ChatColor.RED + "This is going to throw an error.");
                    }
                }
            }
        }
        return returnList;
    }
    public boolean containsToBeOccupiedCells(List<Cell> parentCells, List<Cell>linkedCells) {
        Iterator iter = linkedCells.iterator();
        while (iter.hasNext()) {
            Cell cell = (Cell) iter.next();
            for (Cell parentCell : parentCells) {
                if (cell.x == parentCell.x && cell.z == parentCell.z) {
                    return true;
                }
            }
        }
        return false;
    }
    public List<Cell> markAsOccupied(int xSize, int zSize, int gridX, int gridZ, Cell[][] grid, Direction direction, String schematicName) {
        List<Cell> cellsToRemove = new ArrayList<>();
        if (direction == Direction.S) {
            for (int x = 0; x < xSize; x++) {
                for (int z = 0; z < zSize; z++) {
                    if (((gridX + x) < this.size ) && ((gridZ + z) < size)) {
                        int xVal = gridX + x;
                        int zVal = gridZ + z;
                        //Bukkit.broadcastMessage(ChatColor.AQUA + "Marking cell " + xVal + ", " + zVal + " as occupied");
                        grid[gridX + x][gridZ + z].isOccupied = true;
                        grid[gridX + x][gridZ + z].schematicAtCell = schematicName;
                        cellsToRemove.add(grid[gridX + x][gridZ + z]);
                    } else {
                        Bukkit.broadcastMessage(ChatColor.RED + "This is going to throw an error.");
                    }
                }
            }
        } else if (direction == Direction.W) {
            for (int x = 0; x < xSize; x++) {
                for (int z = 0; z < zSize; z++) {
                    if (((gridX + x) < this.size ) && ((gridZ - z) >= 0 )) {
                        int xVal = gridX + x;
                        int zVal = gridZ - z;
                        grid[xVal][zVal].isOccupied = true;
                        grid[xVal][zVal].schematicAtCell = schematicName;
                        cellsToRemove.add(grid[xVal][zVal]);
                    } else {
                        Bukkit.broadcastMessage(ChatColor.RED + "This is going to throw an error.");
                    }
                }
            }
        } else if (direction == Direction.E) {
            for (int x = 0; x < xSize; x++) {
                for (int z = 0; z < zSize; z++) {
                    if (((gridX - x) >= 0 ) && ((gridZ + z) < size)) {
                        int xVal = gridX - x;
                        int zVal = gridZ + z;
                        //Bukkit.broadcastMessage(ChatColor.AQUA + "Marking cell " + xVal + ", " + zVal + " as occupied");
                        grid[xVal][zVal].isOccupied = true;
                        grid[xVal][zVal].schematicAtCell = schematicName;
                        cellsToRemove.add(grid[xVal][zVal]);
                    } else {
                        Bukkit.broadcastMessage(ChatColor.RED + "This is going to throw an error.");
                    }
                }
            }
        } else if (direction == Direction.N) {
            for (int x = 0; x < xSize; x++) {
                for (int z = 0; z < zSize; z++) {
                    if (((gridX - x) >= 0 ) && ((gridZ - z) >= 0)) {
                        grid[gridX - x][gridZ - z].isOccupied = true;
                        grid[gridX - x][gridZ - z].schematicAtCell = schematicName;
                        cellsToRemove.add(grid[gridX - x][gridZ - z]);
                    } else {
                        Bukkit.broadcastMessage(ChatColor.RED + "This is going to throw an error.");
                    }
                }
            }
        }
        return cellsToRemove;
    }
    public List<Cell> markAsOccupiedOutskirts(int xSize, int zSize, int gridX, int gridZ, Cell[][] grid, Direction direction, String schematicName) {
        List<Cell> cellsToRemove = new ArrayList<>();
        if (direction == Direction.S) {
            for (int x = 0; x < xSize; x++) {
                for (int z = 0; z < zSize; z++) {
                    if (((gridX + x) < this.size ) && ((gridZ + z) < size*1.5)) {
                        int xVal = gridX + x;
                        int zVal = gridZ + z;
                        //Bukkit.broadcastMessage(ChatColor.AQUA + "Marking cell " + xVal + ", " + zVal + " as occupied");
                        grid[xVal][zVal].isOccupied = true;
                        grid[xVal][zVal].schematicAtCell = schematicName;
                        cellsToRemove.add(grid[xVal][zVal]);
                    } else {
                        Bukkit.broadcastMessage(ChatColor.RED + "This is going to throw an error.");
                    }
                }
            }
        } else if (direction == Direction.W) {
            for (int x = 0; x < xSize; x++) {
                for (int z = 0; z < zSize; z++) {
                    if (((gridX + x) < this.size ) && ((gridZ - z) >= size )) {
                        int xVal = gridX + x;
                        int zVal = gridZ - z;
                        grid[xVal][zVal].isOccupied = true;
                        grid[xVal][zVal].schematicAtCell = schematicName;
                        cellsToRemove.add(grid[xVal][zVal]);
                    } else {
                        Bukkit.broadcastMessage(ChatColor.RED + "This is going to throw an error.");
                    }
                }
            }
        } else if (direction == Direction.E) {
            for (int x = 0; x < xSize; x++) {
                for (int z = 0; z < zSize; z++) {
                    if (((gridX - x) >= 0 ) && ((gridZ + z) < size*1.5)) {
                        int xVal = gridX - x;
                        int zVal = gridZ + z;
                        //Bukkit.broadcastMessage(ChatColor.AQUA + "Marking cell " + xVal + ", " + zVal + " as occupied");
                        grid[xVal][zVal].isOccupied = true;
                        grid[xVal][zVal].schematicAtCell = schematicName;
                        cellsToRemove.add(grid[xVal][zVal]);
                    } else {
                        Bukkit.broadcastMessage(ChatColor.RED + "This is going to throw an error.");
                    }
                }
            }
        } else if (direction == Direction.N) {
            for (int x = 0; x < xSize; x++) {
                for (int z = 0; z < zSize; z++) {
                    if (((gridX - x) >= 0 ) && ((gridZ - z) >= size)) {
                        int xVal = gridX - x;
                        int zVal = gridZ - z;
                        grid[xVal][zVal].isOccupied = true;
                        grid[xVal][zVal].schematicAtCell = schematicName;
                        cellsToRemove.add(grid[xVal][zVal]);
                    } else {
                        Bukkit.broadcastMessage(ChatColor.RED + "This is going to throw an error.");
                    }
                }
            }
        }
        return cellsToRemove;
    }

    public String schematicAtCell(int x, int z) {
        return "Cell at " + x + ", " + z + " is occupied by " + this.grid[x][z].schematicAtCell + ".\n isOccupied status is " + this.grid[x][z].isOccupied + "\nisRoad status is " + this.grid[x][z].isRoad;
    }

    public List<Cell> getCellsForDistrict(District district) {
        if (district == District.FARM) {
            return farmCells;
        } else if (district == District.PORT) {
            return portCells;
        } else if (district == District.ARISTOCRACY) {
            return aristocracyCells;
        } else if (district == District.SLUMS) {
            return slumsCells;
        } else {
            return outskirtsCells;
        }
    }

    public List<Schematic> getSchematicsForDistrict(District district) {
        if (district == District.FARM) {
            return plugin.schematicHandler.farmSchematics;
        } else if (district == District.PORT) {
            return plugin.schematicHandler.portSchematics;
        } else if (district == District.ARISTOCRACY) {
            return plugin.schematicHandler.aristocracySchematics;
        } else if (district == District.SLUMS) {
            return plugin.schematicHandler.slumsSchematics;
        } else {
            return plugin.schematicHandler.outskirtsSchematics;
        }
    }
    public List<Schematic> getOmniSchematicsForDistrict(District district) {
        if (district == District.FARM) {
            return localFarmOmni;
        } else if (district == District.PORT) {
            return localPortOmni;
        } else if (district == District.ARISTOCRACY) {
            return localAristocracyOmni;
        } else if (district == District.SLUMS) {
            return localSlumsOmni;
        } else {
            return localOutskirtsOmni;
        }
    }
    public List<Schematic> getRepeatableSchematicsForDistrict(District district) {
        if (district == District.FARM) {
            return localFarmRepeatable;
        } else if (district == District.PORT) {
            return localPortRepeatable;
        } else if (district == District.ARISTOCRACY) {
            return localAristocracyRepeatable;
        } else if (district == District.SLUMS) {
            return localSlumsRepeatable;
        } else {
            return localOutskirtsRepeatable;
        }
    }

    public void localRepeatableRefresh(District district) {
        if (district == District.FARM) {
            localFarmRepeatable.addAll(plugin.schematicHandler.farmSchematicsRepeatable);
            Collections.sort(localFarmRepeatable);
        } else if (district == District.PORT) {
            localPortRepeatable.addAll(plugin.schematicHandler.portSchematicsRepeatable);
            Collections.sort(localPortRepeatable);
        } else if (district == District.ARISTOCRACY) {
            localAristocracyRepeatable.addAll(plugin.schematicHandler.aristocracySchematicsRepeatable);
            Collections.sort(localAristocracyRepeatable);
        } else if (district == District.SLUMS) {
            localSlumsRepeatable.addAll(plugin.schematicHandler.slumsSchematicsRepeatable);
            Collections.sort(localSlumsRepeatable);
        } else {
            localOutskirtsRepeatable.addAll(plugin.schematicHandler.outskirtsSchematicsRepeatable);
            Collections.sort(localOutskirtsRepeatable);
        }
    }

    public void localOmniRefresh(District district) {
        if (district == District.FARM) {
            localFarmOmni.addAll(plugin.schematicHandler.farmSchematicsOmni);
            Collections.sort(localFarmOmni);
        } else if (district == District.PORT) {
            localPortOmni.addAll(plugin.schematicHandler.portSchematicsOmni);
            Collections.sort(localPortOmni);
        } else if (district == District.ARISTOCRACY) {
            localAristocracyOmni.addAll(plugin.schematicHandler.aristocracySchematicsOmni);
            Collections.sort(localAristocracyOmni);
        } else if (district == District.SLUMS) {
            localSlumsOmni.addAll(plugin.schematicHandler.slumsSchematicsOmni);
            Collections.sort(localSlumsOmni);
        } else {
            localOutskirtsOmni.addAll(plugin.schematicHandler.outskirtsSchematicsOmni);
            Collections.sort(localOutskirtsOmni);
        }
    }

    public void registerBlackList(List<String> list) {

        for (String line : list) {
            if (line.matches("[0-9]+,[0-9]+")) {
                String[] parts = line.split(",");
                this.blacklistedCells.add(new int[]{Integer.parseInt(parts[0]),Integer.parseInt(parts[1])});
            } else if (line.matches(">[0-9]+,[0-9]+")) {
                String[] parts = line.split(",");
                parts[0] = parts[0].replace(">","");
                for (int i = Integer.parseInt(parts[0])+1; i < this.size; i++) {
                    this.blacklistedCells.add(new int[]{i, Integer.parseInt(parts[1])});
                }
            } else if (line.matches("<[0-9]+,[0-9]+")) {
                String[] parts = line.split(",");
                parts[0] = parts[0].replace("<","");
                for (int i = 0; i < Integer.parseInt(parts[0]); i++) {
                    this.blacklistedCells.add(new int[]{i, Integer.parseInt(parts[1])});
                }
            } else if (line.matches("[0-9]+,>[0-9]+")) {
                String[] parts = line.split(",");
                parts[1] = parts[1].replace(">","");
                for (int i = Integer.parseInt(parts[1])+1; i < this.size*1.5; i++) {
                    this.blacklistedCells.add(new int[]{Integer.parseInt(parts[0]), i});
                }
            } else if (line.matches("[0-9]+,<[0-9]+")) {
                String[] parts = line.split(",");
                parts[1] = parts[1].replace("<","");
                for (int i = 0; i < Integer.parseInt(parts[1]); i++) {
                    this.blacklistedCells.add(new int[]{Integer.parseInt(parts[0]), i});
                }
            } else if (line.matches(">[0-9]+,>[0-9]+")) {
                String[] parts = line.split(",");
                parts[0] = parts[0].replace(">","");
                parts[1] = parts[1].replace(">","");
                for (int x = Integer.parseInt(parts[0])+1; x < this.size; x++) {
                    for (int z = Integer.parseInt(parts[1])+1; z < this.size*1.5; z++) {
                        this.blacklistedCells.add(new int[]{x,z});
                    }
                }
            } else if (line.matches("<[0-9]+,>[0-9]+")) {
                String[] parts = line.split(",");
                parts[0] = parts[0].replace("<","");
                parts[1] = parts[1].replace(">","");
                for (int x = 0; x < Integer.parseInt(parts[0]); x++) {
                    for (int z = Integer.parseInt(parts[1])+1; z < this.size*1.5; z++) {
                        this.blacklistedCells.add(new int[]{x,z});
                    }
                }
            } else if (line.matches(">[0-9]+,<[0-9]+")) {
                String[] parts = line.split(",");
                parts[0] = parts[0].replace("<","");
                parts[1] = parts[1].replace("<","");
                for (int x = Integer.parseInt(parts[0])+1; x < this.size ; x++) {
                    for (int z = 0; z < Integer.parseInt(parts[1]); z++) {
                        this.blacklistedCells.add(new int[]{x,z});
                    }
                }
            } else if (line.matches("<[0-9]+,<[0-9]+")) {
                String[] parts = line.split(",");
                parts[0] = parts[0].replace("<","");
                parts[1] = parts[1].replace("<","");
                for (int x = 0; x < Integer.parseInt(parts[0]); x++) {
                    for (int z = 0; z < Integer.parseInt(parts[1]); z++) {
                        this.blacklistedCells.add(new int[]{x,z});
                    }
                }
            } else if (line.matches("x = [0-9]+")) {
                String x = line.replace("x = ", "");
                for (int z = 0; z < this.size*1.5; z++) {
                    this.blacklistedCells.add(new int[]{Integer.parseInt(x), z});
                }
            } else if (line.matches("z = [0-9]+")) {
                String z = line.replace("z = ", "");
                for (int x = 0; x < this.size; x++) {
                    this.blacklistedCells.add(new int[]{x, Integer.parseInt(z)});
                }
            }
        }
    }


}
