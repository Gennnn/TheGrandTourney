package me.genn.thegrandtourney.player;

import org.bukkit.ChatColor;

public class PointsMessage extends ActionBarMessage{
    float quantity;
    public PointsMessage(float quantity) {
        super();
        this.quantity = quantity;
        this.priority = 4;
        this.maxDuration = 6;
        this.constructMessage();
    }

    public void constructMessage() {
        this.message = ChatColor.GOLD + "+" + String.format("%.1f",quantity) + " Points!";
    }
}
