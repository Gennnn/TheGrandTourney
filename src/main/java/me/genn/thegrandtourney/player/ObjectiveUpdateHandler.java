package me.genn.thegrandtourney.player;

import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.item.MMOItem;
import me.genn.thegrandtourney.util.ToastMessage;
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

        String textToTranslate = questName.replace(" ", "_") + "." + trackingText.replace(" ", "_");
        if (objective == null) {

            objective = new Objective(questName, status, trackingText);

            objective.icon = new ItemStack(Material.PLAYER_HEAD);
            MMOItem.getHeadFrom64(plugin.questHandler.getQuest(questName).tgtNpc.skin, objective.icon);
            ToastMessage.displayTo(player, "writable_book", ChatColor.translateAlternateColorCodes('&', questName) + ChatColor.GRAY + " started.", ToastMessage.Style.TASK);
            player.playSound(player, "entity.experience_orb.pickup", 1f, 1.5f);
            mmoPlayer.objectives.add(objective);
        } else {

            objective.status = status;

            if (!objective.trackingText.equalsIgnoreCase(trackingText)) {
                objective.trackingText = trackingText;
            }
        }
        if (objective != null && objective.objectiveUpdatesPassed.contains(status)) {
            return;
        } else {
            objective.objectiveUpdatesPassed.add(status);
            if (!completingStep) {
                ToastMessage.displayTo(player, "writable_book",  ChatColor.translateAlternateColorCodes('&', questName) + ChatColor.GRAY + " updated.", ToastMessage.Style.TASK);
                player.playSound(player, "entity.experience_orb.pickup", 1f, 1.5f);
            }

        }
        if (completingStep) {
            objective.completed = true;
            mmoPlayer.trackedObjective = null;
            mmoPlayer.completedObjectives.add(objective);
            mmoPlayer.objectives.remove(mmoPlayer.objectives.stream().filter(obj -> obj.questName.equalsIgnoreCase(questName)).findFirst().orElse(null));
            ToastMessage.displayTo(player, "writable_book", ChatColor.translateAlternateColorCodes('&', questName) + ChatColor.GRAY + " completed.", ToastMessage.Style.TASK);
            player.playSound(player, "entity.experience_orb.pickup", 1f, 1.5f);
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
            } else if (questLocation.startsWith("foraging:")) {
                String locString = questLocation.replaceFirst("foraging:", "");
                Location loc = plugin.foragingZoneHandler.getZoneForObj(locString, player.getLocation()).centerLoc;
                if (loc != null) {
                    objective.objectiveLocation = loc;
                }
            } else if (questLocation.startsWith("station:")) {
                String locString = questLocation.replaceFirst("station:", "");
                if (plugin.tableHandler.getStationForObj(locString, player.getLocation()) == null) {
                    return;
                }

                Location loc = plugin.tableHandler.getStationForObj(locString, player.getLocation()).spawnLocation;
                if (loc != null) {
                    objective.objectiveLocation = loc;
                }
            }
        }
        if (objective.objectiveLocation != null) {

            //player.sendMessage(ChatColor.GRAY + ChatColor.ITALIC.toString() + "Quest location updated.");
        } else {
            player.sendMessage(ChatColor.GRAY + ChatColor.ITALIC.toString() + "The trail for this quest seems to have run cold...");
        }
    }
}
