package me.genn.thegrandtourney.npc;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.item.ItemHandler;
import me.genn.thegrandtourney.item.MMOItem;
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

@TraitName("retrieval")
public class ItemRetrievalQuest extends Quest {
    public String stepToFinish;
    public String itemIdToBring;
    public int amountToBring;
    public String stepToPerform;
    public ItemRetrievalQuest() {
        plugin = JavaPlugin.getPlugin(TGT.class);
    }
    public ItemRetrievalQuest(
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
            String itemIdToBring,
            int amountToBring,
            String stepToPerform,
            Map<Equipment.EquipmentSlot, String> equipment,
            String questDisplayName,
            String questName,
            TGTNpc tgtNpc

    ) {
        super("retrieval", walkAwayCd, lineDelay, useWordCtForDelay, wordCtFactor, talkSound, talkVolume, talkPitch, skinSig, skin, steps, equipment, questDisplayName, questName, tgtNpc);
        this.stepToFinish = stepToFinish;
        this.itemIdToBring = itemIdToBring;
        this.amountToBring = amountToBring;
        this.stepToPerform = stepToPerform;
        this.type = QuestType.RETRIEVAL;
    }
    @Override
    @EventHandler
    public void click(NPCRightClickEvent event){
        Player player = event.getClicker();
        NPC npc = event.getNPC();
        if (npc.hasTrait(ItemRetrievalQuest.class)) {
            if (this.npc == npc) {
                Quest trait = npc.getTrait(ItemRetrievalQuest.class);
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
            } else if (step.stepName.equalsIgnoreCase(this.stepToFinish)) {
                if (this.getAmount(player)) {
                    this.questProgress.put(player.getUniqueId(), this.stepToPerform);
                    MMOItem.removeItem(player, plugin.itemHandler.getMMOItemFromString(this.itemIdToBring), this.amountToBring);
                    dialogueTree(player);
                    return;
                }
            }
            this.createDialogue(step.dialogue, step.narration, player, step.ranged, step.rewards, step.objectiveUpdate);
            if (step.jumpTo != null && !step.jumpTo.equalsIgnoreCase("none")) {
                this.questProgress.put(player.getUniqueId(), step.jumpTo);
            }
        } else {
            plugin.getLogger().log(Level.SEVERE, ChatColor.RED + "SOMEONE FUCKED UP ON A QUEST! QUEST FOR NPC ID " + npc.getId() + " IS NOW SOFTLOCKED!");
        }


    }
    public boolean getAmount(Player player)
    {
        PlayerInventory inventory = player.getInventory();
        ItemStack[] items = inventory.getContents();
        int has = 0;
        for (ItemStack item : items)
        {
            if ((item != null) && (item.getAmount() > 0) && (item.hasItemMeta()))
            {
                NBTItem nbtI = new NBTItem(item);
                if (nbtI.hasTag("ExtraAttributes")) {
                    NBTCompound comp = nbtI.getCompound("ExtraAttributes");
                    if (comp.hasTag("id")) {
                        String mmoId = comp.getString("id");
                        if (mmoId.equalsIgnoreCase(this.itemIdToBring)) {
                            has += item.getAmount();
                        }
                    }
                }

            }
        }
        return (has >= this.amountToBring);
    }
}
