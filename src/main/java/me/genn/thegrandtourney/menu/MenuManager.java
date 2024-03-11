package me.genn.thegrandtourney.menu;

import me.genn.thegrandtourney.TGT;
import org.bukkit.entity.Player;

public class MenuManager {
    private Home homeMenu;
    private AccessoryBag accessoryBagMenu;
    private Storage storageMenu;
    private QuestLog questLogMenu;
    TGT plugin;

    public MenuManager(TGT plugin) {
        this.plugin = plugin;
        this.homeMenu = new Home(plugin);
        this.accessoryBagMenu = new AccessoryBag(plugin);
        this.storageMenu = new Storage(plugin);
        this.questLogMenu = new QuestLog(plugin);
    }

    public void openHomeMenu(Player player) {
        this.homeMenu.loadMenuHome(player);
    }

    public void openAccessoryBag(Player player) {
        this.accessoryBagMenu.loadAccessoryBag(player);
    }

    public void openStorage(Player player) {
        this.storageMenu.loadStorageMenu(player, 0);
    }

    public void openQuestLog(Player player) {this.questLogMenu.loadMenuQuestLog(player, 0, false);}
}
