package net.narutoxboruto.capabilities.info;

import com.mojang.serialization.Codec;
import net.minecraft.world.effect.MobEffectInstance;
import net.narutoxboruto.effect.ModEffects;

public class ChakraControl {
    private boolean value;

    public static final Codec<ChakraControl> CODEC = Codec.BOOL.xmap(ChakraControl::new, ChakraControl::getValue);

    public ChakraControl() { this.value = false; }
    public ChakraControl(boolean value) { this.value = value; }

    public boolean getValue() { return value; }
    public boolean isActive() { return value; }

    public void setValue(boolean value) { this.value = value; }

    public void setValue(boolean value, net.minecraft.server.level.ServerPlayer serverPlayer) {
        this.value = value;
        serverPlayer.removeEffect(ModEffects.CHAKRA_CONTROL);
        if (value) {
            serverPlayer.addEffect(new MobEffectInstance(
                    ModEffects.CHAKRA_CONTROL, -1, 0, false, true, true));
        }
        net.narutoxboruto.main.platform.Services.PLATFORM.syncChakraControl(serverPlayer, value);
    }
}
