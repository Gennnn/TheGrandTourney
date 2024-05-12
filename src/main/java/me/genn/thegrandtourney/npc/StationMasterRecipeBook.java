package me.genn.thegrandtourney.npc;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.item.MMOItem;
import me.genn.thegrandtourney.player.MMOPlayer;
import me.genn.thegrandtourney.player.Objective;
import me.genn.thegrandtourney.skills.Craft;
import me.genn.thegrandtourney.skills.Recipe;
import me.genn.thegrandtourney.xp.Xp;
import me.genn.thegrandtourney.xp.XpType;
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
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class StationMasterRecipeBook implements Listener {
    TGT plugin;
    final String title = "Recipe Selection";
    final String previousPage = ChatColor.GREEN + "Previous Page";
    final String nextPage = ChatColor.GREEN + "Next Page";
    final String close = ChatColor.RED + "Close";
    Map<UUID, Integer> pageForPlayer;
    Map<UUID, XpType> showingTypeForPlayer;
    Map<UUID, StationMaster> speakingToNpc;
    int[] slotsToFill = {10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34,37,38,39,40,41,42,43};

    public StationMasterRecipeBook(TGT plugin) {
        this.plugin = plugin;
        this.pageForPlayer = new HashMap<>();
        this.showingTypeForPlayer = new HashMap<>();
        this.speakingToNpc = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, this.plugin);
    }

    public void loadMenuRecipeBook(Player player, int page, XpType type, StationMaster npc) {
        MMOPlayer mmoPlayer = plugin.players.get(player.getUniqueId());
        this.speakingToNpc.put(player.getUniqueId(), npc);
        createPage(player, mmoPlayer, page, type);
    }

    public void createPage(Player player, MMOPlayer mmoPlayer, int page, XpType type) {
        String modTitle = title;
        List<Recipe> recipes = new ArrayList<>();
        if (type != XpType.ALL) {
            for (int i =0; i < mmoPlayer.recipeBook.size(); i++) {
                Recipe recipe = mmoPlayer.recipeBook.get(i);
                if (recipe.type == type) {
                    recipes.add(recipe);
                }
            }
        } else {
            recipes.addAll(mmoPlayer.recipeBook);
        }

        if (recipes.size() > 28) {
            modTitle = modTitle + " (" + (page+1) + "/" + getMaxPages(mmoPlayer) +")";
        }
        Inventory inv = Bukkit.createInventory(player, 54, modTitle);
        List<String> lore = new ArrayList<>();

        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
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
            } else if (i < 9) {
                setMenuItem(item, " ", lore, i, inv);
            } else {
                if (i == 46 || i==47 || i == 51 || i == 52 || i ==50 || i == 48) {
                    lore.clear();
                    setMenuItem(item, " ", lore, i, inv);
                }  else if (i==49) {
                    lore.clear();
                    setMenuItem(new ItemStack(Material.BARRIER), close, lore, i, inv);
                }
            }
        }
        int startIndex = (28*page);
        for (int i = 0; i < slotsToFill.length; i++) {
            if (i+startIndex >= recipes.size()) {
                continue;
            } else {
                Recipe recipe = recipes.get(i+startIndex);
                setMenuItem(recipe.reward.bukkitItem.clone(), ChatColor.translateAlternateColorCodes('&', recipe.displayName), constructRecipeLore(recipe, mmoPlayer), slotsToFill[i], inv);
            }
        }

        player.openInventory(inv);
        this.pageForPlayer.put(player.getUniqueId(), page);
        this.showingTypeForPlayer.put(player.getUniqueId(), type);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent e) {
        if (e.getWhoClicked().getOpenInventory().getTitle().startsWith(title)) {
            if (e.getCurrentItem() != null && e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()){
                String name = e.getCurrentItem().getItemMeta().getDisplayName();
                if (name.equalsIgnoreCase(" ")) {
                    e.setCancelled(true);
                } else if (name.equalsIgnoreCase(close)) {
                    e.setCancelled(true);
                    e.getInventory().close();
                } else if (name.equalsIgnoreCase(nextPage)) {
                    this.loadMenuRecipeBook(Bukkit.getPlayer(e.getWhoClicked().getUniqueId()), this.pageForPlayer.get(e.getWhoClicked().getUniqueId()) + 1, this.showingTypeForPlayer.get(e.getWhoClicked().getUniqueId()), this.speakingToNpc.get(e.getWhoClicked().getUniqueId()));
                    e.setCancelled(true);
                } else if (name.equalsIgnoreCase(previousPage)) {
                    this.loadMenuRecipeBook(Bukkit.getPlayer(e.getWhoClicked().getUniqueId()), this.pageForPlayer.get(e.getWhoClicked().getUniqueId()) -1, this.showingTypeForPlayer.get(e.getWhoClicked().getUniqueId()), this.speakingToNpc.get(e.getWhoClicked().getUniqueId()));
                    e.setCancelled(true);
                }  else {
                    if (plugin.itemHandler.recipesContainsDisplayName(ChatColor.stripColor(name))) {
                        Recipe recipe = plugin.itemHandler.getRecipeFromDisplayName(ChatColor.stripColor(name));
                        Iterator iter = recipe.components.keySet().iterator();
                        Player player = Bukkit.getPlayer(e.getWhoClicked().getUniqueId());
                        while (iter.hasNext()) {
                            MMOItem item = (MMOItem) iter.next();
                            if (!hasAmount(player, item, recipe.components.get(item))) {
                                e.setCancelled(true);
                                player.playSound(player, "entity.enderman.teleport", 2.0f, 0.0f);
                                player.sendMessage(ChatColor.RED + "You don't have the necessary components to craft that item!");
                                return;
                            }
                        }
                        if (plugin.players.get(player.getUniqueId()).getLvlForType(recipe.type) < recipe.levelRequired) {
                            e.setCancelled(true);
                            player.playSound(player, "entity.enderman.teleport", 2.0f, 0.0f);
                            player.sendMessage(ChatColor.RED + "You don't have the necessary level to craft that item!");
                            return;
                        }
                        iter = recipe.components.keySet().iterator();
                        while (iter.hasNext()) {
                            MMOItem item = (MMOItem) iter.next();
                            MMOItem.removeItem(player, item, recipe.components.get(item));
                        }

                        Craft craft = new Craft(plugin);
                        craft.startCraft(player, recipe, this.speakingToNpc.get(player.getUniqueId()).station);
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

    public List<String> constructRecipeLore(Recipe recipe, MMOPlayer mmoPlayer) {
        List<String> list = new ArrayList<>();
        list.addAll(MMOItem.assembleFullLore(recipe.reward, recipe.reward.statBlock, recipe.reward.abilityBlock));
        list.add("");
        list.add(ChatColor.GRAY + "Components:");
        Iterator iter = recipe.components.keySet().iterator();
        while (iter.hasNext()) {
            MMOItem item = (MMOItem) iter.next();

            list.add("  " + item.bukkitItem.getItemMeta().getDisplayName() +ChatColor.DARK_GRAY + " x" + recipe.components.get(item) );
        }
        list.add("");
        long time = recipe.timeLimit;
        long mins = TimeUnit.SECONDS.toMinutes(time);
        time -= TimeUnit.MINUTES.toSeconds(mins);
        list.add(ChatColor.GRAY + "Limit:  " + String.format("%d:%02d", mins, time));

        time = recipe.goldThreshold;
        mins = TimeUnit.SECONDS.toMinutes(time);
        time -= TimeUnit.MINUTES.toSeconds(mins);
        list.add(ChatColor.GOLD + "✯✯✯" + ChatColor.GRAY + ": " + String.format("%d:%02d", mins, time));

        time = recipe.silverThreshold;
        mins = TimeUnit.SECONDS.toMinutes(time);
        time -= TimeUnit.MINUTES.toSeconds(mins);
        list.add(ChatColor.WHITE + "✯✯" + ChatColor.GRAY + ":   " + String.format("%d:%02d", mins, time));

        time = recipe.bronzeThreshold;
        mins = TimeUnit.SECONDS.toMinutes(time);
        time -= TimeUnit.MINUTES.toSeconds(mins);
        list.add(ChatColor.RED + "✯" + ChatColor.GRAY + ":     " + String.format("%d:%02d", mins, time));
        list.add("");
        if (mmoPlayer.getLvlForType(recipe.type) < recipe.levelRequired) {
            list.add(ChatColor.DARK_RED + "✖ " + ChatColor.RED + "Requires " + ChatColor.GREEN + recipe.type.getName() + " Level " + Xp.intToRoman(recipe.levelRequired) + ChatColor.RED + " to craft.");
        }
        if (recipe.reward.typeRequirement != null && mmoPlayer.getLvlForType(recipe.reward.typeRequirement) < recipe.reward.lvlRequirement) {
            list.add(ChatColor.DARK_RED + "✖ " + ChatColor.RED + "Requires " + ChatColor.GREEN + recipe.reward.typeRequirement.getName() + " Level " + Xp.intToRoman(recipe.reward.lvlRequirement) + ChatColor.RED + " to use.");
        }

        return list;
    }

    public int getMaxPages(MMOPlayer mmoPlayer) {
        return (int)((mmoPlayer.objectives.size() / 28)+1);
    }
    public boolean hasAmount(Player player, MMOItem component, int quantity)
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
                        if (mmoId.equalsIgnoreCase(component.internalName)) {
                            has += item.getAmount();
                        }
                    }
                }

            }
        }
        return (has >= quantity);
    }
}
