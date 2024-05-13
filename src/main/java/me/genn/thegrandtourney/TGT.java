package me.genn.thegrandtourney;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;


import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.mana.ManaChangeReason;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.MaxChangedBlocksException;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.world.DataException;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import io.lumine.mythic.bukkit.MythicBukkit;
import me.genn.gennsgym.GameMode;
import me.genn.gennsgym.GennsGym;
import me.genn.thegrandtourney.dungeons.*;
import me.genn.thegrandtourney.grid.Cell;
import me.genn.thegrandtourney.grid.District;
import me.genn.thegrandtourney.grid.Grid;
import me.genn.thegrandtourney.grid.SchematicHandler;
import me.genn.thegrandtourney.item.ItemHandler;
import me.genn.thegrandtourney.listener.EventListener;
import me.genn.thegrandtourney.menu.MenuManager;
import me.genn.thegrandtourney.mobs.MobHandler;
import me.genn.thegrandtourney.mobs.Spawner;
import me.genn.thegrandtourney.mobs.SpawnerHandler;
import me.genn.thegrandtourney.mobs.SpawnerTemplate;
import me.genn.thegrandtourney.npc.*;
import me.genn.thegrandtourney.player.ActionBarDisplay;
import me.genn.thegrandtourney.player.MMOPlayer;
import me.genn.thegrandtourney.player.StatUpdates;
import me.genn.thegrandtourney.shops.Shop;
import me.genn.thegrandtourney.shops.ShopHandler;
import me.genn.thegrandtourney.skills.*;
import me.genn.thegrandtourney.skills.farming.Crop;
import me.genn.thegrandtourney.skills.farming.CropHandler;
import me.genn.thegrandtourney.skills.farming.CropTemplate;
import me.genn.thegrandtourney.skills.fishing.FishingZone;
import me.genn.thegrandtourney.skills.fishing.FishingZoneHandler;
import me.genn.thegrandtourney.skills.fishing.FishingZoneTemplate;
import me.genn.thegrandtourney.skills.foraging.ForagingZone;
import me.genn.thegrandtourney.skills.foraging.ForagingZoneHandler;
import me.genn.thegrandtourney.skills.foraging.ForagingZoneTemplate;
import me.genn.thegrandtourney.skills.mining.Ore;
import me.genn.thegrandtourney.skills.mining.OreHandler;
import me.genn.thegrandtourney.skills.mining.OreTemplate;
import me.genn.thegrandtourney.tournament.MiniGame;
import me.genn.thegrandtourney.tournament.MiniGameListener;
import me.genn.thegrandtourney.tournament.MiniGameMonitor;
import me.genn.thegrandtourney.util.CasterSpeak;
import me.genn.thegrandtourney.util.SchematicCreator;
import me.genn.thegrandtourney.util.ToastMessage;
import me.genn.thegrandtourney.xp.RewardTableHandler;
import me.genn.thegrandtourney.xp.Xp;
import me.genn.thegrandtourney.xp.XpType;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.TraitInfo;
import net.kyori.adventure.bossbar.BossBar;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.io.FileUtils;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;

import static org.bukkit.scoreboard.Team.OptionStatus.NEVER;


public class TGT extends JavaPlugin implements Listener, GameMode {
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
    public Grid grid;
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
    public Map<Location, ForagingZone> foragingZoneLocList;
    public Map<Location, Dungeon> dungeonLocList;
    public List<FishingZone> fishingZoneList;

    public Economy econ;
    public MenuManager menus;

    public QuestHandler questHandler;
    public RewardTableHandler rewardsHandler;
    public Xp xpHandler;
    public Map<Player, BossBar> currentlyDisplayedBossBar;
    public TableHandler tableHandler;
    public Map<Location, Station> stationList;
    public StatUpdates statUpdates;

    public ForagingZoneHandler foragingZoneHandler;
    public Map<UUID, Room> playerAndDungeonRoom;
    public DungeonTemplateHandler dungeonHandler;
    public Map<UUID, Long> connectTime;
    public Long gameStartTime;
    public Long currentGameTime;
    public int dayLength = 600;
    public int nightLength = 300;
    boolean night = false;
    boolean gameRunning = false;
    boolean gameEnded = false;
    int daysBeforeTournament = 3;
    int day = 1;
    Random random = new Random();
    Map<String, MiniGame> games = new HashMap<>();
    List<MiniGame> gameList = new ArrayList<>();
    boolean override = false;
    int totalGameCount = 5;
    int gameCount = 0;
    public MiniGame nextGame = null;
    public MiniGame currentGame = null;
    public boolean tournament = false;
    List<MiniGame> selectedMiniGames;
    public Map<UUID, Integer> gameScore;
    public boolean awardsCeremony = false;
    boolean dayAnnouncement = false;
    public CasterSpeak gameCaster;
    public List<UUID> debugEnabled;

    public Map<UUID, District> lastDistrict;
    public ActionBarDisplay actionBarMessenger;

    public ShopHandler shopHandler;
    String[] statNames = new String[]{"strength", "defense","health","crit-damage","crit-chance","speed","vigor","stamina-regen","health-regen","ability-damage","mining-fortune","farming-fortune","foraging-fortune","fishing-speed","lure","sea-creature-chance","dialogue-speed","shop-discount","focus","stamina","absorption","evasiveness"};
    String[] potionNames = new String[]{"restoration", "adrenaline","agility","critical","dodge","spirit","regeneration","speed","strength","heal","absorption","resistance","spelunker","angler","harvester"};

    int autoStartTime = 90;
    int minPlayers = 4;
    int endTimerDuration = 300;

    public static TGT getInstance() {
        return plugin;
    }

