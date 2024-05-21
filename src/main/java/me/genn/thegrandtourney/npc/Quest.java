package me.genn.thegrandtourney.npc;

import java.util.*;
import java.util.logging.Level;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.castmodifiers.ModifierSet;
import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.item.MMOItem;
import me.genn.thegrandtourney.player.ObjectiveUpdate;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.minecraft.network.protocol.game.PacketPlayOutNamedSoundEffect;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.trait.SkinTrait;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

@TraitName("quest")
public class Quest extends Trait {
    public Map<UUID, String> questProgress;
    public List<OfflinePlayer> onRefusalCd;
    String questName;
    int npcId;
    int walkAwayCd;
    int lineDelay;
    boolean useWordCtForDelay;
    long wordCtFactor;
    String talkSound;
    float talkVolume;
    float talkPitch;
    QuestType type;
    String skinSig;
    String skin;
    public List<Step> steps;
    public List<UUID> talkingToPlayer;

    public String heldItem;
    public String helmet;
    public String chestplate;
    public String leggings;
    public String boots;
    public String questDisplayName;
    public TGTNpc tgtNpc;

    public Quest() {
        super("quest");
        plugin = JavaPlugin.getPlugin(TGT.class);
    }

    public Quest(
            String questType,
            int walkAwayCd,
            int delay,
            boolean useWordCtForDelay,
            long wordCtFactor,
            String talkSound,
            float talkVolume,
            float talkPitch,
            String skinSig,
            String skin,
            List<Step> steps,
            Map<Equipment.EquipmentSlot, String> equipment,
            String questDisplayName,
            String questName,
            TGTNpc tgtNpc)

    {
        super(questType);
        plugin = JavaPlugin.getPlugin(TGT.class);
        this.walkAwayCd = walkAwayCd;
        this.lineDelay = delay;
        this.useWordCtForDelay = useWordCtForDelay;
        this.wordCtFactor = wordCtFactor;
        this.talkSound = talkSound;
        this.talkVolume = talkVolume;
        this.talkPitch = talkPitch;
        this.type = QuestType.QUEST;
        this.skinSig = skinSig;
        this.skin = skin;
        this.steps = steps;
        if (equipment.containsKey(Equipment.EquipmentSlot.HAND)) {
            this.heldItem = equipment.get(Equipment.EquipmentSlot.HAND);
        }
        if (equipment.containsKey(Equipment.EquipmentSlot.HELMET)) {
            this.helmet = equipment.get(Equipment.EquipmentSlot.HELMET);
        }
        if (equipment.containsKey(Equipment.EquipmentSlot.CHESTPLATE)) {
            this.chestplate = equipment.get(Equipment.EquipmentSlot.CHESTPLATE);
        }
        if (equipment.containsKey(Equipment.EquipmentSlot.LEGGINGS)) {
            this.leggings = equipment.get(Equipment.EquipmentSlot.LEGGINGS);
        }
        if (equipment.containsKey(Equipment.EquipmentSlot.BOOTS)) {
            this.boots = equipment.get(Equipment.EquipmentSlot.BOOTS);
        }
        this.questDisplayName = questDisplayName;
        this.questName = questName;
        this.tgtNpc = tgtNpc;
    }

    TGT plugin = null;

    @EventHandler
    public void click(NPCRightClickEvent event){
        Player player = event.getClicker();
        UUID pId = player.getUniqueId();
        NPC npc = event.getNPC();
        if (npc.hasTrait(Quest.class)) {
            if (this.npc == npc) {
                Quest trait = npc.getTrait(Quest.class);
                if (trait.talkingToPlayer.contains(pId)) {
                    player.sendMessage(ChatColor.RED + "This NPC is still talking!");
                } else {
                    if (!trait.questProgress.keySet().contains(pId)) {
                        trait.questProgress.put(pId, "prequest");
                    }
                    trait.dialogueTree(player);
                }

            }

        }
    }
    @EventHandler
    public void onSpawn(NPCSpawnEvent event) {

    }

