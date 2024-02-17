package me.genn.thegrandtourney.npc;

import org.bukkit.entity.Player;

public class ChainCommand {
    String[] args;
    Player player;
    public ChainCommand(String[] args, Player player) {
        this.args = args;
        this.player = player;
    }

    public void run() {
        String str = String.join(" ", this.args);
        String[] seperatedCommands = str.split("\\|");
        for (String command : seperatedCommands) {
            this.player.performCommand(command.trim());
        }
    }
}
