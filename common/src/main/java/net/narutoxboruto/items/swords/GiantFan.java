package net.narutoxboruto.items.swords;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.narutoxboruto.capabilities.info.Chakra;
import net.narutoxboruto.capabilities.info.ReleaseList;
import net.narutoxboruto.main.platform.Services;
import net.narutoxboruto.util.ModUtil;

import java.util.List;

/**
 * The Giant Fan, Temari's weapon. Needs the Wind release. Hold right click to swing it: a strong wind blows
 * everything in front of you away, players and mobs alike, and it costs chakra every second you keep using it.
 * It stops by itself when the chakra runs out.
 */
public class GiantFan extends Item {

    private static final String REQUIRED_RELEASE = "wind";
    /** Chakra taken at the start and then every second. */
    private static final int CHAKRA_PER_SECOND = 3;
    private static final double RANGE = 10.0D;
    /** How close to the looking direction something has to be to be hit: the cosine of the half angle. */
    private static final double CONE = 0.6D;
    private static final int MAX_USE_TICKS = 72000;

    public GiantFan(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.consume(stack);
        }

        ReleaseList releases = Services.PLATFORM.getReleaseList(serverPlayer);
        if (!releases.getValue().toLowerCase().contains(REQUIRED_RELEASE)) {
            ModUtil.displayColoredMessage(serverPlayer, "msg.no_release", ChatFormatting.RED);
            return InteractionResultHolder.fail(stack);
        }
        Chakra chakra = Services.PLATFORM.getChakra(serverPlayer);
        if (chakra.getValue() < CHAKRA_PER_SECOND) {
            ModUtil.displayColoredMessage(serverPlayer, "msg.no_chakra", ChatFormatting.RED);
            return InteractionResultHolder.fail(stack);
        }

        chakra.subValue(CHAKRA_PER_SECOND, serverPlayer);
        serverPlayer.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BLOCK;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return MAX_USE_TICKS;
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide() || !(user instanceof ServerPlayer player) || !(level instanceof ServerLevel serverLevel)) return;

        int used = MAX_USE_TICKS - remainingUseDuration;

        // Every second costs chakra, and the fan stops when there is none left
        if (used > 0 && used % 20 == 0) {
            Chakra chakra = Services.PLATFORM.getChakra(player);
            if (chakra.getValue() < CHAKRA_PER_SECOND) {
                ModUtil.displayColoredMessage(player, "msg.no_chakra", ChatFormatting.RED);
                player.stopUsingItem();
                return;
            }
            chakra.subValue(CHAKRA_PER_SECOND, player);
        }

        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();

        // Blow everything in the cone away, the closer the harder
        AABB area = player.getBoundingBox().inflate(RANGE);
        for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive())) {
            Vec3 to = target.getBoundingBox().getCenter().subtract(eye);
            double distance = to.length();
            if (distance > RANGE || distance < 0.1D || to.normalize().dot(look) < CONE) continue;

            double strength = 0.35D + 0.9D * (1.0D - distance / RANGE);
            target.setDeltaMovement(target.getDeltaMovement().add(look.x * strength, 0.12D + look.y * strength * 0.5D, look.z * strength));
            target.hurtMarked = true;
        }

        // The wind: clouds flying away from the fan
        for (int i = 0; i < 8; i++) {
            double along = 1.0D + serverLevel.random.nextDouble() * 3.0D;
            double spread = 0.25D + along * 0.18D;
            double x = eye.x + look.x * along + (serverLevel.random.nextDouble() - 0.5D) * spread * 2.0D;
            double y = eye.y - 0.3D + look.y * along + (serverLevel.random.nextDouble() - 0.5D) * spread * 2.0D;
            double z = eye.z + look.z * along + (serverLevel.random.nextDouble() - 0.5D) * spread * 2.0D;
            serverLevel.sendParticles(ParticleTypes.CLOUD, x, y, z, 0, look.x, look.y, look.z, 0.9D);
        }
        if (used % 6 == 0) {
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP,
                    SoundSource.PLAYERS, 0.7F, 0.6F + serverLevel.random.nextFloat() * 0.2F);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.literal("Hold right click to blow everything in front of you away").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Chakra Cost: " + CHAKRA_PER_SECOND + " per second").withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.literal("Requires: Wind Release").withStyle(ChatFormatting.GOLD));
    }
}
