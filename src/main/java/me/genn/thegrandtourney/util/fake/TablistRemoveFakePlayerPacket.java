//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package me.genn.thegrandtourney.util.fake;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.Converters;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.ernestorb.tablistmanager.loaders.ConfigLoader;
import com.ernestorb.tablistmanager.utils.VersionUtil;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.bukkit.GameMode;

public class TablistRemoveFakePlayerPacket extends FakePlayerPacket {
    protected TablistRemoveFakePlayerPacket(UUID uuid, String playerName, String displayText) {
        super(uuid, playerName, displayText);
        PacketContainer packet;
        if (VersionUtil.isNewTablist()) {
            packet = this.getProtocolManager().createPacket(Server.PLAYER_INFO_REMOVE);
            packet.getLists(Converters.passthrough(UUID.class)).write(0, Collections.singletonList(uuid));
            this.setPacket(packet);
        } else {
            packet = this.getProtocolManager().createPacket(Server.PLAYER_INFO);
            packet.getPlayerInfoAction().write(0, PlayerInfoAction.REMOVE_PLAYER);
            List<PlayerInfoData> playerInfoDataList = (List)packet.getPlayerInfoDataLists().writeDefaults().read(0);
            WrappedGameProfile gameProfile = new WrappedGameProfile(uuid, playerName);
            playerInfoDataList.add(new PlayerInfoData(FakePlayerPacket.changeGameProfileSkin(gameProfile), ConfigLoader.getDefaultLatency().getLatency() + 100, NativeGameMode.fromBukkit(GameMode.CREATIVE), WrappedChatComponent.fromText(displayText)));
            packet.getPlayerInfoDataLists().write(0, playerInfoDataList);
            this.setPacket(packet);
        }
    }
    protected TablistRemoveFakePlayerPacket(UUID uuid, String playerName, String displayText, String texture) {
        super(uuid, playerName, displayText);
        PacketContainer packet;
        if (VersionUtil.isNewTablist()) {
            packet = this.getProtocolManager().createPacket(Server.PLAYER_INFO_REMOVE);
            packet.getLists(Converters.passthrough(UUID.class)).write(0, Collections.singletonList(uuid));
            this.setPacket(packet);
        } else {
            packet = this.getProtocolManager().createPacket(Server.PLAYER_INFO);
            packet.getPlayerInfoAction().write(0, PlayerInfoAction.REMOVE_PLAYER);
            List<PlayerInfoData> playerInfoDataList = (List)packet.getPlayerInfoDataLists().writeDefaults().read(0);
            WrappedGameProfile gameProfile = new WrappedGameProfile(uuid, playerName);
            playerInfoDataList.add(new PlayerInfoData(FakePlayerPacket.changeGameProfileSkin(gameProfile,texture), ConfigLoader.getDefaultLatency().getLatency() + 100, NativeGameMode.fromBukkit(GameMode.CREATIVE), WrappedChatComponent.fromText(displayText)));
            packet.getPlayerInfoDataLists().write(0, playerInfoDataList);
            this.setPacket(packet);
        }
    }
}
