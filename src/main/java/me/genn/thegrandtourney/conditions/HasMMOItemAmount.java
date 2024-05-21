package me.genn.thegrandtourney.conditions;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.castmodifiers.Condition;
import com.nisovin.magicspells.castmodifiers.conditions.util.OperatorCondition;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.item.MMOItem;
import me.genn.thegrandtourney.xp.Xp;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

public class HasMMOItemAmount extends OperatorCondition {
    TGT plugin;
    int amount;
    String itemId;
    public HasMMOItemAmount() {
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

            this.itemId = args[1];
            return true;
        }
    }
    @Override
    public boolean check(LivingEntity livingEntity) {
        if (livingEntity instanceof Player) {
            Player player = (Player)livingEntity;
            return this.compare(getAmount(player),this.amount);
        }
        return false;
    }
    @Override
    public boolean check(LivingEntity livingEntity, LivingEntity livingEntity1) {
        return check(livingEntity1);
    }
    @Override
    public boolean check(LivingEntity livingEntity, Location location) {
        return false;
    }

    int getAmount(Player player)
    {

        PlayerInventory inventory = player.getInventory();
        ItemStack[] items = inventory.getContents();
        int has = 0;
        for (ItemStack item : items)
        {
            if (plugin.itemHandler.itemIsMMOItemOfName(item, this.itemId)) {
                has += item.getAmount();
            }
        }
        return has;
    }
}
