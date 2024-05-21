package me.genn.thegrandtourney.menu;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.item.MMOItem;
import me.genn.thegrandtourney.player.BankTransaction;
import me.genn.thegrandtourney.player.MMOPlayer;
import me.genn.thegrandtourney.xp.Xp;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class Bank implements Listener {
    TGT plugin;
    final String title = "Bank";
    final String close = ChatColor.RED + "Close";
    final String back = ChatColor.GREEN + "Go Back";
    final String info = ChatColor.GREEN + "Info";
    final String deposit = ChatColor.GREEN + "Deposit Dosh";
    final String withdraw = ChatColor.GREEN + "Withdraw Dosh";
    final String transactionHistory = ChatColor.GREEN + "Transaction History";
    final String mobileBanking = ChatColor.GREEN + "Mobile Banking";
    final String depositTitle = "Make Deposit";
    final String withdrawalTitle = "Make Withdrawal";
    final String fullDeposit = ChatColor.GREEN + "Deposit All";
    final String halfDeposit = ChatColor.GREEN + "Deposit 50%";
    final String twentyDeposit = ChatColor.GREEN + "Deposit 20%";
    final String customDeposit = ChatColor.GREEN + "Specific Amount";
    final String fullWithdrawal = ChatColor.GREEN + "Withdraw All";
    final String halfWithdrawal = ChatColor.GREEN + "Withdraw 50%";
    final String twentyWithdrawal = ChatColor.GREEN + "Withdraw 20%";
    final String customWithdrawal = ChatColor.GREEN + "Specific Amount";
    Map<UUID, Integer> pageForPlayer = new HashMap<>();
    HashSet<UUID> accessingRemotely = new HashSet<>();
    HashSet<UUID> waitingForSignData = new HashSet<>();
    HashMap<UUID, Double> customAmount = new HashMap<>();

    public Bank(TGT plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void loadBank(Player player, boolean remote, int page) {
        MMOPlayer mmoPlayer = plugin.players.get(player.getUniqueId());
        createBankMenu(player, mmoPlayer, remote, page);
    }

    public void createBankMenu(Player player, MMOPlayer mmoPlayer, boolean remote, int page) {
        if (remote) {
            this.accessingRemotely.add(player.getUniqueId());
        } else {
            this.accessingRemotely.remove(player.getUniqueId());
        }
        Inventory inv = Bukkit.createInventory(player, 36, title);
        setMenuItem(new ItemStack(Material.BARRIER), close, new ArrayList<>(), 31, inv);
        List<String> lore = new ArrayList<>();
        if (remote) {
            lore.add(ChatColor.GRAY + "To Menu");
            setMenuItem(new ItemStack(Material.ARROW), back, lore, 30, inv);
            lore.clear();
        }
        lore.add(ChatColor.GRAY + "Upon dying, you'll lose half of");
        lore.add(ChatColor.GRAY + "the " + ChatColor.GOLD + "Dosh" + ChatColor.GRAY + " in your Purse.");
        lore.add(ChatColor.GRAY + "Store it in the Bank to keep it safe!");
        setMenuItem(new ItemStack(Material.REDSTONE_TORCH), info, lore, 32, inv);
        lore.clear();
        ItemStack item = new ItemStack(Material.CHEST);
        double goldAmt = mmoPlayer.getBankGold();
        if ((int)goldAmt == goldAmt) {
            lore.add(ChatColor.GRAY + "Current Balance: " + ChatColor.GOLD + String.format("%,d", (int)goldAmt));
        } else {
            lore.add(ChatColor.GRAY + "Current Balance: " + ChatColor.GOLD + String.format("%,.1f",goldAmt));

        }
        lore.add(" ");
        lore.add(ChatColor.GRAY + "Store " + ChatColor.GOLD + "Dosh " + ChatColor.GRAY + "in the bank to");
        lore.add(ChatColor.GRAY + "keep it safe while on your adventure!");
        lore.add(" ");
        lore.add(ChatColor.YELLOW + "Click to make a deposit!");
        if (remote) {
            setMenuItem(item, deposit, lore, 11, inv);
        } else {
            setMenuItem(item, deposit, lore, 10, inv);
        }
        lore.clear();
        item = new ItemStack(Material.DROPPER);
        if ((int)goldAmt == goldAmt) {
            lore.add(ChatColor.GRAY + "Current Balance: " + ChatColor.GOLD + String.format("%,d", (int)goldAmt));
        } else {
            lore.add(ChatColor.GRAY + "Current Balance: " + ChatColor.GOLD + String.format("%,.1f",goldAmt));
        }
        lore.add(" ");
        lore.add(ChatColor.GRAY + "Withdraw " + ChatColor.GOLD + "Dosh " + ChatColor.GRAY + "from your Bank");
        lore.add(ChatColor.GRAY + "in order to spend it it.");
        lore.add(" ");
        lore.add(ChatColor.YELLOW + "Click to make a withdrawal!");
        if (remote) {
            setMenuItem(item, withdraw, lore, 13, inv);
        } else {
            setMenuItem(item, withdraw, lore, 12, inv);
        }
        lore.clear();
        item = new ItemStack(Material.FILLED_MAP);
        if (remote) {
            setMenuItem(item, transactionHistory, getTransactionHistory(mmoPlayer, page), 15, inv);
        } else {
            setMenuItem(item, transactionHistory, getTransactionHistory(mmoPlayer, page), 14, inv);
        }
        if (!remote) {
            item = new ItemStack(Material.PLAYER_HEAD);
            MMOItem.getHeadFrom64("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWZkMTA4MzgzZGZhNWIwMmU4NjYzNTYwOTU0MTUyMGU0ZTE1ODk1MmQ2OGMxYzhmOGYyMDBlYzdlODg2NDJkIn19fQ==", item);
            if (mmoPlayer.mobileBankLvl == 0) {
                lore.add(ChatColor.GRAY + "Allows you to access your Bank directly from");
                lore.add(ChatColor.GRAY + "your Player Menu.");
                lore.add(" ");
                lore.add(ChatColor.GRAY + "Cooldown: " + ChatColor.GREEN + "10 minutes");
                lore.add(ChatColor.GRAY + "Cost: " + ChatColor.GOLD + "250 Dosh");
                lore.add(" ");
                lore.add(ChatColor.YELLOW + "Click to unlock!");

            } else if (mmoPlayer.mobileBankLvl == 1) {
                lore.add(ChatColor.GRAY + "Allows you to access your Bank directly from");
                lore.add(ChatColor.GRAY + "your Player Menu.");
                lore.add(" ");
                lore.add(ChatColor.GRAY + "Current: " + ChatColor.YELLOW + "10 minutes");
                lore.add(ChatColor.GRAY + "Upgrade: " + ChatColor.GREEN + "5 minutes");
                lore.add(" ");
                lore.add(ChatColor.GRAY + "Cost: " + ChatColor.GOLD + "500 Dosh");
                lore.add(" ");
                lore.add(ChatColor.YELLOW + "Click to upgrade!");

            } else if (mmoPlayer.mobileBankLvl == 2) {
                lore.add(ChatColor.GRAY + "Allows you to access your Bank directly from");
                lore.add(ChatColor.GRAY + "your Player Menu.");
                lore.add(" ");
                lore.add(ChatColor.GRAY + "Current: " + ChatColor.YELLOW + "5 minutes");
                lore.add(ChatColor.GRAY + "Upgrade: " + ChatColor.GREEN + "2 minutes");
                lore.add(" ");
                lore.add(ChatColor.GRAY + "Cost: " + ChatColor.GOLD + "1000 Dosh");
                lore.add(" ");
                lore.add(ChatColor.YELLOW + "Click to upgrade!");

            } else if (mmoPlayer.mobileBankLvl == 3) {
                lore.add(ChatColor.GRAY + "Allows you to access your Bank directly from");
                lore.add(ChatColor.GRAY + "your Player Menu.");
                lore.add(" ");
                lore.add(ChatColor.GRAY + "Current: " + ChatColor.YELLOW + "2 minutes");
                lore.add(ChatColor.GRAY + "Upgrade: " + ChatColor.GREEN + "None");
                lore.add(" ");
                lore.add(ChatColor.GRAY + "Cost: " + ChatColor.GOLD + "2000 Dosh");
                lore.add(" ");
                lore.add(ChatColor.YELLOW + "Click to upgrade!");
            } else if (mmoPlayer.mobileBankLvl == 4) {
                lore.add(ChatColor.GRAY + "Allows you to access your Bank directly from");
                lore.add(ChatColor.GRAY + "your Player Menu.");
                lore.add(" ");
                lore.add(ChatColor.GREEN + ChatColor.BOLD.toString() + "MAXED OUT!");
            }
            setMenuItem(item, mobileBanking, lore, 16, inv);
        }
        fillRemainderOfInventory(inv);
        this.pageForPlayer.put(player.getUniqueId(), page);
        player.openInventory(inv);
    }
    private void createDepositMenu(Player player, MMOPlayer mmoPlayer) {
        if (waitingForSignData.contains(player.getUniqueId()) && customAmount.containsKey(player.getUniqueId())) {
            double amount = customAmount.get(player.getUniqueId());
            mmoPlayer.addBankGold(amount);
            mmoPlayer.removePurseGold(amount);
            player.sendMessage(ChatColor.GREEN + "You deposited " + ChatColor.GOLD + getMoneyString(amount) + " Dosh " + ChatColor.GREEN + "into your Bank!");
            mmoPlayer.addBankTransaction(amount,System.currentTimeMillis(),ChatColor.AQUA + player.getName());
            plugin.shopHandler.playBuySound(player);
        }
        waitingForSignData.remove(player.getUniqueId());
        customAmount.remove(player.getUniqueId());
        Inventory inv = Bukkit.createInventory(player, 36, depositTitle);
        List<String> lore = new ArrayList<>();
        double goldAmt = mmoPlayer.getBankGold();
        if ((int)goldAmt == goldAmt) {
            lore.add(ChatColor.GRAY + "Current Balance: " + ChatColor.GOLD + String.format("%,d", (int)goldAmt));
        } else {
            lore.add(ChatColor.GRAY + "Current Balance: " + ChatColor.GOLD + String.format("%,.1f",goldAmt));
        }
        lore.add(" ");
        double addedAmt = mmoPlayer.getPurseGold();
        if ((int)(goldAmt+addedAmt) == goldAmt+addedAmt) {
            lore.add(ChatColor.GRAY + "After Deposit: " + ChatColor.GOLD + String.format("%,d", (int)(goldAmt+addedAmt)));
        } else {
            lore.add(ChatColor.GRAY + "After Deposit: " + ChatColor.GOLD + String.format("%,.1f",goldAmt+addedAmt));
        }
        lore.add(" ");
        lore.add(ChatColor.YELLOW + "Click to deposit Dosh!");

        setMenuItem(new ItemStack(Material.CHEST).asQuantity(64), fullDeposit, lore, 10,inv);
        lore.clear();
        if ((int)goldAmt == goldAmt) {
            lore.add(ChatColor.GRAY + "Current Balance: " + ChatColor.GOLD + String.format("%,d", (int)goldAmt));
        } else {
            lore.add(ChatColor.GRAY + "Current Balance: " + ChatColor.GOLD + String.format("%,.1f",goldAmt));
        }
        lore.add(" ");
        addedAmt = mmoPlayer.getPurseGold()/2;
        if ((int)(goldAmt+addedAmt) == goldAmt+addedAmt) {
            lore.add(ChatColor.GRAY + "After Deposit: " + ChatColor.GOLD + String.format("%,d", (int)(goldAmt+addedAmt)));
        } else {
            lore.add(ChatColor.GRAY + "After Deposit: " + ChatColor.GOLD + String.format("%,.1f",goldAmt+addedAmt));
        }
        lore.add(" ");
        lore.add(ChatColor.YELLOW + "Click to deposit Dosh!");

        setMenuItem(new ItemStack(Material.CHEST).asQuantity(32), halfDeposit, lore, 12,inv);
        lore.clear();

        if ((int)goldAmt == goldAmt) {
            lore.add(ChatColor.GRAY + "Current Balance: " + ChatColor.GOLD + String.format("%,d", (int)goldAmt));
        } else {
            lore.add(ChatColor.GRAY + "Current Balance: " + ChatColor.GOLD + String.format("%,.1f",goldAmt));
        }
        lore.add(" ");
        addedAmt = mmoPlayer.getPurseGold() * 0.20;
        if ((int)(goldAmt+addedAmt) == goldAmt+addedAmt) {
            lore.add(ChatColor.GRAY + "After Deposit: " + ChatColor.GOLD + String.format("%,d", (int)(goldAmt+addedAmt)));
        } else {
            lore.add(ChatColor.GRAY + "After Deposit: " + ChatColor.GOLD + String.format("%,.1f",goldAmt+addedAmt));
        }
        lore.add(" ");
        lore.add(ChatColor.YELLOW + "Click to deposit Dosh!");
        setMenuItem(new ItemStack(Material.CHEST).asQuantity(1), twentyDeposit, lore, 14,inv);
        lore.clear();

        if ((int)goldAmt == goldAmt) {
            lore.add(ChatColor.GRAY + "Current Balance: " + ChatColor.GOLD + String.format("%,d", (int)goldAmt));
        } else {
            lore.add(ChatColor.GRAY + "Current Balance: " + ChatColor.GOLD + String.format("%,.1f",goldAmt));
        }
        lore.add(" ");
        lore.add(ChatColor.YELLOW + "Click to deposit Dosh!");
        setMenuItem(new ItemStack(Material.OAK_SIGN), customDeposit, lore, 16,inv);
        lore.clear();
        setMenuItem(new ItemStack(Material.BARRIER), close, lore, 31, inv);
        lore.add(ChatColor.GRAY + "To Bank");
        setMenuItem(new ItemStack(Material.ARROW), back, lore, 30, inv);
        fillRemainderOfInventory(inv);
        player.openInventory(inv);
    }
    private void createWithdrawMenu(Player player, MMOPlayer mmoPlayer) {
        if (waitingForSignData.contains(player.getUniqueId()) && customAmount.containsKey(player.getUniqueId())) {
            double amount = customAmount.get(player.getUniqueId());
            mmoPlayer.removeBankGold(amount);
            mmoPlayer.addPurseGold(amount);
            player.sendMessage(ChatColor.GREEN + "You withdrew " + ChatColor.GOLD + getMoneyString(amount) + " Dosh " + ChatColor.GREEN + "from your Bank!");
            mmoPlayer.addBankTransaction(-amount,System.currentTimeMillis(),ChatColor.AQUA + player.getName());
            plugin.shopHandler.playBuySound(player);
        }
        waitingForSignData.remove(player.getUniqueId());
        customAmount.remove(player.getUniqueId());
        Inventory inv = Bukkit.createInventory(player, 36, withdrawalTitle);
        List<String> lore = new ArrayList<>();
        double goldAmt = mmoPlayer.getBankGold();
        if ((int)goldAmt == goldAmt) {
            lore.add(ChatColor.GRAY + "Current Balance: " + ChatColor.GOLD + String.format("%,d", (int)goldAmt));
        } else {
            lore.add(ChatColor.GRAY + "Current Balance: " + ChatColor.GOLD + String.format("%,.1f",goldAmt));
        }
        lore.add(" ");
        double addedAmt = -mmoPlayer.getBankGold();
        if ((int)(goldAmt+addedAmt) == goldAmt+addedAmt) {
            lore.add(ChatColor.GRAY + "After Withdrawal: " + ChatColor.GOLD + String.format("%,d", (int)(goldAmt+addedAmt)));
        } else {
            lore.add(ChatColor.GRAY + "After Withdrawal: " + ChatColor.GOLD + String.format("%,.1f",goldAmt+addedAmt));
        }
        lore.add(" ");
        lore.add(ChatColor.YELLOW + "Click to withdraw Dosh!");

        setMenuItem(new ItemStack(Material.DROPPER).asQuantity(64), fullWithdrawal, lore, 10,inv);
        lore.clear();
        if ((int)goldAmt == goldAmt) {
            lore.add(ChatColor.GRAY + "Current Balance: " + ChatColor.GOLD + String.format("%,d", (int)goldAmt));
        } else {
            lore.add(ChatColor.GRAY + "Current Balance: " + ChatColor.GOLD + String.format("%,.1f",goldAmt));
        }
        lore.add(" ");
        addedAmt = -mmoPlayer.getBankGold()/2;
        if ((int)(goldAmt+addedAmt) == goldAmt+addedAmt) {
            lore.add(ChatColor.GRAY + "After Withdrawal: " + ChatColor.GOLD + String.format("%,d", (int)(goldAmt+addedAmt)));
        } else {
            lore.add(ChatColor.GRAY + "After Withdrawal: " + ChatColor.GOLD + String.format("%,.1f",goldAmt+addedAmt));
        }
        lore.add(" ");
        lore.add(ChatColor.YELLOW + "Click to withdraw Dosh!");

        setMenuItem(new ItemStack(Material.DROPPER).asQuantity(32), halfWithdrawal, lore, 12,inv);
        lore.clear();

        if ((int)goldAmt == goldAmt) {
            lore.add(ChatColor.GRAY + "Current Balance: " + ChatColor.GOLD + String.format("%,d", (int)goldAmt));
        } else {
            lore.add(ChatColor.GRAY + "Current Balance: " + ChatColor.GOLD + String.format("%,.1f",goldAmt));
        }
        lore.add(" ");
        addedAmt = -mmoPlayer.getBankGold() * 0.20;
        if ((int)(goldAmt+addedAmt) == goldAmt+addedAmt) {
            lore.add(ChatColor.GRAY + "After Withdrawal: " + ChatColor.GOLD + String.format("%,d", (int)(goldAmt+addedAmt)));
        } else {
            lore.add(ChatColor.GRAY + "After Withdrawal: " + ChatColor.GOLD + String.format("%,.1f",goldAmt+addedAmt));
        }
        lore.add(" ");
        lore.add(ChatColor.YELLOW + "Click to withdraw Dosh!");
        setMenuItem(new ItemStack(Material.DROPPER).asQuantity(1), twentyWithdrawal, lore, 14,inv);
        lore.clear();

        if ((int)goldAmt == goldAmt) {
            lore.add(ChatColor.GRAY + "Current Balance: " + ChatColor.GOLD + String.format("%,d", (int)goldAmt));
        } else {
            lore.add(ChatColor.GRAY + "Current Balance: " + ChatColor.GOLD + String.format("%,.1f",goldAmt));
        }
        lore.add(" ");
        lore.add(ChatColor.YELLOW + "Click to withdraw Dosh!");
        setMenuItem(new ItemStack(Material.OAK_SIGN), customWithdrawal, lore, 16,inv);
        lore.clear();
        setMenuItem(new ItemStack(Material.BARRIER), close, lore, 31, inv);
        lore.add(ChatColor.GRAY + "To Bank");
        setMenuItem(new ItemStack(Material.ARROW), back, lore, 30, inv);
        fillRemainderOfInventory(inv);
        player.openInventory(inv);
    }

    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        if (e.getWhoClicked().getOpenInventory().getTitle().equalsIgnoreCase(title)) {
            e.setCancelled(true);
            if (e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR && e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()) {
                String name = e.getCurrentItem().getItemMeta().getDisplayName();
                MMOPlayer mmoPlayer = plugin.players.get(e.getWhoClicked().getUniqueId());
                Player player = Bukkit.getPlayer(e.getWhoClicked().getUniqueId());
                if (name.equalsIgnoreCase(close)) {
                    e.getWhoClicked().closeInventory();
                } else if (name.equalsIgnoreCase(back)) {
                    plugin.menus.openHomeMenu(player);
                    plugin.menus.playClickSound(player);
                } else if (name.equalsIgnoreCase(deposit)) {
                    this.createDepositMenu(player,mmoPlayer);
                    plugin.menus.playClickSound(player);
                } else if (name.equalsIgnoreCase(withdraw)) {
                    this.createWithdrawMenu(player,mmoPlayer);
                    plugin.menus.playClickSound(player);
                } else if (name.equalsIgnoreCase(transactionHistory)) {
                    if (this.pageForPlayer.get(e.getWhoClicked().getUniqueId()) != 0 && (e.getClick() == ClickType.RIGHT || e.getClick() == ClickType.SHIFT_RIGHT)) {
                        this.loadBank(Bukkit.getPlayer(e.getWhoClicked().getUniqueId()), this.accessingRemotely.contains(e.getWhoClicked().getUniqueId()), this.pageForPlayer.get(e.getWhoClicked().getUniqueId()) -1);
                    } else if ((this.pageForPlayer.get(e.getWhoClicked().getUniqueId()) * 10)+10 <= mmoPlayer.transactionHistory.size() && (e.getClick() == ClickType.LEFT || e.getClick() == ClickType.SHIFT_LEFT)) {
                        this.loadBank(Bukkit.getPlayer(e.getWhoClicked().getUniqueId()), this.accessingRemotely.contains(e.getWhoClicked().getUniqueId()), this.pageForPlayer.get(e.getWhoClicked().getUniqueId())+1);
                    }
                    plugin.menus.playClickSound(player);
                } else if (name.equalsIgnoreCase(mobileBanking) && mmoPlayer.mobileBankLvl < 4) {
                    purchaseBankUpgrade(Bukkit.getPlayer(e.getWhoClicked().getUniqueId()));
                }
            }
        } else if (e.getWhoClicked().getOpenInventory().getTitle().equalsIgnoreCase(depositTitle)) {
            e.setCancelled(true);
            if (e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR && e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()) {
                String name = e.getCurrentItem().getItemMeta().getDisplayName();
                MMOPlayer mmoPlayer = plugin.players.get(e.getWhoClicked().getUniqueId());
                Player player = Bukkit.getPlayer(e.getWhoClicked().getUniqueId());
                if (name.equalsIgnoreCase(fullDeposit) || name.equalsIgnoreCase(halfDeposit) || name.equalsIgnoreCase(twentyDeposit)) {
                    double amount = 0;
                    if (name.equalsIgnoreCase(fullDeposit)) {
                        amount = mmoPlayer.getPurseGold();
                    } else if (name.equalsIgnoreCase(halfDeposit)) {
                        amount = mmoPlayer.getPurseGold()/2;
                    } else if (name.equalsIgnoreCase(twentyDeposit)) {
                        amount = mmoPlayer.getPurseGold()*0.2;
                    }
                    if (amount < 1) {
                        e.getWhoClicked().sendMessage(ChatColor.RED + "You don't have any Dosh to deposit!");
                        plugin.shopHandler.playNoMoneySound(player);
                        return;
                    }
                    mmoPlayer.addBankGold(amount);
                    mmoPlayer.removePurseGold(amount);
                    e.getWhoClicked().sendMessage(ChatColor.GREEN + "You deposited " + ChatColor.GOLD + getMoneyString(amount) + " Dosh " + ChatColor.GREEN + "into your Bank!");
                    mmoPlayer.addBankTransaction(amount,System.currentTimeMillis(),ChatColor.AQUA + player.getName());
                    plugin.shopHandler.playBuySound(player);
                    this.createDepositMenu(player, mmoPlayer);
                } else if (name.equalsIgnoreCase(customDeposit)) {
                    openInputSign(Bukkit.getPlayer(e.getWhoClicked().getUniqueId()),"Enter amount", "to deposit", "deposit");
                    plugin.menus.playClickSound(player);
                } else if (name.equalsIgnoreCase(close)) {
                    e.getWhoClicked().closeInventory();
                } else if (name.equalsIgnoreCase(back)) {
                    this.customAmount.remove(player.getUniqueId());
                    this.waitingForSignData.remove(player.getUniqueId());
                    this.loadBank(Bukkit.getPlayer(e.getWhoClicked().getUniqueId()), this.accessingRemotely.contains(e.getWhoClicked().getUniqueId()),0);
                    plugin.menus.playClickSound(player);
                }
            }
        } else if (e.getWhoClicked().getOpenInventory().getTitle().equalsIgnoreCase(withdrawalTitle)) {
            e.setCancelled(true);
            if (e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR && e.getCurrentItem().hasItemMeta() && e.getCurrentItem().getItemMeta().hasDisplayName()) {
                String name = e.getCurrentItem().getItemMeta().getDisplayName();
                MMOPlayer mmoPlayer = plugin.players.get(e.getWhoClicked().getUniqueId());
                Player player = Bukkit.getPlayer(e.getWhoClicked().getUniqueId());
                if (name.equalsIgnoreCase(fullWithdrawal) || name.equalsIgnoreCase(halfWithdrawal) || name.equalsIgnoreCase(twentyWithdrawal)) {
                    double amount = 0;
                    if (name.equalsIgnoreCase(fullWithdrawal)) {
                        amount = mmoPlayer.getBankGold();
                    } else if (name.equalsIgnoreCase(halfWithdrawal)) {
                        amount = mmoPlayer.getBankGold()/2;
                    } else if (name.equalsIgnoreCase(twentyWithdrawal)) {
                        amount = mmoPlayer.getBankGold()*0.2;
                    }
                    if (amount < 1) {
                        e.getWhoClicked().sendMessage(ChatColor.RED + "You don't have any Dosh to withdraw!");
                        plugin.shopHandler.playNoMoneySound(player);
                        return;
                    }
                    mmoPlayer.removeBankGold(amount);
                    mmoPlayer.addPurseGold(amount);
                    mmoPlayer.addBankTransaction(-amount,System.currentTimeMillis(),ChatColor.AQUA + player.getName());
                    e.getWhoClicked().sendMessage(ChatColor.GREEN + "You withdrew " + ChatColor.GOLD + getMoneyString(amount) + " Dosh " + ChatColor.GREEN + "from your Bank!");
                    this.createWithdrawMenu(player, mmoPlayer);
                    plugin.shopHandler.playBuySound(player);
                } else if (name.equalsIgnoreCase(customDeposit)) {
                    openInputSign(Bukkit.getPlayer(e.getWhoClicked().getUniqueId()),"Enter amount", "to withdraw", "withdraw");
                    plugin.menus.playClickSound(player);
                } else if (name.equalsIgnoreCase(close)) {
                    e.getWhoClicked().closeInventory();
                } else if (name.equalsIgnoreCase(back)) {
                    this.customAmount.remove(player.getUniqueId());
                    this.waitingForSignData.remove(player.getUniqueId());
                    this.loadBank(Bukkit.getPlayer(e.getWhoClicked().getUniqueId()), this.accessingRemotely.contains(e.getWhoClicked().getUniqueId()),0);
                    plugin.menus.playClickSound(player);
                }
            }
        }
    }

    private String getMoneyString(double amount) {
        if ((int)amount == amount) {
            return String.format("%,d",(int)amount);
        } else {
            return String.format("%,.1f",amount);
        }
    }
    private void openInputSign(Player player, String descLine1, String descLine2, String menuToOpen) {
        this.waitingForSignData.add(player.getUniqueId());
        new BukkitRunnable() {
            @Override
            public void run() {

                ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
                PacketContainer blockUpdate = new PacketContainer(PacketType.Play.Server.BLOCK_CHANGE);
                blockUpdate.getModifier().writeDefaults();
                BlockPosition signPosition = new BlockPosition(player.getLocation().toVector());
                WrappedBlockData wrappedBlockData = WrappedBlockData.createData(Material.OAK_SIGN);

                blockUpdate.getBlockPositionModifier().

                        write(0, signPosition);
                blockUpdate.getBlockData().

                        write(0, wrappedBlockData);
                protocolManager.sendServerPacket(player, blockUpdate);

                // edit sign
                player.sendBlockChange(player.getLocation(), Material.OAK_SIGN.createBlockData());

                String[] lines = new String[4];
                lines[0] = "";
                lines[1] = "^^^^^^^^^^^^^^^";
                lines[2] = descLine1;
                lines[3] = descLine2;
                player.sendSignChange(player.getLocation(), lines);

                // open sign editor
                PacketContainer openSignPacket = new PacketContainer(PacketType.Play.Server.OPEN_SIGN_EDITOR);
                openSignPacket.getModifier().

                        writeDefaults();
                openSignPacket.getBlockPositionModifier().

                        write(0, signPosition);
                protocolManager.sendServerPacket(player, openSignPacket);

                // change sign to air
                wrappedBlockData = WrappedBlockData.createData(Material.AIR);
                blockUpdate.getBlockData().

                        write(0, wrappedBlockData);
                protocolManager.sendServerPacket(player, blockUpdate);

                //listen to packet update sign
                PacketListener packetListener = new PacketAdapter(plugin, PacketType.Play.Client.UPDATE_SIGN) {
                    @Override
                    public void onPacketReceiving(PacketEvent event) {
                        Player ePlayer = event.getPlayer();
                        if (!ePlayer.getName().equals(player.getName())) return;

                        PacketContainer packet = event.getPacket();
                        String[] lines = packet.getStringArrays().read(0);
                        protocolManager.removePacketListener(this);

                        if (!lines[0].isEmpty()) {
                            double amount = parseAmount(lines[0]);
                            if (amount <= 0) {
                                player.sendMessage(ChatColor.RED + "You must enter a valid number!");
                                Bank.this.waitingForSignData.remove(player.getUniqueId());
                            } else {
                                Bank.this.customAmount.put(player.getUniqueId(), amount);
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "You must enter a valid number!");
                            Bank.this.waitingForSignData.remove(player.getUniqueId());
                        }
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (menuToOpen.equalsIgnoreCase("deposit")) {
                                    Bank.this.createDepositMenu(player, Bank.this.plugin.players.get(player.getUniqueId()));
                                } else if (menuToOpen.equalsIgnoreCase("withdraw")) {
                                    Bank.this.createWithdrawMenu(player, Bank.this.plugin.players.get(player.getUniqueId()));
                                }
                            }
                        }.runTaskLater(plugin, 1L);
                    }
                };
                protocolManager.addPacketListener(packetListener);
            }
        }.runTaskLater(plugin,1L);
    }
    private double parseAmount(String input) {
        input = input.trim();
        int mult = 1;
        if (input.substring(input.length()-1).equalsIgnoreCase("k")) {
            mult = 1000;
            input = input.substring(0,input.length()-1);
        } else if (input.substring(input.length()-1).equalsIgnoreCase("m")) {
            mult = 1000000;
            input = input.substring(0,input.length()-1);
        }
        try {
            return Double.parseDouble(input) * mult;
        } catch (NumberFormatException e) {
            return -1;
        }

    }

    private void purchaseBankUpgrade(Player player) {
        MMOPlayer mmoPlayer = plugin.players.get(player.getUniqueId());
        int cost = 0;
        if (mmoPlayer.mobileBankLvl == 0) {
            cost = 250;
        } else if (mmoPlayer.mobileBankLvl == 1) {
            cost = 500;
        } else if (mmoPlayer.mobileBankLvl == 2) {
            cost = 1000;
        } else if (mmoPlayer.mobileBankLvl == 3) {
            cost = 2000;
        }
        if (mmoPlayer.getPurseGold() >= cost) {
            mmoPlayer.removePurseGold(cost);
            mmoPlayer.mobileBankLvl++;
            if (mmoPlayer.mobileBankLvl < 4) {
                player.sendMessage(ChatColor.GREEN + "You upgraded your " + ChatColor.YELLOW + "Mobile Banking " + ChatColor.GREEN + "to " + ChatColor.YELLOW + "Level " + Xp.intToRoman(mmoPlayer.mobileBankLvl) + ChatColor.GREEN + "!");
            } else {
                player.sendMessage(ChatColor.GREEN + "You upgraded your " + ChatColor.YELLOW + "Mobile Banking " + ChatColor.GREEN + "to " + ChatColor.AQUA + ChatColor.BOLD + "MAX LEVEL" + ChatColor.RESET + ChatColor.GREEN + "!");
            }
            plugin.shopHandler.playBuySound(player);
            this.loadBank(player, this.accessingRemotely.contains(player.getUniqueId()), this.pageForPlayer.get(player.getUniqueId()));
        } else {
            player.sendMessage(ChatColor.RED + "You don't have enough Dosh!");
            plugin.shopHandler.playNoMoneySound(player);
        }
    }

    private List<String> getTransactionHistory(MMOPlayer mmoPlayer, int page) {
        int startIndex = page * 10;
        List<String> retList = new ArrayList<>();
        if (mmoPlayer.transactionHistory.size() < 1) {
            retList.add(ChatColor.GRAY + "No transactions made");
            return retList;
        }

        for (int i = startIndex; i < startIndex + 10; i++) {
            if (i < mmoPlayer.transactionHistory.size()) {
                String ledger = "";
                BankTransaction transaction = mmoPlayer.transactionHistory.get(i);
                if (transaction.amount < 0) {
                    ledger = ledger + ChatColor.RED + "- ";
                } else {
                    ledger = ledger + ChatColor.GREEN + "+ ";
                }
                ledger = ledger + ChatColor.GOLD + getMoneyString(Math.abs(transaction.amount)) + ChatColor.GRAY + ", ";
                ledger = ledger + ChatColor.YELLOW + formatTime(transaction.time) + ChatColor.GRAY + " by " + transaction.cause;
                retList.add(ledger);
            }
        }

        retList.add(" ");
        if (startIndex != 0) {
            retList.add(ChatColor.YELLOW + "▲ Right click to view more recent transactions.");

        }
        if (mmoPlayer.transactionHistory.size() >= startIndex+10) {
            retList.add(ChatColor.YELLOW + "▼ Click to view earlier transactions.");
        }
        return retList;
    }

    private String formatTime(long time) {
        time = System.currentTimeMillis() - time;
        long hours = TimeUnit.MILLISECONDS.toHours(time);
        String retString = "";
        if (hours >= 1) {
            retString = String.format("%d", hours);
            if (hours >=2) {
                return retString + " hours ago";
            } else {
                return retString + " hour ago";
            }
        }

        long mins = TimeUnit.MILLISECONDS.toMinutes(time);
        if (mins >= 1) {
            retString = String.format("%d", mins);
            if (mins >= 2) {
                return retString + " mins ago";
            } else {
                return retString + " min ago";
            }
        }

        long secs = TimeUnit.MILLISECONDS.toSeconds(time);
        if (secs >= 1) {
            retString = String.format("%d", secs);
            if (secs >=2) {
                return retString + " secs ago";
            } else {
                return retString + " sec ago";
            }
        }
        return "Moments ago";

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

}
