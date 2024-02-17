package me.genn.thegrandtourney.mobs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.MythicBukkit;
import me.genn.thegrandtourney.TGT;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;


import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.SkinTrait;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class MMOMob {
    public MythicMob mythicmob;
    public String nameplateName;
    public EntityType entityType;
    public boolean isFakePlayer = false;
    public float defense;
    public String internalName;



    public static MMOMob create(ConfigurationSection config) throws IOException {
        MMOMob mob = new MMOMob();
        TGT plugin = JavaPlugin.getPlugin(TGT.class);
        mob.internalName = config.getName();
        mob.mythicmob = MythicBukkit.inst().getMobManager().getMythicMob(mob.internalName).stream().findFirst().orElse(null);
        if (mob.mythicmob == null) {
            plugin.getLogger().severe("MMMob " + mob.internalName + " had no matching MythicMob!");
            return null;
        }
        mob.nameplateName = ChatColor.translateAlternateColorCodes('&',config.getString("display-name"));
        mob.entityType = EntityType.valueOf(mob.mythicmob.getEntityTypeString().toUpperCase());
        if (mob.entityType == null) {
            mob.entityType = EntityType.ZOMBIE;
        }
        if (mob.mythicmob.isFakePlayer()) {
            mob.isFakePlayer = true;
        }
        String isFake = config.getString("is-player");
        if (isFake != null) {
            mob.isFakePlayer = true;
        }
        mob.defense = config.getInt("defense", 5);

        return mob;
    }



    public String getName() {
        return this.internalName;
    }




}
