package me.genn.thegrandtourney;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;


import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.mana.ManaChangeReason;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.MaxChangedBlocksException;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.world.DataException;

import me.genn.thegrandtourney.grid.Cell;
import me.genn.thegrandtourney.grid.District;
import me.genn.thegrandtourney.grid.Grid;
import me.genn.thegrandtourney.grid.SchematicHandler;
import me.genn.thegrandtourney.item.ItemHandler;
import me.genn.thegrandtourney.listener.EventListener;
import me.genn.thegrandtourney.mobs.MobHandler;
import me.genn.thegrandtourney.mobs.Spawner;
import me.genn.thegrandtourney.mobs.SpawnerHandler;
import me.genn.thegrandtourney.mobs.SpawnerTemplate;
import me.genn.thegrandtourney.npc.*;
import me.genn.thegrandtourney.player.MMOPlayer;
import me.genn.thegrandtourney.player.StatUpdates;
import me.genn.thegrandtourney.skills.farming.Crop;
import me.genn.thegrandtourney.skills.farming.CropHandler;
import me.genn.thegrandtourney.skills.farming.CropTemplate;
import me.genn.thegrandtourney.skills.fishing.FishingZone;
import me.genn.thegrandtourney.skills.fishing.FishingZoneHandler;
import me.genn.thegrandtourney.skills.fishing.FishingZoneTemplate;
import me.genn.thegrandtourney.skills.mining.Ore;
import me.genn.thegrandtourney.skills.mining.OreHandler;
import me.genn.thegrandtourney.skills.mining.OreTemplate;
import me.genn.thegrandtourney.util.SchematicCreator;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.TraitInfo;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;

import static org.bukkit.scoreboard.Team.OptionStatus.NEVER;


public class TGT extends JavaPlugin implements Listener {
    public static TGT plugin;
    public SchematicHandler schematicHandler;
    public ItemHandler itemHandler;
    public NPCHandler npcHandler;
    public MobHandler mobHandler;
    public OreHandler oreHandler;
    public SpawnerHandler spawnerHandler;
    public SchematicCreator schematicCreator;
    public CropHandler cropHandler;
    public FishingZoneHandler fishingZoneHandler;
    Grid grid;
    public Map<String, Integer> npcMap;
    public Map<String, Float> defaultStatValues;
    public NPCRegistry registry;
    public Map<UUID, MMOPlayer> players;
    public EventListener listener;
    BukkitTask displayTask;
    BukkitTask healthRegenTask;
    ProtocolManager pm;
    MagicSpells ms;
    PluginManager pluginManager;
    Scoreboard scoreboard;
    Objective objective;
    Team mobTeam;
    public Map<Location, Ore> oreLocationList;
    public Map<Location, Crop> cropLocationList;
    public Map<Location, TGTNpc> npcLocationList;
    public Map<Location, Spawner> spawnerLocationList;
    public List<FishingZone> fishingZoneList;
    public Score daysRemainingScore;
    public Score timeScore;
    public Score locationScore;

    public Score purseScore;
    public Score objectiveScore;
    public Score objectiveDescriptionScore;
    public Score websiteScore;




    public void onEnable() {
        plugin = this;
        npcMap = new HashMap();
        this.pm = ProtocolLibrary.getProtocolManager();
        this.pluginManager = this.getServer().getPluginManager();
        this.oreLocationList = new HashMap<>();
        this.cropLocationList = new HashMap<>();
        this.npcLocationList = new HashMap<>();
        this.spawnerLocationList = new HashMap<>();
        this.fishingZoneList = new ArrayList<>();

        defaultStatValues = new HashMap();
        players = new HashMap();
        File configFile = new File(this.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            this.saveDefaultConfig();
        }
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(configFile);
        } catch (Exception var5) {
            this.getLogger().severe("FAILED TO LOAD CONFIG FILE");
            var5.printStackTrace();
            this.setEnabled(false);
            return;
        }
        ConfigurationSection defaultStats = config.getConfigurationSection("default-stats");
        if (defaultStats == null) {
            this.getLogger().severe(ChatColor.RED.toString() + "STATS CONFIG IS NULL!");
            this.setEnabled(false);
            return;
        }
        this.defaultStatValues.put("health", (float) defaultStats.getDouble("health"));
        this.defaultStatValues.put("defense", (float) defaultStats.getDouble("defense"));
        this.defaultStatValues.put("damage", (float) defaultStats.getDouble("damage"));
        this.defaultStatValues.put("crit-damage", (float) defaultStats.getDouble("crit-damage"));
        this.defaultStatValues.put("speed", (float) defaultStats.getDouble("speed"));
        this.defaultStatValues.put("crit-chance", (float) defaultStats.getDouble("crit-chance"));
        this.defaultStatValues.put("mana", (float) defaultStats.getDouble("mana"));
        this.defaultStatValues.put("ability-damage", (float) defaultStats.getDouble("ability-damage"));
        this.defaultStatValues.put("shop-discount", (float) defaultStats.getDouble("shop-discount"));
        this.defaultStatValues.put("dialogue-speed", (float) defaultStats.getDouble("dialogue-speed"));
        this.defaultStatValues.put("gold", (float) defaultStats.getDouble("gold"));
        this.defaultStatValues.put("health-regen", (float) defaultStats.getDouble("health-regen"));
        this.defaultStatValues.put("mana-regen", (float) defaultStats.getDouble("mana-regen"));
        this.defaultStatValues.put("attack-speed", (float) defaultStats.getDouble("attack-speed"));
        this.defaultStatValues.put("lure", (float) defaultStats.getDouble("lure"));
        this.defaultStatValues.put("flash", (float) defaultStats.getDouble("flash"));

        this.registry = CitizensAPI.getNPCRegistry();


