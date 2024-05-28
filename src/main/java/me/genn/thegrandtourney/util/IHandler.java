package me.genn.thegrandtourney.util;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;

public interface IHandler {
    public void register(YamlConfiguration configuration) throws IOException;
}
