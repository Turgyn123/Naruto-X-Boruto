package net.narutoxboruto.menu;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.narutoxboruto.networking.jutsu.JutsuStorageMenu;

import static net.narutoxboruto.main.Main.MOD_ID;

public class ForgeMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MOD_ID);

    public static final RegistryObject<MenuType<JutsuStorageMenu>> JUTSU_STORAGE =
            MENUS.register("jutsu_storage", () -> IForgeMenuType.create(JutsuStorageMenu::new));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