        if (!config.contains("items")) {
            config.createSection("items");
        }
        File itemFile = new File(this.getDataFolder(), "items.yml");

        if (!itemFile.exists()) {
            this.saveResource("items.yml", false);
        }
        YamlConfiguration items = new YamlConfiguration();
        ConfigurationSection sec = config.getConfigurationSection("items");
        if (sec == null) {
            sec = config.createSection("items");
        }
        try {
            items.load(itemFile);
            Set<String> keys = items.getKeys(true);
            Iterator i$ = keys.iterator();
            while(i$.hasNext()) {
                String key = (String)i$.next();
                sec.set(key, items.get(key));
            }
        } catch (Exception var5) {
            this.getLogger().severe("FAILED TO LOAD ITEMS FILE");
            var5.printStackTrace();
            this.setEnabled(false);
            return;
        }
        ConfigurationSection itemsSection = config.getConfigurationSection("items");
        if (itemsSection == null) {
            this.getLogger().severe(ChatColor.RED.toString() + "ITEM CONFIG IS NULL!");
            this.setEnabled(false);
            return;
        }

        this.itemHandler = new ItemHandler();
        try {
            this.itemHandler.registerItems(this, itemsSection);
        } catch (IOException e) {
            e.printStackTrace();
        }


        /*
        START ORE REGISTER
         */

        if (!config.contains("ores")) {
            config.createSection("ores");
        }
        File oreFile = new File(this.getDataFolder(), "ores.yml");

        if (!oreFile.exists()) {
            this.saveResource("ores.yml", false);
        }
        YamlConfiguration ores = new YamlConfiguration();
        sec = config.getConfigurationSection("ores");
        if (sec == null) {
            sec = config.createSection("ores");
        }
        try {
            ores.load(oreFile);
            Set<String> keys = ores.getKeys(true);
            Iterator i$ = keys.iterator();
            while(i$.hasNext()) {
                String key = (String)i$.next();
                sec.set(key, ores.get(key));
            }
        } catch (Exception var5) {
            this.getLogger().severe("FAILED TO LOAD ORES FILE");
            var5.printStackTrace();
            this.setEnabled(false);
            return;
        }
        ConfigurationSection oresSection = config.getConfigurationSection("ores");
        if (oresSection == null) {
            this.getLogger().severe(ChatColor.RED.toString() + "ORES CONFIG IS NULL!");
            this.setEnabled(false);
            return;
        }

