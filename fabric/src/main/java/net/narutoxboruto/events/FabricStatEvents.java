package net.narutoxboruto.events;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.narutoxboruto.capabilities.PlayerCapData;
import net.narutoxboruto.capabilities.PlayerDataManager;
import net.narutoxboruto.capabilities.info.*;
import net.narutoxboruto.capabilities.stats.*;
import net.narutoxboruto.effect.ModEffects;
import net.narutoxboruto.items.FabricItems;
import net.narutoxboruto.items.swords.Kiba;
import net.narutoxboruto.items.jutsus.LightningChakraMode;
import net.narutoxboruto.util.ModUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FabricStatEvents {

    private static final Map<UUID, Integer> playerHitCounters = new HashMap<>();
    private static final Map<UUID, Integer> playerDamageCounters = new HashMap<>();
    private static final Map<UUID, Integer> kibaDrainTimers = new HashMap<>();
    private static final Map<UUID, Integer> lightningChakraModeDrainTimers = new HashMap<>();
    private static int chakraDrainTimer;

    public static void register() {
        // Damage events
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamage, damageTaken, blocked) -> {
            // Attacker stats
            if (source.getEntity() instanceof ServerPlayer serverPlayer) {
                UUID playerId = serverPlayer.getUUID();
                PlayerCapData data = PlayerDataManager.get(serverPlayer);

                int currentHits = playerHitCounters.getOrDefault(playerId, 0) + 1;
                playerHitCounters.put(playerId, currentHits);

                if (currentHits >= 20) {
                    if (serverPlayer.getMainHandItem().isEmpty()) {
                        Taijutsu taijutsu = data.getTaijutsu();
                        taijutsu.incrementValue(1, serverPlayer);
                        data.getShinobiPoints().incrementValue(1, serverPlayer);
                    } else if (serverPlayer.getMainHandItem().getItem() instanceof SwordItem) {
                        Kenjutsu kenjutsu = data.getKenjutsu();
                        kenjutsu.incrementValue(1, serverPlayer);
                        data.getShinobiPoints().incrementValue(1, serverPlayer);
                    }
                    playerHitCounters.put(playerId, 0);
                }

                // Shurikenjutsu leveling from projectile hits
                if (source.getDirectEntity() instanceof AbstractArrow) {
                    ItemStack heldItem = serverPlayer.getMainHandItem();
                    if (isCustomThrownWeapon(heldItem)) {
                        Shurikenjutsu shurikenjutsu = data.getShurikenjutsu();
                        shurikenjutsu.incrementValue(1, serverPlayer);
                        data.getShinobiPoints().incrementValue(1, serverPlayer);
                        serverPlayer.displayClientMessage(
                                Component.translatable("msg.shurikenjutsu_increased", shurikenjutsu.getValue()),
                                true
                        );
                    }
                }
            }

            // Defender stats
            if (entity instanceof ServerPlayer serverPlayer) {
                UUID playerId = serverPlayer.getUUID();
                PlayerCapData data = PlayerDataManager.get(serverPlayer);

                int currentDamageTaken = playerDamageCounters.getOrDefault(playerId, 0) + 1;
                playerDamageCounters.put(playerId, currentDamageTaken);

                if (currentDamageTaken >= 20) {
                    Medical medical = data.getMedical();
                    medical.incrementValue(1, serverPlayer);
                    data.getShinobiPoints().incrementValue(1, serverPlayer);

                    playerDamageCounters.put(playerId, 0);
                }
            }
        });

        // Tick-based events
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers()) {
                PlayerCapData data = PlayerDataManager.get(serverPlayer);

                // Speed bonuses
                if (!LightningChakraMode.isActive(serverPlayer)) {
                    Speed speed = data.getSpeed();
                    int speedLevel = speed.getValue() / 10;

                    if (speedLevel > 0) {
                        MobEffectInstance currentEffect = serverPlayer.getEffect(MobEffects.MOVEMENT_SPEED);
                        if (currentEffect == null || currentEffect.getAmplifier() != speedLevel - 1) {
                            serverPlayer.removeEffect(MobEffects.MOVEMENT_SPEED);
                            serverPlayer.addEffect(
                                    new MobEffectInstance(MobEffects.MOVEMENT_SPEED, -1, speedLevel - 1,
                                            false, false, true));
                        }
                    } else {
                        serverPlayer.removeEffect(MobEffects.MOVEMENT_SPEED);
                    }
                }

                // Chakra regen every 6000 ticks
                if (serverPlayer.tickCount % 6000 == 0) {
                    Chakra chakra = data.getChakra();
                    MaxChakra maxChakra = data.getMaxChakra();
                    if (chakra.getValue() < maxChakra.getValue()) {
                        int missingChakra = maxChakra.getValue() - chakra.getValue();
                        int regenAmount = Math.max(1, missingChakra / 5);
                        chakra.addValue(regenAmount, serverPlayer);
                    }
                }

                // Speed stat from sprinting
                if (serverPlayer.tickCount % 100 == 0) {
                    Speed speed = data.getSpeed();
                    int distanceSprinted = ModUtil.getPlayerStatistics(serverPlayer, Stats.SPRINT_ONE_CM) / 100;
                    int targetSpeedValue = distanceSprinted / 150;
                    int currentSpeedValue = speed.getValue();

                    if (targetSpeedValue > currentSpeedValue) {
                        speed.setValue(targetSpeedValue, serverPlayer);
                        data.getShinobiPoints().incrementValue(serverPlayer);
                    }
                }

                // Chakra control drain
                ChakraControl chakraControl = data.getChakraControl();
                if (serverPlayer.hasEffect(ModEffects.CHAKRA_CONTROL)) {
                    Chakra chakra = data.getChakra();
                    if (chakra.getValue() > 0) {
                        if (chakraDrainTimer > 0) {
                            chakraDrainTimer--;
                        } else {
                            chakraDrainTimer = 600;
                            chakra.subValue(1, serverPlayer);
                        }
                    } else {
                        chakraControl.setValue(false, serverPlayer);
                        chakraDrainTimer = 0;
                        serverPlayer.displayClientMessage(Component.translatable("msg.no_chakra"), true);
                    }
                }

                // Kiba chakra drain
                UUID playerId = serverPlayer.getUUID();
                int kibaTimer = kibaDrainTimers.getOrDefault(playerId, 0);
                if (kibaTimer > 0) {
                    kibaDrainTimers.put(playerId, kibaTimer - 1);
                } else {
                    kibaDrainTimers.put(playerId, 20);
                    Kiba.tickChakraDrain(serverPlayer);
                }

                // Lightning chakra mode drain
                int lcmTimer = lightningChakraModeDrainTimers.getOrDefault(playerId, 0);
                if (lcmTimer > 0) {
                    lightningChakraModeDrainTimers.put(playerId, lcmTimer - 1);
                } else {
                    lightningChakraModeDrainTimers.put(playerId, 100);
                    LightningChakraMode.tickChakraDrain(serverPlayer);
                }
            }
        });
    }

    private static boolean isCustomThrownWeapon(ItemStack stack) {
        return stack.getItem() == FabricItems.SHURIKEN ||
                stack.getItem() == FabricItems.KUNAI ||
                stack.getItem() == FabricItems.EXPLOSIVE_KUNAI ||
                stack.getItem() == FabricItems.SENBON ||
                stack.getItem() == FabricItems.POISON_SENBON ||
                stack.getItem() == FabricItems.FUMA_SHURIKEN;
    }
}
