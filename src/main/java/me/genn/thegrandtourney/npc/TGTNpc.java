package me.genn.thegrandtourney.npc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.player.ObjectiveUpdate;
import me.genn.thegrandtourney.xp.Xp;
import me.genn.thegrandtourney.xp.XpType;
import net.citizensnpcs.api.trait.trait.Equipment;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.SkinTrait;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class TGTNpc {
    public NPC npc;
    public String name;
    public String skinAndSigName;
    public EntityType entityType;
    public QuestType questType;
    public String skin;
    public String skinSig;
    public double spawnX;
    public double spawnY;
    public double spawnZ;
    public TGTNpc childNpc;
    public List<Step> steps;
    public String internalName;
    public List<String> childNpcs;
    public TGTNpc parentNpc;
    public static List<TGTNpc> childNpcList;
    public ConfigurationSection stepsConfig;
    public int cdOnWalkaway;


    Map<Equipment.EquipmentSlot, String> equipment;
    public int lineDelay;
    public boolean useWordCtForDelay;
    public long wordCtDelayFactor;
    public int amount;
    public float talkVolume;
    public String talkSound;
    public String itemToBring;
    public String stepJumpOnComplete;
    public float talkPitch;
    public String stepToFinish;
    public String mobToKill;
    public String questDisplayName;
    public String questName;
    public Location pasteLocation;
    public Material objectiveItem;
    public ObjectiveUpdate updateOnCompleteCollection;
    public XpType xpType;
    public String stepToCraft;


    public static TGTNpc create(ConfigurationSection config) throws IOException {
        TGTNpc npc = new TGTNpc();
        TGT plugin = JavaPlugin.getPlugin(TGT.class);
        npc.internalName = config.getName();
        npc.name = config.getString("name");
        npc.entityType = EntityType.valueOf(config.getString("entity-type").toUpperCase());
        if (npc.entityType == null) {
            npc.entityType = EntityType.PLAYER;
        }
        NPC citizenNpc = plugin.registry.createNPC(npc.entityType, ChatColor.translateAlternateColorCodes('&', npc.name));
        npc.childNpcs = config.getStringList("child-npcs");
        npc.skinAndSigName = config.getString("skin", npc.internalName);
        if (npc.skinAndSigName != null) {
            File file = new File(NPCHandler.skinAndSigDirectory, npc.skinAndSigName + ".skin.txt");
            BufferedReader reader;
            if (file.exists()) {
                reader = new BufferedReader(new FileReader(file));
                npc.skin = reader.readLine();
                reader.close();
            }
            file = new File(NPCHandler.skinAndSigDirectory, npc.skinAndSigName + ".sig.txt");
            if (file.exists()) {
                reader = new BufferedReader(new FileReader(file));
                npc.skinSig = reader.readLine();
                reader.close();
            }
        }
        if (config.getBoolean("look-close", true)) {

            citizenNpc.getOrAddTrait(LookClose.class).setRange(config.getInt("look-close-range", 6));
            citizenNpc.getOrAddTrait(LookClose.class).setPerPlayer(true);
            citizenNpc.getOrAddTrait(LookClose.class).lookClose(true);
        }
        String[] coords = (config.getString("relative-position")).split(", ");
        npc.spawnX = Double.parseDouble(coords[0]);
        npc.spawnY = Double.parseDouble(coords[1]);
        npc.spawnZ = Double.parseDouble(coords[2]);

        npc.npc = citizenNpc;
        npc.steps = new ArrayList<Step>();
        ConfigurationSection stepsConfig = config.getConfigurationSection("steps");
        npc.stepsConfig = stepsConfig;
        /*Iterator stepsIter = stepsConfig.getKeys(false).iterator();
        while (stepsIter.hasNext()) {
            String stepKey = (String) stepsIter.next();
            ConfigurationSection stepContent = stepsConfig.getConfigurationSection(stepKey);
            List<String> dialogue = stepContent.getStringList("dialogue");
            boolean ranged = stepContent.getBoolean("ranged", false);
            String jumpTo = stepContent.getString("jump-to");
            TextComponent narration = new TextComponent();
            ConfigurationSection narrationConfig = stepContent.getConfigurationSection("narration");
            Iterator iter2 = narrationConfig.getKeys(false).iterator();
            List<BaseComponent> narrationComponents = new ArrayList<BaseComponent>();
            while (iter2.hasNext()) {
                String key2 = (String) iter2.next();
                ConfigurationSection subSubConfig = narrationConfig.getConfigurationSection(key2);
                narrationComponents.add(narrationParse(subSubConfig, npc));
            }
            narration = (TextComponent) narrationComponents.get(0);
            for (int i = 1; i < narrationComponents.size(); i++) {
                narration.addExtra(narrationComponents.get(i));
            }
            List<String> rewards = stepContent.getStringList("rewards");
            Step stepObj = new Step(stepKey, dialogue, narration, ranged, jumpTo, rewards);
            npc.steps.add(stepObj);
        }*/
        npc.questType = QuestType.valueOf(config.getString("type", "quest").toUpperCase());
        npc.objectiveItem = Material.matchMaterial("minecraft:" + config.getString("objective-item", "paper"));
        List<String> equipmentList = config.getStringList("equipment");
        npc.equipment = new HashMap<>();
        if (equipmentList != null) {
            for (String str : equipmentList) {
                String[] itemAndSlot = str.split(" ");
                npc.equipment.put(Equipment.EquipmentSlot.valueOf(itemAndSlot[1].toUpperCase()), itemAndSlot[0]);
            }
        }
        npc.cdOnWalkaway = config.getInt("cooldown-on-walk-away", 0);
        npc.lineDelay = config.getInt("line-delay");
        npc.useWordCtForDelay = config.getBoolean("use-word-count-for-delay", true);
        npc.wordCtDelayFactor = config.getLong("word-count-delay-factor");
        npc.talkSound = config.getString("talk-sound");
        npc.talkVolume = Float.parseFloat(config.getString("talk-volume"));
        npc.talkPitch = Float.parseFloat(config.getString("talk-pitch"));
        npc.stepToFinish = config.getString("step-to-finish", "active");
        npc.itemToBring = config.getString("quest-item");
        npc.amount = config.getInt("amount", 1);
        npc.mobToKill = config.getString("mob-to-kill");
        npc.stepJumpOnComplete = config.getString("step-jump-on-complete", "complete");
        npc.questName = config.getString("quest-name", npc.name);
        if (config.contains("objective-on-collection")) {
            ObjectiveUpdate objectiveUpdate = new ObjectiveUpdate();
            ConfigurationSection collectSection = config.getConfigurationSection("objective-on-collection");
            List<String> statusText = collectSection.getStringList("status");
            for (int i = 0; i < statusText.size(); i++) {
                objectiveUpdate.statusUpdate.add(i, ChatColor.translateAlternateColorCodes('&', statusText.get(i)));
            }
            objectiveUpdate.locationUpdate = collectSection.getString("objective-location");
            objectiveUpdate.trackingTextUpdate = collectSection.getString("tracking-text");
            objectiveUpdate.completingStep = collectSection.getBoolean("completing-step", false);
            npc.updateOnCompleteCollection = objectiveUpdate;
        }
        npc.stepToCraft = config.getString("step-to-craft");
        if (config.contains("xp-type")) {
            npc.xpType = Xp.parseXpType(config.getString("xp-type"));
        }

        /*if (npc.questType == QuestType.RETRIEVAL) {
            ItemRetrievalQuest quest = new ItemRetrievalQuest(config.getInt("cooldown-on-walk-away"), config.getInt("line-delay"), config.getBoolean("use-word-count-for-delay"), config.getLong("word-count-delay-factor"), config.getString("talk-sound"), Float.parseFloat(config.getString("talk-volume")), Float.parseFloat(config.getString("talk-pitch")), npc.skinSig, npc.skin, npc.steps, config.getString("step-to-finish", "active"), config.getString("quest-item"), config.getInt("amount", 1), config.getString("step-jump-on-complete", "complete"), npc.equipment);
            npc.npc.addTrait(quest);
        } else if (npc.questType == QuestType.SLAYER) {
            SlayerQuest quest = new SlayerQuest(config.getInt("cooldown-on-walk-away"), config.getInt("line-delay"), config.getBoolean("use-word-count-for-delay"), config.getLong("word-count-delay-factor"), config.getString("talk-sound"), Float.parseFloat(config.getString("talk-volume")), Float.parseFloat(config.getString("talk-pitch")), npc.skinSig, npc.skin, npc.steps, config.getString("step-to-finish", "active"), config.getString("mob-to-kill"), config.getInt("amount", 5), config.getString("step-jump-on-complete", "complete"), npc.equipment);
            npc.npc.addTrait(quest);
        } else {
            Quest quest = new Quest("quest", config.getInt("cooldown-on-walk-away"), config.getInt("line-delay"), config.getBoolean("use-word-count-for-delay"), config.getLong("word-count-delay-factor"), config.getString("talk-sound"), Float.parseFloat(config.getString("talk-volume")), Float.parseFloat(config.getString("talk-pitch")), npc.skinSig, npc.skin, npc.steps, npc.equipment);
            npc.npc.addTrait(quest);
        }*/
        return npc;
    }

    public static TGTNpc createStep2(TGTNpc npc) {
        TGT plugin = JavaPlugin.getPlugin(TGT.class);
        Iterator stepsIter = npc.stepsConfig.getKeys(false).iterator();
        while (stepsIter.hasNext()) {
            String stepKey = (String) stepsIter.next();
            ConfigurationSection stepContent = npc.stepsConfig.getConfigurationSection(stepKey);
            List<String> dialogue = stepContent.getStringList("dialogue");
            boolean ranged = stepContent.getBoolean("ranged", false);
            String jumpTo = stepContent.getString("jump-to");
            TextComponent narration = new TextComponent();
            if (stepContent.contains("narration")) {
                ConfigurationSection narrationConfig = stepContent.getConfigurationSection("narration");
                Iterator iter2 = narrationConfig.getKeys(false).iterator();
                List<BaseComponent> narrationComponents = new ArrayList<BaseComponent>();
                while (iter2.hasNext()) {
                    String key2 = (String) iter2.next();
                    ConfigurationSection subSubConfig = narrationConfig.getConfigurationSection(key2);
                    narrationComponents.add(narrationParse(subSubConfig, npc));
                }
                narration = (TextComponent) narrationComponents.get(0);
                for (int i = 1; i < narrationComponents.size(); i++) {
                    narration.addExtra(narrationComponents.get(i));
                }
            }
            List<String> rewards = stepContent.getStringList("rewards");
            String objectiveLocation = "";
            List<String> status = new ArrayList<>();
            String trackingText = "";
            boolean completingStep = false;
            ObjectiveUpdate objectiveUpdate = new ObjectiveUpdate(status, objectiveLocation, trackingText, completingStep);
            if (stepContent.contains("objective")) {
                ConfigurationSection objectiveSection = stepContent.getConfigurationSection("objective");
                if (objectiveSection.contains("status")) {

                    List<String> statusText = objectiveSection.getStringList("status");
                    for (int i = 0; i < statusText.size(); i++) {
                        objectiveUpdate.statusUpdate.add(ChatColor.translateAlternateColorCodes('&', statusText.get(i)));
                    }

                }
                if (objectiveSection.contains("objective-location")) {
                    objectiveUpdate.locationUpdate = objectiveSection.getString("objective-location");
                }
                if (objectiveSection.contains("tracking-text")) {
                    objectiveUpdate.trackingTextUpdate = ChatColor.translateAlternateColorCodes('&', objectiveSection.getString("tracking-text"));
                }
                objectiveUpdate.completingStep = objectiveSection.getBoolean("completing-step", false);
            }

            if (narration.getText().length() == 0) {
                Step stepObj = new Step(stepKey, dialogue, null, ranged, jumpTo, rewards, objectiveUpdate);
                npc.steps.add(stepObj);
            } else {
                Step stepObj = new Step(stepKey, dialogue, narration, ranged, jumpTo, rewards, objectiveUpdate);
                npc.steps.add(stepObj);
            }

        }
        if (npc.questType == QuestType.RETRIEVAL) {
            ItemRetrievalQuest quest = new ItemRetrievalQuest(npc.cdOnWalkaway, npc.lineDelay, npc.useWordCtForDelay, npc.wordCtDelayFactor, npc.talkSound, npc.talkVolume, npc.talkPitch, npc.skinSig, npc.skin, npc.steps, npc.stepToFinish, npc.itemToBring, npc.amount, npc.stepJumpOnComplete, npc.equipment, npc.questDisplayName, npc.questName, npc);
            npc.npc.addTrait(quest);
            plugin.questHandler.allQuests.add(quest);
        } else if (npc.questType == QuestType.SLAYER) {
            SlayerQuest quest = new SlayerQuest(npc.cdOnWalkaway, npc.lineDelay, npc.useWordCtForDelay, npc.wordCtDelayFactor, npc.talkSound, npc.talkVolume, npc.talkPitch, npc.skinSig, npc.skin, npc.steps, npc.stepToFinish, npc.mobToKill, npc.amount, npc.stepJumpOnComplete, npc.equipment, npc.questDisplayName, npc.questName, npc);
            npc.npc.addTrait(quest);
            plugin.questHandler.allQuests.add(quest);
        } else if (npc.questType == QuestType.STATION_MASTER) {
            StationMaster quest = new StationMaster(npc.cdOnWalkaway, npc.lineDelay, npc.useWordCtForDelay, npc.wordCtDelayFactor, npc.talkSound, npc.talkVolume, npc.talkPitch, npc.skinSig, npc.skin, npc.steps, npc.stepToCraft, npc.xpType, npc.equipment, npc.questDisplayName, npc.questName, npc);
            npc.npc.addTrait(quest);
            plugin.questHandler.allQuests.add(quest);
        } else {
            Quest quest = new Quest("quest", npc.cdOnWalkaway, npc.lineDelay, npc.useWordCtForDelay, npc.wordCtDelayFactor, npc.talkSound, npc.talkVolume, npc.talkPitch, npc.skinSig, npc.skin, npc.steps, npc.equipment, npc.questDisplayName, npc.questName, npc);
            npc.npc.addTrait(quest);
            plugin.questHandler.allQuests.add(quest);
        }
        return npc;
    }

    public static BaseComponent narrationParse(ConfigurationSection config, TGTNpc npc) {
        String text = config.getString("text");
        if (text != null) {
            text = ChatColor.translateAlternateColorCodes('&', text);
        }
        String command = config.getString("command");
        String hover = config.getString("hover-text");
        if (hover != null) {
            hover = ChatColor.translateAlternateColorCodes('&', hover);
        }
        TextComponent component = new TextComponent(text);
        if (hover != null) {
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover).create()));
        } else {
            component.setHoverEvent(null);
        }
        if (command != null) {
            if (command.contains("$npcId")) {
                command = command.replaceAll("(\\$npcId)", String.valueOf(npc.npc.getId()));
            }
            if (command.contains("$npcParentId")) {
                command = command.replaceAll("(\\$npcParentId)", String.valueOf(npc.parentNpc.npc.getId()));
            }
            if (command.contains("$npcChildId")) {
                command = command.replaceAll("(\\$npcChildId_)([^\\ \\s]*)",String.valueOf(((TGTNpc)( childNpcList.stream().filter(o -> o.getName().equals("$1")).toArray()[0])).npc.getId()));
            }

            component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        } else {
            component.setClickEvent(null);
        }
        return component;
    }

    public String getName() {
        return this.internalName;
    }


}
