package me.genn.thegrandtourney.skills;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface TournamentZone {
    public void spawn(Player player);

    public void paste(Location minLoc, Location maxLoc);

    public void remove();
}
