//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package me.genn.thegrandtourney.tournament;

import me.genn.thegrandtourney.TGT;

public class MiniGameMonitor implements Runnable {
    TGT plugin;

    public MiniGameMonitor(TGT plugin) {
        this.plugin = plugin;
    }

    public void run() {
        if (this.plugin.currentGame != null) {
            if (this.plugin.currentGame.isEnded()) {
                this.plugin.nextGame();
            } else {
                double var1 = this.plugin.currentGame.checkPercentTimeRemaining();
            }
        }

    }
}
