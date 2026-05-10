package net.narutoxboruto.dojutsu;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.narutoxboruto.capabilities.info.Dojutsu;
import net.narutoxboruto.main.Main;
import net.narutoxboruto.main.platform.Services;

import java.util.HashSet;
import java.util.Set;

/**
 * Per-dojutsu stat buffs applied while the wearer's eyes are activated (eyesVisible == true).
 *
 * Buff matrix (left + right eye combinations):
 *   single 1-tomoe                    → Strength I
 *   single 2-tomoe                    → Strength I + extra speed
 *   single 3-tomoe                    → Strength II + extra speed
 *   single Byakugan                   → Strength III + extra speed
 *   single Ketsuryugan                → Strength II + extra speed
 *   Byakugan + Ketsuryugan            → Strength III + extra speed
 *   Byakugan + 1/2/3-tomoe            → Strength matches the tomoe number + extra speed
 *   Ketsuryugan + 1/2/3-tomoe         → Strength matches the tomoe number + extra speed
 *
 * "Extra speed" is applied as an attribute modifier (+0.20 ADD_MULTIPLIED_TOTAL) on
 * MOVEMENT_SPEED so it stacks on top of ANY existing speed source (vanilla potions, beacons,
 * other mods, our own stat-events loop, LightningChakraMode, etc.) — equivalent to one
 * additional MOVEMENT_SPEED amplifier level.
 *
 * "Strength N" sets a floor (vanilla picks the max amplifier across sources).
 */
public final class DojutsuBuffs {

    private DojutsuBuffs() {}

    private static final ResourceLocation DOJUTSU_SPEED_ID =
            ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "dojutsu_speed");
    /** +20% multiplicative — matches one vanilla MOVEMENT_SPEED amplifier level. */
    private static final double DOJUTSU_SPEED_BOOST = 0.20;

    /** strengthLevel of 0 means "no strength buff". */
    public record Buffs(int strengthLevel, boolean extraSpeed) {
        public static final Buffs NONE = new Buffs(0, false);
    }

    public static Buffs compute(Dojutsu dojutsu) {
        if (dojutsu == null || !dojutsu.areEyesVisible()) return Buffs.NONE;
        String left = nullToEmpty(dojutsu.getLeftEye());
        String right = nullToEmpty(dojutsu.getRightEye());
        if (left.isEmpty() && right.isEmpty()) return Buffs.NONE;

        Set<String> pair = new HashSet<>();
        if (!left.isEmpty()) pair.add(left);
        if (!right.isEmpty()) pair.add(right);

        if (pair.size() == 1) {
            return switch (pair.iterator().next()) {
                case "1_tomoe_sharingan" -> new Buffs(1, false);
                case "2_tomoe_sharingan" -> new Buffs(2, true);
                case "3_tomoe_sharingan" -> new Buffs(3, true);
                case "byakugan"          -> new Buffs(3, true);
                case "ketsuryugan"       -> new Buffs(2, true);
                default -> Buffs.NONE;
            };
        }

        // pair.size() == 2 — only the listed combinations grant buffs
        boolean hasByak = pair.contains("byakugan");
        boolean hasKetsu = pair.contains("ketsuryugan");
        if (hasByak && hasKetsu) return new Buffs(3, true);
        if (hasByak) {
            if (pair.contains("1_tomoe_sharingan")) return new Buffs(1, true);
            if (pair.contains("2_tomoe_sharingan")) return new Buffs(2, true);
            if (pair.contains("3_tomoe_sharingan")) return new Buffs(3, true);
        }
        if (hasKetsu) {
            if (pair.contains("1_tomoe_sharingan")) return new Buffs(1, true);
            if (pair.contains("2_tomoe_sharingan")) return new Buffs(2, true);
            if (pair.contains("3_tomoe_sharingan")) return new Buffs(3, true);
        }
        return Buffs.NONE;
    }

    public static Buffs forPlayer(Player player) {
        if (player == null) return Buffs.NONE;
        return compute(Services.PLATFORM.getDojutsu(player));
    }

    /**
     * Tick hook: ensure the dojutsu speed-attribute modifier is present iff the dojutsu
     * grants extra speed. Uses a transient modifier so it isn't persisted to disk.
     */
    public static void tickSpeedAttribute(ServerPlayer player) {
        AttributeInstance attr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attr == null) return;
        boolean want = forPlayer(player).extraSpeed();
        boolean has = attr.getModifier(DOJUTSU_SPEED_ID) != null;
        if (want && !has) {
            attr.addTransientModifier(new AttributeModifier(
                    DOJUTSU_SPEED_ID,
                    DOJUTSU_SPEED_BOOST,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        } else if (!want && has) {
            attr.removeModifier(DOJUTSU_SPEED_ID);
        }
    }

    /**
     * Tick hook: refresh the Strength effect if the player's dojutsu wants one and the player's
     * current Strength amplifier (from any source) is below the dojutsu floor. Acts as a floor,
     * never strips other buffs.
     */
    public static void tickStrength(ServerPlayer player) {
        Buffs buffs = forPlayer(player);
        int wantedAmp = buffs.strengthLevel() - 1; // -1 means "no buff requested"
        if (wantedAmp < 0) return;
        MobEffectInstance current = player.getEffect(MobEffects.DAMAGE_BOOST);
        if (current == null || current.getAmplifier() < wantedAmp || current.getDuration() < 20) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, wantedAmp, true, false, false));
        }
    }

    private static String nullToEmpty(String s) { return s == null ? "" : s; }
}
