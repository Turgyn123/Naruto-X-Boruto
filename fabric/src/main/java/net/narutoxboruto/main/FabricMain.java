package net.narutoxboruto.main;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.narutoxboruto.entities.shinobis.AbstractShinobiMob;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.narutoxboruto.effect.FabricEffects;
import net.narutoxboruto.effect.ModEffects;
import net.narutoxboruto.entities.FabricEntities;
import net.narutoxboruto.entities.ModEntities;
import net.narutoxboruto.entities.shinobis.JinpachiMunashi;
import net.narutoxboruto.entities.shinobis.JininAkebino;
import net.narutoxboruto.entities.shinobis.KushimaruKuriarare;
import net.narutoxboruto.entities.shinobis.AmeyuriRingo;
import net.narutoxboruto.entities.shinobis.Chojuro;
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
        FabricDefaultAttributeRegistry.register(FabricEntities.JININ_AKEBINO, JininAkebino.setAttribute());
        FabricDefaultAttributeRegistry.register(FabricEntities.KUSHIMARU_KURIARARE, KushimaruKuriarare.setAttribute());
        FabricDefaultAttributeRegistry.register(FabricEntities.AMEYURI_RINGO, AmeyuriRingo.setAttribute());
        FabricDefaultAttributeRegistry.register(FabricEntities.CHOJURO, Chojuro.setAttribute());
        FabricDefaultAttributeRegistry.register(FabricEntities.ZABUZA_MOMOCHI, ZabuzaMomochi.setAttribute());
        FabricDefaultAttributeRegistry.register(FabricEntities.WATER_CLONE, net.narutoxboruto.entities.jutsus.WaterCloneEntity.setAttribute());
        FabricDefaultAttributeRegistry.register(FabricEntities.GOLEM, net.narutoxboruto.entities.jutsus.GolemEntity.setAttribute());

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
        ModEntities.WATER_CLONE = FabricEntities.WATER_CLONE;
        ModEntities.GOLEM = FabricEntities.GOLEM;
        ModEntities.LIGHTNING_ARC = FabricEntities.LIGHTNING_ARC;
        ModEntities.WEAPON_EFFECT = FabricEntities.WEAPON_EFFECT;
        ModEntities.CHAKRA_SPHERE = FabricEntities.CHAKRA_SPHERE;
        ModEntities.ZABUZA_MOMOCHI = FabricEntities.ZABUZA_MOMOCHI;
        ModEntities.JINPACHI_MUNASHI = FabricEntities.JINPACHI_MUNASHI;
        ModEntities.JININ_AKEBINO = FabricEntities.JININ_AKEBINO;
        ModEntities.KUSHIMARU_KURIARARE = FabricEntities.KUSHIMARU_KURIARARE;
        ModEntities.AMEYURI_RINGO = FabricEntities.AMEYURI_RINGO;
        ModEntities.CHOJURO = FabricEntities.CHOJURO;
        ModEntities.KISAME_HOSHIGAKI = FabricEntities.KISAME_HOSHIGAKI;

        ModItems.SHURIKEN_ITEM = FabricItems.SHURIKEN;
        ModItems.FUMA_SHURIKEN_ITEM = FabricItems.FUMA_SHURIKEN;
        ModItems.SAMEHADA_ITEM = FabricItems.SAMEHADA;
        ModItems.KUBIKIRIBOCHO_ITEM = FabricItems.KUBIKIRIBOCHO;
        ModItems.SHIBUKI_ITEM = FabricItems.SHIBUKI;
        ModItems.KABUTOWARI_ITEM = FabricItems.KABUTOWARI;
        ModItems.NUIBARI_ITEM = FabricItems.NUIBARI;
        ModItems.KIBA_ITEM = FabricItems.KIBA;
        ModItems.HIRAMEKAREI_ITEM = FabricItems.HIRAMEKAREI;
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
        SpawnPlacements.register(FabricEntities.ZABUZA_MOMOCHI, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AbstractShinobiMob::checkBossSpawnRules);
        SpawnPlacements.register(FabricEntities.JINPACHI_MUNASHI, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AbstractShinobiMob::checkBossSpawnRules);
        SpawnPlacements.register(FabricEntities.JININ_AKEBINO, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AbstractShinobiMob::checkBossSpawnRules);
        SpawnPlacements.register(FabricEntities.KUSHIMARU_KURIARARE, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AbstractShinobiMob::checkBossSpawnRules);
        SpawnPlacements.register(FabricEntities.AMEYURI_RINGO, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AbstractShinobiMob::checkBossSpawnRules);
        SpawnPlacements.register(FabricEntities.CHOJURO, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AbstractShinobiMob::checkBossSpawnRules);
        SpawnPlacements.register(FabricEntities.KISAME_HOSHIGAKI, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AbstractShinobiMob::checkBossSpawnRules);

        // Server tick for EarthWave and WaterPrison
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            EarthWave.onServerTick();
            WaterPrison.onServerTick();
        });
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            WaterPrison.shutdown();
            EarthWave.clear();
        });

        // Use Fabric to bootstrap the Common mod.
        Main.LOG.info("Hello Fabric world!");
        Main.init();
    }
}