    public void zap(Player player, String string, boolean dialogue) {
        Step step = steps.stream().filter(obj -> obj.stepName.equalsIgnoreCase(string)).findFirst().orElse(null);
        if (step != null && step.modifiersString != null && step.stepJumpIfFail != null) {
            ModifierSet modifiers = new ModifierSet(step.modifiersString);
            if (!modifiers.check(player)) {
                this.questProgress.put(player.getUniqueId(), step.stepJumpIfFail);
                if (dialogue) {
                    this.dialogueTree(player);
                }
                return;
            }
        }

        this.questProgress.put(player.getUniqueId(), string);
        //System.out.println("Progress for " + player.getName() + " is now " + this.questProgress.get(player.getUniqueId()));
        if (dialogue) {
            this.dialogueTree(player);
        }
    }
    public void dialogueTree(Player player) {
        String state = this.questProgress.get(player.getUniqueId());

        if (this.containsName(this.steps, state)) {
            Step step = steps.stream().filter(obj -> obj.stepName.equalsIgnoreCase(state)).findFirst().orElse(null);
            if ((step.stepName.contains("refuse") || step.stepName.contains("accept")) && !this.questProgress.get(player.getUniqueId()).equalsIgnoreCase(steps.get(0).stepName)) {

            }
            if (step.stepName.contains("refuse") || step.stepName.contains("walkaway")) {
                if (!this.onRefusalCd.contains(player)) {
                    this.denyQuest(player);
                }
            } else if (step.stepName.contains("accept")) {
                this.playSound(player, "entity.experience_orb.pickup", 1.0F, 0F);
            }
            this.createDialogue(step.dialogue, step.narration, player, step.ranged, step.rewards, step.objectiveUpdate, step.commands);
            if (step.jumpTo != null && !step.jumpTo.equalsIgnoreCase("none")) {
                this.questProgress.put(player.getUniqueId(), step.jumpTo);
            }
        } else {
            plugin.getLogger().log(Level.SEVERE, ChatColor.RED + "SOMEONE FUCKED UP ON A QUEST! QUEST FOR NPC ID " + npc.getId() + " IS NOW SOFTLOCKED!");
        }


    }


    public void createDialogue(List<String> lines, BaseComponent narration, Player player, boolean ranged, List<String> rewards, ObjectiveUpdate objectiveUpdate, List<String> commands) {
        Dialogue dialogue = new Dialogue(lines, this.lineDelay, this.useWordCtForDelay, this.talkSound, this.talkVolume, this.talkPitch, npc, plugin, this.type, this.wordCtFactor, narration, rewards, this.questName, tgtNpc, objectiveUpdate, commands);
        dialogue.speak(player, ranged);
    }
    @Override
    public void onAttach() {
        questProgress = new HashMap();
        this.onRefusalCd = new ArrayList<OfflinePlayer>();
        this.talkingToPlayer = new ArrayList<UUID>();
        if (this.npc == npc) {
            if (this.skinSig != null && this.skin != null) {
                npc.addTrait(new SkinTrait());
                npc.getOrAddTrait(SkinTrait.class).setSkinPersistent(npc.getName(), this.skinSig, this.skin);
            }
            if (hasEquipment()) {
                npc.getOrAddTrait(Equipment.class);
                if (this.heldItem != null) {
                    npc.getOrAddTrait(Equipment.class).set(Equipment.EquipmentSlot.HAND, plugin.itemHandler.getItemFromString(this.heldItem));
                }
                if (this.helmet != null) {
                    npc.getOrAddTrait(Equipment.class).set(Equipment.EquipmentSlot.HELMET, plugin.itemHandler.getItemFromString(this.helmet));
                }
                if (this.chestplate != null) {
                    npc.getOrAddTrait(Equipment.class).set(Equipment.EquipmentSlot.CHESTPLATE, plugin.itemHandler.getItemFromString(this.chestplate));
                }
                if (this.leggings != null) {
                    npc.getOrAddTrait(Equipment.class).set(Equipment.EquipmentSlot.LEGGINGS, plugin.itemHandler.getItemFromString(this.leggings));
                }
                if (this.boots != null) {
                    npc.getOrAddTrait(Equipment.class).set(Equipment.EquipmentSlot.BOOTS, plugin.itemHandler.getItemFromString(this.boots));
                }
            }

        }

    }



    public void denyQuest(Player player) {
        this.onRefusalCd.add((OfflinePlayer)player);
        this.playSound(player, "entity.zombie.attack_iron_door", 1.0F, 1.5F);
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                Quest.this.onRefusalCd.remove(Quest.this.onRefusalCd.indexOf((OfflinePlayer)player));
                Quest.this.questProgress.put(player.getUniqueId(), "prequest");
            }
        }, this.walkAwayCd * 20L);
    }
    public void playSound(Player player, String sound, float volume, float pitch) {
        player.playSound(this.npc.getEntity().getLocation(), sound, volume, pitch);
    }
    public boolean containsName(final List<Step> list, final String name){
        return list.stream().map(Step::getName).filter(name::equals).findFirst().isPresent();
    }
    public void setTalkingToPlayer(Player player) {
        if (!this.talkingToPlayer.contains(player.getUniqueId())) {
            this.talkingToPlayer.add(player.getUniqueId());
        }
    }
    public void setNotTalkingToPlayer(Player player) {
        if (this.talkingToPlayer.contains(player.getUniqueId())) {
            this.talkingToPlayer.remove(this.talkingToPlayer.indexOf(player.getUniqueId()));
        }
    }

    public boolean hasEquipment() {
        return this.heldItem != null || this.helmet != null || this.chestplate != null || this.leggings != null || this.boots != null;
    }

    public String getQuestName() {
        return this.questName;
    }
}
