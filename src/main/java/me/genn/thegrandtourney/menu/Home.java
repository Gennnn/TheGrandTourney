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
    final String accessories = ChatColor.GREEN + "Accessory Pouch";
    final String storage = ChatColor.GREEN + "Storage";
    final String activeEffects = ChatColor.GREEN + "Active Effects";
    final String mobileBanking = ChatColor.GREEN + "Mobile Banking";
    final String close = ChatColor.RED + "Close";
    final String quiver = ChatColor.GREEN + "Quiver";
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
        lore.add("  " + ChatColor.RED + "❤ Health " + ChatColor.WHITE + formatDouble(mmoPlayer.getMaxHealth()));
        lore.add("  " + ChatColor.AQUA + "❈ Defense " + ChatColor.WHITE + formatDouble(mmoPlayer.getDefense()));
        lore.add("  " + ChatColor.WHITE + "✦ Speed " + formatDouble(mmoPlayer.getSpeed()));
        lore.add("  " + ChatColor.RED + "❁ Strength " + ChatColor.WHITE + formatDouble(mmoPlayer.getDamage()));
        lore.add("  " + ChatColor.GREEN + "⚡ Vigor " + ChatColor.WHITE + formatDouble(mmoPlayer.getVigor()));
        lore.add("  " + ChatColor.BLUE + "☠ Crit Damage " + ChatColor.WHITE + formatDouble(mmoPlayer.getCritDamage()));
        lore.add("  " + ChatColor.LIGHT_PURPLE + "◎ Focus " + ChatColor.WHITE + formatDouble(mmoPlayer.getFocus()));
        lore.add("  " + ChatColor.RED + "๑ Ability Damage " + ChatColor.WHITE + formatDouble(mmoPlayer.getAbilityDamage()));
        lore.add("  " + ChatColor.GOLD + "☘ Luck " + ChatColor.WHITE + formatDouble(mmoPlayer.getLuck()));
        /*lore.add("  " + ChatColor.BLUE + "☣ Crit Chance " + ChatColor.WHITE + mmoPlayer.getCritChance());*/
        /*lore.add("  " + ChatColor.GOLD + "☘ Mining Fortune " + ChatColor.WHITE + mmoPlayer.getMiningFortune());
        lore.add("  " + ChatColor.GOLD + "☘ Farming Fortune " + ChatColor.WHITE + mmoPlayer.getFarmingFortune());
        lore.add("  " + ChatColor.GOLD + "☘ Logging Fortune " + ChatColor.WHITE + mmoPlayer.getLoggingFortune());*/
        lore.add("  " + ChatColor.AQUA + "☂ Fishing Speed " + ChatColor.WHITE + formatDouble(mmoPlayer.getLure()));
        lore.add("  " + ChatColor.YELLOW + "☈ Lure " + ChatColor.WHITE + formatDouble(mmoPlayer.getFlash()));
        lore.add("  " + ChatColor.DARK_AQUA + "α Sea Creature Chance " + ChatColor.WHITE + formatDouble(mmoPlayer.getSeaCreatureChance()));
        /*lore.add("  " + ChatColor.LIGHT_PURPLE + "⌚ Dialogue Speed " +ChatColor.WHITE+ mmoPlayer.getDialogueSpeed());
        lore.add("  " + ChatColor.GOLD + "$ Shop Discount " + ChatColor.WHITE + mmoPlayer.getVendorPrice());*/
        lore.add("  " + ChatColor.RED + "❣ Health Regen " + ChatColor.WHITE + formatDouble(mmoPlayer.getHealthRegen()));
        lore.add("  " + ChatColor.DARK_GREEN + "⌛ Stamina Regen " + ChatColor.WHITE + formatDouble(MagicSpells.getManaHandler().getRegenAmount(player)));
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
        inv.setItem(20, item);
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
        inv.setItem(21, item);
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
        inv.setItem(22, item);
        item = new ItemStack(Material.PLAYER_HEAD);
        MMOItem.getHeadFrom64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTVhOTI1MjFlZDc3OTA0ZWI0NTAwMDUwYjc1NmVkY2NlNmE2MjA3YmFjNGVhMWM2ZjhiMzViNWY4NmE5YWJhNSJ9fX0=", item);
        meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
        meta.setDisplayName(accessories);
        lore.clear();
        lore.add("");
        lore.add(ChatColor.GRAY + "A special bag that can hold your accessories.");
        lore.add(ChatColor.GRAY + "Accessories will only function while in your");
        lore.add(ChatColor.GRAY + "accessory pouch.");
        lore.add("");
        lore.add(ChatColor.YELLOW + "Click to open!");
        meta.setLore(lore);
        item.setItemMeta(meta);
        inv.setItem(23, item);
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
        inv.setItem(24, item);
        item = new ItemStack(Material.BARRIER);
        meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
        meta.setDisplayName(close);
        item.setItemMeta(meta);
        inv.setItem(49, item);
        item = new ItemStack(Material.POTION);
        meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
        meta.setDisplayName(activeEffects);
        lore.clear();
        lore.add("");
        lore.add(ChatColor.GRAY + "View and manage your currently");
        lore.add(ChatColor.GRAY + "active potion effects.");
        lore.add("");
        lore.add(ChatColor.YELLOW + "Click to open!");
        meta.setLore(lore);
        item.setItemMeta(meta);
        inv.setItem(31, item);
        lore.clear();
        item = new ItemStack(Material.PLAYER_HEAD);
        MMOItem.getHeadFrom64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWZkMTA4MzgzZGZhNWIwMmU4NjYzNTYwOTU0MTUyMGU0ZTE1ODk1MmQ2OGMxYzhmOGYyMDBlYzdlODg2NDJkIn19fQ==", item);
        meta = item.getItemMeta();
        meta.setDisplayName(mobileBanking);
        lore.add(" ");
        lore.add(ChatColor.GRAY + "Deposit and withdraw your Dosh");
        lore.add(ChatColor.GRAY + "from anywhere!");
        lore.add(" ");
        lore.add(ChatColor.YELLOW + "Click to view!");
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
        meta.setLore(lore);
        item.setItemMeta(meta);
        inv.setItem(30,item);
        item = new ItemStack(Material.PLAYER_HEAD);
        MMOItem.getHeadFrom64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzg1NmNmOTcxNjIzOWE3NzA2MjY4NjUyZWZmM2IyOWNlNWRhY2RmYWIxZmIyZmIzMGE1NGIwNzk2NzQwMDYyIn19fQ==", item);
        meta = item.getItemMeta();
        meta.setDisplayName(quiver);
        lore.clear();
        lore.add(" ");
        lore.add(ChatColor.GRAY + "Store all of your arrows");
        lore.add(ChatColor.GRAY + "in one spot!");
        lore.add(" ");
        lore.add(ChatColor.YELLOW + "Click to open!");
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
        item.setItemMeta(meta);
        inv.setItem(32, item);
        fillRemainderOfInventory(inv);
        player.openInventory(inv);
    }

    private String formatDouble(double amount) {
        if ((int)amount == amount) {
            return String.format("%,d",(int)amount);
        } else {
            return String.format("%,.1f",amount);
        }
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
                Player player = Bukkit.getPlayer(e.getWhoClicked().getUniqueId());
                String name = e.getCurrentItem().getItemMeta().getDisplayName();
                if (name.equalsIgnoreCase(recipes)) {
                    plugin.menus.openRecipeBook(player);
                    player.playSound(player, "item.book.page_turn", 0.5f,1.5f);
                } else if (name.equalsIgnoreCase(skills)) {
                    plugin.menus.openSkillMenu(player);
                    player.playSound(player, "entity.player.levelup", 0.5f,1.75f);
                } else if (name.equalsIgnoreCase(quests)) {
                    plugin.menus.openQuestLog(player);
                    player.playSound(player, "item.book.page_turn", 0.5f,0.75f);
                } else if (name.equalsIgnoreCase(accessories)) {
                    plugin.menus.openAccessoryBag(player);
                    player.playSound(player, "entity.horse.saddle", 0.5f,1.25f);
                } else if (name.equalsIgnoreCase(storage)) {
                    plugin.menus.openStorage(player);
                    player.playSound(player, "block.chest.open", 0.5f,0.5f);
                } else if (name.equalsIgnoreCase(close)) {
                    e.getInventory().close();
                } else if (name.equalsIgnoreCase(activeEffects)) {
                    plugin.menus.openActiveEffectsMenu(player);
                    player.playSound(player, "block.brewing_stand.brew", 0.5f, 1.0f);
                } else if (name.equalsIgnoreCase(mobileBanking)) {
                    plugin.accessMobileBank(player);
                } else if (name.equalsIgnoreCase(quiver)) {
                    plugin.menus.openQuiver(player);
                    player.playSound(player,"entity.horse.saddle", 0.5f, 2.0f);
                }
            }
        }
    }
}
