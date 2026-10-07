package net.narutoxboruto.dojutsu;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.narutoxboruto.capabilities.info.Dojutsu;
import net.narutoxboruto.main.platform.Services;

/**
 * Server-side dojutsu upkeep, run once per tick for every player: the strength and speed buffs, the
 * clan awakening timer, and the Sharingan playtime and near-death upgrades.
 */
public final class DojutsuProgression {

    private DojutsuProgression() {}

    public static void tick(ServerPlayer serverPlayer) {
        // Dojutsu timer: tick towards dojutsu acquisition
        var dojutsu = Services.PLATFORM.getDojutsu(serverPlayer);
        String clan = Services.PLATFORM.getClan(serverPlayer).getValue();

        // Apply per-dojutsu strength buff while activated (independent of clan/eligibility)
        DojutsuBuffs.tickStrength(serverPlayer);
        DojutsuBuffs.tickSpeedAttribute(serverPlayer);

        if (dojutsu.isClanEligible(clan) && dojutsu.canObtainMore()) {
            String clanDojutsu = dojutsu.getDojutsuForClan(clan);
            if (clanDojutsu != null && !dojutsu.hasClanDojutsuFamily(clan)) {
                dojutsu.incrementTimer();
                if (dojutsu.isTimerComplete()) {
                    dojutsu.unlock(clanDojutsu);
                    dojutsu.resetTimer();
                    dojutsu.syncValue(serverPlayer);
                    serverPlayer.displayClientMessage(
                            Component.translatable("dojutsu.acquired",
                                    Component.translatable("dojutsu." + clanDojutsu)), false);
                } else if (dojutsu.getTimer() % 20 == 0) {
                    // Sync every second to keep client timer updated
                    dojutsu.syncValue(serverPlayer);
                }
            }
        }

        // ── Sharingan progression: playtime + near-death tier upgrades ──
        // Runs whenever the player owns ANY sharingan tier (so admin-given sharingan still
        // progresses even if the player isn't currently in the Uchiha clan), or while in the
        // Uchiha clan (so first-tier acquisition via natural play still ticks playtime).
        boolean hasAnySharingan = dojutsu.hasUnlocked("1_tomoe_sharingan")
                || dojutsu.hasUnlocked("2_tomoe_sharingan")
                || dojutsu.hasUnlocked("3_tomoe_sharingan");
        if ("uchiha".equals(clan) || hasAnySharingan) {
            dojutsu.incrementSharinganPlaytime();

            // Track consecutive low-HP ticks for near-death detection
            if (serverPlayer.isAlive() && serverPlayer.getHealth() <= Dojutsu.NEAR_DEATH_HP_THRESHOLD) {
                dojutsu.incrementLowHpTicks();
                if (dojutsu.getLowHpTicks() >= Dojutsu.NEAR_DEATH_DURATION_TICKS) {
                    // Bind the near-death event to the highest currently unlocked sharingan tier.
                    boolean changed = false;
                    if (dojutsu.hasUnlocked("2_tomoe_sharingan") && !dojutsu.hasNearDeathWith2Tomoe()) {
                        dojutsu.markNearDeathWith2Tomoe();
                        changed = true;
                    } else if (dojutsu.hasUnlocked("1_tomoe_sharingan")
                            && !dojutsu.hasUnlocked("2_tomoe_sharingan")
                            && !dojutsu.hasNearDeathWith1Tomoe()) {
                        dojutsu.markNearDeathWith1Tomoe();
                        changed = true;
                    }
                    dojutsu.resetLowHpTicks();
                    if (changed) dojutsu.syncValue(serverPlayer);
                }
            } else {
                if (dojutsu.getLowHpTicks() != 0) dojutsu.resetLowHpTicks();
            }

            // 1-tomoe → 2-tomoe upgrade
            if (dojutsu.hasUnlocked("1_tomoe_sharingan")
                    && !dojutsu.hasUnlocked("2_tomoe_sharingan")
                    && dojutsu.hasNearDeathWith1Tomoe()
                    && dojutsu.getSharinganPlaytime() >= Dojutsu.SHARINGAN_2_TOMOE_PLAYTIME_TICKS) {
                dojutsu.upgrade("1_tomoe_sharingan", "2_tomoe_sharingan");
                dojutsu.syncValue(serverPlayer);
                serverPlayer.displayClientMessage(
                        Component.translatable("dojutsu.acquired",
                                Component.translatable("dojutsu.2_tomoe_sharingan")), false);
            }
            // 2-tomoe → 3-tomoe upgrade
            else if (dojutsu.hasUnlocked("2_tomoe_sharingan")
                    && !dojutsu.hasUnlocked("3_tomoe_sharingan")
                    && dojutsu.hasNearDeathWith2Tomoe()
                    && dojutsu.getSharinganPlaytime() >= Dojutsu.SHARINGAN_3_TOMOE_PLAYTIME_TICKS) {
                dojutsu.upgrade("2_tomoe_sharingan", "3_tomoe_sharingan");
                dojutsu.syncValue(serverPlayer);
                serverPlayer.displayClientMessage(
                        Component.translatable("dojutsu.acquired",
                                Component.translatable("dojutsu.3_tomoe_sharingan")), false);
            }
        }
    }
}
