package net.narutoxboruto.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

/**
 * Glowing light like {@code RenderType.lightning()} (added to what is behind it), but it does not write to the depth
 * buffer. The lightning type does, and what it writes hides everything that is drawn after it behind it, even where it is
 * almost see-through. The water is drawn after the entities, so a soft glow over the water would cut a dark hole in it.
 * The subclass is only there to reach the render states, which are protected.
 */
public final class GlowRenderType extends RenderType {

    public static final RenderType GLOW = RenderType.create("narutoxboruto_glow", DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS, 1536, false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_LIGHTNING_SHADER)
                    .setWriteMaskState(COLOR_WRITE)
                    .setTransparencyState(LIGHTNING_TRANSPARENCY)
                    .createCompositeState(false));

    private GlowRenderType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling,
                           boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }
}
