package net.narutoxboruto.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.narutoxboruto.main.Main;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Earth Release: Iron Skin: the skin a player has while it is on. It is the skin of the player itself (the body and the second layer:
 * hair, clothes), with every colour turned into earth: the light and dark of the skin become the light and dark of hardened earth,
 * and the grain of the dirt block of the game is blended in. It is made once for each skin, from the skin as the game has it on the
 * graphics card, and kept.
 */
public final class IronSkinSkin {

    /** The plain dirt skin, used when the skin of the player cannot be read. */
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/entity/iron_skin.png");
    private static final ResourceLocation DIRT = ResourceLocation.withDefaultNamespace("textures/block/dirt.png");
    private static final Map<ResourceLocation, ResourceLocation> CACHE = new HashMap<>();
    /** Dark, middle and light of the earth (red, green, blue). */
    private static final int[] DARK = {58, 38, 24};
    private static final int[] MID = {132, 94, 64};
    private static final int[] LIGHT = {204, 160, 112};

    private IronSkinSkin() {
    }

    /** The earth version of the skin of this player. */
    public static ResourceLocation textureFor(AbstractClientPlayer player) {
        ResourceLocation skin = player.getSkin().texture();
        ResourceLocation made = CACHE.get(skin);
        if (made == null) {
            made = recolour(skin);
            CACHE.put(skin, made);
        }
        return made;
    }

    private static ResourceLocation recolour(ResourceLocation skin) {
        Minecraft minecraft = Minecraft.getInstance();
        NativeImage source = null;
        NativeImage dirt = null;
        try {
            AbstractTexture texture = minecraft.getTextureManager().getTexture(skin);
            RenderSystem.assertOnRenderThread();
            GlStateManager._bindTexture(texture.getId());
            int width = GlStateManager._getTexLevelParameter(3553, 0, 4096);
            int height = GlStateManager._getTexLevelParameter(3553, 0, 4097);
            if (width <= 0 || height <= 0 || width > 1024 || height > 1024) return TEXTURE;
            source = new NativeImage(width, height, false);
            source.downloadTexture(0, false);
            try (InputStream in = minecraft.getResourceManager().open(DIRT)) {
                dirt = NativeImage.read(in);
            }
            NativeImage made = new NativeImage(width, height, false);
            float scale = width / 64.0F;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixel = source.getPixelRGBA(x, y);
                    int alpha = pixel >>> 24;
                    if (alpha == 0) {
                        made.setPixelRGBA(x, y, 0);
                        continue;
                    }
                    // The pixels are stored as alpha, blue, green, red
                    int red = pixel & 0xFF;
                    int green = (pixel >> 8) & 0xFF;
                    int blue = (pixel >> 16) & 0xFF;
                    float light = (0.3F * red + 0.59F * green + 0.11F * blue) / 255.0F;
                    // A little more contrast, so the details of the skin stay clear in earth
                    light = Math.max(0.0F, Math.min(1.0F, (light - 0.5F) * 1.25F + 0.5F));
                    int[] earth = light < 0.5F ? blend(DARK, MID, light * 2.0F) : blend(MID, LIGHT, (light - 0.5F) * 2.0F);
                    // The grain of the dirt block
                    int grain = dirt.getPixelRGBA(Math.floorMod((int) (x / scale), 16), Math.floorMod((int) (y / scale), 16));
                    float grainLight = (0.3F * (grain & 0xFF) + 0.59F * ((grain >> 8) & 0xFF) + 0.11F * ((grain >> 16) & 0xFF)) / 255.0F;
                    float factor = 0.75F + 0.5F * grainLight;
                    int r = clamp(earth[0] * factor);
                    int g = clamp(earth[1] * factor);
                    int b = clamp(earth[2] * factor);
                    made.setPixelRGBA(x, y, (alpha << 24) | (b << 16) | (g << 8) | r);
                }
            }
            ResourceLocation location = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "iron_skin/" + Integer.toHexString(skin.hashCode()));
            minecraft.getTextureManager().register(location, new DynamicTexture(made));
            return location;
        } catch (Exception | LinkageError e) {
            Main.LOG.warn("Could not make the earth skin of {}: {}", skin, e.toString());
            return TEXTURE;
        } finally {
            if (source != null) source.close();
            if (dirt != null) dirt.close();
        }
    }

    private static int[] blend(int[] from, int[] to, float t) {
        return new int[]{(int) (from[0] + (to[0] - from[0]) * t), (int) (from[1] + (to[1] - from[1]) * t), (int) (from[2] + (to[2] - from[2]) * t)};
    }

    private static int clamp(float value) {
        return Math.max(0, Math.min(255, (int) value));
    }
}
