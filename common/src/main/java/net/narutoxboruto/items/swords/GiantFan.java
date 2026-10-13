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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.narutoxboruto.capabilities.info.Chakra;
import net.narutoxboruto.capabilities.info.ReleaseList;
import net.narutoxboruto.entities.effects.WeaponEffectEntity;
import net.narutoxboruto.main.platform.Services;
import net.narutoxboruto.util.ModUtil;

import java.util.List;

/**
 * The Giant Fan, Temari's weapon. It is a sword item and needs the Wind release.
 *
 * <ul>
 *   <li><b>Hold right click</b> to swing it: a strong wind blows everything in front of you away, players and mobs
 *       alike. The wind is a real swirl that you see flying out of the fan, not particles. It costs chakra every
 *       second, and stops by itself when the chakra runs out.</li>
 *   <li><b>The moons:</b> as in the anime, the longer you swing, the more of the three moons on the fan open up
 *       (one at the start, a second after two seconds, the third after four). Every moon makes the wind
 *       stronger, longer and more expensive.</li>
 *   <li><b>Cutting Whirlwind (Kamaitachi):</b> let go of right click after at least one second and the fan
 *       releases a blade of wind in front of you (a flying crescent, three of them with all three moons). It cuts
 *       everything in its way and throws it back. The fan needs a moment to recover afterwards.</li>
 * </ul>
 */
public class GiantFan extends SwordItem {

    private static final String REQUIRED_RELEASE = "wind";
    private static final int MAX_USE_TICKS = 72000;
    /** Ticks of swinging for the second and the third moon. */
    private static final int SECOND_MOON = 40;
    private static final int THIRD_MOON = 80;
    /** Swinging for at least this long lets the Cutting Whirlwind go when you let go. */
    private static final int MIN_CHARGE = 20;
    private static final int RECOVER_TICKS = 60;
    /** How close to the looking direction something has to be to be hit by the wind: the cosine of the half angle. */
    private static final double CONE = 0.5D;
    private static final int WIND_COLOR = 0xF0F2FFFA;
    private static final int BLADE_COLOR = 0xF0F4FFFA;

    private static final int[] CHAKRA_PER_SECOND = {2, 4, 6};
    private static final double[] RANGE = {10.0D, 14.0D, 18.0D};
    private static final double[] PUSH = {1.2D, 1.7D, 2.2D};
    private static final float[] WIND_SIZE = {1.5F, 2.0F, 2.6F};
    private static final int[] SLASH_CHAKRA = {4, 8, 12};
    private static final float[] SLASH_DAMAGE = {5.0F, 9.0F, 14.0F};
    private static final double[] SLASH_RANGE = {10.0D, 15.0D, 20.0D};

    public GiantFan(Properties properties) {
        super(SwordCustomTiers.GIANT_FAN, properties);
    }

    /** 1 to 3: how many moons of the fan are open after this many ticks of swinging. */
    private static int moons(int used) {
        return used >= THIRD_MOON ? 3 : used >= SECOND_MOON ? 2 : 1;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.consume(stack);
        }
        if (serverPlayer.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(stack);

        ReleaseList releases = Services.PLATFORM.getReleaseList(serverPlayer);
        if (!releases.getValue().toLowerCase().contains(REQUIRED_RELEASE)) {
            ModUtil.displayColoredMessage(serverPlayer, "msg.no_release", ChatFormatting.RED);
            return InteractionResultHolder.fail(stack);
        }
        Chakra chakra = Services.PLATFORM.getChakra(serverPlayer);
        if (chakra.getValue() < CHAKRA_PER_SECOND[0]) {
            ModUtil.displayColoredMessage(serverPlayer, "msg.no_chakra", ChatFormatting.RED);
            return InteractionResultHolder.fail(stack);
        }

        chakra.subValue(CHAKRA_PER_SECOND[0], serverPlayer);
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
        int moons = moons(used);

        // A new moon opens
        if (used == SECOND_MOON || used == THIRD_MOON) {
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 0.8F, 0.7F);
        }

