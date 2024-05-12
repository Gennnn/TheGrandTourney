package me.genn.thegrandtourney.grid;

import org.bukkit.ChatColor;

public enum District {
    ARISTOCRACY,
    SLUMS,
    PORT,
    FARM,
    OUTSKIRTS,
    NONE;

    public static District getDistrict(String str) {
        str = ChatColor.stripColor(str);
        if (str.equalsIgnoreCase("Aristocracy")) {
            return ARISTOCRACY;
        } else if (str.equalsIgnoreCase("Port")) {
            return PORT;
        } else if (str.equalsIgnoreCase("Slums")) {
            return SLUMS;
        } else if (str.equalsIgnoreCase("Farm")) {
            return FARM;
        } else if (str.equalsIgnoreCase("Outskirts")) {
            return OUTSKIRTS;
        } else {
            return NONE;
        }
    }
}
