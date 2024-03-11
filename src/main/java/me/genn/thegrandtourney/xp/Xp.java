package me.genn.thegrandtourney.xp;

import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.player.MMOPlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class Xp {
    static Map<Integer, Double> xpForLevel;
    static TGT plugin;
    public static void initializeXp(TGT plugin) {
        Xp.plugin = plugin;
        Xp.xpForLevel = new HashMap<>();
        for (int i = 1; i <= 10; i++) {
            xpForLevel.put(i, (62.5 * (i^2))+(37.5*i));
        }

    }

    public static XpType parseXpType(String str) {
        if (str.equalsIgnoreCase("combat")) {
            return XpType.COMBAT;
        } else if (str.equalsIgnoreCase("mining")) {
            return XpType.MINING;
        } else if (str.equalsIgnoreCase("fishing")) {
            return XpType.FISHING;
        } else if (str.equalsIgnoreCase("logging")) {
            return XpType.LOGGING;
        } else if (str.equalsIgnoreCase("farming")) {
            return XpType.FARMING;
        } else if (str.equalsIgnoreCase("cooking")) {
            return  XpType.COOKING;
        } else if (str.equalsIgnoreCase("tailoring")) {
            return XpType.TAILORING;
        } else if (str.equalsIgnoreCase("blacksmithing")) {
            return XpType.BLACKSMITHING;
        } else if (str.equalsIgnoreCase("tinkering")) {
            return XpType.TINKERING;
        }
        return null;
    }


    public void grantXp(XpType type, Player bukkitPlayer, double amount) {
        MMOPlayer player = plugin.players.get(bukkitPlayer.getUniqueId());
        double value = player.getXpForType(type) + amount;
        player.setXpForType(type, value);
        if (player.getLvlForType(type) < 10 && xpForLevel.get(player.getLvlForType(type) + 1) <=player.getXpForType(type)) {
            player.setLvlForType(type, player.getLvlForType(type)+1);
            //send lvl up msgs and perform lvl up bonuses
        }
        bukkitPlayer.setExp((float) (player.getXpForType(type)/xpForLevel.get(player.getLvlForType(type) + 1)));
        bukkitPlayer.setLevel(player.getLvlForType(type));
    }
    public void sendMessagesForLevel(Player bukkitPlayer, XpType type) {
        MMOPlayer player = plugin.players.get(bukkitPlayer.getUniqueId());
        bukkitPlayer.sendMessage(ChatColor.DARK_AQUA + "==============================");
        bukkitPlayer.sendMessage(ChatColor.AQUA + "  " + ChatColor.BOLD + "SKILL LEVEL UP " +ChatColor.RESET + "" + ChatColor.DARK_AQUA + type.getName() + " " + ChatColor.DARK_GRAY + intToRoman(player.getLvlForType(type)-1) + "➡" + ChatColor.DARK_AQUA + intToRoman(player.getLvlForType(type)));
        bukkitPlayer.sendMessage("");
        bukkitPlayer.sendMessage("  " + ChatColor.GREEN + "" + ChatColor.BOLD + "REWARDS");
        //send appropriate reward text
    }
    public static String intToRoman(int num)
    {
        int[] values = {1000,900,500,400,100,90,50,40,10,9,5,4,1};
        String[] romanLetters = {"M","CM","D","CD","C","XC","L","XL","X","IX","V","IV","I"};
        StringBuilder roman = new StringBuilder();
        for(int i=0;i<values.length;i++)
        {
            while(num >= values[i])
            {
                num = num - values[i];
                roman.append(romanLetters[i]);
            }
        }
        return roman.toString();
    }
}
