//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package me.genn.thegrandtourney.util.fake;

import com.ernestorb.tablistmanager.utils.FakePlayerUtil;
import java.util.UUID;

public class FakePlayer {
    private final TablistAddFakePlayerPacket tablistAddPacket;
    private final TablistRemoveFakePlayerPacket tablistRemovePacket;


    public FakePlayer(String name, String displayName) {
        UUID fakeUUID = UUID.randomUUID();
        this.tablistAddPacket = new TablistAddFakePlayerPacket(fakeUUID, name, displayName);
        this.tablistRemovePacket = new TablistRemoveFakePlayerPacket(fakeUUID, name, displayName);

    }
    public FakePlayer(String name, String displayName, String texture) {
        UUID fakeUUID = UUID.randomUUID();
        this.tablistAddPacket = new TablistAddFakePlayerPacket(fakeUUID, name, displayName, texture);
        this.tablistRemovePacket = new TablistRemoveFakePlayerPacket(fakeUUID, name, displayName, texture);
    }

    public FakePlayer(String name) {
        this(name, " ");
    }

    public TablistAddFakePlayerPacket getTablistAddPacket() {
        return this.tablistAddPacket;
    }

    public TablistRemoveFakePlayerPacket getTablistRemovePacket() {
        return this.tablistRemovePacket;
    }

    public static FakePlayer randomFakePlayer() {
        return new FakePlayer(FakePlayerUtil.randomName());
    }
    public static FakePlayer randomFakePlayer(String texture) {
        return new FakePlayer(FakePlayerUtil.randomName(), " ", texture);
    }
}
