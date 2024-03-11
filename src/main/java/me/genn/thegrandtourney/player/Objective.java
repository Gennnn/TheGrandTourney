package me.genn.thegrandtourney.player;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Objective {
    public String questName;
    public ItemStack icon = new ItemStack(Material.PLAYER_HEAD);
    public List<String> status;
    public boolean completed = false;
    public Location objectiveLocation;
    public String trackingText;
    public List<List<String>> objectiveUpdatesPassed;

    public Objective(String questName, List<String> status, String trackingText) {
        this.questName = questName;
        this.status = status;
        this.trackingText = trackingText;
        this.objectiveUpdatesPassed = new ArrayList<List<String>>();
    }



}
