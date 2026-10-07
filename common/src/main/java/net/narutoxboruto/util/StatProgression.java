package net.narutoxboruto.util;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.narutoxboruto.capabilities.info.Chakra;
import net.narutoxboruto.capabilities.info.ChakraControl;
import net.narutoxboruto.capabilities.info.MaxChakra;
import net.narutoxboruto.capabilities.info.ShinobiPoints;
import net.narutoxboruto.capabilities.stats.AbstractStat;
import net.narutoxboruto.capabilities.stats.Speed;
import net.narutoxboruto.effect.ModEffects;
import net.narutoxboruto.entities.BossSpawner;
import net.narutoxboruto.items.jutsus.LightningChakraMode;
import net.narutoxboruto.items.swords.Kiba;
import net.narutoxboruto.items.throwables.ThrowableWeaponItem;
import net.narutoxboruto.main.platform.Services;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Platform-independent stat progression and per-tick upkeep. The loaders only forward their damage
 * and tick events here, so NeoForge, Fabric and Forge can't drift apart.
 *
 * Shinobi Points are only paid out when a stat really went up, so a stat at its cap can't be farmed.
 */
public final class StatProgression {

    private StatProgression() {}

    private static final int HITS_PER_POINT = 20;
    private static final int DAMAGE_TAKEN_PER_POINT = 20;

    private static final int CHAKRA_REGEN_INTERVAL = 6000;
    private static final int SPRINT_CHECK_INTERVAL = 100;
    private static final int CHAKRA_CONTROL_DRAIN_INTERVAL = 600;
    private static final int KIBA_DRAIN_INTERVAL = 20;
    private static final int LIGHTNING_MODE_DRAIN_INTERVAL = 100;
    private static final int SPRINT_CM_PER_SPEED_POINT = 150 * 100;

    /** Only touched from the server thread. */
    private static final Map<UUID, Integer> MELEE_HITS = new HashMap<>();
    private static final Map<UUID, Integer> DAMAGE_TAKEN = new HashMap<>();

    /** Call when a player logs out so the counters don't pile up. */
    public static void forget(UUID playerId) {
        MELEE_HITS.remove(playerId);
        DAMAGE_TAKEN.remove(playerId);
        ServerActions.forget(playerId);
    }

    /** The attacker side of a damage event: melee training, and Shurikenjutsu for thrown weapons. */
    public static void onPlayerDealtDamage(ServerPlayer attacker, DamageSource source) {
        if (source.getDirectEntity() == attacker) {
            countMeleeHit(attacker);
        } else if (source.getDirectEntity() instanceof AbstractArrow
                && attacker.getMainHandItem().getItem() instanceof ThrowableWeaponItem) {
            AbstractStat shurikenjutsu = Services.PLATFORM.getShurikenjutsu(attacker);
            if (rewardGain(attacker, shurikenjutsu.incrementValue(1, attacker))) {
                attacker.displayClientMessage(
                        Component.translatable("msg.shurikenjutsu_increased", shurikenjutsu.getValue()), true);
            }
        }
    }

    /**
     * The victim side of a damage event. Only damage dealt by another entity counts, so cactus, fall
     * damage and the like can't be used to farm Medical.
     */
    public static void onPlayerDamaged(ServerPlayer victim, DamageSource source) {
        if (source.getEntity() == null || source.getEntity() == victim) return;

        int taken = DAMAGE_TAKEN.merge(victim.getUUID(), 1, Integer::sum);
        if (taken >= DAMAGE_TAKEN_PER_POINT) {
            DAMAGE_TAKEN.put(victim.getUUID(), 0);
            rewardGain(victim, Services.PLATFORM.getMedical(victim).incrementValue(1, victim));
        }
    }

    private static void countMeleeHit(ServerPlayer attacker) {
        int hits = MELEE_HITS.merge(attacker.getUUID(), 1, Integer::sum);
        if (hits < HITS_PER_POINT) return;
        MELEE_HITS.put(attacker.getUUID(), 0);

        ItemStack held = attacker.getMainHandItem();
        if (held.isEmpty()) {
            rewardGain(attacker, Services.PLATFORM.getTaijutsu(attacker).incrementValue(1, attacker));
        } else if (held.getItem() instanceof SwordItem) {
            rewardGain(attacker, Services.PLATFORM.getKenjutsu(attacker).incrementValue(1, attacker));
        }
    }

    /** Pays one Shinobi Point if the stat actually went up. Returns whether it did. */
    private static boolean rewardGain(ServerPlayer player, int statGain) {
        if (statGain <= 0) return false;
        ShinobiPoints points = Services.PLATFORM.getShinobiPoints(player);
        points.incrementValue(1, player);
        return true;
    }

    /** Per-player upkeep. Call once per server tick for every player. */
    public static void tick(ServerPlayer player) {
        StatAttributes.tick(player);
        BossSpawner.tick(player);

        int tick = player.tickCount;

        if (tick % CHAKRA_REGEN_INTERVAL == 0) {
            regenerateChakra(player);
        }
        if (tick % SPRINT_CHECK_INTERVAL == 0) {
            trainSpeed(player);
        }
        tickChakraControl(player, tick);
        WallClimbing.tickServer(player);
        if (tick % KIBA_DRAIN_INTERVAL == 0) {
            Kiba.tickChakraDrain(player);
        }
        if (tick % LIGHTNING_MODE_DRAIN_INTERVAL == 0) {
            LightningChakraMode.tickChakraDrain(player);
        }
    }

    private static void regenerateChakra(ServerPlayer player) {
        Chakra chakra = Services.PLATFORM.getChakra(player);
        MaxChakra maxChakra = Services.PLATFORM.getMaxChakra(player);
        int missing = maxChakra.getValue() - chakra.getValue();
        if (missing > 0) {
            chakra.addValue(Math.max(1, missing / 5), player);
        }
    }

    private static void trainSpeed(ServerPlayer player) {
        Speed speed = Services.PLATFORM.getSpeed(player);
        if (speed.isMaxed()) return;

        int target = ModUtil.getPlayerStatistics(player, Stats.SPRINT_ONE_CM) / SPRINT_CM_PER_SPEED_POINT;
        if (target > speed.getValue()) {
            rewardGain(player, speed.setValue(target, player));
        }
    }

    /** Chakra Control costs 1 chakra per 30 seconds and switches itself off when the chakra runs out. */
    private static void tickChakraControl(ServerPlayer player, int tick) {
        if (!player.hasEffect(ModEffects.CHAKRA_CONTROL)) return;

        Chakra chakra = Services.PLATFORM.getChakra(player);
        if (chakra.getValue() > 0) {
            if (tick % CHAKRA_CONTROL_DRAIN_INTERVAL == 0) {
                chakra.subValue(1, player);
            }
        } else {
            ChakraControl control = Services.PLATFORM.getChakraControl(player);
            control.setValue(false, player);
            player.displayClientMessage(Component.translatable("msg.no_chakra"), true);
        }
    }
}
