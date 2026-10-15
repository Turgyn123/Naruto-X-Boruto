package net.narutoxboruto.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.Util;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.narutoxboruto.items.swords.Hiramekarei;
import net.narutoxboruto.main.Main;
import org.joml.Matrix4f;

import java.util.List;

/**
 * The chakra coat of the Hiramekarei: the blades are wrapped in flowing, glowing chakra.
 *
 * The blade's own model is drawn again, a little larger, with a wisp texture that flows along the blade. It uses the
 * additive, fully bright "eyes" render type, so it glows in the dark and blends into the blade instead of covering it.
 * Three layers, from a bright one lying on the blade to a faint wide haze, so the whole blade is covered in an aura. The loaders' item renderer mixins
 * just call {@link #render} after the item itself has been drawn.
 */
public final class HiramekareiChakraRenderer {

    private HiramekareiChakraRenderer() {}

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID,
            "textures/effect/hiramekarei_chakra.png");

    private static final RandomSource RANDOM = RandomSource.create(42L);

    /** Call after the item itself has been drawn. Does nothing unless this is a Hiramekarei with its coat on. */
    public static void render(ItemStack stack, ItemDisplayContext context, boolean leftHand, PoseStack poseStack,
                              MultiBufferSource buffer, BakedModel model) {
        if (!(stack.getItem() instanceof Hiramekarei blade) || !blade.isCoated(stack)) return;

        float time = (Util.getMillis() % 1_000_000L) / 1000.0F;

        poseStack.pushPose();
        // Back to where the item model itself was drawn: its display transform, then the model's own origin
        model.getTransforms().getTransform(context).apply(leftHand, poseStack);
        poseStack.translate(-0.5F, -0.5F, -0.5F);

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer consumer = buffer.getBuffer(RenderType.eyes(TEXTURE));

        // From the outside in: a faint wide haze, the glow, and the chakra lying on the blade itself. They add up,
        // so the blade is covered in light that fades off softly into the air around it.
        layer(consumer, matrix, model, 0.040F, 0.20F, time * 0.5F, 0.75F);
        layer(consumer, matrix, model, 0.018F, 0.42F, time * 0.75F, 0.95F);
        layer(consumer, matrix, model, 0.006F, 0.85F, time, 1.15F);

        poseStack.popPose();
    }

    private static void layer(VertexConsumer consumer, Matrix4f matrix, BakedModel model, float grow, float strength,
                              float time, float scale) {
        int alpha = (int) (255 * strength);
        for (int i = 0; i <= Direction.values().length; i++) {
            Direction side = i < Direction.values().length ? Direction.values()[i] : null;
            List<BakedQuad> quads = model.getQuads(null, side, RANDOM);
            for (BakedQuad quad : quads) {
                draw(consumer, matrix, quad, grow, alpha, time, scale);
            }
        }
    }

    /**
     * One quad of the blade, pushed a little outwards. It is drawn from both sides: the render type culls back faces,
     * and with only one side the chakra shows up on the edge of the blade and not on its flat faces.
     */
    private static void draw(VertexConsumer consumer, Matrix4f matrix, BakedQuad quad, float grow, int alpha,
                             float time, float scale) {
        int[] data = quad.getVertices();
        float[] px = new float[4];
        float[] py = new float[4];
        float[] pz = new float[4];
        for (int v = 0; v < 4; v++) {
            int o = v * 8;
            px[v] = Float.intBitsToFloat(data[o]);
            py[v] = Float.intBitsToFloat(data[o + 1]);
            pz[v] = Float.intBitsToFloat(data[o + 2]);
        }

        // The real normal of the face, pointing the way the model says the face looks
        float ax = px[1] - px[0], ay = py[1] - py[0], az = pz[1] - pz[0];
        float bx = px[3] - px[0], by = py[3] - py[0], bz = pz[3] - pz[0];
        float nx = ay * bz - az * by;
        float ny = az * bx - ax * bz;
        float nz = ax * by - ay * bx;
        float length = Mth.sqrt(nx * nx + ny * ny + nz * nz);
        if (length < 1.0e-8F) return;
        nx /= length;
        ny /= length;
        nz /= length;
        Direction face = quad.getDirection();
        if (nx * face.getStepX() + ny * face.getStepY() + nz * face.getStepZ() < 0.0F) {
            nx = -nx;
            ny = -ny;
            nz = -nz;
        }

        for (int pass = 0; pass < 2; pass++) {
            float sign = pass == 0 ? 1.0F : -1.0F;
            for (int i = 0; i < 4; i++) {
                int v = pass == 0 ? i : 3 - i;
                // A little outwards, so the chakra wraps the blade
                float x = px[v] + nx * grow;
                float y = py[v] + ny * grow;
                float z = pz[v] + nz * grow;
                // The wisps flow along the blade, which runs up the model, and waver a little sideways
                float u = (x + z) * 9.0F * scale + Mth.sin(time * 1.3F + y * 4.0F) * 0.05F;
                float w = y * 1.1F * scale - time * 0.8F;
                consumer.addVertex(matrix, x, y, z)
                        .setColor(255, 255, 255, alpha)
                        .setUv(u, w)
                        .setOverlay(OverlayTexture.NO_OVERLAY)
                        .setLight(LightTexture.FULL_BRIGHT)
                        .setNormal(nx * sign, ny * sign, nz * sign);
            }
        }
    }
}
