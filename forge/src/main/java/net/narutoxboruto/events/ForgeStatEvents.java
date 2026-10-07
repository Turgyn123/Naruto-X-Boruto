package net.narutoxboruto.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.narutoxboruto.util.StatProgression;

/** Forwards Forge events to {@link StatProgression}, which holds the loader-independent logic. */
public class ForgeStatEvents {

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getAmount() <= 0.0F) return;

        DamageSource source = event.getSource();
        if (source.getEntity() instanceof ServerPlayer attacker) {
            StatProgression.onPlayerDealtDamage(attacker, source);
        }
        if (event.getEntity() instanceof ServerPlayer victim) {
            StatProgression.onPlayerDamaged(victim, source);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent.Post event) {
        if (event.player instanceof ServerPlayer serverPlayer) {
            StatProgression.tick(serverPlayer);
        }
    }
}
