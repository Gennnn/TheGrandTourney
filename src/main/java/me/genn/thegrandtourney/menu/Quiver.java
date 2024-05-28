package me.genn.thegrandtourney.menu;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.item.MMOItem;
import me.genn.thegrandtourney.player.MMOPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.checkerframework.checker.units.qual.N;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Quiver implements Listener {

    TGT plugin;
    final String title = "Quiver";
    final String back = ChatColor.GREEN + "Go Back";
    final String close = ChatColor.RED + "Close";
    public HashMap<UUID, Integer> usingQuiverIndex = new HashMap<>();

    public Quiver(TGT plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this,plugin);
    }
    public void loadQuiver(Player player) {
        MMOPlayer mmoPlayer = plugin.players.get(player.getUniqueId());
        this.createQuiverMenu(player, mmoPlayer);
    }
    public void createQuiverMenu(Player player, MMOPlayer mmoPlayer) {
        Inventory inv = Bukkit.createInventory(player, 36, title);
        setMenuItem(new ItemStack(Material.BARRIER), close, new ArrayList<>(), 31, inv);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "To Menu");
        setMenuItem(new ItemStack(Material.ARROW), back, lore, 30, inv);
        lore.clear();
        for (int i = 27; i < 36; i++) {
            if (inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR) {
                setMenuItem(new ItemStack(Material.GRAY_STAINED_GLASS_PANE), " ", lore, i, inv);
            }
        }
        ItemStack[] quiverContents = mmoPlayer.getQuiverContents();
        for (int i = 0; i < 27; i++) {
            if (quiverContents[i] != null || quiverContents[i].getType() != Material.AIR) {
                inv.setItem(i,quiverContents[i]);
            }
        }
        player.openInventory(inv);
    }

    @EventHandler
    public void onInvClose(InventoryCloseEvent e) {
        if (e.getView().getTitle().equalsIgnoreCase(title)) {
            Inventory inv = e.getView().getTopInventory();
            ItemStack[] updatedQuiver = new ItemStack[27];
            for (int i = 0; i < 27; i++) {
                if (inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR) {
                    updatedQuiver[i] = new ItemStack(Material.AIR);
                } else {
                    updatedQuiver[i] = inv.getItem(i);
                }
            }
            MMOPlayer mmoPlayer = plugin.players.get(e.getPlayer().getUniqueId());
            mmoPlayer.setQuiverContents(updatedQuiver);
        }
    }

    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        if (e.getWhoClicked().getOpenInventory().getTitle().equalsIgnoreCase(title)) {
            Player player = Bukkit.getPlayer(e.getWhoClicked().getUniqueId());
            if (e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName() && e.getCurrentItem().getItemMeta().hasLore()) {
                String name = e.getCurrentItem().getItemMeta().getDisplayName();
                if (name.equalsIgnoreCase(close)) {
                    player.closeInventory();
                    e.setCancelled(true);
                } else if (name.equalsIgnoreCase(back)) {
                    plugin.menus.openHomeMenu(player);
                    plugin.menus.playClickSound(player);
                    e.setCancelled(true);
                } else if (!(name.equalsIgnoreCase(" ")) && e.getRawSlot()+6 != e.getWhoClicked().getOpenInventory().countSlots()&& !name.equalsIgnoreCase(ChatColor.GREEN + "Quiver")) {
                    if (!isArrow(e.getCurrentItem())) {
                        e.setCancelled(true);
                        player.sendMessage(ChatColor.RED + "You can only place arrows in your Quiver!");
                        plugin.shopHandler.playNoMoneySound(player);
                    } else {
                        return;
                    }
                } else {
                    e.setCancelled(true);
                }
            } else if (e.getCursor() != null && e.getCursor().hasItemMeta() && e.getCursor().getItemMeta().hasDisplayName() && e.getCursor().getItemMeta().hasLore()) {
                String name = e.getCursor().getItemMeta().getDisplayName();
                if (name.equalsIgnoreCase(close)) {
                    player.closeInventory();
                    e.setCancelled(true);
                } else if (name.equalsIgnoreCase(back)) {
                    plugin.menus.openHomeMenu(player);
                    plugin.menus.playClickSound(player);
                    e.setCancelled(true);
                } else if (!(name.equalsIgnoreCase(" ")) && e.getRawSlot()+6 != e.getWhoClicked().getOpenInventory().countSlots() && !name.equalsIgnoreCase(ChatColor.GREEN + "Quiver")) {
                    if (!isArrow(e.getCursor())) {
                        e.setCancelled(true);
                        player.sendMessage(ChatColor.RED + "You can only place arrows in your Quiver!");
                        plugin.shopHandler.playNoMoneySound(player);
                    } else {
                        return;
                    }
                } else {
                    e.setCancelled(true);
                }
            }
        }
    }

    private boolean isArrow(ItemStack item) {
        if (item != null && item.hasItemMeta()) {
            NBTItem nbtItem = new NBTItem(item);
            if (nbtItem.hasTag("ExtraAttributes")) {
                NBTCompound comp = nbtItem.getCompound("ExtraAttributes");
                if (comp.hasTag("id")) {
                    String id = comp.getString("id");
                    MMOItem mmoItem = plugin.itemHandler.getMMOItemFromString(id);
                    return mmoItem.categoryString != null && (mmoItem.categoryString.equalsIgnoreCase("arrow") || mmoItem.categoryString.equalsIgnoreCase("bolt"));
                }
            }
        }
        return false;
    }

    private boolean isBow(ItemStack item) {
        if (item != null && item.hasItemMeta()) {
            NBTItem nbtItem = new NBTItem(item);
            if (nbtItem.hasTag("ExtraAttributes")) {
                NBTCompound comp = nbtItem.getCompound("ExtraAttributes");
                if (comp.hasTag("id")) {
                    String id = comp.getString("id");
                    MMOItem mmoItem = plugin.itemHandler.getMMOItemFromString(id);
                    return mmoItem.categoryString != null && (mmoItem.categoryString.equalsIgnoreCase("bow") || mmoItem.categoryString.equalsIgnoreCase("shortbow") || mmoItem.categoryString.equalsIgnoreCase("longbow") || mmoItem.categoryString.equalsIgnoreCase("greatbow"));
                }
            }
        }
        return false;
    }
    private boolean isShortBow(ItemStack item) {
        if (item != null && item.hasItemMeta()) {
            NBTItem nbtItem = new NBTItem(item);
            if (nbtItem.hasTag("ExtraAttributes")) {
                NBTCompound comp = nbtItem.getCompound("ExtraAttributes");
                if (comp.hasTag("id")) {
                    String id = comp.getString("id");
                    MMOItem mmoItem = plugin.itemHandler.getMMOItemFromString(id);
                    return mmoItem.categoryString != null && mmoItem.categoryString.equalsIgnoreCase("shortbow");
                }
            }
        }
        return false;
    }

    private boolean isPlayerMenu(ItemStack item) {
        if (item != null && item.hasItemMeta()) {
            NBTItem nbtItem = new NBTItem(item);
            if (nbtItem.hasTag("ExtraAttributes")) {
                NBTCompound comp = nbtItem.getCompound("ExtraAttributes");
                if (comp.hasTag("id")) {
                    String id = comp.getString("id");
                    return id.equalsIgnoreCase(plugin.menuItemName);
                }
            }
        }
        return false;
    }

    private boolean containsArrows(Inventory inventory) {
        for (ItemStack item : inventory) {
            if (isArrow(item)) {
                return true;
            }
        }
        return false;
    }
    private ItemStack getFirstArrowsInQuiver(Player player) {
        MMOPlayer mmoPlayer = plugin.players.get(player.getUniqueId());
        for (int i = 0; i < mmoPlayer.getQuiverContents().length; i++) {
            ItemStack item = mmoPlayer.getQuiverContents()[i];
            if (item != null && item.getType() != Material.AIR) {
                this.usingQuiverIndex.put(player.getUniqueId(), i);
                return item;
            }
        }
        return null;
    }

    private void setMenuItem(ItemStack item, String displayName, List<String> lore, int slot, Inventory inv) {
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

    @EventHandler
    public void onHotbarSel(PlayerItemHeldEvent e) {
        ItemStack item = e.getPlayer().getInventory().getItem(e.getNewSlot());
        if (isBow(item) && e.getPlayer().getInventory().getItem(8) != null && isPlayerMenu(e.getPlayer().getInventory().getItem(8))) {
            Player player = e.getPlayer();
            if (!containsArrows(player.getInventory())) {
                ItemStack arrow = getFirstArrowsInQuiver(player);
                if (arrow == null) {
                    return;
                }
                arrow = getFirstArrowsInQuiver(player).clone();
                ItemMeta meta = arrow.getItemMeta();
                MMOItem.addFakeEnchantment(meta);
                meta.setDisplayName(ChatColor.GRAY + ChatColor.stripColor(meta.getDisplayName()));
                List<String> lore = meta.getLore();
                lore.add(0," ");
                lore.add(0,ChatColor.DARK_GRAY + "Swap off a bow to see the Player Menu again.");
                lore.add(0," ");
                lore.add(0,ChatColor.DARK_GRAY + "and are drawing arrows from your Quiver.");
                lore.add(0,ChatColor.DARK_GRAY + "you currently have a bow selected");
                lore.add(0, ChatColor.DARK_GRAY + "You are seeing this item because");
                meta.setLore(lore);
                arrow.setItemMeta(meta);
                if (isShortBow(item)) {
                    arrow.setType(Material.FEATHER);
                }
                e.getPlayer().getInventory().setItem(8,arrow);
            }
        } else if (isBow(item) && e.getPlayer().getInventory().getItem(8) != null && isArrow(e.getPlayer().getInventory().getItem(8))) {
            if (e.getPlayer().getInventory().getItem(8).getType() == Material.ARROW && isShortBow(item)) {
                e.getPlayer().getInventory().getItem(8).setType(Material.FEATHER);
            } else if (e.getPlayer().getInventory().getItem(8).getType() == Material.FEATHER && !isShortBow(item)) {
                e.getPlayer().getInventory().getItem(8).setType(Material.ARROW);
            }
        } else {
            e.getPlayer().getInventory().setItem(8,plugin.itemHandler.getItemFromString(plugin.menuItemName));
            this.usingQuiverIndex.remove(e.getPlayer().getUniqueId());
        }
    }
    @EventHandler
    public void onBowFire(EntityShootBowEvent e) {
        if (e.getEntity() instanceof Player) {
            Player player = (Player)e.getEntity();
            MMOPlayer mmoPlayer = plugin.players.get(player.getUniqueId());
            if (this.usingQuiverIndex.containsKey(player.getUniqueId())){
                mmoPlayer.getQuiverContents()[this.usingQuiverIndex.get(player.getUniqueId())].setAmount(mmoPlayer.getQuiverContents()[this.usingQuiverIndex.get(player.getUniqueId())].getAmount()-1);
                if (mmoPlayer.getQuiverContents()[this.usingQuiverIndex.get(player.getUniqueId())].getAmount() < 1) {
                    mmoPlayer.getQuiverContents()[this.usingQuiverIndex.get(player.getUniqueId())] = new ItemStack(Material.AIR);
                    ItemStack arrow = getFirstArrowsInQuiver(player);
                    if (arrow == null) {
                        player.getInventory().setItem(8,plugin.itemHandler.getItemFromString(plugin.menuItemName));
                        this.usingQuiverIndex.remove(player.getUniqueId());
                        return;
                    }
                    arrow = getFirstArrowsInQuiver(player).clone();
                    ItemMeta meta = arrow.getItemMeta();
                    MMOItem.addFakeEnchantment(meta);
                    meta.setDisplayName(ChatColor.GRAY + ChatColor.stripColor(meta.getDisplayName()));
                    List<String> lore = meta.getLore();
                    lore.add(" ");
                    lore.add(ChatColor.DARK_GRAY + "Swap off a bow to see the Player Menu again.");
                    lore.add(" ");
                    lore.add(0,ChatColor.DARK_GRAY + "and are drawing arrows from your Quiver");
                    lore.add(0,ChatColor.DARK_GRAY + "you currently have a bow selected");
                    lore.add(0, ChatColor.DARK_GRAY + "You are seeing this item because");
                    meta.setLore(lore);
                    arrow.setItemMeta(meta);
                    if (isShortBow(arrow)) {
                        arrow.setType(Material.FEATHER);
                    }
                    player.getInventory().setItem(8,arrow);
                }
            } else if (e.getConsumable().getAmount()-1 <= 0) {
                e.setConsumeItem(false);
                e.getConsumable().setAmount(0);
                if (!containsArrows(player.getInventory())) {
                    ItemStack arrow = getFirstArrowsInQuiver(player);
                    if (arrow == null) {
                        player.getInventory().setItem(8, plugin.itemHandler.getItemFromString(plugin.menuItemName));
                        this.usingQuiverIndex.remove(player.getUniqueId());
                        return;
                    }
                    arrow = getFirstArrowsInQuiver(player).clone();
                    ItemMeta meta = arrow.getItemMeta();
                    MMOItem.addFakeEnchantment(meta);
                    meta.setDisplayName(ChatColor.GRAY + ChatColor.stripColor(meta.getDisplayName()));
                    List<String> lore = meta.getLore();
                    lore.add(" ");
                    lore.add(ChatColor.DARK_GRAY + "Swap off a bow to see the Player Menu again.");
                    lore.add(" ");
                    lore.add(0, ChatColor.DARK_GRAY + "and are drawing arrows from your Quiver");
                    lore.add(0, ChatColor.DARK_GRAY + "you currently have a bow selected");
                    lore.add(0, ChatColor.DARK_GRAY + "You are seeing this item because");
                    meta.setLore(lore);
                    arrow.setItemMeta(meta);
                    if (isShortBow(arrow)) {
                        arrow.setType(Material.FEATHER);
                    }
                    player.getInventory().setItem(8, arrow);
                }
            }
        }
    }
}
