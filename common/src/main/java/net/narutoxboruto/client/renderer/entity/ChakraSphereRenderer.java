package net.narutoxboruto.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.narutoxboruto.client.model.RasenganModel;
import net.narutoxboruto.client.model.RasenshurikenBladesModel;
import net.narutoxboruto.entities.jutsus.ChakraSphereEntity;
import net.narutoxboruto.main.Main;

/**
 * Draws the {@link ChakraSphereEntity}: the ball of the Rasengan, which is a model of cubes spinning inside each other,
 * and for the Rasenshuriken the ball with a model of four blades of wind spinning round it. While it is charged it is
 * drawn in the hand of the caster, so it does not lag behind them. When the Rasenshuriken hits it grows into a huge
 * see-through ball with three sets of blades spinning inside it.
 */
public class ChakraSphereRenderer extends EntityRenderer<ChakraSphereEntity> {

    private static final ResourceLocation BALL_TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/entity/rasengan.png");
    private static final ResourceLocation BLADES_TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/entity/rasenshuriken.png");
    private static final int FULL_BRIGHT = 0xF000F0;
    /** The size of the biggest cube of the ball: 18 sixteenths of a block. */
    private static final float BALL_DIAMETER = 18.0F / 16.0F;
    /** How far the blades reach, with a scale of 1: 25 sixteenths of a block. */
    private static final float BLADE_REACH = 25.0F / 16.0F;

    private final RasenganModel ball;
    private final RasenshurikenBladesModel blades;

