package me.genn.thegrandtourney.npc;

import me.genn.thegrandtourney.player.ObjectiveUpdate;
import me.genn.thegrandtourney.xp.XpType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;



public class Step {
    public String stepName;
    public BaseComponent narration;
    public List<String> dialogue;
    public boolean ranged;
    public String jumpTo;
    public List<String> rewards;
    public List<String> commands = new ArrayList<>();
    public ObjectiveUpdate objectiveUpdate;
    public XpType requiredXpType;
    public int requiredLvl;
    public String stepToJumpIfFail;


    Step(String name, List<String> dialogue, TextComponent narration, boolean ranged, String jumpTo, List<String> rewards, ObjectiveUpdate objectiveUpdate, List<String> commands, XpType requiredXpType, int requiredLvl, String stepToJumpIfFail) {
        this.stepName = name;
        this.dialogue = dialogue;
        this.narration = narration;
        this.ranged = ranged;
        this.jumpTo = jumpTo;
        this.rewards = rewards;
        this.objectiveUpdate = objectiveUpdate;
        this.commands = commands;
        this.requiredXpType = requiredXpType;
        this.requiredLvl = requiredLvl;
        this.stepToJumpIfFail = stepToJumpIfFail;
    }

    public String getName() {
        return this.stepName;
    }


}
