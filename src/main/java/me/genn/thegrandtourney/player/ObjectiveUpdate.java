package me.genn.thegrandtourney.player;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class ObjectiveUpdate {
    public List<String> statusUpdate;
    public String locationUpdate;
    public String trackingTextUpdate;
    public boolean completingStep;

    public ObjectiveUpdate() {
        this.statusUpdate = new ArrayList<>();
        this.locationUpdate = "";
        this.trackingTextUpdate = "";
        this.completingStep = false;
    }
    public ObjectiveUpdate(List<String> statusUpdate, String locationUpdate, String trackingTextUpdate, boolean completingStep) {
        this.statusUpdate = statusUpdate;
        this.locationUpdate = locationUpdate;
        this.trackingTextUpdate = trackingTextUpdate;
        this.completingStep = completingStep;
    }


}
