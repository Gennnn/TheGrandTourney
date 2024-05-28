package me.genn.thegrandtourney.grid;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.joml.SimplexNoise;


public class Grid {
    public float generationProgress = 0.0f;
    int riverCellsSize = 0;
    int riverCellsGenerated = 0;
    int roadCellsSize = 0;
    int roadCellsGenerated = 0;
    int nonRepeatableSchematicsSize = 0;
    int nonRepeatableSchematicsGenerated = 0;
    int repeatableSchematicsSize = 0;
    int repeatableSchematicsGenerated = 0;
    public String taskBeingPerformed = "Preparing to generate...";
    public int totalTasks = 7;
    public int tasksCompleted = 0;
    public int blocksPerCell;


    public void setRiverNoiseScale(float riverNoiseScale) {
        this.riverNoiseScale = riverNoiseScale;
    }

    float riverNoiseScale = 0.07f;
    float sideRoadNoiseScale = 0.09f;
    float backRoadNoiseScale = 0.11f;
    int sideRoadsPerDistrict = 4;
    int backRoadsPerDistrict = 10;
    public int riverNumber = 5;

    public void setSize(int size) {
        this.size = size;
    }

    public int size = 200;
    public Cell[][] grid;
    public AStarNode[][] riverNodeMap;
    public AStarNode[][] sideRoadNodeMap;
    public AStarNode[][] backRoadNodeMap;
    public float[][] riverNoiseMap;
    public float[][] sideRoadNoiseMap;
    public float[][] backRoadNoiseMap;

    public void setyLvl(double yLvl) {
        this.yLvl = yLvl;
    }

    public void setStartX(double startX) {
        this.startX = startX;
    }

    public void setStartZ(double startZ) {
        this.startZ = startZ;
    }

    public double yLvl = 64.0D;
    public double startX = 12.0D;
    public double startZ = -13.0D;
    public int cellSize = 5;
    public byte ADark = 1;
    public byte ALight = 4;
    public byte SDark = 7;
    public byte SLight = 8;
    public byte FDark = 13;
    public byte FLight = 5;
    public byte PDark = 9;
    public byte PLight = 3;
    public List<Cell> highwayCells = new ArrayList<>();
    public boolean districtCheck = false;
    public int numBackRoads = 5;
    SchematicHandler schemHandler;
    List<Direction> potentialDirections = Arrays.asList(Direction.N, Direction.S, Direction.E, Direction.W);
    public List<Cell> portCells;
    public List<Cell> farmCells;
    public List<Cell> aristocracyCells;
    public List<Cell> slumsCells;
    public List<Cell> outskirtsCells;
    public List<Cell> roadCells = new ArrayList<>();
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

    public void setOceanXMin(int oceanXMin) {
        this.oceanXMin = oceanXMin;
    }

    public void setOceanXMax(int oceanXMax) {
        this.oceanXMax = oceanXMax;
    }

    public void setOceanZMin(int oceanZMin) {
        this.oceanZMin = oceanZMin;
    }

    public void setOceanZMax(int oceanZMax) {
        this.oceanZMax = oceanZMax;
    }

    public int oceanXMin;
    public int oceanXMax;
    public int oceanZMin;
    public int oceanZMax;
    long lastGeneration;
    public boolean generationCompleted = false;
    List<int[]> blacklistedCells = new ArrayList<>();
    List<Cell> riverCells = new ArrayList<>();
    int riverPasses = 0;
    boolean riverPathing = false;

    public void setBlacklistedCellsList(List<String> blacklistedCellsList) {
        this.blacklistedCellsList = blacklistedCellsList;
    }

    List<String> blacklistedCellsList = new ArrayList<>();


    public Grid(TGT plugin) {
        this.plugin = plugin;
        this.r = new Random();
        this.schematicDetailsDirectory = new File(plugin.getDataFolder(), "schematic-contents");
    }

    public void setSchematicHandler(SchematicHandler schemHandler) {
        this.schemHandler = schemHandler;
    }

    public void setSideRoadsPerDistrict(int num) {
        this.sideRoadsPerDistrict = num;
    }
    public void setBackRoadsPerDistrict(int num) {
        this.backRoadsPerDistrict = num;
    }
    public void setBackRoadNoiseScale(float num) {
        this.backRoadNoiseScale = num;
    }
    public void setSideRoadNoiseScale(float num) {
        this.sideRoadNoiseScale = num;
    }
    public void setRiverCount(int num) {
        this.riverNumber = num;
    }

