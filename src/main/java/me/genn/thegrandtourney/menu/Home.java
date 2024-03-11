package me.genn.thegrandtourney.menu;

import com.nisovin.magicspells.MagicSpells;
import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.item.MMOItem;
import me.genn.thegrandtourney.player.MMOPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Home implements Listener {
    final String title = "Menu";
    final String recipes = ChatColor.GREEN + "Recipe Book";
    final String skills = ChatColor.GREEN + "Skill Progress";
    final String quests = ChatColor.GREEN + "Quest Log";
    final String accessories = ChatColor.GREEN + "Accessory Bag";
    final String storage = ChatColor.GREEN + "Storage";
    final String close = ChatColor.RED + "Close";
    TGT plugin;

    public Home(TGT plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void loadMenuHome(final Player player) {
        Inventory inv = Bukkit.createInventory(player, 54, title);
        MMOPlayer mmoPlayer = plugin.players.get(player.getUniqueId());
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
        meta.setDisplayName(ChatColor.YELLOW + "Your Player Stats");
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("  " + ChatColor.RED + "❤ Health " + ChatColor.WHITE + mmoPlayer.getMaxHealth());
        lore.add("  " + ChatColor.BLUE + "❈ Defense " + ChatColor.WHITE + mmoPlayer.getDefense());
        lore.add("  " + ChatColor.WHITE + "✦ Speed " + mmoPlayer.getSpeed());
        lore.add("  " + ChatColor.RED + "❁ Strength " + ChatColor.WHITE + mmoPlayer.getDamage());
        lore.add("  " + ChatColor.GREEN + "⚡ Vigor " + ChatColor.WHITE + (MagicSpells.getManaHandler().getMaxMana(player) - mmoPlayer.getBaseMaxMana()));
        lore.add("  " + ChatColor.DARK_GREEN + "⌛ Vigor Regen " + ChatColor.WHITE + (MagicSpells.getManaHandler().getRegenAmount(player)));
        lore.add("  " + ChatColor.AQUA + "☠ Crit Damage " + ChatColor.WHITE + mmoPlayer.getCritDamage());
        lore.add("  " + ChatColor.AQUA + "☣ Crit Chance " + ChatColor.WHITE + mmoPlayer.getCritChance());
        lore.add("  " + ChatColor.RED + "๑ Ability Damage " + ChatColor.WHITE + mmoPlayer.getAbilityDamage());
        lore.add("  " + ChatColor.GOLD + "☘ Mining Fortune " + ChatColor.WHITE + mmoPlayer.getMiningFortune());
        lore.add("  " + ChatColor.GOLD + "☘ Farming Fortune " + ChatColor.WHITE + mmoPlayer.getFarmingFortune());
        lore.add("  " + ChatColor.GOLD + "☘ Logging Fortune " + ChatColor.WHITE + mmoPlayer.getLoggingFortune());
        lore.add("  " + ChatColor.DARK_AQUA + "☂ Fishing Speed " + ChatColor.WHITE + mmoPlayer.getLure());
        lore.add("  " + ChatColor.AQUA + "☈ Lure " + ChatColor.WHITE + mmoPlayer.getFlash());
        lore.add("  " + ChatColor.LIGHT_PURPLE + "⌚ Dialogue Speed " +ChatColor.WHITE+ mmoPlayer.getDialogueSpeed());
        lore.add("  " + ChatColor.GOLD + "$ Shop Discount " + ChatColor.WHITE + mmoPlayer.getVendorPrice());
        meta.setLore(lore);
        SkullMeta skullMeta = (SkullMeta) meta;
        skullMeta.setPlayerProfile(player.getPlayerProfile());
        item.setItemMeta(skullMeta);
        inv.setItem(13, item);
        item = new ItemStack(Material.BOOK);
        meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
        meta.setDisplayName(recipes);
        lore.clear();
        lore.add("");
        lore.add(ChatColor.GRAY + "View the recipes you've unlocked throughout");
        lore.add(ChatColor.GRAY + "your adventures.");
        lore.add("");
        lore.add(ChatColor.YELLOW + "Click to open!");
        meta.setLore(lore);
        item.setItemMeta(meta);
        inv.setItem(29, item);
        item = new ItemStack(Material.EXPERIENCE_BOTTLE);
        meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
        meta.setDisplayName(skills);
        lore.clear();
        lore.add("");
        lore.add(ChatColor.GRAY + "View your skill progression and");
        lore.add(ChatColor.GRAY + "rewards.");
        lore.add("");
        lore.add(ChatColor.YELLOW + "Click to open!");
        meta.setLore(lore);
        item.setItemMeta(meta);
        inv.setItem(30, item);
        item = new ItemStack(Material.WRITABLE_BOOK);
        meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
        meta.setDisplayName(quests);
        lore.clear();
        lore.add("");
        lore.add(ChatColor.GRAY + "View and track your current quests,");
        lore.add(ChatColor.GRAY + "progress, and rewards.");
        lore.add("");
        lore.add(ChatColor.YELLOW + "Click to open!");
        meta.setLore(lore);
        item.setItemMeta(meta);
        inv.setItem(31, item);
        item = new ItemStack(Material.PLAYER_HEAD);
        MMOItem.getHeadFrom64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTVhOTI1MjFlZDc3OTA0ZWI0NTAwMDUwYjc1NmVkY2NlNmE2MjA3YmFjNGVhMWM2ZjhiMzViNWY4NmE5YWJhNSJ9fX0=", item);
        meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
        meta.setDisplayName(accessories);
        lore.clear();
        lore.add("");
        lore.add(ChatColor.GRAY + "A special bag that can hold your charms.");
        lore.add(ChatColor.GRAY + "Charms will only function while in your inventory");
        lore.add(ChatColor.GRAY + "or while in this bag.");
        lore.add("");
        lore.add(ChatColor.YELLOW + "Click to open!");
        meta.setLore(lore);
        item.setItemMeta(meta);
        inv.setItem(32, item);
        item = new ItemStack(Material.CHEST);
        meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
        meta.setDisplayName(storage);
        lore.clear();
        lore.add("");
        lore.add(ChatColor.GRAY + "Store items that you can access at");
        lore.add(ChatColor.GRAY + "anytime, anywhere.");
        lore.add("");
        lore.add(ChatColor.YELLOW + "Click to open!");
        meta.setLore(lore);
        item.setItemMeta(meta);
        inv.setItem(33, item);
        item = new ItemStack(Material.BARRIER);
        meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
        meta.setDisplayName(close);
        item.setItemMeta(meta);
        inv.setItem(49, item);
        fillRemainderOfInventory(inv);
        player.openInventory(inv);
    }

    public void fillRemainderOfInventory(Inventory inv) {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null || Objects.requireNonNull(inv.getItem(i)).getType() == Material.AIR) {
                inv.setItem(i, item);
            }
        }
    }
    @EventHandler
    public void onItemMove(InventoryClickEvent e) {
        if (e.getWhoClicked().getOpenInventory().getTitle().equalsIgnoreCase(title)) {
            e.setCancelled(true);
            if (e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()){
                String name = e.getCurrentItem().getItemMeta().getDisplayName();
                if (name.equalsIgnoreCase(recipes)) {
                    e.getWhoClicked().sendMessage("You clicked on recipe boko");
                } else if (name.equalsIgnoreCase(skills)) {
                    e.getWhoClicked().sendMessage("You clicked on skills");
                } else if (name.equalsIgnoreCase(quests)) {
                    plugin.menus.openQuestLog(Bukkit.getPlayer(e.getWhoClicked().getUniqueId()));
                } else if (name.equalsIgnoreCase(accessories)) {
                    plugin.menus.openAccessoryBag(Bukkit.getPlayer(e.getWhoClicked().getUniqueId()));
                } else if (name.equalsIgnoreCase(storage)) {
                    plugin.menus.openStorage(Bukkit.getPlayer(e.getWhoClicked().getUniqueId()));
                } else if (name.equalsIgnoreCase(close)) {
                    e.getInventory().close();
                }
            }
        }
    }
}
