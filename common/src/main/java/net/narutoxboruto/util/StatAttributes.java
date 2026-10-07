package net.narutoxboruto.util;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.narutoxboruto.main.Main;
import net.narutoxboruto.main.platform.Services;

/**
 * Turns the Medical and Taijutsu stats into attribute modifiers, and calls {@link StatSpeed} for Speed.
 *
 * The modifiers are transient and re-checked every tick, so the bonuses come back by themselves
 * after death, a dimension change or a relog. They also stack with other mods and effects instead
 * of overwriting the attribute base the way the old one-shot {@code setBaseValue} did.
 */
public final class StatAttributes {

    private StatAttributes() {}

    /** Vanilla player base values. Older versions of this mod wrote the stat bonus straight into the base. */
    private static final double PLAYER_BASE_MAX_HEALTH = 20.0D;
    private static final double PLAYER_BASE_ATTACK_DAMAGE = 1.0D;

    /** +1 heart per Medical point. */
    private static final double HEALTH_PER_MEDICAL = 2.0D;
    private static final double DAMAGE_PER_TAIJUTSU = 0.04D;

    private static final ResourceLocation MEDICAL_ID = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "stat_medical");
    private static final ResourceLocation TAIJUTSU_ID = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "stat_taijutsu");

    public static void tick(ServerPlayer player) {
        StatSpeed.tickSpeedAttribute(player);

        apply(player, Attributes.MAX_HEALTH, PLAYER_BASE_MAX_HEALTH, MEDICAL_ID,
                Services.PLATFORM.getMedical(player).getValue() * HEALTH_PER_MEDICAL);
        apply(player, Attributes.ATTACK_DAMAGE, PLAYER_BASE_ATTACK_DAMAGE, TAIJUTSU_ID,
                Services.PLATFORM.getTaijutsu(player).getValue() * DAMAGE_PER_TAIJUTSU);
    }

    private static void apply(ServerPlayer player, Holder<Attribute> attribute, double vanillaBase,
                              ResourceLocation id, double wantedAmount) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;

        // Migrates worlds saved by older versions, which stored the bonus in the base value.
        if (instance.getBaseValue() != vanillaBase) {
            instance.setBaseValue(vanillaBase);
        }

        AttributeModifier existing = instance.getModifier(id);
        if (wantedAmount <= 0.0D) {
            if (existing != null) instance.removeModifier(id);
            return;
        }
        if (existing == null || existing.amount() != wantedAmount) {
            if (existing != null) instance.removeModifier(id);
            instance.addTransientModifier(new AttributeModifier(id, wantedAmount, AttributeModifier.Operation.ADD_VALUE));
        }
    }
}
