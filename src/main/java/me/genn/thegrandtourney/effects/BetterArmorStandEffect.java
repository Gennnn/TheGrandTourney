//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package me.genn.thegrandtourney.effects;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spelleffects.SpellEffect;
import com.nisovin.magicspells.spelleffects.effecttypes.ArmorStandEffect;
import com.nisovin.magicspells.util.EntityData;
import com.nisovin.magicspells.util.SpellData;
import com.nisovin.magicspells.util.Util;
import com.nisovin.magicspells.util.config.ConfigData;
import com.nisovin.magicspells.util.config.ConfigDataUtil;
import com.nisovin.magicspells.util.magicitems.MagicItem;
import com.nisovin.magicspells.util.magicitems.MagicItems;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

public class BetterArmorStandEffect extends ArmorStandEffect {
    public static final String ENTITY_TAG = "MS_ARMOR_STAND";
    private EntityData entityData;
    private boolean gravity;
    private String customName;
    private boolean customNameVisible;
    private ItemStack headItem;
    private ItemStack mainhandItem;
    private ItemStack offhandItem;
    private ConfigData<Vector> headAngle;
    private ConfigData<Vector> bodyAngle;
    private ConfigData<Vector> rightArmAngle;
    private ConfigData<Vector> leftArmAngle;
    private ConfigData<Boolean> repeatHeadRotation;
    private ConfigData<Boolean> repeatBodyRotation;
    private ConfigData<Boolean> repeatRightArmRotation;
    private ConfigData<Boolean> repeatLeftArmRotation;

    public BetterArmorStandEffect() {
    }

    protected void loadFromConfig(ConfigurationSection config) {
        ConfigurationSection section = config.getConfigurationSection("armorstand");
        if (section != null) {
            this.entityData = new EntityData(section);
            this.entityData.setEntityType((data) -> {
                return EntityType.ARMOR_STAND;
            });
            this.gravity = section.getBoolean("gravity", false);
            this.customName = section.getString("custom-name", "");
            this.customNameVisible = section.getBoolean("custom-name-visible", false);
            String strMagicItem = section.getString("head", "");
            MagicItem magicItem = MagicItems.getMagicItemFromString(strMagicItem);
            if (magicItem != null) {
                this.headItem = magicItem.getItemStack();
            }

            strMagicItem = section.getString("mainhand", "");
            magicItem = MagicItems.getMagicItemFromString(strMagicItem);
            if (magicItem != null) {
                this.mainhandItem = magicItem.getItemStack();
            }

            strMagicItem = section.getString("offhand", "");
            magicItem = MagicItems.getMagicItemFromString(strMagicItem);
            if (magicItem != null) {
                this.offhandItem = magicItem.getItemStack();
            }
            this.headAngle = ConfigDataUtil.getVector(config,"head-angle", new Vector(0,0,0));
            this.bodyAngle = ConfigDataUtil.getVector(config,"body-angle", new Vector(0,0,0));
            this.rightArmAngle = ConfigDataUtil.getVector(config,"right-arm-angle", new Vector(0,0,0));
            this.leftArmAngle = ConfigDataUtil.getVector(config,"left-arm-angle", new Vector(0,0,0));
            this.repeatRightArmRotation = ConfigDataUtil.getBoolean(config,"repeat-right-arm-rot", false);
            this.repeatLeftArmRotation = ConfigDataUtil.getBoolean(config,"repeat-left-arm-rot", false);
            this.repeatHeadRotation = ConfigDataUtil.getBoolean(config,"repeat-head-rot", false);
            this.repeatBodyRotation = ConfigDataUtil.getBoolean(config,"repeat-body-rot", false);
        }
    }

    protected ArmorStand playArmorStandEffectLocation(Location location, SpellData data) {
        ArmorStand as = (ArmorStand)this.entityData.spawn(this.applyOffsets(location,data), data, (entity) -> {
            ArmorStand armorStand = (ArmorStand)entity;
            armorStand.addScoreboardTag("MS_ARMOR_STAND");
            armorStand.setGravity(this.gravity);
            armorStand.setSilent(true);
            armorStand.customName(Util.getMiniMessage(this.customName, data));
            armorStand.setCustomNameVisible(this.customNameVisible);
            armorStand.setItem(EquipmentSlot.HEAD, this.headItem);
            armorStand.setItem(EquipmentSlot.HAND, this.mainhandItem);
            armorStand.setItem(EquipmentSlot.OFF_HAND, this.offhandItem);
            armorStand.setHeadPose(new EulerAngle(Math.toRadians(this.headAngle.get().getX()),Math.toRadians(this.headAngle.get().getY()),Math.toRadians(this.headAngle.get().getZ())));
            armorStand.setBodyPose(new EulerAngle(Math.toRadians(this.bodyAngle.get().getX()),Math.toRadians(this.bodyAngle.get().getY()),Math.toRadians(this.bodyAngle.get().getZ())));
            armorStand.setRightArmPose(new EulerAngle(Math.toRadians(this.rightArmAngle.get().getX()),Math.toRadians(this.rightArmAngle.get().getY()),Math.toRadians(this.rightArmAngle.get().getZ())));
            armorStand.setLeftArmPose(new EulerAngle(Math.toRadians(this.leftArmAngle.get().getX()),Math.toRadians(this.leftArmAngle.get().getY()),Math.toRadians(this.leftArmAngle.get().getZ())));

        });

        new BukkitRunnable() {

            @Override
            public void run() {
                if (!as.isValid()) {
                    this.cancel();
                    return;
                }
                if (BetterArmorStandEffect.this.repeatHeadRotation.get()) {
                    as.setHeadPose(as.getHeadPose().add(Math.toRadians(BetterArmorStandEffect.this.headAngle.get().getX()),Math.toRadians(BetterArmorStandEffect.this.headAngle.get().getY()),Math.toRadians(BetterArmorStandEffect.this.headAngle.get().getZ())));
                }
                if (BetterArmorStandEffect.this.repeatBodyRotation.get()) {
                    as.setBodyPose(as.getBodyPose().add(Math.toRadians(BetterArmorStandEffect.this.bodyAngle.get().getX()),Math.toRadians(BetterArmorStandEffect.this.bodyAngle.get().getY()),Math.toRadians(BetterArmorStandEffect.this.bodyAngle.get().getZ())));
                }
                if (BetterArmorStandEffect.this.repeatRightArmRotation.get()) {
                    as.setRightArmPose(as.getRightArmPose().add(Math.toRadians(BetterArmorStandEffect.this.rightArmAngle.get().getX()),Math.toRadians(BetterArmorStandEffect.this.rightArmAngle.get().getY()),Math.toRadians(BetterArmorStandEffect.this.rightArmAngle.get().getZ())));
                }
                if (BetterArmorStandEffect.this.repeatLeftArmRotation.get()) {
                    as.setLeftArmPose(as.getLeftArmPose().add(Math.toRadians(BetterArmorStandEffect.this.leftArmAngle.get().getX()),Math.toRadians(BetterArmorStandEffect.this.leftArmAngle.get().getY()),Math.toRadians(BetterArmorStandEffect.this.leftArmAngle.get().getZ())));
                }
            }
        }.runTaskTimer(MagicSpells.getInstance(), 0L, 1L);
        return as;
    }
}
