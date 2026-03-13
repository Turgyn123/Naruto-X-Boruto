package net.narutoxboruto.main.platform;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.narutoxboruto.capabilities.info.Affiliation;
import net.narutoxboruto.main.platform.services.IPlatformHelper;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;

public class ForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {

        return "Forge";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return !FMLLoader.isProduction();
    }

    @Override
    public Affiliation getAffiliation(Player player) {
        return null;
    }

    @Override
    public void syncAffiliation(ServerPlayer player, String value) {

    }
}