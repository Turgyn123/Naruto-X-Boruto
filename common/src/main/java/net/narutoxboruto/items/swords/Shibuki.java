package net.narutoxboruto.items.swords;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.narutoxboruto.capabilities.info.Chakra;
import net.narutoxboruto.main.platform.Services;

import java.util.List;


public class Shibuki extends AbstractAbilitySword {
    public Shibuki(Properties pProperties) {
        super(SwordCustomTiers.SHIBUKI, pProperties);
    }

    @Override
    public int getChakraCost() {
        return 30;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack stack = pPlayer.getItemInHand(pHand);
        if (!pLevel.isClientSide() && pPlayer instanceof ServerPlayer serverPlayer) {
            Chakra chakra = Services.PLATFORM.getChakra(serverPlayer);
            ItemCooldowns cooldowns = serverPlayer.getCooldowns();
            if (!cooldowns.isOnCooldown(this) && chakra.getValue() >= getChakraCost()) {
                chakra.subValue(getChakraCost(), serverPlayer);
                doSpecialAbility(null, serverPlayer);
                cooldowns.addCooldown(this, cooldown);
                pPlayer.swing(pHand, true);
                return InteractionResultHolder.success(stack);
            }
        }
        return InteractionResultHolder.pass(stack);
    }

    protected void doSpecialAbility(LivingEntity pTarget, ServerPlayer serverPlayer) {
        Level level = serverPlayer.level();

        // Explosion at targeted location (where player is looking)
        Vec3 start = serverPlayer.getEyePosition(1.0F);
        Vec3 lookVec = serverPlayer.getViewVector(1.0F);
        Vec3 end = start.add(lookVec.scale(30.0D));

        ClipContext clipContext = new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, serverPlayer);
        BlockHitResult rayTraceResult = level.clip(clipContext);
        Vec3 explosionPos = rayTraceResult.getLocation();

        // Single explosion at moderate power for visual + simple knockback
        level.explode(
                serverPlayer,
                explosionPos.x(),
                explosionPos.y(),
                explosionPos.z(),
                1.0F,
                false,
                Level.ExplosionInteraction.NONE
        );

        // Particle effects
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.EXPLOSION,
                    explosionPos.x(),
                    explosionPos.y(),
                    explosionPos.z(),
                    5,
                    0.5D, 0.5D, 0.5D,
                    0.3D
            );

            serverLevel.sendParticles(
                    ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    explosionPos.x(),
                    explosionPos.y() + 1.0,
                    explosionPos.z(),
                    10,
                    1.0D, 1.0D, 1.0D,
                    0.05D
            );
        }

        // Sound effect
        level.playSound(
                null,
                serverPlayer.getX(),
                serverPlayer.getY(),
                serverPlayer.getZ(),
                SoundEvents.GENERIC_EXPLODE,
                SoundSource.PLAYERS,
                1.0F,
                (1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F) * 0.7F
        );

        super.doSpecialAbility(pTarget, serverPlayer);
    }
}
