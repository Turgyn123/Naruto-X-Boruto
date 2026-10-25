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
 * Earth Release: Earth Dragon Bullet. Rank C, 30 chakra and 15 chakra per second while the dragon stands, range 20 meters. A dragon head of mud rises out of the ground in
 * front of the caster and spits three balls of mud at what they look at. Requires Earth release affinity.
 */
public class EarthDragonBullet extends AbstractJutsuItem {

    public EarthDragonBullet(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public String getJutsuName() {
        return "Earth Dragon Bullet";
    }

    @Override
    public String getRequiredRelease() {
        return "earth";
    }

    @Override
    public int getChakraCost() {
        return 30;
    }

    @Override
    public int getCooldownTicks() {
        return 20 * 10; // 10 seconds
    }

    @Override
    protected boolean executeJutsu(ServerPlayer serverPlayer, Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        WeaponEffectEntity.spawnEarthDragon(serverLevel, serverPlayer);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.literal("Rank: C").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Range: 20 meters").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Upkeep: 15 chakra per second").withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.literal("A dragon head of mud spits three balls of mud").withStyle(ChatFormatting.GRAY));
    }
}
