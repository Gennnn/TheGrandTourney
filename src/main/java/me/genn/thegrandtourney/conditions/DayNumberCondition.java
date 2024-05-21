package me.genn.thegrandtourney.conditions;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.castmodifiers.Condition;
import com.nisovin.magicspells.castmodifiers.conditions.util.OperatorCondition;
import me.genn.thegrandtourney.TGT;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;

public class DayNumberCondition extends OperatorCondition {
    TGT plugin;
    int number;
    public DayNumberCondition() {
        this.plugin = JavaPlugin.getPlugin(TGT.class);
    }
    @Override
    public boolean initialize(String var) {
        if (super.initialize(var)) {
            try {
                this.number = Integer.parseInt(var);
                return true;
            } catch (NumberFormatException e) {
                MagicSpells.handleException(e);
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean check(LivingEntity livingEntity) {
        return compare(plugin.day, number);
    }

    @Override
    public boolean check(LivingEntity livingEntity, LivingEntity livingEntity1) {
        return compare(plugin.day, number);
    }

    @Override
    public boolean check(LivingEntity livingEntity, Location location) {
        return compare(plugin.day, number);
    }
}
