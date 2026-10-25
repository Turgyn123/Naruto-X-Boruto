package net.narutoxboruto.client;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.narutoxboruto.entities.effects.WeaponEffectEntity;

/**
 * A player being launched by the shadow clone of the Boruto Stream: the model lies flat, face down, like Boruto flying
 * forward with the clone behind him pushing his feet. Found by looking for the clone of that player.
 */
public final class BorutoDash {

    private BorutoDash() {
    }

    /** Whether {@code player} is being pushed by a shadow clone right now. */
    public static boolean applies(Player player) {
        for (WeaponEffectEntity effect : player.level().getEntitiesOfClass(WeaponEffectEntity.class,
                new AABB(player.blockPosition()).inflate(6.0D),
                e -> e.getKind() == WeaponEffectEntity.SHADOW_CLONE && e.getTargetId() == player.getId())) {
            return true;
        }
        return false;
    }
}
