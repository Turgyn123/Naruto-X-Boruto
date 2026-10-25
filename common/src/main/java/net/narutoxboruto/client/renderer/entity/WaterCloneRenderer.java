package net.narutoxboruto.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.narutoxboruto.entities.jutsus.WaterCloneEntity;

import java.util.UUID;

/**
 * Draws the Water Clone as the caster: it has the caster's skin and armor and holds what they hold, but it is see-through
 * and tinted blue, like a body of water.
 */
public class WaterCloneRenderer extends HumanoidMobRenderer<WaterCloneEntity, PlayerModel<WaterCloneEntity>> {

    /** ARGB: pale water blue, about two thirds see-through. */
    private static final int WATER_TINT = (170 << 24) | (140 << 16) | (195 << 8) | 255;

    private final PlayerModel<WaterCloneEntity> wide;
    private final PlayerModel<WaterCloneEntity> slim;

    public WaterCloneRenderer(EntityRendererProvider.Context context) {
        super(context, new Tinted(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
        this.wide = this.model;
        this.slim = new Tinted(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);
        this.addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));
    }

    @Override
    public void render(WaterCloneEntity entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        this.model = skin(entity).model() == PlayerSkin.Model.SLIM ? slim : wide;
        super.render(entity, yaw, partialTick, poseStack, bufferSource, light);
    }

    private static PlayerSkin skin(WaterCloneEntity entity) {
        UUID owner = entity.getOwnerId();
        if (owner == null) return DefaultPlayerSkin.get(entity.getUUID());
        if (Minecraft.getInstance().getConnection() != null) {
            PlayerInfo info = Minecraft.getInstance().getConnection().getPlayerInfo(owner);
            if (info != null) return info.getSkin();
        }
        return DefaultPlayerSkin.get(owner);
    }

    @Override
    public ResourceLocation getTextureLocation(WaterCloneEntity entity) {
        return skin(entity).texture();
    }

    @Override
    protected RenderType getRenderType(WaterCloneEntity entity, boolean bodyVisible, boolean translucent, boolean glowing) {
        return RenderType.entityTranslucent(getTextureLocation(entity));
    }

    /** The player model, but always drawn in the colour of water. */
    private static class Tinted extends PlayerModel<WaterCloneEntity> {
        Tinted(ModelPart part, boolean slim) {
            super(part, slim);
        }

        @Override
        public void renderToBuffer(PoseStack poseStack, VertexConsumer consumer, int light, int overlay, int color) {
            super.renderToBuffer(poseStack, consumer, light, overlay, WATER_TINT);
        }
    }
}
