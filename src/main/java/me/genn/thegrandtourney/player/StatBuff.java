package me.genn.thegrandtourney.player;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class StatBuff {
    long expiryTime;
    String statName;
    float amount;
    boolean added = false;

    public StatBuff(String statName, float amount, long expiryTime) {
        this.statName = statName;
        this.amount = amount;
        this.expiryTime = expiryTime;
    }

    public long getExpiryTime() {
        return this.expiryTime;
    }



}