    public void onEnable() {
        plugin = this;
        npcMap = new HashMap<>();
        menus = new MenuManager(this);
        this.pm = ProtocolLibrary.getProtocolManager();
        this.pluginManager = this.getServer().getPluginManager();
        this.oreLocationList = new HashMap<>();
        this.cropLocationList = new HashMap<>();
        this.npcLocationList = new HashMap<>();
        this.spawnerLocationList = new HashMap<>();
        this.fishingZoneList = new ArrayList<>();
        this.questHandler = new QuestHandler(this);
        this.xpHandler = new Xp(this);
        this.currentlyDisplayedBossBar = new HashMap<>();
        this.tableHandler = new TableHandler(this);
        this.stationList = new HashMap<>();
        this.statUpdates = new StatUpdates(this);
        this.foragingZoneLocList = new HashMap<>();
        this.dungeonLocList = new HashMap<>();
        this.playerAndDungeonRoom = new HashMap<>();
        this.connectTime = new HashMap<>();
        this.selectedMiniGames = new ArrayList<>();
        this.gameScore = new HashMap<>();
        this.gameList = new ArrayList<>();
        this.debugEnabled = new ArrayList<>();
        this.gameCaster = new CasterSpeak(this);
        defaultStatValues = new HashMap<>();
        this.lastDistrict = new HashMap<>();
        this.actionBarMessenger = new ActionBarDisplay(this);
        players = new HashMap<>();
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
        this.defaultStatValues.put("health", (float) defaultStats.getDouble("health",0.0));
        this.defaultStatValues.put("defense", (float) defaultStats.getDouble("defense",0.0));
        this.defaultStatValues.put("strength", (float) defaultStats.getDouble("strength",0.0));
        this.defaultStatValues.put("crit-damage", (float) defaultStats.getDouble("crit-damage",0.0));
        this.defaultStatValues.put("speed", (float) defaultStats.getDouble("speed",0.0));
        this.defaultStatValues.put("crit-chance", (float) defaultStats.getDouble("crit-chance",0.0));
        this.defaultStatValues.put("stamina", (float) defaultStats.getDouble("stamina",0.0));
        this.defaultStatValues.put("ability-damage", (float) defaultStats.getDouble("ability-damage",0.0));
        this.defaultStatValues.put("shop-discount", (float) defaultStats.getDouble("shop-discount",0.0));
        this.defaultStatValues.put("dialogue-speed", (float) defaultStats.getDouble("dialogue-speed",0.0));
        this.defaultStatValues.put("dosh", (float) defaultStats.getDouble("dosh", 0.0));
        this.defaultStatValues.put("health-regen", (float) defaultStats.getDouble("health-regen", 0.0f));
        this.defaultStatValues.put("stamina-regen", (float) defaultStats.getDouble("stamina-regen", 0.0f));
        this.defaultStatValues.put("fishing-speed", (float) defaultStats.getDouble("fishing-speed", 0.0f));
        this.defaultStatValues.put("lure", (float) defaultStats.getDouble("lure", 0.0f));
        this.defaultStatValues.put("vigor", (float) defaultStats.getDouble("vigor", 0.0f));
        this.defaultStatValues.put("mining-fortune", (float)defaultStats.getDouble("mining-fortune", 0.0f));
        this.defaultStatValues.put("foraging-fortune", (float)defaultStats.getDouble("foraging-fortune", 0.0f));
        this.defaultStatValues.put("farming-fortune", (float)defaultStats.getDouble("farming-fortune", 0.0f));
        this.defaultStatValues.put("focus", (float)defaultStats.getDouble("focus", 0.0f));
        this.dayLength = config.getInt("day-length", 600);
        this.nightLength = config.getInt("night-length", 300);
        this.daysBeforeTournament = config.getInt("days-before-tournament", 3);
        this.totalGameCount = config.getInt("tournament-games", 5);
        this.autoStartTime = config.getInt("auto-start-time", 0);
        this.minPlayers = config.getInt("min-players");
        this.endTimerDuration = config.getInt("end-timer-duration");

        Bukkit.getWorlds().get(0).setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        Bukkit.getWorlds().get(0).setGameRule(GameRule.DO_MOB_LOOT, false);
        Bukkit.getWorlds().get(0).setGameRule(GameRule.DO_MOB_SPAWNING, false);
        Bukkit.getWorlds().get(0).setGameRule(GameRule.NATURAL_REGENERATION, false);
        List<String> blacklistedCells = new ArrayList<>();
        if (config.contains("blacklisted-cells")) {
            blacklistedCells.addAll(config.getStringList("blacklisted-cells"));
        }
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

        this.itemHandler = new ItemHandler(this);
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
        START FORAGING ZONE REGISTER
         */

        if (!config.contains("foraging-zones")) {
            config.createSection("foraging-zones");
        }
        File foragingZonesFile = new File(this.getDataFolder(), "foraging-zones.yml");

        if (!foragingZonesFile.exists()) {
            this.saveResource("foraging-zones.yml", false);
        }
        YamlConfiguration foragingZones = new YamlConfiguration();
        sec = config.getConfigurationSection("foraging-zones");
        if (sec == null) {
            sec = config.createSection("foraging-zones");
        }
        try {
            foragingZones.load(foragingZonesFile);
            Set<String> keys = foragingZones.getKeys(true);
            Iterator i$ = keys.iterator();
            while(i$.hasNext()) {
                String key = (String)i$.next();
                sec.set(key, foragingZones.get(key));
            }
        } catch (Exception var5) {
            this.getLogger().severe("FAILED TO LOAD FORAGING ZONES FILE");
            var5.printStackTrace();
            this.setEnabled(false);
            return;
        }
        ConfigurationSection foragingZonesSection = config.getConfigurationSection("foraging-zones");
        if (foragingZonesSection == null) {
            this.getLogger().severe(ChatColor.RED.toString() + "FORAGING CONFIG IS NULL!");
            this.setEnabled(false);
            return;
        }

        this.foragingZoneHandler = new ForagingZoneHandler();
        try {
            this.foragingZoneHandler.registerZones(this, foragingZonesSection);
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
        
        /*
        START REWARD TABLE REGISTER
         */
        

        if (!config.contains("level-rewards")) {
            config.createSection("level-rewards");
        }
        File levelRewardsFile = new File(this.getDataFolder(), "level-rewards.yml");

        if (!levelRewardsFile.exists()) {
            this.saveResource("level-rewards.yml", false);
        }
        YamlConfiguration levelRewards = new YamlConfiguration();
        sec = config.getConfigurationSection("level-rewards");
        if (sec == null) {
            sec = config.createSection("level-rewards");
        }
        try {
            levelRewards.load(levelRewardsFile);
            Set<String> keys = levelRewards.getKeys(true);
            Iterator i$ = keys.iterator();
            while(i$.hasNext()) {
                String key = (String)i$.next();
                sec.set(key, levelRewards.get(key));
            }
        } catch (Exception var5) {
            this.getLogger().severe("FAILED TO LOAD LEVEL REWARDS FILE");
            var5.printStackTrace();
            this.setEnabled(false);
            return;
        }
        ConfigurationSection levelRewardsSection = config.getConfigurationSection("level-rewards");
        if (levelRewardsSection == null) {
            this.getLogger().severe(ChatColor.RED.toString() + "LEVEL REWARDS CONFIG IS NULL!");
            this.setEnabled(false);
            return;
        }

        this.rewardsHandler = new RewardTableHandler();
        try {
            this.rewardsHandler.registerRewardTables(levelRewardsSection);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        /*
        START NPC REGISTER
         */



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
            this.npcHandler = new NPCHandler(skinAndSigFolderContents, skinAndSigDirectory, this);
            this.npcHandler.generate();
            try {
                this.npcHandler.registerNPCs(this, npcsSection);
                this.npcHandler.registerSubNPCs(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

         /*
        START DUNGEON REGISTER
         */


        if (!config.contains("dungeons")) {
            config.createSection("dungeons");
        }
        File dungeonsFile = new File(this.getDataFolder(), "dungeons.yml");

        if (!dungeonsFile.exists()) {
            this.saveResource("dungeons.yml", false);
        }
        YamlConfiguration dungeons = new YamlConfiguration();
        sec = config.getConfigurationSection("dungeons");
        if (sec == null) {
            sec = config.createSection("dungeons");
        }
        try {
            dungeons.load(dungeonsFile);
            Set<String> keys = dungeons.getKeys(true);
            Iterator i$ = keys.iterator();
            while(i$.hasNext()) {
                String key = (String)i$.next();
                sec.set(key, dungeons.get(key));
            }
        } catch (Exception var5) {
            this.getLogger().severe("FAILED TO LOAD DUNGEONS FILE");
            var5.printStackTrace();
            this.setEnabled(false);
            return;
        }
        ConfigurationSection dungeonsSection = config.getConfigurationSection("dungeons");
        if (dungeonsSection == null) {
            this.getLogger().severe(ChatColor.RED.toString() + "DUNGEONS CONFIG IS NULL!");
            this.setEnabled(false);
            return;
        }

        this.dungeonHandler = new DungeonTemplateHandler();
        try {
            this.dungeonHandler.registerTemplates(plugin, dungeonsSection);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*
        MINIGAME REGISTER
         */

        if (!config.contains("games")) {
            config.createSection("games");
        }
        File gamesFile = new File(this.getDataFolder(), "games.yml");

        if (!gamesFile.exists()) {
            this.saveResource("games.yml", false);
        }
        YamlConfiguration games = new YamlConfiguration();
        sec = config.getConfigurationSection("games");
        if (sec == null) {
            sec = config.createSection("games");
        }
        try {
            games.load(gamesFile);
            Set<String> keys = games.getKeys(true);
            Iterator i$ = keys.iterator();
            while(i$.hasNext()) {
                String key = (String)i$.next();
                sec.set(key, games.get(key));
            }
        } catch (Exception var5) {
            this.getLogger().severe("FAILED TO LOAD GAMES FILE");
            var5.printStackTrace();
            this.setEnabled(false);
            return;
        }
        ConfigurationSection gamesSection = config.getConfigurationSection("games");
        if (gamesSection == null) {
            this.getLogger().severe(ChatColor.RED.toString() + "GAMES CONFIG IS NULL!");
            this.setEnabled(false);
            return;
        }

        Iterator var4 = gamesSection.getKeys(false).iterator();
        while(var4.hasNext()) {
            String key = (String)var4.next();
            if (config.getBoolean("games." + key + ".enabled", true)) {
                MiniGame game = new MiniGame(this, key, config.getConfigurationSection("games." + key));
                this.games.put(key, game);
                this.gameList.add(game);

            }
        }

        this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        this.chooseGames();
        Bukkit.broadcastMessage("Games list size is " + this.selectedMiniGames.size());
        this.chooseNextGame();
        this.getServer().getPluginManager().registerEvents(new MiniGameListener(this), this);
        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new MiniGameMonitor(this), 20L, 20L);
        GennsGym.initializeGameMode(this);
        GennsGym.setServerStatus("The Tournament");



        /*
        SCHEMATIC REGISTER
         */
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
        this.grid.registerBlackList(blacklistedCells);
        this.grid.oceanXMin = config.getInt("ocean-fishing-min-x",0);
        this.grid.oceanXMax = config.getInt("ocean-fishing-max-x",640);
        this.grid.oceanZMin = config.getInt("ocean-fishing-min-z", -46);
        this.grid.oceanZMax = config.getInt("ocean-fishing-min", -960);

        /*
        SHOP REGISTER
         */

        if (!config.contains("shops")) {
            config.createSection("shops");
        }
        File shopsFile = new File(this.getDataFolder(), "shops.yml");

        if (!shopsFile.exists()) {
            this.saveResource("shops.yml", false);
        }
        YamlConfiguration shops = new YamlConfiguration();
        sec = config.getConfigurationSection("shops");
        if (sec == null) {
            sec = config.createSection("shops");
        }
        try {
            shops.load(shopsFile);
            Set<String> keys = shops.getKeys(true);
            Iterator i$ = keys.iterator();
            while(i$.hasNext()) {
                String key = (String)i$.next();
                sec.set(key, shops.get(key));
            }
        } catch (Exception var5) {
            this.getLogger().severe("FAILED TO LOAD SHOPS FILE");
            var5.printStackTrace();
            this.setEnabled(false);
            return;
        }
        ConfigurationSection shopSection = config.getConfigurationSection("shops");
        if (shopSection == null) {
            this.getLogger().severe(ChatColor.RED.toString() + "SHOPS CONFIG IS NULL!");
            this.setEnabled(false);
            return;
        }

        this.shopHandler = new ShopHandler(this);
        this.shopHandler.registerShops(shopSection);

        /*

         */


        this.listener = new EventListener(this);
        if (!this.setupEconomy()) {
            this.getLogger().severe("FAILED TO LOAD LINK TO VAULT");
            this.getLogger().severe("FAILED TO LOAD LINK TO VAULT");
            this.getLogger().severe("FAILED TO LOAD LINK TO VAULT");
            this.setEnabled(false);
            return;
        }
        Bukkit.getPluginManager().registerEvents(this.listener, this);
        Bukkit.getPluginManager().registerEvents(this, this);
        Iterator iter = Bukkit.getOnlinePlayers().iterator();
        while (iter.hasNext()) {
            Player p = (Player) iter.next();
            this.players.put(p.getUniqueId(), this.createNewPlayer(p));
        }



        this.healthRegenTask = Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            public void run() {
                TGT.this.healthRegen();
            }
        }, (long)(40), (long)(40));
        this.registerTrait();
        GennsGym.initializeGameMode(this);
        this.initializeScoreboard();
        if (this.autoStartTime > 0) {
            GennsGym.startCountdown(this.autoStartTime, this.minPlayers);
        }
    }


    private int parseInt(String string) {
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException var3) {
            return 0;
        }
    }
    private float parseFloat(String string) {
        try {
            return Float.parseFloat(string);
        } catch (NumberFormatException var3) {
            return 0;
        }
    }
    public void nextGame() {
        //this.showMainScoreboard();
        this.currentGame = this.nextGame;
        if (this.currentGame != null) {
            this.getLogger().info("Game " + this.currentGame.name + " will begin in 10 seconds");
            Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                public void run() {
                    TGT.this.currentGame.initialize();
                }
            }, 100L);
            this.chooseNextGame();
        } else {
            Bukkit.broadcastMessage("END OF GAME!");
            this.awardsCeremony = true;
            Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                public void run() {
                    performAwardsCeremony();
                }
            }, 20L);
        }

    }
    void performAwardsCeremony() {
        Bukkit.broadcastMessage(ChatColor.WHITE + "<" + ChatColor.YELLOW + "Herald" + ChatColor.WHITE + "> That concludes" + ChatColor.GOLD + " The Tournament" + ChatColor.WHITE + ", let's tabulate the scores!");
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), "entity.villager.trade", 5.0f, 1.25f);
        }
        new BukkitRunnable(){

            @Override
            public void run() {
                Bukkit.broadcastMessage(ChatColor.WHITE + "<" + ChatColor.GOLD + "King Posh" + ChatColor.WHITE + "> Please do hurry Herald, the anticipation is killing me!");
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.playSound(player.getLocation(), "entity.villager.celebrate", 5.0f, 0.75f);
                }
            }
        }.runTaskLater(this, 100L);
        new BukkitRunnable(){

            @Override
            public void run() {
                Bukkit.broadcastMessage(ChatColor.WHITE + "<" + ChatColor.YELLOW + "Herald" + ChatColor.WHITE + "> Done! And without further ado, here are the results...");
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.playSound(player.getLocation(), "entity.villager.trade", 5.0f, 1.25f);
                }
            }
        }.runTaskLater(this, 150L);
        new BukkitRunnable(){

            @Override
            public void run() {
                runResults();
            }
        }.runTaskLater(this, 200L);
    }
    void runResults() {
        final int[] numberOfPlayers = {this.gameScore.size()};
        Map<UUID, Integer> sortedList = sortByValue(this.gameScore);
        List<UUID> idsInOrder = new ArrayList<>(sortedList.keySet());
        new BukkitRunnable(){

            @Override
            public void run() {
                if (numberOfPlayers[0] == 1){
                    Bukkit.broadcastMessage(ChatColor.WHITE + "<" + ChatColor.YELLOW + "Herald" + ChatColor.WHITE + "> And finally, coming in at " + ChatColor.GOLD + "1st" + ChatColor.WHITE + " Place...");
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.playSound(player.getLocation(), "entity.villager.trade", 5.0f, 1.25f);
                    }
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Bukkit.broadcastMessage(ChatColor.WHITE + "<" + ChatColor.GOLD + "King Posh" + ChatColor.WHITE + "> " + ChatColor.YELLOW + Bukkit.getPlayer(idsInOrder.get(0)).getName() + ChatColor.WHITE + " with " + ChatColor.YELLOW + sortedList.get(idsInOrder.get(0)) + ChatColor.WHITE + " points!" );
                            idsInOrder.remove(0);
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                player.playSound(player.getLocation(), "entity.villager.celebrate", 5.0f, 0.75f);
                            }
                        }
                    }.runTaskLater(TGT.this, 20L);
                    this.cancel();
                    return;
                } else {
                    if (numberOfPlayers[0] == 2) {
                        Bukkit.broadcastMessage(ChatColor.WHITE + "<" + ChatColor.YELLOW + "Herald" + ChatColor.WHITE + "> Coming in at " + ChatColor.GRAY + "2nd" + ChatColor.WHITE + " Place...");
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            player.playSound(player.getLocation(), "entity.villager.trade", 5.0f, 1.25f);
                        }
                        new BukkitRunnable() {

                            @Override
                            public void run() {
                                Bukkit.broadcastMessage(ChatColor.WHITE + "<" + ChatColor.YELLOW + "Herald" + ChatColor.WHITE + "> " + ChatColor.YELLOW + Bukkit.getPlayer(idsInOrder.get(0)).getName() + ChatColor.WHITE + " with " + ChatColor.YELLOW + sortedList.get(idsInOrder.get(0)) + ChatColor.WHITE + " points!" );
                                idsInOrder.remove(0);
                                for (Player player : Bukkit.getOnlinePlayers()) {
                                    player.playSound(player.getLocation(), "entity.villager.trade", 5.0f, 1.25f);
                                }
                            }
                        }.runTaskLater(TGT.this, 20L);
                    } else if (numberOfPlayers[0] == 3) {
                        Bukkit.broadcastMessage(ChatColor.WHITE + "<" + ChatColor.YELLOW + "Herald" + ChatColor.WHITE + "> Coming in at " + ChatColor.RED + "3rd" + ChatColor.WHITE + " Place...");
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            player.playSound(player.getLocation(), "entity.villager.trade", 5.0f, 1.25f);
                        }
                        new BukkitRunnable() {

                            @Override
                            public void run() {
                                Bukkit.broadcastMessage(ChatColor.WHITE + "<" + ChatColor.YELLOW + "Herald" + ChatColor.WHITE + "> " + ChatColor.YELLOW + Bukkit.getPlayer(idsInOrder.get(0)).getName() + ChatColor.WHITE + " with " + ChatColor.YELLOW + sortedList.get(idsInOrder.get(0)) + ChatColor.WHITE + " points!" );
                                idsInOrder.remove(0);
                                for (Player player : Bukkit.getOnlinePlayers()) {
                                    player.playSound(player.getLocation(), "entity.villager.trade", 5.0f, 1.25f);
                                }
                            }
                        }.runTaskLater(TGT.this, 20L);
                    } else {
                        Bukkit.broadcastMessage(ChatColor.WHITE + "<" + ChatColor.YELLOW + "Herald" + ChatColor.WHITE + "> Coming in at " + ChatColor.WHITE + numberOfPlayers[0] + "th" + ChatColor.WHITE + " Place...");
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            player.playSound(player.getLocation(), "entity.villager.trade", 5.0f, 1.25f);
                        }
                        new BukkitRunnable() {

                            @Override
                            public void run() {
                                Bukkit.broadcastMessage(ChatColor.WHITE + "<" + ChatColor.YELLOW + "Herald" + ChatColor.WHITE + "> " + ChatColor.YELLOW + Bukkit.getPlayer(idsInOrder.get(0)).getName() + ChatColor.WHITE + " with " + ChatColor.YELLOW + sortedList.get(idsInOrder.get(0)) + ChatColor.WHITE + " points!" );
                                idsInOrder.remove(0);
                                for (Player player : Bukkit.getOnlinePlayers()) {
                                    player.playSound(player.getLocation(), "entity.villager.trade", 5.0f, 1.25f);
                                }
                            }
                        }.runTaskLater(TGT.this, 20L);
                    }
                    numberOfPlayers[0]--;
                }
            }
        }.runTaskTimer(this, 20L, 120L);


    }
    void showMainScoreboard() {
        List<Player> var4 = new ArrayList<>(Bukkit.getOnlinePlayers());
        int var3 = (var4).size();

        for(int var2 = 0; var2 < var3; ++var2) {
            Player player = var4.get(var2);
            player.setScoreboard(this.scoreboard);
        }

    }
    void chooseGames() {
        for (int i = 0; i < this.totalGameCount; i++) {
            if (this.gameList.size() > 0) {
                int index = this.random.nextInt(this.gameList.size());
                this.selectedMiniGames.add(this.gameList.remove(index));
            }
        }
    }
    void chooseNextGame() {
        ++this.gameCount;
        if (this.gameCount < this.totalGameCount && this.selectedMiniGames.size() > 0) {
            this.nextGame = this.selectedMiniGames.remove(this.random.nextInt(this.selectedMiniGames.size()));
        } else {
            this.nextGame = null;
        }

    }
    public MiniGame getCurrentGame() {
        return this.currentGame;
    }
    public void copyFile(File fileToCopy, File folder) {
        try {
            File target = new File(folder, fileToCopy.getName());
            if (target.exists()) {
                target.delete();
            }

            FileUtils.copyFileToDirectory(fileToCopy, folder);
        } catch (Exception var4) {
            this.getLogger().severe("FAILED TO COPY FILE: " + fileToCopy.getAbsolutePath());
            var4.printStackTrace();
        }

    }
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
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
            net.citizensnpcs.api.CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(StationMaster.class).withName("station-master"));
        }
    }
    private void initializeScoreboard() {
        this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        this.objective = this.scoreboard.getObjective("Game");
        if (this.objective == null) {
            this.objective = this.scoreboard.registerNewObjective("Game", "Game");
            this.objective.setDisplayName(ChatColor.YELLOW.toString() + ChatColor.BOLD.toString() + "THE TOURNAMENT");
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


    public String formatProgressName(MMOPlayer mmoPlayer, Craft craft) {
        String retString = ChatColor.DARK_GRAY + "[";
        int num = 12;
        float percentUnfilled = (int) (12 * ((craft.totalCraftScore-craft.totalProgress)/craft.totalCraftScore));
        for (int i = 0; i < (12-percentUnfilled); i++) {
            if (i < 3) {
                retString = retString.concat(ChatColor.RED + "|");
            } else if (i < 6) {
                retString = retString.concat( ChatColor.GOLD + "|");
            } else if (i < 9) {
                retString = retString.concat( ChatColor.YELLOW + "|");
            } else {
                retString = retString.concat( ChatColor.GREEN + "|");
            }
        }
        for (int i = 0; i <percentUnfilled; i++) {
            retString += ChatColor.GRAY + "|";
        }
        retString = retString + ChatColor.DARK_GRAY + "]";
        return retString;
    }

    private void sendScoreboard(Player player) {
        if (!gameRunning || awardsCeremony) {
            return;
        }
        Scoreboard playerScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = playerScoreboard.getObjective("Player Scoreboard");
        if (obj == null) {
            obj = playerScoreboard.registerNewObjective("PlayerScoreboard", "Player Scoreboard");
            obj.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "THE TOURNAMENT");
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        }
        int counter = 1;
        Score websiteScore = obj.getScore(ChatColor.DARK_AQUA + ChatColor.BOLD.toString() + "jimmysgym.net");
        websiteScore.setScore(counter);
        counter++;
        Score blankScore = obj.getScore(" ");
        blankScore.setScore(counter);
        counter++;
        if (!tournament) {
            if (players.get(player.getUniqueId()).currentCraftObj != null) {
                Score objectiveText = obj.getScore("  " + formatProgressName(players.get(player.getUniqueId()), players.get(player.getUniqueId()).currentCraftObj));
                objectiveText.setScore(counter);
                counter++;
                double progress = (players.get(player.getUniqueId()).currentCraftObj.totalCraftScore-players.get(player.getUniqueId()).currentCraftObj.totalProgress)/players.get(player.getUniqueId()).currentCraftObj.totalCraftScore;
                Score objectTitle = obj.getScore(ChatColor.WHITE + "Craft Progress: "  + ChatColor.YELLOW.toString() + String.format("%,.1f", 100-(progress*100)) + "%");
                objectTitle.setScore(counter);
                counter++;
                Score blankScore2 = obj.getScore("  ");
                blankScore2.setScore(counter);
                counter++;
            } else if (players.get(player.getUniqueId()).trackedObjective != null && !players.get(player.getUniqueId()).trackedObjective.completed) {
                MMOPlayer mmoPlayer = players.get(player.getUniqueId());
                String fullStr = mmoPlayer.trackedObjective.trackingText;
                List<String> strings = splitStringToLinedString(fullStr, 24);
                for (int i = strings.size()-1; i >= 0; i--) {
                    Score lineScore = obj.getScore(ChatColor.YELLOW + ChatColor.stripColor(strings.get(i)));
                    lineScore.setScore(counter);
                    counter++;
                }
                Score objScore = obj.getScore(ChatColor.WHITE + "Objective");
                objScore.setScore(counter);
                counter++;
            }

            Score purseScore = obj.getScore(ChatColor.WHITE + "Purse: " + ChatColor.GOLD + players.get(player.getUniqueId()).getPurseGold());
            purseScore.setScore(counter);
            counter++;
            Score blankScore3 = obj.getScore("   ");
            blankScore3.setScore(counter);
            counter++;
            this.lastDistrict.putIfAbsent(player.getUniqueId(), District.getDistrict(getKingdomDistrictForScoreboard(player.getLocation())));
            String locationString = " " + ChatColor.GRAY + "◆ " + getKingdomDistrictForScoreboard(player.getLocation());
            Score locationScore = obj.getScore(locationString);
            locationScore.setScore(counter);
            if (this.lastDistrict.get(player.getUniqueId()) != District.getDistrict(getKingdomDistrictForScoreboard(player.getLocation()))) {
                this.actionBarMessenger.queueLocationMessage(player, locationString.trim());
            }
            this.lastDistrict.put(player.getUniqueId(), District.getDistrict(getKingdomDistrictForScoreboard(player.getLocation())));
            counter++;
            Score timeScore = obj.getScore(" " + getTimeForScoreboard(player.getLocation()));
            timeScore.setScore(counter);
            counter++;
            Score timeProgress = obj.getScore(ChatColor.WHITE + " " + getGeneralTimeString(player.getLocation()));
            timeProgress.setScore(counter);
            counter++;
            Score blankScore4 = obj.getScore("    ");
            blankScore4.setScore(counter);

        } else {
            if (this.currentGame == null || !this.currentGame.gameRunning) {

                Score purseScore = obj.getScore(ChatColor.WHITE + "Purse: " + ChatColor.GOLD + players.get(player.getUniqueId()).getPurseGold());
                purseScore.setScore(counter);
                counter++;

                Score blankScore2 = obj.getScore("   ");
                blankScore2.setScore(counter);
                counter++;

                counter = formatTournamentScoresBlock(player, obj, counter);


                Score scoresTitle = obj.getScore(ChatColor.GOLD + ChatColor.BOLD.toString() + "Tournament Scores:");
                scoresTitle.setScore(counter);
                counter++;

                Score blankScore3 = obj.getScore("    ");
                blankScore3.setScore(counter);
                counter++;
                if (currentGame.gameStartTime - System.currentTimeMillis() > 0) {
                    Score prepTimeRemaining = obj.getScore(ChatColor.RED + ChatColor.BOLD.toString() + "Round Begins: " + ChatColor.RESET + ChatColor.WHITE + formatRemainingGameTime(currentGame.gameStartTime - System.currentTimeMillis()));
                    prepTimeRemaining.setScore(counter);
                    counter++;
                }

                Score nextGameScore = obj.getScore(ChatColor.GREEN + ChatColor.BOLD.toString() + "Next Game: " + ChatColor.RESET + ChatColor.stripColor(this.currentGame.name));
                nextGameScore.setScore(counter);
                counter++;
                if (this.gameCount-1 >= this.totalGameCount) {
                    Score finalGameScore = obj.getScore(ChatColor.YELLOW + ChatColor.BOLD.toString() + "Final Round");
                    finalGameScore.setScore(counter);
                    counter++;
                } else {
                    Score remainingGameScore = obj.getScore(ChatColor.YELLOW +ChatColor.BOLD.toString() + "Game" + " " + (this.gameCount-1) + "/" + this.totalGameCount );
                    remainingGameScore.setScore(counter);
                    counter++;
                }
                Score blankScore4 = obj.getScore("     ");
                blankScore4.setScore(counter);
                counter++;

            } else {
                if (this.currentGame.type == MiniGame.MiniGameType.LAST_MAN_STANDING) {
                    MiniGame game = this.currentGame;

                    Score playersAliveScore = obj.getScore(ChatColor.GREEN + ChatColor.BOLD.toString() + "Players Alive: " + ChatColor.RESET + ChatColor.WHITE + game.participants.size() + "/" + game.playerScores.size());
                    playersAliveScore.setScore(counter);
                    counter++;

                    if (game.teamsEnabled) {

                        Score teamsAliveScore = obj.getScore(ChatColor.GREEN + ChatColor.BOLD.toString() + "Teams Alive: " + ChatColor.RESET + ChatColor.WHITE + game.getRemainingTeamNames().size() + "/" + game.teamNames.size());
                        teamsAliveScore.setScore(counter);
                        counter++;

                        Score playerTeam = obj.getScore(ChatColor.YELLOW + ChatColor.BOLD.toString() + "Your Team: " + ChatColor.RESET + game.scoreboard.getTeam(game.playerTeams.get(player.getName())).getPrefix() + game.playerTeams.get(player.getName()));
                        playerTeam.setScore(counter);
                        counter++;


                        Score blankScore1point5 = obj.getScore("        ");
                        blankScore1point5.setScore(counter);
                        counter++;

                        counter = formatTeamRoundScoresBlock(player, obj, counter, game);

                        String roundScoreTitleString = ChatColor.GOLD + ChatColor.BOLD.toString() + "Round Score: ";
                        if (game.multiplier != 1.0f) {
                            roundScoreTitleString = roundScoreTitleString + ChatColor.RESET + ChatColor.WHITE + "(" + ChatColor.YELLOW + "x" + String.format("%.1f", game.multiplier) + ChatColor.WHITE + ")";
                        }
                        Score roundScoreTitle = obj.getScore(roundScoreTitleString);
                        roundScoreTitle.setScore(counter);
                        counter++;

                        Score blankScore2 = obj.getScore("   ");
                        blankScore2.setScore(counter);
                        counter++;

                        Score timeRemaining = obj.getScore(ChatColor.RED + ChatColor.BOLD.toString() + "Time Remaining" + ChatColor.WHITE + ": " + formatRemainingGameTime(game.endTime - System.currentTimeMillis()));
                        timeRemaining.setScore(counter);
                        counter++;
                        Score gameName = obj.getScore(ChatColor.YELLOW + ChatColor.BOLD.toString() + "Game " + (this.gameCount-1) + "/" + this.totalGameCount + ": " + ChatColor.RESET + game.name);
                        gameName.setScore(counter);
                        counter++;
                        Score blankScore3 = obj.getScore("    ");
                        blankScore3.setScore(counter);
                        counter++;
                    } else {
                        Score blankScore1point5 = obj.getScore("        ");
                        blankScore1point5.setScore(counter);
                        counter++;

                        counter = formatIndividualRoundScoresBlock(player, obj, counter, game);

                        String roundScoreTitleString = ChatColor.GOLD + ChatColor.BOLD.toString() + "Round Score: ";
                        if (game.multiplier != 1.0f) {
                            roundScoreTitleString = roundScoreTitleString + ChatColor.RESET + ChatColor.WHITE + "(" + ChatColor.YELLOW + "x" + String.format("%.1f", game.multiplier) + ChatColor.WHITE + ")";
                        }
                        Score roundScoreTitle = obj.getScore(roundScoreTitleString);
                        roundScoreTitle.setScore(counter);
                        counter++;

                        Score blankScore2 = obj.getScore("   ");
                        blankScore2.setScore(counter);
                        counter++;

                        Score timeRemaining = obj.getScore(ChatColor.RED + ChatColor.BOLD.toString() + "Time Remaining: " + ChatColor.WHITE + formatRemainingGameTime(game.endTime - System.currentTimeMillis()));
                        timeRemaining.setScore(counter);
                        counter++;
                        Score gameName = obj.getScore(ChatColor.YELLOW + ChatColor.BOLD.toString() + "Game " + (this.gameCount-1) + "/" + this.totalGameCount + ": " + ChatColor.RESET + game.name);
                        gameName.setScore(counter);
                        counter++;
                        Score blankScore3 = obj.getScore("    ");
                        blankScore3.setScore(counter);
                        counter++;
                    }
                } else if (this.currentGame.type == MiniGame.MiniGameType.TIMED) {
                    MiniGame game = this.currentGame;
                    if (game.teamsEnabled) {
                        Score playerTeam = obj.getScore(ChatColor.YELLOW + ChatColor.BOLD.toString() + "Your Team: " + ChatColor.RESET + game.scoreboard.getTeam(game.playerTeams.get(player.getName())).getPrefix() + game.playerTeams.get(player.getName()));
                        playerTeam.setScore(counter);
                        counter++;

                        Score blankScore1point5 = obj.getScore("        ");
                        blankScore1point5.setScore(counter);
                        counter++;

                        counter = formatTeamRoundScoresBlock(player, obj, counter, game);
                        String roundScoreTitleString = ChatColor.GOLD + ChatColor.BOLD.toString() + "Round Score: ";
                        if (game.multiplier != 1.0f) {
                            roundScoreTitleString = roundScoreTitleString + ChatColor.RESET + ChatColor.WHITE + "(" + ChatColor.YELLOW + "x" + String.format("%.1f", game.multiplier) + ChatColor.WHITE + ")";
                        }
                        Score roundScoreTitle = obj.getScore(roundScoreTitleString);
                        roundScoreTitle.setScore(counter);
                        counter++;

                        Score blankScore2 = obj.getScore("   ");
                        blankScore2.setScore(counter);
                        counter++;

                        Score timeRemaining = obj.getScore(ChatColor.RED + ChatColor.BOLD.toString() + "Time Remaining: " + ChatColor.RESET + formatRemainingGameTime(game.endTime - System.currentTimeMillis()));
                        timeRemaining.setScore(counter);
                        counter++;
                        Score gameName = obj.getScore(ChatColor.YELLOW + ChatColor.BOLD.toString() + "Game " + (this.gameCount-1) + "/" + this.totalGameCount + ": " + ChatColor.RESET + game.name);
                        gameName.setScore(counter);
                        counter++;
                        Score blankScore3 = obj.getScore("    ");
                        blankScore3.setScore(counter);
                        counter++;
                    } else {

                        Score blankScore1point5 = obj.getScore("        ");
                        blankScore1point5.setScore(counter);
                        counter++;

                        counter = formatIndividualRoundScoresBlock(player, obj, counter, game);

                        String roundScoreTitleString = ChatColor.GOLD + ChatColor.BOLD.toString() + "Round Score: ";
                        if (game.multiplier != 1.0f) {
                            roundScoreTitleString = roundScoreTitleString + ChatColor.RESET + ChatColor.WHITE + "(" + ChatColor.YELLOW + "x" + String.format("%.1f", game.multiplier) + ChatColor.WHITE + ")";
                        }
                        Score roundScoreTitle = obj.getScore(roundScoreTitleString);
                        roundScoreTitle.setScore(counter);
                        counter++;

                        Score blankScore2 = obj.getScore("   ");
                        blankScore2.setScore(counter);
                        counter++;

                        Score timeRemaining = obj.getScore(ChatColor.RED + ChatColor.BOLD.toString() + "Time Remaining: " + ChatColor.RESET + formatRemainingGameTime(game.endTime - System.currentTimeMillis()));
                        timeRemaining.setScore(counter);
                        counter++;
                        Score gameName = obj.getScore(ChatColor.YELLOW + ChatColor.BOLD.toString() + "Game " + (this.gameCount-1) + "/" + this.totalGameCount + ": " + ChatColor.RESET + game.name);
                        gameName.setScore(counter);
                        counter++;
                        Score blankScore3 = obj.getScore("    ");
                        blankScore3.setScore(counter);
                        counter++;
                    }
                }
            }
        }
        player.setScoreboard(playerScoreboard);

    }
    public int formatTournamentScoresBlock(Player player, Objective obj, int counter) {
        Map<UUID, Integer> values = sortByValue(this.gameScore);
        List<UUID> sortedValues = new ArrayList<>(values.keySet());
        UUID firstId = null;
        UUID secondId = null;
        UUID thirdId = null;
        UUID fourthId = null;
        int fourthNum = 4;
        if (sortedValues.size() > 0) {
            firstId = sortedValues.get(sortedValues.size()-1);
        }
        if (sortedValues.size() > 1) {
            secondId = sortedValues.get(sortedValues.size()-2);
        }
        if (sortedValues.size() > 2) {
            thirdId = sortedValues.get(sortedValues.size()-3);
        }
        if (sortedValues.size() > 3) {
            if (firstId.equals(player.getUniqueId()) || secondId.equals(player.getUniqueId()) || thirdId.equals(player.getUniqueId())) {
                fourthId = sortedValues.get(sortedValues.size()-4);
            } else {
                fourthNum = -sortedValues.indexOf(player.getUniqueId()) + sortedValues.size() + 1;
                fourthId = player.getUniqueId();
            }
        }
        List<String> placingTexts = new ArrayList<>();
        String fourthText = null;
        String thirdText = null;
        String secondText = null;
        String firstText = null;
        if (fourthId != null) {
            fourthText = "  " + fourthNum + ". " + ChatColor.GRAY + Bukkit.getPlayer(fourthId).getName() + ChatColor.YELLOW + Math.round(values.get(fourthId));
            placingTexts.add(fourthText);
        }
        if (thirdId != null) {
            thirdText ="  3. " + ChatColor.RED + Bukkit.getPlayer(thirdId).getName() +  ChatColor.YELLOW +Math.round(values.get(thirdId));
            placingTexts.add(thirdText);
        }
        if (secondId != null) {
            secondText ="  2. " + ChatColor.WHITE + Bukkit.getPlayer(secondId).getName() +  ChatColor.YELLOW +Math.round(values.get(secondId));
            placingTexts.add(secondText);
        }
        if (firstId != null) {
            firstText = "  1. " + ChatColor.GOLD + Bukkit.getPlayer(firstId).getName() +  ChatColor.YELLOW +Math.round(values.get(firstId));
            placingTexts.add(firstText);
        }
        int targetLength = ("Tournament Scores:").length();

        if (fourthText != null) {
            String text = formatScoreString("  " + fourthNum + ". " + ChatColor.GRAY + Bukkit.getPlayer(fourthId).getName() , ChatColor.YELLOW.toString() + Math.round(values.get(fourthId)), targetLength);
            Score fourthScore = obj.getScore(text);
            fourthScore.setScore(counter);
            counter++;
        }
        if (thirdText != null) {
            String text = formatScoreString("  3. " + ChatColor.RED + Bukkit.getPlayer(thirdId).getName() , ChatColor.YELLOW.toString() + Math.round(values.get(thirdId)), targetLength);
            Score thirdScore = obj.getScore(text);
            thirdScore.setScore(counter);
            counter++;
        }
        if (secondText != null) {
            String text = formatScoreString("  2. " + ChatColor.WHITE + Bukkit.getPlayer(secondId).getName() , ChatColor.YELLOW.toString() + Math.round(values.get(secondId)), targetLength);
            Score secondScore = obj.getScore(text);
            secondScore.setScore(counter);
            counter++;
        }
        if (firstText != null) {
            String text = formatScoreString("  1. " + ChatColor.GOLD + Bukkit.getPlayer(firstId).getName() , ChatColor.YELLOW.toString() + Math.round(values.get(firstId)), targetLength);
            Score firstScore = obj.getScore(text);
            firstScore.setScore(counter);
            counter++;
        }
        return counter;
    }
    public int formatIndividualRoundScoresBlock(Player player, Objective obj, int counter, MiniGame game) {
        Map<UUID, Float> values = sortByValue(game.playerScores);
        List<UUID> sortedValues = new ArrayList<>(values.keySet());
        UUID firstId = null;
        UUID secondId = null;
        UUID thirdId = null;
        UUID fourthId = null;
        int fourthNum = 4;
        if (sortedValues.size() > 0) {
            firstId = sortedValues.get(sortedValues.size()-1);
        }
        if (sortedValues.size() > 1) {
            secondId = sortedValues.get(sortedValues.size()-2);
        }
        if (sortedValues.size() > 2) {
            thirdId = sortedValues.get(sortedValues.size()-3);
        }
        if (sortedValues.size() > 3) {
            if (firstId.equals(player.getUniqueId()) || secondId.equals(player.getUniqueId()) || thirdId.equals(player.getUniqueId())) {
                fourthId = sortedValues.get(sortedValues.size()-4);
            } else {
                fourthNum = -sortedValues.indexOf(player.getUniqueId()) + sortedValues.size() + 1;
                fourthId = player.getUniqueId();
            }
        }
        List<String> placingTexts = new ArrayList<>();
        String fourthText = null;
        String thirdText = null;
        String secondText = null;
        String firstText = null;
        if (fourthId != null) {
            fourthText = "  " + fourthNum + ". ";
            if (!game.participants.contains(Bukkit.getPlayer(fourthId).getName())) {
                fourthText = fourthText + ChatColor.GRAY; 
            } else {
                fourthText = fourthText + ChatColor.WHITE;
            }
            fourthText = fourthText + Bukkit.getPlayer(fourthId).getName() +  ChatColor.YELLOW + Math.round(values.get(fourthId));
            placingTexts.add(fourthText);
        }
        if (thirdId != null) {
            thirdText = "  3. ";
            if (!game.participants.contains(Bukkit.getPlayer(thirdId).getName())) {
                thirdText = thirdText + ChatColor.GRAY;
            } else {
                thirdText = thirdText + ChatColor.WHITE;
            }
            thirdText = thirdText + Bukkit.getPlayer(thirdId).getName() +   ChatColor.YELLOW + Math.round(values.get(thirdId));
            placingTexts.add(thirdText);
        }
        if (secondId != null) {
            secondText = "  2. ";
            if (!game.participants.contains(Bukkit.getPlayer(secondId).getName())) {
                secondText = secondText + ChatColor.GRAY;
            } else {
                secondText = secondText + ChatColor.WHITE;
            }
            secondText = secondText + Bukkit.getPlayer(secondId).getName() +   ChatColor.YELLOW + Math.round(values.get(secondId));
            placingTexts.add(secondText);
        }
        if (firstId != null) {
            firstText = "  1. ";
            if (!game.participants.contains(Bukkit.getPlayer(firstId).getName())) {
                firstText = firstText + ChatColor.GRAY;
            } else {
                firstText = firstText + ChatColor.WHITE;
            }
            firstText = firstText + Bukkit.getPlayer(firstId).getName() + ChatColor.YELLOW + Math.round(values.get(firstId));
            placingTexts.add(firstText);
        }
        int targetLength = ("Round Score:").length();
        if (game.multiplier != 1.0f) {
            targetLength = ChatColor.stripColor(("Round Score:" + ChatColor.RESET + ChatColor.WHITE + "(" + ChatColor.YELLOW + "x" + String.format("%.1f", game.multiplier) + ChatColor.WHITE + ")")).length();
        }

        if (fourthText != null) {
            String leftText = "  " + fourthNum + ". ";
            String rightText = ChatColor.YELLOW.toString() + Math.round(values.get(fourthId));
            if (!game.participants.contains(Bukkit.getPlayer(fourthId).getName())) {
                leftText = leftText + ChatColor.GRAY + Bukkit.getPlayer(fourthId).getName();
            } else {
                leftText = leftText + ChatColor.WHITE + Bukkit.getPlayer(fourthId).getName();
            }
            String text = formatScoreString(leftText ,rightText , targetLength);
            Score fourthScore = obj.getScore(text);
            fourthScore.setScore(counter);
            counter++;
        }
        if (thirdText != null) {
            String leftText = "  3. ";
            String rightText = ChatColor.YELLOW.toString() + values.get(thirdId);
            if (!game.participants.contains(Bukkit.getPlayer(thirdId).getName())) {
                leftText = leftText + ChatColor.GRAY + Bukkit.getPlayer(thirdId).getName();
            } else {
                leftText = leftText + ChatColor.WHITE + Bukkit.getPlayer(thirdId).getName();
            }
            String text = formatScoreString(leftText ,rightText , targetLength);
            Score thirdScore = obj.getScore(text);
            thirdScore.setScore(counter);
            counter++;
        }
        if (secondText != null) {
            String leftText = "  2. ";
            String rightText = ChatColor.YELLOW.toString() + Math.round(values.get(secondId));
            if (!game.participants.contains(Bukkit.getPlayer(secondId).getName())) {
                leftText = leftText + ChatColor.GRAY + Bukkit.getPlayer(secondId).getName();
            } else {
                leftText = leftText + ChatColor.WHITE + Bukkit.getPlayer(secondId).getName();
            }
            String text = formatScoreString(leftText ,rightText , targetLength);
            Score secondScore = obj.getScore(text);
            secondScore.setScore(counter);
            counter++;
        }
        if (firstText != null) {
            String leftText = "  1. ";
            String rightText = ChatColor.YELLOW.toString() + Math.round(values.get(firstId));
            if (!game.participants.contains(Bukkit.getPlayer(firstId).getName())) {
                leftText = leftText + ChatColor.GRAY + Bukkit.getPlayer(firstId).getName();
            } else {
                leftText = leftText + ChatColor.WHITE + Bukkit.getPlayer(firstId).getName();
            }
            String text = formatScoreString(leftText ,rightText , targetLength);
            Score firstScore = obj.getScore(text);
            firstScore.setScore(counter);
            counter++;
        }
        return counter;
    }

    public int formatTeamRoundScoresBlock(Player player, Objective obj, int counter, MiniGame game) {
        Map<String, Float> values = sortByValue(game.teamScores);
        List<String> sortedValues = new ArrayList<>(values.keySet());
        String firstId = null;
        String secondId = null;
        String thirdId = null;
        String fourthId = null;
        int fourthNum = 4;
        if (sortedValues.size() > 0) {
            firstId = sortedValues.get(sortedValues.size()-1);
        }
        if (sortedValues.size() > 1) {
            secondId = sortedValues.get(sortedValues.size()-2);
        }
        if (sortedValues.size() > 2) {
            thirdId = sortedValues.get(sortedValues.size()-3);
        }
        if (sortedValues.size() > 3) {
            if (firstId.equals(game.playerTeams.get(player.getName())) || secondId.equals(game.playerTeams.get(player.getName())) || thirdId.equals(game.playerTeams.get(player.getName()))) {
                fourthId = sortedValues.get(sortedValues.size()-4);
            } else {
                fourthNum = -sortedValues.indexOf(game.playerTeams.get(player.getName())) + sortedValues.size() + 1;
                fourthId = game.playerTeams.get(player.getName());
            }
        }
        List<String> placingTexts = new ArrayList<>();
        String fourthText = null;
        String thirdText = null;
        String secondText = null;
        String firstText = null;
        if (fourthId != null) {
            fourthText = "  " + fourthNum + ". ";
            if (!game.getRemainingTeamNames().contains(fourthId)) {
                fourthText = fourthText + ChatColor.GRAY;
            } else {
                fourthText = fourthText + game.scoreboard.getTeam(fourthId).getPrefix();
            }
            fourthText = fourthText + fourthId +  "" + ChatColor.YELLOW + Math.round(values.get(fourthId));
            placingTexts.add(fourthText);
        }
        if (thirdId != null) {
            thirdText = "  3. ";
            if (!game.getRemainingTeamNames().contains(thirdId)) {
                thirdText = thirdText + ChatColor.GRAY;
            } else {
                thirdText = thirdText + game.scoreboard.getTeam(thirdId).getPrefix();
            }
            thirdText = thirdText + thirdId +  "" + ChatColor.YELLOW + Math.round(values.get(thirdId));
            placingTexts.add(thirdText);
        }
        if (secondId != null) {
            secondText = "  2. ";
            if (!game.getRemainingTeamNames().contains(secondId)) {
                secondText = secondText + ChatColor.GRAY;
            } else {
                secondText = secondText + game.scoreboard.getTeam(secondId).getPrefix();
            }
            secondText = secondText + secondId +  "" + ChatColor.YELLOW + Math.round(values.get(secondId));
            placingTexts.add(secondText);
        }
        if (firstId != null) {
            firstText = "  2. ";
            if (!game.getRemainingTeamNames().contains(firstId)) {
                firstText = firstText + ChatColor.GRAY;
            } else {
                firstText = firstText + game.scoreboard.getTeam(firstId).getPrefix();
            }
            firstText = firstText + firstId +  "" + ChatColor.YELLOW + Math.round(values.get(firstId));
            placingTexts.add(firstText);
        }
        int targetLength = getLongestString(placingTexts);

        if (fourthText != null) {
            String leftText = "  " + fourthNum + ". ";
            String rightText = ChatColor.YELLOW.toString() + Math.round(values.get(fourthId));
            if (!game.getRemainingTeamNames().contains(fourthId)) {
                leftText = leftText + ChatColor.GRAY + fourthId +  "";
            } else {
                leftText = leftText + game.scoreboard.getTeam(fourthId).getPrefix() + fourthId +  "";
            }
            String text = formatScoreString(leftText ,rightText , targetLength);
            Score fourthScore = obj.getScore(text);
            fourthScore.setScore(counter);
            counter++;
        }
        if (thirdText != null) {
            String leftText = "  3. ";
            String rightText = ChatColor.YELLOW.toString() + Math.round(values.get(thirdId));
            if (!game.getRemainingTeamNames().contains(thirdId)) {
                leftText = leftText + ChatColor.GRAY + thirdId +  "";
            } else {
                leftText = leftText + game.scoreboard.getTeam(thirdId).getPrefix() + thirdId +  "";
            }
            String text = formatScoreString(leftText ,rightText , targetLength);
            Score thirdScore = obj.getScore(text);
            thirdScore.setScore(counter);
            counter++;
        }
        if (secondText != null) {
            String leftText = "  2. ";
            String rightText = ChatColor.YELLOW.toString() + Math.round(values.get(secondId));
            if (!game.getRemainingTeamNames().contains(secondId)) {
                leftText = leftText + ChatColor.GRAY + secondId +  "";
            } else {
                leftText = leftText + game.scoreboard.getTeam(secondId).getPrefix() + secondId +  "";
            }
            String text = formatScoreString(leftText ,rightText , targetLength);
            Score secondScore = obj.getScore(text);
            secondScore.setScore(counter);
            counter++;
        }
        if (firstText != null) {
            String leftText = "  1. ";
            String rightText = ChatColor.YELLOW.toString() + Math.round(values.get(firstId));
            if (!game.getRemainingTeamNames().contains(firstId)) {
                leftText = leftText + ChatColor.GRAY + firstId +  "";
            } else {
                leftText = leftText + game.scoreboard.getTeam(firstId).getPrefix() + firstId +  "";
            }
            String text = formatScoreString(leftText ,rightText , targetLength);
            Score firstScore = obj.getScore(text);
            firstScore.setScore(counter);
            counter++;
        }
        return counter;
    }
    
    public String formatRemainingGameTime(long time) {
        long mins = TimeUnit.MILLISECONDS.toMinutes(time);
        time -= TimeUnit.MINUTES.toMillis(mins);
        long secs = TimeUnit.MILLISECONDS.toSeconds(time);
        return String.format("%d:%02d", mins, secs);
    }
    public static List<String> splitStringToLinedString(String input, int targetChars) {
        List<String> substrings = new ArrayList<>();

        // Loop until input string is empty
        while (!input.isEmpty()) {
            // Check if input length is more than 20 characters
            if (input.length() > targetChars) {
                // Find the last space within the first 20 characters
                int lastSpaceIndex = input.substring(0, targetChars).lastIndexOf(' ');
                if (lastSpaceIndex == -1) {
                    // If no space found, split at 20 characters
                    lastSpaceIndex = targetChars;
                }
                // Split the string at the last space within the first 20 characters
                String substring = input.substring(0, lastSpaceIndex);
                substrings.add(substring);
                // Remove the processed characters from the input string
                input = input.substring(lastSpaceIndex).trim();
            } else {
                // Add the remaining characters to the list
                substrings.add(input);
                // Empty the input string to exit the loop
                input = "";
            }
        }

        return substrings;
    }
    public String getGeneralTimeString(Location loc) {
        if (loc.getWorld().getTime() > 23000) {
            return "Sunrise, Day " + day;
        } else if (loc.getWorld().getTime() < 6000) {
            return "Morning, Day " + day;
        } else if (loc.getWorld().getTime() < 12000) {
            return "Afternoon, Day " + day;
        } else if (loc.getWorld().getTime() < 13000) {
            return "Sunset, Day " + day;
        } else if (loc.getWorld().getTime() < 18000) {
            return "Evening, Day " + day;
        } else if (loc.getWorld().getTime() <= 23000) {
            return "Night, Day " + day;
        } else {
            return "Day " + day;
        }

    }
    public String getTimeForScoreboard(Location loc) {
        long time = loc.getWorld().getTime();
        int hour = (int)(time / 1000) + 6;
        if (hour > 24) {
            hour = hour - 24;
        }
        if (time > 18000L) {
            time -= 24000;
        }
        int minute =(int) (((time+6000) - (hour*1000)) * 3/50);
        String retString = "";
        if (hour == 24) {
            hour = hour -12;
            retString = ChatColor.GRAY.toString() + hour + ":" + String.format("%02d", minute) + "am";
        } else if (hour == 12) {
            retString = ChatColor.GRAY.toString() + hour + ":" + String.format("%02d", minute) + "pm";
        } else if (hour > 12) {
            hour = hour - 12;
            retString = ChatColor.GRAY.toString() + hour + ":" + String.format("%02d", minute) + "pm";
        } else {
            retString = ChatColor.GRAY.toString() + hour + ":" + String.format("%02d", minute) + "am";
        }
        if (night) {
            retString = ChatColor.AQUA + "☽ " + ChatColor.RESET + retString;
        } else {
            retString = ChatColor.YELLOW + "☀ " + ChatColor.RESET + retString;
        }
        return retString;
    }
    public String getKingdomDistrictForScoreboard(Location location) {
        if (this.grid.grid == null) {
            return ChatColor.RED + "Kingdom";
        }
        Cell cell = getCellFromLocation(location);
        if (cell.district == District.PORT) {
            return ChatColor.AQUA + "Port";
        } else if (cell.district == District.ARISTOCRACY) {
            return ChatColor.GOLD + "Aristocracy";
        } else if (cell.district == District.SLUMS) {
            return ChatColor.DARK_GRAY + "Slums";
        } else if (cell.district == District.FARM) {
            return ChatColor.GREEN + "Farm";
        } else if (cell.district == District.OUTSKIRTS) {
            return ChatColor.DARK_GREEN + "Outskirts";
        } else {
            return ChatColor.RED + "Kingdom";
        }

    }

    public Cell getCellFromLocation(Location loc) {
        if (this.grid == null) {
            return null;
        }
        int x = (int)this.grid.startX + ((int)loc.getX());
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
            if (args.length != 2 && args.length != 0) {
                sender.sendMessage("You need an x and a z!");
            } else if (args.length == 2) {
                sender.sendMessage(grid.schematicAtCell(Integer.parseInt(args[0]), Integer.parseInt(args[1])));
            } else if (sender instanceof Player) {
                Cell cell = getCellFromLocation(((Player)sender).getLocation());
                if (cell != null) {
                    String schematicAtCell = cell.schematicAtCell;
                    if (schematicAtCell == null) {
                        schematicAtCell = "null";
                    }
                    sender.sendMessage("You are standing on cell " + cell.x + "," + cell.z + ": ISOCCUPIED=" + cell.isOccupied + ", ISROAD=" + cell.isRoad + ", DISTRICT=" + cell.district + ", SCHEMATIC=" + schematicAtCell);
                    if (cell.pasteDetails != null) {
                        String additionalDetails = "ISOMNI=" + cell.pasteDetails.isOmni + ", DIR=" + cell.pasteDetails.schematicFacing + ", TIME=" + cell.pasteDetails.pasteTime + ", CELLS=";
                        for (Cell pasteCell : cell.pasteDetails.cellsOccupied) {
                            additionalDetails = additionalDetails + "[" + pasteCell.x + "," + pasteCell.z + "]";
                        }
                        sender.sendMessage(additionalDetails);
                    }
                } else {
                    sender.sendMessage("You are not standing on a cell.");
                }

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

            } else if (npc.hasTrait(StationMaster.class)) {
                StationMaster trait = npc.getTrait(StationMaster.class);
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
                String fullStr = "";
                for (int i = 0; i < args.length; i++ ) {
                    fullStr = fullStr + args[i] + " ";
                }
                fullStr = fullStr.trim();
                String[] parts = fullStr.split(" \\| ");
                List<String> commands = new ArrayList<>();
                for (String part : parts) {
                    commands.add(part);
                }
                ChainCommand chain = new ChainCommand(commands, player);
                chain.run();
            }

        } else if (command.getName().equalsIgnoreCase("tgtitem")) {
            if (args.length == 1) {
                if (sender instanceof Player) {
                    ((Player) sender).getInventory().addItem(this.itemHandler.getItemFromString(args[0]));
                    sender.sendMessage(ChatColor.GREEN + "Gave " + ChatColor.YELLOW + "1x " + this.itemHandler.getItemFromString(args[0]).getItemMeta().getDisplayName() + ChatColor.GREEN + " to " + ChatColor.YELLOW + ((Player)sender).getName() + ChatColor.GREEN + ".");
                }
            } else if (args.length == 2) {
                if (sender instanceof Player && args[1].matches("^[0-9]+$")) {
                    ((Player) sender).getInventory().addItem(this.itemHandler.getItemFromString(args[0]).asQuantity(Integer.parseInt(args[1])));
                    sender.sendMessage(ChatColor.GREEN + "Gave " + ChatColor.YELLOW + args[1] + "x " + this.itemHandler.getItemFromString(args[0]).getItemMeta().getDisplayName() + ChatColor.GREEN + " to " + ChatColor.YELLOW + ((Player)sender).getName() + ChatColor.GREEN + ".");

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
            } else if (args[0].equalsIgnoreCase("holdingtable")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    spawnHoldingTable(player, args);
                } else {
                    sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
                }
            } else if (args[0].equalsIgnoreCase("mashingtable")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    spawnMashingTable(player, args);
                } else {
                    sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
                }
            } else if (args[0].equalsIgnoreCase("timingtable")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    spawnTimingTable(player, args);
                } else {
                    sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
                }
            } else if (args[0].equalsIgnoreCase("station")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    createStation(player, args);
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
        } else if (command.getName().equalsIgnoreCase("fishing-zone")) {
            if (sender instanceof Player) {
                if (args.length > 0) {
                    this.createFishingZone((Player)sender, args);
                } else {
                    sender.sendMessage(ChatColor.RED + "You must specify a fishing zone template name!");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Only players can perform this command!");
            }
        } else if (command.getName().equalsIgnoreCase("menu")) {
            if (sender instanceof Player) {
                menus.openHomeMenu((Player)sender);
            } else {
                sender.sendMessage(ChatColor.RED + "Only players can perform this command!");
            }
        } else if (command.getName().equalsIgnoreCase("slots")) {
            if (args.length >= 3) {


                Player player = Bukkit.getPlayer(args[0]);
                if (player == null) {
                    sender.sendMessage(ChatColor.RED + "You must specify a valid player!");
                    return false;
                }
                try {
                    int num = Integer.parseInt(args[2]);
                    if (args[1].equalsIgnoreCase("storage")) {
                        this.players.get(player.getUniqueId()).setStorageSlots(this.players.get(player.getUniqueId()).getStorageSlots() + num);
                        sender.sendMessage(ChatColor.GREEN + "Added " + ChatColor.YELLOW + num + ChatColor.GREEN + " storage slots for " + ChatColor.YELLOW.toString() + player.getName() + ChatColor.GREEN + ".");
                    } else if (args[1].equalsIgnoreCase("accessory")) {
                        this.players.get(player.getUniqueId()).setAccessoryBagSlots(this.players.get(player.getUniqueId()).getAccessoryBagSlots() + num);
                        sender.sendMessage(ChatColor.GREEN + "Added " + ChatColor.YELLOW + num + ChatColor.GREEN + " accessory bag slots for " + ChatColor.YELLOW.toString() + player.getName() + ChatColor.GREEN + ".");

                    } else {
                        sender.sendMessage(ChatColor.RED + "The slot type you specified is invalid!");
                    }
                } catch (NumberFormatException | NullPointerException e) {
                    sender.sendMessage(ChatColor.RED + "You must specify an integer!");
                    return false;
                }
            } else if (args.length == 2) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                    return false;
                }
                Player player = (Player) sender;
                try {
                    int num = Integer.parseInt(args[1]);
                    if (args[0].equalsIgnoreCase("storage")) {
                        this.players.get(player.getUniqueId()).setStorageSlots(this.players.get(player.getUniqueId()).getStorageSlots() + num);
                        if (player.isOp() && debugEnabled(player)) {
                            sender.sendMessage(ChatColor.GREEN + "Added " + ChatColor.YELLOW + num + ChatColor.GREEN + " storage slots for " + ChatColor.YELLOW.toString() + player.getName() + ChatColor.GREEN + ".");

                        }
                    } else if (args[0].equalsIgnoreCase("accessory")) {
                        this.players.get(player.getUniqueId()).setAccessoryBagSlots(this.players.get(player.getUniqueId()).getAccessoryBagSlots() + num);
                        if (player.isOp() && debugEnabled(player)) {
                            sender.sendMessage(ChatColor.GREEN + "Added " + ChatColor.YELLOW + num + ChatColor.GREEN + " accessory bag slots for " + ChatColor.YELLOW.toString() + player.getName() + ChatColor.GREEN + ".");

                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "The slot type you specified is invalid!");
                    }
                } catch (NumberFormatException | NullPointerException e) {
                    sender.sendMessage(ChatColor.RED + "You must specify an integer!");
                    return false;
                }
            }


        } else if (command.getName().equalsIgnoreCase("skill-xp")) {
            if (args.length >= 3) {
                Player player = Bukkit.getPlayer(args[0]);
                if (player == null) {
                    sender.sendMessage(ChatColor.RED + "You must specify a valid player!");
                    return true;
                }
                XpType type = Xp.parseXpType(args[1]);
                if (type == null) {
                    sender.sendMessage(ChatColor.RED + "You must specify a valid XP Type!");
                    return true;
                }
                try {
                    double amount = Double.parseDouble(args[2]);
                    this.xpHandler.grantXp(type, player, amount);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "You must input a number!");
                }
            } else if (args.length == 2) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                    return true;
                }
                Player player = (Player) sender;
                XpType type = Xp.parseXpType(args[0]);
                if (type == null) {
                    sender.sendMessage(ChatColor.RED + "You must specify a valid XP Type!");
                    return true;
                }
                try {
                    double amount = Double.parseDouble(args[1]);
                    this.xpHandler.grantXp(type, player, amount);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "You must input a number!");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Not enough arguments.");
            }
        } else if (command.getName().equalsIgnoreCase("dosh")) {
            if (args.length >= 2) {
                Player player = Bukkit.getPlayer(args[0]);
                if (player == null) {
                    sender.sendMessage(ChatColor.RED + "You must specify a valid player!");
                    return true;
                }
                try {
                    double amount = Double.parseDouble(args[1]);
                    if (plugin.players.containsKey(player.getUniqueId())) {
                        plugin.players.get(player.getUniqueId()).addPurseGold(amount);
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "You must input a number!");
                }
            } else if (args.length == 1) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                }
                Player player = (Player) sender;
                try {
                    double amount = Double.parseDouble(args[0]);
                    if (plugin.players.containsKey(player.getUniqueId())) {
                        plugin.players.get(player.getUniqueId()).addPurseGold(amount);
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "You must input a number!");
                }
            }
        } else if (command.getName().equalsIgnoreCase("grant-recipe")) {
            if (args.length == 1) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                    return true;
                }
                Player player = (Player)sender;
                MMOPlayer mmoPlayer = plugin.players.get(player.getUniqueId());
                Recipe recipe = this.itemHandler.getRecipeFromString(args[0]);
                if (recipe == null) {
                    player.sendMessage(ChatColor.RED + "No recipe with the name " + args[0] + " could be found!");
                    return true;
                }
                if (mmoPlayer.recipeBook.contains(recipe)) {
                    return true;
                }
                mmoPlayer.recipeBook.add(recipe);
                ToastMessage.displayTo(player, recipe.reward.bukkitItem.getType().toString().toLowerCase(), recipe.displayName + ChatColor.GRAY + " recipe discovered.", ToastMessage.Style.GOAL);
                if (sender.isOp() && debugEnabled(player)) {
                    sender.sendMessage(ChatColor.GREEN + "Added recipe for " + recipe.reward.internalName + " to " + Bukkit.getPlayer(mmoPlayer.getMinecraftUUID()).getName() + ChatColor.GREEN + ".");
                }
            } else if (args.length >= 2) {
                Player player = Bukkit.getPlayer(args[0]);
                if (player == null) {
                    sender.sendMessage(ChatColor.RED + "You must specify a valid player!");
                    return true;
                }
                MMOPlayer mmoPlayer = plugin.players.get(player.getUniqueId());
                Recipe recipe = this.itemHandler.getRecipeFromString(args[1]);
                if (recipe == null) {
                    sender.sendMessage(ChatColor.RED + "No recipe with the name " + args[1] + " could be found!");
                    return true;
                }
                if (mmoPlayer.recipeBook.contains(recipe)) {
                    return true;
                }
                mmoPlayer.recipeBook.add(recipe);
                ToastMessage.displayTo(player, recipe.reward.bukkitItem.getType().toString().toLowerCase(), recipe.displayName + ChatColor.GRAY + " recipe discovered.", ToastMessage.Style.GOAL);
                if (sender.isOp() && debugEnabled(player)) {
                    sender.sendMessage(ChatColor.GREEN + "Added recipe for " + recipe.reward.internalName + " to " + Bukkit.getPlayer(mmoPlayer.getMinecraftUUID()).getName() + ChatColor.GREEN + ".");
                }
            }

        } else if (command.getName().equalsIgnoreCase("stat")) {
            if (args.length ==2) {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                    return true;
                }
                MMOPlayer mmoPlayer = this.players.get(player.getUniqueId());
                if (mmoPlayer == null) {
                    return true;
                }
                try {
                    boolean success = baseStatChange(args[0].toLowerCase(), Double.parseDouble(args[1]), mmoPlayer);
                    if (sender.isOp() && debugEnabled(player)) {
                        if (success) {
                            sender.sendMessage(ChatColor.GREEN + "Modified " + ChatColor.YELLOW + args[0].toUpperCase() + ChatColor.GREEN + " for player " + ChatColor.YELLOW + player.getName() + ChatColor.GREEN + " by " + ChatColor.YELLOW + args[1] + ChatColor.GREEN + ".");
                        } else {
                            sender.sendMessage(ChatColor.RED + "You must enter a valid stat type!");
                        }
                    }
                    return true;
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "You must enter a valid number!");
                    return true;
                }
            } else if (args.length >= 3) {

                Player player = Bukkit.getPlayer(args[0]);
                if (player == null) {
                    sender.sendMessage(ChatColor.RED + "You must specify a valid player!");
                    return true;
                }
                MMOPlayer mmoPlayer = this.players.get(player.getUniqueId());
                if (mmoPlayer == null) {
                    return true;
                }
                try {
                    boolean success = baseStatChange(args[1].toLowerCase(), Double.parseDouble(args[2]), mmoPlayer);
                    if (sender.isOp() && debugEnabled(player)) {
                        if (success) {
                            sender.sendMessage(ChatColor.GREEN + "Modified " + ChatColor.YELLOW + args[1].toUpperCase() + ChatColor.GREEN + " for player " + ChatColor.YELLOW + player.getName() + ChatColor.GREEN + " by " + ChatColor.YELLOW + args[2] + ChatColor.GREEN + ".");
                        } else {
                            sender.sendMessage(ChatColor.RED + "You must enter a valid stat type!");
                        }
                    }
                    return true;
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "You must enter a valid number!");
                    return true;
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Not enough arguments.");
            }
        } else if (command.getName().equalsIgnoreCase("potion-effect")) {
            if (args.length == 4) {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                    return true;
                }
                MMOPlayer mmoPlayer = this.players.get(player.getUniqueId());
                if (mmoPlayer == null) {
                    return true;
                }
                try {
                    boolean success = potionEffect(args[0].toLowerCase(), Integer.parseInt(args[1]),mmoPlayer, Integer.parseInt(args[2]),args[3]);
                    if (sender.isOp() && debugEnabled(player)) {
                        if (success) {
                            sender.sendMessage(ChatColor.GREEN + "Applied " + ChatColor.YELLOW + args[0].toUpperCase() + ChatColor.GREEN + " to player " + ChatColor.YELLOW + player.getName() + ChatColor.GREEN + " level " + ChatColor.YELLOW + args[1] + ChatColor.GREEN + " for " + ChatColor.YELLOW + args[2] + ChatColor.GREEN + " seconds.");
                        } else {
                            sender.sendMessage(ChatColor.RED + "You must enter a valid potion type!");
                        }
                    }
                    return true;
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "You must enter a valid number!");
                    return true;
                }
            } else if (args.length >= 5) {

                Player player = Bukkit.getPlayer(args[0]);
                if (player == null) {
                    sender.sendMessage(ChatColor.RED + "You must specify a valid player!");
                    return true;
                }
                MMOPlayer mmoPlayer = this.players.get(player.getUniqueId());
                if (mmoPlayer == null) {
                    return true;
                }
                try {
                    boolean success = potionEffect(args[1].toLowerCase(),Integer.parseInt(args[2]),mmoPlayer,Integer.parseInt(args[3]),args[4]);
                    if (sender.isOp() && debugEnabled(player)) {
                        if (success) {
                            sender.sendMessage(ChatColor.GREEN + "Applied " + ChatColor.YELLOW + args[1].toUpperCase() + ChatColor.GREEN + " for player " + ChatColor.YELLOW + player.getName() + ChatColor.GREEN + " level " + ChatColor.YELLOW + args[2] + ChatColor.GREEN + " for " + ChatColor.YELLOW + args[3] + ChatColor.GREEN + " seconds.");
                        } else {
                            sender.sendMessage(ChatColor.RED + "You must enter a valid stat type!");
                        }
                    }
                    return true;
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "You must enter a valid number!");
                    return true;
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Not enough arguments.");
            }
        } else if (command.getName().equalsIgnoreCase("temp-stat")) {
            if (args.length == 3) {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                    return true;
                }
                MMOPlayer mmoPlayer = this.players.get(player.getUniqueId());
                if (mmoPlayer == null) {
                    return true;
                }
                try {
                    boolean success = tempStatChange(args[0].toLowerCase(), Double.parseDouble(args[1]), mmoPlayer, Integer.parseInt(args[2]));
                    if (sender.isOp() && debugEnabled(player)) {
                        if (success) {
                            sender.sendMessage(ChatColor.GREEN + "Modified " + ChatColor.YELLOW + args[0].toUpperCase() + ChatColor.GREEN + " for player " + ChatColor.YELLOW + player.getName() + ChatColor.GREEN + " by " + ChatColor.YELLOW + args[1] + ChatColor.GREEN + " for " + ChatColor.YELLOW + args[2] + ChatColor.GREEN + "seconds.");
                        } else {
                            sender.sendMessage(ChatColor.RED + "You must enter a valid stat type!");
                        }
                    }
                    return true;
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "You must enter a valid number!");
                    return true;
                }
            } else if (args.length >= 4) {

                Player player = Bukkit.getPlayer(args[0]);
                if (player == null) {
                    sender.sendMessage(ChatColor.RED + "You must specify a valid player!");
                    return true;
                }
                MMOPlayer mmoPlayer = this.players.get(player.getUniqueId());
                if (mmoPlayer == null) {
                    return true;
                }
                try {
                    boolean success = tempStatChange(args[1].toLowerCase(), Double.parseDouble(args[2]), mmoPlayer, Integer.parseInt(args[3]));
                    if (sender.isOp() && debugEnabled(player)) {
                        if (success) {
                            sender.sendMessage(ChatColor.GREEN + "Modified " + ChatColor.YELLOW + args[1].toUpperCase() + ChatColor.GREEN + " for player " + ChatColor.YELLOW + player.getName() + ChatColor.GREEN + " by " + ChatColor.YELLOW + args[2] + ChatColor.GREEN + " for " + ChatColor.YELLOW + args[3] + ChatColor.GREEN + ".");
                        } else {
                            sender.sendMessage(ChatColor.RED + "You must enter a valid stat type!");
                        }
                    }
                    return true;
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "You must enter a valid number!");
                    return true;
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Not enough arguments.");
            }
        } else if (command.getName().equalsIgnoreCase("debug")) {
            if (args.length == 0) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Only players may execute that command!");
                    return true;
                }
                if (debugEnabled((Player)sender)) {
                    disableDebug((Player)sender);
                    sender.sendMessage(ChatColor.GREEN + "Set debug to " + ChatColor.RED + "DISABLED" + ChatColor.GREEN + ".");
                } else {
                    enableDebug((Player)sender);
                    sender.sendMessage(ChatColor.GREEN + "Set debug to " + ChatColor.YELLOW + "ENABLED" + ChatColor.GREEN + ".");
                }
                return true;
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("enable")) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.RED + "Only players may execute that command!");
                        return true;
                    }
                    enableDebug((Player)sender);
                    sender.sendMessage(ChatColor.GREEN + "Set debug to " + ChatColor.YELLOW + "ENABLED" + ChatColor.GREEN + ".");
                    return true;
                } else if (args[0].equalsIgnoreCase("off") || args[0].equalsIgnoreCase("disable")) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.RED + "Only players may execute that command!");
                        return true;
                    }
                    disableDebug((Player)sender);
                    sender.sendMessage(ChatColor.GREEN + "Set debug to " + ChatColor.RED + "DISABLED" + ChatColor.GREEN + ".");
                    return true;
                } else {
                    Player player = Bukkit.getPlayerExact(args[0]);
                    if (player == null) {
                        sender.sendMessage(ChatColor.RED + "You must specify a valid player!");
                        return true;
                    }
                    if (debugEnabled(player)) {
                        disableDebug(player);
                        sender.sendMessage(ChatColor.GREEN + "Set debug to " + ChatColor.RED + "DISABLED" + ChatColor.GREEN + " for " + ChatColor.YELLOW + player.getName() + ChatColor.GREEN + ".");
                    } else {
                        enableDebug(player);
                        sender.sendMessage(ChatColor.GREEN + "Set debug to " + ChatColor.YELLOW + "ENABLED" + ChatColor.GREEN + " for " + ChatColor.YELLOW + player.getName() + ChatColor.GREEN + ".");
                    }
                    return true;
                }
            }
        } else if (command.getName().equalsIgnoreCase("foraging-zone")) {
            if (sender instanceof Player) {
                if (args.length > 0) {
                    this.createForagingZone((Player)sender, args);

                } else {
                    sender.sendMessage(ChatColor.RED + "You must specify a foraging zone template name!");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Only players can perform this command!");
            }
        } else if (command.getName().equalsIgnoreCase("dungeon")) {
            if (!(sender instanceof Player)){
                sender.sendMessage(ChatColor.RED + "Only players can use this command!");
                return true;
            }
            spawnDungeon((Player)sender, args);

        } else if (command.getName().equalsIgnoreCase("room")) {
            if (!(sender instanceof Player)){
                sender.sendMessage(ChatColor.RED + "Only players can use this command!");
                return true;
            }
            if (args.length >= 1) {
                spawnRoom((Player)sender, args);
            }

        } else if (command.getName().equalsIgnoreCase("door")) {
            if (!(sender instanceof Player)){
                sender.sendMessage(ChatColor.RED + "Only players can use this command!");
                return true;
            }
            if (args.length >= 1) {
                spawnDoor((Player)sender, args);
            }

        } else if (command.getName().equalsIgnoreCase("exit")) {
            if (!(sender instanceof Player)){
                sender.sendMessage(ChatColor.RED + "Only players can use this command!");
                return true;
            }
            if (args.length >= 1) {
                spawnExit((Player)sender, args);
            }

        } else if (command.getName().equalsIgnoreCase("reward-chest")) {
            if (!(sender instanceof Player)){
                sender.sendMessage(ChatColor.RED + "Only players can use this command!");
                return true;
            }
            if (args.length >= 1) {
                spawnRewardChest((Player)sender, args);
            }

        } else if (command.getName().equalsIgnoreCase("startgame")) {
            if (!this.gameRunning && !this.gameEnded) {
                GennsGym.stopCountdown();
                this.startGame();
                sender.sendMessage("Game started.");
            } else {
                sender.sendMessage("Game already started.");
            }
        } else if (command.getName().equalsIgnoreCase("start-tournament")) {
            if (!this.gameRunning || this.gameEnded) {
                sender.sendMessage("Game must be started!");
            } else {
                this.startTournament();
            }
        } else if (command.getName().equalsIgnoreCase("invshop") && args.length == 2 && args[0].equalsIgnoreCase("open") && sender.hasPermission("invshop.open") && sender instanceof Player) {
            Shop shop = (Shop)this.shopHandler.shops.get(args[1]);
            if (shop != null) {
                this.shopHandler.invShops.openShop((Player)sender, shop);
            }
        } else if (command.getName().equals("nextgame")) {
            if (args.length == 0) {
                sender.sendMessage("Next game: " + (this.nextGame != null ? this.nextGame.name : "none"));
            } else {
                MiniGame game = (MiniGame)this.games.get(args[0]);
                if (game != null) {
                    this.nextGame = game;
                    sender.sendMessage("Next game set to " + this.nextGame.name);
                } else {
                    sender.sendMessage("No game found by that name");
                }
            }
        } else if (command.getName().equals("override")) {
            this.override = true;
        } else if (command.getName().equals("endround")) {
            if (this.currentGame != null) {
                if (args.length == 0) {
                    this.currentGame.end();
                    this.nextGame();
                    sender.sendMessage("Current game ended");
                } else {
                    int seconds = Integer.parseInt(args[0]);
                    this.currentGame.setTimeRemaining(seconds);
                    sender.sendMessage("Current game will end in " + seconds + " seconds");
                }
            } else {
                sender.sendMessage("No game is running");
            }
        } else {
            int points;
            Player player;
            if (command.getName().equals("roundscoreadd")) {
                float pointsF = 0;
                if (this.currentGame != null) {
                    player = Bukkit.getPlayerExact(args[0]);
                    if (player != null) {
                        pointsF = this.parseFloat(args[1]);
                        this.currentGame.addScore(player, pointsF);
                    }
                }
            } else if (command.getName().equals("roundscoreset")) {
                float pointsF = 0;
                if (this.currentGame != null) {
                    player = Bukkit.getPlayerExact(args[0]);
                    if (player != null) {
                        pointsF = this.parseFloat(args[1]);
                        this.currentGame.setScore(player, pointsF);
                    }
                }
            } else if (command.getName().equals("gamescoreadd")) {
                if (this.currentGame != null) {
                    player = Bukkit.getPlayerExact(args[0]);
                    if (player != null) {
                        points = this.parseInt(args[1]);
                        if (!this.gameScore.keySet().contains(player.getUniqueId())) {
                            this.gameScore.put(player.getUniqueId(), 0);
                        }

                        int score = this.gameScore.get(player.getUniqueId()) + points;
                        if (score < 0) {
                            score = 0;
                        }
                        this.gameScore.put(player.getUniqueId(), score);
                    }
                }
            } else if (command.getName().equals("gamescoreset")) {
                if (this.currentGame != null) {
                    player = Bukkit.getPlayerExact(args[0]);
                    if (player != null) {
                        points = this.parseInt(args[1]);
                        this.gameScore.put(player.getUniqueId(), points);
                    }
                }
            } else if (command.getName().equals("playerout") && this.currentGame != null) {
                player = Bukkit.getPlayerExact(args[0]);
                if (player != null) {
                    this.currentGame.setPlayerOut(player);
                }
            } else if (command.getName().equals("points-multiplier") && this.currentGame != null) {
                if (this.parseFloat(args[0]) > 0.0f) {
                    this.currentGame.multiplier = this.parseFloat(args[0]);
                } else {
                    this.currentGame.multiplier = 1.0f;
                }
            }
        }
        return true;

    }
    void startTournament() {
        this.tournament = true;
        this.nextGame();
    }
    public boolean tempStatChange(String statName, double value, MMOPlayer mmoPlayer, int timeInSecs) {
        if (Arrays.stream(this.statNames).toList().contains(statName)) {
            this.statUpdates.temporaryStatUpdate(mmoPlayer,statName,(float)value,timeInSecs);
            return true;
        } else {
            return false;
        }

    }
    public boolean potionEffect(String effectName, int lvl, MMOPlayer mmoPlayer, int timeInSecs, String itemName) {
        ItemStack item = new ItemStack(Material.GRAY_DYE);
        if (this.itemHandler.containsName(this.itemHandler.allItems, itemName)) {
            item = this.itemHandler.getItemFromString(itemName);
        } else {
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + "Unknown Source");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "This potion effect comes from places unknown");
            lore.add(ChatColor.GRAY + "with values unknown...");
            lore.add(ChatColor.GRAY + "(You should probably bug God about it.)");
        }
        if (Arrays.stream(this.potionNames).toList().contains(effectName)) {
            mmoPlayer.addPotionEffect(effectName,lvl,timeInSecs,item);
            return true;
        } else {
            return false;
        }

    }
    public boolean baseStatChange(String statName, double value, MMOPlayer mmoPlayer) {
        if (statName.equalsIgnoreCase("strength")) {
            mmoPlayer.setBaseStrength((float)(mmoPlayer.getBaseStrength() + value));
            return true;
        } else if (statName.equalsIgnoreCase("defense")) {
            mmoPlayer.setBaseDefense((float)(mmoPlayer.getBaseDefense() + value));
            return true;
        } else if (statName.equalsIgnoreCase("health")) {
            mmoPlayer.setBaseHealth((float)(mmoPlayer.getBaseHealth() + value));
            return true;
        } else if (statName.equalsIgnoreCase("crit-damage")) {
            mmoPlayer.setBaseCritDamage((float)(mmoPlayer.getBaseCritDamage() + value));
            return true;
        } else if (statName.equalsIgnoreCase("crit-chance")) {
            mmoPlayer.setBaseCritChance((float)(mmoPlayer.getBaseCritChance() + value));
            return true;
        } else if (statName.equalsIgnoreCase("speed")) {
            mmoPlayer.setBaseSpeed((float)(mmoPlayer.getBaseSpeed() + value));
            return true;
        } else if (statName.equalsIgnoreCase("vigor")) {
            mmoPlayer.setBaseVigor((float)(mmoPlayer.getBaseVigor() + value));
            return true;
        } else if (statName.equalsIgnoreCase("stamina-regen")) {
            mmoPlayer.setBaseManaRegen((float)(mmoPlayer.getBaseManaRegen() + value));
            return true;
        } else if (statName.equalsIgnoreCase("ability-damage")) {
            mmoPlayer.setBaseAbilityDamage((float)(mmoPlayer.getBaseAbilityDamage() + value));
            return true;
        } else if (statName.equalsIgnoreCase("mining-fortune")) {
            mmoPlayer.setBaseMiningFortune((float)(mmoPlayer.getBaseMiningFortune() + value));
            return true;
        } else if (statName.equalsIgnoreCase("farming-fortune")) {
            mmoPlayer.setBaseFarmingFortune((float)(mmoPlayer.getBaseFarmingFortune() + value));
            return true;
        } else if (statName.equalsIgnoreCase("foraging-fortune")) {
            mmoPlayer.setBaseLoggingFortune((float)(mmoPlayer.getBaseLoggingFortune() + value));
            return true;
        } else if (statName.equalsIgnoreCase("fishing-speed")) {
            mmoPlayer.setBaseLure((float)(mmoPlayer.getBaseLure() + value));
            return true;
        } else if (statName.equalsIgnoreCase("lure")) {
            mmoPlayer.setBaseFlash((float)(mmoPlayer.getBaseFlash() + value));
            return true;
        } else if (statName.equalsIgnoreCase("dialogue-speed")) {
            mmoPlayer.setBaseDialogueSpeed((float)(mmoPlayer.getBaseDialogueSpeed() + value));
            return true;
        } else if (statName.equalsIgnoreCase("shop-discount")) {
            mmoPlayer.setBaseVendorPrice((float)(mmoPlayer.getBaseVendorPrice() + value));
            return true;
        } else if (statName.equalsIgnoreCase("focus")) {
            mmoPlayer.setBaseFocus((float)(mmoPlayer.getBaseFocus() + value));
            return true;
        } else if (statName.equalsIgnoreCase("stamina")) {
            mmoPlayer.setBaseMaxMana((float) (mmoPlayer.getBaseMaxMana() + value));
            return true;
        } else if (statName.equalsIgnoreCase("sea-creature-chance")) {
            mmoPlayer.setBaseSeaCreatureChance((float) (mmoPlayer.getBaseSeaCreatureChance() + value));
            return true;
        } else if (statName.equalsIgnoreCase("health-regen")) {
            mmoPlayer.setBaseHealthRegen((float) (mmoPlayer.getBaseHealthRegen() + value));
            return true;
        }
        return false;
    }
    public void spawnDungeon(Player player, String[] args) {
        if (args.length > 0) {
            DungeonTemplate template = this.dungeonHandler.allTemplates.stream().filter(obj -> obj.getName().equalsIgnoreCase(args[0])).findFirst().orElse(null);
            if (template != null) {
                Dungeon dungeon = new Dungeon(template, this);
                dungeon.spawn(player);
            }
        } else {
            player.sendMessage(ChatColor.RED + "You must specify a Dungeon's internal name!");
        }
    }
    public void spawnDoor(Player player, String[] args) {
        if (args.length > 0) {
            Dungeon dungeon = this.dungeonHandler.getDungeonForRoom(player.getLocation());
            if (dungeon == null) {
                dungeon = this.dungeonHandler.getDungeonForObj(args[0], player.getLocation());
                if (dungeon == null) {
                    player.sendMessage(ChatColor.RED + "You must be standing in a valid dungeon!");
                    return;
                }
                dungeon.createDungeonDoor(player);
                return;
            }
            dungeon.createDoor(player, args[0]);
        } else {
            player.sendMessage(ChatColor.RED + "Invalid arguments.");
        }
    }
    public void spawnRewardChest(Player player, String[] args) {
        if (args.length > 0) {
            Dungeon dungeon = this.dungeonHandler.getDungeonForRoom(player.getLocation());
            if (dungeon == null) {
                player.sendMessage(ChatColor.RED + "You must be standing in a valid dungeon!");
                return;
            }
            if (!dungeon.template.containsRoomWithName(args[0])){
                player.sendMessage(ChatColor.RED + "You must specify one of the dungeon's defined rooms!");
                return;
            }
            dungeon.createRewardChest(player, args[0]);
            return;
        } else {
            player.sendMessage(ChatColor.RED + "Invalid arguments.");
        }
    }
    public void spawnExit(Player player, String[] args) {
        if (args.length > 1) {
            Dungeon dungeon = this.dungeonHandler.getDungeonForObj(args[0], player.getLocation());
            if (dungeon == null) {
                player.sendMessage(ChatColor.RED + "You must be specify a valid dungeon!");
                return;
            }
            if (args[1].equalsIgnoreCase("target")) {
                dungeon.createExitTarget(player);
            } else if (args[1].equalsIgnoreCase("entry")) {
                dungeon.createExitWarp(player);
            } else {
                player.sendMessage(ChatColor.RED + "You must specify either ''entry'' or ''target''!");
            }

            return;
        } else {
            player.sendMessage(ChatColor.RED + "Invalid arguments.");
        }
    }
    public void spawnRoom(Player player, String[] args) {
        Dungeon dungeon = this.dungeonHandler.getDungeonForRoom(player.getLocation());
        if (dungeon == null) {
            player.sendMessage(ChatColor.RED + "You must be standing in a valid dungeon!");
            return;
        }
        if (!dungeon.template.containsRoomWithName(args[0])){
            player.sendMessage(ChatColor.RED + "You must specify one of the dungeon's defined rooms!");
            return;
        }
        RoomData data = dungeon.template.getRoomWithName(args[0]);
        dungeon.createRoom(player, data.goal, data.name, data.quantity, data.toProgress, data.preventAbilities, data.doorClosedByDefault);
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
                cropObj.spawn(loc);
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
                zone.spawn(player);

            }
        } else {
            player.sendMessage(ChatColor.RED + "You must specify a Fishing Zone Template's internal name!");
        }
    }
    public void createForagingZone(Player player, String[] args) {
        if (args.length > 0) {
            ForagingZoneTemplate template = this.foragingZoneHandler.allZones.stream().filter(obj -> obj.name.equalsIgnoreCase(args[0])).findFirst().orElse(null);
            if (template != null) {
                ForagingZone zone = new ForagingZone(this, template);
                zone.spawn(player);
                player.sendMessage("Creating foraging zone " + template.getName());
            }
        } else {
            player.sendMessage(ChatColor.RED + "You must specify a Foraging Zone Template's internal name!");
        }
    }

    public void spawnTimingTable(Player player, String[] args) {
        if (args.length > 2) {
            XpType type = Xp.parseXpType(args[1]);
            if (type == null) {
                player.sendMessage(ChatColor.RED + "You must specify a valid table type!");
                return;
            }
            TimingTable table = new TimingTable();
            table.spawn(player.getLocation().toCenterLocation(), args[2], type, this);
        } else {
            player.sendMessage(ChatColor.RED + "You must specify a table type and a name!");
        }
    }
    public void spawnMashingTable(Player player, String[] args) {
        if (args.length > 2) {
            XpType type = Xp.parseXpType(args[1]);
            if (type == null) {
                player.sendMessage(ChatColor.RED + "You must specify a valid table type!");
                return;
            }
            MashingTable table = new MashingTable();
            table.spawn(player.getLocation().toCenterLocation(), args[2], type, this);
        } else {
            player.sendMessage(ChatColor.RED + "You must specify a table type and a name!");
        }
    }
    public void spawnHoldingTable(Player player, String[] args) {
        if (args.length > 2) {
            XpType type = Xp.parseXpType(args[1]);
            if (type == null) {
                player.sendMessage(ChatColor.RED + "You must specify a valid table type!");
                return;
            }
            HoldingTable table = new HoldingTable();
            table.spawn(player.getLocation().toCenterLocation(), args[2], type, this);
        } else {
            player.sendMessage(ChatColor.RED + "You must specify a table type and a name!");
        }
    }
    public void createStation(Player player, String[] args) {
        if (args.length > 1) {
            XpType type = Xp.parseXpType(args[1]);
            if (type == null) {
                player.sendMessage(ChatColor.RED + "You must specify a valid station type!");
                return;
            }
            Station station = new Station(this, type);
            station.spawn(player);
        } else {
            player.sendMessage(ChatColor.RED + "You must specify a valid station type!");
        }
    }

    public MMOPlayer createNewPlayer(Player player) {
        player.setHealth(player.getMaxHealth());
        MMOPlayer mmoPlayer = new MMOPlayer();
        mmoPlayer.plugin = this;
        UUID id = player.getUniqueId();
        mmoPlayer.setMinecraftUUID(id);
        mmoPlayer.setHealth(plugin.defaultStatValues.get("health"));
        mmoPlayer.setBaseHealth(mmoPlayer.getHealth());
        mmoPlayer.setMaxHealth(mmoPlayer.getHealth());
        mmoPlayer.setBaseMaxHealth(mmoPlayer.getHealth());
        mmoPlayer.setDefense(plugin.defaultStatValues.get("defense"));
        mmoPlayer.setBaseDefense(mmoPlayer.getDefense());
        mmoPlayer.setStrength(plugin.defaultStatValues.get("strength"));
        mmoPlayer.setBaseStrength(mmoPlayer.getDamage());
        mmoPlayer.setCritDamage(plugin.defaultStatValues.get("crit-damage"));
        mmoPlayer.setBaseCritDamage(mmoPlayer.getCritDamage());
        mmoPlayer.setSpeed(plugin.defaultStatValues.get("speed"));
        mmoPlayer.setBaseSpeed(mmoPlayer.getSpeed());
        mmoPlayer.setCritChance(plugin.defaultStatValues.get("crit-chance"));
        mmoPlayer.setBaseCritChance(mmoPlayer.getCritChance());
        mmoPlayer.setMaxMana(plugin.defaultStatValues.get("stamina"));
        mmoPlayer.setBaseMaxMana(mmoPlayer.getMaxMana());
        mmoPlayer.setMana(mmoPlayer.getMaxMana());
        mmoPlayer.setAbilityDamage(plugin.defaultStatValues.get("ability-damage"));
        mmoPlayer.setBaseAbilityDamage(mmoPlayer.getAbilityDamage());
        mmoPlayer.setVendorPrice(plugin.defaultStatValues.get("shop-discount"));
        mmoPlayer.setBaseVendorPrice(mmoPlayer.getVendorPrice());
        mmoPlayer.setDialogueSpeed(plugin.defaultStatValues.get("dialogue-speed"));
        mmoPlayer.setBaseDialogueSpeed(mmoPlayer.getDialogueSpeed());
        mmoPlayer.setHealthRegen(plugin.defaultStatValues.get("health-regen"));
        mmoPlayer.setBaseHealthRegen(mmoPlayer.getHealthRegen());
        mmoPlayer.setManaRegen(plugin.defaultStatValues.get("stamina-regen"));
        mmoPlayer.setBaseManaRegen(mmoPlayer.getManaRegen());
        mmoPlayer.setLure(defaultStatValues.get("fishing-speed"));
        mmoPlayer.setBaseLure(mmoPlayer.getLure());
        mmoPlayer.setFlash(defaultStatValues.get("lure"));
        mmoPlayer.setBaseFlash(mmoPlayer.getFlash());
        mmoPlayer.setFarmingFortune(defaultStatValues.get("farming-fortune"));
        mmoPlayer.setBaseFarmingFortune(mmoPlayer.getBaseFarmingFortune());
        mmoPlayer.setMiningFortune(defaultStatValues.get("mining-fortune"));
        mmoPlayer.setBaseMiningFortune(mmoPlayer.getMiningFortune());
        mmoPlayer.setLoggingFortune(defaultStatValues.get("foraging-fortune"));
        mmoPlayer.setBaseLoggingFortune(mmoPlayer.getLoggingFortune());
        mmoPlayer.setVigor(defaultStatValues.get("vigor"));
        mmoPlayer.setBaseVigor(mmoPlayer.getVigor());
        MagicSpells.getManaHandler().setMaxMana(player, (int)mmoPlayer.getMaxMana());
        MagicSpells.getManaHandler().setMana(player, (int)mmoPlayer.getMaxMana(), ManaChangeReason.OTHER);
        mmoPlayer.setCombatLvl(0);
        mmoPlayer.setLoggingLvl(0);
        mmoPlayer.setMiningLvl(0);
        mmoPlayer.setFishingLvl(0);
        mmoPlayer.setFarmingLvl(0);
        mmoPlayer.setSmithingLvl(0);
        mmoPlayer.setCookingLvl(0);
        mmoPlayer.setTailoringLvl(0);
        mmoPlayer.setTinkeringLvl(0);
        mmoPlayer.setTinkeringProg(0.0f);
        mmoPlayer.setTailoringProg(0.0f);
        mmoPlayer.setCombatProg(0.0f);
        mmoPlayer.setMiningProg(0.0f);
        mmoPlayer.setFishingProg(0.0f);
        mmoPlayer.setLoggingProg(0.0f);
        mmoPlayer.setFarmingProg(0.0f);
        mmoPlayer.setSmithingProg(0.0f);
        mmoPlayer.setCookingProg(0.0f);;
        mmoPlayer.setRespawnLocation(player.getLocation().getWorld().getSpawnLocation());
        mmoPlayer.removePurseGold(mmoPlayer.getPurseGold());
        plugin.econ.createBank("Bank." + player.getName(), Bukkit.getOfflinePlayer(player.getUniqueId()));
        mmoPlayer.removeBankGold(mmoPlayer.getBankGold());
        this.gameScore.put(player.getUniqueId(), 0);
        player.setLevel(0);
        player.setExp(0.0f);
        mmoPlayer.addPurseGold(defaultStatValues.get("dosh"));
        return mmoPlayer;
    }

    public void accessMobileBank(Player player) {
        MMOPlayer mmoPlayer = plugin.players.get(player.getUniqueId());
        if (mmoPlayer.mobileBankLvl == 0) {
            return;
        } else if (mmoPlayer.mobileBankLvl == 1) {
            if (mmoPlayer.lastMobileBankUse + (600 * 1000L) <= System.currentTimeMillis()) {
                this.menus.openRemoteBankMenu(player);
                return;
            }

        } else if (mmoPlayer.mobileBankLvl == 2) {
            if (mmoPlayer.lastMobileBankUse + (300 * 1000L) <= System.currentTimeMillis()) {
                this.menus.openRemoteBankMenu(player);
                return;
            }

        } else if (mmoPlayer.mobileBankLvl == 3) {
            if (mmoPlayer.lastMobileBankUse + (120 * 1000L) <= System.currentTimeMillis()) {
                this.menus.openRemoteBankMenu(player);
                return;
            }

        } else {
            this.menus.openRemoteBankMenu(player);
            return;
        }
        int secsOnCd = (int)(System.currentTimeMillis() - mmoPlayer.lastMobileBankUse)/1000;
        player.sendMessage(ChatColor.RED + "Your Mobile Banking is still on cooldown for " + formatMinsAndSecsRemaining(secsOnCd) + ".");
    }
    private String formatMinsAndSecsRemaining(long time) {
        String retString = "";
        time = System.currentTimeMillis() - time;
        long mins = TimeUnit.MILLISECONDS.toMinutes(time);
        time = time - TimeUnit.MINUTES.toMillis(mins);
        long secs = TimeUnit.MILLISECONDS.toSeconds(time);
        if (mins > 0 && secs > 0) {
            retString = retString + mins;
            if (mins > 1) {
                 retString = retString + " minutes ";
            } else {
                retString = retString +" minute ";
            }
            retString = retString + "and " + secs;
            if (secs > 1) {
                retString = retString + " seconds";
            } else {
                retString = retString + " second";
            }
        }
        if (mins > 0 && secs <= 0) {
            retString = retString + mins;
            if (mins > 1) {
                retString = retString + " minutes";
            } else {
                retString = retString +" minute";
            }
        }
        if (mins <= 0 && secs > 0) {
            retString = retString + secs;
            if (secs > 1) {
                retString = retString + " seconds";
            } else {
                retString = retString + " second";
            }
        }
        return retString;
    }



    public void displayStats() {
        Iterator iter = Bukkit.getOnlinePlayers().iterator();

        while (iter.hasNext()) {
            Player p = (Player) iter.next();
            if (this.connectTime.get(p.getUniqueId()) > System.currentTimeMillis() + 250L) {
                return;
            }
            if (players.containsKey(p.getUniqueId())) {
                plugin.updatePlayerMaxMana(plugin.players.get(p.getUniqueId()));
                plugin.updatePlayerSpeed(plugin.players.get(p.getUniqueId()));
                this.statUpdates.updateFullInventory(p, plugin.players.get(p.getUniqueId()));
                this.actionBarMessenger.display(p);
                plugin.questHandler.checkInvForFulfillingItems(p);
                plugin.questHandler.updateTrackingDetails(p);
                if (plugin.itemHandler.getItemFromString("tgt_menu") != null) {
                    if (p.getInventory().getItem(8) != null) {
                        NBTItem nbtI = new NBTItem(p.getInventory().getItem(8));
                        NBTCompound comp = nbtI.getCompound("ExtraAttributes");
                        if (comp != null && comp.hasTag("id") && comp.getString("id").equalsIgnoreCase("tgt_menu")) {

                        } else {
                            ItemStack item = plugin.itemHandler.getItemFromString("tgt_menu");
                            p.getInventory().setItem(8, item);
                        }

                    } else {
                        ItemStack item = plugin.itemHandler.getItemFromString("tgt_menu");
                        p.getInventory().setItem(8, item);
                    }
                }
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
                /*if (players.get(p.getUniqueId()).getHealth() + healthToAdd >= players.get(p.getUniqueId()).getMaxHealth()) {
                    players.get(p.getUniqueId()).setHealth(players.get(p.getUniqueId()).getMaxHealth());
                    return;
                }*/
                this.updatePlayerHealth(players.get(p.getUniqueId()), (float)healthToAdd);

            } else {
                this.getLogger().severe("PLAYER " + p.getName() + " HAS NO MMOPLAYER ENTRY");
            }
        }
    }
    public void updatePlayerHealth(MMOPlayer player, float change) {
        float newHealth = player.getHealth() + change;
        Player bukkitPlayer = Bukkit.getPlayer(player.getMinecraftUUID());
        float absorptionHealth = player.getAbsorptionHealth();
        if (bukkitPlayer == null || bukkitPlayer.isDead()) {
            return;
        }
        if (newHealth <= 0) {
            player.setHealth(0.0f);
            bukkitPlayer.setHealth(0.0D);
            return;
        }
        int hearts = getHeartsToDisplay(player.getMaxHealth());
        bukkitPlayer.setMaxHealth(hearts);

        if (absorptionHealth > 0) {
            bukkitPlayer.setAbsorptionAmount(getAbsorptionHeartsToDisplay(absorptionHealth));
            player.takeDamage(0.0f);
        } else if (absorptionHealth <= 0 && bukkitPlayer.getAbsorptionAmount() != 0) {
            bukkitPlayer.setAbsorptionAmount(0);
            player.takeDamage(0.0f);
        }
        bukkitPlayer.sendHealthUpdate();

        if (newHealth > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
            bukkitPlayer.setHealth(bukkitPlayer.getMaxHealth());
            return;
        }

        player.setHealth(newHealth);
        bukkitPlayer.setHealth((player.getHealth()/player.getMaxHealth()) * hearts);
    }

    public int getHeartsToDisplay(float health) {
        if (health >= 1250) {
            return 40;
        } else if (health >= 1000) {
            return 38;
        } else if (health >= 800) {
            return 36;
        } else if (health >= 650) {
            return 34;
        } else if (health >= 500) {
            return 32;
        } else if (health >= 400) {
            return 30;
        } else if (health >= 300) {
            return 28;
        } else if (health >= 230) {
            return 26;
        } else if (health >= 165) {
            return 24;
        } else if (health >= 125) {
            return 22;
        } else {
            return 20;
        }
    }
    public int getAbsorptionHeartsToDisplay(float health) {
        if (health <= 0) {
            return 0;
        }else if (health <= 100) {
            return 2;
        } else if (health <= 200) {
            return 4;
        } else if (health <= 300) {
            return 6;
        } else if (health <= 400) {
            return 8;
        } else if (health <= 500) {
            return 10;
        } else if (health <= 600) {
            return 12;
        } else if (health <= 700) {
            return 14;
        } else if (health <= 800) {
            return 16;
        } else if (health <= 900) {
            return 18;
        } else {
            return 20;
        }
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
        double returnDam = statUpdates.calculateNormalDamage(damage, player, isCrit);
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

    public double calculateDamageAxe(MMOPlayer player, ItemStack item, boolean isCrit) {

        Player bukkitPlayer = Bukkit.getPlayer(player.getMinecraftUUID());
        float damage = 1;
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
            Iterator iter = item.getItemMeta().getLore().iterator();
            while (iter.hasNext()) {
                String line = (String) iter.next();
                if (ChatColor.stripColor(line).startsWith("Logging Damage: ")) {
                    line = ChatColor.stripColor(line);
                    line = line.replaceFirst("(Logging Damage: +\\+)", "");
                    damage = Float.parseFloat(line);
                }
            }
        }
        if (isCrit) {
            return (5+damage) * (1+(player.getStrength()/100));
        } else {
            return (5+damage);
        }

    }



    public void updatePlayerMaxMana(MMOPlayer player) {
        Player bukkitPlayer = Bukkit.getPlayer(player.getMinecraftUUID());
        int newMaxMana = (int) (player.getMaxMana() + player.getVigor());
        MagicSpells.getManaHandler().setMaxMana(bukkitPlayer, newMaxMana);
        MagicSpells.getManaHandler().setRegenAmount(bukkitPlayer, getPlayerManaRegen(player,bukkitPlayer));
    }

    public int getPlayerManaRegen(MMOPlayer mmoPlayer, Player player) {
        float regen = ((float) (MagicSpells.getManaHandler().getMaxMana(player) * 0.02)) + mmoPlayer.getManaRegen();
        return (int) regen;
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
    public void setDayTime(long value) {
        float fractionalValue = (float)value / (dayLength*20L);
        long timeToSetTo = (long) ((14000 * fractionalValue) - 1000);
        if (timeToSetTo < 0) {
            timeToSetTo += 24000;
        }
        Bukkit.getWorlds().get(0).setTime(timeToSetTo);
    }
    public void setNightTime(long value) {
        float fractionalValue = (float)value / (nightLength*20L);
        long timeToSetTo = (long) ((10000 * fractionalValue) + 13000);

        Bukkit.getWorlds().get(0).setTime(timeToSetTo);
    }


    @Override
    public int getGameModeId() {
        return 4;
    }

    @Override
    public String getGameModeCode() {
        return "TGT";
    }

    @Override
    public String getGameModeName() {
        return "The Tournament";
    }

    @Override
    public void startGame() {
        this.gameStartTime = System.currentTimeMillis();
        this.currentGameTime = 0L;
        this.gameRunning = true;
        Bukkit.getWorlds().get(0).setTime(23000L);
        Bukkit.getWorlds().get(0).setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        new BukkitRunnable() {
            @Override
            public void run() {
                currentGameTime++;
                if (!night) {
                    if (currentGameTime > dayLength*20L) {
                        night=true;
                        currentGameTime = 0L;
                    }
                } else {
                    if (currentGameTime > nightLength*20L) {
                        night=false;
                        currentGameTime = 0L;
                        day++;
                        dayAnnouncement = false;
                    }
                }
                if (day > daysBeforeTournament && !tournament && !dayAnnouncement) {
                    TGT.this.startTournament();
                    gameCaster.speakHerald(ChatColor.WHITE + "The day of "+ ChatColor.GOLD + "The Tournament " + ChatColor.WHITE + "has arrived! Competitors, prepare yourselves...", 0);
                    dayAnnouncement = true;
                } else if (day <= daysBeforeTournament && !tournament && !dayAnnouncement) {
                    if (daysBeforeTournament-day+1 == 1) {
                        gameCaster.speakHerald(ChatColor.WHITE + "Hear ye, hear ye! "+ ChatColor.GOLD + "The Tournament " + ChatColor.WHITE + "will take place in " + (daysBeforeTournament-day+1) + " day!", 0);
                    } else {
                        gameCaster.speakHerald(ChatColor.WHITE + "Hear ye, hear ye! "+ ChatColor.GOLD + "The Tournament " + ChatColor.WHITE + "will take place in " + (daysBeforeTournament-day+1) + " days!", 0);
                    }
                    dayAnnouncement = true;
                }
                if (!night) {
                    setDayTime(currentGameTime);
                } else {
                    setNightTime(currentGameTime);
                }
            }
        }.runTaskTimer(this, 20L, 1L);
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    sendScoreboard(p);
                }
            }
        }.runTaskTimer(this, 20L, 20L);
        this.displayTask = Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            public void run() {
                TGT.this.displayStats();
            }
        }, (long)(10), (long)(10));

    }


    public void onDisable() {
        File folder = MythicBukkit.inst().getSpawnerManager().getSpawnerFolder();
        File[] files = folder.listFiles();
        if(files!=null) {
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        files = CitizensAPI.getDataFolder().listFiles();
        if (files != null) {
            for(File f: files) {
                if (f.getName().equalsIgnoreCase("saves.yml")) {
                    f.delete();
                }
            }
        }
        for (Ore ore : oreHandler.allSpawnedOres) {
            ore.remove();
        }
        for (Crop crop : cropHandler.allSpawnedCrops) {
            crop.remove();
        }
        for (FishingZone zone : fishingZoneList) {
            zone.remove();
        }
        for (ForagingZone zone : foragingZoneHandler.allSpawnedZones) {
            zone.remove();
        }
        for (Spawner spawner : spawnerHandler.allSpawnedSpawners) {
            spawner.remove();
        }
        for (Dungeon dungeon : dungeonHandler.allDungeons) {
            dungeon.remove();
        }
        for (Station station : tableHandler.allStations) {
            station.remove();
        }
    }

    public static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if(files!=null) {
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

    public boolean debugEnabled(Player player) {
        return this.debugEnabled.contains(player.getUniqueId());
    }
    public boolean debugEnabled(UUID uuid) {
        return this.debugEnabled.contains(uuid);
    }
    public void enableDebug(Player player) {
        this.debugEnabled.add(player.getUniqueId());
    }
    public void disableDebug(Player player) {
        this.debugEnabled.remove(player.getUniqueId());
    }
    public String trimCasterDialogue(String string) {
        if (string.startsWith("h:")) {
            return string.replaceFirst("h:","");
        } else if (string.startsWith("Herald:")) {
            return string.replaceFirst("Herald:", "");
        } else if (string.startsWith("herald:")) {
            return string.replaceFirst("herald:", "");
        } else if (string.startsWith("k:")) {
            return string.replaceFirst("k:", "");
        } else if (string.startsWith("King:")) {
            return string.replaceFirst("King:","");
        } else if (string.startsWith("king:")) {
            return string.replaceFirst("king:","");
        } else {
            return string;
        }
    }

    @Override
    public void endGame() {
        this.gameRunning = false;
    }


    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());
        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    private int getTrueStringLength(String string) {
        return ChatColor.stripColor(string).length();
    }
    private int getLongestString(List<String> list) {
        String longestString = list.get(0);
        int longestLength = getTrueStringLength(longestString);
        for (int i = 1; i < list.size(); i++) {
            String str = list.get(i);
            if (str.length() > longestLength) {
                longestString = str;
                longestLength = str.length();
            }
        }
        return longestLength;
    }

    private String formatScoreString(String leftSide, String rightSide, int longest) {
        while (leftSide.length() + rightSide.length() < longest) {
            leftSide = leftSide + " ";
        }
        return leftSide + rightSide;
    }

}
