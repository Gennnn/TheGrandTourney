package me.genn.thegrandtourney.npc;

import org.bukkit.entity.Player;
import scala.concurrent.impl.FutureConvertersImpl;

import java.util.List;

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

    public ChainCommand(List<String> list, Player player) {

        String startString = "";
        for (int i = 0; i < list.size(); i++) {
            String str = list.get(i);
            String[] partsOfCmd =  str.split(" ");
            for (String part : partsOfCmd) {
                startString = startString.concat(part + " ");
            }
            if (i < list.size()-1) {
                startString = startString.concat("| ");
            }
        }
        this.args = startString.split(" ");

        this.player = player;
    }
}
