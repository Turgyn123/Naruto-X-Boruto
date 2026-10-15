package net.narutoxboruto.events;

import net.narutoxboruto.entities.shinobis.AbstractShinobiMob;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.narutoxboruto.entities.ForgeEntities;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ForgeSpawnEvents {
    @SubscribeEvent
    public static void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        event.register(ForgeEntities.ZABUZA_MOMOCHI.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AbstractShinobiMob::checkBossSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(ForgeEntities.JINPACHI_MUNASHI.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AbstractShinobiMob::checkBossSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(ForgeEntities.JININ_AKEBINO.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AbstractShinobiMob::checkBossSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(ForgeEntities.KUSHIMARU_KURIARARE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AbstractShinobiMob::checkBossSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(ForgeEntities.AMEYURI_RINGO.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AbstractShinobiMob::checkBossSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(ForgeEntities.CHOJURO.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AbstractShinobiMob::checkBossSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(ForgeEntities.KISAME_HOSHIGAKI.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AbstractShinobiMob::checkBossSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
    }
}
