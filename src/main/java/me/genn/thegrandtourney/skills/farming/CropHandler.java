package me.genn.thegrandtourney.skills.farming;

import java.io.IOException;
import java.util.*;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import me.genn.thegrandtourney.TGT;
import me.genn.thegrandtourney.skills.mining.Ore;
import me.genn.thegrandtourney.util.IHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class CropHandler implements IHandler {

    public List<CropTemplate> allCrops;

    public List<Crop> allSpawnedCrops;
    TGT plugin;

    public CropHandler(TGT plugin) {
        this.plugin = plugin;
        this.allCrops = new ArrayList<>();
        this.allSpawnedCrops = new ArrayList<>();
    }
    public void registerCropTemplates(ConfigurationSection config) throws IOException {

        Iterator var4 = config.getKeys(false).iterator();
        while(var4.hasNext()) {
            String key = (String)var4.next();
            CropTemplate crop = CropTemplate.create(config.getConfigurationSection(key));
            if (crop != null) {
                this.allCrops.add(crop);
            } else {
                plugin.getLogger().severe("Crop Template " + key + " was empty!");
            }
        }

    }
    public boolean containsName(final List<CropTemplate> list, final String name){
        return list.stream().map(CropTemplate::getName).filter(name::equals).findFirst().isPresent();
    }

    public void getHeadFrom64(String value, Skull skull) {
        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
        profile.setProperty(new ProfileProperty("textures", value));
        skull.setPlayerProfile(profile);
        List<BlockFace> facesList = new ArrayList<>(List.of(BlockFace.values()));
        facesList.remove(BlockFace.UP);
        facesList.remove(BlockFace.DOWN);
        facesList.remove(BlockFace.SELF);
        Random r = new Random();
        skull.setRotation(facesList.get(r.nextInt(facesList.size())));
        skull.update();

    }

    private List<Crop> listOfCropsWithTemplateName(final List<Crop> list, final String name){
        return list.stream().filter(o -> o.getName().equals(name)).toList();
    }

    private Crop getClosestCrop(List<Crop> crops, Location originLoc) {
        Crop crop = crops.get(0);
        Location minLoc = crop.loc;
        for (int i = 1; i<crops.size(); i++) {
            if (crops.get(i).loc.distanceSquared(originLoc) < minLoc.distanceSquared(originLoc)) {
                minLoc = crops.get(i).loc;
                crop = crops.get(i);
            }
        }
        return crop;
    }

    public Crop getCropForObj(String name, Location originLoc) {
        List<Crop> crops = listOfCropsWithTemplateName(allSpawnedCrops, name);
        if (crops.size() == 0) {
            return null;
        } else if (crops.size() == 1) {
            return crops.get(0);
        } else {
            Crop crop = getClosestCrop(crops, originLoc);
            return crop;
        }
    }

    @Override
    public void register(YamlConfiguration configuration) throws IOException {
        this.registerCropTemplates(configuration.getConfigurationSection("crops"));
    }
}
