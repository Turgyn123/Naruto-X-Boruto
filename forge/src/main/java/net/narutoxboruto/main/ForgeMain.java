package net.narutoxboruto.main;

import net.minecraft.core.Holder;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.narutoxboruto.effect.ForgeEffects;
import net.narutoxboruto.effect.ModEffects;
import net.narutoxboruto.entities.ForgeEntities;
import net.narutoxboruto.entities.ModEntities;
import net.narutoxboruto.entities.shinobis.JinpachiMunashi;
import net.narutoxboruto.entities.shinobis.KizameHoshigaki;
import net.narutoxboruto.entities.shinobis.ZabuzaMomochi;
import net.narutoxboruto.events.*;
import net.narutoxboruto.fluids.ForgeBlocks;
import net.narutoxboruto.items.ForgeItems;
import net.narutoxboruto.items.ForgeTab;
import net.narutoxboruto.items.ModItems;
import net.narutoxboruto.items.jutsus.EarthWave;
import net.narutoxboruto.items.jutsus.WaterPrison;
import net.narutoxboruto.menu.ForgeMenuTypes;
import net.narutoxboruto.networking.ForgePacketHandler;
import net.narutoxboruto.particles.ForgeParticles;

@Mod(Main.MOD_ID)
public class ForgeMain {

    public ForgeMain(FMLJavaModLoadingContext context) {
        IEventBus eventBus = context.getModEventBus();

        ForgeItems.register(eventBus);
        ForgeTab.register(eventBus);
        ForgeEntities.register(eventBus);
        ForgeEffects.register(eventBus);
        ForgeParticles.register(eventBus);
        ForgeMenuTypes.register(eventBus);
        ForgeBlocks.register(eventBus);

        // Register event handlers on GAME bus (server-side gameplay events)
        MinecraftForge.EVENT_BUS.register(ForgeAttachmentEvents.class);
        MinecraftForge.EVENT_BUS.register(ForgeEvents.class);
        MinecraftForge.EVENT_BUS.register(ForgeStatEvents.class);
        MinecraftForge.EVENT_BUS.register(ForgeCommandEvents.class);
        MinecraftForge.EVENT_BUS.register(ForgeJutsuItemEvents.class);
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ServerTickEvent.Post event) -> {
            EarthWave.onServerTick();
            WaterPrison.onServerTick();
        });

        // Register event handlers on MOD bus (registration events)
        eventBus.register(ForgeSpawnEvents.class);
        eventBus.addListener(this::registerPackets);

        eventBus.addListener(this::Entities);
        eventBus.addListener(this::attributes);
        Main.init();
    }

    private void registerPackets(final FMLCommonSetupEvent event) {
        event.enqueueWork(ForgePacketHandler::register);
    }

    private void Entities(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModEntities.KUNAI = ForgeEntities.KUNAI.get();
            ModEntities.EXPLOSIVE_KUNAI = ForgeEntities.EXPLOSIVE_KUNAI.get();
            ModEntities.SHURIKEN = ForgeEntities.SHURIKEN.get();
            ModEntities.SENBON = ForgeEntities.SENBON.get();
            ModEntities.POISON_SENBON = ForgeEntities.POISON_SENBON.get();
            ModEntities.FUMA_SHURIKEN = ForgeEntities.FUMA_SHURIKEN.get();
            ModEntities.FIRE_BALL = ForgeEntities.FIRE_BALL.get();
            ModEntities.SHARK_BOMB = ForgeEntities.SHARK_BOMB.get();
            ModEntities.WATER_DRAGON = ForgeEntities.WATER_DRAGON.get();
            ModEntities.LIGHTNING_ARC = ForgeEntities.LIGHTNING_ARC.get();

            ModItems.SHURIKEN_ITEM = ForgeItems.SHURIKEN.get();
            ModItems.FUMA_SHURIKEN_ITEM = ForgeItems.FUMA_SHURIKEN.get();
            ModItems.SAMEHADA_ITEM = ForgeItems.SAMEHADA.get();
            ModItems.KUBIKIRIBOCHO_ITEM = ForgeItems.KUBIKIRIBOCHO.get();
            ModItems.SHIBUKI_ITEM = ForgeItems.SHIBUKI.get();
            ModItems.CHAKRA_PAPER_ITEM = ForgeItems.CHAKRA_PAPER.get();

            ModEffects.CHAKRA_CONTROL = Holder.direct(ForgeEffects.CHAKRA_CONTROL.get());
        });
    }

    private void attributes(EntityAttributeCreationEvent event) {
        event.put(ForgeEntities.KISAME_HOSHIGAKI.get(), KizameHoshigaki.setAttribute());
        event.put(ForgeEntities.JINPACHI_MUNASHI.get(), JinpachiMunashi.setAttribute());
        event.put(ForgeEntities.ZABUZA_MOMOCHI.get(), ZabuzaMomochi.setAttribute());
    }
}