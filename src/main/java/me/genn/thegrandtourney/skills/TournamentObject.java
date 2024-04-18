package me.genn.thegrandtourney.skills;

import org.bukkit.Location;

public interface TournamentObject {

    public void spawn(Location loc);

    public void remove();

    public void paste(Location loc);


}
