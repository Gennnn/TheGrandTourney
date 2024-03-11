package me.genn.thegrandtourney.menu;

import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.player.MMOPlayer;
import me.genn.thegrandtourney.player.Objective;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class QuestLog implements Listener {
    TGT plugin;
    final String title = "Quest Log";
    final String back = ChatColor.GREEN + "Go Back";
    final String previousPage = ChatColor.GREEN + "Previous Page";
    final String nextPage = ChatColor.GREEN + "Next Page";
    final String close = ChatColor.RED + "Close";
    Map<UUID, Integer> pageForPlayer;
    Map<UUID, Boolean> showingCompleteForPlayer;
    int[] slotsToFill = {10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34,37,38,39,40,41,42,43};

    public QuestLog(TGT plugin) {
        this.plugin = plugin;
        this.pageForPlayer = new HashMap<>();
        this.showingCompleteForPlayer = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, this.plugin);
    }

    public void loadMenuQuestLog(Player player, int page, boolean completedQuests) {
        MMOPlayer mmoPlayer = plugin.players.get(player.getUniqueId());

        createPage(player, mmoPlayer, page, completedQuests);
    }

    public void createPage(Player player, MMOPlayer mmoPlayer, int page, boolean completedQuests) {
        String modTitle = title;
        int quests = mmoPlayer.objectives.size();
        if (quests > 28) {
            modTitle = modTitle + " (" + (page+1) + "/" + getMaxPages(mmoPlayer) +")";
        }
        Inventory inv = Bukkit.createInventory(player, 54, modTitle);
        ItemStack item = new ItemStack(Material.WRITABLE_BOOK);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "View and track your current quests,");
        lore.add(ChatColor.GRAY + "progress, and rewards.");
        setMenuItem(item, ChatColor.GREEN + "Quest Log", lore, 4, inv);
        item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        lore.clear();
        for (int i = 0; i < inv.getSize(); i++) {
            if (i==0) {
                setMenuItem(item, " ", lore,i,inv);
            } else if ((i % 9)==0 || ((i+1)%9)==0) {
                if (i==45 && getMaxPages(mmoPlayer) > 1 && page != 0) {
                    lore.clear();
                    lore.add(ChatColor.GRAY + "To Page " + page);
                    setMenuItem(new ItemStack(Material.ARROW), previousPage, lore, 45, inv);
                } else if (i==53 && getMaxPages(mmoPlayer) > 1 && page+1 == getMaxPages(mmoPlayer)) {
                    lore.clear();
                    lore.add(ChatColor.GRAY + "To Page " + page+2);
                    setMenuItem(new ItemStack(Material.ARROW), nextPage, lore, 53, inv);
                } else {
                    lore.clear();
                    setMenuItem(item, " ", lore, i, inv);
                }
            } else if (i < 9 && i != 4) {
                setMenuItem(item, " ", lore, i, inv);
            } else {
                if (i == 46 || i==47 || i == 51 || i == 52) {
                    lore.clear();
                    setMenuItem(item, " ", lore, i, inv);
                } else if (i==48) {
                    lore.clear();
                    lore.add(ChatColor.GRAY + "To Menu");
                    setMenuItem(new ItemStack(Material.ARROW), back, lore, i, inv);
                } else if (i==49) {
                    lore.clear();
                    setMenuItem(new ItemStack(Material.BARRIER), close, lore, i, inv);
                } else if (i==50) {
                    lore.clear();
                    if (!completedQuests) {
                        lore.add(ChatColor.AQUA + "▶ In Progress");
                        lore.add(ChatColor.GRAY + "  Completed");
                        lore.add("");
                        lore.add(ChatColor.YELLOW + "Click to toggle!");
                        setMenuItem(new ItemStack(Material.BOOK), ChatColor.GREEN + "Showing: In Progress Quests",lore,i,inv);
                    } else {
                        lore.add(ChatColor.GRAY + "  In Progress");
                        lore.add(ChatColor.AQUA + "▶ Completed");
                        lore.add("");
                        lore.add(ChatColor.YELLOW + "Click to toggle!");
                        setMenuItem(new ItemStack(Material.BOOK), ChatColor.GREEN + "Showing: Completed Quests",lore,i,inv);
                    }
                }
            }
        }
        int startIndex = (28*page);
        if (!completedQuests) {
            for (int i = 0; i < slotsToFill.length; i++) {
                if (i+startIndex >= mmoPlayer.objectives.size()) {
                    continue;
                } else {
                    Objective objective = mmoPlayer.objectives.get(i+startIndex);
                    setMenuItem(objective.icon, ChatColor.translateAlternateColorCodes('&', objective.questName), objective.status, slotsToFill[i], inv);
                    if (player.isOp()) {
                        player.sendMessage("Lore is supposed to be:");
                        for (int k =0 ; k < objective.status.size(); k++) {
                            player.sendMessage(objective.status.get(k));
                        }
                    }

                }

            }
        } else {
            for (int i = 0; i < slotsToFill.length; i++) {
                if (i+startIndex >= mmoPlayer.completedObjectives.size()) {
                    continue;
                } else {
                    Objective objective = mmoPlayer.completedObjectives.get(i+startIndex);
                    setMenuItem(objective.icon, ChatColor.translateAlternateColorCodes('&', objective.questName), objective.status, slotsToFill[i], inv);
                }

            }
        }
        player.openInventory(inv);
        this.pageForPlayer.put(player.getUniqueId(), page);
        this.showingCompleteForPlayer.put(player.getUniqueId(), completedQuests);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent e) {
        if (e.getWhoClicked().getOpenInventory().getTitle().startsWith(title)) {
            if (e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()){
                String name = e.getCurrentItem().getItemMeta().getDisplayName();
                if (name.equalsIgnoreCase(" ")) {
                    e.setCancelled(true);
                } else if (name.equalsIgnoreCase(back)) {
                    e.setCancelled(true);
                    plugin.menus.openHomeMenu(Bukkit.getPlayer(e.getWhoClicked().getUniqueId()));
                } else if (name.equalsIgnoreCase(close)) {
                    e.setCancelled(true);
                    e.getInventory().close();
                } else if (name.equalsIgnoreCase(nextPage)) {
                    this.loadMenuQuestLog(Bukkit.getPlayer(e.getWhoClicked().getUniqueId()), this.pageForPlayer.get(e.getWhoClicked().getUniqueId()) + 1, this.showingCompleteForPlayer.get(e.getWhoClicked().getUniqueId()));
                    e.setCancelled(true);
                } else if (name.equalsIgnoreCase(previousPage)) {
                    this.loadMenuQuestLog(Bukkit.getPlayer(e.getWhoClicked().getUniqueId()), this.pageForPlayer.get(e.getWhoClicked().getUniqueId()) -1, this.showingCompleteForPlayer.get(e.getWhoClicked().getUniqueId()));
                    e.setCancelled(true);
                } else if (ChatColor.stripColor(name).equalsIgnoreCase("Showing: In Progress Quests")) {
                    this.loadMenuQuestLog(Bukkit.getPlayer(e.getWhoClicked().getUniqueId()), 0, true);
                    e.setCancelled(true);
                } else if (ChatColor.stripColor(name).equalsIgnoreCase("Showing: Completed Quests")) {
                    this.loadMenuQuestLog(Bukkit.getPlayer(e.getWhoClicked().getUniqueId()), 0, false);
                    e.setCancelled(true);
                } else if (e.getCurrentItem().getType() == Material.PLAYER_HEAD && !this.showingCompleteForPlayer.get(e.getWhoClicked().getUniqueId())) {
                    ItemStack item = e.getCurrentItem();
                    Objective objective = plugin.players.get(e.getWhoClicked().getUniqueId()).objectives.stream().filter(obj -> ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', obj.questName)).equalsIgnoreCase(ChatColor.stripColor(item.getItemMeta().getDisplayName()))).findFirst().orElse(null);
                    if (objective != null) {
                        plugin.players.get(e.getWhoClicked().getUniqueId()).trackedObjective = objective;
                        e.getWhoClicked().sendMessage(ChatColor.GREEN + "Set tracked quest to: " + item.getItemMeta().getDisplayName() + ChatColor.GREEN + ".");
                    }
                    e.setCancelled(true);
                }
            } else if (e.getCursor() != null && e.getCursor().hasItemMeta() && e.getCursor().getItemMeta().hasDisplayName()) {
                String name = e.getCursor().getItemMeta().getDisplayName();
                if (name.equalsIgnoreCase(" ")) {
                    e.setCancelled(true);
                }
            }
        }
    }

    public void setMenuItem(ItemStack item, String displayName, List<String> lore, int slot, Inventory inv) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
        if (lore.size() > 0) {
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
        inv.setItem(slot,item);
    }

    public int getMaxPages(MMOPlayer mmoPlayer) {
        return (int)((mmoPlayer.objectives.size() / 28)+1);
    }
}
