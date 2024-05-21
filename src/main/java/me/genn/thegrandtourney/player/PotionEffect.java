package me.genn.thegrandtourney.player;

import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PotionEffect {
    public long expiryTime;
    HashSet<StatBuff> statsImpacted = new HashSet<>();
    boolean added = false;
    public String name;
    public int lvl;
    public ItemStack activatingItem;

    public PotionEffect(String name, long expiryTime, int lvl, ItemStack activatingItem) {
        this.name = name;
        this.expiryTime = expiryTime;
        this.lvl = lvl;
        this.activatingItem = activatingItem;
    }

    public void addStat(String name, float value) {
        StatBuff buff = new StatBuff(name,value,this.expiryTime);
        this.statsImpacted.add(buff);
    }

    public void addStatsForName(MMOPlayer mmoPlayer) {
        if (name.equalsIgnoreCase("restoration")) {
            addStat("stamina",50*lvl);
            mmoPlayer.heal(100 * lvl);
        } else if (name.equalsIgnoreCase("adrenaline")) {
            addStat("speed",5*lvl);
            addStat("absorption",20*lvl);
        } else if (name.equalsIgnoreCase("agility")) {
            addStat("evasiveness",10*lvl);
            addStat("speed",10*lvl);
        } else if (name.equalsIgnoreCase("critical")) {
            addStat("crit-chance",5*lvl);
            addStat("crit-damage",10*lvl);
        } else if (name.equalsIgnoreCase("dodge")) {
            addStat("evasiveness", 10*lvl);
        } else if (name.equalsIgnoreCase("spirit")) {
            addStat("speed",10*lvl);
            addStat("crit-damage", 10*lvl);
        } else if (name.equalsIgnoreCase("regeneration")) {
            addStat("health-regen", 5*lvl);
        } else if (name.equalsIgnoreCase("speed")) {
            addStat("speed", 5*lvl);
        } else if (name.equalsIgnoreCase("strength")) {
            addStat("strength",5*lvl);
        } else if (name.equalsIgnoreCase("heal")) {
            if (lvl == 1) {
                mmoPlayer.heal(20);
            } else if (lvl < 6) {
                mmoPlayer.heal((lvl-1)*50);
            } else {
                mmoPlayer.heal(300 + ((lvl-6)*100));
            }
        } else if (name.equalsIgnoreCase("absorption")) {
            if (lvl < 6) {
                addStat("absorption",20*lvl);
            } else if (lvl == 6) {
                addStat("absorption",150);
            } else {
                addStat("absorption", 200 + (100*(7-lvl)));
            }
        } else if (name.equalsIgnoreCase("resistance")) {
            addStat("defense", 5*lvl);
        } else if (name.equalsIgnoreCase("spelunker")) {
            addStat("mining-fortune", 5*lvl);
        } else if (name.equalsIgnoreCase("angler")) {
            addStat("fishing-speed", 10*lvl);
        } else if (name.equalsIgnoreCase("harvester")) {
            addStat("farming-fortune", 5*lvl);
        }

    }

    public String getName() {
        return this.name;
    }

}
