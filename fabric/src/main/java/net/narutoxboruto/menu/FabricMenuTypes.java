package net.narutoxboruto.menu;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.narutoxboruto.main.Main;
import net.narutoxboruto.networking.jutsu.JutsuStorageMenu;

public class FabricMenuTypes {

    public static final MenuType<JutsuStorageMenu> JUTSU_STORAGE = Registry.register(
            BuiltInRegistries.MENU,
            ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "jutsu_storage"),
            new MenuType<>(JutsuStorageMenu::new, net.minecraft.world.flag.FeatureFlags.VANILLA_SET)
    );

    public static void register() {
        // Class load triggers static field registration
    }
}
