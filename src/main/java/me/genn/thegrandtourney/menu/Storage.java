package me.genn.thegrandtourney.menu;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.player.MMOPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class Storage implements Listener {
    TGT plugin;
    final String title = "Storage";
    final String back = ChatColor.GREEN + "Go Back";
    final String previousPage = ChatColor.GREEN + "Previous Page";
    final String nextPage = ChatColor.GREEN + "Next Page";
    final String close = ChatColor.RED + "Close";
    Map<UUID, Integer> pageForPlayer;

    public Storage(TGT plugin) {
        this.plugin = plugin;
        this.pageForPlayer = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, this.plugin);
    }

    public void loadStorageMenu(Player player, int page) {

        MMOPlayer mmoPlayer = plugin.players.get(player.getUniqueId());
        int slots = mmoPlayer.getStorageSlots();
        int slotsForPage = slots - (45*page);
        if ((slotsForPage / 9.0f) <= 1.0f) {
            createBag(player, 18, mmoPlayer, page);
        } else if ((slotsForPage / 18.0f) <= 1.0f) {
            createBag(player, 27, mmoPlayer, page);
        } else if ((slotsForPage / 27.0f) <= 1.0f) {
            createBag(player, 36, mmoPlayer, page);
        } else if ((slotsForPage / 36.0f) <= 1.0f) {
            createBag(player, 45, mmoPlayer, page);
        } else  {
            createBag(player, 54, mmoPlayer, page);
        }
    }

    private void createBag(Player player, int size, MMOPlayer mmoPlayer, int page) {

        String modTitle = title;
        int slots = mmoPlayer.getStorageSlots() - (45*page);
        if (mmoPlayer.getStorageSlots() > 45) {
            modTitle = modTitle + " (" + (page+1) + "/" + getMaxPages(mmoPlayer) +")";
        }
        Inventory inv = Bukkit.createInventory(player, size, modTitle);
        int startIndex = (45*page);
        for (int i = startIndex; i < (startIndex + (size-9)); i++) {
            if (i < mmoPlayer.storageContents.size()) {
                ItemStack item =  mmoPlayer.storageContents.get(i);
                inv.setItem(i - (45*page), item);
            } else {
                ItemStack item = new ItemStack(Material.AIR);
                inv.setItem(i - (45*page), item);
            }

        }
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
        item.setItemMeta(meta);
        if (getMaxPages(mmoPlayer) > 1) {
            if (page == 0) {
                inv.setItem((size-9), item);
            } else {
                ItemStack prevPage = new ItemStack(Material.ARROW);
                ItemMeta prevMeta = prevPage.getItemMeta();
                prevMeta.setDisplayName(previousPage);
                prevMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                prevMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                prevMeta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "To Page " + (page));
                prevMeta.setLore(lore);
                prevPage.setItemMeta(prevMeta);
                inv.setItem((size-9), prevPage);
            }
        } else {
            inv.setItem((size-9), item);
        }
        inv.setItem((size-9)+1, item);
        inv.setItem((size-9)+2, item);
        inv.setItem((size-9)+5, item);
        inv.setItem((size-9)+6, item);
        inv.setItem((size-9)+7, item);
        if (getMaxPages(mmoPlayer) > 1) {
            if (page+1 == getMaxPages(mmoPlayer)) {
                inv.setItem((size-9)+8, item);
            } else {
                ItemStack nextPage = new ItemStack(Material.ARROW);
                ItemMeta nextMeta = nextPage.getItemMeta();
                nextMeta.setDisplayName(this.nextPage);
                nextMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                nextMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                nextMeta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "To Page " + (page+2));
                nextMeta.setLore(lore);
                nextPage.setItemMeta(nextMeta);
                inv.setItem((size-9)+8, nextPage);
            }
        } else {
            inv.setItem((size-9)+8, item);
        }

        for (int i = 0; i < inv.getSize(); i++) {
            if (i >= slots && i < (size-9)) {
                inv.setItem(i, item);
            }
        }
        item = new ItemStack(Material.ARROW);
        meta = item.getItemMeta();
        List<String> lore = new ArrayList<>();
        meta.setDisplayName(back);
        lore.add(ChatColor.GRAY + "To Menu");
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
        item.setItemMeta(meta);
        inv.setItem((size-9)+3, item);

        item = new ItemStack(Material.BARRIER);
        meta = item.getItemMeta();
        meta.setDisplayName(close);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
        item.setItemMeta(meta);
        inv.setItem((size-9)+4, item);
        player.openInventory(inv);
        this.pageForPlayer.put(player.getUniqueId(), page);
    }

    public int getMaxPages(MMOPlayer mmoPlayer) {
        return (int)((mmoPlayer.getStorageSlots() / 45)+1);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onItemMove(InventoryClickEvent e) {
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
                }  else if (name.equalsIgnoreCase(nextPage)) {
                    this.loadStorageMenu(Bukkit.getPlayer(e.getWhoClicked().getUniqueId()), this.pageForPlayer.get(e.getWhoClicked().getUniqueId()) + 1);
                } else if (name.equalsIgnoreCase(previousPage)) {
                    this.loadStorageMenu(Bukkit.getPlayer(e.getWhoClicked().getUniqueId()), this.pageForPlayer.get(e.getWhoClicked().getUniqueId()) - 1);
                }
            } else if (e.getCursor() != null && e.getCursor().hasItemMeta() && e.getCursor().getItemMeta().hasDisplayName()) {
                String name = e.getCursor().getItemMeta().getDisplayName();
                if (name.equalsIgnoreCase(" ")) {
                    e.setCancelled(true);
                }
            }
        }
    }
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInvClose(InventoryCloseEvent e) {
        if (e.getPlayer().getOpenInventory().getTitle().startsWith(title)) {
            MMOPlayer mmoPlayer = plugin.players.get(e.getPlayer().getUniqueId());
            Iterator iter = e.getPlayer().getOpenInventory().getTopInventory().iterator();

            int counter = (45 * this.pageForPlayer.get(e.getPlayer().getUniqueId()));
            while (iter.hasNext()) {
                ItemStack item = (ItemStack) iter.next();
                if (item != null) {
                    if (item.hasItemMeta() && item.getItemMeta().getDisplayName().equalsIgnoreCase(" ")) {
                        continue;
                    }
                    if (item.hasItemMeta() && (item.getItemMeta().getDisplayName().equalsIgnoreCase(previousPage)
                    || item.getItemMeta().getDisplayName().equalsIgnoreCase(nextPage)
                    || item.getItemMeta().getDisplayName().equalsIgnoreCase(back)
                    || item.getItemMeta().getDisplayName().equalsIgnoreCase(close))) {
                        continue;
                    }
                    if (item.getType() == Material.AIR) {
                        mmoPlayer.storageContents.put(counter, new ItemStack(Material.AIR));

                    } else {
                        mmoPlayer.storageContents.put(counter, item);
                    }
                    counter++;
                } else {
                    mmoPlayer.storageContents.put(counter, new ItemStack(Material.AIR));
                    counter++;
                }
            }

        }
    }
}
