//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package me.genn.thegrandtourney.shops;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import me.genn.thegrandtourney.TGT;
import org.bukkit.configuration.ConfigurationSection;

public class Shop {
    String name;
    List<ShopItem> items;
    String soundSale;
    String soundNoMoney;
    public float buyPitch = 1.0f;
    public float buyVolume = 1.0f;
    public float noMoneyPitch = 1.0f;
    public float noMoneyVolume = 1.0f;

    public Shop() {
    }

    public static Shop create(ConfigurationSection config, Map<String, ShopItem> allItems, TGT plugin) {
        Shop shop = new Shop();
        shop.name = config.getString("name", "SHOP NAME");
        shop.soundSale = config.getString("sound-sale", plugin.shopHandler.soundSale);
        shop.soundNoMoney = config.getString("sound-no-money", plugin.shopHandler.soundNoMoney);
        shop.buyPitch = (float)config.getDouble("sound-sale-pitch", plugin.shopHandler.buyPitch);
        shop.buyVolume = (float)config.getDouble("sound-sale-volume", plugin.shopHandler.buyVolume);
        shop.noMoneyPitch = (float)config.getDouble("sound-no-money-pitch", plugin.shopHandler.noMoneyPitch);
        shop.noMoneyVolume = (float)config.getDouble("sound-no-money-volume", plugin.shopHandler.noMoneyVolume);
        shop.items = new ArrayList();
        List<String> itemNames = config.getStringList("items");
        if (itemNames != null && !itemNames.isEmpty()) {
            Iterator var6 = itemNames.iterator();

            while(var6.hasNext()) {
                String itemName = (String)var6.next();
                ShopItem item = (ShopItem)allItems.get(itemName);
                if (item == null) {
                    System.out.println("INVALID ITEM IN SHOP " + shop.name + ": " + itemName);
                } else {
                    shop.items.add(item);
                }
            }
        } else {
            System.out.println("SHOP " + shop.name + " HAS NO ITEMS");
        }

        return shop;
    }
}
