package me.genn.thegrandtourney.conditions;

import com.nisovin.magicspells.castmodifiers.Condition;
import me.genn.thegrandtourney.TGT;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class HasMMOItem extends Condition {
    TGT plugin;
    String itemId;
    public HasMMOItem() {
        this.plugin = JavaPlugin.getPlugin(TGT.class);
    }
    @Override
    public boolean initialize(String s) {
        this.itemId = s;
        return true;
    }

    @Override
    public boolean check(LivingEntity livingEntity) {
        if (livingEntity instanceof Player) {
            return hasItem(((Player) livingEntity).getInventory());
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

    public boolean hasItem(Inventory inv) {
        for (ItemStack item : inv) {
            if (plugin.itemHandler.itemIsMMOItemOfName(item,this.itemId)) {
                return true;
            }
        }
        return false;
    }
}
