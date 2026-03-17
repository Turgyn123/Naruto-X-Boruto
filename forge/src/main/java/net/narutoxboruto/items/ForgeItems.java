package net.narutoxboruto.items;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.narutoxboruto.entities.ForgeEntities;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.narutoxboruto.items.jutsus.*;
import net.narutoxboruto.items.misc.ChakraPaper;
import net.narutoxboruto.items.misc.ClanReroll;
import net.narutoxboruto.items.misc.RandomDna;
import net.narutoxboruto.items.misc.ReleaseDnaBottleItem;
import net.narutoxboruto.items.scrolls.*;
import net.narutoxboruto.items.swords.*;
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
    public static final RegistryObject<Item> FUMA_SHURIKEN = ITEMS.register("fuma_shuriken", () -> new FumaShurikenItem(new Item.Properties().stacksTo(1), "fuma_shuriken"));

    //Swords
    public static final RegistryObject<Item> SAMEHADA = ITEMS.register("samehada", () -> new Samehada(new Item.Properties().stacksTo(1).attributes(SwordItem.createAttributes(SwordCustomTiers.SAMEHADA, 2, -2.5f))));
    public static final RegistryObject<Item> KUBIKIRIBOCHO = ITEMS.register("kubikiribocho", () -> new Kubikiribocho(new Item.Properties().stacksTo(1).attributes(SwordItem.createAttributes(SwordCustomTiers.KUBIKIRIBOCHO, 6, -3f))));
    public static final RegistryObject<Item> SHIBUKI = ITEMS.register("shibuki", () -> new Shibuki(new Item.Properties().stacksTo(1).attributes(SwordItem.createAttributes(SwordCustomTiers.SHIBUKI, 2, -2f))));
    public static final RegistryObject<Item> NUIBARI = ITEMS.register("nuibari", () -> new Nuibari(new Item.Properties().stacksTo(1).attributes(SwordItem.createAttributes(SwordCustomTiers.NUIBARI, 2, -1.5f))));
    public static final RegistryObject<Item> KABUTOWARI = ITEMS.register("kabutowari", () -> new Kabutowari(new Item.Properties().stacksTo(1).attributes(SwordItem.createAttributes(SwordCustomTiers.KABUTOWARI, 6, -3.0f))));
    public static final RegistryObject<Item> KIBA = ITEMS.register("kiba", () -> new Kiba(new Item.Properties().stacksTo(1).attributes(SwordItem.createAttributes(SwordCustomTiers.KIBA, 4, -1.5f))));

    //Misc
    public static final RegistryObject<Item> CHAKRA_PAPER = ITEMS.register("chakra_paper", () -> new ChakraPaper(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CLAN_REROLL = ITEMS.register("clan_reroll", () -> new ClanReroll(new Item.Properties().stacksTo(8)));

    //Scrolls
    public static final RegistryObject<Item> TAIJUTSU_SCROLL = ITEMS.register("taijutsu_scroll", () -> new TaijutsuScroll(new Item.Properties().stacksTo(8)));
    public static final RegistryObject<Item> GENJUTSU_SCROLL = ITEMS.register("genjutsu_scroll", () -> new GenjutsuScroll(new Item.Properties().stacksTo(8)));
    public static final RegistryObject<Item> KENJUTSU_SCROLL = ITEMS.register("kenjutsu_scroll", () -> new KenjutsuScroll(new Item.Properties().stacksTo(8)));
    public static final RegistryObject<Item> KINJUTSU_SCROLL = ITEMS.register("kinjutsu_scroll", () -> new KinjutsuScroll(new Item.Properties().stacksTo(8)));
    public static final RegistryObject<Item> MEDICAL_SCROLL = ITEMS.register("medical_scroll", () -> new MedicalScroll(new Item.Properties().stacksTo(8)));
    public static final RegistryObject<Item> NINJUTSU_SCROLL = ITEMS.register("ninjutsu_scroll", () -> new NinjutsuScroll(new Item.Properties().stacksTo(8)));
    public static final RegistryObject<Item> SENJUTSU_SCROLL = ITEMS.register("senjutsu_scroll", () -> new SenjutsuScroll(new Item.Properties().stacksTo(8)));
    public static final RegistryObject<Item> SHURIKENJUTSU_SCROLL = ITEMS.register("shurikenjutsu_scroll", () -> new ShurikenjutsuScroll(new Item.Properties().stacksTo(8)));
    public static final RegistryObject<Item> SPEED_SCROLL = ITEMS.register("speed_scroll", () -> new SpeedScroll(new Item.Properties().stacksTo(8)));
    public static final RegistryObject<Item> SUMMONING_SCROLL = ITEMS.register("summoning_scroll", () -> new SummoningScroll(new Item.Properties().stacksTo(8)));

    //DNA Bottles
    public static final RegistryObject<Item> RANDOM_DNA = ITEMS.register("random_dna", () -> new RandomDna(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> FIRE_DNA = ITEMS.register("fire_dna", () -> new ReleaseDnaBottleItem(new Item.Properties().stacksTo(1), "fire"));
    public static final RegistryObject<Item> EARTH_DNA = ITEMS.register("earth_dna", () -> new ReleaseDnaBottleItem(new Item.Properties().stacksTo(1), "earth"));
    public static final RegistryObject<Item> LIGHTNING_DNA = ITEMS.register("lightning_dna", () -> new ReleaseDnaBottleItem(new Item.Properties().stacksTo(1), "lightning"));
    public static final RegistryObject<Item> WATER_DNA = ITEMS.register("water_dna", () -> new ReleaseDnaBottleItem(new Item.Properties().stacksTo(1), "water"));
    public static final RegistryObject<Item> WIND_DNA = ITEMS.register("wind_dna", () -> new ReleaseDnaBottleItem(new Item.Properties().stacksTo(1), "wind"));
    public static final RegistryObject<Item> YIN_DNA = ITEMS.register("yin_dna", () -> new ReleaseDnaBottleItem(new Item.Properties().stacksTo(1), "yin"));
    public static final RegistryObject<Item> YANG_DNA = ITEMS.register("yang_dna", () -> new ReleaseDnaBottleItem(new Item.Properties().stacksTo(1), "yang"));

    //Jutsu Items
    public static final RegistryObject<Item> FIRE_BALL_JUTSU = ITEMS.register("fire_ball_jutsu", () -> new FireBall(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SHARK_BOMB_JUTSU = ITEMS.register("shark_bomb_jutsu", () -> new SharkBomb(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> WATER_DRAGON_JUTSU = ITEMS.register("water_dragon_jutsu", () -> new WaterDragon(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> EARTH_WALL_JUTSU = ITEMS.register("earth_wall_jutsu", () -> new EarthWall(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> EARTH_WAVE_JUTSU = ITEMS.register("earth_wave_jutsu", () -> new EarthWave(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> WATER_PRISON_JUTSU = ITEMS.register("water_prison_jutsu", () -> new WaterPrison(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> LIGHTNING_CHAKRA_MODE = ITEMS.register("lightning_chakra_mode", () -> new LightningChakraMode(new Item.Properties().stacksTo(1)));

    //Spawn Eggs
    public static final RegistryObject<Item> KISAME_SPAWN_EGG = ITEMS.register("kisame_hoshigaki_egg", () -> new ForgeSpawnEggItem(ForgeEntities.KISAME_HOSHIGAKI, 0x14161E, 14278624, new Item.Properties()));
    public static final RegistryObject<Item> ZABUZA_SPAWN_EGG = ITEMS.register("zabuza_momochi_egg", () -> new ForgeSpawnEggItem(ForgeEntities.ZABUZA_MOMOCHI, 4342344, 13948116, new Item.Properties()));
    public static final RegistryObject<Item> JINPACHI_SPAWN_EGG = ITEMS.register("jinpachi_munashi_egg", () -> new ForgeSpawnEggItem(ForgeEntities.JINPACHI_MUNASHI, 12758635, 5000268, new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
