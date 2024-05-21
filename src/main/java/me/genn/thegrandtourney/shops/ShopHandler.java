//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package me.genn.thegrandtourney.shops;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import me.genn.thegrandtourney.TGT;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class ShopHandler {
    public String soundSale;
    public String soundNoMoney;
    Map<String, ShopItem> items;
    public Map<String, Shop> shops;
    public InvShop invShops;
    public float buyPitch = 1.0f;
    public float buyVolume = 1.0f;
    public float noMoneyPitch = 1.0f;
    public float noMoneyVolume = 1.0f;
    TGT plugin;

    public ShopHandler(TGT plugin) {
        this.plugin = plugin;
        this.invShops = new InvShop(plugin);
    }

    public void registerShops(ConfigurationSection config) {
        this.items = new HashMap<>();
        this.shops = new HashMap<>();

        this.soundSale = config.getString("sound-sale", "");
        this.soundNoMoney = config.getString("sound-no-money", "");
        this.buyPitch = (float)config.getDouble("sound-sale-pitch", 1.0f);
        this.buyVolume = (float)config.getDouble("sound-sale-volume", 1.0f);
        this.noMoneyPitch = (float)config.getDouble("sound-no-money-pitch", 1.0f);
        this.noMoneyVolume = (float)config.getDouble("sound-no-money-volume", 1.0f);
        ConfigurationSection itemsSection = config.getConfigurationSection("items");
        Iterator<String> var5 = itemsSection.getKeys(false).iterator();

        while(var5.hasNext()) {
            String itemKey = (String)var5.next();
            ShopItem item = ShopItem.create(itemsSection.getConfigurationSection(itemKey));
            if (item != null) {
                this.items.put(itemKey, item);
            } else {
                System.out.println("NULL RETURNED " + itemKey);
            }
        }

        ConfigurationSection shopsSection = config.getConfigurationSection("shops");
        Iterator<String> var10 = shopsSection.getKeys(false).iterator();

        while(var10.hasNext()) {
            String shopKey = (String)var10.next();
            Shop shop = Shop.create(shopsSection.getConfigurationSection(shopKey), this.items, plugin);
            if (shop != null) {
                this.shops.put(shopKey, shop);
            }
        }

    }

    public void playBuySound(Player player) {
        player.playSound(player, this.soundSale, this.buyVolume, this.buyPitch);
    }
    public void playNoMoneySound(Player player) {
        player.playSound(player, this.soundNoMoney, this.noMoneyVolume, this.noMoneyPitch);
    }
}