        this.oreHandler = new OreHandler();
        try {
            this.oreHandler.registerOreTemplates(this, oresSection);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*
        START CROP REGISTER
         */

        if (!config.contains("crops")) {
            config.createSection("crops");
        }
        File cropFile = new File(this.getDataFolder(), "crops.yml");

        if (!cropFile.exists()) {
            this.saveResource("crops.yml", false);
        }
        YamlConfiguration crops = new YamlConfiguration();
        sec = config.getConfigurationSection("crops");
        if (sec == null) {
            sec = config.createSection("crops");
        }
        try {
            crops.load(cropFile);
            Set<String> keys = crops.getKeys(true);
            Iterator i$ = keys.iterator();
            while(i$.hasNext()) {
                String key = (String)i$.next();
                sec.set(key, crops.get(key));
            }
        } catch (Exception var5) {
            this.getLogger().severe("FAILED TO LOAD CROPS FILE");
            var5.printStackTrace();
            this.setEnabled(false);
            return;
        }
        ConfigurationSection cropsSection = config.getConfigurationSection("crops");
        if (cropsSection == null) {
            this.getLogger().severe(ChatColor.RED.toString() + "CROPS CONFIG IS NULL!");
            this.setEnabled(false);
            return;
        }

        this.cropHandler = new CropHandler();
        try {
            this.cropHandler.registerCropTemplates(this, cropsSection);
        } catch (IOException e) {
            e.printStackTrace();
        }



        /*
        START MOB REGISTER
         */

        if (!config.contains("mobs")) {
            config.createSection("mobs");
        }
        File mobFile = new File(this.getDataFolder(), "mobs.yml");

        if (!mobFile.exists()) {
            this.saveResource("mobs.yml", false);
        }
        YamlConfiguration mobs = new YamlConfiguration();
        sec = config.getConfigurationSection("mobs");
        if (sec == null) {
            sec = config.createSection("mobs");
        }
        try {
            mobs.load(mobFile);
            Set<String> keys = mobs.getKeys(true);
            Iterator i$ = keys.iterator();
            while(i$.hasNext()) {
                String key = (String)i$.next();
                sec.set(key, mobs.get(key));
            }
        } catch (Exception var5) {
            this.getLogger().severe("FAILED TO LOAD MOBS FILE");
            var5.printStackTrace();
            this.setEnabled(false);
            return;
        }
        ConfigurationSection mobsSection = config.getConfigurationSection("mobs");
        if (mobsSection == null) {
            this.getLogger().severe(ChatColor.RED.toString() + "MOBS CONFIG IS NULL!");
            this.setEnabled(false);
            return;
        }

        this.mobHandler = new MobHandler();
        try {
            this.mobHandler.registerMobs(this, mobsSection);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*
        START FISHING ZONE REGISTER
         */

        if (!config.contains("fishing-zones")) {
            config.createSection("fishing-zones");
        }
        File zoneFile = new File(this.getDataFolder(), "fishing-zones.yml");

        if (!zoneFile.exists()) {
            this.saveResource("fishing-zones.yml", false);
        }
        YamlConfiguration zones = new YamlConfiguration();
        sec = config.getConfigurationSection("fishing-zones");
        if (sec == null) {
            sec = config.createSection("fishing-zones");
        }
        try {
            zones.load(zoneFile);
            Set<String> keys = zones.getKeys(true);
            Iterator i$ = keys.iterator();
            while(i$.hasNext()) {
                String key = (String)i$.next();
                sec.set(key, zones.get(key));
            }
        } catch (Exception var5) {
            this.getLogger().severe("FAILED TO LOAD FISHING FILE");
            var5.printStackTrace();
            this.setEnabled(false);
            return;
        }
        ConfigurationSection zonesSection = config.getConfigurationSection("fishing-zones");
        if (zonesSection == null) {
            this.getLogger().severe(ChatColor.RED.toString() + "FISHING CONFIG IS NULL!");
            this.setEnabled(false);
            return;
        }

        this.fishingZoneHandler = new FishingZoneHandler();
        try {
            this.fishingZoneHandler.registerZones(this, zonesSection);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*
        START SPAWNER REGISTER
         */


        if (!config.contains("spawners")) {
            config.createSection("spawners");
        }
        File spawnerFile = new File(this.getDataFolder(), "spawners.yml");

        if (!spawnerFile.exists()) {
            this.saveResource("spawners.yml", false);
        }
        YamlConfiguration spawners = new YamlConfiguration();
        sec = config.getConfigurationSection("spawners");
        if (sec == null) {
            sec = config.createSection("spawners");
        }
        try {
            spawners.load(spawnerFile);
            Set<String> keys = spawners.getKeys(true);
            Iterator i$ = keys.iterator();
            while(i$.hasNext()) {
                String key = (String)i$.next();
                sec.set(key, spawners.get(key));
            }
        } catch (Exception var5) {
            this.getLogger().severe("FAILED TO LOAD SPAWNERS FILE");
            var5.printStackTrace();
            this.setEnabled(false);
            return;
        }
        ConfigurationSection spawnersSection = config.getConfigurationSection("spawners");
        if (spawnersSection == null) {
            this.getLogger().severe(ChatColor.RED.toString() + "SPAWNERS CONFIG IS NULL!");
            this.setEnabled(false);
            return;
        }

        this.spawnerHandler = new SpawnerHandler();
        try {
            this.spawnerHandler.registerSpawners(this, spawnersSection);
        } catch (IOException e) {
            e.printStackTrace();
        }



        if (!config.contains("npcs")) {
            config.createSection("npcs");
        }
        File npcFile = new File(this.getDataFolder(), "npcs.yml");

        if (!npcFile.exists()) {
            this.saveResource("npcs.yml", false);
        }
        YamlConfiguration npcs = new YamlConfiguration();
        sec = config.getConfigurationSection("npcs");
        if (sec == null) {
            sec = config.createSection("npcs");
        }
        try {
            npcs.load(npcFile);
            Set<String> keys = npcs.getKeys(true);
            Iterator i$ = keys.iterator();
            while(i$.hasNext()) {
                String key = (String)i$.next();
                sec.set(key, npcs.get(key));
            }
        } catch (Exception var5) {
            this.getLogger().severe("FAILED TO LOAD NPCS FILE");
            var5.printStackTrace();
            this.setEnabled(false);
            return;
        }
        ConfigurationSection npcsSection = config.getConfigurationSection("npcs");
        if (npcsSection == null) {
            this.getLogger().severe(ChatColor.RED.toString() + "NPC CONFIG IS NULL!");
            this.setEnabled(false);
            return;
        }
        //schematics load
        File skinAndSigFolderContents = new File(this.getDataFolder(), "npc-skins/");
        File skinAndSigDirectory = new File(this.getDataFolder(), "npc-skins");
        if (skinAndSigFolderContents.list().length == 0) {
            this.getLogger().severe("FAILED TO LOAD NPC SKINS");
            this.setEnabled(false);
            return;
        } else {
            this.npcHandler = new NPCHandler(skinAndSigFolderContents, skinAndSigDirectory);
            this.npcHandler.generate();
            try {
                this.npcHandler.registerNPCs(this, npcsSection);
                this.npcHandler.registerSubNPCs(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



        if (!config.contains("schematics")) {
            config.createSection("schematics");
        }
        File schematicFile = new File(this.getDataFolder(), "schematics.yml");
        if (!schematicFile.exists()) {
            this.saveResource("schematics.yml", false);
        }
        YamlConfiguration schematics = new YamlConfiguration();
        sec = config.getConfigurationSection("schematics");
        if (sec == null) {
            sec = config.createSection("schematics");
        }
        try {
            schematics.load(schematicFile);
            Set<String> keys = schematics.getKeys(true);
            Iterator i$ = keys.iterator();
            while(i$.hasNext()) {
                String key = (String)i$.next();
                sec.set(key, schematics.get(key));
            }
        } catch (Exception var5) {
            this.getLogger().severe("FAILED TO LOAD SCHEMATICS FILE");
            var5.printStackTrace();
            this.setEnabled(false);
            return;
        }

        ConfigurationSection linkedSchematicsSection = config.getConfigurationSection("schematics.linked-schematics");
        ConfigurationSection mainSchematicsSection = config.getConfigurationSection("schematics.main-schematics");
        if (linkedSchematicsSection == null || mainSchematicsSection == null) {
            this.getLogger().severe(ChatColor.RED.toString() + "SCHEMATIC CONFIG IS NULL!");
            this.setEnabled(false);
            return;
        }
        //schematics load
        File linkedSchematicFolderContents = new File(this.getDataFolder(), "linked_schematics/");
        File schematicFolderContents = new File(this.getDataFolder(), "schematics/");
        File schemDirectory = new File(this.getDataFolder(), "schematics");
        File linkedDirectory = new File(this.getDataFolder(), "linked_schematics");
        if (schematicFolderContents.list().length == 0) {
            this.getLogger().severe("FAILED TO LOAD SCHEMATICS");
            this.setEnabled(false);
            return;
        } else {
            this.schematicHandler = new SchematicHandler(schematicFolderContents, linkedSchematicFolderContents, schemDirectory, linkedDirectory);
            this.schematicHandler.generate();
            this.schematicHandler.registerLinkedSchematics(this, linkedSchematicsSection);
            this.schematicHandler.registerSchematics(this, mainSchematicsSection);
        }
        this.schematicCreator = new SchematicCreator(this);
        this.grid = new Grid(this.schematicHandler, this);
        this.listener = new EventListener(this);
        Bukkit.getPluginManager().registerEvents(this.listener, this);
        Bukkit.getPluginManager().registerEvents(this, this);
        Iterator iter = Bukkit.getOnlinePlayers().iterator();
        while (iter.hasNext()) {
            Player p = (Player) iter.next();
            this.players.put(p.getUniqueId(), this.createNewPlayer(p));
        }

        this.displayTask = Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            public void run() {
                TGT.this.displayStats();
            }
        }, (long)(10), (long)(10));

        this.healthRegenTask = Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            public void run() {
                TGT.this.healthRegen();
            }
        }, (long)(40), (long)(40));
        this.registerTrait();

        this.initializeScoreboard();
    }

    public void onLoad() {

    }
    private static TGT getImplementation() {
        if (plugin == null)
            throw new IllegalStateException("no implementation set");
        return plugin;
    }


    public void registerTrait() {
        if (getServer().getPluginManager().getPlugin("Citizens") == null || !getServer().getPluginManager().getPlugin("Citizens").isEnabled()) {
            getLogger().log(Level.SEVERE, "Citizens 2.0 not found or not enabled");
        } else {
            net.citizensnpcs.api.CitizensAPI.getTraitFactory().registerTrait(net.citizensnpcs.api.trait.TraitInfo.create(Quest.class).withName("quest"));
            net.citizensnpcs.api.CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(ItemRetrievalQuest.class).withName("retrieval"));
            net.citizensnpcs.api.CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(SlayerQuest.class).withName("slayer"));
        }
    }
    private void initializeScoreboard() {
        this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        this.objective = this.scoreboard.getObjective("Game");
        if (this.objective == null) {
            this.objective = this.scoreboard.registerNewObjective("Game", "Game");
            this.objective.setDisplayName(ChatColor.YELLOW.toString() + ChatColor.BOLD.toString() + "THE GRAND TOURNEY");
            this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        }

        this.mobTeam = this.scoreboard.getTeam("Mobs");
        if (this.mobTeam == null) {
            this.mobTeam = this.scoreboard.registerNewTeam("Mobs");
        }
        this.mobTeam.setPrefix(ChatColor.RED.toString());
        this.mobTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, NEVER);
        this.mobTeam.setAllowFriendlyFire(false);
        this.mobTeam.setNameTagVisibility(NameTagVisibility.NEVER);
    }

    private void sendScoreboard(Player player) {
        Scoreboard playerScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = playerScoreboard.getObjective("Player Scoreboard");
        if (obj == null) {
            obj = playerScoreboard.registerNewObjective("PlayerScoreboard", "Player Scoreboard");
            obj.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "THE GREAT TOURNAMENT");
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        }
        int counter = 1;
        Score websiteScore = obj.getScore(ChatColor.AQUA + "jimmysgym.net");
        websiteScore.setScore(counter);
        counter++;
        Score blankScore = obj.getScore(" ");
        blankScore.setScore(counter);
        counter++;
        if (players.get(player.getUniqueId()).currentGoal != null) {
            Score objectiveText = obj.getScore(ChatColor.YELLOW + players.get(player.getUniqueId()).currentGoal);
            objectiveText.setScore(counter);
            counter++;
            Score objectTitle = obj.getScore(ChatColor.WHITE + "Objective:");
            objectTitle.setScore(counter);
            counter++;
            Score blankScore2 = obj.getScore("  ");
            blankScore2.setScore(counter);
            counter++;
        }
        Score purseScore = obj.getScore(ChatColor.WHITE + "Purse: " + ChatColor.GOLD + players.get(player.getUniqueId()).getGold());
        purseScore.setScore(counter);
        counter++;
        Score blankScore3 = obj.getScore("   ");
        blankScore3.setScore(counter);
        counter++;
        Score locationScore = obj.getScore("  " + ChatColor.GRAY + "◆ " + getKingdomDistrictForScoreboard(player.getLocation()));
        locationScore.setScore(counter);
        counter++;
        Score timeScore = obj.getScore("  " + getTimeForScoreboard(player.getLocation()));
        timeScore.setScore(counter);
        counter++;


    }
    public String getTimeForScoreboard(Location loc) {
        long time = loc.getWorld().getTime();
        int hour = (int)(time / 1000) + 6000;
        if (hour > 24) {
            hour = hour - 24;
        }
        int minute =(int) (time - hour) * 3/50;
        String retString = "";
        if (hour == 24) {
            hour = hour -12;
            retString = ChatColor.GRAY.toString() + hour + ":" + minute + "am";
        } else if (hour > 11) {
            hour = hour - 12;
            retString = ChatColor.GRAY.toString() + hour + ":" + minute + "pm";
        } else {
            retString = ChatColor.GRAY.toString() + hour + ":" + minute + "am";
        }
        if (time >= 13500 && time <= 23500) {
            retString = ChatColor.AQUA + "☽" + ChatColor.RESET + retString;
        } else {
            retString = ChatColor.YELLOW + "☀" + ChatColor.RESET + retString;
        }
        return retString;
    }
    public String getKingdomDistrictForScoreboard(Location location) {
        if (this.grid == null) {
            return ChatColor.RED + "Kingdom";
        }
        Cell cell = getCellFromLocation(location);
        if (cell.district == District.PORT) {
            return ChatColor.AQUA + "Port";
        } else if (cell.district == District.ARISTOCRACY) {
            return ChatColor.GOLD + "Aristocracy";
        } else if (cell.district == District.SLUMS) {
            return ChatColor.GRAY + "Slums";
        } else if (cell.district == District.FARM) {
            return ChatColor.GREEN + "Farm";
        } else if (cell.district == District.OUTSKIRTS) {
            return ChatColor.YELLOW + "Outskirts";
        } else {
            return ChatColor.RED + "Kingdom";
        }

    }

    public Cell getCellFromLocation(Location loc) {
        if (this.grid == null) {
            return null;
        }
        int x = (int)this.grid.startX - ((int)loc.getX());
        int z = (int)this.grid.startZ - ((int)loc.getZ());
        return this.grid.grid[(x-(x%5))/5][(z-(z%5))/5];
    }
    public void addMonsterToMobs(Entity entity) {
        this.mobTeam.addEntity(entity);
    }
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("testgen")) {
            try {
                grid.initialize();
            } catch (DataException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (MaxChangedBlocksException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException | WorldEditException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (sender instanceof Player) {
                sender.sendMessage("Testing generation");
            }
            return true;
        } else if (command.getName().equalsIgnoreCase("checkcell")) {
            if (args.length < 2) {
                sender.sendMessage("You need an x and a z!");
            } else {
                sender.sendMessage(grid.schematicAtCell(Integer.parseInt(args[0]), Integer.parseInt(args[1])));
            }
        } else if (command.getName().equalsIgnoreCase("zap")) {
            if (getServer().getPluginManager().getPlugin("Citizens") == null || !getServer().getPluginManager().getPlugin("Citizens").isEnabled()) {
                getLogger().log(Level.SEVERE, "Citizens 2.0 not found or not enabled");
                return true;
            }
            NPC npc = this.registry.getById(Integer.parseInt(args[0]));
            if (npc.hasTrait(Quest.class)) {
                Quest trait = npc.getTrait(Quest.class);
                if (!trait.onRefusalCd.contains((OfflinePlayer)sender)) {
                    Step step = trait.steps.stream().filter(obj -> obj.stepName.equalsIgnoreCase(args[1])).findFirst().orElse(null);
                    if (args.length == 2) {
                        if (step.ranged) {
                            if (((Player)sender).getLocation().distanceSquared(npc.getEntity().getLocation()) <= 20) {
                                trait.zap((Player)sender, args[1], true);
                            }
                        } else {
                            trait.zap((Player)sender, args[1], true);
                        }
                    } else if (args.length >= 3) {
                        if (Boolean.parseBoolean(args[2])) {
                            if (step.ranged) {
                                if (((Player)sender).getLocation().distanceSquared(npc.getEntity().getLocation()) <= 20) {
                                    trait.zap((Player)sender, args[1], true);
                                }
                            } else {
                                trait.zap((Player)sender, args[1], true);
                            }
                        } else {
                            if (step.ranged) {
                                if (((Player)sender).getLocation().distanceSquared(npc.getEntity().getLocation()) <= 20) {
                                    trait.zap((Player)sender, args[1], false);
                                }
                            } else {
                                trait.zap((Player)sender, args[1], false);
                            }

                        }

                    }

                }

            } else if (npc.hasTrait(ItemRetrievalQuest.class)) {
                ItemRetrievalQuest trait = npc.getTrait(ItemRetrievalQuest.class);
                if (!trait.onRefusalCd.contains((OfflinePlayer)sender)) {
                    Step step = trait.steps.stream().filter(obj -> obj.stepName.equalsIgnoreCase(args[1])).findFirst().orElse(null);

                    if (args.length == 2) {
                        if (step.ranged) {
                            if (((Player)sender).getLocation().distanceSquared(npc.getEntity().getLocation()) <= 20) {
                                trait.zap((Player)sender, args[1], true);
                            }
                        } else {
                            trait.zap((Player)sender, args[1], true);
                        }
                    } else if (args.length >= 3) {
                        if (Boolean.parseBoolean(args[2])) {
                            if (step.ranged) {
                                if (((Player)sender).getLocation().distanceSquared(npc.getEntity().getLocation()) <= 20) {
                                    trait.zap((Player)sender, args[1], true);
                                }
                            } else {
                                trait.zap((Player)sender, args[1], true);
                            }
                        } else {
                            if (step.ranged) {
                                if (((Player)sender).getLocation().distanceSquared(npc.getEntity().getLocation()) <= 20) {
                                    trait.zap((Player)sender, args[1], false);
                                }
                            } else {
                                trait.zap((Player)sender, args[1], false);
                            }

                        }

                    }

                }

            } else if (npc.hasTrait(SlayerQuest.class)) {
                SlayerQuest trait = npc.getTrait(SlayerQuest.class);
                if (!trait.onRefusalCd.contains((OfflinePlayer)sender)) {
                    Step step = trait.steps.stream().filter(obj -> obj.stepName.equalsIgnoreCase(args[1])).findFirst().orElse(null);

                    if (args.length == 2) {
                        if (step.ranged) {
                            if (((Player)sender).getLocation().distanceSquared(npc.getEntity().getLocation()) <= 20) {
                                trait.zap((Player)sender, args[1], true);
                            }
                        } else {
                            trait.zap((Player)sender, args[1], true);
                        }
                    } else if (args.length >= 3) {
                        if (Boolean.parseBoolean(args[2])) {
                            if (step.ranged) {
                                if (((Player)sender).getLocation().distanceSquared(npc.getEntity().getLocation()) <= 20) {
                                    trait.zap((Player)sender, args[1], true);
                                }
                            } else {
                                trait.zap((Player)sender, args[1], true);
                            }
                        } else {
                            if (step.ranged) {
                                if (((Player)sender).getLocation().distanceSquared(npc.getEntity().getLocation()) <= 20) {
                                    trait.zap((Player)sender, args[1], false);
                                }
                            } else {
                                trait.zap((Player)sender, args[1], false);
                            }

                        }

                    }

                }

            }
        } else if (command.getName().equals("chain")) {
            if (sender instanceof Player) {
                Player player = (Player)sender;
                ChainCommand chain = new ChainCommand(args, player);
                chain.run();
            }

        } else if (command.getName().equalsIgnoreCase("tgtitem")) {
            if (args.length == 1) {
                if (sender instanceof Player) {
                    ((Player) sender).getInventory().addItem(this.itemHandler.getItemFromString(args[0]));
                    sender.sendMessage(ChatColor.GREEN + "Gave " + ChatColor.YELLOW + "1x " + this.itemHandler.getItemFromString(args[0]).getItemMeta().getDisplayName() + ChatColor.GREEN + " to " + ChatColor.YELLOW + sender + ChatColor.GREEN + ".");
                }
            } else if (args.length == 2) {
                if (sender instanceof Player && args[1].matches("^[0-9]+$")) {
                    ((Player) sender).getInventory().addItem(this.itemHandler.getItemFromString(args[0]).asQuantity(Integer.parseInt(args[1])));
                    sender.sendMessage(ChatColor.GREEN + "Gave " + ChatColor.YELLOW + args[1] + "x " + this.itemHandler.getItemFromString(args[0]).getItemMeta().getDisplayName() + ChatColor.GREEN + " to " + ChatColor.YELLOW + sender + ChatColor.GREEN + ".");

                }
            } else if (args.length == 3) {
                if (Bukkit.getPlayerExact(args[0]).isValid()) {
                    Bukkit.getPlayerExact(args[0]).getInventory().addItem(this.itemHandler.getItemFromString(args[1]).asQuantity(Integer.parseInt(args[2])));
                    sender.sendMessage(ChatColor.GREEN + "Gave " + ChatColor.YELLOW + args[2] + "x " + this.itemHandler.getItemFromString(args[1]).getItemMeta().getDisplayName() + ChatColor.GREEN + " to " + ChatColor.YELLOW + Bukkit.getPlayerExact(args[0]).getName() + ChatColor.GREEN + ".");
                }
            }
        } else if (command.getName().equalsIgnoreCase("conjure")) {
            if (args.length == 1) {
                if (sender instanceof Player) {
                    ((Player) sender).getInventory().addItem(this.itemHandler.getItemFromString(args[0]));
                }
            } else if (args.length == 2) {
                if (sender instanceof Player && args[1].matches("^[0-9]+$")) {
                    ((Player) sender).getInventory().addItem(this.itemHandler.getItemFromString(args[0]).asQuantity(Integer.parseInt(args[1])));

                }
            } else if (args.length == 3) {
                if (Bukkit.getPlayerExact(args[0]).isValid()) {
                    Bukkit.getPlayerExact(args[0]).getInventory().addItem(this.itemHandler.getItemFromString(args[1]).asQuantity(Integer.parseInt(args[2])));
                }
            }
        }  else if (command.getName().equalsIgnoreCase("spawn")) {
            if (args[0].equalsIgnoreCase("npc")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    spawnNpc(player, args);
                } else {
                    sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
                }
            } else if (args[0].equalsIgnoreCase("ore")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    spawnOre(player, args);
                } else {
                    sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
                }
            } else if (args[0].equalsIgnoreCase("spawner")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    spawnSpawner(player, args);
                } else {
                    sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
                }
            } else if (args[0].equalsIgnoreCase("crop")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    spawnCrop(player, args);
                } else {
                    sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
                }
            }


        } else if (command.getName().equalsIgnoreCase("saveschematic")) {
            if (sender instanceof Player) {
                if (args.length > 0) {
                    try {
                        this.schematicCreator.writeSchematic((Player)sender,args[0]);
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    } catch (EmptyClipboardException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "You must specify a schematic name!");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Only players can perform this command!");
            }
        } else if (command.getName().equalsIgnoreCase("fishingzone")) {
            if (sender instanceof Player) {
                if (args.length > 0) {
                    this.createFishingZone((Player)sender, args);
                } else {
                    sender.sendMessage(ChatColor.RED + "You must specify a fishing zone template name!");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Only players can perform this command!");
            }
        }
        return true;

    }
    public void spawnNpc(Player player, String[] args) {
        if (args.length > 1) {
            TGTNpc npc = this.npcHandler.allNpcs.stream().filter(obj -> obj.internalName.equalsIgnoreCase(args[1])).findFirst().orElse(null);
            if (npc != null) {
                Location loc = player.getLocation().toCenterLocation();
                this.npcLocationList.put(loc, npc);
                npc.npc.spawn(loc);
            }
        } else {
            player.sendMessage(ChatColor.RED + "You must specify a TGT NPC's internal name!");
        }
    }
    public void spawnOre(Player player, String[] args) {
        if (args.length > 1) {
            OreTemplate ore = this.oreHandler.allOres.stream().filter(obj -> obj.name.equalsIgnoreCase(args[1])).findFirst().orElse(null);
            if (ore != null) {
                Location loc = player.getLocation().toCenterLocation();
                Ore oreObj = new Ore(plugin, ore);
                Bukkit.getPluginManager().registerEvents(oreObj, this);
                this.oreLocationList.put(loc, oreObj);
                oreObj.spawn(loc);
            }
        } else {
            player.sendMessage(ChatColor.RED + "You must specify an Ore Template's internal name!");
        }
    }
    public void spawnCrop(Player player, String[] args) {
        if (args.length > 1) {
            CropTemplate crop = this.cropHandler.allCrops.stream().filter(obj -> obj.name.equalsIgnoreCase(args[1])).findFirst().orElse(null);
            if (crop != null) {
                Location loc = player.getLocation().toCenterLocation();
                Crop cropObj = new Crop(plugin, crop);
                Bukkit.getPluginManager().registerEvents(cropObj, this);
                this.cropLocationList.put(loc, cropObj);
                cropObj.spawn(loc, true);
            }
        } else {
            player.sendMessage(ChatColor.RED + "You must specify a Crop Template's internal name!");
        }
    }
    public void spawnSpawner(Player player, String[] args) {
        if (args.length > 1) {
            SpawnerTemplate spawner = this.spawnerHandler.allSpawners.stream().filter(obj -> obj.name.equalsIgnoreCase(args[1])).findFirst().orElse(null);
            if (spawner != null) {
                Location loc = player.getLocation().toCenterLocation();
                Spawner spawnerObj = new Spawner(plugin, spawner);
                this.spawnerLocationList.put(loc, spawnerObj);
                spawnerObj.spawn(loc);
            }
        } else {
            player.sendMessage(ChatColor.RED + "You must specify a Spawner Template's internal name!");
        }
    }

    public void createFishingZone(Player player, String[] args) {
        if (args.length > 0) {
            FishingZoneTemplate template = this.fishingZoneHandler.allZones.stream().filter(obj -> obj.name.equalsIgnoreCase(args[0])).findFirst().orElse(null);
            if (template != null) {
                FishingZone zone = new FishingZone(template, this);
                zone.createZone(player);

            }
        } else {
            player.sendMessage(ChatColor.RED + "You must specify a Fishing Zone Template's internal name!");
        }
    }

    public MMOPlayer createNewPlayer(Player player) {
        player.setHealth(player.getMaxHealth());
        MMOPlayer mmoPlayer = new MMOPlayer();
        UUID id = player.getUniqueId();
        mmoPlayer.setMinecraftUUID(id);
        mmoPlayer.setHealth(plugin.defaultStatValues.get("health"));
        mmoPlayer.setBaseHealth(mmoPlayer.getHealth());
        mmoPlayer.setMaxHealth(mmoPlayer.getHealth());
        mmoPlayer.setBaseMaxHealth(mmoPlayer.getHealth());
        mmoPlayer.setDefense(plugin.defaultStatValues.get("defense"));
        mmoPlayer.setBaseDefense(mmoPlayer.getDefense());
        mmoPlayer.setDamage(plugin.defaultStatValues.get("damage"));
        mmoPlayer.setBaseStrength(mmoPlayer.getDamage());
        mmoPlayer.setCritDamage(plugin.defaultStatValues.get("crit-damage"));
        mmoPlayer.setBaseCritDamage(mmoPlayer.getCritDamage());
        mmoPlayer.setSpeed(plugin.defaultStatValues.get("speed"));
        mmoPlayer.setBaseSpeed(mmoPlayer.getSpeed());
        mmoPlayer.setCritChance(plugin.defaultStatValues.get("crit-chance"));
        mmoPlayer.setBaseCritChance(mmoPlayer.getCritChance());
        mmoPlayer.setMaxMana(plugin.defaultStatValues.get("mana"));
        mmoPlayer.setBaseMaxMana(mmoPlayer.getMaxMana());
        mmoPlayer.setMana(mmoPlayer.getMaxMana());
        mmoPlayer.setAbilityDam(plugin.defaultStatValues.get("ability-damage"));
        mmoPlayer.setBaseAbilityDamage(mmoPlayer.getAbilityDam());
        mmoPlayer.setVendorPrice(plugin.defaultStatValues.get("shop-discount"));
        mmoPlayer.setBaseVendorPrice(mmoPlayer.getVendorPrice());
        mmoPlayer.setDialogueSpeed(plugin.defaultStatValues.get("dialogue-speed"));
        mmoPlayer.setBaseDialogueSpeed(mmoPlayer.getDialogueSpeed());
        mmoPlayer.setHealthRegen(plugin.defaultStatValues.get("health-regen"));
        mmoPlayer.setBaseHealthRegen(mmoPlayer.getHealthRegen());
        mmoPlayer.setManaRegen(plugin.defaultStatValues.get("mana-regen"));
        mmoPlayer.setBaseManaRegen(mmoPlayer.getManaRegen());
        mmoPlayer.setAttackSpeed(defaultStatValues.get("attack-speed"));
        mmoPlayer.setBaseAttackSpeed(mmoPlayer.getAttackSpeed());
        mmoPlayer.setLure(defaultStatValues.get("lure"));
        mmoPlayer.setBaseLure(mmoPlayer.getLure());
        mmoPlayer.setFlash(defaultStatValues.get("flash"));
        mmoPlayer.setBaseFlash(mmoPlayer.getFlash());
        MagicSpells.getManaHandler().setMaxMana(player, (int)mmoPlayer.getMaxMana());
        MagicSpells.getManaHandler().setMana(player, (int)mmoPlayer.getMaxMana(), ManaChangeReason.OTHER);
        mmoPlayer.setCombatLvl(1);
        mmoPlayer.setLoggingLvl(1);
        mmoPlayer.setMiningLvl(1);
        mmoPlayer.setFishingLvl(1);
        mmoPlayer.setFarmingLvl(1);
        mmoPlayer.setSmithingLvl(1);
        mmoPlayer.setAlchemyLvl(1);
        mmoPlayer.setCookingLvl(1);
        mmoPlayer.setCombatProg(0.0f);
        mmoPlayer.setMiningProg(0.0f);
        mmoPlayer.setFishingProg(0.0f);
        mmoPlayer.setLoggingProg(0.0f);
        mmoPlayer.setFarmingProg(0.0f);
        mmoPlayer.setSmithingProg(0.0f);
        mmoPlayer.setAlchemyProg(0.0f);
        mmoPlayer.setCookingProg(0.0f);
        mmoPlayer.setGold((double)plugin.defaultStatValues.get("gold"));
        return mmoPlayer;
    }

    public void displayStats() {
        Iterator iter = Bukkit.getOnlinePlayers().iterator();

        while (iter.hasNext()) {
            Player p = (Player) iter.next();
            if (players.containsKey(p.getUniqueId())) {
                plugin.updatePlayerMaxMana(plugin.players.get(p.getUniqueId()));
                plugin.updatePlayerSpeed(plugin.players.get(p.getUniqueId()));
                StatUpdates.updateFullInventory(p, plugin.players.get(p.getUniqueId()));
                p.sendActionBar(ChatColor.RED.toString() + (int)players.get(p.getUniqueId()).getHealth() + "/" + (int)players.get(p.getUniqueId()).getMaxHealth() + "❤    " + ChatColor.BLUE.toString() + (int)players.get(p.getUniqueId()).getDefense() + "❈ Defense    " + ChatColor.GREEN.toString() + this.ms.getManaHandler().getMana(p) + "/" + this.ms.getManaHandler().getMaxMana(p) + "⚡ Stamina");
            } else {
                this.getLogger().severe("PLAYER " + p.getName() + " HAS NO MMOPLAYER ENTRY");
            }
        }
    }
    public void healthRegen() {
        Iterator iter = Bukkit.getOnlinePlayers().iterator();
        while (iter.hasNext()) {
            Player p = (Player) iter.next();
            if (players.containsKey(p.getUniqueId())) {
                if (p.isDead()) {
                    return;
                }

                double healthToAdd = ((players.get(p.getUniqueId()).getMaxHealth() * 0.01) + 1.5) * (players.get(p.getUniqueId()).getHealthRegen() / 100);
                if (players.get(p.getUniqueId()).getHealth() + healthToAdd >= players.get(p.getUniqueId()).getMaxHealth()) {
                    players.get(p.getUniqueId()).setHealth(players.get(p.getUniqueId()).getMaxHealth());
                    return;
                }
                this.updatePlayerHealth(players.get(p.getUniqueId()), (float)healthToAdd);

            } else {
                this.getLogger().severe("PLAYER " + p.getName() + " HAS NO MMOPLAYER ENTRY");
            }
        }
    }
    public void updatePlayerHealth(MMOPlayer player, float change) {
        float newHealth = player.getHealth() + change;
        Player bukkitPlayer = Bukkit.getPlayer(player.getMinecraftUUID());
        if (bukkitPlayer.isDead()) {
            return;
        }
        if (newHealth <= 0) {
            player.setHealth(0.0f);
            bukkitPlayer.setHealth(0.0D);
            return;
        }
        if (newHealth > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
            bukkitPlayer.setHealth(bukkitPlayer.getMaxHealth());
            return;
        }

        player.setHealth(newHealth);
        bukkitPlayer.setHealth((player.getHealth()/player.getMaxHealth()) * bukkitPlayer.getMaxHealth());
    }

    public double calculateDamage(MMOPlayer player, ItemStack item, boolean isCrit) {

        Player bukkitPlayer = Bukkit.getPlayer(player.getMinecraftUUID());
        float damage = 1;
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
            Iterator iter = item.getItemMeta().getLore().iterator();
            while (iter.hasNext()) {
                String line = (String) iter.next();
                if (ChatColor.stripColor(line).startsWith("Damage: ")) {
                    line = ChatColor.stripColor(line);
                    line = line.replaceFirst("(Damage: +\\+)", "");
                    damage = Float.parseFloat(line);
                }
            }
        }
        double returnDam = (5+damage) * (1+(player.getDamage()/100));
        if (isCrit) {
            returnDam = returnDam * (1+player.getCritDamage()/100);
        }
        return returnDam;
    }

    public double calculateDamagePickaxe(MMOPlayer player, ItemStack item, boolean isCrit) {

        Player bukkitPlayer = Bukkit.getPlayer(player.getMinecraftUUID());
        float damage = 1;
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
            Iterator iter = item.getItemMeta().getLore().iterator();
            while (iter.hasNext()) {
                String line = (String) iter.next();
                if (ChatColor.stripColor(line).startsWith("Mining Damage: ")) {
                    line = ChatColor.stripColor(line);
                    line = line.replaceFirst("(Mining Damage: +\\+)", "");
                    damage = Float.parseFloat(line);
                }
            }
        }

        double returnDam = (5+damage) * (1+(player.getDamage()/100));
        if (isCrit) {
            returnDam = returnDam * (1+player.getCritDamage()/100);
        }
        return returnDam;
    }



    public void updatePlayerMaxMana(MMOPlayer player) {
        Player bukkitPlayer = Bukkit.getPlayer(player.getMinecraftUUID());
        MagicSpells.getManaHandler().setMaxMana(bukkitPlayer, (int) player.getMaxMana());
    }

    public void updatePlayerSpeed(MMOPlayer player) {
        Player bukkitPlayer = Bukkit.getPlayer(player.getMinecraftUUID());
        double moveSpeed = player.getSpeed() / 1000;
        if (moveSpeed >= 0.4) {
            moveSpeed = 0.4;
        }
        bukkitPlayer.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(moveSpeed);
        if (bukkitPlayer.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue() != moveSpeed) {

        }

    }



    public double calculateDefenseDamage(float defense, float damage) {
        return (damage * (1-((defense)/(defense+100))));
    }
}
