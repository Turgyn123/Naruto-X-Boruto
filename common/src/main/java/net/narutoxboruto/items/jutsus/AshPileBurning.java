package net.narutoxboruto.items.jutsus;

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
import net.narutoxboruto.entities.effects.WeaponEffectEntity;

import java.util.List;

/**
 * Fire Release: Ash Pile Burning. Rank B, 35 chakra, range 40 meters. The caster spews chakra infused gunpowder that
 * stays in the air around where they aim like a cloud, and works as a smokescreen. Using the jutsu again sets the ash
 * alight with a spark and the whole cloud burns in a violent explosion. It burns everyone in it, allies too, but not
 * the caster, so the timing is what matters. Requires Fire release affinity.
 */
public class AshPileBurning extends AbstractJutsuItem {

    private static final double RANGE = 40.0D;

    public AshPileBurning(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public String getJutsuName() {
        return "Fire Release: Ash Pile Burning";
    }

    @Override
    public String getRequiredRelease() {
        return "fire";
    }

    @Override
    public int getChakraCost() {
        return 35;
    }

    @Override
    public int getCooldownTicks() {
        return 300; // 15 seconds
    }

    /** With a cloud of ash out, using the jutsu again sets it alight, and costs nothing. */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer && level instanceof ServerLevel serverLevel) {
            WeaponEffectEntity cloud = WeaponEffectEntity.findAshCloud(serverLevel, serverPlayer);
            if (cloud != null) {
                cloud.ignite();
                return InteractionResultHolder.consume(player.getItemInHand(hand));
            }
        }
        return super.use(level, player, hand);
    }

    @Override
    protected boolean executeJutsu(ServerPlayer serverPlayer, Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }

        // The cloud goes where the caster aims: at the first creature or block in sight, or 40 meters ahead
        Vec3 eye = serverPlayer.getEyePosition();
        Vec3 end = eye.add(serverPlayer.getLookAngle().scale(RANGE));
        BlockHitResult block = serverLevel.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, serverPlayer));
        if (block.getType() != HitResult.Type.MISS) end = block.getLocation();
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(serverLevel, serverPlayer, eye, end, new AABB(eye, end).inflate(1.0D),
                e -> e instanceof LivingEntity && e.isAlive() && !e.isSpectator() && e.isPickable());
        Vec3 center = hit != null ? hit.getEntity().position() : end;

        WeaponEffectEntity.spawnAshCloud(serverLevel, serverPlayer, center);
        level.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0F, 1.6F);
        serverPlayer.displayClientMessage(Component.literal("Use it again to set the ash alight").withStyle(ChatFormatting.GRAY), true);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.literal("Rank: B").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Range: 40 meters").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Use it again to set the ash alight").withStyle(ChatFormatting.GRAY));
    }
}
