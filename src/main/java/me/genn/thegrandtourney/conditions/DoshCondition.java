package me.genn.thegrandtourney.conditions;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.castmodifiers.conditions.util.OperatorCondition;
import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.xp.Xp;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import scala.Int;

public class DoshCondition extends OperatorCondition {
    TGT plugin;
    int amount;

    public DoshCondition() {
        this.plugin = JavaPlugin.getPlugin(TGT.class);
    }
    @Override
    public boolean initialize(String var) {
        if (super.initialize(var)) {
            try {
                this.amount = Integer.parseInt(var);
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
        if (livingEntity instanceof Player) {
            Player player = (Player)livingEntity;
            if (plugin.players.containsKey(player.getUniqueId())) {
                return this.compare(plugin.players.get(player.getUniqueId()).getPurseGold(), this.amount);
            }
        }
        return false;
    }

    @Override
    public boolean check(LivingEntity livingEntity, LivingEntity livingEntity1) {
        return this.check(livingEntity1);
    }

    @Override
    public boolean check(LivingEntity livingEntity, Location location) {
        return false;
    }
}
