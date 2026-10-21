package net.narutoxboruto.items.jutsus;

import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.narutoxboruto.capabilities.info.Chakra;
import net.narutoxboruto.capabilities.info.ReleaseList;
import net.narutoxboruto.entities.jutsus.ChakraSphereEntity;
import net.narutoxboruto.main.platform.Services;
import net.narutoxboruto.util.ModUtil;

/**
 * A jutsu with a sphere of chakra (see {@link ChakraSphereEntity}). One right click and the sphere appears in the hand of
 * the caster and grows. Another right click puts it away again, and so does changing to another item. Once it has
 * grown, a left click (a punch, at the air or at anything) uses it. The chakra is only taken when it is used, and
 * then the jutsu rests.
 */
public abstract class ChargedSphereJutsu extends AbstractJutsuItem {

    protected ChargedSphereJutsu(Properties properties) {
        super(properties);
    }

    /** The kind of {@link ChakraSphereEntity} that is the sphere. */
    public abstract int sphereKind();

    /** Never used: this jutsu is used by a left click, see {@link #strike}. */
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
        // A sphere that is already there is put away
        ChakraSphereEntity held = ChakraSphereEntity.findHeld(serverLevel, serverPlayer, sphereKind());
        if (held != null) {
            held.cancel();
            return InteractionResultHolder.consume(stack);
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

        ChakraSphereEntity.spawn(serverLevel, serverPlayer, sphereKind());
        return InteractionResultHolder.consume(stack);
    }

    /** A punch with the item that lands on a living thing uses the sphere there, see {@link ChakraSphereEntity#punchInto}. */
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof ServerPlayer player && attacker.level() instanceof ServerLevel serverLevel) {
            ChakraSphereEntity sphere = ChakraSphereEntity.findHeld(serverLevel, player, sphereKind());
            if (sphere != null) sphere.punchInto(player, target);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    /** The caster punched with a sphere that has grown: it is used, and costs chakra, and the jutsu rests. */
    public void strike(ServerPlayer player, ChakraSphereEntity sphere) {
        Chakra chakra = Services.PLATFORM.getChakra(player);
        if (chakra.getValue() < getChakraCost()) {
            ModUtil.displayColoredMessage(player, "msg.no_chakra", ChatFormatting.RED);
            sphere.cancel();
            return;
        }
        chakra.subValue(getChakraCost(), player);
        sphere.release(player);
        player.getCooldowns().addCooldown(this, getCooldownTicks());
    }
}
