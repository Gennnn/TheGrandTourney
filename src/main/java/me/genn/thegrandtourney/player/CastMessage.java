package me.genn.thegrandtourney.player;

import org.bukkit.ChatColor;

public class CastMessage extends ActionBarMessage{
    public String spellName;
    public int spellCost;

    public CastMessage(String spellName, int spellCost) {
        super();
        this.spellName = spellName;
        this.spellCost = spellCost;
        this.priority = 3;
        this.maxDuration = 4;
        constructMessage();
    }

    public void constructMessage() {
        this.message = ChatColor.GREEN + "-" + spellCost + " Stamina (" + ChatColor.GOLD + spellName + ChatColor.GREEN + ")";
    }
}
