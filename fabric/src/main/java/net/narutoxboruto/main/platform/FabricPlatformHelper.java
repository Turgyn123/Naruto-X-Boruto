package net.narutoxboruto.main.platform;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.narutoxboruto.capabilities.info.Affiliation;
import net.narutoxboruto.main.platform.services.IPlatformHelper;
import net.fabricmc.loader.api.FabricLoader;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public Affiliation getAffiliation(Player player) {
        return null;
    }

    @Override
    public void syncAffiliation(ServerPlayer player, String value) {

    }
}
