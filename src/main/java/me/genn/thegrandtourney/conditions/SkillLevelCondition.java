package me.genn.thegrandtourney.conditions;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.castmodifiers.Condition;
import com.nisovin.magicspells.castmodifiers.conditions.util.OperatorCondition;
import com.nisovin.magicspells.handlers.DebugHandler;
import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.player.MMOPlayer;
import me.genn.thegrandtourney.xp.Xp;
import me.genn.thegrandtourney.xp.XpType;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Debug;

public class SkillLevelCondition extends OperatorCondition {
    TGT plugin;
    XpType xpType;
    int amount;

    public SkillLevelCondition() {
        this.plugin = JavaPlugin.getPlugin(TGT.class);
    }
    @Override
    public boolean initialize(String var) {
        String[] args = var.split(";");
        if (args.length<2) {
            return false;
        } else if (!super.initialize(var)) {
            return false;
        } else {
            try {
                this.amount = Integer.parseInt(args[0].substring(1));
            } catch (NumberFormatException e) {
                MagicSpells.handleException(e);
                return false;
            }

            this.xpType = Xp.parseXpType(args[1]);

            return this.xpType != null;
        }
    }

    @Override
    public boolean check(LivingEntity livingEntity) {
        if (livingEntity instanceof Player) {
            Player player = (Player)livingEntity;
            if (plugin.players.containsKey(player.getUniqueId())) {
                return this.compare(plugin.players.get(player.getUniqueId()).getLvlForType(this.xpType), this.amount);
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
