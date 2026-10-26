package net.narutoxboruto.items.jutsus;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.narutoxboruto.entities.effects.WeaponEffectEntity;

import java.util.List;

/**
 * Earth Release: Sandwich. Rank B, 50 chakra, range 30 meters. Two huge walls of rock rise on both sides of where the caster aims, stand a moment, then slam together and crush everything between them. Requires Earth release affinity.
 */
public class EarthSandwich extends AbstractJutsuItem {

    public EarthSandwich(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public String getJutsuName() {
        return "Earth Sandwich";
    }

    @Override
    public String getRequiredRelease() {
        return "earth";
    }

    @Override
    public int getChakraCost() {
        return 50;
    }

    @Override
    public int getCooldownTicks() {
        return 20 * 30; // 30 seconds
    }

    @Override
    protected boolean executeJutsu(ServerPlayer serverPlayer, Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        WeaponEffectEntity.spawnSandwich(serverLevel, serverPlayer);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.literal("Rank: B").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Range: 30 meters").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Two huge walls of rock slam together and crush what is between them").withStyle(ChatFormatting.GRAY));
    }
}
