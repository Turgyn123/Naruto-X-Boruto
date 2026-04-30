package net.narutoxboruto.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.narutoxboruto.capabilities.stats.Speed;
import net.narutoxboruto.main.Main;
import net.narutoxboruto.main.platform.Services;

/**
 * Applies the player's Speed stat as a transient MOVEMENT_SPEED attribute modifier instead of a
 * MobEffect, so it stacks additively with potions, beacons, dojutsu buffs and Lightning Chakra
 * Mode (which all use their own modifiers/effects) rather than clobbering them.
 *
 * Each 10 stat points = +20% MOVEMENT_SPEED (matches one vanilla Speed amplifier level).
 */
public final class StatSpeed {

    private StatSpeed() {}

    private static final ResourceLocation STAT_SPEED_ID =
            ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "stat_speed");

    /** +20% per amplifier level — same as one vanilla MOVEMENT_SPEED level. */
    private static final double PER_LEVEL_BOOST = 0.20;

    public static void tickSpeedAttribute(ServerPlayer player) {
        AttributeInstance attr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attr == null) return;

        Speed speed = Services.PLATFORM.getSpeed(player);
        int level = speed.getValue() / 10;
        double wantedAmount = level * PER_LEVEL_BOOST;

        AttributeModifier existing = attr.getModifier(STAT_SPEED_ID);

        if (wantedAmount <= 0.0) {
            if (existing != null) attr.removeModifier(STAT_SPEED_ID);
            return;
        }

        if (existing == null || existing.amount() != wantedAmount) {
            if (existing != null) attr.removeModifier(STAT_SPEED_ID);
            attr.addTransientModifier(new AttributeModifier(
                    STAT_SPEED_ID,
                    wantedAmount,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }
}
