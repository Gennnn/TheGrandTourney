package me.genn.thegrandtourney.player;

import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.item.MMOItem;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ObjectiveUpdateHandler {
    TGT plugin;

    public ObjectiveUpdateHandler(TGT plugin) {
        this.plugin = plugin;
    }

    public void performStatusUpdates(Player player, String questName, ObjectiveUpdate objectiveUpdate) {
        MMOPlayer mmoPlayer = plugin.players.get(player.getUniqueId());
        boolean completingStep = objectiveUpdate.completingStep;
        List<String> status = objectiveUpdate.statusUpdate;

        String trackingText = objectiveUpdate.trackingTextUpdate;
        String questLocation = objectiveUpdate.locationUpdate;
        Objective objective = mmoPlayer.objectives.stream().filter(obj -> obj.questName.equalsIgnoreCase(questName)).findFirst().orElse(null);

        if (objective == null) {
            if (player.isOp()) {
                player.sendMessage("No objective was found. Creating new one...");
            }
            objective = new Objective(questName, status, trackingText);
            if (player.isOp()) {
                player.sendMessage("Created new objective " + objective.questName + " with TT " + trackingText);
            }
            objective.icon = new ItemStack(Material.PLAYER_HEAD);
            MMOItem.getHeadFrom64(plugin.questHandler.getQuest(questName).tgtNpc.skin, objective.icon);
            mmoPlayer.objectives.add(objective);
        } else {
            if (player.isOp()) {
                player.sendMessage("Objective was found!");
            }
            objective.status = status;

            if (!objective.trackingText.equalsIgnoreCase(trackingText)) {
                objective.trackingText = trackingText;
            }
        }
        if (objective != null && objective.objectiveUpdatesPassed.contains(status)) {
            return;
        } else {
            objective.objectiveUpdatesPassed.add(status);
        }
        if (completingStep) {
            objective.completed = true;
            return;
        }

        if (!questLocation.equalsIgnoreCase("")) {
            if (questLocation.startsWith("npc:")) {
                String locString = questLocation.replaceFirst("npc:", "");
                Location loc = plugin.npcHandler.getNpcForObj(locString, player.getLocation()).pasteLocation;
                if (loc != null) {
                    objective.objectiveLocation = loc;
                }
            } else if (questLocation.startsWith("ore:")) {
                String locString = questLocation.replaceFirst("ore:", "");
                Location loc = plugin.oreHandler.getOreForObj(locString, player.getLocation()).loc;
                if (loc != null) {
                    objective.objectiveLocation = loc;
                }
            } else if (questLocation.startsWith("crop:")) {
                String locString = questLocation.replaceFirst("crop:", "");
                Location loc = plugin.cropHandler.getCropForObj(locString, player.getLocation()).loc;
                if (loc != null) {
                    objective.objectiveLocation = loc;
                }
            } else if (questLocation.startsWith("fishing:")) {
                String locString = questLocation.replaceFirst("fishing:", "");
                Location loc = plugin.fishingZoneHandler.getZoneForObj(locString, player.getLocation()).centerLocation;
                if (loc != null) {
                    objective.objectiveLocation = loc;
                }
            } else if (questLocation.startsWith("mob:")) {
                String locString = questLocation.replaceFirst("mob:", "");
                Location loc = plugin.spawnerHandler.getSpawnerForObj(locString, player.getLocation()).loc;
                if (loc != null) {
                    objective.objectiveLocation = loc;
                }
            }
        }
        if (objective.objectiveLocation != null) {
            player.sendMessage(ChatColor.GRAY + ChatColor.ITALIC.toString() + "Quest location updated.");
        } else {
            player.sendMessage(ChatColor.GRAY + ChatColor.ITALIC.toString() + "The trail for this quest seems to have run cold...");
        }
    }
}
