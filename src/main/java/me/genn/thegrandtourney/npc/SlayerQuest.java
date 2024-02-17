package me.genn.thegrandtourney.npc;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.item.ItemHandler;
import me.genn.thegrandtourney.item.MMOItem;
import me.genn.thegrandtourney.util.IntMap;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;

@TraitName("slayer")
public class SlayerQuest extends Quest {
    public String stepToFinish;
    public String mobToKill;
    public int amountToBring;
    public String stepToPerform;
    public SlayerQuest() {
        plugin = JavaPlugin.getPlugin(TGT.class);
    }
    public SlayerQuest(
            int walkAwayCd,
            int lineDelay,
            boolean useWordCtForDelay,
            long wordCtFactor,
            String talkSound,
            float talkVolume,
            float talkPitch,
            String skinSig,
            String skin,
            List<Step> steps,
            String stepToFinish,
            String mobToKill,
            int amountToBring,
            String stepToPerform,
            Map<Equipment.EquipmentSlot, String> equipment
    ) {
        super("slayer", walkAwayCd, lineDelay, useWordCtForDelay, wordCtFactor, talkSound, talkVolume, talkPitch, skinSig, skin, steps, equipment);
        this.stepToFinish = stepToFinish;
        this.mobToKill = mobToKill;
        this.amountToBring = amountToBring;
        this.stepToPerform = stepToPerform;
        this.type = QuestType.SLAYER;
    }
    @Override
    @EventHandler
    public void click(NPCRightClickEvent event){
        Player player = event.getClicker();
        NPC npc = event.getNPC();
        if (npc.hasTrait(SlayerQuest.class)) {
            if (this.npc == npc) {
                Quest trait = npc.getTrait(SlayerQuest.class);
                if (trait.talkingToPlayer.contains(player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "This NPC is still talking!");
                } else {
                    if (!trait.questProgress.containsKey(player.getUniqueId())) {
                        trait.questProgress.put(player.getUniqueId(), "prequest");
                    }
                    trait.dialogueTree(player);
                }

            }

        }
    }
    @Override
    public void dialogueTree(Player player) {
        String state = this.questProgress.get(player.getUniqueId());

        if (this.containsName(this.steps, state)) {
            Step step = steps.stream().filter(obj -> obj.stepName.equalsIgnoreCase(state)).findFirst().orElse(null);
            if (step.stepName.contains("refuse") || step.stepName.contains("walkaway")) {
                if (!this.onRefusalCd.contains(player)) {
                    this.denyQuest(player);
                }
            } else if (step.stepName.contains("accept")) {
                this.playSound(player, "random.orb", 1.0F, 1.5F);
                this.playSound(player, "random.levelup", 1.0F, 2.0F);
                if (plugin.players.containsKey(player.getUniqueId()) && !plugin.players.get(player.getUniqueId()).slayerMap.containsKey(this.questName)) {
                    IntMap map = new IntMap();
                    map.put(plugin.mobHandler.getMobFromString(this.mobToKill), 0);
                    plugin.players.get(player.getUniqueId()).slayerMap.put(this.questName, map);
                }
            } else if (step.stepName.equalsIgnoreCase(this.stepToFinish)) {
                if (plugin.players.containsKey(player.getUniqueId()) && plugin.players.get(player.getUniqueId()).slayerMap.containsKey(this.questName)) {
                    IntMap map = plugin.players.get(player.getUniqueId()).slayerMap.get(this.questName);
                    if (map.containsKey(plugin.mobHandler.getMobFromString(this.mobToKill)) && map.get(plugin.mobHandler.getMobFromString(this.mobToKill)) >= this.amountToBring) {
                        this.questProgress.put(player.getUniqueId(), this.stepToPerform);
                        dialogueTree(player);
                        return;
                    }
                }
            }
            this.createDialogue(step.dialogue, step.narration, player, step.ranged, step.rewards);
            if (step.jumpTo != null && !step.jumpTo.equalsIgnoreCase("none")) {
                this.questProgress.put(player.getUniqueId(), step.jumpTo);
            }
        } else {
            plugin.getLogger().log(Level.SEVERE, ChatColor.RED + "SOMEONE FUCKED UP ON A QUEST! QUEST FOR NPC ID " + npc.getId() + " IS NOW SOFTLOCKED!");
        }

    }



}
