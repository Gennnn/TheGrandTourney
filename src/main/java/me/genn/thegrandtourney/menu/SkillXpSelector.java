package me.genn.thegrandtourney.menu;

import com.nisovin.magicspells.MagicSpells;
import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.item.MMOItem;
import me.genn.thegrandtourney.player.MMOPlayer;
import me.genn.thegrandtourney.xp.Xp;
import me.genn.thegrandtourney.xp.XpType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SkillXpSelector implements Listener {

    final String title = "Skill XP";
    final String combat = ChatColor.GREEN + "Combat";
    final String mining = ChatColor.GREEN + "Mining";
    final String foraging = ChatColor.GREEN + "Foraging";
    final String farming = ChatColor.GREEN + "Farming";
    final String fishing = ChatColor.GREEN + "Fishing";
    final String blacksmithing = ChatColor.GREEN + "Smithing";
    final String tailoring = ChatColor.GREEN + "Tailoring";
    final String cooking = ChatColor.GREEN + "Cooking";
    final String alchemy = ChatColor.GREEN + "Alchemy";
    final String carpentry = ChatColor.GREEN + "Carpentry";
    final String close = ChatColor.RED + "Close";
    final String back = ChatColor.GREEN + "Go Back";
    TGT plugin;

    public SkillXpSelector(TGT plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void loadSkillSelectionMenu(final Player player) {
        Inventory inv = Bukkit.createInventory(player, 54, title);
        MMOPlayer mmoPlayer = plugin.players.get(player.getUniqueId());
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Fight monsters and bosses to gain");
        lore.add(ChatColor.GRAY + "Combat XP!");

        createSkillSelectorIcon(player, Material.STONE_SWORD,XpType.COMBAT, combat, lore, 20, inv);

        lore.clear();
        lore.add(ChatColor.GRAY + "Mine ores to gain Mining XP!");

        createSkillSelectorIcon(player, Material.GOLDEN_PICKAXE, XpType.MINING, mining, lore, 21, inv);

        lore.clear();
        lore.add(ChatColor.GRAY + "Fell trees and collect their fruits");
        lore.add(ChatColor.GRAY + "to gain Foraging XP!");

        createSkillSelectorIcon(player, Material.WOODEN_AXE, XpType.LOGGING, foraging, lore, 22, inv);

        lore.clear();
        lore.add(ChatColor.GRAY + "Destroy crops to gain Farming XP!");

        createSkillSelectorIcon(player, Material.DIAMOND_HOE, XpType.FARMING, farming, lore, 23, inv);

        lore.clear();
        lore.add(ChatColor.GRAY + "Catch fish and fight sea creatures");
        lore.add(ChatColor.GRAY + "to gain Fishing XP!");

        createSkillSelectorIcon(player, Material.FISHING_ROD, XpType.FISHING, fishing, lore, 24, inv);

        lore.clear();
        lore.add(ChatColor.GRAY + "Forge items at a Smithing Station");
        lore.add(ChatColor.GRAY + "to gain Smithing XP!");

        createSkillSelectorIcon(player, Material.ANVIL, XpType.BLACKSMITHING, blacksmithing, lore, 29, inv);

        lore.clear();
        lore.add(ChatColor.GRAY + "Craft items at a Tailoring Station");
        lore.add(ChatColor.GRAY + "to gain Tailoring XP!");

        createSkillSelectorIcon(player, Material.PINK_WOOL, XpType.TAILORING, tailoring, lore, 30, inv);

        lore.clear();
        lore.add(ChatColor.GRAY + "Create items at a Carpentry Station");
        lore.add(ChatColor.GRAY + "to gain Carpentry XP!");

        createSkillSelectorIcon(player, Material.OAK_PLANKS, XpType.CARPENTRY, carpentry, lore, 31, inv);

        lore.clear();
        lore.add(ChatColor.GRAY + "Cook food and at a Cooking");
        lore.add(ChatColor.GRAY + "Station to gain Cooking XP!");

        createSkillSelectorIcon(player, Material.FURNACE, XpType.COOKING, cooking, lore, 32, inv);

        lore.clear();
        lore.add(ChatColor.GRAY + "Brew potions at an Alchemy");
        lore.add(ChatColor.GRAY + "Station to gain Alchemy XP!");

        createSkillSelectorIcon(player, Material.BREWING_STAND, XpType.ALCHEMY, alchemy, lore, 33, inv);
        lore.clear();
        setMenuItem(new ItemStack(Material.BARRIER), close, lore, 49, inv);
        lore.add(ChatColor.GRAY + "To Menu");
        setMenuItem(new ItemStack(Material.ARROW), back, lore, 48, inv);
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

    public void createSkillSelectorIcon(Player player, Material material, XpType type, String name, List<String> topLore, int slot, Inventory inv) {
        MMOPlayer mmoPlayer = plugin.players.get(player.getUniqueId());
        ItemStack item = new ItemStack(material);
        name = name + " " + Xp.intToRoman(mmoPlayer.getLvlForType(type));
        List<String> lore = new ArrayList<>();
        for (String str : topLore) {
            lore.add(str);
        }
        lore.add("");
        double numerator = ( mmoPlayer.getXpForType(type) - ( Xp.xpForLevel.get(mmoPlayer.getLvlForType(type))));
        double denominator = (( Xp.xpForLevel.get(mmoPlayer.getLvlForType(type) + 1)) - Xp.xpForLevel.get(mmoPlayer.getLvlForType(type) ));
        double progress = 100 * (numerator/denominator);
        lore.add(ChatColor.GRAY + "Progress to Level " + Xp.intToRoman(mmoPlayer.getLvlForType(type) + 1) + ": " + ChatColor.YELLOW + String.format("%,.2f", progress) + "%" );
        int numOfFilledBars = (int)((progress/100) * 20);
        int numOfEmtptyBars = 20 - numOfFilledBars;
        String barString = "";
        for (int i = 0; i < numOfFilledBars; i++) {
            barString = barString.concat(ChatColor.DARK_GREEN + "-");
        }
        for (int i = 0; i < numOfEmtptyBars; i++) {
            barString = barString.concat(ChatColor.GRAY + "-");
        }
        barString = barString + " " + ChatColor.YELLOW + String.format("%.1f", numerator) + ChatColor.GOLD + "/" + ChatColor.YELLOW + denominator;
        lore.add(barString);
        lore.add("");
        lore.add(ChatColor.GRAY + "Level " + Xp.intToRoman(mmoPlayer.getLvlForType(type) + 1) + " Rewards:");
        List<String> text = plugin.rewardsHandler.getTableForType(type).getRewardsForLevel(mmoPlayer.getLvlForType(type)+1).rewardText;
        for (String str : text) {
            lore.add("  " + str);
        }
        lore.add("");
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
        item.setItemMeta(meta);
        inv.setItem(slot, item);
    }
}
