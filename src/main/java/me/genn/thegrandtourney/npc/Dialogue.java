package me.genn.thegrandtourney.npc;

import java.util.List;

import me.genn.thegrandtourney.TGT;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.md_5.bungee.api.chat.BaseComponent;

public class Dialogue {
    public TGT plugin;
    List<String> lines;
    String walkAwayDialogue;
    int delayBetweenLines;
    boolean useWordCountForDelay;
    String talkSound;
    float talkVolume;
    float talkPitch;
    NPC npc;
    Trait trait;
    QuestType type;
    long wordCountMult;
    String postDialogueText;
    BaseComponent postDialogueComponentText;
    List<String> rewards;




    Dialogue(List<String> lines, int delay, boolean useWordCt, String talkSound, float talkVolume, float talkPitch, NPC npc, TGT tgt, QuestType questType, long wordCountMult, BaseComponent postDialogueText, List<String> rewards) {
        this.lines = lines;
        this.delayBetweenLines = delay;
        this.useWordCountForDelay = useWordCt;
        this.wordCountMult = wordCountMult;
        this.talkSound = talkSound;
        this.talkVolume = talkVolume;
        this.talkPitch = talkPitch;
        this.npc = npc;
        this.plugin = tgt;
        this.type = questType;
        this.postDialogueText = null;
        this.postDialogueComponentText = postDialogueText;
        this.rewards = rewards;
    }

    public void speak(Player player, boolean isRanged) {
        if (type == QuestType.RETRIEVAL) {
            ItemRetrievalQuest trait = npc.getTrait(ItemRetrievalQuest.class);
            if (isRanged) {
                trait.setTalkingToPlayer(player);
                this.speakRangedRetrieval(0, player);
            } else {
                trait.setTalkingToPlayer(player);
                this.speakUnrangedRetrieval(0, player);
            }
        } else if (type == QuestType.SLAYER) {
            SlayerQuest trait = npc.getTrait(SlayerQuest.class);
            if (isRanged) {
                trait.setTalkingToPlayer(player);
                this.speakRangedSlayer(0, player);
            } else {
                trait.setTalkingToPlayer(player);
                this.speakUnrangedSlayer(0, player);
            }
        } else {
            Quest trait = npc.getTrait(Quest.class);
            if (isRanged) {
                trait.setTalkingToPlayer(player);
                this.speakRangedQuest(0, player);
            } else {
                trait.setTalkingToPlayer(player);
                this.speakUnrangedQuest(0, player);
            }
        }


    }

