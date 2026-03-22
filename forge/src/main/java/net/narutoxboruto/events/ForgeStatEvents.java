package net.narutoxboruto.events;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.narutoxboruto.capabilities.PlayerCapData;
import net.narutoxboruto.capabilities.PlayerDataManager;
import net.narutoxboruto.capabilities.info.*;
import net.narutoxboruto.capabilities.stats.*;
import net.narutoxboruto.effect.ModEffects;
import net.narutoxboruto.items.ForgeItems;
import net.narutoxboruto.items.swords.Kiba;
import net.narutoxboruto.items.jutsus.LightningChakraMode;
import net.narutoxboruto.util.ModUtil;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ForgeStatEvents {

    private static final Map<UUID, Integer> playerHitCounters = new HashMap<>();
    private static final Map<UUID, Integer> playerDamageCounters = new HashMap<>();
    private static final Map<UUID, Integer> kibaDrainTimers = new HashMap<>();
    private static final Map<UUID, Integer> lightningChakraModeDrainTimers = new HashMap<>();
    private static int chakraDrainTimer;

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer serverPlayer) {
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
        }

        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
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
    }

    @SubscribeEvent
    public static void onProjectileLaunch(ProjectileImpactEvent event) {
        if (event.getProjectile() instanceof AbstractArrow arrow) {
            if (arrow.getOwner() instanceof ServerPlayer player) {
                ItemStack heldItem = player.getMainHandItem();

                if (isCustomThrownWeapon(heldItem)) {
                    PlayerCapData data = PlayerDataManager.get(player);
                    Shurikenjutsu shurikenjutsu = data.getShurikenjutsu();
                    shurikenjutsu.incrementValue(1, player);
                    data.getShinobiPoints().incrementValue(1, player);

                    player.displayClientMessage(
                            Component.translatable("msg.shurikenjutsu_increased", shurikenjutsu.getValue()),
                            true
                    );
                }
            }
        }
    }

    private static boolean isCustomThrownWeapon(ItemStack stack) {
        Item item = stack.getItem();
        return item == ForgeItems.SHURIKEN.get() ||
                item == ForgeItems.KUNAI.get() ||
                item == ForgeItems.EXPLOSIVE_KUNAI.get() ||
                item == ForgeItems.SENBON.get() ||
                item == ForgeItems.POISON_SENBON.get() ||
                item == ForgeItems.FUMA_SHURIKEN.get();
    }

    @SubscribeEvent
    public static void addStatBonuses(TickEvent.PlayerTickEvent.Post event) {
        if (event.player instanceof ServerPlayer serverPlayer) {
            if (LightningChakraMode.isActive(serverPlayer)) {
                return;
            }

            Speed speed = PlayerDataManager.get(serverPlayer).getSpeed();
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
    }

    @SubscribeEvent
    public static void tickStats(TickEvent.PlayerTickEvent.Post event) {
        if (event.player instanceof ServerPlayer serverPlayer) {
            PlayerCapData data = PlayerDataManager.get(serverPlayer);

            if (serverPlayer.tickCount % 6000 == 0) {
                Chakra chakra = data.getChakra();
                MaxChakra maxChakra = data.getMaxChakra();
                if (chakra.getValue() < maxChakra.getValue()) {
                    int missingChakra = maxChakra.getValue() - chakra.getValue();
                    int regenAmount = Math.max(1, missingChakra / 5);
                    chakra.addValue(regenAmount, serverPlayer);
                }
            }

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
        }
    }

    @SubscribeEvent
    public static void chakraControl(TickEvent.PlayerTickEvent.Pre event) {
        if (event.player instanceof ServerPlayer serverPlayer) {
            PlayerCapData data = PlayerDataManager.get(serverPlayer);
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
        }
    }

    @SubscribeEvent
    public static void kibaChakraDrain(TickEvent.PlayerTickEvent.Post event) {
        if (event.player instanceof ServerPlayer serverPlayer) {
            UUID playerId = serverPlayer.getUUID();
            int timer = kibaDrainTimers.getOrDefault(playerId, 0);

            if (timer > 0) {
                kibaDrainTimers.put(playerId, timer - 1);
            } else {
                kibaDrainTimers.put(playerId, 20);
                Kiba.tickChakraDrain(serverPlayer);
            }
        }
    }

    @SubscribeEvent
    public static void lightningChakraModeChakraDrain(TickEvent.PlayerTickEvent.Post event) {
        if (event.player instanceof ServerPlayer serverPlayer) {
            UUID playerId = serverPlayer.getUUID();
            int timer = lightningChakraModeDrainTimers.getOrDefault(playerId, 0);

            if (timer > 0) {
                lightningChakraModeDrainTimers.put(playerId, timer - 1);
            } else {
                lightningChakraModeDrainTimers.put(playerId, 100);
                LightningChakraMode.tickChakraDrain(serverPlayer);
            }
        }
    }
}
