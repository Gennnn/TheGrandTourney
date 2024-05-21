//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package me.genn.thegrandtourney.tournament;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.Spell.SpellCastState;
//import com.nisovin.magicspells.materials.MagicMaterial;
import java.io.File;
import java.util.*;
import java.util.logging.Level;

import com.nisovin.magicspells.util.Util;
import me.genn.thegrandtourney.TGT;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class MiniGame {
    static Random random = new Random();
    TGT plugin;
    String code;
    public String name;
    String scoreboardName;
    public MiniGameType type;
    boolean pvp;
    boolean crafting;
    boolean joinMidGame;
    public boolean teamsEnabled;
    boolean friendlyFire;
    boolean allowNegative;
    int waitTime;
    int gameLength;
    public List<String> teamNames;
    List<String> initCommands;
    List<String> startCommands;
    List<String> endCommands;
    Spell initSpell;
    Spell startSpell;
    Spell respawnSpell;
    Spell victorySpell;
    Spell endSpell;
    Location initSpawnPoint;
    int initSpawnRadius;
    List<Location> startSpawnPoints;
    int startSpawnRadius;
    List<Location> respawnPoints;
    int respawnRadius;
    Location outSpawnPoint;
    int outSpawnRadius;
    List<Material> breakableBlocks;
    List<Material> interactableBlocks;
    Set<EntityType> interactableEntities;
    public List<String> participants = new ArrayList<>();
    Map<String, Team> teams = new HashMap<>();
    public Map<String, String> playerTeams = new HashMap<>();
    public Map<String, List<String>> teamPlayers = new HashMap<>();
    public Map<String, Float> teamScores = new HashMap<>();
    public boolean gameRunning = false;
    boolean gameEnded = false;
    public long endTime;
    public long gameStartTime;
    public Scoreboard scoreboard;
    public List<String> introductoryText;
    public Map<UUID, Float> playerScores = new HashMap<>();
    public float multiplier = 1.0f;

    public MiniGame(TGT plugin, String code, ConfigurationSection config) {
        this.plugin = plugin;
        this.code = code;
        List<String> list = null;
        this.name = ChatColor.translateAlternateColorCodes('&', config.getString("name", code));
        this.scoreboardName = ChatColor.translateAlternateColorCodes('&', config.getString("scoreboard-name", code));
        String t = config.getString("type", "timed").toLowerCase();
        if (t.contains("last")) {
            this.type = MiniGame.MiniGameType.LAST_MAN_STANDING;
        } else {
            this.type = MiniGame.MiniGameType.TIMED;
        }
        this.pvp = config.getBoolean("pvp", false);
        this.crafting = config.getBoolean("crafting", false);
        this.joinMidGame = config.getBoolean("join-mid-game", false);
        this.teamsEnabled = config.getBoolean("teams-enabled", false);
        this.friendlyFire = config.getBoolean("friendly-fire", false);
        this.allowNegative = config.getBoolean("allow-negative", false);
        this.waitTime = config.getInt("wait-time", 15);
        this.gameLength = config.getInt("game-length", 120);
        this.teamNames = config.getStringList("team-names");
        this.initCommands = config.getStringList("init-commands");
        this.startCommands = config.getStringList("start-commands");
        this.endCommands = config.getStringList("end-commands");
        this.initSpell = MagicSpells.getSpellByInternalName(config.getString("init-spell", ""));
        this.startSpell = MagicSpells.getSpellByInternalName(config.getString("start-spell", ""));
        this.respawnSpell = MagicSpells.getSpellByInternalName(config.getString("respawn-spell", ""));
        this.victorySpell = MagicSpells.getSpellByInternalName(config.getString("victory-spell", ""));
        this.endSpell = MagicSpells.getSpellByInternalName(config.getString("end-spell", ""));
        this.initSpawnPoint = this.getLocationFromString(config.getString("init-spawn-point", ""));
        this.introductoryText = new ArrayList<>();
        for (String str : config.getStringList("introductory-text")) {
            this.introductoryText.add(ChatColor.translateAlternateColorCodes('&', str));
        }

        if (this.initSpawnPoint == null) {
            this.initSpawnPoint = ((World)Bukkit.getWorlds().get(0)).getSpawnLocation();
        }

        this.initSpawnRadius = config.getInt("init-spawn-radius", 0);
        list = config.getStringList("start-spawn-points");
        this.startSpawnPoints = new ArrayList();
        String s;
        Iterator var7;
        Location loc;
        if (list != null && list.size() > 0) {
            var7 = list.iterator();

            while(var7.hasNext()) {
                s = (String)var7.next();
                loc = this.getLocationFromString(s);
                if (loc != null) {
                    this.startSpawnPoints.add(loc);
                }
            }
        }

        this.startSpawnRadius = config.getInt("start-spawn-radius", 0);
        list = config.getStringList("respawn-points");
        this.respawnPoints = new ArrayList();
        if (list != null && list.size() > 0) {
            var7 = list.iterator();

            while(var7.hasNext()) {
                s = (String)var7.next();
                loc = this.getLocationFromString(s);
                if (loc != null) {
                    this.respawnPoints.add(loc);
                }
            }
        } else {
            var7 = this.startSpawnPoints.iterator();

            while(var7.hasNext()) {
                Location loc1 = (Location)var7.next();
                this.respawnPoints.add(loc1);
            }
        }

        this.respawnRadius = config.getInt("respawn-radius", 0);
        this.outSpawnPoint = this.getLocationFromString(config.getString("out-spawn-point", ""));
        if (this.outSpawnPoint == null) {
            this.outSpawnPoint = this.initSpawnPoint;
        }

        this.outSpawnRadius = config.getInt("out-spawn-radius", 0);
        list = config.getStringList("breakable-blocks");
        Material mat;
        if (list != null && list.size() > 0) {
            this.breakableBlocks = new ArrayList();
            var7 = list.iterator();

            while(var7.hasNext()) {
                s = (String)var7.next();
                mat = Util.getMaterial(s);
                if (mat != null) {
                    this.breakableBlocks.add(mat);
                }
            }
        } else {
            this.breakableBlocks = null;
        }

        list = config.getStringList("interactable-blocks");
        if (list != null && list.size() > 0) {
            this.interactableBlocks = new ArrayList();
            var7 = list.iterator();

            while(var7.hasNext()) {
                s = (String)var7.next();
                mat = Util.getMaterial(s);
                if (mat != null) {
                    this.interactableBlocks.add(mat);
                }
            }
        } else {
            this.interactableBlocks = null;
        }
        Bukkit.getLogger().log(Level.INFO, "Registered game " + name);

    }

    public void initialize() {
        this.plugin.getLogger().info("Initializing game " + this.name);
        File baseFolder = new File(this.plugin.getDataFolder(), "magicspellsbase");
        if (!baseFolder.exists()) {
            baseFolder.mkdir();
        }

        File gameFolder = new File(this.plugin.getDataFolder(), this.code);
        if (!gameFolder.exists()) {
            gameFolder.mkdir();
        }

        File magicSpellsFolder = MagicSpells.plugin.getDataFolder();
        File[] var7;
        int var6 = (var7 = magicSpellsFolder.listFiles()).length;

        File file;
        int var5;
        for(var5 = 0; var5 < var6; ++var5) {
            file = var7[var5];
            if (file.isFile() && file.getName().endsWith(".yml")) {
                file.delete();
            }
        }

        var6 = (var7 = baseFolder.listFiles()).length;

        for(var5 = 0; var5 < var6; ++var5) {
            file = var7[var5];
            if (file.isFile() && file.getName().endsWith(".yml")) {
                this.plugin.copyFile(file, magicSpellsFolder);
            }
        }

        var6 = (var7 = gameFolder.listFiles()).length;

        for(var5 = 0; var5 < var6; ++var5) {
            file = var7[var5];
            if (file.isFile() && file.getName().endsWith(".yml")) {
                this.plugin.copyFile(file, magicSpellsFolder);
            }
        }

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ms reload");
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        /*this.objective = this.scoreboard.registerNewObjective(this.code, "CUSTOM");
        this.objective.setDisplayName(this.scoreboardName);
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);*/
        if (this.teamsEnabled) {
            for(int i = 0; i < this.teamNames.size(); ++i) {
                String teamName = (String)this.teamNames.get(i);
                String prefix = "";
                String name = teamName;
                String coloredName = teamName;
                if (teamName.startsWith("&")) {
                    ChatColor color = ChatColor.getByChar(teamName.substring(1, 2));
                    name = teamName.substring(2);
                    coloredName = color + name;
                    prefix = color.toString();
                    this.teamNames.set(i, name);
                }

                Team team = this.scoreboard.registerNewTeam(name);
                team.setPrefix(prefix);
                team.setDisplayName(coloredName);
                team.setAllowFriendlyFire(this.friendlyFire);
                this.teams.put(name, team);
                this.teamPlayers.put(name, new ArrayList<>());
                this.teamScores.put(name, 0.0f);
            }
        }

        if (this.initCommands != null) {
            Iterator var16 = this.initCommands.iterator();

            while(var16.hasNext()) {
                String cmd = (String)var16.next();
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }
        }

        List<Player> var18 = new ArrayList<>(Bukkit.getOnlinePlayers());
        var6 = (var18).size();

        for(var5 = 0; var5 < var6; ++var5) {
            Player player = var18.get(var5);
            if (player.isValid()) {
                this.initPlayer(player);
            }
        }

        final int[] lineNum = {0};
        final long[] delay = {100L};
        int runningDelay = 10;
        for (String line : this.introductoryText) {
            if (line.startsWith("herald:")||line.startsWith("Herald:")||line.startsWith("h:"))  {
                plugin.gameCaster.speakHerald(plugin.trimCasterDialogue(line), runningDelay);
            } else if (line.startsWith("king:")||line.startsWith("King:")||line.startsWith("k:")) {
                plugin.gameCaster.speakKing(plugin.trimCasterDialogue(line), runningDelay);
            }
            String[] wordCount = line.split(" ");
            runningDelay = runningDelay + wordCount.length * 10;
        }
        final int[] secondNum = new int[]{30};
        new BukkitRunnable() {

            @Override
            public void run() {
                if (secondNum[0] == 30 || secondNum[0] == 20 || secondNum[0] == 10 || (secondNum[0] < 6 && secondNum[0] >-1)) {

                    if (secondNum[0]==0) {
                        this.cancel();
                        return;
                    }
                    if (secondNum[0] <= 5) {
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            player.sendTitle("", ChatColor.GOLD.toString() + secondNum[0] + "...", 0, 21, 0);
                        }
                    }
                    plugin.gameCaster.speakHerald(ChatColor.WHITE + "The round will begin in " + ChatColor.YELLOW + secondNum[0] + ChatColor.WHITE + " seconds...", 0);

                }
                secondNum[0]--;

            }
        }.runTaskTimer(plugin, (this.waitTime*20L) - 600L, 20L);
        gameStartTime = System.currentTimeMillis() + (this.waitTime*1000L);
        Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
            public void run() {
                MiniGame.this.start();
            }
        }, (long)(this.waitTime * 20L));

    }


    public void start() {
        this.plugin.getLogger().info("Starting game " + this.name);
        this.gameRunning = true;
        if (this.startCommands != null) {
            Iterator var2 = this.startCommands.iterator();

            while(var2.hasNext()) {
                String cmd = (String)var2.next();
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }
        }

        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        Collections.shuffle(players);
        Iterator<Player> var3 = players.iterator();

        while(var3.hasNext()) {
            Player player = var3.next();
            this.startPlayer(player);
        }
        plugin.gameCaster.speakHerald(ChatColor.GOLD + "BEGIN!", 0);

        this.endTime = System.currentTimeMillis() + (long)(this.gameLength * 1000);
    }

    public void setTimeRemaining(int seconds) {
        this.endTime = System.currentTimeMillis() + (long)(seconds * 1000);
    }

    public double checkPercentTimeRemaining() {
        if (this.gameEnded) {
            return 0.0;
        } else if (!this.gameRunning) {
            return 1.0;
        } else {
            long secondsLeft = (this.endTime - System.currentTimeMillis()) / 1000L;
            if (secondsLeft <= 0L) {
                this.end();
                return 0.0;
            } else {
                return (double)secondsLeft / (double)this.gameLength;
            }
        }
    }

    public void addScore(Player player, float points) {
        if (this.gameRunning) {
            if (this.teamsEnabled) {
                String teamName = (String)this.playerTeams.get(player.getName());
                if (teamName != null) {
                    float score = this.teamScores.get(teamName);

                    float s = score + (points * multiplier);
                    if (s < 0 && !this.allowNegative) {
                        s = 0;
                    }
                    this.teamScores.put(teamName, s);

                }
            } else {
                float score = this.playerScores.get(player.getUniqueId());
                float s = score + (points * multiplier);
                if (s < 0 && !this.allowNegative) {
                    s = 0;
                }
                this.playerScores.put(player.getUniqueId(), s);
            }
            player.playSound(player, "entity.experience_orb.pickup", 0.5f, 2.0f);
            plugin.actionBarMessenger.queuePointsMessage(player, points);
        }
    }

    public void setScore(Player player, float points) {
        if (this.gameRunning) {
            if (this.teamsEnabled) {
                String teamName = (String)this.playerTeams.get(player.getName());
                if (teamName != null) {
                    this.teamScores.put(teamName, points);
                }
            } else {
                this.playerScores.put(player.getUniqueId(), points);
            }

        }
    }

    public float getScore(Player player) {
        if (this.gameRunning) {
            if (this.teamsEnabled) {
                String teamName = (String)this.playerTeams.get(player.getName());
                if (teamName != null) {
                    return this.teamScores.get(teamName);
                }
            } else {
                return this.playerScores.get(player.getUniqueId());
            }
        }
        return 0.0f;
    }



    /*public String getHighestScoringPlayer() {
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (players.size() < 1) {
            return "none";
        } else if (players.size() == 1) {
            return players.get(0).getName();
        } else {
            String playerName = players.get(0).getName();
            Player player;
            do {
                player = Bukkit.getPlayerExact(playerName);
            } while (player == null);
            int maxScore = this.objective.getScore(player).getScore();
            for (int i = 1; i < players.size(); i++) {
                if (players.get(i) == null) {
                    continue;
                }
                Player compPlayer = players.get(i);
                if (this.objective.getScore(compPlayer).getScore() > maxScore) {
                    maxScore = this.objective.getScore(compPlayer).getScore();
                    playerName = compPlayer.getName();
                }
            }
            return playerName;
        }
    }

    public List<String> getWinners() {
        List<Integer> scores = new ArrayList<>();
        List<Player> pList = new ArrayList<>(Bukkit.getOnlinePlayers());
        List<String> retList = new ArrayList<>();
        Iterator<Player> iter = pList.iterator();
        int max = 0;
        while (iter.hasNext()) {
            Player p = iter.next();
            scores.add(this.objective.getScore(p).getScore());
            Bukkit.broadcastMessage("Score for " + p.getName() + " was " + this.objective.getScore(p).getScore());
            if (this.objective.getScore(p).getScore()  > max) {
                max = this.objective.getScore(p).getScore();
            }
        }
        Bukkit.broadcastMessage("Max score was " + max);

        if (Collections.frequency(scores, max) > 1) {
            for (Player p : pList) {
                if (this.objective.getScore(p).getScore() == max) {
                    retList.add(p.getName());
                }
            }
        } else {
            retList.add(getHighestScoringPlayer());
        }
        return retList;
    }*/

    public Player getHighestScoringPlayer() {
        List<Player> players = new ArrayList<>();
        for (UUID id : this.playerScores.keySet()) {
            players.add(Bukkit.getPlayer(id));
        }
        Player highestPlayer = players.get(0);
        float highestScore = this.playerScores.get(highestPlayer.getUniqueId());
        for (int i = 1; i < players.size(); i++) {
            Player player = players.get(i);
            if (this.playerScores.get(player.getUniqueId()) > highestScore) {
                highestPlayer = player;
                highestScore = this.playerScores.get(player.getUniqueId());
            }
        }
        return highestPlayer;
    }

    public Player getHighestScoringPlayer(List<String> playerNames) {
        List<Player> players = new ArrayList<>();
        for (String name : playerNames) {
            players.add(Bukkit.getPlayerExact(name));
        }
        if (players.size() < 1) {
            return null;
        } else if (players.size() == 1) {
            return players.get(0);
        }
        Player highestPlayer = players.get(0);
        float highestScore = this.playerScores.get(highestPlayer.getUniqueId());
        for (int i = 1; i < players.size(); i++) {
            Player player = players.get(i);
            if (this.playerScores.get(player.getUniqueId()) > highestScore) {
                highestPlayer = player;
                highestScore = this.playerScores.get(player.getUniqueId());
            }
        }
        return highestPlayer;
    }

    public String getHighestScoringTeamName() {
        String highestTeam = this.teamNames.get(0);
        float highestScore = this.teamScores.get(highestTeam);
        for (int i = 1; i < teamNames.size(); i++) {
            String teamName = teamNames.get(i);
            if (this.teamScores.get(teamName) > highestScore) {
                highestTeam = teamName;
                highestScore = this.teamScores.get(teamName);
            }
        }
        return highestTeam;
    }

    public void annoucementMessageIndividuals(List<String> winners) {
        if (winners.size() == 1) {
            Bukkit.broadcastMessage(ChatColor.WHITE + "<" + ChatColor.YELLOW + "Herald" + ChatColor.WHITE + "> The winner of " + this.name + ChatColor.WHITE + " is " + ChatColor.GOLD + winners.get(0) + ChatColor.WHITE + "!");
        } else if (winners.size() > 1) {
            String winnersString = ChatColor.GOLD + winners.get(0);
            for (int i = 1; i < winners.size(); i++) {
                if (i == winners.size()-1) {
                    winnersString += ChatColor.WHITE + " and " + ChatColor.GOLD + winners.get(i);
                } else {
                    winnersString += ChatColor.WHITE + ", " + ChatColor.GOLD + winners.get(i);
                }
            }
            Bukkit.broadcastMessage(ChatColor.WHITE + "<" + ChatColor.YELLOW + "Herald" + ChatColor.WHITE + "> The winners of " + this.name + ChatColor.WHITE + " are " + winnersString + ChatColor.WHITE + "!");
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), "entity.villager.trade", 5.0f, 1.25f);
        }
    }

    public void annoucementMessageTeams(List<String> winners, boolean tie) {
        if (winners.size() == 1) {
            Bukkit.broadcastMessage(ChatColor.WHITE + "<" + ChatColor.YELLOW + "Herald" + ChatColor.WHITE + "> The winning team of " + this.name + ChatColor.WHITE + " is the " + ChatColor.RESET + this.scoreboard.getTeam(winners.get(0)).getPrefix() + winners.get(0) + ChatColor.WHITE + " Team!");
        } else if (winners.size() > 1) {
            String winnersString = ChatColor.GOLD + winners.get(0);
            for (int i = 1; i < winners.size(); i++) {
                if (i == winners.size()-1) {
                    winnersString += ChatColor.WHITE + " and " +ChatColor.RESET + this.scoreboard.getTeam(winners.get(i)).getPrefix() + winners.get(i);
                } else {
                    winnersString += ChatColor.WHITE + ", " + ChatColor.RESET + this.scoreboard.getTeam(winners.get(i)).getPrefix() + winners.get(i);
                }
            }
            Bukkit.broadcastMessage(ChatColor.WHITE + "<" + ChatColor.YELLOW + "Herald" + ChatColor.WHITE + "> The winning teams of " + this.name + " are " + winnersString + ChatColor.WHITE + " Teams!");
        } else if (!tie) {
            Bukkit.broadcastMessage(ChatColor.WHITE + "<" + ChatColor.YELLOW + "Herald" + ChatColor.WHITE + "> No teams won this round. What a shame!");

        } else {
            Bukkit.broadcastMessage(ChatColor.WHITE + "<" + ChatColor.YELLOW + "Herald" + ChatColor.WHITE + "> It seems both teams tied! I'm aftraid that means no points will be awarded!");

        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), "entity.villager.trade", 5.0f, 1.25f);
        }
    }

    public void end() {
        this.plugin.getLogger().info("Ending game " + this.name);
        if (this.endCommands != null) {
            String command;
            for (String endCommand : this.endCommands) {
                command = endCommand;
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            }
        }
        Player player;
        for (Player player1 : Bukkit.getOnlinePlayers()) {
            int score = plugin.gameScore.get(player1.getUniqueId());
            if (teamsEnabled) {
                String teamName = this.playerTeams.get(player1.getName());
                if (teamName != null) {
                    float teamScore = this.teamScores.get(teamName);
                    plugin.gameScore.put(player1.getUniqueId(), score + Math.round(teamScore));
                }
            } else {
                plugin.gameScore.put(player1.getUniqueId(), Math.round(this.playerScores.get(player1.getUniqueId())) + score);
            }

        }
        if (victorySpell != null) {
            if (!this.teamsEnabled) {
                if (this.type == MiniGameType.LAST_MAN_STANDING) {
                    if (this.participants.size() == 1) {
                        player = Bukkit.getPlayerExact(this.participants.get(0));
                        if (player != null) {
                            this.victorySpell.castSpell(player, SpellCastState.NORMAL, 1.0F, (String[])null);
                        }
                    } else if (this.participants.size() > 1) {
                        for (String playerName : this.participants) {
                            player = Bukkit.getPlayerExact(playerName);
                            if (player != null) {
                                this.victorySpell.castSpell(player, SpellCastState.NORMAL, 1.0F, (String[])null);
                            }
                        }
                    }
                } else {
                    player = getHighestScoringPlayer();
                    if (player != null) {
                        this.victorySpell.castSpell(player, SpellCastState.NORMAL, 1.0F, (String[])null);
                    }
                }
            } else {
                if (this.type == MiniGameType.LAST_MAN_STANDING) {
                    if (this.participants.size() == 1) {
                        player = Bukkit.getPlayerExact(this.participants.get(0));
                        /*String teamName = this.playerTeams.get(player.getName());
                        if (teamName != null) {
                            List<String> playerNames = this.teamPlayers.get(teamName);
                            for (String playerName : playerNames) {
                                Player teamPlayer = Bukkit.getPlayerExact(playerName);
                                if (teamPlayer != null) {
                                    this.victorySpell.castSpell(teamPlayer, SpellCastState.NORMAL, 1.0F, (String[])null);

                                }
                            }
                        }*/
                        if (player != null) {
                            this.victorySpell.castSpell(player, SpellCastState.NORMAL, 1.0F, (String[])null);
                        }
                    } else if (this.participants.size() > 1) {
                        for (String name : this.participants) {
                            player = Bukkit.getPlayerExact(name);
                            if (player != null) {
                                this.victorySpell.castSpell(player, SpellCastState.NORMAL, 1.0F, (String[])null);
                            }
                        }
                    }
                } else {
                    String teamName = getHighestScoringTeamName();
                    if (teamName != null) {
                        List<String> teamPlayers = this.teamPlayers.get(teamName);
                        player = getHighestScoringPlayer(teamPlayers);
                        if (player != null) {
                            this.victorySpell.castSpell(player, SpellCastState.NORMAL, 1.0F, (String[])null);
                        }
                    }
                }
            }
        }
        /*if (this.victorySpell != null) {
            if (!this.teamsEnabled) {
                if (this.type == MiniGame.MiniGameType.LAST_MAN_STANDING && this.participants.size() == 1) {
                    player = Bukkit.getPlayerExact((String)this.participants.get(0));
                    if (player != null) {
                        this.victorySpell.castSpell(player, SpellCastState.NORMAL, 1.0F, (String[])null);
                        List<String> winner = new ArrayList<>();
                        winner.add(player.getName());
                        annoucementMessageIndividuals(winner);
                    }

                } else if (this.participants.size() > 1) {
                    List<String> winners = new ArrayList<>();
                    for (String playerName : this.participants) {
                        player = Bukkit.getPlayerExact(playerName);

                        if (player != null) {
                            this.victorySpell.castSpell(player, SpellCastState.NORMAL, 1.0F, (String[])null);
                            winners.add(playerName);
                        }
                    }
                    annoucementMessageIndividuals(winners);
                } else if (this.type == MiniGameType.TIMED) {
                    List<String> winners = getWinners();
                    for (String winner : winners) {
                        player = Bukkit.getPlayerExact(winner);
                        if (player != null) {
                            this.victorySpell.castSpell(player,  SpellCastState.NORMAL, 1.0F, (String[])null);
                        } else {
                            winners.remove(winner);
                        }
                    }
                    annoucementMessageIndividuals(winners);

                }
            } else if (this.type == MiniGame.MiniGameType.LAST_MAN_STANDING) {
                String winningTeam = null;
                Iterator var3 = this.teamPlayers.keySet().iterator();

                String playerName;
                while(var3.hasNext()) {
                    playerName = (String)var3.next();
                    if (((List)this.teamPlayers.get(playerName)).size() > 0) {
                        if (winningTeam != null) {
                            winningTeam = null;
                            break;
                        }

                        winningTeam = playerName;
                    }
                }

                if (winningTeam != null) {
                    var3 = this.playerTeams.keySet().iterator();

                    while(var3.hasNext()) {
                        playerName = (String)var3.next();
                        if (((String)this.playerTeams.get(playerName)).equals(winningTeam)) {
                            Player player2 = Bukkit.getPlayerExact(playerName);
                            if (player2 != null) {
                                this.victorySpell.castSpell(player2, SpellCastState.NORMAL, 1.0F, (String[])null);
                            }
                        }
                    }
                    List<String> winner = new ArrayList<>();
                    winner.add(winningTeam);
                    annoucementMessageTeams(winner, false);
                } else {
                    List<String> winner = new ArrayList<>();
                    annoucementMessageTeams(winner, false);
                }
            } else if (this.type == MiniGame.MiniGameType.TIMED) {
                String winningTeam = null;
                highestScore = 0;
                boolean tie = false;
                Iterator var5 = this.teams.keySet().iterator();

                String playerName;
                while(var5.hasNext()) {
                    playerName = (String)var5.next();
                    float score = this.teamScores.get(playerName);
                    if (score > highestScore) {
                        winningTeam = playerName;
                        highestScore = score;
                        tie = false;
                    } else if (score > 0 && score == highestScore) {
                        tie = true;
                    }
                }

                if (winningTeam != null && !tie) {
                    var5 = ((List)this.teamPlayers.get(winningTeam)).iterator();

                    while(var5.hasNext()) {
                        playerName = (String)var5.next();
                        Player player2 = Bukkit.getPlayerExact(playerName);
                        if (player2 != null) {
                            this.victorySpell.castSpell(player2, SpellCastState.NORMAL, 1.0F, (String[])null);
                        }

                    }
                    List<String> winner = new ArrayList<>();
                    winner.add(winningTeam);
                    annoucementMessageTeams(winner, false);
                } else {
                    List<String> winner = new ArrayList<>();
                    annoucementMessageTeams(winner, true);
                }
            }
        }*/
        if (this.endSpell != null) {
            for (Player endSpellPlayer : Bukkit.getOnlinePlayers()) {
                this.endSpell.castSpell(endSpellPlayer, SpellCastState.NORMAL, 1.0F, (String[])null);
            }
        }

        if (this.teamsEnabled) {
            for (Team team : this.scoreboard.getTeams()) {
                team.unregister();
            }
        }



        this.gameRunning = false;
        this.gameEnded = true;
    }

    public void playerJoin(Player player) {
        this.playerRespawn(player);
    }

    public void playerRespawn(Player player) {
        if (this.gameEnded) {
            player.teleport(this.getRespawnLocation(player));
        } else if (this.gameRunning) {
            if (!this.participants.contains(player.getName())) {
                if (this.joinMidGame && this.type != MiniGame.MiniGameType.LAST_MAN_STANDING) {
                    this.initPlayer(player);
                    this.startPlayer(player);
                } else {
                    this.setPlayerOut(player);
                }
            } else {
                this.respawnPlayer(player);
            }
        } else {
            this.initPlayer(player);
        }

    }

    public void playerDeath(Player player) {
        if (this.type == MiniGame.MiniGameType.LAST_MAN_STANDING) {
            this.participants.remove(player.getName());
            if (this.gameRunning) {
                if (this.teamsEnabled) {
                    String teamName = (String)this.playerTeams.get(player.getName());
                    if (teamName != null) {
                        ((List)this.teamPlayers.get(teamName)).remove(player.getName());
                    }
                }

                this.checkLastManStandingEnd();
            }
        }

    }

    public void playerQuit(Player player) {
        this.participants.remove(player.getName());
        if (this.teamsEnabled) {
            String teamName = (String)this.playerTeams.remove(player.getName());
            if (teamName != null) {
                ((List)this.teamPlayers.get(teamName)).remove(player.getName());
            }
        }

        if (this.gameRunning && this.type == MiniGame.MiniGameType.LAST_MAN_STANDING) {
            this.checkLastManStandingEnd();
        }

    }

    private void checkLastManStandingEnd() {
        if (this.participants.size() == 0) {
            this.end();
        } else {
            if (!this.teamsEnabled && this.participants.size() == 1) {
                this.end();
            } else if (this.teamsEnabled) {
                String winningTeam = null;
                Iterator var3 = this.teamPlayers.keySet().iterator();

                while(var3.hasNext()) {
                    String team = (String)var3.next();
                    if (((List)this.teamPlayers.get(team)).size() > 0) {
                        if (winningTeam != null) {
                            winningTeam = null;
                            break;
                        }

                        winningTeam = team;
                    }
                }

                if (winningTeam != null) {
                    this.end();
                }
            }

        }
    }

    public boolean canBreak(Block block) {
        if (this.gameRunning && !this.gameEnded && this.breakableBlocks != null) {
            Iterator<Material> var3 = this.breakableBlocks.iterator();

            while(var3.hasNext()) {
                Material mat = var3.next();
                if (mat == block.getType() ) {
                    return true;
                }
            }

        }
        return false;
    }

    public boolean canInteract(Block block) {
        if (this.gameRunning && !this.gameEnded && this.interactableBlocks != null) {
            Iterator<Material> var3 = this.interactableBlocks.iterator();

            while(var3.hasNext()) {
                Material mat = var3.next();
                if (mat == block.getType()) {
                    return true;
                }
            }

        }
        return false;
    }

    public boolean isRunning() {
        return this.gameRunning;
    }

    public boolean isEnded() {
        return this.gameEnded;
    }

    public boolean canPvp() {
        return this.pvp;
    }

    public boolean canCraft() {
        return this.crafting;
    }

    public Location getInitSpawn() {
        return this.initSpawnPoint;
    }

    private void initPlayer(Player player) {
        this.resetPlayer(player);
        player.teleport(this.getInitSpawnLocation(player));
        if (this.initSpell != null) {
            this.initSpell.castSpell(player, SpellCastState.NORMAL, 1.0F, (String[])null);
        }

        if (!this.teamsEnabled) {
            this.playerScores.put(player.getUniqueId(), 0.0f);
        }

    }

    private void startPlayer(Player player) {
        this.participants.add(player.getName());
        if (this.teamsEnabled) {
            String smallestTeam = null;
            int smallestSize = 1000;
            Iterator var5 = this.teams.keySet().iterator();

            while(var5.hasNext()) {
                String teamName = (String)var5.next();
                int size = ((List)this.teamPlayers.get(teamName)).size();
                if (size < smallestSize) {
                    smallestTeam = teamName;
                    smallestSize = size;
                }
            }

            Team team = (Team)this.teams.get(smallestTeam);
            team.addPlayer(player);
            this.playerTeams.put(player.getName(), smallestTeam);
            ((List)this.teamPlayers.get(smallestTeam)).add(player.getName());
        }

        this.resetPlayer(player);
        if (this.startSpawnPoints.size() > 0) {
            player.teleport(this.getStartSpawnLocation(player));
        }

        if (this.startSpell != null) {
            this.startSpell.castSpell(player, SpellCastState.NORMAL, 1.0F, (String[])null);
        }

    }

    private void respawnPlayer(Player player) {
        player.teleport(this.getRespawnLocation(player));
        if (this.respawnSpell != null) {
            this.respawnSpell.castSpell(player, SpellCastState.NORMAL, 1.0F, (String[])null);
        }

    }

    public void setPlayerOut(Player player) {
        this.participants.remove(player.getName());
        this.resetPlayer(player);
        player.teleport(this.getOutSpawnLocation(player));
    }

    private void resetPlayer(Player player) {
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setFallDistance(0.0F);
        player.setFireTicks(0);
        Iterator var3 = (new ArrayList(player.getActivePotionEffects())).iterator();

        while(var3.hasNext()) {
            PotionEffect effect = (PotionEffect)var3.next();
            player.removePotionEffect(effect.getType());
        }
    }

    private int getPlayerTeamIndex(Player player) {
        String teamName = (String)this.playerTeams.get(player.getName());
        return teamName != null ? this.teamNames.indexOf(teamName) : -1;
    }

    private Location getInitSpawnLocation(Player player) {
        Location location = this.initSpawnPoint.clone();
        this.applyRadiusToLocation(location, this.initSpawnRadius);
        location.add(0.0, 0.5, 0.0);
        return location;
    }

    private Location getStartSpawnLocation(Player player) {
        Location location = null;
        if (this.teamsEnabled && this.teamNames.size() == this.startSpawnPoints.size()) {
            int index = this.getPlayerTeamIndex(player);
            if (index >= 0) {
                location = ((Location)this.startSpawnPoints.get(index)).clone();
            }
        }

        if (location == null) {
            location = ((Location)this.startSpawnPoints.get(random.nextInt(this.startSpawnPoints.size()))).clone();
        }

        this.applyRadiusToLocation(location, this.startSpawnRadius);
        location.add(0.0, 0.5, 0.0);
        return location;
    }

    private Location getRespawnLocation(Player player) {
        Location location = null;
        if (this.teamsEnabled && this.teamNames.size() == this.respawnPoints.size()) {
            int index = this.getPlayerTeamIndex(player);
            if (index >= 0) {
                location = ((Location)this.respawnPoints.get(index)).clone();
            }
        }

        if (location == null) {
            location = ((Location)this.respawnPoints.get(random.nextInt(this.respawnPoints.size()))).clone();
        }

        this.applyRadiusToLocation(location, this.respawnRadius);
        location.add(0.0, 0.5, 0.0);
        return location;
    }

    private Location getOutSpawnLocation(Player player) {
        Location location = this.outSpawnPoint.clone();
        this.applyRadiusToLocation(location, this.outSpawnRadius);
        location.add(0.0, 0.5, 0.0);
        return location;
    }

    private void applyRadiusToLocation(Location location, int radius) {
        location.add(0.5, 0.5, 0.5);
        if (radius > 0) {
            location.add(random.nextDouble() * (double)radius * 2.0 - (double)radius, random.nextDouble() * (double)radius * 2.0 - (double)radius, random.nextDouble() * (double)radius * 2.0 - (double)radius);
        }

        Block block = location.getBlock();
        if (block.getType() == Material.AIR) {
            for(Block down = block.getRelative(BlockFace.DOWN); down.getType() == Material.AIR; down = down.getRelative(BlockFace.DOWN)) {
                location.add(0.0, -1.0, 0.0);
            }
        } else {
            while(block.getType() != Material.AIR) {
                location.add(0.0, 1.0, 0.0);
                block = block.getRelative(BlockFace.UP);
            }
        }

    }

    private Location getLocationFromString(String string) {
        try {
            String[] s = string.split(",");
            World w = Bukkit.getWorld(s[0]);
            if (w == null) {
                this.plugin.getLogger().severe("ERROR GETTING LOCATION FROM STRING (INVALID WORLD): " + string);
                return null;
            } else {
                int x = Integer.parseInt(s[1]);
                int y = Integer.parseInt(s[2]);
                int z = Integer.parseInt(s[3]);
                int f = s.length > 4 ? Integer.parseInt(s[4]) : 0;
                return new Location(w, (double)x, (double)y, (double)z, (float)f, 0.0F);
            }
        } catch (Exception var8) {
            this.plugin.getLogger().severe("ERROR GETTING LOCATION FROM STRING: " + string);
            var8.printStackTrace();
            return null;
        }
    }

    public List<String> getRemainingTeamNames() {
        List<String> retList = new ArrayList<>();

        if (this.teamsEnabled) {
            for (String teamName : teamNames) {
                if (teamPlayers.get(teamName).size() > 0) {
                    retList.add(teamName);
                }
            }
        }

        return retList;
    }

    public static enum MiniGameType {
        TIMED,
        LAST_MAN_STANDING;

        private MiniGameType() {
        }
    }
}
