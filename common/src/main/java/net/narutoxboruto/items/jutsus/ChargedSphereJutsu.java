package net.narutoxboruto.items.jutsus;

import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.narutoxboruto.capabilities.info.Chakra;
import net.narutoxboruto.capabilities.info.ReleaseList;
import net.narutoxboruto.entities.jutsus.ChakraSphereEntity;
import net.narutoxboruto.main.platform.Services;
import net.narutoxboruto.util.ModUtil;

/**
 * A jutsu that is charged by holding the right mouse button, with a sphere of chakra growing in the hand of the caster
 * (see {@link ChakraSphereEntity}), and used by letting go. Let go too early and the sphere falls apart and costs
 * nothing. The chakra is only taken when it is used, and then the jutsu rests.
 */
public abstract class ChargedSphereJutsu extends AbstractJutsuItem {

    protected ChargedSphereJutsu(Properties properties) {
        super(properties);
    }

    /** The kind of {@link ChakraSphereEntity} that is the sphere. */
    protected abstract int sphereKind();

    /** Which look the sphere has. */
    protected int sphereVariant() {
        return 0;
    }

    /** Never used: this jutsu is used by letting go, see {@link #releaseUsing}. */
    @Override
    protected boolean executeJutsu(ServerPlayer serverPlayer, Level level) {
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResultHolder.pass(stack);
        if (!(player instanceof ServerPlayer serverPlayer) || !(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.fail(stack);
        }
        if (serverPlayer.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(stack);

        String requiredRelease = getRequiredRelease();
        if (requiredRelease != null && !requiredRelease.isEmpty()) {
            ReleaseList releaseList = Services.PLATFORM.getReleaseList(serverPlayer);
            if (!releaseList.getValue().toLowerCase().contains(requiredRelease.toLowerCase())) {
                ModUtil.displayColoredMessage(serverPlayer, "msg.no_release", ChatFormatting.RED);
                return InteractionResultHolder.fail(stack);
            }
        }
        if (Services.PLATFORM.getChakra(serverPlayer).getValue() < getChakraCost()) {
            ModUtil.displayColoredMessage(serverPlayer, "msg.no_chakra", ChatFormatting.RED);
            return InteractionResultHolder.fail(stack);
        }

        player.startUsingItem(hand);
        ChakraSphereEntity.spawn(serverLevel, serverPlayer, sphereKind(), sphereVariant());
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    /** Let go of: a charged sphere is used, one that is not charged yet falls apart. */
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (level.isClientSide() || !(entity instanceof ServerPlayer player) || !(level instanceof ServerLevel serverLevel)) return;
        ChakraSphereEntity sphere = ChakraSphereEntity.findHeld(serverLevel, player, sphereKind());
        if (sphere == null) return;
        Chakra chakra = Services.PLATFORM.getChakra(player);
        if (!sphere.isCharged() || chakra.getValue() < getChakraCost()) {
            sphere.cancel();
            return;
        }
        chakra.subValue(getChakraCost(), player);
        sphere.release(player);
        player.getCooldowns().addCooldown(this, getCooldownTicks());
    }
}
