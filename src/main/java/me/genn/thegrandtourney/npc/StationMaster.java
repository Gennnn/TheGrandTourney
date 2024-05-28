package me.genn.thegrandtourney.npc;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.item.ItemHandler;
import me.genn.thegrandtourney.item.MMOItem;
import me.genn.thegrandtourney.skills.Station;
import me.genn.thegrandtourney.xp.XpType;
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

@TraitName("station-master")
public class StationMaster extends Quest {
    public String stepToCraft;
    public Station station;
    public XpType xpType;
    public StationMaster() {
        plugin = JavaPlugin.getPlugin(TGT.class);
    }
    public StationMaster(
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
            String stepToCraft,
            XpType xpType,
            Map<Equipment.EquipmentSlot, String> equipment,
            String questDisplayName,
            String questName,
            TGTNpc tgtNpc

    ) {
        super("station-master", walkAwayCd, lineDelay, useWordCtForDelay, wordCtFactor, talkSound, talkVolume, talkPitch, skinSig, skin, steps, equipment, questDisplayName, questName, tgtNpc);
        this.stepToCraft = stepToCraft;
        this.xpType = xpType;
        this.type = QuestType.STATION_MASTER;
    }
    @Override
    @EventHandler
    public void click(NPCRightClickEvent event){
        Player player = event.getClicker();
        NPC npc = event.getNPC();
        if (npc.hasTrait(StationMaster.class)) {
            //plugin.getLogger().log(Level.INFO,"Successfully tracked that it's a station master, " + this.type.toString());
            if (this.npc == npc) {
                Quest trait = npc.getTrait(StationMaster.class);
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
        if (this.station == null) {
           this.station = plugin.tableHandler.getStationForCraft(this.xpType, npc.getEntity().getLocation());
        }
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
            }
            this.createDialogue(step.dialogue, step.narration, player, step.ranged, step.rewards, step.objectiveUpdate, step.commands);
            if (step.jumpTo != null && !step.jumpTo.equalsIgnoreCase("none")) {
                this.questProgress.put(player.getUniqueId(), step.jumpTo);
            }
        } else {
            plugin.getLogger().log(Level.SEVERE, ChatColor.RED + "SOMEONE FUCKED UP ON A QUEST! QUEST FOR NPC ID " + npc.getId() + " IS NOW SOFTLOCKED!");
        }


    }



}
