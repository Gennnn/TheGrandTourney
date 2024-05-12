package me.genn.thegrandtourney.dungeons;

import me.genn.thegrandtourney.dungeons.RoomGoal;
import me.genn.thegrandtourney.item.DropTable;
import me.genn.thegrandtourney.item.MMOItem;
import me.genn.thegrandtourney.mobs.MMOMob;
import org.bukkit.Material;
import org.bukkit.Particle;

public class RoomData {
    public RoomGoal goal;
    public String toProgress;
    public int quantity;
    public boolean preventAbilities = false;
    public boolean doorClosedByDefault = true;
    public Material doorMat;
    public String name;
    public String goalText;
    public Material rewardChestBase;
    public Material rewardChestMid;
    public String rewardChestBase64;
    public DropTable rewardChestDrops;
    public Particle chestParticle;
    public int chestParticleCount;

    String getRoomName() {
        return name;
    }

}