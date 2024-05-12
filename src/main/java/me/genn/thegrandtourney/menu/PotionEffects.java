package me.genn.thegrandtourney.menu;

import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.player.MMOPlayer;
import me.genn.thegrandtourney.player.PotionEffect;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class PotionEffects implements Listener {
    TGT plugin;
    final String title = "Active Effects";
    final String back = ChatColor.GREEN + "Go Back";
    final String close = ChatColor.RED + "Close";
    final String sort = ChatColor.GREEN + "Sort";
    List<UUID> filterDuration = new ArrayList<>();
    int[] slotsToFill = {10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34,37,38,39,40,41,42,43};

    public PotionEffects(TGT plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, this.plugin);
        this.refreshTimes();
    }

    public void loadMenuPotionEffects(Player player) {
        MMOPlayer mmoPlayer = plugin.players.get(player.getUniqueId());
        createEffectsMenu(mmoPlayer, player);
    }

    public void createEffectsMenu(MMOPlayer mmoPlayer, Player player) {
        Inventory inv = Bukkit.createInventory(player, 54, title);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "View and manage your active");
        lore.add(ChatColor.GRAY + "potion effects.");
        setMenuItem(new ItemStack(Material.POTION), ChatColor.GREEN + "Active Effects", lore, 4, inv);
        lore.clear();
        setMenuItem(new ItemStack(Material.BARRIER),close,lore,49,inv);
        lore.add(ChatColor.GRAY + "To Menu");
        setMenuItem(new ItemStack(Material.ARROW),back,lore,48,inv);
        lore.clear();
        if (!filterDuration.contains(player.getUniqueId())) {
            lore.add(ChatColor.AQUA + "▶ Alphabetical");
            lore.add(ChatColor.GRAY + "  Duration");
            lore.add("");
            lore.add(ChatColor.YELLOW + "Click to change!");
        } else {
            lore.add(ChatColor.GRAY + "  Alphabetical");
            lore.add(ChatColor.AQUA + "▶ Duration");
            lore.add("");
            lore.add(ChatColor.YELLOW + "Click to change!");
        }
        setMenuItem(new ItemStack(Material.HOPPER),sort,lore,50,inv);
        lore.clear();
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        for (int i = 0; i < 54; i++) {
            if (inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR) {
                if (i < 9 || i > 44 || i % 9 == 0 || (i+1)%9==0) {
                    inv.setItem(i,item);
                }
            }
        }
        int c = 0;
        Iterator<PotionEffect> effects = mmoPlayer.potionEffects.iterator();
        while (effects.hasNext()) {
            PotionEffect effect = effects.next();
            ItemStack effectItem = effect.activatingItem;
            String remaining = ChatColor.GRAY + "Remaining: ";
            remaining = remaining + formatTime(effect.expiryTime);
            if (ChatColor.stripColor(effectItem.getItemMeta().getDisplayName()).equalsIgnoreCase("Unknown Source")) {
                List<String> effectLore = new ArrayList<>(effectItem.getLore());
                effectLore.add(effectLore.size()-2," ");
                effectLore.add(effectLore.size()-2, remaining);
                effectLore.add(effectLore.size()-2, " ");
                effectItem.setLore(effectLore);
            } else {
                List<String> effectLore = new ArrayList<>(effectItem.getLore());
                effectLore.add(effectLore.size()-1," ");
                effectLore.add(effectLore.size()-1, remaining);
                effectLore.add(effectLore.size()-1, " ");
                effectItem.setLore(effectLore);
            }
            inv.setItem(slotsToFill[c], effectItem);
            c++;
        }
        player.openInventory(inv);

    }

    private String formatTime(long time) {
        long remainingTime = time - System.currentTimeMillis();
        long mins = TimeUnit.MILLISECONDS.toMinutes(remainingTime);
        remainingTime -= TimeUnit.MINUTES.toMillis(mins);
        long secs = TimeUnit.MILLISECONDS.toSeconds(remainingTime);
        return String.format("%d:%02d", mins, secs);

    }
    public void refreshTimes() {
        new BukkitRunnable() {

            @Override
            public void run() {
                for(Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getOpenInventory().getTitle().equalsIgnoreCase(title)) {
                        Inventory displayInv = player.getOpenInventory().getTopInventory();
                        int c = 0;
                        MMOPlayer mmoPlayer = plugin.players.get(player.getUniqueId());
                        Iterator<PotionEffect> effects = mmoPlayer.potionEffects.iterator();
                        while (effects.hasNext()) {
                            PotionEffect effect = effects.next();
                            ItemStack effectItem = effect.activatingItem;
                            String remaining = ChatColor.GRAY + "Remaining: ";
                            remaining = remaining + formatTime(effect.expiryTime);
                            if (ChatColor.stripColor(effectItem.getItemMeta().getDisplayName()).equalsIgnoreCase("Unknown Source")) {
                                List<String> effectLore = new ArrayList<>(effectItem.getLore());
                                effectLore.set(effectLore.size()-4," ");
                                effectLore.set(effectLore.size()-3, remaining);
                                effectLore.set(effectLore.size()-2, " ");
                                effectItem.setLore(effectLore);
                            } else {
                                List<String> effectLore = new ArrayList<>(effectItem.getLore());
                                effectLore.set(effectLore.size()-3," ");
                                effectLore.set(effectLore.size()-2, remaining);
                                effectLore.set(effectLore.size()-1, " ");
                                effectItem.setLore(effectLore);
                            }
                            displayInv.setItem(slotsToFill[c], effectItem);
                            c++;
                        }
                    }
                }
            }
        }.runTaskTimer(plugin,0L,20L);
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
    public void onInvClick(InventoryClickEvent e) {
        if (e.getWhoClicked().getOpenInventory().getTitle().equalsIgnoreCase(title)) {
            e.setCancelled(true);
            if (e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()) {
                String name = e.getCurrentItem().getItemMeta().getDisplayName();
                if (name.equalsIgnoreCase(close)) {
                    e.getInventory().close();
                } else if (name.equalsIgnoreCase(back)) {
                    plugin.menus.openHomeMenu(Bukkit.getPlayer(e.getWhoClicked().getUniqueId()));
                } else if (name.equalsIgnoreCase(sort)) {
                    if (this.filterDuration.contains(e.getWhoClicked().getUniqueId())) {
                        this.filterDuration.remove(e.getWhoClicked().getUniqueId());
                        this.loadMenuPotionEffects(Bukkit.getPlayer(e.getWhoClicked().getUniqueId()));
                    } else {
                        this.filterDuration.add(e.getWhoClicked().getUniqueId());
                        this.loadMenuPotionEffects(Bukkit.getPlayer(e.getWhoClicked().getUniqueId()));
                    }
                }
            }
        }
    }


}
