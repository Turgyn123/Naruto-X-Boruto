package net.narutoxboruto.items;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.narutoxboruto.main.Main;

public class FabricTab {

    public static final CreativeModeTab THROWABLE = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "throwable"),
            FabricItemGroup.builder()
                    .title(Component.translatable("itemGroup.throwable"))
                    .icon(() -> FabricItems.SHURIKEN.getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(FabricItems.SHURIKEN);
                        output.accept(FabricItems.KUNAI);
                        output.accept(FabricItems.EXPLOSIVE_KUNAI);
                        output.accept(FabricItems.SENBON);
                        output.accept(FabricItems.POISON_SENBON);
                        output.accept(FabricItems.FUMA_SHURIKEN);
                    }).build()
    );

    public static final CreativeModeTab SWORDS = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "swords"),
            FabricItemGroup.builder()
                    .title(Component.translatable("itemGroup.swords"))
                    .icon(() -> FabricItems.SAMEHADA.getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(FabricItems.SAMEHADA);
                        output.accept(FabricItems.KUBIKIRIBOCHO);
                        output.accept(FabricItems.SHIBUKI);
                        output.accept(FabricItems.KIBA);
                        output.accept(FabricItems.KABUTOWARI);
                        //output.accept(FabricItems.NUIBARI);
                    }).build()
    );

    public static final CreativeModeTab NXB_TAB = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "nxb_tab"),
            FabricItemGroup.builder()
                    .title(Component.translatable("itemGroup.nxb_tab"))
                    .icon(() -> FabricItems.CHAKRA_PAPER.getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(FabricItems.CHAKRA_PAPER);
                        output.accept(FabricItems.CLAN_REROLL);
                    }).build()
    );

    public static final CreativeModeTab NXB_DNA = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "nxb_dna"),
            FabricItemGroup.builder()
                    .title(Component.translatable("itemGroup.dna_tab"))
                    .icon(() -> FabricItems.RANDOM_DNA.getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(FabricItems.RANDOM_DNA);
                        output.accept(FabricItems.EARTH_DNA);
                        output.accept(FabricItems.FIRE_DNA);
                        output.accept(FabricItems.WATER_DNA);
                        output.accept(FabricItems.WIND_DNA);
                        output.accept(FabricItems.LIGHTNING_DNA);
                        output.accept(FabricItems.YIN_DNA);
                        output.accept(FabricItems.YANG_DNA);
                    }).build()
    );

    public static final CreativeModeTab NXB_BOSSES = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "nxb_bosses"),
            FabricItemGroup.builder()
                    .title(Component.translatable("itemGroup.bosses_tab"))
                    .icon(() -> FabricItems.ZABUZA_SPAWN_EGG.getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(FabricItems.KISAME_SPAWN_EGG);
                        output.accept(FabricItems.JINPACHI_SPAWN_EGG);
                        output.accept(FabricItems.ZABUZA_SPAWN_EGG);
                    }).build()
    );

    public static final CreativeModeTab NXB_SCROLLS = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "nxb_scroll"),
            FabricItemGroup.builder()
                    .title(Component.translatable("itemGroup.scroll"))
                    .icon(() -> FabricItems.TAIJUTSU_SCROLL.getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(FabricItems.TAIJUTSU_SCROLL);
                        output.accept(FabricItems.GENJUTSU_SCROLL);
                        output.accept(FabricItems.KENJUTSU_SCROLL);
                        output.accept(FabricItems.KINJUTSU_SCROLL);
                        output.accept(FabricItems.MEDICAL_SCROLL);
                        output.accept(FabricItems.NINJUTSU_SCROLL);
                        output.accept(FabricItems.SENJUTSU_SCROLL);
                        output.accept(FabricItems.SHURIKENJUTSU_SCROLL);
                        output.accept(FabricItems.SPEED_SCROLL);
                        output.accept(FabricItems.SUMMONING_SCROLL);
                    }).build()
    );

    public static final CreativeModeTab NXB_JUTSUS = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "nxb_jutsus"),
            FabricItemGroup.builder()
                    .title(Component.translatable("itemGroup.jutsus_tab"))
                    .icon(() -> FabricItems.FIRE_BALL_JUTSU.getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(FabricItems.FIRE_BALL_JUTSU);
                        output.accept(FabricItems.SHARK_BOMB_JUTSU);
                        output.accept(FabricItems.WATER_DRAGON_JUTSU);
                        output.accept(FabricItems.EARTH_WALL_JUTSU);
                        output.accept(FabricItems.EARTH_WAVE_JUTSU);
                        output.accept(FabricItems.WATER_PRISON_JUTSU);
                        output.accept(FabricItems.LIGHTNING_CHAKRA_MODE);
                    }).build()
    );

    public static void register() {
        // Class load triggers static field registration
    }
}
