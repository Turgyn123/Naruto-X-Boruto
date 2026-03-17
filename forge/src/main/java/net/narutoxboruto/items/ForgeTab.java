package net.narutoxboruto.items;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import static net.narutoxboruto.items.ForgeItems.*;
import static net.narutoxboruto.main.Main.MOD_ID;

public class ForgeTab {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final RegistryObject<CreativeModeTab> THORWABLE = CREATIVE_MODE_TAB.register("throwable", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.throwable"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> SHURIKEN.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(SHURIKEN.get());
                output.accept(KUNAI.get());
                output.accept(EXPLOSIVE_KUNAI.get());
                output.accept(SENBON.get());
                output.accept(POISON_SENBON.get());
                output.accept(FUMA_SHURIKEN.get());
            }).build());

    public static final RegistryObject<CreativeModeTab> SWORDS = CREATIVE_MODE_TAB.register("swords", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.swords"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> SAMEHADA.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(SAMEHADA.get());
                output.accept(KUBIKIRIBOCHO.get());
                output.accept(SHIBUKI.get());
                output.accept(KIBA.get());
                output.accept(KABUTOWARI.get());
                //output.accept(NUIBARI.get());
            }).build());

    public static final RegistryObject<CreativeModeTab> NXB_TAB = CREATIVE_MODE_TAB.register("nxb_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.nxb_tab"))
            .icon(() -> CHAKRA_PAPER.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(CHAKRA_PAPER.get());
                output.accept(CLAN_REROLL.get());
            }).build());

    public static final RegistryObject<CreativeModeTab> NXB_DNA = CREATIVE_MODE_TAB.register("nxb_dna", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.dna_tab"))
            .icon(() -> RANDOM_DNA.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(RANDOM_DNA.get());
                output.accept(EARTH_DNA.get());
                output.accept(FIRE_DNA.get());
                output.accept(WATER_DNA.get());
                output.accept(WIND_DNA.get());
                output.accept(LIGHTNING_DNA.get());
                output.accept(YIN_DNA.get());
                output.accept(YANG_DNA.get());
            }).build());

    public static final RegistryObject<CreativeModeTab> NXB_BOSSES = CREATIVE_MODE_TAB.register("nxb_bosses", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.bosses_tab"))
            .icon(() -> ZABUZA_SPAWN_EGG.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(KISAME_SPAWN_EGG.get());
                output.accept(JINPACHI_SPAWN_EGG.get());
                output.accept(ZABUZA_SPAWN_EGG.get());
            }).build());

    public static final RegistryObject<CreativeModeTab> NXB_SCROLLS = CREATIVE_MODE_TAB.register("nxb_scroll", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.scroll"))
            .icon(() -> TAIJUTSU_SCROLL.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(TAIJUTSU_SCROLL.get());
                output.accept(GENJUTSU_SCROLL.get());
                output.accept(KENJUTSU_SCROLL.get());
                output.accept(KINJUTSU_SCROLL.get());
                output.accept(MEDICAL_SCROLL.get());
                output.accept(NINJUTSU_SCROLL.get());
                output.accept(SENJUTSU_SCROLL.get());
                output.accept(SHURIKENJUTSU_SCROLL.get());
                output.accept(SPEED_SCROLL.get());
                output.accept(SUMMONING_SCROLL.get());
            }).build());

    public static final RegistryObject<CreativeModeTab> NXB_JUTSUS = CREATIVE_MODE_TAB.register("nxb_jutsus", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.jutsus_tab"))
            .icon(() -> FIRE_BALL_JUTSU.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(FIRE_BALL_JUTSU.get());
                output.accept(SHARK_BOMB_JUTSU.get());
                output.accept(WATER_DRAGON_JUTSU.get());
                output.accept(EARTH_WALL_JUTSU.get());
                output.accept(EARTH_WAVE_JUTSU.get());
                output.accept(WATER_PRISON_JUTSU.get());
                output.accept(LIGHTNING_CHAKRA_MODE.get());
            }).build());

    public static void register(IEventBus eventBus) {CREATIVE_MODE_TAB.register(eventBus);}
}
