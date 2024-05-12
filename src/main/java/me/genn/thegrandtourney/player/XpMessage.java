package me.genn.thegrandtourney.player;

import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.xp.Xp;
import me.genn.thegrandtourney.xp.XpType;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class XpMessage extends ActionBarMessage{
    public XpType xpType;
    public double amount;

    public XpMessage(XpType xpType, double amount) {
        super();
        this.xpType = xpType;
        this.amount = amount;
        this.priority = 2;
        this.maxDuration = 5;
    }

    public void constructMessage(Player p) {
        double numerator = ( plugin.players.get(p.getUniqueId()).getXpForType(xpType) - ( Xp.xpForLevel.get(plugin.players.get(p.getUniqueId()).getLvlForType(xpType))));
        double denominator = (( Xp.xpForLevel.get(plugin.players.get(p.getUniqueId()).getLvlForType(xpType) + 1)) - Xp.xpForLevel.get(plugin.players.get(p.getUniqueId()).getLvlForType(xpType) ));
        this.message = ChatColor.DARK_AQUA + "+" + String.format("%.1f", this.amount) + " " + xpType.getName() + " (" + String.format("%.2f", (numerator/denominator)*100) + "%)";
    }
}
