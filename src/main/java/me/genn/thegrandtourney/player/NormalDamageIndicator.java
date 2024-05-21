package me.genn.thegrandtourney.player;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import ch.twidev.spectraldamage.api.DamageTypeFactory;

public class NormalDamageIndicator implements DamageTypeFactory {

    @Override
    public String getFormat(double damage) {
        int[] digitArray = String.valueOf((int)damage).replaceAll("\\D", "").chars().map(Character::getNumericValue).toArray();
        String str = "";
        int index = 0;
        for (int digit : digitArray) {
            str = str + digit;
            if (digitArray.length >= 3) {
                if (digitArray.length % 3 == 0 && index + 1 < digitArray.length) {
                    if (index%3 == 2) {
                        str = str + ",";
                    }
                } else if (digitArray.length % 3 == 2 && index + 1 < digitArray.length) {
                    if (index%3 == 1) {
                        str = str + ",";
                    }
                } else if (digitArray.length % 3 == 1 && index + 1 < digitArray.length) {
                    if (index%3 == 0) {
                        str = str + ",";
                    }
                }
            }
            index++;
        }
        str = ChatColor.GRAY + str;
        return str;
    }



}
