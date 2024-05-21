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
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AccessoryBag implements Listener {
    TGT plugin;
    final String title = "Accessory Pouch";
    final String back = ChatColor.GREEN + "Go Back";
    final String previousPage = ChatColor.GREEN + "Previous Page";
    final String nextPage = ChatColor.GREEN + "Next Page";
    final String close = ChatColor.RED + "Close";

    public AccessoryBag(TGT plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, this.plugin);
    }

    public void loadAccessoryBag(Player player) {
        MMOPlayer mmoPlayer = plugin.players.get(player.getUniqueId());
        int slots = mmoPlayer.getAccessoryBagSlots();
            if ((slots / 9) <= 1) {
                createBag(player, 18, mmoPlayer);
            } else if ((slots / 18) <= 1) {
                createBag(player, 27, mmoPlayer);
            } else if ((slots / 27) <= 1) {
                createBag(player, 36, mmoPlayer);
            } else if ((slots / 36) <= 1) {
                createBag(player, 45, mmoPlayer);
            } else if ((slots / 45) <= 1) {
                createBag(player, 54, mmoPlayer);
            }
    }

    public void createBag(Player player, int size, MMOPlayer mmoPlayer) {
        int slots = mmoPlayer.getAccessoryBagSlots();
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
        item.setItemMeta(meta);
        Inventory inv = Bukkit.createInventory(player, size, title);
        for (int i = 0; i < inv.getSize(); i++) {
            if (i >= slots && i < (size-9)) {
                inv.setItem(i, item);
            }
        }

        inv.setItem(size-9,item);
        inv.setItem((size-9)+1,item);
        inv.setItem((size-9)+2,item);
        inv.setItem((size-9)+5,item);
        inv.setItem((size-9)+6,item);
        inv.setItem((size-9)+7,item);
        inv.setItem((size-9)+8,item);
        item = new ItemStack(Material.ARROW);
        meta = item.getItemMeta();
        meta.setDisplayName(back);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "To Menu");
        meta.setLore(lore);
        item.setItemMeta(meta);
        inv.setItem((size-9)+3, item);
        item = new ItemStack(Material.BARRIER);
        meta = item.getItemMeta();
        meta.setDisplayName(close);
        lore.clear();
        meta.setLore(lore);
        item.setItemMeta(meta);
        inv.setItem((size-9)+4,item);

        if (mmoPlayer.getAccessoryBagContents().size() > 0) {
            Iterator iter = mmoPlayer.getAccessoryBagContents().iterator();
            int counter = 0;
            while (iter.hasNext()) {
                ItemStack charm = (ItemStack) iter.next();
                inv.setItem(counter, charm);
                counter++;
            }
        }
        player.openInventory(inv);
    }
    @EventHandler
    public void onItemMove(InventoryClickEvent e) {
        if (e.getWhoClicked().getOpenInventory().getTitle().equalsIgnoreCase(title)) {
            Player player = Bukkit.getPlayer(e.getWhoClicked().getUniqueId());
            if (e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()){
                String name = e.getCurrentItem().getItemMeta().getDisplayName();

                if (name.equalsIgnoreCase(" ")) {
                    e.setCancelled(true);
                } else if (name.equalsIgnoreCase(back)) {
                    e.setCancelled(true);
                    plugin.menus.openHomeMenu(Bukkit.getPlayer(e.getWhoClicked().getUniqueId()));
                    plugin.menus.playClickSound(player);
                } else if (name.equalsIgnoreCase(close)) {
                    e.setCancelled(true);
                    e.getInventory().close();
                } else {
                    if (e.getCurrentItem().getItemMeta().hasLore()) {
                        if (!isAccessory(e.getCurrentItem())) {
                            if (!e.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.GREEN + "Accessory Pouch")) {
                                e.setCancelled(true);
                                if (!plugin.itemHandler.itemIsMMOItemOfName(e.getCurrentItem(), "tgt_menu")) {
                                    e.getWhoClicked().sendMessage(ChatColor.RED + "You can't place that item in the Accessory Pouch!");
                                    plugin.shopHandler.playNoMoneySound(player);
                                }
                            }
                        }
                    } else {
                        e.setCancelled(true);
                    }
                }
            } else if (e.getCursor() != null && e.getCursor().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()) {
                String name = e.getCursor().getItemMeta().getDisplayName();
                if (name.equalsIgnoreCase(" ")) {
                    e.setCancelled(true);
                } else {
                    if (e.getCursor().getItemMeta().hasLore()) {
                        if (!isAccessory(e.getCursor())) {
                            if (!e.getCursor().getItemMeta().getDisplayName().equalsIgnoreCase(ChatColor.GREEN + "Accessory Pouch")) {
                                e.setCancelled(true);
                                if (!plugin.itemHandler.itemIsMMOItemOfName(e.getCursor(), "tgt_menu")) {
                                    e.getWhoClicked().sendMessage(ChatColor.RED + "You can't place that item in the Accessory Pouch!");
                                    plugin.shopHandler.playNoMoneySound(player);
                                }

                            }
                        }
                    } else {
                        e.setCancelled(true);
                    }
                }
            }
        }
    }
    @EventHandler
    public void onInvClose(InventoryCloseEvent e) {
        if (e.getPlayer().getOpenInventory().getTitle().equalsIgnoreCase(title)) {
            MMOPlayer mmoPlayer = plugin.players.get(e.getPlayer().getUniqueId());
            Iterator iter = e.getPlayer().getOpenInventory().getTopInventory().iterator();
            List<ItemStack> charms = new ArrayList<>();
            while (iter.hasNext()) {
                ItemStack item = (ItemStack) iter.next();
                if (item != null) {
                    NBTItem nbtI = new NBTItem(item);
                    if (nbtI.hasTag("ExtraAttributes")) {
                        NBTCompound comp = nbtI.getCompound("ExtraAttributes");
                        if (comp.hasTag("accessory")) {
                            charms.add(item);
                        }
                    }
                }
            }
            mmoPlayer.setAccessoryBagContents(charms);
        }
    }

    private boolean isAccessory(ItemStack item) {
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore() && item.getItemMeta().hasDisplayName()) {
            NBTItem nbtI = new NBTItem(item);
            if (nbtI.hasTag("ExtraAttributes")) {
                NBTCompound comp = nbtI.getCompound("ExtraAttributes");
                return comp.hasTag("accessory");
            }
        }
        return false;
    }
}
