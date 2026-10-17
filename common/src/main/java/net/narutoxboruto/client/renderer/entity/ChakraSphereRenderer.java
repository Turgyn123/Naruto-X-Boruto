package net.narutoxboruto.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
import net.narutoxboruto.client.model.RasenshurikenDiscModel;
import net.narutoxboruto.entities.jutsus.ChakraSphereEntity;
import net.narutoxboruto.main.Main;

/**
 * Draws the {@link ChakraSphereEntity} with models of its own:
 * <ul>
 *   <li>Rasengan: a soft round ball of pale blue and white (a model of see-through cubes spinning inside each other),
 *       drawn in the hand of the caster while it is charged.</li>
 *   <li>Rasenshuriken: held up above the head of the caster while it is charged, as a flat disc of swirling white
 *       wind that spins and grows. When it is thrown it is a bright white ball, and where it hits it grows into a
 *       huge white ball.</li>
 * </ul>
 */
public class ChakraSphereRenderer extends EntityRenderer<ChakraSphereEntity> {

    private static final ResourceLocation RASENGAN_TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/entity/rasengan.png");
    private static final ResourceLocation SHURIKEN_BALL_TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/entity/rasenshuriken.png");
    private static final ResourceLocation DISC_TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/entity/rasenshuriken_disc.png");
    private static final int FULL_BRIGHT = 0xF000F0;
    /** About how far from the middle the ball of the model reaches with a scale of 1: a cube of 12 sixteenths of a block, turned. */
    private static final float BALL_RADIUS = 0.5F;

    private final RasenganModel ball;
    private final RasenshurikenDiscModel disc;

    public ChakraSphereRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.ball = new RasenganModel(context.bakeLayer(RasenganModel.LAYER_LOCATION));
        this.disc = new RasenshurikenDiscModel(context.bakeLayer(RasenshurikenDiscModel.LAYER_LOCATION));
    }

    @Override
    public void render(ChakraSphereEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        boolean shuriken = entity.getKind() == ChakraSphereEntity.RASENSHURIKEN;
        int phase = entity.getPhase();
        float phaseAge = entity.getPhaseTicks() + partialTick;
        float age = entity.tickCount + partialTick;
        float charge = Mth.clamp(phaseAge / (shuriken ? ChakraSphereEntity.RASENSHURIKEN_CHARGE_TICKS : ChakraSphereEntity.RASENGAN_CHARGE_TICKS), 0.0F, 1.0F);

        // In the hand of the caster while charging (and for the Rasengan while it is driven forward), and otherwise
        // where it is
        poseStack.pushPose();
        if (phase == ChakraSphereEntity.CHARGING || (!shuriken && phase == ChakraSphereEntity.ACTIVE)) {
            Entity owner = entity.level().getEntity(entity.getOwnerId());
            if (owner != null) {
                Vec3 hand = ChakraSphereEntity.handPosition(owner, entity.getKind(), partialTick);
                Vec3 here = entity.getPosition(partialTick);
                poseStack.translate(hand.x - here.x, hand.y - here.y, hand.z - here.z);
            }
        }

        if (!shuriken) {
            float radius;
            float alpha = 1.0F;
            if (phase == ChakraSphereEntity.CHARGING) {
                radius = 0.08F + 0.27F * charge;
                if (charge >= 1.0F) radius *= 1.0F + 0.07F * Mth.sin(age * 1.3F);
            } else if (phase == ChakraSphereEntity.ACTIVE) {
                radius = 0.36F;
            } else {
                float t = Mth.clamp(phaseAge / ChakraSphereEntity.RASENGAN_BURST_TICKS, 0.0F, 1.0F);
                radius = 0.36F + 1.7F * t;
                alpha = 1.0F - t;
            }
            drawBall(bufferSource, poseStack, RASENGAN_TEXTURE, radius, age, alpha, 0.32F, 0.9F);
        } else if (phase == ChakraSphereEntity.CHARGING) {
            // A flat disc of swirling white wind, held up above the head, that grows and spins faster as it is charged
            float size = (0.4F + 2.8F * charge) / 1.75F;
            if (charge >= 1.0F) size *= 1.0F + 0.05F * Mth.sin(age * 1.3F);
            VertexConsumer vc = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(DISC_TEXTURE));
            poseStack.pushPose();
            poseStack.scale(size, size, size);
            disc.render(poseStack, vc, FULL_BRIGHT, OverlayTexture.NO_OVERLAY, age * (0.15F + 0.25F * charge), 0.6F);
            poseStack.popPose();
            drawBall(bufferSource, poseStack, SHURIKEN_BALL_TEXTURE, 0.1F + 0.2F * charge, age, 0.9F, 0.35F, 0.95F);
        } else if (phase == ChakraSphereEntity.ACTIVE) {
            drawBall(bufferSource, poseStack, SHURIKEN_BALL_TEXTURE, 1.3F, age, 1.0F, 0.3F, 0.9F);
        } else {
            float grown = Mth.clamp(phaseAge / ChakraSphereEntity.SHURIKEN_GROW_TICKS, 0.0F, 1.0F);
            grown = grown * grown * (3.0F - 2.0F * grown);
            float radius = Mth.lerp(grown, 1.3F, (float) ChakraSphereEntity.SHURIKEN_RADIUS);
            float alpha = Mth.clamp((ChakraSphereEntity.SHURIKEN_BLAST_TICKS - phaseAge) / 6.0F, 0.0F, 1.0F);
            drawBall(bufferSource, poseStack, SHURIKEN_BALL_TEXTURE, radius, age, alpha, 0.2F, 0.6F);
        }
        poseStack.popPose();
    }

    /** The ball model, as big as {@code radius}. */
    private void drawBall(MultiBufferSource bufferSource, PoseStack poseStack, ResourceLocation texture, float radius, float age,
                          float alpha, float cubeAlpha, float coreAlpha) {
        if (alpha < 0.01F) return;
        VertexConsumer vc = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(texture));
        float scale = radius / BALL_RADIUS;
        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);
        ball.render(poseStack, vc, FULL_BRIGHT, OverlayTexture.NO_OVERLAY, age, cubeAlpha * alpha, coreAlpha * alpha);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(ChakraSphereEntity entity) {
        return RASENGAN_TEXTURE;
    }
}
