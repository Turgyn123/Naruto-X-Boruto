package net.narutoxboruto.items.jutsus;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.narutoxboruto.entities.jutsus.WaterCloneEntity;

import java.util.List;

/**
 * Water Release: Water Clone Technique. Rank C, 20 chakra and 20 chakra per second for every clone that stands. Makes a
 * real clone of the caster out of water (up to 3 at a time) that follows them and fights what they fight. Requires Water
 * release affinity.
 */
public class WaterClone extends AbstractJutsuItem {

    public WaterClone(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public String getJutsuName() {
        return "Water Clone Technique";
    }

    @Override
    public String getRequiredRelease() {
        return "water";
    }

    @Override
    public int getChakraCost() {
        return 20;
    }

    @Override
    public int getCooldownTicks() {
        return 20 * 3; // 3 seconds
    }

    @Override
    protected boolean executeJutsu(ServerPlayer serverPlayer, Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        return WaterCloneEntity.spawn(serverLevel, serverPlayer);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.literal("Rank: C").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Range: 0-30 meters (80 with water near)").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Upkeep: 20 chakra per second for each clone").withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.literal("A real clone of water that follows you and fights with you (up to 3)").withStyle(ChatFormatting.GRAY));
    }
}
