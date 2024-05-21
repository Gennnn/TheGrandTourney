package me.genn.thegrandtourney.conditions;

import com.nisovin.magicspells.castmodifiers.Condition;
import me.genn.thegrandtourney.TGT;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;

public class TGTNightCondition extends Condition {
    TGT plugin;
    public TGTNightCondition() {
        this.plugin = JavaPlugin.getPlugin(TGT.class);
    }

    @Override
    public boolean initialize(String s) {
        return true;
    }

    @Override
    public boolean check(LivingEntity livingEntity) {
        return plugin.night;
    }

    @Override
    public boolean check(LivingEntity livingEntity, LivingEntity livingEntity1) {
        return plugin.night;
    }

    @Override
    public boolean check(LivingEntity livingEntity, Location location) {
        return plugin.night;
    }
}
