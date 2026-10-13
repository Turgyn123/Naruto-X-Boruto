package net.narutoxboruto.items.swords;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * Nuibari, the needle sword of Kushimaru Kuriarare. With the ability switched on (the Special Action key) every
 * hit stitches the target in place with chakra thread: it is slowed down heavily and weakened for a few
 * seconds. Costs a little chakra per hit and needs the same Kenjutsu as the other swordsmen's blades.
 */
public class Nuibari extends AbstractAbilitySword {

    private static final int BIND_TICKS = 80;

    public Nuibari(Properties pProperties) {
        super(SwordCustomTiers.NUIBARI, pProperties);
    }

    @Override
    public int getChakraCost() {
        return 2;
    }

    @Override
    protected void doSpecialAbility(LivingEntity pTarget, ServerPlayer serverPlayer) {
        pTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, BIND_TICKS, 3));
        pTarget.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, BIND_TICKS, 1));

        if (serverPlayer.level() instanceof ServerLevel level) {
            // Threads in the air between the sword and the target
            double fromX = serverPlayer.getX();
            double fromY = serverPlayer.getEyeY() - 0.3;
            double fromZ = serverPlayer.getZ();
            double toX = pTarget.getX();
            double toY = pTarget.getY() + pTarget.getBbHeight() / 2.0;
            double toZ = pTarget.getZ();
            for (int i = 0; i <= 12; i++) {
                double t = i / 12.0;
                level.sendParticles(ParticleTypes.CRIT, fromX + (toX - fromX) * t, fromY + (toY - fromY) * t,
                        fromZ + (toZ - fromZ) * t, 1, 0.02, 0.02, 0.02, 0.0);
            }
            level.sendParticles(ParticleTypes.ENCHANTED_HIT, toX, toY, toZ, 12, 0.3, 0.4, 0.3, 0.1);
            level.playSound(null, pTarget.blockPosition(), SoundEvents.CHAIN_PLACE, SoundSource.PLAYERS, 0.8F, 1.4F);
        }
    }
}
