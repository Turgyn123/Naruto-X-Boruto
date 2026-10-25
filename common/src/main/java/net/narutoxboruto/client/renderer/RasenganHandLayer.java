package net.narutoxboruto.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.narutoxboruto.client.renderer.entity.ChakraSphereRenderer;
import net.narutoxboruto.entities.jutsus.ChakraSphereEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Draws the Rasengan or Rasenshuriken in the right hand of a player, on the arm itself, so it swings with the arm when the player walks
 * and turns with the body. Drawn like an item in the hand, on the end of the hand.
 */
public class RasenganHandLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public RasenganHandLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        ChakraSphereEntity sphere = ChakraSphereEntity.findHeldClient(player, ChakraSphereEntity.RASENGAN);
        boolean shuriken = false;
        if (sphere == null) {
            sphere = ChakraSphereEntity.findHeldClient(player, ChakraSphereEntity.RASENSHURIKEN);
            shuriken = true;
        }
        if (sphere == null) return;
        poseStack.pushPose();
        if (shuriken) {
            // Not in the frame of the arm: the disc must be flat whatever the arm does. So go to the end of the hand in
            // the frame of the body, from the shoulder along the arm, and then straight up
            ModelPart arm = getParentModel().rightArm;
            Vector3f hand = new Quaternionf().rotationZYX(arm.zRot, arm.yRot, arm.xRot).transform(new Vector3f(0.0F, 0.8F, 0.0F));
            poseStack.translate(arm.x / 16.0F + hand.x, arm.y / 16.0F + hand.y - 1.3F, arm.z / 16.0F + hand.z);
        } else {
            getParentModel().translateToHand(HumanoidArm.RIGHT, poseStack);
            // The arm hangs along +y from the shoulder and the front is -z: the Rasengan is on the end of the hand
            poseStack.translate(-0.03F, 0.8F, -0.02F);
        }
        ChakraSphereRenderer.drawHeld(bufferSource, poseStack, sphere, partialTick);
        poseStack.popPose();
    }
}
