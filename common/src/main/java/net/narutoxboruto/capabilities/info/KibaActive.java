package net.narutoxboruto.capabilities.info;

import com.mojang.serialization.Codec;

public class KibaActive {
    private boolean value;

    public static final Codec<KibaActive> CODEC = Codec.BOOL.xmap(KibaActive::new, KibaActive::getValue);

    public KibaActive() { this.value = false; }
    public KibaActive(boolean value) { this.value = value; }

    public boolean getValue() { return value; }

    public void setValue(boolean value) { this.value = value; }

    public boolean isActive() { return value; }
    public void setActive(boolean active, net.minecraft.server.level.ServerPlayer player) {
        this.value = active;
        net.narutoxboruto.main.platform.Services.PLATFORM.syncKibaActive(player, active);
    }
    public void toggle(net.minecraft.server.level.ServerPlayer player) {
        this.value = !this.value;
        net.narutoxboruto.main.platform.Services.PLATFORM.syncKibaActive(player, this.value);
    }
}
