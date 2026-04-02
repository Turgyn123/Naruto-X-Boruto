package net.narutoxboruto.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.narutoxboruto.capabilities.PlayerCapData;
import net.narutoxboruto.capabilities.PlayerDataManager;
import net.narutoxboruto.capabilities.info.Chakra;
import net.narutoxboruto.capabilities.info.ChakraControl;
import net.narutoxboruto.capabilities.info.Dojutsu;
import net.narutoxboruto.capabilities.jutsu.JutsuStorage;
import net.narutoxboruto.items.swords.AbstractAbilitySword;
import net.narutoxboruto.items.throwables.FumaShurikenItem;
import net.narutoxboruto.items.throwables.ThrowableWeaponItem;
import net.narutoxboruto.main.platform.ForgePlatformHelper;
import net.narutoxboruto.networking.jutsu.JutsuStorageMenu;
import net.narutoxboruto.util.JutsuGrantHelper;

public class ServerActionPacket {
    private final String action;

    public ServerActionPacket(String action) {
        this.action = action;
    }

    public static void encode(ServerActionPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.action);
    }

    public static ServerActionPacket decode(FriendlyByteBuf buf) {
        return new ServerActionPacket(buf.readUtf());
    }

    public static void handle(ServerActionPacket msg, CustomPayloadEvent.Context ctx) {
        ServerPlayer serverPlayer = ctx.getSender();
        if (serverPlayer == null) return;
        PlayerCapData data = PlayerDataManager.get(serverPlayer);

        switch (msg.action) {
            case "toggle_sword_ability" -> {
                ItemStack stack = serverPlayer.getItemInHand(InteractionHand.MAIN_HAND);
                if (stack.getItem() instanceof AbstractAbilitySword sword) {
                    sword.toggleAbility(serverPlayer);
                }
            }
            case "recharge_chakra" -> {
                Chakra chakra = data.getChakra();
                chakra.addValue(1, serverPlayer);
                serverPlayer.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 1, false, true));
            }
            case "toggle_chakra_control" -> {
                Chakra chakra = data.getChakra();
                if (chakra.getValue() > 0) {
                    ChakraControl control = data.getChakraControl();
                    control.setValue(!control.isActive(), serverPlayer);
                } else {
                    serverPlayer.displayClientMessage(Component.translatable("msg.no_chakra"), true);
                }
            }
            case "special_throw" -> {
                ItemStack stack = serverPlayer.getItemInHand(InteractionHand.MAIN_HAND);
                if (stack.getItem() instanceof ThrowableWeaponItem throwableItem &&
                        !(stack.getItem() instanceof FumaShurikenItem)) {
                    throwableItem.performSpecialThrow(serverPlayer, stack);
                }
            }
            case "open_jutsu_storage" -> {
                JutsuGrantHelper.cleanupDuplicateJutsus(serverPlayer);
                JutsuStorage storage = data.getJutsuStorage();
                serverPlayer.openMenu(new SimpleMenuProvider(
                        (containerId, playerInventory, player) ->
                                new JutsuStorageMenu(containerId, playerInventory, ForgePlatformHelper.toItemStackHandler(storage)),
                        Component.translatable("container.narutoxboruto.jutsu_storage")
                ));
            }
            default -> {
                if (msg.action.startsWith("set_left_eye:")) {
                    String type = msg.action.substring("set_left_eye:".length());
                    Dojutsu dojutsu = data.getDojutsu();
                    dojutsu.setLeftEye(type);
                    data.setDojutsu(dojutsu);
                    dojutsu.syncValue(serverPlayer);
                } else if (msg.action.startsWith("set_right_eye:")) {
                    String type = msg.action.substring("set_right_eye:".length());
                    Dojutsu dojutsu = data.getDojutsu();
                    dojutsu.setRightEye(type);
                    data.setDojutsu(dojutsu);
                    dojutsu.syncValue(serverPlayer);
                } else if (msg.action.startsWith("set_offset_left_eye:") || msg.action.startsWith("set_offset_right_eye:")) {
                    boolean isLeft = msg.action.startsWith("set_offset_left_eye:");
                    String coords = msg.action.substring(msg.action.indexOf(':') + 1);
                    String[] parts = coords.split(",");
                    if (parts.length == 2) {
                        int ox = Integer.parseInt(parts[0]);
                        int oy = Integer.parseInt(parts[1]);
                        Dojutsu dojutsu = data.getDojutsu();
                        if (isLeft) {
                            dojutsu.setLeftEyeOffset(ox, oy);
                        } else {
                            dojutsu.setRightEyeOffset(ox, oy);
                        }
                        data.setDojutsu(dojutsu);
                        dojutsu.syncValue(serverPlayer);
                    }
                } else if (msg.action.startsWith("set_scale_eye:")) {
                    float scale = Float.parseFloat(msg.action.substring("set_scale_eye:".length()));
                    Dojutsu dojutsu = data.getDojutsu();
                    dojutsu.setEyeScale(scale);
                    data.setDojutsu(dojutsu);
                    dojutsu.syncValue(serverPlayer);
                } else if (msg.action.startsWith("set_hide_eye:")) {
                    Dojutsu dojutsu = data.getDojutsu();
                    dojutsu.setEyesVisible(false);
                    data.setDojutsu(dojutsu);
                    dojutsu.syncValue(serverPlayer);
                } else if (msg.action.startsWith("set_show_eye:")) {
                    Dojutsu dojutsu = data.getDojutsu();
                    dojutsu.setEyesVisible(true);
                    data.setDojutsu(dojutsu);
                    dojutsu.syncValue(serverPlayer);
                } else if (msg.action.startsWith("set_reset_eye:")) {
                    Dojutsu dojutsu = data.getDojutsu();
                    dojutsu.resetEyeVisuals();
                    data.setDojutsu(dojutsu);
                    dojutsu.syncValue(serverPlayer);
                }
            }
        }
    }
}
