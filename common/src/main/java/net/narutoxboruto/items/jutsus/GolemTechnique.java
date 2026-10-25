package net.narutoxboruto.items.jutsus;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.narutoxboruto.entities.jutsus.GolemEntity;

import java.util.List;

/**
 * Earth Release: Golem Technique. Rank A, 60 chakra and 30 chakra per second while the golem stands, within 25 meters of the
 * caster. A huge, very tough and very strong humanoid of rock rises from the ground, follows the caster and fights what they
 * fight. Requires Earth release affinity.
 */
public class GolemTechnique extends AbstractJutsuItem {

    public GolemTechnique(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public String getJutsuName() {
        return "Golem Technique";
    }

    @Override
    public String getRequiredRelease() {
        return "earth";
    }

    @Override
    public int getChakraCost() {
        return 60;
    }

    @Override
    public int getCooldownTicks() {
        return 20 * 20; // 20 seconds
    }

    @Override
    protected boolean executeJutsu(ServerPlayer serverPlayer, Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        return GolemEntity.spawn(serverLevel, serverPlayer);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.literal("Rank: A").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Range: 25 meters").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Upkeep: 30 chakra per second").withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.literal("A huge golem of rock that guards you and fights with you").withStyle(ChatFormatting.GRAY));
    }
}
