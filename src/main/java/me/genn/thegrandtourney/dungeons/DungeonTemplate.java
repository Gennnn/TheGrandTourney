package me.genn.thegrandtourney.dungeons;

import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.item.DropTable;
import me.genn.thegrandtourney.item.MMOItem;
import me.genn.thegrandtourney.mobs.MMOMob;
import me.genn.thegrandtourney.npc.Quest;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class DungeonTemplate {
    public String name;
    public MMOItem key;
    public String enterText;
    public Material doorMat;
    public double openRadius;
    List<RoomData> rooms;
    TGT plugin;
    public static DungeonTemplate create(ConfigurationSection config, TGT plugin) {
        DungeonTemplate template = new DungeonTemplate();
        template.rooms = new ArrayList<>();
        template.name = config.getName();
        template.plugin = plugin;
        template.key = plugin.itemHandler.getMMOItemFromString(config.getString("key"));
        template.doorMat = Material.matchMaterial("minecraft:" + config.getString("door-material", "obsidian"));
        if (config.contains("enter-text")) {
            template.enterText = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("enter-text")));
        }
        template.openRadius = config.getDouble("open-radius", 5);
        ConfigurationSection roomsSection = config.getConfigurationSection("rooms");
        template.rooms.addAll(parseRooms(roomsSection, plugin));
        return template;
    }

    public static List<RoomData> parseRooms(ConfigurationSection section, TGT plugin) {
        List<RoomData> returnData = new ArrayList<>();
        for (String key : section.getKeys(false)) {
            ConfigurationSection roomSec = section.getConfigurationSection(key);
            RoomData data = new RoomData();
            data.goal = RoomGoal.valueOf(roomSec.getString("goal", "thru").toUpperCase());
            data.toProgress = roomSec.getString("to-progress");
            data.quantity = roomSec.getInt("quantity", 1);
            data.preventAbilities = roomSec.getBoolean("prevent-abilities", false);
            data.doorClosedByDefault = roomSec.getBoolean("door-closed-by-default", true);
            data.doorMat = Material.matchMaterial("minecraft:" + roomSec.getString("door-material", "obsidian"));
            data.name = roomSec.getName();
            if (roomSec.contains("goal-text")) {
                data.goalText = ChatColor.translateAlternateColorCodes('&', roomSec.getString("goal-text"));
            }
            if (roomSec.contains("reward-chest")) {
                ConfigurationSection rewardChestSection = roomSec.getConfigurationSection("reward-chest");
                data.rewardChestBase = Material.matchMaterial("minecraft:" + rewardChestSection.get("base-material", "stone_slab"));
                data.rewardChestMid = Material.matchMaterial("minecraft:" + rewardChestSection.get("mid-material", "stone_bricks"));
                data.rewardChestBase64 = rewardChestSection.getString("texture", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDliMjk4M2MwMWI4ZGE3ZGMxYzBmMTJkMDJjNGFiMjBjZDhlNjg3NWU4ZGY2OWVhZTJhODY3YmFlZTYyMzZkNCJ9fX0=");
                String particle = rewardChestSection.getString("particle", "cloud");

                Particle p = com.nisovin.magicspells.util.ParticleUtil.getParticle(particle);
                if (p == null) {
                    data.chestParticle = Particle.CLOUD;
                } else {
                    data.chestParticle = p;
                }

                data.chestParticleCount = rewardChestSection.getInt("particle-count", 1);
                DropTable rewardTable = new DropTable(plugin, true, false);
                ConfigurationSection rewardsSection = rewardChestSection.getConfigurationSection("rewards");
                rewardTable.addDropsFromSection(rewardsSection);
                data.rewardChestDrops = rewardTable;
            }
            returnData.add(data);
        }
        return returnData;
    }

    public String getName() {
        return this.name;
    }
    public boolean containsRoomWithName(final String name){
        return this.rooms.stream().anyMatch(obj -> obj.getRoomName().equalsIgnoreCase(name));
    }

    public RoomData getRoomWithName(final String name){
        return this.rooms.stream().filter(obj -> obj.getRoomName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }
}


