package net.narutoxboruto.items;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.narutoxboruto.items.throwables.FumaShurikenItem;
import net.narutoxboruto.items.throwables.ThrowableWeaponItem;

import static net.narutoxboruto.main.Main.MOD_ID;

public class ForgeItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MOD_ID);

    //Throwables
    public static final RegistryObject<Item> SHURIKEN = ITEMS.register("shuriken", () -> new ThrowableWeaponItem(new Item.Properties().stacksTo(64), "shuriken"));
    public static final RegistryObject<Item> KUNAI = ITEMS.register("kunai", () -> new ThrowableWeaponItem(new Item.Properties().stacksTo(64), "kunai"));
    public static final RegistryObject<Item> EXPLOSIVE_KUNAI = ITEMS.register("explosive_kunai", () -> new ThrowableWeaponItem(new Item.Properties().stacksTo(16), "explosive_kunai"));
    public static final RegistryObject<Item> SENBON = ITEMS.register("senbon", () -> new ThrowableWeaponItem(new Item.Properties().stacksTo(64), "senbon"));
    public static final RegistryObject<Item> POISON_SENBON = ITEMS.register("poison_senbon", () -> new ThrowableWeaponItem(new Item.Properties().stacksTo(64), "poison_senbon"));
    public static final RegistryObject<Item> FUMA_SHURIKEN = ITEMS.register("fuma_shuriken", () -> new FumaShurikenItem(new Item.Properties().stacksTo(1),"fuma_shuriken"));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
