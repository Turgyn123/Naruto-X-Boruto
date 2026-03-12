package net.narutoxboruto.items;

import net.minecraft.world.item.Item;
import net.narutoxboruto.main.Main;
import net.narutoxboruto.items.throwables.FumaShurikenItem;
import net.narutoxboruto.items.throwables.ThrowableWeaponItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NeoForgeItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Main.MOD_ID);

    //Throwables
    public static final DeferredItem<Item> SHURIKEN = ITEMS.register("shuriken", () -> new ThrowableWeaponItem(new Item.Properties().stacksTo(64), "shuriken"));
    public static final DeferredItem<Item> KUNAI = ITEMS.register("kunai", () -> new ThrowableWeaponItem(new Item.Properties().stacksTo(64), "kunai"));
    public static final DeferredItem<Item> EXPLOSIVE_KUNAI = ITEMS.register("explosive_kunai", () -> new ThrowableWeaponItem(new Item.Properties().stacksTo(16), "explosive_kunai"));
    public static final DeferredItem<Item> SENBON = ITEMS.register("senbon", () -> new ThrowableWeaponItem(new Item.Properties().stacksTo(64), "senbon"));
    public static final DeferredItem<Item> POISON_SENBON = ITEMS.register("poison_senbon", () -> new ThrowableWeaponItem(new Item.Properties().stacksTo(64), "poison_senbon"));
    public static final DeferredItem<Item> FUMA_SHURIKEN = ITEMS.register("fuma_shuriken", () -> new FumaShurikenItem(new Item.Properties().stacksTo(1),"fuma_shuriken"));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
