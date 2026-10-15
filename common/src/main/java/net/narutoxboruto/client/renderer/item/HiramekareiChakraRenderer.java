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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * The chakra coat of the Hiramekarei: the blades are wrapped in swirling chakra that has the shape of the weapon.
 *
 * The blade's own model is drawn again in layers, each one a little larger than the last, with a soft pale blue mist
 * texture that drifts along the blade. The layers get fainter the further out they are, so the chakra is thick on
 * the blade and thins out into the air, and the edge of every layer ripples. It is drawn fully bright and
 * see-through. The loaders' item renderer mixins just call {@link #render} after the item itself has been drawn.
 */
public final class HiramekareiChakraRenderer {

    private HiramekareiChakraRenderer() {}

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID,
            "textures/effect/hiramekarei_chakra.png");

    private static final RandomSource RANDOM = RandomSource.create(42L);

    /** The faces of a model, measured once: the four corners of every face and the way it looks. */
    private record Mesh(float[] corners, float[] normals, int faces) {}

    private static final Map<BakedModel, Mesh> MESHES = new WeakHashMap<>();

    /** Call after the item itself has been drawn. Does nothing unless this is a Hiramekarei with its coat on. */
    public static void render(ItemStack stack, ItemDisplayContext context, boolean leftHand, PoseStack poseStack,
                              MultiBufferSource buffer, BakedModel model) {
        if (!(stack.getItem() instanceof Hiramekarei blade) || !blade.isCoated(stack)) return;

        Mesh mesh = MESHES.computeIfAbsent(model, HiramekareiChakraRenderer::measure);
        if (mesh.faces == 0) return;

        float time = (Util.getMillis() % 1_000_000L) / 1000.0F;

        poseStack.pushPose();
        // Back to where the item model itself was drawn: its display transform, then the model's own origin
        model.getTransforms().getTransform(context).apply(leftHand, poseStack);
        poseStack.translate(-0.5F, -0.5F, -0.5F);

        // The chakra does not write depth, so the blade must be drawn first or it paints over the chakra
        if (buffer instanceof MultiBufferSource.BufferSource source) {
            source.endBatch();
        }

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE));

        // From the outside in: grow, strength, flow speed, size of the mist. Each layer flows at its own speed, so
        // the mist crosses itself instead of moving as one.
        if (context != ItemDisplayContext.GUI) {
            layer(consumer, matrix, mesh, time, 0.110F, 0.07F, 0.35F, 0.55F);
            layer(consumer, matrix, mesh, time, 0.075F, 0.11F, 0.45F, 0.65F);
            layer(consumer, matrix, mesh, time, 0.045F, 0.17F, 0.60F, 0.80F);
            layer(consumer, matrix, mesh, time, 0.022F, 0.26F, 0.75F, 0.95F);
        }
        layer(consumer, matrix, mesh, time, 0.009F, 0.38F, 0.90F, 1.10F);
        layer(consumer, matrix, mesh, time, 0.003F, 0.50F, 1.00F, 1.20F);

        poseStack.popPose();
    }

    /** Reads the faces of the blade model once. */
    private static Mesh measure(BakedModel model) {
        List<float[]> faces = new ArrayList<>();
        for (int i = 0; i <= Direction.values().length; i++) {
            Direction side = i < Direction.values().length ? Direction.values()[i] : null;
            for (BakedQuad quad : model.getQuads(null, side, RANDOM)) {
                int[] data = quad.getVertices();
                float[] f = new float[15]; // 4 corners, then the normal
                for (int v = 0; v < 4; v++) {
                    f[v * 3] = Float.intBitsToFloat(data[v * 8]);
                    f[v * 3 + 1] = Float.intBitsToFloat(data[v * 8 + 1]);
                    f[v * 3 + 2] = Float.intBitsToFloat(data[v * 8 + 2]);
                }
                float ax = f[3] - f[0], ay = f[4] - f[1], az = f[5] - f[2];
                float bx = f[9] - f[0], by = f[10] - f[1], bz = f[11] - f[2];
                float nx = ay * bz - az * by;
                float ny = az * bx - ax * bz;
                float nz = ax * by - ay * bx;
                float length = Mth.sqrt(nx * nx + ny * ny + nz * nz);
                if (length < 1.0e-8F) continue;
                nx /= length;
                ny /= length;
                nz /= length;
                Direction face = quad.getDirection();
                if (nx * face.getStepX() + ny * face.getStepY() + nz * face.getStepZ() < 0.0F) {
                    nx = -nx;
                    ny = -ny;
                    nz = -nz;
                }
                f[12] = nx;
                f[13] = ny;
                f[14] = nz;
                faces.add(f);
            }
        }
        float[] corners = new float[faces.size() * 12];
        float[] normals = new float[faces.size() * 3];
        for (int i = 0; i < faces.size(); i++) {
            System.arraycopy(faces.get(i), 0, corners, i * 12, 12);
            System.arraycopy(faces.get(i), 12, normals, i * 3, 3);
        }
        return new Mesh(corners, normals, faces.size());
    }

    /** One layer of chakra: every face of the blade, pushed outwards by {@code grow}, with the mist on it. */
    private static void layer(VertexConsumer consumer, Matrix4f matrix, Mesh mesh, float time, float grow,
                              float strength, float speed, float scale) {
        int alpha = Mth.clamp((int) (strength * 255.0F), 0, 255);
        for (int f = 0; f < mesh.faces; f++) {
            float nx = mesh.normals[f * 3];
            float ny = mesh.normals[f * 3 + 1];
            float nz = mesh.normals[f * 3 + 2];
            for (int v = 0; v < 4; v++) {
                int o = f * 12 + v * 3;
                float px = mesh.corners[o];
                float py = mesh.corners[o + 1];
                float pz = mesh.corners[o + 2];
                // The edge of the layer ripples, so the chakra seems alive
                float ripple = 1.0F + 0.45F * Mth.sin(time * 2.5F * speed + px * 7.0F + py * 5.0F + pz * 9.0F);
                float g = grow * ripple;
                float x = px + nx * g;
                float y = py + ny * g;
                float z = pz + nz * g;
                // The mist drifts along the blade towards the tip, and slowly round it
                float u = (x + z) * 5.0F * scale + time * speed * 0.06F;
                float w = y * 0.9F * scale - time * speed * 0.55F;
                consumer.addVertex(matrix, x, y, z)
                        .setColor(255, 255, 255, alpha)
                        .setUv(u, w)
                        .setOverlay(OverlayTexture.NO_OVERLAY)
                        .setLight(LightTexture.FULL_BRIGHT)
                        .setNormal(nx, ny, nz);
            }
        }
    }
}
