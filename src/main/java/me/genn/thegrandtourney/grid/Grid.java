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
import me.genn.thegrandtourney.mobs.Spawner;
import me.genn.thegrandtourney.npc.TGTNpc;
import me.genn.thegrandtourney.skills.farming.Crop;
import me.genn.thegrandtourney.skills.fishing.FishingZone;
import me.genn.thegrandtourney.skills.mining.Ore;
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
    public List<Cell> listOfCellsThatShouldHaveBeenPastedTo;
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

    public Grid(SchematicHandler schematicHandler, TGT plugin) {
        this.schemHandler = schematicHandler;
        this.plugin = plugin;
        this.r = new Random();
        this.schematicDetailsDirectory = new File(plugin.getDataFolder(), "schematic-contents");
    }

    public void initialize() throws DataException, WorldEditException, IOException {
        grid = new Cell[size][size];
        highways = new HashMap();
        listOfCellsThatShouldHaveBeenPastedTo = new ArrayList();
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
        for (int z = 0; z < size; z++) {
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
        for (int z = 0; z < size; z++) {
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
                    this.fillCellWithBlock(Material.COBBLESTONE, lastX , lastZ);


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
                    Operation operation = (new ClipboardHolder(clipboard)).createPaste(editSession).to(BlockVector3.at(startX + (paste.targetCellX * cellSize), yLvl + 1, startZ + (paste.targetCellZ * -cellSize))).ignoreAirBlocks(true).build();
                    try {
                        Operations.complete(operation);
                    } catch (WorldEditException e) {
                        throw new RuntimeException(e);
                    }
                    editSession.close();
                    List<Cell> newCells = markAsOccupied(finalX, finalZ, paste.targetCellX, paste.targetCellZ, grid, finalDirection, paste.schematic.name);
                    getCellsForDistrict(paste.schematic.district).removeAll(newCells);
                    try {
                        paste(parentPaste, getCellsForDistrict(parentPaste.schematic.district));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
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
                    Operation operation = holder.createPaste(editSession).to(BlockVector3.at(startX + (paste.targetCellX * cellSize) + 4, yLvl + 1, (startZ + (paste.targetCellZ * -cellSize)) + 5 - cellSize)).ignoreAirBlocks(true).build();
                    try {
                        Operations.complete(operation);
                    } catch (WorldEditException e) {
                        throw new RuntimeException(e);
                    }
                    editSession.close();
                    List<Cell> newCells = markAsOccupied(finalX, finalZ, paste.targetCellX, paste.targetCellZ, grid, finalDirection, paste.schematic.name);
                    getCellsForDistrict(paste.schematic.district).removeAll(newCells);
                    try {
                        paste(parentPaste, getCellsForDistrict(parentPaste.schematic.district));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
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
                    Operation operation = holder.createPaste(editSession).to(BlockVector3.at((startX + ((paste.targetCellX + 1) * cellSize) - 1), yLvl + 1, (startZ + (paste.targetCellZ * -cellSize)) + 1 - cellSize)).ignoreAirBlocks(true).build();
                    try {
                        Operations.complete(operation);
                    } catch (WorldEditException e) {
                        throw new RuntimeException(e);
                    }
                    editSession.close();
                    List<Cell> newCells = markAsOccupied(finalX, finalZ, paste.targetCellX, paste.targetCellZ, grid, finalDirection, paste.schematic.name);
                    getCellsForDistrict(paste.schematic.district).removeAll(newCells);
                    try {
                        paste(parentPaste, getCellsForDistrict(parentPaste.schematic.district));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
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
                    Operation operation = holder.createPaste(editSession).to(BlockVector3.at((startX + ((paste.targetCellX + 1) * cellSize) - 5), yLvl + 1, (startZ + (paste.targetCellZ * -cellSize) - 4))).ignoreAirBlocks(true).build();
                    try {
                        Operations.complete(operation);
                    } catch (WorldEditException e) {
                        throw new RuntimeException(e);
                    }
                    editSession.close();
                    List<Cell> newCells = markAsOccupied(finalX, finalZ, paste.targetCellX, paste.targetCellZ, grid, finalDirection, paste.schematic.name);
                    getCellsForDistrict(paste.schematic.district).removeAll(newCells);
                    try {
                        paste(parentPaste, getCellsForDistrict(parentPaste.schematic.district));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
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
        Bukkit.getLogger().log(Level.INFO, "Performing paste of " + paste.schematic.name + " coords=" + paste.targetCellX + "," + paste.targetCellZ + " dir=" + paste.direction);
        if (paste.direction == Direction.S) {
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(Bukkit.getWorlds().get(0)), -1);
                    Operation operation = (new ClipboardHolder(clipboard)).createPaste(editSession).to(BlockVector3.at(startX + (paste.targetCellX * cellSize), yLvl + 1, startZ + (paste.targetCellZ * -cellSize))).ignoreAirBlocks(true).build();
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
                    Operation operation = holder.createPaste(editSession).to(BlockVector3.at(startX + (paste.targetCellX * cellSize) + 4, yLvl + 1, (startZ + (paste.targetCellZ * -cellSize)) + 5 - cellSize)).ignoreAirBlocks(true).build();
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
                    Operation operation = holder.createPaste(editSession).to(BlockVector3.at((startX + ((paste.targetCellX + 1) * cellSize) - 1), yLvl + 1, (startZ + (paste.targetCellZ * -cellSize)) + 1 - cellSize)).ignoreAirBlocks(true).build();
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
                    Operation operation = holder.createPaste(editSession).to(BlockVector3.at((startX + ((paste.targetCellX + 1) * cellSize) - 5), yLvl + 1, (startZ + (paste.targetCellZ * -cellSize) - 4))).ignoreAirBlocks(true).build();
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
                plugin.oreObjectiveLocList.put(ore, loc);
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
                plugin.npcObjectiveLocList.put(npc, loc);
                npc.pasteLocation = loc;
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
                spawner.paste(loc, paste, counter);
                plugin.spawnerObjectiveLocList.put(spawner, loc);
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
                crop.spawn(loc, true);
                plugin.cropObjectiveLocList.put(crop, loc);
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
                zone.pasteZone(minLoc, maxLoc, paste.schematic.name + "." + key + "." + zone.template.name);
                Location objLocation = new Location(minLoc.getWorld(), minLoc.getX()+(maxLoc.getX()- minLoc.getX())*0.5,minLoc.getY()+(maxLoc.getY()- minLoc.getY())*0.5,minLoc.getZ()+(maxLoc.getZ()- minLoc.getZ())*0.5);
                plugin.fishingZoneObjectiveLocList.put(zone, objLocation);
            }
        }

    }
    public Location getPasteLocation(Paste paste, double x, double y, double z) {
        if (paste.direction == Direction.S) {
            Location loc = new Location(Bukkit.getWorlds().get(0), startX + (paste.targetCellX * cellSize), yLvl + 1, startZ + (paste.targetCellZ * -cellSize));
            loc.add(x, y, z);
            return loc;
        } else if (paste.direction == Direction.N) {
            Location loc = new Location(Bukkit.getWorlds().get(0), (startX + ((paste.targetCellX + 1) * cellSize) - 1), yLvl + 1, (startZ + (paste.targetCellZ * -cellSize)) + 1 - cellSize);
            loc.add(-x, y, -z);
            return loc;
        } else if (paste.direction == Direction.E) {
            Location loc = new Location(Bukkit.getWorlds().get(0), startX + (paste.targetCellX * cellSize) + 4, yLvl + 1, (startZ + (paste.targetCellZ * -cellSize)) + 5 - cellSize);
            loc.add(z, y, -x);
            return loc;
        } else if (paste.direction == Direction.W) {
            Location loc = new Location(Bukkit.getWorlds().get(0), (startX + ((paste.targetCellX + 1) * cellSize) - 5), yLvl + 1, (startZ + (paste.targetCellZ * -cellSize) - 4));
            loc.add(-z, y, x);
            return loc;
        }
        return null;
    }
    public void generateNpcsInPaste(Paste paste) {
        for (String str : paste.schematic.npcNames) {
            TGTNpc npc = plugin.npcHandler.allNpcs.stream().filter(obj -> obj.internalName.equalsIgnoreCase(str)).findFirst().orElse(null);

            if (npc != null) {

                if (paste.direction == Direction.S) {
                    Location loc = new Location(Bukkit.getWorlds().get(0), startX + (paste.targetCellX * cellSize), yLvl + 1, startZ + (paste.targetCellZ * -cellSize));
                    loc.add(npc.spawnX, npc.spawnY, npc.spawnZ);
                    npc.npc.spawn(loc);
                } else if (paste.direction == Direction.N) {
                    Location loc = new Location(Bukkit.getWorlds().get(0), (startX + ((paste.targetCellX + 1) * cellSize) - 1), yLvl + 1, (startZ + (paste.targetCellZ * -cellSize)) + 1 - cellSize);
                    loc.add(-npc.spawnX, npc.spawnY, -npc.spawnZ);
                    npc.npc.spawn(loc);
                } else if (paste.direction == Direction.E) {
                    Location loc = new Location(Bukkit.getWorlds().get(0), startX + (paste.targetCellX * cellSize) + 4, yLvl + 1, (startZ + (paste.targetCellZ * -cellSize)) + 5 - cellSize);
                    loc.add(-npc.spawnX, npc.spawnY, npc.spawnZ);
                    npc.npc.spawn(loc);
                } else if (paste.direction == Direction.W) {
                    Location loc = new Location(Bukkit.getWorlds().get(0), (startX + ((paste.targetCellX + 1) * cellSize) - 5), yLvl + 1, (startZ + (paste.targetCellZ * -cellSize) - 4));
                    loc.add(npc.spawnX, npc.spawnY, -npc.spawnZ);
                    npc.npc.spawn(loc);
                }

            }
        }
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
    public void pasteRepeatable(Paste paste, List<Cell> cells) throws IOException {
        ClipboardFormat format = ClipboardFormats.findByFile(new File(schemHandler.schematicDirectory, paste.schematic.fileName + ".schematic"));
        ClipboardReader reader = format.getReader(new FileInputStream(new File(schemHandler.schematicDirectory, paste.schematic.fileName + ".schematic")));
        Clipboard clipboard = reader.read();
        final int finalX = paste.x;
        final int finalZ = paste.z;
        final Direction finalDirection = paste.direction;
        Bukkit.getLogger().log(Level.INFO, "Performing repeatable paste of " + paste.schematic.name + " coords=" + paste.targetCellX + "," + paste.targetCellZ + " dir=" + paste.direction);
        if (paste.direction == Direction.S) {
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(Bukkit.getWorlds().get(0)), -1);
                    Operation operation = (new ClipboardHolder(clipboard)).createPaste(editSession).to(BlockVector3.at(startX + (paste.targetCellX * cellSize), yLvl + 1, startZ + (paste.targetCellZ * -cellSize))).ignoreAirBlocks(true).build();
                    try {
                        Operations.complete(operation);
                    } catch (WorldEditException e) {
                        throw new RuntimeException(e);
                    }
                    editSession.close();
                    List<Cell> newCells = markAsOccupied(finalX, finalZ, paste.targetCellX, paste.targetCellZ, grid, finalDirection, paste.schematic.name);
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
            }, paste.schematic.area);
            return;
        } else if (paste.direction == Direction.E) {
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(Bukkit.getWorlds().get(0)), -1);
                    ClipboardHolder holder = new ClipboardHolder(clipboard);
                    holder.setTransform(new AffineTransform().rotateY(90));
                    Operation operation = holder.createPaste(editSession).to(BlockVector3.at(startX + (paste.targetCellX * cellSize) + 4, yLvl + 1, (startZ + (paste.targetCellZ * -cellSize)) + 5 - cellSize)).ignoreAirBlocks(true).build();
                    try {
                        Operations.complete(operation);
                    } catch (WorldEditException e) {
                        throw new RuntimeException(e);
                    }
                    editSession.close();
                    List<Cell> newCells = markAsOccupied(finalX, finalZ, paste.targetCellX, paste.targetCellZ, grid, finalDirection, paste.schematic.name);
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
            }, paste.schematic.area/*paste.schematic.area + runningDelay*/);
            return;
        } else if (paste.direction == Direction.N) {
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(Bukkit.getWorlds().get(0)), -1);
                    ClipboardHolder holder = new ClipboardHolder(clipboard);
                    holder.setTransform(new AffineTransform().rotateY(180));
                    Operation operation = holder.createPaste(editSession).to(BlockVector3.at((startX + ((paste.targetCellX + 1) * cellSize) - 1), yLvl + 1, (startZ + (paste.targetCellZ * -cellSize)) + 1 - cellSize)).ignoreAirBlocks(true).build();
                    try {
                        Operations.complete(operation);
                    } catch (WorldEditException e) {
                        throw new RuntimeException(e);
                    }
                    editSession.close();
                    List<Cell> newCells = markAsOccupied(finalX, finalZ, paste.targetCellX, paste.targetCellZ, grid, finalDirection, paste.schematic.name);
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
            }, paste.schematic.area/*paste.schematic.area + runningDelay*/);
            return;
        } else if (paste.direction == Direction.W) {
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(Bukkit.getWorlds().get(0)), -1);
                    ClipboardHolder holder = new ClipboardHolder(clipboard);
                    holder.setTransform(new AffineTransform().rotateY(270));
                    Operation operation = holder.createPaste(editSession).to(BlockVector3.at((startX + ((paste.targetCellX + 1) * cellSize) - 5), yLvl + 1, (startZ + (paste.targetCellZ * -cellSize) - 4))).ignoreAirBlocks(true).build();
                    try {
                        Operations.complete(operation);
                    } catch (WorldEditException e) {
                        throw new RuntimeException(e);
                    }
                    editSession.close();
                    List<Cell> newCells = markAsOccupied(finalX, finalZ, paste.targetCellX, paste.targetCellZ, grid, finalDirection, paste.schematic.name);
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
            }, paste.schematic.area/*paste.schematic.area + runningDelay*/);
        }
    }

    public District cycleToNextDistrict(District district) {
        if (district == District.FARM) {
            return District.ARISTOCRACY;
        } else if (district == District.PORT) {
            return District.SLUMS;
        } else if (district == District.ARISTOCRACY) {
            return District.PORT;
        } else if (district == District.SLUMS) {
            return  District.FARM;
        } else {
            return District.OUTSKIRTS;
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
            if (this.schemFitsInBounds(x, z, targetX, targetZ, grid, direction)) {
                Paste paste = new Paste(schematic, x, z, direction, targetX, targetZ);
                if (schematic.linkedSchematic == null) {
                    paste(paste, getCellsForDistrict(paste.schematic.district));
                    return;
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
                                            Operation operation = (new ClipboardHolder(clipboard)).createPaste(editSession).to(BlockVector3.at(startX + (linkedTargetX * cellSize), yLvl + 1, startZ + (linkedTargetZ * -cellSize))).ignoreAirBlocks(true).build();
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
                                            operation = (new ClipboardHolder(clipboard)).createPaste(editSession).to(BlockVector3.at(startX + (targetX * cellSize), yLvl + 1, startZ + (targetZ * -cellSize))).ignoreAirBlocks(true).build();
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
                                            Operation operation = holder.createPaste(editSession).to(BlockVector3.at(startX + (targetX * cellSize) + 4, yLvl + 1, (startZ + (targetZ * -cellSize)) + 5 - cellSize)).ignoreAirBlocks(true).build();
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
                                            Operation operation = holder.createPaste(editSession).to(BlockVector3.at((startX + ((targetX + 1) * cellSize) - 1), yLvl + 1, (startZ + (targetZ * -cellSize)) + 1 - cellSize)).ignoreAirBlocks(true).build();
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
                                            Operation operation = holder.createPaste(editSession).to(BlockVector3.at((startX + ((targetX + 1) * cellSize) - 5), yLvl + 1, (startZ + (targetZ * -cellSize) - 4))).ignoreAirBlocks(true).build();
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
            Cell cell = (Cell) remainingCells.get(0);
            Bukkit.getLogger().log(Level.INFO, "Got cell " + cell.x + "," + cell.z);
            if (cell.isOccupied || cell.isRoad) {
                remainingCells.remove(cell);
                generateRepeatables(grid, remainingCells);
                Bukkit.getLogger().log(Level.INFO, "Cell was occupied moving on...");
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
                List<Schematic> schematicList = getOmniSchematicsForDistrict(cell.district);
                int targetZ = cell.z;
                int targetX = cell.x;
                Iterator schemIter = schematicList.iterator();
                boolean pasted = false;
                do {
                    Schematic schematic = (Schematic) schemIter.next();
                    int x = schematic.xLength;
                    int z = schematic.zHeight;
                    List<Direction> randomDir = new ArrayList<>();
                    randomDir.addAll(potentialDirections);
                    Collections.shuffle(randomDir);
                    Iterator dirIter = randomDir.iterator();

                    do {
                        Direction oDirection = (Direction) dirIter.next();
                        if (this.schemFitsInBounds(x, z, targetX, targetZ, grid, oDirection)) {
                            Paste paste = new Paste(schematic, x, z, oDirection, targetX, targetZ);
                            pasteRepeatable(paste, remainingCells);
                            getOmniSchematicsForDistrict(paste.schematic.district).remove(paste.schematic);
                            pasted = true;

                        }
                    } while (dirIter.hasNext() && !pasted);
                } while (schemIter.hasNext() && !pasted);

            } else {
                if (getRepeatableSchematicsForDistrict(cell.district).size() < 1 || getRepeatableSchematicsForDistrict(cell.district).isEmpty()) {
                    localRepeatableRefresh(cell.district);
                }
                List<Schematic> schematicList = getRepeatableSchematicsForDistrict(cell.district);
                int targetZ = cell.z;
                int targetX = cell.x;
                Iterator schemIter = schematicList.iterator();
                boolean pass = false;
                do {

                    Schematic schematic = (Schematic) schemIter.next();
                    int x = schematic.xLength;
                    int z = schematic.zHeight;
                    if (direction == Direction.W || direction == Direction.E) {
                        x = schematic.zHeight;
                        z = schematic.xLength;
                    }
                    if (schemFitsInBounds(x, z, targetX, targetZ, grid, direction)) {
                        Paste paste = new Paste(schematic, x, z, direction, targetX, targetZ);
                        pasteRepeatable(paste, remainingCells);
                        pass = true;
                    }
                } while (schemIter.hasNext() && !pass);
            }
        } else {
            Bukkit.getLogger().log(Level.INFO, "Finished repeatable generation.");
        }

    }
    public void generateBuildingsHighway(Cell[][] grid, int count) throws DataException, IOException, WorldEditException, WorldEditException {
        Random r = new Random();
        List<List<Schematic>> schematicsByDistrict = new ArrayList<>();
        schematicsByDistrict.add(this.schemHandler.slumsSchematics);
        schematicsByDistrict.add(this.schemHandler.aristocracySchematics);
        schematicsByDistrict.add(this.schemHandler.farmSchematics);
        schematicsByDistrict.add(this.schemHandler.portSchematics);
        //schematicsByDistrict.add(this.schemHandler.outskirtsSchematics);
        List<Schematic> schematicList = schematicsByDistrict.get(count);
            List<Cell> cells = new ArrayList<>();
            if (schematicList.get(0).district == District.PORT) {
                cells = this.portCells;
            } else if (schematicList.get(0).district == District.SLUMS) {
                cells = this.slumsCells;
            } else if (schematicList.get(0).district == District.ARISTOCRACY) {
                cells = this.aristocracyCells;
            } else if (schematicList.get(0).district == District.FARM) {
                cells = this.farmCells;
            } //else if (schematicList.get(0).district == District.OUTSKIRTS) {
                //cells = this.outskirtsCells;
           // }


        /*for (Cell cell : cellsForBuildings) {
            List<Direction> nearbyRoads = nearbyRoadsOnly(cell.x, cell.z);
            Direction direction = null;
            if (nearbyRoads.size() > 1) {
                direction = nearbyRoads.get(r.nextInt(nearbyRoads.size()));
            } else if (nearbyRoads.size() == 1) {
                direction = nearbyRoads.get(0);
            }
            List<Schematic> schematicList = new ArrayList();
            if (cell.district == District.SLUMS) {
                schematicList.addAll(this.schemHandler.slumsSchematics);
            } else if (cell.district == District.ARISTOCRACY) {
                schematicList.addAll(this.schemHandler.aristocracySchematics);
            } else if (cell.district == District.FARM) {
                schematicList.addAll(this.schemHandler.farmSchematics);
            } else if (cell.district == District.PORT) {
                schematicList.addAll(this.schemHandler.portSchematics);
            }
            if (!schematicList.isEmpty()) {
                if (direction != null) {
                    boolean pass = false;
                    do {
                        if (schematicList.size() < 1) {
                            pass = true;
                        } else {
                            Schematic schematic = schematicList.get(r.nextInt(schematicList.size()));
                            int targetZ = cell.z;
                            int targetX = cell.x;
                            int x = schematic.xLength;
                            int z = schematic.zHeight;
                            if (direction == Direction.W || direction == Direction.E) {
                                x = schematic.zHeight;
                                z = schematic.xLength;
                            }
                            if (this.schemFitsInBounds(x, z, targetX, targetZ, grid, direction)) {
                                EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(Bukkit.getWorlds().get(0)), -1);
                                ClipboardFormat format = ClipboardFormats.findByFile(new File(schemHandler.schematicDirectory, schematic.fileName + ".schematic"));
                                ClipboardReader reader = format.getReader(new FileInputStream(new File(schemHandler.schematicDirectory, schematic.fileName + ".schematic")));
                                Clipboard clipboard = reader.read();

                                if (direction == Direction.S) {
                                    Operation operation = (new ClipboardHolder(clipboard)).createPaste(editSession).to(BlockVector3.at(startX + (targetX * cellSize), yLvl+1, startZ + (targetZ * -cellSize))).ignoreAirBlocks(true).build();
                                    Operations.complete(operation);
                                    editSession.close();
                                     markAsOccupied(x, z, targetX, targetZ, grid, direction, schematic.name);
                                    pass=true;
                                }else if (direction == Direction.E) {
                                    ClipboardHolder holder = new ClipboardHolder(clipboard);
                                    holder.setTransform(new AffineTransform().rotateY(90));
                                    Operation operation = holder.createPaste(editSession).to(BlockVector3.at(startX + (targetX * cellSize) + 4, yLvl+1, (startZ + (targetZ * -cellSize)) + 5 - cellSize)).ignoreAirBlocks(true).build();
                                    Operations.complete(operation);
                                    editSession.close();
                                    markAsOccupied(x, z, targetX, targetZ, grid, direction, schematic.name);
                                    pass=true;
                                } else if (direction == Direction.N) {
                                    ClipboardHolder holder = new ClipboardHolder(clipboard);
                                    holder.setTransform(new AffineTransform().rotateY(180));
                                    Operation operation = holder.createPaste(editSession).to(BlockVector3.at((startX + ((targetX + 1) * cellSize) - 1), yLvl+1, (startZ + (targetZ * -cellSize)) + 1 - cellSize)).ignoreAirBlocks(true).build();
                                    Operations.complete(operation);
                                    editSession.close();
                                    markAsOccupied(x, z, targetX, targetZ, grid, direction, schematic.name);
                                    pass=true;
                                } else if (direction == Direction.W) {
                                    ClipboardHolder holder = new ClipboardHolder(clipboard);
                                    holder.setTransform(new AffineTransform().rotateY(270));
                                    Operation operation = holder.createPaste(editSession).to(BlockVector3.at((startX + ((targetX + 1) * cellSize) - 5), yLvl+1, (startZ + (targetZ * -cellSize) - 4))).ignoreAirBlocks(true).build();
                                    Operations.complete(operation);
                                    editSession.close();
                                    markAsOccupied(x, z, targetX, targetZ, grid, direction, schematic.name);
                                    pass = true;
                                }
                                pass = true;
                            } else {

                                schematicList.remove(schematic);
                            }
                        }
                    }while(!pass);

                }
            } else {
                if (schematicList.isEmpty()) {
                }

            }

        }*/
    }


    public boolean schemFitsInBounds(int xSize, int zSize, int gridX, int gridZ, Cell[][] grid, Direction direction) {
        if (direction == Direction.S) {
            for (int x = 0; x < xSize; x++) {
                for (int z = 0; z < zSize; z++) {
                    if (((gridX + x) < this.size ) && ((gridZ + z) < this.size)) {
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
                    if (((gridX - x) >= 0 ) && ((gridZ + z) < this.size)) {
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
                    if (((gridX + x) < this.size ) && ((gridZ + z) < this.size)) {
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
                    if (((gridX - x) >= 0 ) && ((gridZ + z) < this.size)) {
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
                    if (((gridX + x) < this.size ) && ((gridZ + z) < this.size)) {
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
                    if (((gridX - x) >= 0 ) && ((gridZ + z) < this.size)) {
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
            Collections.shuffle(localFarmRepeatable);
        } else if (district == District.PORT) {
            localPortRepeatable.addAll(plugin.schematicHandler.portSchematicsRepeatable);
            Collections.shuffle(localPortRepeatable);
        } else if (district == District.ARISTOCRACY) {
            localAristocracyRepeatable.addAll(plugin.schematicHandler.aristocracySchematicsRepeatable);
            Collections.shuffle(localAristocracyRepeatable);
        } else if (district == District.SLUMS) {
            localSlumsRepeatable.addAll(plugin.schematicHandler.slumsSchematicsRepeatable);
            Collections.shuffle(localSlumsRepeatable);
        } else {
            localOutskirtsRepeatable.addAll(plugin.schematicHandler.outskirtsSchematicsRepeatable);
            Collections.shuffle(localOutskirtsRepeatable);
        }
    }

    public void localOmniRefresh(District district) {
        if (district == District.FARM) {
            localFarmOmni.addAll(plugin.schematicHandler.farmSchematicsOmni);
            Collections.shuffle(localFarmOmni);
        } else if (district == District.PORT) {
            localPortOmni.addAll(plugin.schematicHandler.portSchematicsOmni);
            Collections.shuffle(localPortOmni);
        } else if (district == District.ARISTOCRACY) {
            localAristocracyOmni.addAll(plugin.schematicHandler.aristocracySchematicsOmni);
            Collections.shuffle(localAristocracyOmni);
        } else if (district == District.SLUMS) {
            localSlumsOmni.addAll(plugin.schematicHandler.slumsSchematicsOmni);
            Collections.shuffle(localSlumsOmni);
        } else {
            localOutskirtsOmni.addAll(plugin.schematicHandler.outskirtsSchematicsOmni);
            Collections.shuffle(localOutskirtsOmni);
        }
    }


}
