package me.genn.thegrandtourney.player;

import ch.twidev.spectraldamage.api.DamageTypeFactory;
import org.bukkit.ChatColor;

public class DodgeIndicator implements DamageTypeFactory {

    @Override
    public String getFormat(double v) {
        return ChatColor.AQUA + ChatColor.BOLD.toString() + "DODGED!";
    }
}
