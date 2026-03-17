package net.narutoxboruto.main;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.narutoxboruto.effect.FabricEffects;
import net.narutoxboruto.effect.ModEffects;
import net.narutoxboruto.entities.FabricEntities;
import net.narutoxboruto.entities.ModEntities;
import net.narutoxboruto.entities.shinobis.JinpachiMunashi;
import net.narutoxboruto.entities.shinobis.KizameHoshigaki;
import net.narutoxboruto.entities.shinobis.ZabuzaMomochi;
import net.narutoxboruto.events.*;
import net.narutoxboruto.fluids.FabricBlocks;
import net.narutoxboruto.items.FabricItems;
import net.narutoxboruto.items.FabricTab;
import net.narutoxboruto.items.ModItems;
import net.narutoxboruto.items.jutsus.EarthWave;
import net.narutoxboruto.items.jutsus.WaterPrison;
import net.narutoxboruto.menu.FabricMenuTypes;
import net.narutoxboruto.networking.FabricPacketHandler;
import net.narutoxboruto.particles.FabricParticles;

public class FabricMain implements ModInitializer {
    
    @Override
    public void onInitialize() {
        
        // Register Fabric-specific items, tabs, and entities
        FabricItems.register();
        FabricTab.register();
        FabricEntities.register();
        FabricEffects.register();
        FabricParticles.register();
        FabricMenuTypes.register();
        FabricBlocks.register();

        // Register entity attributes
        FabricDefaultAttributeRegistry.register(FabricEntities.KISAME_HOSHIGAKI, KizameHoshigaki.setAttribute());
        FabricDefaultAttributeRegistry.register(FabricEntities.JINPACHI_MUNASHI, JinpachiMunashi.setAttribute());
        FabricDefaultAttributeRegistry.register(FabricEntities.ZABUZA_MOMOCHI, ZabuzaMomochi.setAttribute());

        // Populate common holder fields
        ModEntities.KUNAI = FabricEntities.KUNAI;
        ModEntities.EXPLOSIVE_KUNAI = FabricEntities.EXPLOSIVE_KUNAI;
        ModEntities.SHURIKEN = FabricEntities.SHURIKEN;
        ModEntities.SENBON = FabricEntities.SENBON;
        ModEntities.POISON_SENBON = FabricEntities.POISON_SENBON;
        ModEntities.FUMA_SHURIKEN = FabricEntities.FUMA_SHURIKEN;
        ModEntities.FIRE_BALL = FabricEntities.FIRE_BALL;
        ModEntities.SHARK_BOMB = FabricEntities.SHARK_BOMB;
        ModEntities.WATER_DRAGON = FabricEntities.WATER_DRAGON;
        ModEntities.LIGHTNING_ARC = FabricEntities.LIGHTNING_ARC;

        ModItems.SHURIKEN_ITEM = FabricItems.SHURIKEN;
        ModItems.FUMA_SHURIKEN_ITEM = FabricItems.FUMA_SHURIKEN;
        ModItems.SAMEHADA_ITEM = FabricItems.SAMEHADA;
        ModItems.KUBIKIRIBOCHO_ITEM = FabricItems.KUBIKIRIBOCHO;
        ModItems.SHIBUKI_ITEM = FabricItems.SHIBUKI;
        ModItems.CHAKRA_PAPER_ITEM = FabricItems.CHAKRA_PAPER;

        ModEffects.CHAKRA_CONTROL = FabricEffects.CHAKRA_CONTROL;

        // Register networking (server-side)
        FabricPacketHandler.registerServer();

        // Register event handlers
        FabricAttachmentEvents.register();
        FabricEvents.register();
        FabricStatEvents.register();
        FabricCommandEvents.register();
        FabricJutsuItemEvents.register();

        // Register spawn placements for custom mobs
        SpawnPlacements.register(FabricEntities.ZABUZA_MOMOCHI, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules);
        SpawnPlacements.register(FabricEntities.JINPACHI_MUNASHI, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules);
        SpawnPlacements.register(FabricEntities.KISAME_HOSHIGAKI, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules);

        // Server tick for EarthWave and WaterPrison
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            EarthWave.onServerTick();
            WaterPrison.onServerTick();
        });

        // Use Fabric to bootstrap the Common mod.
        Main.LOG.info("Hello Fabric world!");
        Main.init();
    }
}
