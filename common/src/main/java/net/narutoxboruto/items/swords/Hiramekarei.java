package net.narutoxboruto.items.swords;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Hiramekarei, the water storing sword of Mangetsu Hozuki. With the ability switched on (the Special Action
 * key) every hit releases the water the blade has soaked up: a burst of water that hits the target again,
 * pushes it back and leaves it soaked and slow for a moment. Costs chakra per hit and needs the same
 * Kenjutsu as the other swordsmen's blades.
 */
public class Hiramekarei extends AbstractAbilitySword {

    private static final float BURST_DAMAGE = 4.0F;
    private static final double PUSH = 0.9D;

    public Hiramekarei(Properties pProperties) {
        super(SwordCustomTiers.HIRAMEKAREI, pProperties);
    }

    @Override
    public int getChakraCost() {
        return 3;
    }

    @Override
    protected void doSpecialAbility(LivingEntity pTarget, ServerPlayer serverPlayer) {
        pTarget.hurt(serverPlayer.damageSources().playerAttack(serverPlayer), BURST_DAMAGE);

        Vec3 away = pTarget.position().subtract(serverPlayer.position()).multiply(1.0, 0.0, 1.0);
        if (away.lengthSqr() > 1.0e-4) {
            Vec3 push = away.normalize().scale(PUSH);
            pTarget.setDeltaMovement(pTarget.getDeltaMovement().add(push.x, 0.25, push.z));
            pTarget.hurtMarked = true;
        }
        pTarget.clearFire();
        pTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1));

        if (serverPlayer.level() instanceof ServerLevel level) {
            double x = pTarget.getX();
            double y = pTarget.getY() + pTarget.getBbHeight() / 2.0;
            double z = pTarget.getZ();
            level.sendParticles(ParticleTypes.SPLASH, x, y, z, 40, 0.5, 0.5, 0.5, 0.3);
            level.sendParticles(ParticleTypes.BUBBLE, x, y, z, 20, 0.4, 0.4, 0.4, 0.2);
            level.playSound(null, pTarget.blockPosition(), SoundEvents.PLAYER_SPLASH, SoundSource.PLAYERS, 0.9F, 1.1F);
        }
    }
}