        // Every second costs chakra, and the fan stops when there is none left
        if (used > 0 && used % 20 == 0) {
            Chakra chakra = Services.PLATFORM.getChakra(player);
            int cost = CHAKRA_PER_SECOND[moons - 1];
            if (chakra.getValue() < cost) {
                ModUtil.displayColoredMessage(player, "msg.no_chakra", ChatFormatting.RED);
                player.stopUsingItem();
                return;
            }
            chakra.subValue(cost, player);
        }

        double range = RANGE[moons - 1];
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();

        // Blow everything in the cone away, the closer the harder
        AABB area = player.getBoundingBox().inflate(range);
        for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive())) {
            Vec3 to = target.getBoundingBox().getCenter().subtract(eye);
            double distance = to.length();
            if (distance > range || distance < 0.1D || to.normalize().dot(look) < CONE) continue;

            double strength = (0.35D + 0.9D * (1.0D - distance / range)) * PUSH[moons - 1];
            target.setDeltaMovement(target.getDeltaMovement().add(look.x * strength, 0.12D + look.y * strength * 0.5D, look.z * strength));
            target.hurtMarked = true;
        }

        // The wind: a swirl flies out of the fan every few ticks, far enough to cover the range
        if (used % 3 == 0) {
            double speed = 0.9D;
            int life = (int) Math.ceil(range / speed);
            Vec3 start = eye.add(look.scale(0.9D)).add(0.0D, -0.3D, 0.0D);
            WeaponEffectEntity.spawnWind(serverLevel, start, look, WIND_SIZE[moons - 1], WIND_COLOR, life, speed);
        }
        if (used % 6 == 0) {
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP,
                    SoundSource.PLAYERS, 0.7F, 0.6F + serverLevel.random.nextFloat() * 0.2F);
        }
    }

    /** Letting go after a good swing releases the Cutting Whirlwind. */
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int timeLeft) {
        if (level.isClientSide() || !(user instanceof ServerPlayer player) || !(level instanceof ServerLevel serverLevel)) return;

        int used = MAX_USE_TICKS - timeLeft;
        if (used < MIN_CHARGE) return;

        int index = moons(used) - 1;
        Chakra chakra = Services.PLATFORM.getChakra(player);
        if (chakra.getValue() < SLASH_CHAKRA[index]) {
            ModUtil.displayColoredMessage(player, "msg.no_chakra", ChatFormatting.RED);
            return;
        }
        chakra.subValue(SLASH_CHAKRA[index], player);
        player.getCooldowns().addCooldown(this, RECOVER_TICKS);

        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        double range = SLASH_RANGE[index];

        // The blades of wind: one, two or three crescents flying out, one behind the other
        double speed = 1.4D;
        int life = (int) Math.ceil(range / speed);
        for (int i = 0; i <= index; i++) {
            Vec3 start = eye.add(look.scale(1.5D + i * 1.8D)).add(0.0D, -0.4D, 0.0D);
            WeaponEffectEntity.spawnCrescent(serverLevel, start, look, 1.7F + 0.5F * index, BLADE_COLOR, life, speed);
        }
        serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.2F, 0.5F);

        AABB area = player.getBoundingBox().inflate(range);
        for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive())) {
            Vec3 to = target.getBoundingBox().getCenter().subtract(eye);
            double distance = to.length();
            if (distance > range || distance < 0.1D || to.normalize().dot(look) < 0.4D) continue;

            target.hurt(player.damageSources().playerAttack(player), SLASH_DAMAGE[index]);
            double strength = 0.8D + 0.5D * index;
            target.setDeltaMovement(target.getDeltaMovement().add(look.x * strength, 0.3D, look.z * strength));
            target.hurtMarked = true;
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("giant_fan.tip_blow").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("giant_fan.tip_moons").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("giant_fan.tip_slash").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Chakra Cost: 2 / 4 / 6 per second").withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.literal("Requires: Wind Release").withStyle(ChatFormatting.GOLD));
    }
}
