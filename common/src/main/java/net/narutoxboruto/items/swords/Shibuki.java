package net.narutoxboruto.items.swords;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.narutoxboruto.capabilities.info.Chakra;
import net.narutoxboruto.main.platform.Services;

import java.util.List;


public class Shibuki extends AbstractAbilitySword {

    /** Chakra spent per detonation. */
    private static final int CHAKRA_COST = 10;
    /** Ticks before the scroll has re-deposited tags (20 ticks = 1 second). */
    private static final int REARM_TICKS = 8;
    /** Blast radius in blocks. */
    private static final double BLAST_RADIUS = 3.0D;
    /** Damage at the centre of the blast, on top of the normal sword hit. */
    private static final float BLAST_DAMAGE = 6.0F;
    /** Horizontal knockback at the centre of the blast. */
    private static final double BLAST_KNOCKBACK = 0.8D;

    public Shibuki(Properties pProperties) {
        super(SwordCustomTiers.SHIBUKI, pProperties);
        this.cooldown = REARM_TICKS;
    }

    @Override
    public int getChakraCost() {
        return CHAKRA_COST;
    }

    @Override
    protected void doSpecialAbility(LivingEntity pTarget, ServerPlayer serverPlayer) {
        if (pTarget != null && serverPlayer.level() instanceof ServerLevel serverLevel) {
            Vec3 away = new Vec3(pTarget.getX() - serverPlayer.getX(), 0.0D, pTarget.getZ() - serverPlayer.getZ()).normalize();
            Vec3 center = pTarget.position()
                    .add(0.0D, pTarget.getBbHeight() * 0.5D, 0.0D)
                    .subtract(away.scale(pTarget.getBbWidth() * 0.5D));

            detonateTags(serverLevel, serverPlayer, center);
        }
        super.doSpecialAbility(pTarget, serverPlayer);
    }

    private void detonateTags(ServerLevel level, ServerPlayer wielder, Vec3 center) {
        DamageSource blastSource = level.damageSources().explosion(wielder, wielder);
        AABB area = new AABB(center, center).inflate(BLAST_RADIUS);
        List<LivingEntity> victims = level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != wielder
                        && e.isAlive()
                        && !e.isSpectator()
                        && !(e instanceof TamableAnimal pet && pet.isOwnedBy(wielder)));

        for (LivingEntity victim : victims) {
            double dist = victim.position().add(0.0D, victim.getBbHeight() * 0.5D, 0.0D).distanceTo(center);
            if (dist > BLAST_RADIUS) {
                continue;
            }
            // Full strength at the centre, 40% at the edge.
            double falloff = 1.0D - 0.6D * (dist / BLAST_RADIUS);

            victim.invulnerableTime = 0;

            if (victim.hurt(blastSource, (float) (BLAST_DAMAGE * falloff))) {
                // Push away from the blast centre (respects knockback resistance).
                victim.knockback(BLAST_KNOCKBACK * falloff, center.x - victim.getX(), center.z - victim.getZ());
                victim.hurtMarked = true; // make sure players receive the velocity change
            }
        }

        playBlastEffects(level, center);
    }

    private void playBlastEffects(ServerLevel level, Vec3 center) {
        level.sendParticles(ParticleTypes.FLASH, center.x, center.y, center.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y, center.z, 4, 0.8D, 0.5D, 0.8D, 0.0D);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, center.x, center.y, center.z, 12, 0.7D, 0.5D, 0.7D, 0.04D);
        level.sendParticles(ParticleTypes.ASH, center.x, center.y, center.z, 20, 0.9D, 0.6D, 0.9D, 0.02D);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0F, 1.2F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F);
    }
}