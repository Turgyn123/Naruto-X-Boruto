package net.narutoxboruto.items.jutsus;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Wind Release: Boruto Stream. Rank C, 30 chakra, range 20 meters. The caster speeds up the movements of someone with
 * wind: the player looked at (an ally, up to 20 blocks away), or the caster when nobody is looked at. The speed is raised
 * by 60 points, which is the same as +120% movement speed in this mod (every 10 points of the Speed stat are +20%, like
 * one level of the Speed effect), for 30 seconds. Requires Wind release affinity.
 */
public class BorutoStream extends AbstractJutsuItem {

    private static final double RANGE = 20.0D;
    /** The speed effect has the same amplifier as 60 points of the Speed stat: 60 / 10 levels, minus the first. */
    private static final int AMPLIFIER = 5;
    private static final int DURATION_TICKS = 20 * 30;

    public BorutoStream(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public String getJutsuName() {
        return "Boruto Stream";
    }

    @Override
    public String getRequiredRelease() {
        return "wind";
    }

    @Override
    public int getChakraCost() {
        return 30;
    }

    @Override
    public int getCooldownTicks() {
        return 20 * 40; // 40 seconds
    }

    @Override
    protected boolean executeJutsu(ServerPlayer serverPlayer, Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        LivingEntity target = findAlly(serverPlayer);
        if (target == null) target = serverPlayer;
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, DURATION_TICKS, AMPLIFIER, false, true));

        // A stream of wind that curls round the one that is sped up
        for (int i = 0; i < 24; i++) {
            double t = i / 24.0D;
            double angle = t * Math.PI * 4.0D;
            double radius = 0.9D - 0.4D * t;
            serverLevel.sendParticles(ParticleTypes.CLOUD,
                    target.getX() + Math.cos(angle) * radius, target.getY() + t * 1.8D, target.getZ() + Math.sin(angle) * radius,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 1.8F);
        return true;
    }

    /** The player that the caster looks at, within 20 meters, or null. */
    private static LivingEntity findAlly(ServerPlayer caster) {
        Vec3 eye = caster.getEyePosition();
        Vec3 end = eye.add(caster.getLookAngle().scale(RANGE));
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(caster.level(), caster, eye, end,
                new AABB(eye, end).inflate(1.0D), e -> e instanceof Player && e.isAlive() && e != caster && !e.isSpectator());
        Entity entity = hit == null ? null : hit.getEntity();
        return entity instanceof Player player ? player : null;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.literal("Rank: C").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Range: 20 meters").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Speed +60 for 30 seconds, on you or on an ally you look at").withStyle(ChatFormatting.GRAY));
    }
}
