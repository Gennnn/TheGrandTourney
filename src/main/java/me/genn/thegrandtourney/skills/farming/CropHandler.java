package me.genn.thegrandtourney.skills.farming;

import java.io.IOException;
import java.util.*;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import me.genn.thegrandtourney.TGT;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class CropHandler {

    public List<CropTemplate> allCrops;



    public CropHandler() {
    }
    public void registerCropTemplates(TGT plugin, ConfigurationSection config) throws IOException {
        this.allCrops = new ArrayList<>();
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
}
