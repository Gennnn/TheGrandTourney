package me.genn.thegrandtourney.npc;

import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.player.MMOPlayer;
import me.genn.thegrandtourney.player.ObjectiveUpdateHandler;
import me.genn.thegrandtourney.skills.farming.CropTemplate;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestHandler {
    public List<Quest> allQuests;
    public TGT plugin;
    public ObjectiveUpdateHandler objectiveUpdater;

    public QuestHandler(TGT plugin) {
        this.plugin = plugin;
        this.allQuests = new ArrayList<>();
        this.objectiveUpdater = new ObjectiveUpdateHandler(this.plugin);
    }

    public Quest getQuest(String name) {
        return getQuestWithName(this.allQuests, name);
    }
    private Quest getQuestWithName(final List<Quest> list, final String name){
       return list.stream().filter(obj -> obj.getQuestName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public void checkInvForFulfillingItems(Player player) {
        MMOPlayer mmoPlayer = plugin.players.get(player.getUniqueId());
        if (mmoPlayer == null) {
            return;
        }
        if (mmoPlayer.trackedObjective == null) {
            return;
        }
        if (!(this.getQuest(mmoPlayer.trackedObjective.questName) instanceof ItemRetrievalQuest)) {
            return;
        }
        ItemRetrievalQuest quest = (ItemRetrievalQuest) this.getQuest(mmoPlayer.trackedObjective.questName);
        if (quest.getAmount(player)) {
            plugin.questHandler.objectiveUpdater.performStatusUpdates(player, quest.getQuestName(), quest.tgtNpc.updateOnCompleteCollection);
        }
    }

    public void updateTrackingDetails(Player player) {
        MMOPlayer mmoPlayer = plugin.players.get(player.getUniqueId());
        if (mmoPlayer == null) {
            return;
        }
        if (mmoPlayer.isCrafting) {
            return;
        }
        if (plugin.currentlyDisplayedBossBar.get(player) != null) {
            player.hideBossBar(plugin.currentlyDisplayedBossBar.get(player));
        }
        if (mmoPlayer.trackedObjective != null) {
            double distance = Math.sqrt(player.getLocation().distanceSquared(mmoPlayer.trackedObjective.objectiveLocation));
            String name = ChatColor.translateAlternateColorCodes('&',mmoPlayer.trackedObjective.trackingText) + " (" + (int)distance + "m away)";
            final BossBar bar = BossBar.bossBar(Component.text(name), 1, BossBar.Color.PURPLE, BossBar.Overlay.PROGRESS);
            plugin.currentlyDisplayedBossBar.put(player, bar);
            player.showBossBar(bar);
        }
    }
}
