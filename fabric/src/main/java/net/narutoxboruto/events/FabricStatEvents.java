package net.narutoxboruto.events;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import net.narutoxboruto.util.StatProgression;

/** Forwards Fabric events to {@link StatProgression}, which holds the loader-independent logic. */
public class FabricStatEvents {

    public static void register() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamage, damageTaken, blocked) -> {
            if (damageTaken <= 0.0F) return;

            if (source.getEntity() instanceof ServerPlayer attacker) {
                StatProgression.onPlayerDealtDamage(attacker, source);
            }
            if (entity instanceof ServerPlayer victim) {
                StatProgression.onPlayerDamaged(victim, source);
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers()) {
                StatProgression.tick(serverPlayer);
            }
        });
    }
}
