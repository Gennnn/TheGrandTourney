package me.genn.thegrandtourney.menu;

import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.npc.StationMaster;
import me.genn.thegrandtourney.npc.StationMasterRecipeBook;
import me.genn.thegrandtourney.xp.XpType;
import org.bukkit.entity.Player;

public class MenuManager {
    private Home homeMenu;
    private AccessoryBag accessoryBagMenu;
    private Storage storageMenu;
    private QuestLog questLogMenu;
    private SkillXpSelector skillMenu;
    private RecipeBook recipeMenu;
    private StationMasterRecipeBook stationMasterRecipeBook;
    TGT plugin;

    public MenuManager(TGT plugin) {
        this.plugin = plugin;
        this.homeMenu = new Home(plugin);
        this.accessoryBagMenu = new AccessoryBag(plugin);
        this.storageMenu = new Storage(plugin);
        this.questLogMenu = new QuestLog(plugin);
        this.skillMenu = new SkillXpSelector(plugin);
        this.recipeMenu = new RecipeBook(plugin);
        this.stationMasterRecipeBook = new StationMasterRecipeBook(plugin);
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

    public void openSkillMenu(Player player) {this.skillMenu.loadSkillSelectionMenu(player);}
    public void openRecipeBook(Player player) {this.recipeMenu.loadMenuRecipeBook(player, 0, XpType.ALL);}
    public void openStationMasterMenu(Player player, XpType type, StationMaster sm) {this.stationMasterRecipeBook.loadMenuRecipeBook(player, 0, type, sm);}
}
