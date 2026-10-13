package net.narutoxboruto.items.swords;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.narutoxboruto.capabilities.info.Chakra;
import net.narutoxboruto.main.platform.Services;
import net.narutoxboruto.util.ModUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Hiramekarei, the twin blades of Mangetsu Hozuki, the swordsman of the Mist who stored water inside his swords.
 *
 * <ul>
 *   <li><b>Water:</b> the blades soak up water. They fill slowly while the wielder stands in water or in the
 *       rain, and right click on a water source to drink it in. The stored water (0 to 100) is shown in the
 *       tooltip.</li>
 *   <li><b>Twin blades</b> (the Special Action key): joins the two swords in the hand (model
 *       {@code hiramekarei_to}). While they are drawn, every hit is a double strike, and a hit with stored
 *       water in the blades carries it as a burst of water with extra damage.</li>
 *   <li><b>Water cannon</b> (right click): releases the stored water in a straight jet in front of you that
 *       hits everything in its way and throws it back.</li>
 * </ul>
 * Needs Kenjutsu like the other swordsmen's blades and chakra for every use.
 */
public class Hiramekarei extends AbstractAbilitySword {

    private static final String WATER_KEY = "hiramekarei_water";
    private static final int MAX_WATER = 100;
    private static final int DRINK_AMOUNT = 35;
    private static final int HIT_WATER = 10;
    private static final int CANNON_WATER = 30;
    private static final int CANNON_CHAKRA = 6;
    private static final int CANNON_RANGE = 14;
    private static final float CANNON_DAMAGE = 8.0F;
    private static final float SECOND_STRIKE = 0.6F;
    private static final float WATER_BONUS = 3.0F;

    public Hiramekarei(Properties pProperties) {
        super(SwordCustomTiers.HIRAMEKAREI, pProperties);
    }

    @Override
    public int getChakraCost() {
        return 2;
    }

    // ---------------------------------------------------------------- the stored water

    public int getWater(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt(WATER_KEY);
    }

