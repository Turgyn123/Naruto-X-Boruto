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
 * Earth Release: Earth Spear. Rank C, 25 chakra, range 20 meters. A row of spears of rock bursts out of the ground in front of the caster, one after the other, and throws up what they hit. Requires Earth release affinity.
 */
public class EarthSpear extends AbstractJutsuItem {

    public EarthSpear(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public String getJutsuName() {
        return "Earth Spear";
    }

    @Override
    public String getRequiredRelease() {
        return "earth";
    }

    @Override
    public int getChakraCost() {
        return 25;
    }

    @Override
    public int getCooldownTicks() {
        return 20 * 8; // 8 seconds
    }

    @Override
    protected boolean executeJutsu(ServerPlayer serverPlayer, Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        WeaponEffectEntity.spawnEarthSpears(serverLevel, serverPlayer);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.literal("Rank: C").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Range: 20 meters").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("A row of spears of rock bursts out of the ground in front of you").withStyle(ChatFormatting.GRAY));
    }
}
