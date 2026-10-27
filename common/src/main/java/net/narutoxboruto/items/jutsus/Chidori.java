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
 * Lightning Release: Chidori. Rank A, 50 chakra. A ball of crackling lightning gathers in the right hand for a second, then the
 * caster dashes forward with it and drives it into the first thing in the way (30 damage). Requires Lightning release affinity.
 */
public class Chidori extends AbstractJutsuItem {

    public Chidori(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public String getJutsuName() {
        return "Chidori";
    }

    @Override
    public String getRequiredRelease() {
        return "lightning";
    }

    @Override
    public int getChakraCost() {
        return 50;
    }

    @Override
    public int getCooldownTicks() {
        return 20 * 15;
    }

    @Override
    protected boolean executeJutsu(ServerPlayer serverPlayer, Level level) {
        if (!(level instanceof ServerLevel serverLevel) || WeaponEffectEntity.hasChidori(serverLevel, serverPlayer)) {
            return false;
        }
        WeaponEffectEntity.spawnChidori(serverLevel, serverPlayer);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.literal("Rank: A").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Gather lightning in your hand, then dash and strike (30 damage)").withStyle(ChatFormatting.GRAY));
    }
}
