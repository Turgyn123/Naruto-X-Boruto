package net.narutoxboruto.items.misc;

import net.narutoxboruto.main.platform.Services;

import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.narutoxboruto.capabilities.info.ReleaseList;
import net.narutoxboruto.util.JutsuGrantHelper;
import net.narutoxboruto.util.ModUtil;

public class ReleaseDnaBottleItem extends Item {
    private final String natureType;

    public ReleaseDnaBottleItem(Properties properties, String natureType) {
        super(properties);
        this.natureType = natureType;
    }

    protected void implementNature(ServerPlayer serverPlayer) {
        ReleaseList releaseList = Services.PLATFORM.getReleaseList(serverPlayer);

        if (!releaseList.getValue().contains(natureType)) {
            if (serverPlayer.getAbilities().instabuild || serverPlayer.getRandom().nextInt(3) == 0) {
                releaseList.concatList(natureType, serverPlayer);
                
                // Grant corresponding jutsu item to player's jutsu storage
                JutsuGrantHelper.grantJutsuForRelease(serverPlayer, natureType);
                
                ModUtil.displayColoredMessage(serverPlayer, "dna_bottle.release.success",
                        "release." + natureType, ChatFormatting.GREEN);
                serverPlayer.getCooldowns().addCooldown(this, 20);
            }
            else {
                ModUtil.displayColoredMessage(serverPlayer, "dna_bottle.release.fail",
                        "release." + natureType, ChatFormatting.RED);
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player player, InteractionHand pHand) {
        ItemStack stack = player.getItemInHand(pHand);

        if (!pLevel.isClientSide) {
            if (!(player instanceof ServerPlayer serverPlayer)) {
                return InteractionResultHolder.fail(stack);
            }

            ReleaseList playerReleaseList = Services.PLATFORM.getReleaseList(serverPlayer);
            if (serverPlayer.getCooldowns().isOnCooldown(this) || playerReleaseList.getValue().contains(natureType)) {
                ModUtil.displayColoredMessage(serverPlayer, "dna_bottle.already_implemented",
                        "release." + natureType, ChatFormatting.YELLOW);
                return InteractionResultHolder.fail(stack);
            }

            // Apply effect instantly
            implementNature(serverPlayer);

            // Play drink sound and consume
            pLevel.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.GENERIC_DRINK,
                    SoundSource.NEUTRAL, 1.0F, 1.0F + (pLevel.random.nextFloat() - pLevel.random.nextFloat()) * 0.4F);
            if (!serverPlayer.getAbilities().instabuild) {
                stack.shrink(1);
            }
            serverPlayer.awardStat(Stats.ITEM_USED.get(this));
        }

        return InteractionResultHolder.sidedSuccess(stack, pLevel.isClientSide());
    }
}