    public void initialize() throws DataException, WorldEditException, IOException {
        generationProgress = 0.0f;
        taskBeingPerformed = "Generating grid...";
        grid = new Cell[size][size];

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
        for (int z = 0; z < size; z++) {
            for (int x = 0; x<size; x++) {
                Cell cell = new Cell();
                cell.isRoad = false;
                cell.isOccupied = false;
                cell.x = x;
                cell.z = z;
                grid[x][z] = cell;
            }
        }

        generationProgress = 0.10f;
        taskBeingPerformed = "Creating noise and node maps...";
        riverNoiseMap = new float[size/2][size/2];
        sideRoadNoiseMap = new float[size][size];
        backRoadNoiseMap = new float[size][size];
        riverNodeMap = new AStarNode[size/2][size/2];
        sideRoadNodeMap = new AStarNode[size][size];
        backRoadNodeMap = new AStarNode[size][size];
        generationProgress = 0.20f;
        float xOffset = r.nextFloat(-10000f,10000f);
        float zOffset = r.nextFloat(-10000f,10000f);
        for (int x = 0; x < size/2; x++) {
            for (int z = 0; z < size/2; z++) {
                float noiseValue = Math.abs((float) SimplexNoise.noise(x * riverNoiseScale + xOffset, z * riverNoiseScale + zOffset));
                riverNoiseMap[x][z] = noiseValue;
            }
        }
        generationProgress = 0.3f;
        xOffset = r.nextFloat(-10000f,10000f);
        zOffset = r.nextFloat(-10000f,10000f);
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                float noiseValue = Math.abs((float) SimplexNoise.noise(x * sideRoadNoiseScale + xOffset, z * sideRoadNoiseScale + zOffset));
                sideRoadNoiseMap[x][z] = noiseValue;
            }
        }
        generationProgress = 0.4f;
        xOffset = r.nextFloat(-10000f,10000f);
        zOffset = r.nextFloat(-10000f,10000f);
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                float noiseValue = Math.abs((float) SimplexNoise.noise(x * backRoadNoiseScale + xOffset, z * backRoadNoiseScale + zOffset));
                backRoadNoiseMap[x][z] = noiseValue;
            }
        }

        generationProgress = 0.5f;
        for (int x = 0; x < size/2f; x++) {
            for (int z = 0; z < size/2f; z++) {
                List<Cell> cells = new ArrayList<>();
                cells.add(grid[2*x][2*z]);
                cells.add(grid[(2*x)+1][2*z]);
                cells.add(grid[(2*x)][(2*z)+1]);
                cells.add(grid[(2*x)+1][(2*z)+1]);
                //Bukkit.broadcastMessage("NOISE VALUE= " + riverNoiseMap[x][z]);

                riverNodeMap[x][z] = new AStarNode(cells,x,z,riverNoiseMap[x][z] < 0.5f);
                /*if (riverNodeMap[x][z].navigatable) {
                    counter++;
                }*/
            }
        }
        generationProgress = 0.6f;

        //Bukkit.broadcastMessage("Number of valid aStar cells " + counter + "/" + (riverNodeMap.length*riverNodeMap[0].length));
        for (int x = 0; x< size; x++) {
            for (int z = 0; z<size; z++) {
                List<Cell> cells = new ArrayList<>();
                cells.add(grid[x][z]);
                AStarNode node = new AStarNode(cells,x,z,sideRoadNoiseMap[x][z] < 0.65f);
                sideRoadNodeMap[x][z] = node;
            }
        }
        generationProgress = 0.7f;
        for (int x = 0; x< size; x++) {
            for (int z = 0; z<size; z++) {
                List<Cell> cells = new ArrayList<>();
                cells.add(grid[x][z]);
                AStarNode node = new AStarNode(cells,x,z,backRoadNoiseMap[x][z] < 0.5f);
                backRoadNodeMap[x][z] = node;
            }
        }
        generationProgress = 0.8f;
        this.assignDistricts(grid);
        generationProgress = 0.9f;
        taskBeingPerformed = "Mapping restrictions...";
        this.blacklistedCells.addAll(this.registerBlackList(this.blacklistedCellsList));
        for (int[] coords : this.blacklistedCells) {
            Cell cell = grid[coords[0]][coords[1]];
            cell.isOccupied = true;
            int[] riverNode = getRiverNodeContainingCell(cell);
            if (riverNode != null) {
                riverNodeMap[riverNode[0]][riverNode[1]].navigatable = false;
            }
            backRoadNodeMap[cell.x][cell.z].navigatable = false;
            sideRoadNodeMap[cell.x][cell.z].navigatable = false;
        }
        tasksCompleted++;
        taskBeingPerformed = "Mapping rivers...";
        //this.aStarCheck();
        new BukkitRunnable() {

            @Override
            public void run() {
                if (riverPasses >= riverNumber) {
                    taskBeingPerformed = "Mapping roads...";
                    tasksCompleted++;
                    generationProgress = 0.0f;
                    //Bukkit.broadcastMessage("River cells " + riverCells.size());
                    Grid.this.assignHighways();
                    generationProgress = 0.5f;
                    Grid.this.assignSideRoads();
                    tasksCompleted++;
                    generationProgress = 0.0f;
                    taskBeingPerformed = "Generating rivers...";
                    riverCellsSize = riverCells.size();
                    /*Grid.this.generateSideRoads(grid);
                    Grid.this.generateBackRoads(grid);*/
                    try {
                        Grid.this.generateRivers(Grid.this.riverCells.get(r.nextInt(riverCells.size())));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    this.cancel();
                    return;
                } else if (!riverPathing) {
                    int startX = 0;
                    int startZ = 0;
                    int goalX = 0;
                    int goalZ = 0;
                    riverPathing = true;
                    //Bukkit.broadcastMessage("Pathing true");
                    do {
                        startX = 0;
                        startZ = 0;
                        goalX = 0;
                        goalZ = 0;
                        int startCoordinate = r.nextInt((size/2));
                        int endCoordinate = r.nextInt(size/2);
                        if (r.nextInt(2) == 1) {
                            startX = startCoordinate;
                            if (r.nextInt(2) == 1) {
                                startZ = (size/2)-1;
                            }
                            if (r.nextInt(2) == 1){
                                goalX = endCoordinate;
                                if (r.nextInt(2)==1) {
                                    goalZ = (size/2)-1;
                                }

                            } else {
                                goalZ = endCoordinate;
                                if (r.nextInt(2)==1) {
                                    goalX = (size/2)-1;
                                }

                            }
                        } else {
                            startZ = startCoordinate;
                            if (r.nextInt(2) == 1) {
                                startX = (size/2)-1;
                            }
                            if (r.nextInt(2) == 1){
                                goalZ = endCoordinate;
                                if (r.nextInt(2) == 1) {
                                    goalX = (size/2)-1;
                                }
                            } else {
                                goalX = endCoordinate;
                                if (r.nextInt(2)==1) {
                                    goalZ = (size/2)-1;
                                }

                            }
                        }

                        //Bukkit.broadcastMessage("Trying start " + startX + "," + startZ + " and goal " + goalX + "," + goalZ);
                    } while (!riverNodeMap[startX][startZ].navigatable || !riverNodeMap[goalX][goalZ].navigatable || calculateDistance(new int[]{startX,startZ},new int[]{goalX,goalZ}) < 15);

                    List<AStarNode> riverNodes = performRiverMapping(startX,startZ,goalX,goalZ);
                    riverPasses++;
                    generationProgress = (float) (1.0/riverPasses);
                    if (riverNodes != null && riverNodes.size() > 0) {
                        for (AStarNode node : riverNodes) {
                            //Bukkit.broadcastMessage("Adding ASTARNODE " + node.x + "," + node.z);
                            for (Cell cell : node.cells) {
                                cell.isRiver = true;
                                cell.isOccupied = true;
                                if (!riverCells.contains(cell)) {
                                    riverCells.add(cell);
                                }

                            }
                        }
                        //Bukkit.broadcastMessage("Added batch of " + riverNodes.size() + " river nodes");
                    } else {
                        //Bukkit.broadcastMessage("Batch was empty");
                    }



                }

            }
        }.runTaskTimer(plugin, 0L, 10L);


        //Grid.this.generateBackRoads(grid);


        /*this.generateHighways(grid);
        this.generateSideRoads(grid);
        this.generateBackRoads(grid);
        this.generateRivers(this.riverCells.get(r.nextInt(riverCells.size())));*/
    }

    public float calculateDistance(int[] p1, int[] p2) {
        return (float) Math.sqrt(Math.pow(p1[0] - p2[0],2) + Math.pow(p1[1] - p2[1],2));
    }


    public void resetRiverMap() {
        for (int x = 0; x < size/2; x++) {
            for (int z = 0; z< size/2; z++) {
                riverNodeMap[x][z].reset();
            }
        }
    }

    public int[] getRiverNodeContainingCell(Cell cell) {
        for (int x = 0; x < size/2 ; x++) {
            for (int z = 0; z < size/2; z++ ){
                if (riverNodeMap[x][z].cells.contains(cell)) {
                   return new int[]{x,z};
                }
            }
        }
        return null;
    }
    public List<AStarNode> performRiverMapping(int startX, int startZ, int goalX, int goalZ) {
        resetRiverMap();
        AStarNode startNode = new AStarNode(riverNodeMap[startX][startZ].cells, startX, startZ, riverNodeMap[startX][startZ].navigatable);
        /*for (Cell cell : startNode.cells) {
            fillCellWithBlock(Material.LIME_WOOL, cell.x,cell.z);
        }*/
        AStarNode goalNode = new AStarNode(riverNodeMap[goalX][goalZ].cells, goalX, goalZ, riverNodeMap[goalX][goalZ].navigatable);
        /*for (Cell cell : goalNode.cells) {
            fillCellWithBlock(Material.YELLOW_WOOL, cell.x,cell.z);
        }*/
        ArrayList<AStarNode> openList = new ArrayList<>();
        List<AStarNode> closedList = new ArrayList<>();

        int outerIterations = 0;
        int maxIterations = Math.floorDiv((riverNodeMap.length * riverNodeMap[0].length),2);
        int[] xDirections = {1,0,-1,0};
        int[] zDirections = {0,1,0,-1};
        openList.add(startNode);
        AStarNode currentNode = null;
        while (openList.size() > 0) {
            outerIterations++;

            if (outerIterations > maxIterations) {
                riverPathing = false;
                return getReturnPath(currentNode);
            }
            openList.sort(new Comparator<AStarNode>() {
                @Override
                public int compare(AStarNode o1, AStarNode o2) {
                    return Float.compare(o1.f,o2.f);
                }
            });

            currentNode = openList.remove(0);

            if (openList.size() > 0) {
                for (int i = 0; i < openList.size(); i++) {
                }

            }

            closedList.add(currentNode);
            /*for (Cell cell : startNode.cells) {
                if (!(startNode.cells.contains(cell)) && !(goalNode.cells.contains(cell))) {
                    fillCellWithBlock(Material.BLUE_WOOL, cell.x,cell.z);
                }
            }*/
            //Bukkit.broadcastMessage("Added " + currentNode.x + "," + currentNode.z + " to closed list");
            if (currentNode.equals(goalNode)) {
                if (openList.get(0) != null && openList.get(0).f >= currentNode.f) {
                    riverPathing = false;
                    return getReturnPath(currentNode);
                }
            }
            for (int i = 0; i < 4; i++) {
                int[] position = new int[]{currentNode.x + xDirections[i], currentNode.z + zDirections[i]};
                if (position[0] > riverNodeMap.length-1 || position[0] < 0 || position[1] > riverNodeMap[0].length-1 || position[1] < 0) {
                    continue;
                }
                AStarNode newNode = new AStarNode(riverNodeMap[position[0]][position[1]].cells, position[0],position[1], riverNodeMap[position[0]][position[1]].navigatable, currentNode);

                if (!newNode.navigatable) {
                    continue;
                }
                if (closedList.contains(newNode)) {
                    continue;
                }
                //if (riverNodeMap[newX][newZ].parent != null) continue;
                AStarNode childNode = newNode;
                childNode.g = (currentNode.g + calculateDistance(new int[]{childNode.x, childNode.z},new int[]{currentNode.x, currentNode.z}));
                childNode.h = getEuclideanDistance(new int[]{childNode.x, childNode.z},new int[]{goalNode.x,goalNode.z});
                childNode.f = childNode.g + childNode.h;

                if (openList.contains(childNode)) {
                    int index = openList.indexOf(childNode);
                    if (childNode.g < openList.get(index).g) {
                        openList.set(index, childNode);
                    }
                } else {
                    openList.add(childNode);
                }
            }


        }
        this.riverPathing = false;
        return null;
    }

    public List<AStarNode> performSideRoadMapping(int startX, int startZ, int goalX, int goalZ) {

        AStarNode startNode = new AStarNode(sideRoadNodeMap[startX][startZ].cells, startX, startZ, sideRoadNodeMap[startX][startZ].navigatable);
        /*for (Cell cell : startNode.cells) {
            fillCellWithBlock(Material.LIME_WOOL, cell.x,cell.z);
        }*/
        AStarNode goalNode = new AStarNode(sideRoadNodeMap[goalX][goalZ].cells, goalX, goalZ, sideRoadNodeMap[goalX][goalZ].navigatable);
        /*for (Cell cell : goalNode.cells) {
            fillCellWithBlock(Material.YELLOW_WOOL, cell.x,cell.z);
        }*/
        ArrayList<AStarNode> openList = new ArrayList<>();
        List<AStarNode> closedList = new ArrayList<>();

        int outerIterations = 0;
        int maxIterations = Math.floorDiv((sideRoadNodeMap.length * sideRoadNodeMap[0].length),2);
        int[] xDirections = {1,0,-1,0};
        int[] zDirections = {0,1,0,-1};
        openList.add(startNode);
        AStarNode currentNode = null;
        while (openList.size() > 0) {
            outerIterations++;

            if (outerIterations > maxIterations) {
                return getReturnPath(currentNode);
            }
            openList.sort(new Comparator<AStarNode>() {
                @Override
                public int compare(AStarNode o1, AStarNode o2) {
                    return Float.compare(o1.f,o2.f);
                }
            });

            currentNode = openList.remove(0);

            if (openList.size() > 0) {
                for (int i = 0; i < openList.size(); i++) {
                }

            }

            closedList.add(currentNode);
            /*for (Cell cell : startNode.cells) {
                if (!(startNode.cells.contains(cell)) && !(goalNode.cells.contains(cell))) {
                    fillCellWithBlock(Material.BLUE_WOOL, cell.x,cell.z);
                }
            }*/
            //Bukkit.broadcastMessage("Added " + currentNode.x + "," + currentNode.z + " to closed list");
            if (currentNode.equals(goalNode)) {
                if (openList.get(0) != null && openList.get(0).f >= currentNode.f) {
                    return getReturnPath(currentNode);
                }
            }
            for (int i = 0; i < 4; i++) {
                int[] position = new int[]{currentNode.x + xDirections[i], currentNode.z + zDirections[i]};
                if (position[0] > sideRoadNodeMap.length-1 || position[0] < 0 || position[1] > sideRoadNodeMap[0].length-1 || position[1] < 0) {
                    continue;
                }
                AStarNode newNode = new AStarNode(sideRoadNodeMap[position[0]][position[1]].cells, position[0],position[1], sideRoadNodeMap[position[0]][position[1]].navigatable, currentNode);

                if (!newNode.navigatable) {
                    continue;
                }
                if (closedList.contains(newNode)) {
                    continue;
                }
                //if (riverNodeMap[newX][newZ].parent != null) continue;
                AStarNode childNode = newNode;
                childNode.g = (currentNode.g + calculateDistance(new int[]{childNode.x, childNode.z},new int[]{currentNode.x, currentNode.z}));
                childNode.h = getEuclideanDistance(new int[]{childNode.x, childNode.z},new int[]{goalNode.x,goalNode.z});
                childNode.f = childNode.g + childNode.h;

                if (openList.contains(childNode)) {
                    int index = openList.indexOf(childNode);
                    if (childNode.g < openList.get(index).g) {
                        openList.set(index, childNode);
                    }
                } else {
                    openList.add(childNode);
                }
            }


        }
        return null;
    }

    public List<AStarNode> performSideRoadMappingOutskirts(int startX, int startZ, int goalX, int goalZ) {

        AStarNode startNode = new AStarNode(sideRoadNodeMap[startX][startZ].cells, startX, startZ, sideRoadNodeMap[startX][startZ].navigatable);
        /*for (Cell cell : startNode.cells) {
            fillCellWithBlock(Material.LIME_WOOL, cell.x,cell.z);
        }*/
        AStarNode goalNode = new AStarNode(sideRoadNodeMap[goalX][goalZ].cells, goalX, goalZ, sideRoadNodeMap[goalX][goalZ].navigatable);
        /*for (Cell cell : goalNode.cells) {
            fillCellWithBlock(Material.YELLOW_WOOL, cell.x,cell.z);
        }*/
        ArrayList<AStarNode> openList = new ArrayList<>();
        List<AStarNode> closedList = new ArrayList<>();

        int outerIterations = 0;
        int maxIterations = Math.floorDiv((sideRoadNodeMap.length * sideRoadNodeMap[0].length),2);
        int[] xDirections = {1,0,-1,0};
        int[] zDirections = {0,1,0,-1};
        openList.add(startNode);
        AStarNode currentNode = null;
        while (openList.size() > 0) {
            outerIterations++;

            if (outerIterations > maxIterations) {
                return getReturnPath(currentNode);
            }
            openList.sort(new Comparator<AStarNode>() {
                @Override
                public int compare(AStarNode o1, AStarNode o2) {
                    return Float.compare(o1.f,o2.f);
                }
            });

            currentNode = openList.remove(0);
            closedList.add(currentNode);
            /*for (Cell cell : startNode.cells) {
                if (!(startNode.cells.contains(cell)) && !(goalNode.cells.contains(cell))) {
                    fillCellWithBlock(Material.BLUE_WOOL, cell.x,cell.z);
                }
            }*/
            //Bukkit.broadcastMessage("Added " + currentNode.x + "," + currentNode.z + " to closed list");
            if (currentNode.equals(goalNode)) {
                if (openList.get(0) != null && openList.get(0).f >= currentNode.f) {
                    return getReturnPath(currentNode);
                }
            } else if (grid[currentNode.x][currentNode.z].isRoad && grid[currentNode.x][currentNode.z].roadTier == RoadTier.HIGHWAY) {
                return getReturnPath(currentNode.parent);
            }
            for (int i = 0; i < 4; i++) {
                int[] position = new int[]{currentNode.x + xDirections[i], currentNode.z + zDirections[i]};
                if (position[0] > sideRoadNodeMap.length-1 || position[0] < 0 || position[1] > sideRoadNodeMap[0].length-1 || position[1] < 0) {
                    continue;
                }
                AStarNode newNode = new AStarNode(sideRoadNodeMap[position[0]][position[1]].cells, position[0],position[1], sideRoadNodeMap[position[0]][position[1]].navigatable, currentNode);

                if (!newNode.navigatable) {
                    continue;
                }
                if (closedList.contains(newNode)) {
                    continue;
                }
                if (grid[newNode.x][newNode.z].district != District.OUTSKIRTS) {
                    continue;
                }
                //if (riverNodeMap[newX][newZ].parent != null) continue;
                AStarNode childNode = newNode;
                childNode.g = (currentNode.g + calculateDistance(new int[]{childNode.x, childNode.z},new int[]{currentNode.x, currentNode.z}));
                childNode.h = getEuclideanDistance(new int[]{childNode.x, childNode.z},new int[]{goalNode.x,goalNode.z});
                childNode.f = childNode.g + childNode.h;

                if (openList.contains(childNode)) {
                    int index = openList.indexOf(childNode);
                    if (childNode.g < openList.get(index).g) {
                        openList.set(index, childNode);
                    }
                } else {
                    openList.add(childNode);
                }
            }


        }
        return null;
    }

    public float getEuclideanDistance(int[] p1, int[] p2) {
        BigDecimal bd = new BigDecimal(Double.toString(this.calculateDistance(p1,p2)));
        bd = bd.setScale(3, RoundingMode.HALF_UP);
        return bd.floatValue();
    }



    public List<AStarNode> getReturnPath(AStarNode startNode) {
        List<AStarNode> path = new ArrayList<>();
        AStarNode current = startNode;
        while (!(current.parent == null)) {
            //Bukkit.broadcastMessage("Adding node " + current.x + "," + current.z);
            path.add(current);
            current = current.parent;
        }
        path.add(current);
        Collections.reverse(path);
        return path;
    }

    /*public void performRiverMapping(int startX, int startZ, int goalX, int goalZ) {
        this.riverPathing = true;
        AStarNode startNode = riverNodeMap[startX][startZ];
        AStarNode goalNode = riverNodeMap[goalX][goalZ];
        List<AStarNode> openList = new ArrayList<>();
        List<AStarNode> closedList = new ArrayList<>();
        List<AStarNode> retList = new ArrayList<>();
        openList.add(startNode);
        final int[] runningDelay = {1};
        boolean[] pass = {false};
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (int j = 0; j < 100; j++) {

                        if (openList.size() > 0) {
                            AStarNode currentNode = openList.get(0);
                            int currentIndex = 0;
                            for (AStarNode iterNode : openList) {
                                if (iterNode.f < currentNode.f) {
                                    currentNode = iterNode;
                                    currentIndex = openList.indexOf(iterNode);
                                }
                            }
                            HeapopenList.remove(currentIndex);
                            closedList.add(currentNode);
                            if (currentNode.equals(goalNode)) {
                                List<AStarNode> path = new ArrayList<>();
                                AStarNode current = currentNode;
                                while (!(current.parent == null)) {
                                    path.add(current);
                                    current = riverNodeMap[current.parent[0]][current.parent[1]];
                                }
                                Collections.reverse(path);
                                retList.addAll(path);
                                pass[0] = true;
                                this.cancel();
                                return;
                            }
                            List<AStarNode> children = new ArrayList<>();
                            int[] xDirections = {1,0,-1,0};
                            int[] zDirections = {0,1,0,-1};
                            for (int i = 0; i < 4; i++ ){
                                int x = xDirections[i];
                                int z = zDirections[i];
                                int newX = currentNode.x + x;
                                int newZ = currentNode.z + z;
                                if (newX > riverNodeMap.length-1 || newX < 0 || newZ > riverNodeMap[0].length-1 || newZ < 0) continue;
                                if (!riverNodeMap[newX][newZ].navigatable) continue;
                                AStarNode newNode = riverNodeMap[newX][newZ];
                                newNode.parent = new int[]{currentNode.x, currentNode.z};
                                children.add(newNode);
                            }
                            //Bukkit.broadcastMessage("Node " + currentNode.x + "," + currentNode.z +  " has " + children.size() + " children");
                            for (AStarNode childNode : children) {
                                if (AStarNode.contains(closedList,childNode)) {
                                    continue;
                                }
                                childNode.g = currentNode.g+1;
                                childNode.h = Math.abs(childNode.x - goalNode.x) + Math.abs(childNode.z - goalNode.z);
                                childNode.f = childNode.g + childNode.h;
                                if (AStarNode.contains(openList, childNode)) {
                                    int childIndex = openList.indexOf(childNode);
                                    if (childNode.g > openList.get(childIndex).g) {
                                        openList.set(childIndex, childNode);
                                    }
                                }
                                openList.add(childNode);
                                //runningDelay[0]++;
                                //Bukkit.broadcastMessage("Added " + childNode.x + "," + childNode.z +  " to openlist");
                            }

                        } else {
                            pass[0] = true;
                            this.cancel();
                        }

                    }

                }
            }.runTaskTimer(plugin, 0L, 1L);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (pass[0]) {
                    if (retList.size() > 0) {
                        Bukkit.broadcastMessage("Added to retlist");
                        Grid.this.pathedRiverNodes.add(retList);
                    }
                    this.cancel();
                    riverPathing = false;
                    riverPasses++;
                    return;
                } else {
                    Bukkit.broadcastMessage("Still pathing...");
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }*/
    public void aStarCheck() {
        long[] runningDelay = {0};
        for (int x = 0; x < size/2f; x++) {
            int finalX = x;
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (int z = 0; z < size/2f; z++) {
                        if (riverNodeMap[finalX][z].navigatable) {
                            for (Cell cell : riverNodeMap[finalX][z].cells) {
                                fillCellWithBlock(Material.GREEN_WOOL, cell.x, cell.z);
                            }
                        } else {
                            for (Cell cell : riverNodeMap[finalX][z].cells) {
                                fillCellWithBlock(Material.RED_WOOL, cell.x, cell.z);
                            }
                        }

                    }
                }
            }.runTaskLater(plugin, runningDelay[0]);
            runningDelay[0]+=2;

        }
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

    public void assignHighways() {
        for (int x = 0; x < size; x++) {
            for (int z = (size/2)-1; z < (size/2)+1; z++) {
                grid[x][z].isRoad = true;
                grid[x][z].isOccupied = true;
                grid[x][z].roadTier = RoadTier.HIGHWAY;
                this.roadCells.add(grid[x][z]);
            }
        }
        for (int x = (size/2)-1; x < (size/2)+1; x++) {
            for (int z = 0; z < size; z++) {
                grid[x][z].isRoad = true;
                grid[x][z].isOccupied = true;
                grid[x][z].roadTier = RoadTier.HIGHWAY;
                this.roadCells.add(grid[x][z]);
            }
        }
    }
    public void generateRivers(Cell cell) throws IOException {
        List<Direction> nearbyRivers = nearbyRiversOnly(cell.x,cell.z);
        Direction pasteDir = Direction.N;
        Schematic schematic;
        List<Schematic> schemList = new ArrayList<>();
        if (nearbyRivers.size() == 0) {
            this.riverCells.remove(cell);
            this.riverCellsGenerated++;
            this.generationProgress = (float)riverCellsGenerated / (float)riverCellsSize;
            this.generateRivers(this.riverCells.get(r.nextInt(this.riverCells.size())));
            return;
        }
        if (nearbyRivers.size() == 1) {
            schemList.addAll(this.schemHandler.getRiversWithQualities(cell.district,RoadType.UNI));
            if (nearbyRivers.contains(Direction.E)) {
                pasteDir = Direction.E;
            } else if (nearbyRivers.contains(Direction.S)) {
                pasteDir = Direction.S;
            } else if (nearbyRivers.contains(Direction.W)) {
                pasteDir = Direction.W;
            }
        } else if (nearbyRivers.size() == 2) {
            if (nearbyRivers.contains(Direction.N) && nearbyRivers.contains(Direction.S)) {
                schemList.addAll(this.schemHandler.getRiversWithQualities(cell.district,RoadType.STRAIGHT));
                if (r.nextInt(2) == 1) {
                    pasteDir = Direction.S;
                } else {
                    pasteDir = Direction.N;
                }
            } else if (nearbyRivers.contains(Direction.E) && nearbyRivers.contains(Direction.W)) {
                schemList.addAll(this.schemHandler.getRiversWithQualities(cell.district,RoadType.STRAIGHT));
                if (r.nextInt(2) == 1) {
                    pasteDir = Direction.E;
                } else {
                    pasteDir = Direction.W;
                }
            } else {
                schemList.addAll(this.schemHandler.getRiversWithQualities(cell.district,RoadType.CURVE));
                if (nearbyRivers.contains(Direction.N) && nearbyRivers.contains(Direction.W)) {
                    pasteDir = Direction.W;
                } else if (nearbyRivers.contains(Direction.S) && nearbyRivers.contains(Direction.E)) {
                    pasteDir = Direction.E;
                } else if (nearbyRivers.contains(Direction.S) && nearbyRivers.contains(Direction.W)) {
                    pasteDir = Direction.S;
                }
            }
        } else if (nearbyRivers.size() == 3) {
            schemList.addAll(this.schemHandler.getRiversWithQualities(cell.district,RoadType.TRI));
            if (nearbyRivers.contains(Direction.N) && nearbyRivers.contains(Direction.E) && nearbyRivers.contains(Direction.S)) {
                pasteDir = Direction.E;
            } else if (nearbyRivers.contains(Direction.W) && nearbyRivers.contains(Direction.E) && nearbyRivers.contains(Direction.S)) {
                pasteDir = Direction.S;
            } else if (nearbyRivers.contains(Direction.N) && nearbyRivers.contains(Direction.W) && nearbyRivers.contains(Direction.S)) {
                pasteDir = Direction.W;
            }
        } else if (nearbyRivers.size() == 4) {
            schemList.addAll(this.schemHandler.getRiversWithQualities(cell.district,RoadType.QUAD));
            int rInt = r.nextInt(4);
            if (rInt == 1) {
                pasteDir = Direction.E;
            } else if (rInt == 2) {
                pasteDir = Direction.S;
            } else if (rInt == 3) {
                pasteDir = Direction.W;
            }
        }
        schematic = schemList.get(r.nextInt(schemList.size()));
        pasteRiverSchematic(cell, pasteDir, schematic);
    }
    public void generateRoads(Cell cell) throws IOException {
            List<Direction> nearbyRoads = nearbyRoadsOnly(cell.x,cell.z);
            Direction pasteDir = Direction.N;
            Schematic schematic;
            List<Schematic> schemList = new ArrayList<>();
            if (nearbyRoads.size() == 0) {
                this.roadCells.remove(cell);
                roadCellsGenerated++;
                generationProgress = (float)roadCellsGenerated/(float)roadCellsSize;
                this.generateRoads(this.roadCells.get(r.nextInt(this.roadCells.size())));
                return;
            }
            if (nearbyRoads.size() == 1) {
                schemList.addAll(this.schemHandler.getRoadsWithQualities(cell.district,cell.roadTier,RoadType.UNI));
                if (nearbyRoads.contains(Direction.E)) {
                    pasteDir = Direction.E;
                } else if (nearbyRoads.contains(Direction.S)) {
                    pasteDir = Direction.S;
                } else if (nearbyRoads.contains(Direction.W)) {
                    pasteDir = Direction.W;
                }
            } else if (nearbyRoads.size() == 2) {
                if (nearbyRoads.contains(Direction.N) && nearbyRoads.contains(Direction.S)) {
                    schemList.addAll(this.schemHandler.getRoadsWithQualities(cell.district,cell.roadTier,RoadType.STRAIGHT));
                    if (r.nextInt(2) == 1) {
                        pasteDir = Direction.S;
                    } else {
                        pasteDir = Direction.N;
                    }
                } else if (nearbyRoads.contains(Direction.E) && nearbyRoads.contains(Direction.W)) {
                    schemList.addAll(this.schemHandler.getRoadsWithQualities(cell.district,cell.roadTier,RoadType.STRAIGHT));
                    if (r.nextInt(2) == 1) {
                        pasteDir = Direction.E;
                    } else {
                        pasteDir = Direction.W;
                    }
                } else {
                    schemList.addAll(this.schemHandler.getRoadsWithQualities(cell.district,cell.roadTier,RoadType.CURVE));
                    if (nearbyRoads.contains(Direction.N) && nearbyRoads.contains(Direction.W)) {
                        pasteDir = Direction.W;
                    } else if (nearbyRoads.contains(Direction.S) && nearbyRoads.contains(Direction.E)) {
                        pasteDir = Direction.E;
                    } else if (nearbyRoads.contains(Direction.S) && nearbyRoads.contains(Direction.W)) {
                        pasteDir = Direction.S;
                    }
                }
            } else if (nearbyRoads.size() == 3) {
                schemList.addAll(this.schemHandler.getRoadsWithQualities(cell.district,cell.roadTier,RoadType.TRI));
                if (nearbyRoads.contains(Direction.N) && nearbyRoads.contains(Direction.E) && nearbyRoads.contains(Direction.S)) {
                    pasteDir = Direction.E;
                } else if (nearbyRoads.contains(Direction.W) && nearbyRoads.contains(Direction.E) && nearbyRoads.contains(Direction.S)) {
                    pasteDir = Direction.S;
                } else if (nearbyRoads.contains(Direction.N) && nearbyRoads.contains(Direction.W) && nearbyRoads.contains(Direction.S)) {
                    pasteDir = Direction.W;
                }
            } else if (nearbyRoads.size() == 4) {
                schemList.addAll(this.schemHandler.getRoadsWithQualities(cell.district,cell.roadTier,RoadType.QUAD));
                int rInt = r.nextInt(4);
                if (rInt == 1) {
                    pasteDir = Direction.E;
                } else if (rInt == 2) {
                    pasteDir = Direction.S;
                } else if (rInt == 3) {
                    pasteDir = Direction.W;
                }
            }
            schematic = schemList.get(r.nextInt(schemList.size()));
            pasteRoadSchematic(cell, pasteDir, schematic);
    }

    public void pasteRiverSchematic(Cell cell, Direction direction, Schematic schematic) throws IOException {
        Paste paste = new Paste(schematic, schematic.xLength, schematic.zHeight, direction, cell.x, cell.z);
        ClipboardFormat format = ClipboardFormats.findByFile(new File(schemHandler.schematicDirectory, paste.schematic.fileName + ".schematic"));
        ClipboardReader reader = format.getReader(new FileInputStream(new File(schemHandler.schematicDirectory, paste.schematic.fileName + ".schematic")));
        Clipboard clipboard = reader.read();
        int rotation = 0;
        if (paste.direction == Direction.E) {
            rotation = 270;
        } else if (paste.direction == Direction.S) {
            rotation = 180;
        } else if (paste.direction == Direction.W) {
            rotation = 90;
        }
        int finalRotation = rotation;
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(Bukkit.getWorlds().get(0)), -1);
                ClipboardHolder holder = new ClipboardHolder(clipboard);
                if (finalRotation > 0) {
                    holder.setTransform(new AffineTransform().rotateY(finalRotation));
                }
                if (paste.direction == Direction.N) {
                    Operation operation = holder.createPaste(editSession).to(BlockVector3.at(startX + (paste.targetCellX * cellSize), yLvl + 1, startZ + (paste.targetCellZ * -cellSize))).ignoreAirBlocks(false).build();
                    try {
                        Operations.complete(operation);
                    } catch (WorldEditException e) {
                        throw new RuntimeException(e);
                    }
                } else if (paste.direction == Direction.E) {
                    Operation operation = holder.createPaste(editSession).to(BlockVector3.at((startX + ((paste.targetCellX + 1) * cellSize) - 5), yLvl + 1, (startZ + (paste.targetCellZ * -cellSize) - 4))).ignoreAirBlocks(false).build();
                    try {
                        Operations.complete(operation);
                    } catch (WorldEditException e) {
                        throw new RuntimeException(e);
                    }
                } else if (paste.direction == Direction.S) {
                    Operation operation = holder.createPaste(editSession).to(BlockVector3.at((startX + ((paste.targetCellX + 1) * cellSize) - 1), yLvl + 1, (startZ + (paste.targetCellZ * -cellSize)) + 1 - cellSize)).ignoreAirBlocks(false).build();
                    try {
                        Operations.complete(operation);
                    } catch (WorldEditException e) {
                        throw new RuntimeException(e);
                    }
                } else if (paste.direction == Direction.W) {
                    Operation operation = holder.createPaste(editSession).to(BlockVector3.at(startX + (paste.targetCellX * cellSize) + 4, yLvl + 1, (startZ + (paste.targetCellZ * -cellSize)) + 5 - cellSize)).ignoreAirBlocks(false).build();
                    try {
                        Operations.complete(operation);
                    } catch (WorldEditException e) {
                        throw new RuntimeException(e);
                    }
                }
                editSession.close();
                File configFile = new File(Grid.this.schematicDetailsDirectory, paste.schematic.fileName + ".yml");
                if (configFile.exists()) {
                    generateDetails(paste, configFile);
                }
                Grid.this.riverCells.remove(cell);
                riverCellsGenerated++;
                generationProgress = (float)riverCellsGenerated / (float)riverCellsSize;
                if (Grid.this.riverCells.size() > 0) {
                    try {
                        generateRivers(Grid.this.riverCells.get(r.nextInt(Grid.this.riverCells.size())));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    try {
                        generationProgress = 0.0f;
                        tasksCompleted++;
                        taskBeingPerformed = "Generating roads...";
                        roadCellsSize = roadCells.size();
                        generateRoads(Grid.this.roadCells.get(r.nextInt(Grid.this.roadCells.size())));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }, paste.schematic.area);
    }

    public void pasteRoadSchematic(Cell cell, Direction direction, Schematic schematic) throws IOException {
        Paste paste = new Paste(schematic, schematic.xLength, schematic.zHeight, direction, cell.x, cell.z);
        ClipboardFormat format = ClipboardFormats.findByFile(new File(schemHandler.schematicDirectory, paste.schematic.fileName + ".schematic"));
        ClipboardReader reader = format.getReader(new FileInputStream(new File(schemHandler.schematicDirectory, paste.schematic.fileName + ".schematic")));
        Clipboard clipboard = reader.read();
        int rotation = 0;
        if (paste.direction == Direction.E) {
            rotation = 270;
        } else if (paste.direction == Direction.S) {
            rotation = 180;
        } else if (paste.direction == Direction.W) {
            rotation = 90;
        }
        int finalRotation = rotation;
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(Bukkit.getWorlds().get(0)), -1);
                ClipboardHolder holder = new ClipboardHolder(clipboard);
                if (finalRotation > 0) {
                    holder.setTransform(new AffineTransform().rotateY(finalRotation));
                }
                if (paste.direction == Direction.N) {
                    Operation operation = holder.createPaste(editSession).to(BlockVector3.at(startX + (paste.targetCellX * cellSize), yLvl + 1, startZ + (paste.targetCellZ * -cellSize))).ignoreAirBlocks(false).build();
                    try {
                        Operations.complete(operation);
                    } catch (WorldEditException e) {
                        throw new RuntimeException(e);
                    }
                } else if (paste.direction == Direction.E) {
                    Operation operation = holder.createPaste(editSession).to(BlockVector3.at((startX + ((paste.targetCellX + 1) * cellSize) - 5), yLvl + 1, (startZ + (paste.targetCellZ * -cellSize) - 4))).ignoreAirBlocks(false).build();
                    try {
                        Operations.complete(operation);
                    } catch (WorldEditException e) {
                        throw new RuntimeException(e);
                    }
                } else if (paste.direction == Direction.S) {
                    Operation operation = holder.createPaste(editSession).to(BlockVector3.at((startX + ((paste.targetCellX + 1) * cellSize) - 1), yLvl + 1, (startZ + (paste.targetCellZ * -cellSize)) + 1 - cellSize)).ignoreAirBlocks(false).build();
                    try {
                        Operations.complete(operation);
                    } catch (WorldEditException e) {
                        throw new RuntimeException(e);
                    }
                } else if (paste.direction == Direction.W) {
                    Operation operation = holder.createPaste(editSession).to(BlockVector3.at(startX + (paste.targetCellX * cellSize) + 4, yLvl + 1, (startZ + (paste.targetCellZ * -cellSize)) + 5 - cellSize)).ignoreAirBlocks(false).build();
                    try {
                        Operations.complete(operation);
                    } catch (WorldEditException e) {
                        throw new RuntimeException(e);
                    }
                }
                editSession.close();
                File configFile = new File(Grid.this.schematicDetailsDirectory, paste.schematic.fileName + ".yml");
                if (configFile.exists()) {
                    generateDetails(paste, configFile);
                }
                Grid.this.roadCells.remove(cell);
                roadCellsGenerated++;
                generationProgress = (float)roadCellsGenerated/(float)roadCellsSize;
                if (Grid.this.roadCells.size() > 0) {
                    try {
                        generateRoads(Grid.this.roadCells.get(r.nextInt(Grid.this.roadCells.size())));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    try {
                        generationProgress = 0.0f;
                        taskBeingPerformed = "Generating structures...";
                        tasksCompleted++;
                        nonRepeatableSchematicsSize = plugin.schematicHandler.slumsSchematics.size() + plugin.schematicHandler.outskirtsSchematics.size() + plugin.schematicHandler.aristocracySchematics.size() + plugin.schematicHandler.farmSchematics.size() + plugin.schematicHandler.portSchematics.size();
                        Grid.this.generateStructure(getSchematicsForDistrict(District.PORT), grid, getCellsForDistrict(District.PORT), District.PORT);
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


    public void assignDistricts(Cell[][] grid) {
        float threeFourths = (3*(float)size)/4.0f;
        float oneFourth = (float)size/4.0f;
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                if (z <= (oneFourth-1) &&
                        x >= (float)(1 / 5) * (float)z + (oneFourth-1) &&
                        x <= -(float)(1 / 5) * (float)z + (threeFourths-1)) {
                    grid[x][z].district = District.PORT;
                    this.portCells.add(grid[x][z]);
                } else if (x <= (float) (1 / 5) * (float) (z - (size - 1)) + (threeFourths - 1) &&
                        x >= -(float) (1 / 5) * (float) (z - (size - 1)) + (oneFourth - 1) &&
                        z >= (threeFourths-1)) {
                    grid[x][z].district = District.FARM;
                    this.farmCells.add(grid[x][z]);
                } else if (z >= -(float) (1 / 5) * (float) (x-(size-1)) + (oneFourth-1) &&
                        z <= (float)(1/5) * (float)(x-(size-1)) + (threeFourths-1) &&
                        x >= (threeFourths-1)) {
                    grid[x][z].district = District.ARISTOCRACY;
                    this.aristocracyCells.add(grid[x][z]);
                } else if (z <= -(float)(1 / 5) * (float)x + (threeFourths-1) &&
                        z >= (float)(1 / 5) * (float)x + (oneFourth-1) &&
                        x <= (oneFourth-1)) {
                    grid[x][z].district = District.SLUMS;
                    this.slumsCells.add(grid[x][z]);
                } else {
                    grid[x][z].district = District.OUTSKIRTS;
                    this.outskirtsCells.add(grid[x][z]);
                }
            }
        }
    }

    public void assignSideRoads() {
        List<Cell> outskirtSideRoadStartPoints = new ArrayList<>();
        for (int districtCount = 0; districtCount < 5; districtCount++) {
            District district;
            Direction toDirection = null;
            Direction fromDirection = null;
            if (districtCount == 0) {
                district = District.PORT;
                toDirection = Direction.N;
                fromDirection = Direction.S;
            } else if (districtCount == 1) {
                district = District.ARISTOCRACY;
                toDirection = Direction.W;
                fromDirection = Direction.E;
            } else if (districtCount == 2) {
                district = District.FARM;
                toDirection = Direction.S;
                fromDirection = Direction.N;
            } else if (districtCount==3) {
                district = District.SLUMS;
                toDirection = Direction.E;
                fromDirection = Direction.W;
            } else {
                district = District.OUTSKIRTS;
            }
            if (districtCount != 4) {
                List<Cell> fromCells = getBorderCells(district,fromDirection);
                List<Cell> toCells = getBorderCells(district,toDirection);
                fromCells = sortCellsByDirection(fromCells,fromDirection);
                toCells = sortCellsByDirection(toCells,toDirection);

                for (int j = 0; j < sideRoadsPerDistrict; j++) {
                    int fromStart = Math.round(j * ((float)fromCells.size()/(float)sideRoadsPerDistrict));
                    int fromBound = Math.round((j+1) * ((float)fromCells.size()/(float)sideRoadsPerDistrict));
                    if (fromStart > fromBound) {
                        int num1 = fromStart;
                        int num2 = fromBound;
                        fromBound = num1;
                        fromStart = num2;
                    }
                    int toStart = Math.round(j * ((float)toCells.size()/(float)sideRoadsPerDistrict));
                    int toBound = Math.round((j+1) * ((float)toCells.size()/(float)sideRoadsPerDistrict));
                    if (toStart > toBound) {
                        int num1 = toStart;
                        int num2 = toBound;
                        toBound = num1;
                        toStart = num2;
                    }
                    List<AStarNode> path = null;
                    int attempts = fromCells.size()*2;
                    do {
                        attempts--;

                        Cell fromCell = fromCells.get(r.nextInt(fromStart,fromBound));
                        Cell toCell = toCells.get(r.nextInt(toStart,toBound));
                        if (!(fromCell.isRoad && fromCell.roadTier == RoadTier.HIGHWAY) && !(toCell.isRoad && toCell.roadTier == RoadTier.HIGHWAY)) {
                            path = performSideRoadMapping(fromCell.x,fromCell.z,toCell.x,toCell.z);
                            if (path != null) {
                                outskirtSideRoadStartPoints.add(toCell);
                            }
                        }
                    } while(path == null && attempts > 0);
                    if (path != null) {
                        for(AStarNode node : path) {
                            int x = node.x;
                            int z = node.z;
                            grid[x][z].isRoad = true;
                            grid[x][z].isOccupied = true;
                            grid[x][z].roadTier = RoadTier.NORMAL;
                            this.roadCells.add(grid[x][z]);
                        }
                    }
                }
            } else {
                Iterator<Cell> iter = outskirtSideRoadStartPoints.iterator();
                while (iter.hasNext()) {
                    Cell cell = iter.next();
                    Direction dir;
                    if (cell.district == District.PORT) {
                        dir = Direction.N;
                    } else if (cell.district == District.ARISTOCRACY) {
                        dir = Direction.W;
                    } else if (cell.district == District.SLUMS) {
                        dir = Direction.E;
                    } else if (cell.district == District.FARM) {
                        dir = Direction.S;
                    } else {
                        continue;
                    }

                    List<Cell> borderCells = getCellsOnMapBorder(dir);
                    List<AStarNode> path = null;
                    int attempts = borderCells.size()*2;
                    do {
                        attempts--;
                        Cell toCell = borderCells.get(r.nextInt(borderCells.size()));
                        if (!(cell.isRoad && cell.roadTier == RoadTier.HIGHWAY) && !(toCell.isRoad && toCell.roadTier == RoadTier.HIGHWAY)) {
                            path = performSideRoadMappingOutskirts(cell.x, cell.z,toCell.x,toCell.z);
                        }
                    } while(path == null && attempts > 0);
                    if (path != null) {
                        for(AStarNode node : path) {
                            int x = node.x;
                            int z = node.z;
                            grid[x][z].isRoad = true;
                            grid[x][z].isOccupied = true;
                            grid[x][z].roadTier = RoadTier.NORMAL;
                            this.roadCells.add(grid[x][z]);
                        }
                    }
                }
            }

        }
        tasksCompleted++;

    }

    public List<Cell> sortCellsByDirection(List<Cell> cells, Direction direction) {
        if (direction == Direction.N || direction == Direction.S) {
            cells.sort(new Comparator<Cell>() {
                @Override
                public int compare(Cell o1, Cell o2) {
                    return o1.x - o2.x;
                }
            });
            return cells;
        } else {
            cells.sort(new Comparator<Cell>() {
                @Override
                public int compare(Cell o1, Cell o2) {
                    return o1.z - o2.z;
                }
            });
            return cells;
        }
    }

    public Cell getCellOnMapBorder(District district) {
        List<Cell> cellsForDistrict = new ArrayList<>(getCellsForDistrict(district));
        Collections.shuffle(cellsForDistrict);
        Iterator<Cell> cells = cellsForDistrict.iterator();
        int[][] directions = {{1,0},{0,1},{-1,0},{0,-1}};
        Cell returnCell = null;
        do {
            Cell cell = cells.next();
            for (int i = 0; i < directions.length; i++) {
                int x = cell.x+directions[i][0];
                int z = cell.z+directions[i][1];
                if (x < 0 || x >= size || z < 0 || z >= size) {
                    returnCell = cell;
                }
            }
        } while(returnCell == null && cells.hasNext());
        return returnCell;
    }

    public Cell getCellOnDistrictBorder(District district) {
        List<Cell> cellsForDistrict = new ArrayList<>(getCellsForDistrict(district));
        Collections.shuffle(cellsForDistrict);
        Iterator<Cell> cells = cellsForDistrict.iterator();
        int[][] directions = {{1,0},{0,1},{-1,0},{0,-1}};
        Cell returnCell = null;
        do {
            Cell cell = cells.next();
            for (int i = 0; i < directions.length; i++) {
                int x = cell.x+directions[i][0];
                int z = cell.z+directions[i][1];
                if (x >= 0 && x < size && z >= 0 && z < size) {
                    if (grid[x][z].district != cell.district) {
                        returnCell = cell;
                    }
                }
            }
        } while(returnCell == null && cells.hasNext());
        return returnCell;
    }
    public List<Cell> getCellsOnMapBorder(District district) {
        List<Cell> cellsForDistrict = new ArrayList<>(getCellsForDistrict(district));
        Collections.shuffle(cellsForDistrict);
        Iterator<Cell> cells = cellsForDistrict.iterator();
        //int[][] directions = {{1,0},{0,1},{-1,0},{0,-1}};
        List<Cell> returnCells = new ArrayList<>();
        do {
            Cell cell = cells.next();
            if (cell.x == 0 || cell.z == 0 || cell.x == size-1 || cell.z == size-1) {
                returnCells.add(cell);
            }
        } while(cells.hasNext());
        return returnCells;
    }
    public List<Cell> getCellsOnMapBorder(Direction dir) {
        List<Cell> allCells = new ArrayList<>();
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                allCells.add(grid[x][z]);
            }
        }
        Collections.shuffle(allCells);
        Iterator<Cell> cells = allCells.iterator();
        //int[][] directions = {{1,0},{0,1},{-1,0},{0,-1}};
        List<Cell> returnCells = new ArrayList<>();
        do {
            Cell cell = cells.next();
            if (dir == Direction.N && cell.z == size-1) {
                returnCells.add(cell);
            } else if (dir == Direction.S && cell.z == 0) {
                returnCells.add(cell);
            } else if (dir == Direction.E && cell.x == size-1) {
                returnCells.add(cell);
            } else if (dir == Direction.W && cell.x == 0) {
                returnCells.add(cell);
            }
        } while(cells.hasNext());
        return returnCells;
    }
    public List<Cell> getCellsOnMapBorder(District district, Direction dir) {
        List<Cell> cellsForDistrict = new ArrayList<>(getCellsForDistrict(district));
        Collections.shuffle(cellsForDistrict);
        Iterator<Cell> cells = cellsForDistrict.iterator();
        //int[][] directions = {{1,0},{0,1},{-1,0},{0,-1}};
        List<Cell> returnCells = new ArrayList<>();
        do {
            Cell cell = cells.next();
            if (dir == Direction.N && cell.z == size-1) {
                returnCells.add(cell);
            } else if (dir == Direction.S && cell.z == 0) {
                returnCells.add(cell);
            } else if (dir == Direction.E && cell.x == size-1) {
                returnCells.add(cell);
            } else if (dir == Direction.W && cell.x == 0) {
                returnCells.add(cell);
            }

        } while(cells.hasNext());
        return returnCells;
    }

    public List<Cell> getCellsOnDistrictBorder(District district) {
        List<Cell> cellsForDistrict = new ArrayList<>(getCellsForDistrict(district));
        Collections.shuffle(cellsForDistrict);
        Iterator<Cell> cells = cellsForDistrict.iterator();
        int[][] directions = {{1,0},{0,1},{-1,0},{0,-1}};
        List<Cell> returnCells = new ArrayList<>();
        do {
            Cell cell = cells.next();
            for (int i = 0; i < directions.length; i++) {
                int x = cell.x+directions[i][0];
                int z = cell.z+directions[i][1];
                if (x >= 0 && x < size && z >= 0 && z < size) {
                    if (grid[x][z].district != cell.district) {
                        returnCells.add(cell);
                    }
                }
            }
        } while(cells.hasNext());
        return returnCells;
    }
    public List<Cell> getCellsOnDistrictBorder(District district, Direction direction) {
        List<Cell> cellsForDistrict = new ArrayList<>(getCellsForDistrict(district));
        Collections.shuffle(cellsForDistrict);
        Iterator<Cell> cells = cellsForDistrict.iterator();
        int[][] directions = {{1,0},{0,1},{-1,0},{0,-1}};
        List<Cell> returnCells = new ArrayList<>();
        int[] offsetToMatch = getOffsetForDirection(direction);
        do {
            Cell cell = cells.next();
            int count = 0;
            for (int i = 0; i < directions.length; i++) {
                int x = cell.x+directions[i][0];
                int z = cell.z+directions[i][1];
                if (x >= 0 && x < size && z >= 0 && z < size) {
                    if (grid[x][z].district != cell.district) {
                        count++;
                    }
                }
            }
            if ((cell.x+offsetToMatch[0] >= 0 && cell.x+offsetToMatch[0] < size && cell.z+offsetToMatch[1] >= 0 && cell.z+offsetToMatch[1] < size) && count == 1 && grid[cell.x+offsetToMatch[0]][cell.z+offsetToMatch[1]].district != cell.district) {
                returnCells.add(cell);
            }
        } while(cells.hasNext());
        return returnCells;
    }

    private int[] getOffsetForDirection(Direction direction) {
        if (direction == Direction.N) {
            return new int[]{0,1};
        } else if (direction == Direction.S) {
            return new int[]{0,-1};
        } else if (direction == Direction.E) {
            return new int[]{1,0};
        } else if (direction == Direction.W) {
            return new int[]{-1,0};
        }
        return null;
    }

    public List<Cell> getBorderCells(District district, Direction direction) {
        if (district == District.PORT && direction == Direction.S) {
            return getCellsOnMapBorder(district);
        } else if (district == District.ARISTOCRACY && direction == Direction.E) {
            return getCellsOnMapBorder(district);
        } else if (district == District.FARM && direction == Direction.N) {
            return getCellsOnMapBorder(district);
        } else if (district == District.SLUMS && direction == Direction.W) {
            return getCellsOnMapBorder(district);
        } else {
            return getCellsOnDistrictBorder(district,direction);
        }
    }

    public Cell getBorderCell(District district, Direction direction) {

        if (district == District.PORT && direction == Direction.S) {
            return getCellOnMapBorder(district);
        } else if (district == District.ARISTOCRACY && direction == Direction.E) {
            return getCellOnMapBorder(district);
        } else if (district == District.FARM && direction == Direction.N) {
            return getCellOnMapBorder(district);
        } else if (district == District.SLUMS && direction == Direction.W) {
            return getCellOnMapBorder(district);
        } else {
            return getCellOnDistrictBorder(district);
        }
    }
    public List<Cell> getBorderCells(District district) {
        List<Cell> returnList = new ArrayList<>();
        returnList.addAll(getCellsOnMapBorder(district));
        returnList.addAll(getCellsOnDistrictBorder(district));
        Collections.shuffle(returnList);
        return returnList;
    }


    /*public void assignDistricts(Cell[][] grid) {
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
    }*/

    public void generateSideRoads(Cell[][] grid) {
        for (int x = 0; x < size; x++) {
            if (x < size/2) {
                if (x % 9 == 0) {
                    for (int z = 0; z < size; z++) {

                        if (!grid[x][z].isOccupied /*&& !(grid[x][z].district == District.OUTSKIRTS)*/) {
                            grid[x][z].isRoad = true;
                            grid[x][z].isOccupied = true;
                            grid[x][z].roadTier = RoadTier.NORMAL;
                            this.roadCells.add(grid[x][z]);
                            //this.fillCellWithBlock(Material.COBBLESTONE, x , z );
                        }

                    }
                }
            } else {
                if ((x-1) % 9 == 0) {
                    for (int z = 0; z < size; z++) {

                        if (!(grid[x][z].isOccupied && grid[x][z].isRiver) && !grid[x][z].isOccupied /*&& !(grid[x][z].district == District.OUTSKIRTS)*/) {
                            grid[x][z].isRoad = true;
                            grid[x][z].isOccupied = true;
                            grid[x][z].roadTier = RoadTier.NORMAL;
                            this.roadCells.add(grid[x][z]);
                            //this.fillCellWithBlock(Material.COBBLESTONE, x , z );
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
            List<Direction> potentialDirections = new ArrayList(Arrays.asList(Direction.N, Direction.S, Direction.E, Direction.W));
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
                    } else if (!(lastZ < startZ-4)) {
                        nextDirection = Direction.S;
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
                    } else if (nextDirection == Direction.S) {
                        lastZ--;
                    } else {
                        lastZ++;
                    }
                    /*if (grid[lastX][lastZ].district == District.OUTSKIRTS) {
                        return;
                    }*/
                    grid[lastX][lastZ].isRoad = true;
                    grid[lastX][lastZ].isOccupied = true;
                    grid[lastX][lastZ].roadTier = RoadTier.BACKROAD;
                    this.roadCells.add(grid[lastX][lastZ]);
                    //this.fillCellWithBlock(Material.GRAVEL, lastX , lastZ);


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
    public List<Direction> nearbyRiversOnly(int cellX, int cellZ) {
        List<Direction> list = new ArrayList();
        int[][] neighbors = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        int direction = 0;
        for (int[] neighbor : neighbors) {

            int newRow = cellX + neighbor[0];
            int newCol = cellZ + neighbor[1];
            if (newRow >= 0 && newRow < size && newCol >= 0 && newCol < size) {
                Cell neighborCell = grid[newRow][newCol];

                if (neighborCell.isRiver) {
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
                        List<Cell> newCells = markAsOccupied(finalX, finalZ, paste.targetCellX, paste.targetCellZ, grid, finalDirection, paste.schematic.name);
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
                        List<Cell> newCells = markAsOccupied(finalX, finalZ, paste.targetCellX, paste.targetCellZ, grid, finalDirection, paste.schematic.name);
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
                        List<Cell> newCells = markAsOccupied(finalX, finalZ, paste.targetCellX, paste.targetCellZ, grid, finalDirection, paste.schematic.name);
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
                        List<Cell> newCells = markAsOccupied(finalX, finalZ, paste.targetCellX, paste.targetCellZ, grid, finalDirection, paste.schematic.name);
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
                    nonRepeatableSchematicsGenerated++;
                    generationProgress = (float)nonRepeatableSchematicsGenerated/(float)nonRepeatableSchematicsSize;

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
                    nonRepeatableSchematicsGenerated++;
                    generationProgress = (float)nonRepeatableSchematicsGenerated/(float)nonRepeatableSchematicsSize;


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
                    nonRepeatableSchematicsGenerated++;
                    generationProgress = (float)nonRepeatableSchematicsGenerated/(float)nonRepeatableSchematicsSize;

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
                    nonRepeatableSchematicsGenerated++;
                    generationProgress = (float)nonRepeatableSchematicsGenerated/(float)nonRepeatableSchematicsSize;

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
        if (!config.contains(paste.schematic.fileName)) {
            return;
        }
        ConfigurationSection master = config.getConfigurationSection(paste.schematic.fileName);
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
                        newCells.addAll(markAsOccupied(finalX, finalZ, paste.targetCellX, paste.targetCellZ, grid, finalDirection, paste.schematic.name));
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
                    repeatableSchematicsGenerated+= newCells.size();
                    generationProgress = (float)repeatableSchematicsGenerated/(float)repeatableSchematicsSize;
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
        if (schematicList.size() < 1) {
            if (plugin.schematicHandler.slumsSchematics.size() < 1 && plugin.schematicHandler.portSchematics.size() < 1 && plugin.schematicHandler.farmSchematics.size() < 1 && plugin.schematicHandler.aristocracySchematics.size() < 1) {
                generateRepeatablesInitial(grid);
                return;
            }
            generateStructure(getSchematicsForDistrict(cycleToNextDistrict(lastDistrict)), grid, getCellsForDistrict(cycleToNextDistrict(lastDistrict)), cycleToNextDistrict(lastDistrict));
            return;
        }
        Schematic schematic = schematicList.get(0);

        Collections.shuffle(cells);
        Iterator<Cell> cellsIter = cells.iterator();
        do {
            Cell cell = (Cell) cellsIter.next();
            Direction direction;
            if (schematic.omnidirectional) {
                direction = potentialDirections.get(r.nextInt(potentialDirections.size()));
            } else {
                List<Direction> nearbyRoads = nearbyRoadsOnly(cell.x, cell.z);
                direction = null;
                if (nearbyRoads.size() > 1) {
                    direction = nearbyRoads.get(r.nextInt(nearbyRoads.size()));
                } else if (nearbyRoads.size() == 1) {
                    direction = nearbyRoads.get(0);
                }
                if (direction == null) {
                    continue;
                }
            }
            int targetZ = cell.z;
            int targetX = cell.x;
            int x = schematic.xLength;
            int z = schematic.zHeight;
            if (direction == Direction.W || direction == Direction.E) {
                x = schematic.zHeight;
                z = schematic.xLength;
            }
            if (this.schemFitsInBounds(x, z, targetX, targetZ, grid, direction) && !this.containsBlacklistedCells(x,z,targetX,targetZ,direction,schematic)) {
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
                        Direction linkedDirection;
                        if (linkedSchem.omnidirectional) {
                            linkedDirection = potentialDirections.get(r.nextInt(potentialDirections.size()));
                        } else {
                            List<Direction> linkedNearbyRoads = nearbyRoadsOnly(linkedCell.x, linkedCell.z);
                            linkedDirection = null;
                            if (linkedNearbyRoads.size() > 1) {
                                linkedDirection = linkedNearbyRoads.get(r.nextInt(linkedNearbyRoads.size()));
                            } else if (linkedNearbyRoads.size() == 1) {
                                linkedDirection = linkedNearbyRoads.get(0);
                            }
                            if (linkedDirection == null) {
                                continue;
                            }
                        }
                        int linkedTargetZ = linkedCell.z;
                        int linkedTargetX = linkedCell.x;
                        int linkedX = linkedSchem.xLength;
                        int linkedZ = linkedSchem.zHeight;
                        if (linkedDirection == Direction.W || linkedDirection == Direction.E) {
                            linkedX = linkedSchem.zHeight;
                            linkedZ = linkedSchem.xLength;
                        }
                        if (this.schemFitsInBounds(linkedX, linkedZ, linkedTargetX, linkedTargetZ, grid, linkedDirection) && !this.containsBlacklistedCells(linkedX,linkedZ,linkedTargetX,linkedTargetZ,linkedDirection,linkedSchem) && !containsToBeOccupiedCells(toBeOccupiedCells(paste.x, paste.z, paste.targetCellX, paste.targetCellZ, grid, paste.direction), toBeOccupiedCells(linkedX, linkedZ, linkedTargetX, linkedTargetZ, grid, linkedDirection))) {
                            Paste pasteLinked = new Paste(linkedSchem, linkedX, linkedZ, linkedDirection, linkedTargetX, linkedTargetZ);
                            pasteLinked(pasteLinked, paste, getCellsForDistrict(pasteLinked.schematic.district));
                            return;
                        }

                    } while (linkedIter.hasNext());
                    schematicList.remove(schematic);
                    nonRepeatableSchematicsGenerated++;
                    generationProgress = (float)nonRepeatableSchematicsGenerated/(float)nonRepeatableSchematicsSize;
                    generateStructure(getSchematicsForDistrict(cycleToNextDistrict(lastDistrict)), grid, getCellsForDistrict(cycleToNextDistrict(lastDistrict)), cycleToNextDistrict(lastDistrict));
                    return;
                }
            }
        } while (cellsIter.hasNext());
        schematicList.remove(schematic);
        nonRepeatableSchematicsGenerated++;
        generationProgress = (float)nonRepeatableSchematicsGenerated/(float)nonRepeatableSchematicsSize;
        generateStructure(getSchematicsForDistrict(cycleToNextDistrict(lastDistrict)), grid, getCellsForDistrict(cycleToNextDistrict(lastDistrict)), cycleToNextDistrict(lastDistrict));
    }
    public void generateRepeatablesInitial(Cell[][] grid) throws IOException {
        Bukkit.getLogger().log(Level.INFO, "Starting repeatable generation...");
        lastGeneration = System.currentTimeMillis();
        tasksCompleted++;
        generationProgress = 0.0f;
        taskBeingPerformed = "Generating resources...";
        List<Cell> remainingCells = new ArrayList<>();
        remainingCells.addAll(getCellsForDistrict(District.FARM));
        remainingCells.addAll(getCellsForDistrict(District.ARISTOCRACY));
        remainingCells.addAll(getCellsForDistrict(District.SLUMS));
        remainingCells.addAll(getCellsForDistrict(District.PORT));
        remainingCells.addAll(getCellsForDistrict(District.OUTSKIRTS));
        Collections.shuffle(remainingCells);
        repeatableSchematicsSize = remainingCells.size();
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
                repeatableSchematicsGenerated++;
                generationProgress = (float)repeatableSchematicsGenerated/(float)repeatableSchematicsSize;
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
                            if (this.schemFitsInBounds(x, z, targetX, targetZ, grid, oDirection) && !this.containsBlacklistedCells(x,z,targetX,targetZ,oDirection,schematic)) {

                                Paste paste = new Paste(schematic, x, z, oDirection, targetX, targetZ);

                                pasteRepeatable(paste, remainingCells, true);
                                getOmniSchematicsForDistrict(paste.schematic.district).remove(paste.schematic);
                                pasted = true;
                                //Bukkit.getLogger().log(Level.INFO, "Passing to pasteRepeatable");

                            }
                        } else {
                            if (this.schemFitsInBounds(x, z, targetX, targetZ, grid, oDirection) && !this.containsBlacklistedCells(x,z,targetX,targetZ,oDirection,schematic)) {
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
                        if (schemFitsInBounds(x, z, targetX, targetZ, grid, direction) && !this.containsBlacklistedCells(x,z,targetX,targetZ,direction,schematic)) {
                            Paste paste = new Paste(schematic, x, z, direction, targetX, targetZ);
                            pasteRepeatable(paste, remainingCells, false);
                            //Bukkit.getLogger().log(Level.INFO, "Passing to pasteRepeatable");
                            pass = true;
                        }
                    } else {

                        if (schemFitsInBounds(x, z, targetX, targetZ, grid, direction) && !this.containsBlacklistedCells(x,z,targetX,targetZ,direction,schematic)) {
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


    public boolean containsBlacklistedCells(int xSize, int zSize, int gridX, int gridZ, Direction dir, Schematic schematic) {
        int xFactor = 1;
        int zFactor = 1;
        if (dir == Direction.W) {
            zFactor = -1;
        } else if (dir == Direction.N) {
            xFactor = -1;
            zFactor = -1;
        } else if (dir == Direction.E) {
            xFactor = -1;
        }
        for (int x = 0 ; x < xSize; x++ ){
            for (int z = 0; z < zSize; z++) {
                int newX = gridX + (x * xFactor);
                int newZ = gridZ + (z * zFactor);
                for (int[] blacklist : schematic.blacklistedCells) {
                    if (newX == blacklist[0] && newZ == blacklist[1]) {
                        return true;
                    }
                }
            }
        }
        return false;
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

    public List<int[]> registerBlackList(List<String> list) {
        List<int[]> returnList = new ArrayList<>();
        for (String line : list) {
            if (line.matches("[0-9]+,[0-9]+")) {
                String[] parts = line.split(",");
                returnList.add(new int[]{Integer.parseInt(parts[0]),Integer.parseInt(parts[1])});
            } else if (line.matches(">[0-9]+,[0-9]+")) {
                String[] parts = line.split(",");
                parts[0] = parts[0].replace(">","");
                for (int i = Integer.parseInt(parts[0])+1; i < this.size; i++) {
                    returnList.add(new int[]{i, Integer.parseInt(parts[1])});
                }
            } else if (line.matches("<[0-9]+,[0-9]+")) {
                String[] parts = line.split(",");
                parts[0] = parts[0].replace("<","");
                for (int i = 0; i < Integer.parseInt(parts[0]); i++) {
                    returnList.add(new int[]{i, Integer.parseInt(parts[1])});
                }
            } else if (line.matches("[0-9]+,>[0-9]+")) {
                String[] parts = line.split(",");
                parts[1] = parts[1].replace(">","");
                for (int i = Integer.parseInt(parts[1])+1; i < size; i++) {
                    returnList.add(new int[]{Integer.parseInt(parts[0]), i});
                }
            } else if (line.matches("[0-9]+,<[0-9]+")) {
                String[] parts = line.split(",");
                parts[1] = parts[1].replace("<","");
                for (int i = 0; i < Integer.parseInt(parts[1]); i++) {
                    returnList.add(new int[]{Integer.parseInt(parts[0]), i});
                }
            } else if (line.matches(">[0-9]+,>[0-9]+")) {
                String[] parts = line.split(",");
                parts[0] = parts[0].replace(">","");
                parts[1] = parts[1].replace(">","");
                for (int x = Integer.parseInt(parts[0])+1; x < this.size; x++) {
                    for (int z = Integer.parseInt(parts[1])+1; z < size; z++) {
                        returnList.add(new int[]{x,z});
                    }
                }
            } else if (line.matches("<[0-9]+,>[0-9]+")) {
                String[] parts = line.split(",");
                parts[0] = parts[0].replace("<","");
                parts[1] = parts[1].replace(">","");
                for (int x = 0; x < Integer.parseInt(parts[0]); x++) {
                    for (int z = Integer.parseInt(parts[1])+1; z < size; z++) {
                        returnList.add(new int[]{x,z});
                    }
                }
            } else if (line.matches(">[0-9]+,<[0-9]+")) {
                String[] parts = line.split(",");
                parts[0] = parts[0].replace("<","");
                parts[1] = parts[1].replace("<","");
                for (int x = Integer.parseInt(parts[0])+1; x < this.size ; x++) {
                    for (int z = 0; z < Integer.parseInt(parts[1]); z++) {
                        returnList.add(new int[]{x,z});
                    }
                }
            } else if (line.matches("<[0-9]+,<[0-9]+")) {
                String[] parts = line.split(",");
                parts[0] = parts[0].replace("<","");
                parts[1] = parts[1].replace("<","");
                for (int x = 0; x < Integer.parseInt(parts[0]); x++) {
                    for (int z = 0; z < Integer.parseInt(parts[1]); z++) {
                        returnList.add(new int[]{x,z});
                    }
                }
            } else if (line.matches("x = [0-9]+")) {
                String x = line.replace("x = ", "");
                for (int z = 0; z < size; z++) {
                    returnList.add(new int[]{Integer.parseInt(x), z});
                }
            } else if (line.matches("z = [0-9]+")) {
                String z = line.replace("z = ", "");
                for (int x = 0; x < size; x++) {
                    returnList.add(new int[]{x, Integer.parseInt(z)});
                }
            }
        }
        return returnList;
    }




}
