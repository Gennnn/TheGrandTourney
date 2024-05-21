package me.genn.thegrandtourney.util;

import com.comphenix.protocol.events.PacketContainer;
import com.ernestorb.tablistmanager.packets.PlaceholderCallback;
import com.ernestorb.tablistmanager.packets.TablistTemplate;
import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.util.fake.FakePlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TabList {
    TGT plugin;
    public TabList(TGT plugin) {
        this.plugin = plugin;
    }

    /*public void display(Player player) {
        plugin.tablistManager.getTablistHandler().setPlayerTablist(player, Template.getInstance());
    }*/

}

class Template extends TablistTemplate {

    public Template() {
        super((tablistTemplate, player) -> {
        FakePlayer.randomFakePlayer().getTablistAddPacket().sendPacketOnce(player);
        });
    }
    public static Template getInstance() {
        return new Template();
    }
}
