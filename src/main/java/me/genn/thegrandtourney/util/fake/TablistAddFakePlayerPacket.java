//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package me.genn.thegrandtourney.util.fake;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.ernestorb.tablistmanager.loaders.ConfigLoader;
import com.ernestorb.tablistmanager.utils.VersionUtil;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class TablistAddFakePlayerPacket extends FakePlayerPacket {
    protected TablistAddFakePlayerPacket(UUID uuid, String playerName, String displayText) {
        super(uuid, playerName, displayText);
        WrappedGameProfile gameProfile = new WrappedGameProfile(uuid, playerName);
        PacketContainer packet = this.getProtocolManager().createPacket(Server.PLAYER_INFO);
        this.setPacket(packet);

        PlayerInfoData data = new PlayerInfoData(FakePlayerPacket.changeGameProfileSkin(gameProfile), ConfigLoader.getDefaultLatency().getLatency() + 1, NativeGameMode.CREATIVE, WrappedChatComponent.fromText(displayText));
        List<PlayerInfoData> infoLists = Collections.singletonList(data);
        if (VersionUtil.isNewTablist()) {
            EnumSet<EnumWrappers.PlayerInfoAction> actions = EnumSet.of(PlayerInfoAction.ADD_PLAYER, PlayerInfoAction.UPDATE_LATENCY, PlayerInfoAction.UPDATE_LISTED, PlayerInfoAction.UPDATE_DISPLAY_NAME);
            packet.getPlayerInfoActions().write(0, actions);
            packet.getPlayerInfoDataLists().write(1, infoLists);
        } else {
            packet.getPlayerInfoAction().write(0, PlayerInfoAction.ADD_PLAYER);
            packet.getPlayerInfoDataLists().write(0, infoLists);
        }
    }
    protected TablistAddFakePlayerPacket(UUID uuid, String playerName, String displayText, String texture) {
        super(uuid, playerName, displayText);
        WrappedGameProfile gameProfile = new WrappedGameProfile(uuid, playerName);
        PacketContainer packet = this.getProtocolManager().createPacket(Server.PLAYER_INFO);
        this.setPacket(packet);

        PlayerInfoData data = new PlayerInfoData(FakePlayerPacket.changeGameProfileSkin(gameProfile, texture), ConfigLoader.getDefaultLatency().getLatency() + 1, NativeGameMode.CREATIVE, WrappedChatComponent.fromText(displayText));
        List<PlayerInfoData> infoLists = Collections.singletonList(data);
        if (VersionUtil.isNewTablist()) {
            EnumSet<EnumWrappers.PlayerInfoAction> actions = EnumSet.of(PlayerInfoAction.ADD_PLAYER, PlayerInfoAction.UPDATE_LATENCY, PlayerInfoAction.UPDATE_LISTED, PlayerInfoAction.UPDATE_DISPLAY_NAME);
            packet.getPlayerInfoActions().write(0, actions);
            packet.getPlayerInfoDataLists().write(1, infoLists);
        } else {
            packet.getPlayerInfoAction().write(0, PlayerInfoAction.ADD_PLAYER);
            packet.getPlayerInfoDataLists().write(0, infoLists);
        }
    }
}
