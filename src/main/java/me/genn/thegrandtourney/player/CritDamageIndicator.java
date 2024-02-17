package me.genn.thegrandtourney.player;

import java.util.Random;

import org.bukkit.ChatColor;

import ch.twidev.spectraldamage.api.DamageTypeFactory;

public class CritDamageIndicator implements DamageTypeFactory {

    @Override
    public String getFormat(double damage) {
        int[] digitArray = String.valueOf((int)damage).replaceAll("\\D", "").chars().map(Character::getNumericValue).toArray();
        Random r = new Random();
        String str = "";
        int index = 0;
        int indexOffset = 2;
        for (int digit : digitArray) {
            str = str + this.randomColor(String.valueOf(digit), index + indexOffset);
            if (digitArray.length >= 3) {
                if (digitArray.length % 3 == 0) {
                    if (index%3 == 2 && index + 1 < digitArray.length) {
                        str = str + (this.randomColor(",", index + indexOffset));
                        indexOffset++;
                    }
                } else if (digitArray.length % 3 == 2 && index + 1 < digitArray.length) {
                    if (index%3 == 1) {
                        str = str + (this.randomColor(",", index + indexOffset));
                        indexOffset++;
                    }
                } else if (digitArray.length % 3 == 1 && index + 1 < digitArray.length) {
                    if (index%3 == 0) {
                        str = str + (this.randomColor(",", index + indexOffset));
                        indexOffset++;

                    }
                }
            }
            index++;
        }
        str = this.randomColor("✧", 1) + str;
        str = str +this.randomColor("✧", index + indexOffset);
        return str;
    }


    public String randomColor(String string, int i) {
        if (i % 7 == 0) {
            return ChatColor.DARK_PURPLE.toString() + string;

        } else if (i % 6 == 0) {
            return ChatColor.LIGHT_PURPLE.toString() + string;

        } else if (i % 5 == 0) {
            return ChatColor.AQUA.toString() + string;

        } else if (i % 4 == 0) {
            return ChatColor.GREEN.toString() + string;
        } else if (i % 3 == 0) {
            return ChatColor.YELLOW.toString() + string;
        } else if (i % 2 == 0) {
            return ChatColor.GOLD.toString() + string;
        } else {
            return ChatColor.RED.toString() + string;
        }

    }
}
