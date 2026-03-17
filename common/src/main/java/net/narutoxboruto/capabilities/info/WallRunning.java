package net.narutoxboruto.capabilities.info;

import com.mojang.serialization.Codec;
import net.narutoxboruto.util.RotationUtil;

public class WallRunning {
    private boolean value;
    private RotationUtil.Surface surface = RotationUtil.Surface.GROUND;

    public static final Codec<WallRunning> CODEC = Codec.BOOL.xmap(WallRunning::new, WallRunning::getValue);

    public WallRunning() { this.value = false; }
    public WallRunning(boolean value) { this.value = value; }

    public boolean getValue() { return value; }
    public void setValue(boolean value) { this.value = value; }

    public RotationUtil.Surface getSurface() { return surface; }
    public void setSurface(RotationUtil.Surface surface) { this.surface = surface; }
    public void setSurface(RotationUtil.Surface surface, net.minecraft.server.level.ServerPlayer serverPlayer) {
        this.surface = surface;
    }
}
