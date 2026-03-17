package net.narutoxboruto.main;


import net.narutoxboruto.capabilities.NeoForgeCapabilities;
import net.narutoxboruto.effect.ModEffects;
import net.narutoxboruto.effect.NeoForgeEffects;
import net.narutoxboruto.entities.ModEntities;
import net.narutoxboruto.entities.NeoForgeEntities;
import net.narutoxboruto.entities.shinobis.JinpachiMunashi;
import net.narutoxboruto.entities.shinobis.KizameHoshigaki;
import net.narutoxboruto.entities.shinobis.ZabuzaMomochi;
import net.narutoxboruto.events.*;
import net.narutoxboruto.fluids.ModFluidBlocks;
import net.narutoxboruto.fluids.ModFluids;
import net.narutoxboruto.items.ModItems;
import net.narutoxboruto.items.NeoForgeItems;
import net.narutoxboruto.items.NeoForgeTab;
import net.narutoxboruto.items.jutsus.EarthWave;
import net.narutoxboruto.items.jutsus.WaterPrison;
import net.narutoxboruto.menu.ModMenuTypes;
import net.narutoxboruto.networking.NeoForgePacketHandler;
import net.narutoxboruto.particles.ModParticles;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod(Main.MOD_ID)
public class NeoForgeMain {

    public NeoForgeMain(IEventBus eventBus) {

        NeoForgeItems.register(eventBus);
        NeoForgeTab.register(eventBus);
        NeoForgeEntities.register(eventBus);
        NeoForgeCapabilities.register(eventBus);
        NeoForgeEffects.register(eventBus);
        ModFluids.register(eventBus);
        ModFluidBlocks.register(eventBus);
        ModParticles.register(eventBus);
        ModMenuTypes.register(eventBus);

        // Register event handlers on GAME bus (server-side gameplay events)
        NeoForge.EVENT_BUS.register(AttachmentEvents.class);
        NeoForge.EVENT_BUS.register(Events.class);
        NeoForge.EVENT_BUS.register(StatEvents.class);
        NeoForge.EVENT_BUS.register(CommandEvents.class);
        NeoForge.EVENT_BUS.register(JutsuItemEvents.class);
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> {
            EarthWave.onServerTick();
            WaterPrison.onServerTick();
        });

        // Register event handlers on MOD bus (registration events)
        eventBus.register(SpawnEvents.class);
        eventBus.register(NeoForgePacketHandler.class);

        eventBus.addListener(this::Entities);
        eventBus.addListener(this::attributes);
        Main.init();
    }

    private void Entities(final FMLCommonSetupEvent event) {
        ModEntities.KUNAI = NeoForgeEntities.KUNAI.get();
        ModEntities.EXPLOSIVE_KUNAI = NeoForgeEntities.EXPLOSIVE_KUNAI.get();
        ModEntities.SHURIKEN = NeoForgeEntities.SHURIKEN.get();
        ModEntities.SENBON = NeoForgeEntities.SENBON.get();
        ModEntities.POISON_SENBON = NeoForgeEntities.POISON_SENBON.get();
        ModEntities.FUMA_SHURIKEN = NeoForgeEntities.FUMA_SHURIKEN.get();
        ModEntities.FIRE_BALL = NeoForgeEntities.FIRE_BALL.get();
        ModEntities.SHARK_BOMB = NeoForgeEntities.SHARK_BOMB.get();
        ModEntities.WATER_DRAGON = NeoForgeEntities.WATER_DRAGON.get();
        ModEntities.LIGHTNING_ARC = NeoForgeEntities.LIGHTNING_ARC.get();

        ModItems.SHURIKEN_ITEM = NeoForgeItems.SHURIKEN.get();
        ModItems.FUMA_SHURIKEN_ITEM = NeoForgeItems.FUMA_SHURIKEN.get();
        ModItems.SAMEHADA_ITEM = NeoForgeItems.SAMEHADA.get();
        ModItems.KUBIKIRIBOCHO_ITEM = NeoForgeItems.KUBIKIRIBOCHO.get();
        ModItems.SHIBUKI_ITEM = NeoForgeItems.SHIBUKI.get();
        ModItems.CHAKRA_PAPER_ITEM = NeoForgeItems.CHAKRA_PAPER.get();

        ModEffects.CHAKRA_CONTROL = NeoForgeEffects.CHAKRA_CONTROL;
    }

    private void attributes(EntityAttributeCreationEvent event) {
        event.put(NeoForgeEntities.KISAME_HOSHIGAKI.get(), KizameHoshigaki.setAttribute());
        event.put(NeoForgeEntities.JINPACHI_MUNASHI.get(), JinpachiMunashi.setAttribute());
        event.put(NeoForgeEntities.ZABUZA_MOMOCHI.get(), ZabuzaMomochi.setAttribute());
    }

}