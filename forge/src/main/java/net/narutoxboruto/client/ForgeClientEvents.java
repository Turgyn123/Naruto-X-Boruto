package net.narutoxboruto.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.narutoxboruto.client.gui.ShinobiStatsGui;
import net.narutoxboruto.capabilities.info.Dojutsu;
import net.narutoxboruto.items.swords.AbstractAbilitySword;
import net.narutoxboruto.items.throwables.FumaShurikenItem;
import net.narutoxboruto.items.throwables.ThrowableWeaponItem;
import net.narutoxboruto.main.Main;
import net.narutoxboruto.main.platform.Services;
import net.narutoxboruto.networking.ForgePacketHandler;
import net.narutoxboruto.networking.ServerActionPacket;
import net.narutoxboruto.particles.ForgeParticles;
import net.narutoxboruto.particles.LightningSparksParticle;
import net.narutoxboruto.util.ModKeyBinds;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class ForgeClientEvents {

    private static int keyPressTime = 0;
    private static boolean iskeyHeldDown = false;
    private static boolean cKeyWasPressed = false;

    @Mod.EventBusSubscriber(modid = Main.MOD_ID, value = Dist.CLIENT)
    public static class GameBusEvents {

        @SubscribeEvent
        public static void clientTicker(TickEvent.PlayerTickEvent.Post event) {
            if (iskeyHeldDown) {
                keyPressTime++;
            } else {
                keyPressTime = 0;
            }
        }

        @SubscribeEvent
        public static void shinobiStatsKeybind(InputEvent.Key event) {
            Minecraft minecraft = Minecraft.getInstance();
            if (ModKeyBinds.OPEN_GUI.consumeClick()) {
                minecraft.setScreen(new ShinobiStatsGui());
            }
            if (ModKeyBinds.DOJUTSU_MENU.consumeClick()) {
                minecraft.setScreen(new net.narutoxboruto.client.gui.DojutsuScreen());
            }
        }

        @SubscribeEvent
        public static void dojutsuToggleKeybind(InputEvent.Key event) {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer player = minecraft.player;
            if (player == null || minecraft.screen != null) return;
            if (ModKeyBinds.DOJUTSU_TOGGLE.consumeClick()) {
                Dojutsu dojutsu = Services.PLATFORM.getDojutsu(player);
                if (dojutsu != null) {
                    if (dojutsu.areEyesVisible()) {
                        dojutsu.setEyesVisible(false);
                        Services.PLATFORM.sendEquipDojutsu("hide", "");
                    } else {
                        dojutsu.setEyesVisible(true);
                        Services.PLATFORM.sendEquipDojutsu("show", "");
                    }
                }
            }
        }

        @SubscribeEvent
        public static void jutsuStorageKeybind(InputEvent.Key event) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.screen == null && ModKeyBinds.JUTSU_STORAGE.consumeClick()) {
                ForgePacketHandler.sendToServer(new ServerActionPacket("open_jutsu_storage"));
            }
        }

        @SubscribeEvent
        public static void specialActionKeybind(TickEvent.ClientTickEvent.Post event) {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer player = minecraft.player;

            if (player == null) return;

            if (ModKeyBinds.SPECIAL_ACTION.consumeClick()) {
                ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
                if (itemStack.getItem() instanceof AbstractAbilitySword) {
                    ForgePacketHandler.sendToServer(new ServerActionPacket("toggle_sword_ability"));
                } else if (itemStack.getItem() instanceof ThrowableWeaponItem &&
                        !(itemStack.getItem() instanceof FumaShurikenItem)) {
                    ForgePacketHandler.sendToServer(new ServerActionPacket("special_throw"));
                }
            }
        }

        @SubscribeEvent
        public static void chakraKeybind(InputEvent.Key event) {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer player = minecraft.player;

            if (player == null) return;

            boolean cKeyPressed = ModKeyBinds.CHAKRA_CONTROL.isDown();

            if (cKeyPressed && !cKeyWasPressed && !player.isCrouching()) {
                ForgePacketHandler.sendToServer(new ServerActionPacket("toggle_chakra_control"));
            }

            cKeyWasPressed = cKeyPressed;
        }

        @SubscribeEvent
        public static void chakraCharge(InputEvent.Key event) {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer player = minecraft.player;
            if (player != null && player.isCrouching() && ModKeyBinds.CHAKRA_RECHARGE.consumeClick()) {
                ForgePacketHandler.sendToServer(new ServerActionPacket("recharge_chakra"));
            }
        }
    }

    @Mod.EventBusSubscriber(modid = Main.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {

        @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event) {
            event.register(ModKeyBinds.SPECIAL_ACTION);
            event.register(ModKeyBinds.CHAKRA_RECHARGE);
            event.register(ModKeyBinds.OPEN_GUI);
            event.register(ModKeyBinds.CHAKRA_CONTROL);
            event.register(ModKeyBinds.JUTSU_STORAGE);
            event.register(ModKeyBinds.DOJUTSU_MENU);
            event.register(ModKeyBinds.DOJUTSU_TOGGLE);
        }

        @SubscribeEvent
        public static void onRegisterParticles(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(ForgeParticles.LIGHTNING_SPARKS.get(), LightningSparksParticle.Provider::new);
        }
    }
}
