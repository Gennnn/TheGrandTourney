package me.genn.thegrandtourney.xp;

import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.npc.ChainCommand;
import me.genn.thegrandtourney.npc.Dialogue;
import me.genn.thegrandtourney.player.MMOPlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import scala.concurrent.impl.FutureConvertersImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class Xp {
    public static Map<Integer, Double> xpForLevel;
    static TGT plugin;
    public Xp(TGT plugin) {
        Xp.plugin = plugin;
        Xp.xpForLevel = new HashMap<>();
        xpForLevel.put(0,0.0D);
        for (int i = 1; i <= 10; i++) {
            double baseXp = 0;
            if (i != 1) {
                baseXp = xpForLevel.get(i-1);
            }
            double extraXp = (62.5 * (Math.pow(i,2)))+(37.5*i);
            xpForLevel.put(i, (baseXp + extraXp));
            plugin.getLogger().log(Level.INFO, "Set xp needed for level " + i + " to " + xpForLevel.get(i) ) ;
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
            this.sendMessagesForLevel(bukkitPlayer, type);
        } else {
            bukkitPlayer.setExp((float) (player.getXpForType(type)/xpForLevel.get(player.getLvlForType(type) + 1)));
            bukkitPlayer.setLevel(player.getLvlForType(type));
        }

    }
    public void sendMessagesForLevel(Player bukkitPlayer, XpType type) {
        MMOPlayer player = plugin.players.get(bukkitPlayer.getUniqueId());

        bukkitPlayer.sendMessage(ChatColor.DARK_AQUA + "=====================================================");
        if ((player.getLvlForType(type) - 1) > 0 ) {
            bukkitPlayer.sendMessage(ChatColor.AQUA + "  " + ChatColor.BOLD + "SKILL LEVEL UP " +ChatColor.RESET + "" + ChatColor.DARK_AQUA + type.getName() + " " + ChatColor.DARK_GRAY + intToRoman(player.getLvlForType(type)-1) + "➡" + ChatColor.DARK_AQUA + intToRoman(player.getLvlForType(type)));
        } else {
            bukkitPlayer.sendMessage(ChatColor.AQUA + "  " + ChatColor.BOLD + "SKILL LEVEL UP " +ChatColor.RESET + "" + ChatColor.DARK_AQUA + type.getName() + " " + ChatColor.DARK_AQUA + intToRoman(player.getLvlForType(type)));

        }
        bukkitPlayer.sendMessage("");
        bukkitPlayer.sendMessage("  " + ChatColor.GREEN + "" + ChatColor.BOLD + "REWARDS");
        List<String> text = plugin.rewardsHandler.getTableForType(type).getRewardsForLevel(player.getLvlForType(type)).rewardText;
        for (String str : text) {
            bukkitPlayer.sendMessage("    " + str);
        }
        bukkitPlayer.sendMessage("");
        bukkitPlayer.sendMessage(ChatColor.DARK_AQUA + "=====================================================");
        ChainCommand chain = new ChainCommand(plugin.rewardsHandler.getTableForType(type).getRewardsForLevel(player.getLvlForType(type)).rewardCommands, bukkitPlayer);
        chain.run();
        double numerator = ( player.getXpForType(type) - ( xpForLevel.get(player.getLvlForType(type))));
        double denominator = (( xpForLevel.get(player.getLvlForType(type) + 1)) - xpForLevel.get(player.getLvlForType(type) ));
        bukkitPlayer.playSound(bukkitPlayer, "entity.player.levelup", 1.0f, 1.0f);
        bukkitPlayer.setExp((float)  (numerator/denominator));
        bukkitPlayer.setLevel(player.getLvlForType(type));
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
