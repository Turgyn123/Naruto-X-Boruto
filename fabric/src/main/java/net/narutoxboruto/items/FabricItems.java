package net.narutoxboruto.items;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.SwordItem;
import net.narutoxboruto.entities.FabricEntities;
import net.narutoxboruto.items.jutsus.*;
import net.narutoxboruto.items.misc.ChakraPaper;
import net.narutoxboruto.items.misc.ClanReroll;
import net.narutoxboruto.items.misc.RandomDna;
import net.narutoxboruto.items.misc.ReleaseDnaBottleItem;
import net.narutoxboruto.items.scrolls.*;
import net.narutoxboruto.items.swords.*;
import net.narutoxboruto.items.throwables.FumaShurikenItem;
import net.narutoxboruto.items.throwables.ThrowableWeaponItem;
import net.narutoxboruto.main.Main;

public class FabricItems {

    //Throwables
    public static final Item SHURIKEN = register("shuriken", new ThrowableWeaponItem(new Item.Properties().stacksTo(64), "shuriken"));
    public static final Item KUNAI = register("kunai", new ThrowableWeaponItem(new Item.Properties().stacksTo(64), "kunai"));
    public static final Item EXPLOSIVE_KUNAI = register("explosive_kunai", new ThrowableWeaponItem(new Item.Properties().stacksTo(16), "explosive_kunai"));
    public static final Item SENBON = register("senbon", new ThrowableWeaponItem(new Item.Properties().stacksTo(64), "senbon"));
    public static final Item POISON_SENBON = register("poison_senbon", new ThrowableWeaponItem(new Item.Properties().stacksTo(64), "poison_senbon"));
    public static final Item FUMA_SHURIKEN = register("fuma_shuriken", new FumaShurikenItem(new Item.Properties().stacksTo(1), "fuma_shuriken"));

    //Swords
    public static final Item SAMEHADA = register("samehada", new Samehada(new Item.Properties().stacksTo(1).attributes(SwordItem.createAttributes(SwordCustomTiers.SAMEHADA, 2, -2.5f))));
    public static final Item KUBIKIRIBOCHO = register("kubikiribocho", new Kubikiribocho(new Item.Properties().stacksTo(1).attributes(SwordItem.createAttributes(SwordCustomTiers.KUBIKIRIBOCHO, 6, -3f))));
    public static final Item SHIBUKI = register("shibuki", new Shibuki(new Item.Properties().stacksTo(1).attributes(SwordItem.createAttributes(SwordCustomTiers.SHIBUKI, 2, -2f))));
    public static final Item NUIBARI = register("nuibari", new Nuibari(new Item.Properties().stacksTo(1).attributes(SwordItem.createAttributes(SwordCustomTiers.NUIBARI, 2, -1.5f))));
    public static final Item KABUTOWARI = register("kabutowari", new Kabutowari(new Item.Properties().stacksTo(1).attributes(SwordItem.createAttributes(SwordCustomTiers.KABUTOWARI, 6, -3.0f))));
    public static final Item KIBA = register("kiba", new Kiba(new Item.Properties().stacksTo(1).attributes(SwordItem.createAttributes(SwordCustomTiers.KIBA, 4, -1.5f))));

    //Misc
    public static final Item CHAKRA_PAPER = register("chakra_paper", new ChakraPaper(new Item.Properties().stacksTo(1)));
    public static final Item CLAN_REROLL = register("clan_reroll", new ClanReroll(new Item.Properties().stacksTo(8)));

