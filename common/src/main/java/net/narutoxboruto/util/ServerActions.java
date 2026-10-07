package net.narutoxboruto.util;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.narutoxboruto.capabilities.info.Chakra;
import net.narutoxboruto.capabilities.info.ChakraControl;
import net.narutoxboruto.capabilities.info.Dojutsu;
import net.narutoxboruto.capabilities.info.MaxChakra;
import net.narutoxboruto.items.swords.AbstractAbilitySword;
import net.narutoxboruto.items.throwables.FumaShurikenItem;
import net.narutoxboruto.items.throwables.ThrowableWeaponItem;
import net.narutoxboruto.main.platform.Services;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Server-side handling of everything a client can ask for: key presses and the dojutsu screen.
 *
 * Every loader's packet class decodes its own wire format and then calls into here, so the rules
 * live in one place. Nothing the client sends is trusted: the server re-checks that the request
 * makes sense and rate-limits the ones that give a reward.
 */
public final class ServerActions {

    private ServerActions() {}

    /** At most one recharge every 2 ticks, which is about as fast as a human can mash the key. */
    private static final int CHAKRA_RECHARGE_MIN_INTERVAL_TICKS = 2;
    private static final int PRUNE_THRESHOLD = 64;
    private static final int PRUNE_AGE_TICKS = 200;

    /** Only touched from the server thread. */
    private static final Map<UUID, Long> LAST_RECHARGE = new HashMap<>();

    /** Matches the {@code set_<slot>_eye:<value>} actions the Fabric and Forge clients send. */
    private static final Pattern EYE_ACTION = Pattern.compile("set_([a-z_]+)_eye:(.*)", Pattern.DOTALL);

    public static void toggleSwordAbility(ServerPlayer player) {
        ItemStack held = player.getMainHandItem();
        if (held.getItem() instanceof AbstractAbilitySword sword) {
            sword.toggleAbility(player, held);
        }
    }

    public static void specialThrow(ServerPlayer player) {
        ItemStack held = player.getMainHandItem();
        if (held.getItem() instanceof ThrowableWeaponItem throwable && !(held.getItem() instanceof FumaShurikenItem)) {
            throwable.performSpecialThrow(player, held);
        }
    }

    public static void toggleChakraControl(ServerPlayer player) {
        Chakra chakra = Services.PLATFORM.getChakra(player);
        if (chakra.getValue() > 0) {
            ChakraControl control = Services.PLATFORM.getChakraControl(player);
            control.setValue(!control.isActive(), player);
        } else {
            player.displayClientMessage(Component.translatable("msg.no_chakra"), true);
        }
    }

    /** Shift + the recharge key. The server checks the player is really crouching and limits how often it counts. */
    public static void rechargeChakra(ServerPlayer player) {
        if (!player.isCrouching()) return;

        long now = player.level().getGameTime();
        Long last = LAST_RECHARGE.get(player.getUUID());
        if (last != null && now - last < CHAKRA_RECHARGE_MIN_INTERVAL_TICKS) return;
        LAST_RECHARGE.put(player.getUUID(), now);
        if (LAST_RECHARGE.size() > PRUNE_THRESHOLD) {
            LAST_RECHARGE.values().removeIf(time -> now - time > PRUNE_AGE_TICKS);
        }

        Chakra chakra = Services.PLATFORM.getChakra(player);
        MaxChakra maxChakra = Services.PLATFORM.getMaxChakra(player);
        if (chakra.getValue() >= maxChakra.getValue()) return;

        chakra.addValue(1, player);
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 1, false, true));
    }

    /**
     * Applies a change from the dojutsu screen. Unknown slots and malformed values are ignored
     * instead of throwing, and {@link Dojutsu} itself checks ownership and clamps offsets and scale.
     *
     * @param slot  left, right, offset_left, offset_right, scale, hide, show or reset
     * @param value the dojutsu type, an "x,y" pair or a scale, depending on the slot
     */
    public static void equipDojutsu(ServerPlayer player, String slot, String value) {
        Dojutsu dojutsu = Services.PLATFORM.getDojutsu(player);

        switch (slot) {
            case "left" -> dojutsu.setLeftEye(value);
            case "right" -> dojutsu.setRightEye(value);
            case "offset_left", "offset_right" -> {
                int[] offset = parseOffset(value);
                if (offset == null) return;
                if (slot.equals("offset_left")) {
                    dojutsu.setLeftEyeOffset(offset[0], offset[1]);
                } else {
                    dojutsu.setRightEyeOffset(offset[0], offset[1]);
                }
            }
            case "scale" -> {
                Float scale = parseFloat(value);
                if (scale == null) return;
                dojutsu.setEyeScale(scale);
            }
            case "hide" -> dojutsu.setEyesVisible(false);
            case "show" -> dojutsu.setEyesVisible(true);
            case "reset" -> dojutsu.resetEyeVisuals();
            default -> {
                return;
            }
        }

        Services.PLATFORM.setDojutsu(player, dojutsu);
        dojutsu.syncValue(player);
    }

    /**
     * Handles one action string from the Fabric or Forge client.
     *
     * @return false when the action is not one handled here, so the caller can deal with it
     */
    public static boolean handleAction(ServerPlayer player, String action) {
        switch (action) {
            case "toggle_sword_ability" -> toggleSwordAbility(player);
            case "recharge_chakra" -> rechargeChakra(player);
            case "toggle_chakra_control" -> toggleChakraControl(player);
            case "special_throw" -> specialThrow(player);
            default -> {
                Matcher eye = EYE_ACTION.matcher(action);
                if (!eye.matches()) return false;
                equipDojutsu(player, eye.group(1), eye.group(2));
            }
        }
        return true;
    }

    public static void forget(UUID playerId) {
        LAST_RECHARGE.remove(playerId);
    }

    private static int[] parseOffset(String value) {
        String[] parts = value.split(",");
        if (parts.length != 2) return null;
        try {
            return new int[]{Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim())};
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Float parseFloat(String value) {
        try {
            float parsed = Float.parseFloat(value.trim());
            return Float.isNaN(parsed) ? null : parsed;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