    private void setWater(ItemStack stack, int water) {
        int value = Math.max(0, Math.min(MAX_WATER, water));
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt(WATER_KEY, value));
    }

    /** The blades fill up on their own, slowly, while their owner stands in water or in the rain. */
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (level.isClientSide() || !(entity instanceof ServerPlayer player) || player.tickCount % 10 != 0) return;
        if (getWater(stack) >= MAX_WATER) return;
        if (player.isInWater() || level.isRainingAt(player.blockPosition())) {
            setWater(stack, getWater(stack) + 2);
        }
    }

    // ---------------------------------------------------------------- twin blades

    /** The Special Action key: draws both blades, or puts one away. The model follows the stance. */
    @Override
    public void toggleAbility(Player pPlayer, ItemStack pStack) {
        super.toggleAbility(pPlayer, pStack);
        if (!pPlayer.level().isClientSide()) {
            pStack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(isActive(pStack) ? 1 : 0));
        }
    }

    /** Twin blades: the second blade strikes right after the first, and stored water adds a burst of water. */
    @Override
    protected void doSpecialAbility(LivingEntity pTarget, ServerPlayer serverPlayer) {
        ItemStack stack = serverPlayer.getMainHandItem();
        float damage = (float) serverPlayer.getAttributeValue(Attributes.ATTACK_DAMAGE) * SECOND_STRIKE;

        boolean burst = stack.getItem() == this && getWater(stack) >= HIT_WATER;
        if (burst) {
            setWater(stack, getWater(stack) - HIT_WATER);
            damage += WATER_BONUS;
        }

        pTarget.invulnerableTime = 0;
        pTarget.hurt(serverPlayer.damageSources().playerAttack(serverPlayer), damage);

        if (serverPlayer.level() instanceof ServerLevel level) {
            double x = pTarget.getX();
            double y = pTarget.getY() + pTarget.getBbHeight() / 2.0;
            double z = pTarget.getZ();
            level.sendParticles(ParticleTypes.SWEEP_ATTACK, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
            if (burst) {
                Vec3 away = pTarget.position().subtract(serverPlayer.position()).multiply(1.0, 0.0, 1.0);
                if (away.lengthSqr() > 1.0e-4) {
                    Vec3 push = away.normalize().scale(0.8);
                    pTarget.setDeltaMovement(pTarget.getDeltaMovement().add(push.x, 0.2, push.z));
                    pTarget.hurtMarked = true;
                }
                pTarget.clearFire();
                pTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1));
                level.sendParticles(ParticleTypes.SPLASH, x, y, z, 40, 0.5, 0.5, 0.5, 0.3);
                level.sendParticles(ParticleTypes.BUBBLE, x, y, z, 15, 0.4, 0.4, 0.4, 0.2);
                level.playSound(null, pTarget.blockPosition(), SoundEvents.PLAYER_SPLASH, SoundSource.PLAYERS, 0.9F, 1.1F);
            }
        }
    }

    // ---------------------------------------------------------------- drinking and the water cannon

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Aiming at water: soak it up into the blades
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = hit.getBlockPos();
            if (level.getFluidState(pos).is(FluidTags.WATER)) {
                if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                    if (getWater(stack) >= MAX_WATER) {
                        serverPlayer.displayClientMessage(Component.translatable("hiramekarei.full").withStyle(ChatFormatting.AQUA), true);
                    } else {
                        setWater(stack, getWater(stack) + DRINK_AMOUNT);
                        serverPlayer.displayClientMessage(Component.translatable("hiramekarei.water", getWater(stack), MAX_WATER).withStyle(ChatFormatting.AQUA), true);
                        level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.PLAYERS, 0.8F, 1.2F);
                        if (level instanceof ServerLevel serverLevel) {
                            serverLevel.sendParticles(ParticleTypes.BUBBLE, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 12, 0.3, 0.2, 0.3, 0.1);
                        }
                    }
                }
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
            }
        }

        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.pass(stack);
        }

        // Otherwise the stored water leaves the blades as a cannon
        if (!meetsKenjutsuRequirement(serverPlayer)) {
            serverPlayer.displayClientMessage(Component.translatable("sword_ability.kenjutsu_required",
                    this.getDescription(), 350, Services.PLATFORM.getKenjutsu(serverPlayer).getValue()).withStyle(ChatFormatting.RED), false);
            return InteractionResultHolder.fail(stack);
        }
        if (serverPlayer.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(stack);
        if (getWater(stack) < CANNON_WATER) {
            serverPlayer.displayClientMessage(Component.translatable("hiramekarei.no_water").withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }
        Chakra chakra = Services.PLATFORM.getChakra(serverPlayer);
        if (chakra.getValue() < CANNON_CHAKRA) {
            ModUtil.displayColoredMessage(serverPlayer, "msg.no_chakra", ChatFormatting.RED);
            return InteractionResultHolder.fail(stack);
        }

        chakra.subValue(CANNON_CHAKRA, serverPlayer);
        setWater(stack, getWater(stack) - CANNON_WATER);
        fireCannon(serverPlayer, (ServerLevel) level);
        serverPlayer.getCooldowns().addCooldown(this, 40);
        return InteractionResultHolder.consume(stack);
    }

    private void fireCannon(ServerPlayer player, ServerLevel level) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Set<LivingEntity> hit = new HashSet<>();

        for (int i = 1; i <= CANNON_RANGE; i++) {
            Vec3 point = eye.add(look.scale(i));
            BlockPos pos = BlockPos.containing(point);
            if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) break;
            level.sendParticles(ParticleTypes.SPLASH, point.x, point.y - 0.2, point.z, 14, 0.25, 0.25, 0.25, 0.15);
            level.sendParticles(ParticleTypes.BUBBLE_POP, point.x, point.y - 0.2, point.z, 3, 0.2, 0.2, 0.2, 0.05);
            AABB box = new AABB(point, point).inflate(1.0);
            hit.addAll(level.getEntitiesOfClass(LivingEntity.class, box, e -> e != player && e.isAlive()));
        }

        for (LivingEntity target : hit) {
            target.hurt(player.damageSources().playerAttack(player), CANNON_DAMAGE);
            target.setDeltaMovement(target.getDeltaMovement().add(look.x * 1.4, 0.3, look.z * 1.4));
            target.hurtMarked = true;
            target.clearFire();
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 1.0F, 0.8F);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("hiramekarei.water", getWater(stack), MAX_WATER).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("hiramekarei.tip_drink").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("hiramekarei.tip_twin").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("hiramekarei.tip_cannon").withStyle(ChatFormatting.GRAY));
    }
}