    //Scrolls
    public static final Item TAIJUTSU_SCROLL = register("taijutsu_scroll", new TaijutsuScroll(new Item.Properties().stacksTo(8)));
    public static final Item GENJUTSU_SCROLL = register("genjutsu_scroll", new GenjutsuScroll(new Item.Properties().stacksTo(8)));
    public static final Item KENJUTSU_SCROLL = register("kenjutsu_scroll", new KenjutsuScroll(new Item.Properties().stacksTo(8)));
    public static final Item KINJUTSU_SCROLL = register("kinjutsu_scroll", new KinjutsuScroll(new Item.Properties().stacksTo(8)));
    public static final Item MEDICAL_SCROLL = register("medical_scroll", new MedicalScroll(new Item.Properties().stacksTo(8)));
    public static final Item NINJUTSU_SCROLL = register("ninjutsu_scroll", new NinjutsuScroll(new Item.Properties().stacksTo(8)));
    public static final Item SENJUTSU_SCROLL = register("senjutsu_scroll", new SenjutsuScroll(new Item.Properties().stacksTo(8)));
    public static final Item SHURIKENJUTSU_SCROLL = register("shurikenjutsu_scroll", new ShurikenjutsuScroll(new Item.Properties().stacksTo(8)));
    public static final Item SPEED_SCROLL = register("speed_scroll", new SpeedScroll(new Item.Properties().stacksTo(8)));
    public static final Item SUMMONING_SCROLL = register("summoning_scroll", new SummoningScroll(new Item.Properties().stacksTo(8)));

    //DNA Bottles
    public static final Item RANDOM_DNA = register("random_dna", new RandomDna(new Item.Properties().stacksTo(1)));
    public static final Item FIRE_DNA = register("fire_dna", new ReleaseDnaBottleItem(new Item.Properties().stacksTo(1), "fire"));
    public static final Item EARTH_DNA = register("earth_dna", new ReleaseDnaBottleItem(new Item.Properties().stacksTo(1), "earth"));
    public static final Item LIGHTNING_DNA = register("lightning_dna", new ReleaseDnaBottleItem(new Item.Properties().stacksTo(1), "lightning"));
    public static final Item WATER_DNA = register("water_dna", new ReleaseDnaBottleItem(new Item.Properties().stacksTo(1), "water"));
    public static final Item WIND_DNA = register("wind_dna", new ReleaseDnaBottleItem(new Item.Properties().stacksTo(1), "wind"));
    public static final Item YIN_DNA = register("yin_dna", new ReleaseDnaBottleItem(new Item.Properties().stacksTo(1), "yin"));
    public static final Item YANG_DNA = register("yang_dna", new ReleaseDnaBottleItem(new Item.Properties().stacksTo(1), "yang"));

    //Jutsu Items
    public static final Item FIRE_BALL_JUTSU = register("fire_ball_jutsu", new FireBall(new Item.Properties().stacksTo(1)));
    public static final Item SHARK_BOMB_JUTSU = register("shark_bomb_jutsu", new SharkBomb(new Item.Properties().stacksTo(1)));
    public static final Item WATER_DRAGON_JUTSU = register("water_dragon_jutsu", new WaterDragon(new Item.Properties().stacksTo(1)));
    public static final Item EARTH_WALL_JUTSU = register("earth_wall_jutsu", new EarthWall(new Item.Properties().stacksTo(1)));
    public static final Item EARTH_WAVE_JUTSU = register("earth_wave_jutsu", new EarthWave(new Item.Properties().stacksTo(1)));
    public static final Item WATER_PRISON_JUTSU = register("water_prison_jutsu", new WaterPrison(new Item.Properties().stacksTo(1)));
    public static final Item LIGHTNING_CHAKRA_MODE = register("lightning_chakra_mode", new LightningChakraMode(new Item.Properties().stacksTo(1)));

    //Spawn Eggs
    public static final Item KISAME_SPAWN_EGG = register("kisame_hoshigaki_egg", new SpawnEggItem(FabricEntities.KISAME_HOSHIGAKI, 0x14161E, 14278624, new Item.Properties()));
    public static final Item ZABUZA_SPAWN_EGG = register("zabuza_momochi_egg", new SpawnEggItem(FabricEntities.ZABUZA_MOMOCHI, 4342344, 13948116, new Item.Properties()));
    public static final Item JINPACHI_SPAWN_EGG = register("jinpachi_munashi_egg", new SpawnEggItem(FabricEntities.JINPACHI_MUNASHI, 12758635, 5000268, new Item.Properties()));

    private static Item register(String name, Item item) {
        return Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, name), item);
    }

    public static void register() {
        // Class load triggers static field registration
    }
}
