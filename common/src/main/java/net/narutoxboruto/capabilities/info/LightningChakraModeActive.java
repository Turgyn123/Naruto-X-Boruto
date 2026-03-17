package net.narutoxboruto.capabilities.info;

import com.mojang.serialization.Codec;

public class LightningChakraModeActive {
    private boolean value;

    public static final Codec<LightningChakraModeActive> CODEC = Codec.BOOL.xmap(LightningChakraModeActive::new, LightningChakraModeActive::getValue);

    public LightningChakraModeActive() { this.value = false; }
    public LightningChakraModeActive(boolean value) { this.value = value; }

    public boolean getValue() { return value; }

    public void setValue(boolean value) { this.value = value; }

    public boolean isActive() { return value; }

    public void setActive(boolean active, net.minecraft.server.level.ServerPlayer player) {
        this.value = active;
        net.narutoxboruto.main.platform.Services.PLATFORM.syncLightningChakraModeActive(player, active);
    }

    public void toggle(net.minecraft.server.level.ServerPlayer player) {
        this.value = !this.value;
        net.narutoxboruto.main.platform.Services.PLATFORM.syncLightningChakraModeActive(player, this.value);
    }
}
