package net.narutoxboruto.capabilities.info;

import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;
import net.narutoxboruto.main.platform.Services;

public class NarutoRun {
    private boolean value;

    public static final Codec<NarutoRun> CODEC = Codec.BOOL.xmap(NarutoRun::new, NarutoRun::getValue);

    public NarutoRun() { this.value = false; }
    public NarutoRun(boolean value) { this.value = value; }

    public boolean getValue() { return value; }
    public boolean isActive() { return value; }

    public void setValue(boolean value) { this.value = value; }

    public void setValue(boolean value, ServerPlayer serverPlayer) {
        this.value = value;
        Services.PLATFORM.syncNarutoRun(serverPlayer, value);
    }
}
