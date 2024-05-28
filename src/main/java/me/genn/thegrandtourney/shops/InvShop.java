//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package me.genn.thegrandtourney.shops;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.Spell.SpellCastState;

import java.lang.reflect.Array;
import java.util.*;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.item.MMOItem;
import me.genn.thegrandtourney.player.MMOPlayer;
import me.genn.thegrandtourney.player.Objective;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class InvShop implements Listener {
    final String shopWindowPrefix = "Shop: ";
    final String confirmationWindowTitle = "Confirm Purchase: ";
    final String quantityWindowTitle = "Select Quantity: ";
    final String confirm = ChatColor.GREEN + "Confirm";
    final String cancel = ChatColor.RED + "Cancel";
    TGT plugin;
    Map<String, Shop> shopsOpen;
    Map<String, ShopItem> confirmations;
    Map<String, ShopItem> quantities;
    Map<UUID, HashMap<Integer, List<String>>> originalLores;
    final String close = ChatColor.RED + "Close";
    final String sell = ChatColor.GREEN + "Sell Item";
    final String back = ChatColor.GREEN + "Go Back";
    int[] slotsToFill = {10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34,37,38,39,40,41,42,43};

    public InvShop(TGT plugin) {
        this.plugin = plugin;
        this.shopsOpen = new HashMap<>();
        this.confirmations = new HashMap<>();
        this.quantities = new HashMap<>();
        this.originalLores = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openShop(Player player, Shop shop) {
        if (shop.items.size() != 0) {
            new BukkitRunnable() {

                @Override
                public void run() {
                    HashMap<Integer, List<String>> lores = new HashMap<>();
                    for (int i = 0; i < player.getInventory().getSize(); i++) {
                        ItemStack item = player.getInventory().getItem(i);
                        if (item != null && item.getType() != Material.AIR && item.hasItemMeta() && item.getItemMeta().hasLore()) {
                            NBTItem nbtI = new NBTItem(item);
                            if (nbtI.hasTag("ExtraAttributes")) {
                                NBTCompound comp = nbtI.getCompound("ExtraAttributes");
                                if (comp.hasTag("id")) {
                                    String mmoId = comp.getString("id");
                                    if (mmoId.equalsIgnoreCase(plugin.menuItemName)) {
                                        continue;
                                    }
                                    MMOItem mmoItem = plugin.itemHandler.getMMOItemFromString(mmoId);
                                    List<String> lore = item.getItemMeta().getLore();
                                    lores.put(i,lore);
                                    List<String> sellLore = new ArrayList<>(lore);
                                    sellLore.add(" ");
                                    sellLore.add(ChatColor.GRAY + "Sell Price:");
                                    float price = mmoItem.sellPrice * item.getAmount();
                                    if (comp.hasTag("statBoost")) {
                                        price = price * comp.getFloat("statBoost");
                                    }
                                    sellLore.add(ChatColor.GOLD + String.format("%,.1f", price) + " Dosh");
                                    sellLore.add(" ");
                                    sellLore.add(ChatColor.YELLOW + "Click to sell!");
                                    item.setLore(sellLore);
                                }
                            }
                        } else {
                            lores.put(i, new ArrayList<>());
                        }
                    }
                    InvShop.this.originalLores.put(player.getUniqueId(), lores);
                }
            }.runTaskLater(plugin, 1L);

            this.shopsOpen.put(player.getName(), shop);
            new BukkitRunnable() {
                @Override
                public void run() {
                    InvShop.this.shopsOpen.put(player.getName(), shop);
                }
            }.runTaskLater(plugin, 1L);
            Inventory inv = Bukkit.createInventory(player, 54, shopWindowPrefix + shop.name);
            ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            for (int i = 0; i < inv.getSize(); i++) {
                if (i < 9) {
                    setBorderItem(item, " ", i, inv);
                } else if (i % 9 == 0 || (i+1) % 9 == 0) {
                    setBorderItem(item, " ", i, inv);
                } else if (i > 44 && i != 49) {
                    setBorderItem(item, " ", i, inv);
                }
            }
            if (plugin.players.get(player.getUniqueId()).soldItems.size() == 0) {
                item = new ItemStack(Material.HOPPER);

                List<String> sellLore = new ArrayList<>();
                sellLore.add(ChatColor.GRAY + "Click items in your inventory to");
                sellLore.add(ChatColor.GRAY + "sell them to this shop!");
                item.setLore(sellLore);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(sell);
                item.setItemMeta(meta);
                inv.setItem(49, item);
            } else {
                item = plugin.players.get(player.getUniqueId()).soldItems.get(0);
                List<String> lore = item.getLore();
                lore.set(lore.size()-1, ChatColor.YELLOW + "Click to buyback!");
                lore.set(lore.size()-4, ChatColor.GRAY + "Cost:");
                item.setLore(lore);
                inv.setItem(49,item);
            }
            for (int i = 0; i < slotsToFill.length; i++) {
                if (i < shop.items.size()) {

                    item = shop.items.get(i).getShopItem();
                    ItemMeta meta = item.getItemMeta();
                    List<String> lore = meta.getLore();
                    lore.add(" ");
                    lore.add(ChatColor.YELLOW + "Click to trade!");
                    if (shop.items.get(i).quantifiable) {
                        lore.add(ChatColor.YELLOW + "Right-click for quantity options!");
                    }
                    meta.setLore(lore);
                    item.setLore(lore);
                    inv.setItem(slotsToFill[i], item);
                }

            }
            player.openInventory(inv);
        }
    }
    private void setBorderItem(ItemStack item, String displayName, int slot, Inventory inv) {
        if (inv.getItem(slot) != null && inv.getItem(slot).getType() != Material.AIR) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
        item.setItemMeta(meta);
        inv.setItem(slot,item);
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        final Player player;
        if (event.getWhoClicked().getOpenInventory().getTitle().startsWith("Shop: ")) {
            event.setCancelled(true);
            player = (Player)event.getWhoClicked();
            boolean close = true;
            final Shop shop = (Shop)this.shopsOpen.get(player.getName());

            if (shop != null && event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR && slotArrayList().contains(event.getRawSlot()) && shop.items.size() > slotArrayList().indexOf(event.getRawSlot())) {
                ShopItem item = shop.items.get(slotArrayList().indexOf(event.getRawSlot()));
                if (item.confirm) {
                    this.confirmations.put(player.getName(), item);
                    InvShop.this.getConfirmation(player);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                        public void run() {


                            InvShop.this.shopsOpen.put(player.getName(), shop);
                        }
                    }, 1L);
                } else if (item.quantifiable && event.getClick().isRightClick()) {
                    this.quantities.put(player.getName(), item);
                    InvShop.this.getQuantities(player);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                        public void run() {


                            InvShop.this.shopsOpen.put(player.getName(), shop);
                        }
                    }, 1L);
                } else {
                    this.purchaseItem(shop, player, item, event.getCurrentItem().getAmount());
                    if (!item.closeAfter) {
                        close = false;
                    }
                    if (close) {
                        this.shopsOpen.remove(player.getName());
                        Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                            public void run() {
                                player.closeInventory();
                            }
                        }, 1L);
                    }
                }
            } else if (shop != null && event.getCurrentItem() != null && event.getCurrentItem().hasItemMeta() && event.getCurrentItem().getItemMeta().hasDisplayName() && event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(this.close)) {
                this.shopsOpen.remove(player.getName());
                Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                    public void run() {
                        player.closeInventory();
                    }
                }, 1L);
            } else if (shop != null && event.getCurrentItem() != null && event.getRawSlot() == 49 && event.getCurrentItem().hasItemMeta() && !event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(sell)) {
                if (buyBackItem(player, event.getCurrentItem())) {
                    player.getInventory().addItem(plugin.players.get(player.getUniqueId()).soldItems.get(0));
                    plugin.players.get(player.getUniqueId()).soldItems.remove(0);
                    this.removeSellPrices(player);
                    new BukkitRunnable() {

                        @Override
                        public void run() {
                            openShop(player, InvShop.this.shopsOpen.get(player.getName()));
                        }
                    }.runTaskLater(plugin, 1L);
                }
            } else if (shop != null && event.getWhoClicked().getOpenInventory().getBottomInventory().contains(event.getCurrentItem())) {
                ItemStack item = event.getCurrentItem();
                if (item != null && item.getType() != Material.AIR && item.hasItemMeta() && item.getItemMeta().hasLore()) {
                    NBTItem nbtI = new NBTItem(item);
                    if (nbtI.hasTag("ExtraAttributes")) {
                        NBTCompound comp = nbtI.getCompound("ExtraAttributes");
                        if (comp.hasTag("id")) {
                            String mmoId = comp.getString("id");
                            if (mmoId.equalsIgnoreCase(plugin.menuItemName)) {
                                return;
                            }
                            MMOItem mmoItem = plugin.itemHandler.getMMOItemFromString(mmoId);
                            float price = mmoItem.sellPrice * item.getAmount();
                            if (comp.hasTag("statBoost")) {
                                price = price * comp.getFloat("statBoost");
                            }

                            player.sendMessage(ChatColor.GREEN + "You sold " + item.getItemMeta().getDisplayName() + " " + ChatColor.DARK_GRAY + "(" + item.getAmount() + ")" + ChatColor.GREEN + " for " + ChatColor.GOLD + String.format("%,.1f", price) + " Dosh" + ChatColor.GREEN + "!");
                            player.playSound(player, plugin.shopHandler.soundSale, plugin.shopHandler.buyVolume, plugin.shopHandler.buyPitch);
                            plugin.players.get(player.getUniqueId()).soldItems.add(0, item);
                            plugin.players.get(player.getUniqueId()).addPurseGold(price);
                            event.getClickedInventory().setItem(event.getSlot(), new ItemStack(Material.AIR));
                            this.removeSellPrices(player);
                            new BukkitRunnable() {

                                @Override
                                public void run() {
                                    openShop(player, InvShop.this.shopsOpen.get(player.getName()));
                                }
                            }.runTaskLater(plugin, 1L);

                        }
                    }


                }
            }

        } else if (event.getWhoClicked().getOpenInventory().getTitle().startsWith(confirmationWindowTitle)) {
            event.setCancelled(true);
            boolean close = true;
            player = (Player)event.getWhoClicked();
            Shop shop = (Shop)this.shopsOpen.get(player.getName());
            ShopItem item = (ShopItem)this.confirmations.get(player.getName());
            if (shop != null && item != null && event.getCurrentItem() != null && event.getCurrentItem().hasItemMeta() && event.getCurrentItem().getItemMeta().hasDisplayName() && event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(confirm)) {
                this.purchaseItem(shop, player, item, event.getCurrentItem().getAmount());
                if (!item.closeAfter) {
                    close = false;
                }
                if (close) {
                    this.shopsOpen.remove(player.getName());
                    this.confirmations.remove(player.getName());
                    Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                        public void run() {
                            player.closeInventory();
                        }
                    }, 1L);
                } else {
                    this.confirmations.remove(player.getName());
                    Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                        public void run() {
                            InvShop.this.openShop(player, InvShop.this.shopsOpen.get(player.getName()));
                        }
                    }, 1L);
                }
            } else if (shop != null && item != null && event.getCurrentItem() != null && event.getCurrentItem().hasItemMeta() && event.getCurrentItem().getItemMeta().hasDisplayName() && event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(cancel)) {
                this.confirmations.remove(player.getName());
                Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                    public void run() {
                        InvShop.this.openShop(player, InvShop.this.shopsOpen.get(player.getName()));
                    }
                }, 1L);
            }
        } else if (event.getWhoClicked().getOpenInventory().getTitle().startsWith(quantityWindowTitle)) {
            event.setCancelled(true);
            player = (Player)event.getWhoClicked();
            Shop shop = (Shop)this.shopsOpen.get(player.getName());
            ShopItem item = (ShopItem)this.quantities.get(player.getName());
            if (shop != null && item != null && event.getCurrentItem() != null && event.getCurrentItem().hasItemMeta() && event.getCurrentItem().getItemMeta().hasDisplayName() && event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(back)) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                    public void run() {
                        InvShop.this.quantities.remove(player.getName());
                        InvShop.this.openShop(player, InvShop.this.shopsOpen.get(player.getName()));
                    }
                }, 1L);

            } else if (shop != null && item != null && event.getCurrentItem() != null && event.getCurrentItem().hasItemMeta() && event.getCurrentItem().getItemMeta().hasDisplayName() && event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(close)) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                    public void run() {
                        InvShop.this.quantities.remove(player.getName());
                        player.closeInventory();
                    }
                }, 1L);
            }else if (shop != null && item != null && event.getCurrentItem() != null && event.getCurrentItem().hasItemMeta() && event.getCurrentItem().getItemMeta().hasDisplayName() && !event.getCurrentItem().getItemMeta().getDisplayName().equalsIgnoreCase(" ") && event.getWhoClicked().getOpenInventory().getTopInventory().contains(event.getCurrentItem()) && event.getRawSlot() != 49) {
                this.purchaseItem(shop, player, item, event.getCurrentItem().getAmount());
                boolean close = item.closeAfter;
                if (close) {
                    this.shopsOpen.remove(player.getName());
                    this.quantities.remove(player.getName());
                    Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                        public void run() {
                            player.closeInventory();
                        }
                    }, 1L);
                }
            }
        }

    }

    private List<Integer> slotArrayList() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < slotsToFill.length; i++) {
            list.add(slotsToFill.clone()[i]);
        }
        return list;
    }

    boolean buyBackItem(Player player, ItemStack item) {
        MMOPlayer mmoPlayer = plugin.players.get(player.getUniqueId());
        if (item != null && item.getType() != Material.AIR && item.hasItemMeta() && item.getItemMeta().hasLore()) {
            NBTItem nbtI = new NBTItem(item);
            if (nbtI.hasTag("ExtraAttributes")) {
                NBTCompound comp = nbtI.getCompound("ExtraAttributes");
                if (comp.hasTag("id")) {
                    String mmoId = comp.getString("id");
                    MMOItem mmoItem = plugin.itemHandler.getMMOItemFromString(mmoId);
                    float price = mmoItem.sellPrice * item.getAmount();
                    if (comp.hasTag("statBoost")) {
                        price = price * comp.getFloat("statBoost");
                    }
                    if (mmoPlayer.getPurseGold() >= price) {
                        player.playSound(player, plugin.shopHandler.soundSale, plugin.shopHandler.buyVolume, plugin.shopHandler.buyPitch);
                        player.sendMessage(ChatColor.GREEN + "You bought back " + item.getItemMeta().getDisplayName() + ChatColor.DARK_GRAY + " (" + item.getAmount() + ") " + ChatColor.GREEN + " for " + ChatColor.GOLD + String.format("%,.1f", price) + " Dosh" + ChatColor.GREEN + ".");
                        mmoPlayer.removePurseGold(price);
                        return true;
                    } else {
                        player.sendMessage(ChatColor.RED + "You don't have enough Dosh!");
                        player.playSound(player, plugin.shopHandler.soundNoMoney, plugin.shopHandler.noMoneyVolume, plugin.shopHandler.noMoneyPitch);
                        return false;
                    }
                }
            }


        }
        return false;
    }

    void getConfirmation(Player player) {
        Shop shop = (Shop)this.shopsOpen.get(player.getName());
        ShopItem item = (ShopItem)this.confirmations.get(player.getName());
        if (shop != null && item != null) {
            Inventory inv = Bukkit.createInventory(player, 27, confirmationWindowTitle + ChatColor.stripColor(item.item.displayName));
            ItemStack borderItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            for (int i = 0; i < 27; i++) {
                setBorderItem(borderItem, " ", i, inv);
            }
            List<String> lore = new ArrayList<>();
            inv.setItem(4, item.getShopItem());
            ItemStack cancelButton = new ItemStack(Material.RED_WOOL);
            ItemMeta meta = cancelButton.getItemMeta();
            meta.setDisplayName(cancel);
            lore.add(ChatColor.YELLOW + "Go Back");
            lore.add(ChatColor.YELLOW + "To " + this.shopsOpen.get(player.getName()).name);
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
            cancelButton.setItemMeta(meta);
            inv.setItem(15, cancelButton);
            ItemStack confirmButton = new ItemStack(Material.GREEN_WOOL);
            meta = confirmButton.getItemMeta();
            meta.setDisplayName(confirm);
            lore.clear();
            lore.add(ChatColor.YELLOW + "Purchase " + ChatColor.stripColor(item.item.displayName));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
            confirmButton.setItemMeta(meta);
            inv.setItem(11, confirmButton);
            player.openInventory(inv);
        } else {
            this.shopsOpen.remove(player.getName());
            this.confirmations.remove(player.getName());
        }
    }

    void getQuantities(Player player) {
        Shop shop = (Shop)this.shopsOpen.get(player.getName());
        ShopItem item = (ShopItem)this.quantities.get(player.getName());
        if (shop != null && item != null) {
            Inventory inv = Bukkit.createInventory(player, 45, quantityWindowTitle + ChatColor.stripColor(item.item.displayName));
            ItemStack borderItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            for (int i = 0; i < 45; i++) {
                if (i != 40 && i != 39 && !(i>=20&&i<=24)) {
                    setBorderItem(borderItem, " ", i, inv);
                } else if (i == 40) {
                    ItemStack closeItem = new ItemStack(Material.BARRIER);
                    ItemMeta meta = closeItem.getItemMeta();
                    meta.setDisplayName(close);
                    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                    meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
                    closeItem.setItemMeta(meta);
                    inv.setItem(i, closeItem);
                } else if (i == 39) {
                    ItemStack backItem = new ItemStack(Material.ARROW);
                    ItemMeta meta = backItem.getItemMeta();
                    meta.setDisplayName(back);
                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.YELLOW + "To " + this.shopsOpen.get(player.getName()).name);
                    meta.setLore(lore);
                    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                    meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
                    backItem.setItemMeta(meta);
                    inv.setItem(i, backItem);
                }
            }
            inv.setItem(20, item.getShopItem(1));
            inv.setItem(21, item.getShopItem(5));
            inv.setItem(22, item.getShopItem(10));
            inv.setItem(23, item.getShopItem(32));
            inv.setItem(24, item.getShopItem(64));
            player.openInventory(inv);
        } else {
            this.shopsOpen.remove(player.getName());
            this.confirmations.remove(player.getName());
        }
    }

    boolean purchaseItem(Shop shop, final Player player, ShopItem item, int quantity) {
        if (this.ifCanAffordRemoveCost(player, item, quantity)) {
            if (item.giveItem) {
                ItemStack i = plugin.itemHandler.getItem(item.item).asQuantity(quantity);
                player.getInventory().addItem(i);
                if (!shop.soundSale.isEmpty()) {
                    player.playSound(player.getLocation(), shop.soundSale, shop.buyVolume, shop.buyPitch);
                }
                return true;
            } else {
                final Spell spell = MagicSpells.getSpellByInternalName(item.spell);
                if (spell != null) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                        public void run() {
                            spell.castSpell(player, SpellCastState.NORMAL, 1.0F, (String[])null);
                        }
                    }, 5L);
                    if (!shop.soundSale.isEmpty()) {
                        player.playSound(player.getLocation(), shop.soundSale, shop.buyVolume, shop.buyPitch);
                    }

                    return true;
                } else {
                    this.plugin.getLogger().severe("INVALID SPELL: " + item.spell + " ON SHOP " + shop.name);
                    return false;
                }
            }
        } else {
            player.sendMessage(ChatColor.RED + "You cannot afford that item!");
            if (!shop.soundNoMoney.isEmpty()) {
                player.playSound(player.getLocation(), shop.soundNoMoney, shop.noMoneyVolume, shop.noMoneyPitch);
            }

            return false;
        }
    }

    boolean ifCanAffordRemoveCost(Player player, ShopItem item, int quantity) {
        if (item.costItems.size() < 1) {
            if (this.plugin.players.get(player.getUniqueId()).getPurseGold() >= (item.cost * quantity)) {
                this.plugin.players.get(player.getUniqueId()).removePurseGold(item.cost*quantity);
                return true;
            }
        } else {
            boolean hasDosh = false;
            if (item.cost > 0) {
                if (this.plugin.players.get(player.getUniqueId()).getPurseGold() >= (item.cost * quantity)) {
                    hasDosh = true;
                }
            } else {
                hasDosh = true;
            }
            for (MMOItem costItem : item.costItems.keySet()) {
                if (getAmount(player, costItem) < (item.costItems.get(costItem) * quantity)) {
                    return false;
                }
            }

            if (hasDosh) {
                for (MMOItem costItem : item.costItems.keySet()) {
                    MMOItem.removeItem(player, costItem, (item.costItems.get(costItem)*quantity));
                }
                this.plugin.players.get(player.getUniqueId()).removePurseGold(item.cost*quantity);
                return true;
            }
        }

        return false;
    }

    int getAmount(Player player, MMOItem mmoItem)
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
                        if (mmoId.equalsIgnoreCase(mmoItem.internalName)) {
                            has += item.getAmount();
                        }
                    }
                }

            }
        }
        return has;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (this.originalLores.containsKey(event.getPlayer().getUniqueId())) {
            this.removeSellPrices(Bukkit.getPlayer(event.getPlayer().getUniqueId()));
        }
        this.shopsOpen.remove(event.getPlayer().getName());
        if (event.getView().getTitle().startsWith(quantityWindowTitle)) {
            this.quantities.remove(event.getPlayer().getName());
        } else if (event.getView().getTitle().startsWith(confirmationWindowTitle)) {
            this.confirmations.remove(event.getPlayer().getName());
        }
    }

    public void removeSellPrices(Player player) {
        for (int i = 0; i < player.getOpenInventory().getBottomInventory().getSize(); i++) {
            ItemStack item = player.getOpenInventory().getBottomInventory().getItem(i);
            if (item != null && item.getType() != Material.AIR && item.hasItemMeta() && item.getItemMeta().hasLore()) {
            /*if (item != null && item.getType() != Material.AIR && item.hasItemMeta() && item.getItemMeta().hasLore()) {
                ItemMeta meta = item.getItemMeta();
                meta.setLore(this.originalLores.get(player.getUniqueId()).get(i));
                item.setItemMeta(meta);
            } else {
                continue;
            }*/
                List<String> lore = item.getLore();
                for (int k = 0; k < lore.size(); k++) {
                    String line = ChatColor.stripColor(lore.get(k));
                    if (line.equalsIgnoreCase("Sell Price:") || line.equalsIgnoreCase("Cost:")) {
                        k = k - 1;
                        int c = 0;
                        while (c < 5) {
                            lore.remove(k);
                            c++;
                        }
                        break;
                    }
                }
                item.setLore(lore);
            }
        }
        this.originalLores.remove(player.getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (shopsOpen.containsKey(event.getPlayer().getName()) && !this.quantities.containsKey(event.getPlayer().getName()) && !this.confirmations.containsKey(event.getPlayer().getName())) {
            removeSellPrices(event.getPlayer());
        }
        this.shopsOpen.remove(event.getPlayer().getName());
        this.quantities.remove(event.getPlayer().getName());
        this.confirmations.remove(event.getPlayer().getName());

    }
}
