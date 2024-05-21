package me.genn.thegrandtourney.npc;

import com.nisovin.magicspells.castmodifiers.ModifierSet;
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
    public List<String> modifiersString;
    public String stepJumpIfFail;


    Step(String name, List<String> dialogue, TextComponent narration, boolean ranged, String jumpTo, List<String> rewards, ObjectiveUpdate objectiveUpdate, List<String> commands, List<String> modifierStrings, String stepJumpIfFail) {
        this.stepName = name;
        this.dialogue = dialogue;
        this.narration = narration;
        this.ranged = ranged;
        this.jumpTo = jumpTo;
        this.rewards = rewards;
        this.objectiveUpdate = objectiveUpdate;
        this.commands = commands;
        for (int i = 0; i < modifierStrings.size(); i++) {
            modifierStrings.set(i, modifierStrings.get(i) + "$$aaa");
        }
        this.modifiersString = modifierStrings;
        this.stepJumpIfFail = stepJumpIfFail;
    }

    public String getName() {
        return this.stepName;
    }


}
