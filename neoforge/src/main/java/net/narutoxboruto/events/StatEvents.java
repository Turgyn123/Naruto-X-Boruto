package net.narutoxboruto.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.narutoxboruto.util.StatProgression;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Forwards NeoForge events to {@link StatProgression}, which holds the loader-independent logic. */
public class StatEvents {

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        DamageSource source = event.getSource();
        if (source.getEntity() instanceof ServerPlayer attacker) {
            StatProgression.onPlayerDealtDamage(attacker, source);
        }
        if (event.getEntity() instanceof ServerPlayer victim) {
            StatProgression.onPlayerDamaged(victim, source);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            StatProgression.tick(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        StatProgression.forget(event.getEntity().getUUID());
    }
}