    public ChakraSphereRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.ball = new RasenganModel(context.bakeLayer(RasenganModel.LAYER_LOCATION));
        this.blades = new RasenshurikenBladesModel(context.bakeLayer(RasenshurikenBladesModel.LAYER_LOCATION));
    }

    @Override
    public void render(ChakraSphereEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        boolean shuriken = entity.getKind() == ChakraSphereEntity.RASENSHURIKEN;
        int phase = entity.getPhase();
        float phaseAge = entity.getPhaseTicks() + partialTick;
        float age = entity.tickCount + partialTick;

        // Where it is and the way it faces: in the hand of the caster while charging (and for the Rasengan while it
        // is driven forward), and otherwise where it is
        float yaw = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
        float pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        poseStack.pushPose();
        if (phase == ChakraSphereEntity.CHARGING || (!shuriken && phase == ChakraSphereEntity.ACTIVE)) {
            Entity owner = entity.level().getEntity(entity.getOwnerId());
            if (owner != null) {
                Vec3 hand = ChakraSphereEntity.handPosition(owner, partialTick);
                Vec3 here = entity.getPosition(partialTick);
                poseStack.translate(hand.x - here.x, hand.y - here.y, hand.z - here.z);
                yaw = owner.getViewYRot(partialTick);
                pitch = owner.getViewXRot(partialTick);
            }
        }

        // How big the ball is, how see-through, and how big the blades are
        float charge = Mth.clamp(phaseAge / (shuriken ? ChakraSphereEntity.RASENSHURIKEN_CHARGE_TICKS : ChakraSphereEntity.RASENGAN_CHARGE_TICKS), 0.0F, 1.0F);
        float ballScale;
        float bladeScale = 0.0F;
        float alpha = 1.0F;
        boolean blast = false;
        if (!shuriken) {
            if (phase == ChakraSphereEntity.CHARGING) {
                ballScale = 0.2F + 0.35F * charge;
                if (charge >= 1.0F) ballScale *= 1.0F + 0.07F * Mth.sin(age * 1.3F);
            } else if (phase == ChakraSphereEntity.ACTIVE) {
                ballScale = 0.6F;
            } else {
                float t = Mth.clamp(phaseAge / ChakraSphereEntity.RASENGAN_BURST_TICKS, 0.0F, 1.0F);
                ballScale = 0.6F + 2.4F * t;
                alpha = 1.0F - t;
            }
        } else if (phase == ChakraSphereEntity.CHARGING) {
            ballScale = 0.2F + 0.4F * charge;
            bladeScale = 0.25F + 0.4F * charge;
            if (charge >= 1.0F) {
                float pulse = 1.0F + 0.07F * Mth.sin(age * 1.3F);
                ballScale *= pulse;
                bladeScale *= pulse;
            }
        } else if (phase == ChakraSphereEntity.ACTIVE) {
            ballScale = 0.65F;
            bladeScale = 0.75F;
        } else {
            blast = true;
            float grown = Mth.clamp(phaseAge / ChakraSphereEntity.SHURIKEN_GROW_TICKS, 0.0F, 1.0F);
            grown = grown * grown * (3.0F - 2.0F * grown);
            ballScale = Mth.lerp(grown, 0.65F, 2.0F * (float) ChakraSphereEntity.SHURIKEN_RADIUS / BALL_DIAMETER);
            bladeScale = Mth.lerp(grown, 0.75F, (float) ChakraSphereEntity.SHURIKEN_RADIUS / BLADE_REACH);
            alpha = Mth.clamp((ChakraSphereEntity.SHURIKEN_BLAST_TICKS - phaseAge) / 6.0F, 0.0F, 1.0F);
        }
        if (alpha < 0.01F) {
            poseStack.popPose();
            return;
        }

        // The model's z axis points the way the jutsu is aimed
        poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));

        // The blades first, behind the ball
        if (shuriken) {
            RenderType bladeType = RenderType.entityTranslucentEmissive(BLADES_TEXTURE);
            VertexConsumer bladeBuffer = bufferSource.getBuffer(bladeType);
            if (!blast) {
                poseStack.pushPose();
                poseStack.scale(bladeScale, bladeScale, bladeScale);
                blades.render(poseStack, bladeBuffer, FULL_BRIGHT, OverlayTexture.NO_OVERLAY, age * 0.55F, alpha);
                poseStack.popPose();
                poseStack.pushPose();
                poseStack.scale(bladeScale * 0.7F, bladeScale * 0.7F, bladeScale * 0.7F);
                blades.render(poseStack, bladeBuffer, FULL_BRIGHT, OverlayTexture.NO_OVERLAY, -age * 0.8F + 0.4F, alpha * 0.8F);
                poseStack.popPose();
            } else {
                // Three sets of blades spinning in different planes fill the sphere
                for (int k = 0; k < 3; k++) {
                    poseStack.pushPose();
                    if (k == 1) poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
                    if (k == 2) poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                    poseStack.scale(bladeScale, bladeScale, bladeScale);
                    blades.render(poseStack, bladeBuffer, FULL_BRIGHT, OverlayTexture.NO_OVERLAY, age * (0.5F + 0.15F * k) * (k == 1 ? -1.0F : 1.0F), alpha * 0.8F);
                    poseStack.popPose();
                }
            }
            flush(bufferSource, bladeType);
        }

        // The ball. The huge sphere of the Rasenshuriken is more see-through, so that the blades show through it
        RenderType ballType = RenderType.entityTranslucentEmissive(BALL_TEXTURE);
        VertexConsumer ballBuffer = bufferSource.getBuffer(ballType);
        poseStack.pushPose();
        poseStack.scale(ballScale, ballScale, ballScale);
        ball.render(poseStack, ballBuffer, FULL_BRIGHT, OverlayTexture.NO_OVERLAY, age, blast ? alpha * 0.45F : alpha);
        poseStack.popPose();
        flush(bufferSource, ballType);
        poseStack.popPose();
    }

    /** Draws what has been collected for this render type now, so the layers keep their order. */
    private static void flush(MultiBufferSource bufferSource, RenderType type) {
        if (bufferSource instanceof MultiBufferSource.BufferSource source) source.endBatch(type);
    }

    @Override
    public ResourceLocation getTextureLocation(ChakraSphereEntity entity) {
        return BALL_TEXTURE;
    }
}
