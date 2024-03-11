package me.genn.thegrandtourney.npc;

import me.genn.thegrandtourney.player.ObjectiveUpdate;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.List;



public class Step {
    public String stepName;
    public BaseComponent narration;
    public List<String> dialogue;
    public boolean ranged;
    public String jumpTo;
    public List<String> rewards;
    public ObjectiveUpdate objectiveUpdate;

    Step(String name, List<String> dialogue, TextComponent narration, boolean ranged, String jumpTo, List<String> rewards, ObjectiveUpdate objectiveUpdate) {
        this.stepName = name;
        this.dialogue = dialogue;
        this.narration = narration;
        this.ranged = ranged;
        this.jumpTo = jumpTo;
        this.rewards = rewards;
        this.objectiveUpdate = objectiveUpdate;
    }

    public String getName() {
        return this.stepName;
    }


}
