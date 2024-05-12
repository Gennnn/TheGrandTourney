package me.genn.thegrandtourney.player;

import com.nisovin.magicspells.MagicSpells;
import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.xp.XpType;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.checkerframework.checker.units.qual.C;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ActionBarDisplay {
    TGT plugin;

    Map<UUID, ActionBarMessage> messageQueue;

    public ActionBarDisplay(TGT plugin) {
        this.plugin = plugin;
        this.messageQueue = new HashMap<>();
    }

    public void display(Player p) {
        String middleMessage;
        if (!this.messageQueue.containsKey(p.getUniqueId()) || this.messageQueue.get(p.getUniqueId()) == null) {
            queueDefenseMessage(p);
        }
        if (this.messageQueue.get(p.getUniqueId()).duration + 1 == this.messageQueue.get(p.getUniqueId()).maxDuration) {
            p.sendActionBar(constructMessage(p, this.messageQueue.get(p.getUniqueId()).message));
            this.messageQueue.remove(p.getUniqueId());
        } else {
            p.sendActionBar(constructMessage(p, this.messageQueue.get(p.getUniqueId()).message));
            this.messageQueue.get(p.getUniqueId()).duration++;
        }
    }

    private String constructMessage(Player p, String middleMessage) {
        String leftSide = getLeftSide(p);
        String rightSide = ChatColor.GREEN.toString() + "    " + MagicSpells.getManaHandler().getMana(p) + "/" + MagicSpells.getManaHandler().getMaxMana(p) + "⚡ Stamina";
        int rightStringLength = getTrueStringLength(rightSide);
        int leftStringLength = getTrueStringLength(leftSide);
        while (rightStringLength > leftStringLength) {
            leftSide = " " + leftSide;
            leftStringLength = getTrueStringLength(leftSide);
        }
        while (leftStringLength > rightStringLength) {
            rightSide = rightSide + " ";
            rightStringLength = getTrueStringLength(rightSide);
        }
        return leftSide + middleMessage + rightSide;
    }

    private String getLeftSide(Player p) {
        MMOPlayer mmoPlayer = plugin.players.get(p.getUniqueId());
        if (mmoPlayer.getAbsorptionHealth() > 0) {
            return ChatColor.GOLD.toString() + (int)mmoPlayer.getTotalHealth() + "/" + (int)mmoPlayer.getMaxHealth() + "❤    ";
        } else {
            return  ChatColor.RED.toString() + (int)plugin.players.get(p.getUniqueId()).getHealth() + "/" + (int)plugin.players.get(p.getUniqueId()).getMaxHealth() + "❤    ";
        }
    }

    private int getTrueStringLength(String string) {
        return ChatColor.stripColor(string).length();
    }

    public void queueXpMessage(Player player, XpType type, double amount) {
        XpMessage message = new XpMessage(type, amount);
        message.constructMessage(player);
        this.queueMessage(player, message);
    }
    public void queueCastMessage(Player player, String spellName, int spellCost) {
        CastMessage message = new CastMessage(spellName, spellCost);
        this.queueMessage(player, message);
    }
    public void queuePointsMessage(Player player, float points) {
        PointsMessage message = new PointsMessage(points);
        this.queueMessage(player ,message);
    }

    public void queueOutOfManaMessage(Player player) {
        ActionBarMessage message = new ActionBarMessage();
        message.maxDuration = 2;
        message.priority = 5;
        message.message = ChatColor.RED.toString() + ChatColor.BOLD + "NOT ENOUGH MANA";
        queueMessage(player, message);
    }
    public void queueLocationMessage(Player player, String location) {
        ActionBarMessage message = new ActionBarMessage();
        message.maxDuration = 10;
        message.priority = 1;
        message.message = location;
        queueMessage(player, message);
    }

    public void queueDefenseMessage(Player player) {
        ActionBarMessage message = new ActionBarMessage();
        message.maxDuration = 2;
        message.priority = 0;
        message.message = ChatColor.AQUA.toString() + (int)plugin.players.get(player.getUniqueId()).getDefense() + "❈ Defense";
        queueMessage(player, message);
    }

    private void queueMessage(Player player, ActionBarMessage msg) {
        if (this.messageQueue.putIfAbsent(player.getUniqueId(), msg) != null) {
            if (msg.priority >= this.messageQueue.get(player.getUniqueId()).priority || !this.messageQueue.containsKey(player.getUniqueId())) {
                this.messageQueue.put(player.getUniqueId(), msg);
            }
        }
    }


}
