package net.narutoxboruto.entities.jutsus;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.narutoxboruto.entities.shinobis.AbstractShinobiMob;

/**
 * Makes hostile mobs go for a clone or golem like they would for a player: hostile mobs near it that have no target, or
 * that have a target further away than it, take it as their target (when they can see it).
 */
final class DecoyTaunt {

    private static final double RANGE = 16.0D;

    private DecoyTaunt() {
    }

    static void attract(ServerLevel level, LivingEntity decoy) {
        for (Mob mob : level.getEntitiesOfClass(Mob.class, decoy.getBoundingBox().inflate(RANGE),
                m -> m != decoy && m.isAlive() && (m instanceof Enemy || m instanceof AbstractShinobiMob)
                        && !(m instanceof WaterCloneEntity) && !(m instanceof GolemEntity))) {
            LivingEntity current = mob.getTarget();
            boolean wantsDecoy = current == null || !current.isAlive()
                    || (current != decoy && current.distanceToSqr(mob) > decoy.distanceToSqr(mob));
            if (wantsDecoy && mob.hasLineOfSight(decoy)) {
                mob.setTarget(decoy);
            }
        }
    }
}
