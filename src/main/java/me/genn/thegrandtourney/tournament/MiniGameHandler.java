package me.genn.thegrandtourney.tournament;

import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.util.IHandler;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.util.*;

public class MiniGameHandler implements IHandler {
    public Map<String, MiniGame> games = new HashMap<>();
    public List<MiniGame> gameList = new ArrayList<>();
    TGT plugin;

    public MiniGameHandler(TGT plugin) {
        this.plugin = plugin;
    }
    @Override
    public void register(YamlConfiguration configuration) throws IOException {
        ConfigurationSection section = configuration.getConfigurationSection("games");
        Iterator var4 = section.getKeys(false).iterator();
        while(var4.hasNext()) {
            String key = (String)var4.next();
            if (configuration.getBoolean("games." + key + ".enabled", true)) {
                MiniGame game = new MiniGame(plugin, key, configuration.getConfigurationSection("games." + key));
                this.games.put(key, game);
                this.gameList.add(game);
            }
        }
    }
}
