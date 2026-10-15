package net.narutoxboruto.items.swords;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.narutoxboruto.capabilities.info.Chakra;
import net.narutoxboruto.entities.effects.WeaponEffectEntity;
import net.narutoxboruto.main.platform.Services;
import net.narutoxboruto.util.ModUtil;

import java.util.List;

/**
 * Nuibari, the needle sword of Kushimaru Kuriarare, that stitches its enemies with chakra thread.
 *
 * Right click on a target in sight, up to 25 blocks away: a thin thread of chakra shoots from the sword to the target,
 * reels it in to just in front of you and holds it there for 3 seconds. Every hit on the held target does 4 extra
 * damage. The thread breaks, and the target is free, when someone hits the wielder or when the wielder dies or goes
 * away. Whichever way it ends, the ability rests for 8 seconds. Needs the same Kenjutsu as the other swordsmen's
 * blades.
 *
 * The thread itself (and holding the target) is the {@link WeaponEffectEntity} of kind THREAD.
 */
public class Nuibari extends AbstractAbilitySword {

    private static final double RANGE = 25.0D;
    /** A hit on a target that is held by the thread does this much extra damage. */
    private static final float STITCH_BONUS = 4.0F;

    public Nuibari(Properties pProperties) {
        super(SwordCustomTiers.NUIBARI, pProperties);
    }

    @Override
    public int getChakraCost() {
        return 3;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer) || !(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.consume(stack);
        }

        if (!meetsKenjutsuRequirement(serverPlayer)) {
            serverPlayer.displayClientMessage(Component.translatable("sword_ability.kenjutsu_required",
                    this.getDescription(), getRequiredKenjutsu(), Services.PLATFORM.getKenjutsu(serverPlayer).getValue()).withStyle(ChatFormatting.RED), false);
            return InteractionResultHolder.fail(stack);
        }
        if (serverPlayer.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(stack);

        // The target: the first living thing in the way of the look direction, before the first block
        Vec3 eye = serverPlayer.getEyePosition();
        Vec3 end = eye.add(serverPlayer.getLookAngle().scale(RANGE));
        BlockHitResult block = serverLevel.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, serverPlayer));
        if (block.getType() != HitResult.Type.MISS) end = block.getLocation();
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(serverLevel, serverPlayer, eye, end, new AABB(eye, end).inflate(1.0D),
                e -> e instanceof LivingEntity && e.isAlive() && !e.isSpectator() && e.isPickable());
        if (hit == null || !(hit.getEntity() instanceof LivingEntity target)) {
            serverPlayer.displayClientMessage(Component.translatable("nuibari.no_target").withStyle(ChatFormatting.GRAY), true);
            return InteractionResultHolder.fail(stack);
        }

        Chakra chakra = Services.PLATFORM.getChakra(serverPlayer);
        if (chakra.getValue() < getChakraCost()) {
            ModUtil.displayColoredMessage(serverPlayer, "msg.no_chakra", ChatFormatting.RED);
            return InteractionResultHolder.fail(stack);
        }
        chakra.subValue(getChakraCost(), serverPlayer);

        // The sword rests while the thread reels and holds, and for a few seconds after
        serverPlayer.getCooldowns().addCooldown(this, WeaponEffectEntity.THREAD_TICKS + WeaponEffectEntity.THREAD_COOLDOWN_TICKS);
        WeaponEffectEntity.spawnThread(serverLevel, serverPlayer, target, this);

        serverPlayer.displayClientMessage(Component.translatable("nuibari.bound").withStyle(ChatFormatting.DARK_RED), true);
        serverLevel.playSound(null, target.blockPosition(), SoundEvents.CHAIN_PLACE, SoundSource.PLAYERS, 1.0F, 0.7F);
        return InteractionResultHolder.consume(stack);
    }

    /** A hit on a target that the thread holds does extra damage. */
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);
        if (attacker instanceof ServerPlayer player && WeaponEffectEntity.isHeldByThread(target)) {
            target.invulnerableTime = 0;
            target.hurt(player.damageSources().playerAttack(player), STITCH_BONUS);
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("nuibari.tip_bind").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("nuibari.tip_break").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("nuibari.tip_stitch").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("nuibari.tip_cooldown").withStyle(ChatFormatting.GRAY));
    }
}
