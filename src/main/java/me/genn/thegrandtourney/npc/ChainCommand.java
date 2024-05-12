package me.genn.thegrandtourney.npc;

import org.bukkit.entity.Player;
import scala.concurrent.impl.FutureConvertersImpl;

import java.util.List;

public class ChainCommand {
    List<String> commands;
    Player player;

    public void run() {
        for (String command : commands) {
            this.player.performCommand(command.trim());
        }
    }

    public ChainCommand(List<String> list, Player player) {

        this.commands = list;

        this.player = player;
    }
}