    private void speakRangedSlayer(int lineNum, Player player) {
        String line = lines.get(lineNum);
        player.sendMessage(ChatColor.WHITE + "<" + ChatColor.RESET + npc.getFullName() + ChatColor.WHITE.toString() + "> " + ChatColor.translateAlternateColorCodes('&', line));
        playSpeakSound(player, talkSound, talkVolume, talkPitch);


        long delay = 0L;
        if (Dialogue.this.useWordCountForDelay) {
            String[] wordCount = line.split(" ");
            delay = wordCount.length * this.wordCountMult;
        } else {
            delay = Dialogue.this.delayBetweenLines * 20L;
        }
        if (lineNum == (this.lines.size() - 1) ) {
            SlayerQuest trait = npc.getTrait(SlayerQuest.class);
            if (this.postDialogueComponentText == null) {
                trait.setNotTalkingToPlayer(player);
                return;
            }
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    player.spigot().sendMessage(Dialogue.this.postDialogueComponentText);
                    trait.setNotTalkingToPlayer(player);
                    if (Dialogue.this.rewards != null && !Dialogue.this.rewards.isEmpty()) {
                        Dialogue.this.giveRewards(player);
                    }

                }
            }, delay * 1L);
            return;
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                if (player.getLocation().distanceSquared(npc.getEntity().getLocation()) > 20) {
                    if (Dialogue.this.type == QuestType.RETRIEVAL) {
                        ItemRetrievalQuest trait = npc.getTrait(ItemRetrievalQuest.class);
                        trait.zap(player, "walkaway", true);
                    }
                    return;
                }
                speakRangedSlayer(lineNum + 1, player);
            }
        }, delay * 1L);
    }

    private void speakUnrangedSlayer(int lineNum, Player player) {
        String line = lines.get(lineNum);
        player.sendMessage(ChatColor.WHITE + "<" + ChatColor.RESET + npc.getFullName() + ChatColor.WHITE.toString() + "> " + ChatColor.translateAlternateColorCodes('&', line));
        playSpeakSound(player, talkSound, talkVolume, talkPitch);


        long delay = 0L;
        if (Dialogue.this.useWordCountForDelay) {
            String[] wordCount = line.split(" ");
            delay = wordCount.length * this.wordCountMult;
        } else {
            delay = Dialogue.this.delayBetweenLines * 20L;
        }
        if (lineNum == (this.lines.size() - 1) ) {
            SlayerQuest trait = npc.getTrait(SlayerQuest.class);
            if (this.postDialogueComponentText == null) {
                trait.setNotTalkingToPlayer(player);
                return;
            }
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    player.spigot().sendMessage(Dialogue.this.postDialogueComponentText);
                    trait.setNotTalkingToPlayer(player);
                    if (Dialogue.this.rewards != null && !Dialogue.this.rewards.isEmpty()) {
                        Dialogue.this.giveRewards(player);
                    }
                }
            }, delay * 1L);
            return;
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                speakUnrangedSlayer(lineNum + 1, player);
            }
        }, delay* 1L);
    }

    private void speakRangedRetrieval(int lineNum, Player player) {
        String line = lines.get(lineNum);
        player.sendMessage(ChatColor.WHITE + "<" + ChatColor.RESET + npc.getFullName() + ChatColor.WHITE.toString() + "> " + ChatColor.translateAlternateColorCodes('&', line));
        playSpeakSound(player, talkSound, talkVolume, talkPitch);


        long delay = 0L;
        if (Dialogue.this.useWordCountForDelay) {
            String[] wordCount = line.split(" ");
            delay = wordCount.length * this.wordCountMult;
        } else {
            delay = Dialogue.this.delayBetweenLines * 20L;
        }
        if (lineNum == (this.lines.size() - 1) ) {
            ItemRetrievalQuest trait = npc.getTrait(ItemRetrievalQuest.class);
            if (this.postDialogueComponentText == null) {
                trait.setNotTalkingToPlayer(player);
                return;
            }
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    player.spigot().sendMessage(Dialogue.this.postDialogueComponentText);
                    trait.setNotTalkingToPlayer(player);
                    if (Dialogue.this.rewards != null && !Dialogue.this.rewards.isEmpty()) {
                        Dialogue.this.giveRewards(player);
                    }

                }
            }, delay * 1L);
            return;
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                if (player.getLocation().distanceSquared(npc.getEntity().getLocation()) > 20) {
                    if (Dialogue.this.type == QuestType.RETRIEVAL) {
                        ItemRetrievalQuest trait = npc.getTrait(ItemRetrievalQuest.class);
                        trait.zap(player, "walkaway", true);
                    }
                    return;
                }
                speakRangedRetrieval(lineNum + 1, player);
            }
        }, delay * 1L);
    }

    private void speakUnrangedRetrieval(int lineNum, Player player) {
        String line = lines.get(lineNum);
        player.sendMessage(ChatColor.WHITE + "<" + ChatColor.RESET + npc.getFullName() + ChatColor.WHITE.toString() + "> " + ChatColor.translateAlternateColorCodes('&', line));
        playSpeakSound(player, talkSound, talkVolume, talkPitch);


        long delay = 0L;
        if (Dialogue.this.useWordCountForDelay) {
            String[] wordCount = line.split(" ");
            delay = wordCount.length * this.wordCountMult;
        } else {
            delay = Dialogue.this.delayBetweenLines * 20L;
        }
        if (lineNum == (this.lines.size() - 1) ) {
            ItemRetrievalQuest trait = npc.getTrait(ItemRetrievalQuest.class);
            if (this.postDialogueComponentText == null) {
                trait.setNotTalkingToPlayer(player);
                return;
            }
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    player.spigot().sendMessage(Dialogue.this.postDialogueComponentText);
                    trait.setNotTalkingToPlayer(player);
                    if (Dialogue.this.rewards != null && !Dialogue.this.rewards.isEmpty()) {
                        Dialogue.this.giveRewards(player);
                    }
                }
            }, delay * 1L);
            return;
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                speakUnrangedRetrieval(lineNum + 1, player);
            }
        }, delay* 1L);
    }
    private void speakRangedQuest(int lineNum, Player player) {
        String line = lines.get(lineNum);
        player.sendMessage(ChatColor.WHITE + "<" + ChatColor.RESET + npc.getFullName() + ChatColor.WHITE.toString() + "> " + ChatColor.translateAlternateColorCodes('&', line));
        playSpeakSound(player, talkSound, talkVolume, talkPitch);


        long delay = 0L;
        if (Dialogue.this.useWordCountForDelay) {
            String[] wordCount = line.split(" ");
            delay = wordCount.length * this.wordCountMult;
        } else {
            delay = Dialogue.this.delayBetweenLines * 20L;
        }
        if (lineNum == (this.lines.size() - 1) ) {
            Quest trait = npc.getTrait(Quest.class);
            if (this.postDialogueComponentText == null) {
                trait.setNotTalkingToPlayer(player);
                return;
            }
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    player.spigot().sendMessage(Dialogue.this.postDialogueComponentText);
                    trait.setNotTalkingToPlayer(player);
                    if (Dialogue.this.rewards != null && !Dialogue.this.rewards.isEmpty()) {
                        Dialogue.this.giveRewards(player);
                    }
                }
            }, delay * 1L);
            return;
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                if (player.getLocation().distanceSquared(npc.getEntity().getLocation()) > 20) {
                    if (Dialogue.this.type == QuestType.QUEST) {
                        Quest trait = npc.getTrait(Quest.class);
                        trait.zap(player, "walkaway", true);
                    }
                    return;
                }
                speakRangedQuest(lineNum + 1, player);
            }
        }, delay * 1L);
    }

    private void speakUnrangedQuest(int lineNum, Player player) {
        String line = lines.get(lineNum);
        player.sendMessage(ChatColor.WHITE + "<" + ChatColor.RESET + npc.getFullName() + ChatColor.WHITE.toString() + "> " + ChatColor.translateAlternateColorCodes('&', line));
        playSpeakSound(player, talkSound, talkVolume, talkPitch);


        long delay = 0L;
        if (Dialogue.this.useWordCountForDelay) {
            String[] wordCount = line.split(" ");
            delay = wordCount.length * this.wordCountMult;
        } else {
            delay = Dialogue.this.delayBetweenLines * 20L;
        }
        if (lineNum == (this.lines.size() - 1) ) {
            Quest trait = npc.getTrait(Quest.class);
            if (this.postDialogueComponentText == null) {
                trait.setNotTalkingToPlayer(player);
                return;
            }
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    player.spigot().sendMessage(Dialogue.this.postDialogueComponentText);
                    trait.setNotTalkingToPlayer(player);
                    if (Dialogue.this.rewards != null && !Dialogue.this.rewards.isEmpty()) {
                        Dialogue.this.giveRewards(player);
                    }
                }
            }, delay * 1L);
            return;
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                speakUnrangedQuest(lineNum + 1, player);
            }
        }, delay* 1L);
    }



    private void playSpeakSound(Player player, String sound, float volume, float pitch) {
        player.playSound(this.npc.getEntity().getLocation(), sound, volume, pitch);
    }

    private void giveRewards(Player player) {
        this.playSpeakSound(player, "random.orb", 1.0F, 0.5F);
        this.playSpeakSound(player, "random.levelup", 1.0F, 1.25F);
        ChainCommand chain = new ChainCommand(Dialogue.this.rewards.toArray(new String[0]), player);
        chain.run();
    }


}
