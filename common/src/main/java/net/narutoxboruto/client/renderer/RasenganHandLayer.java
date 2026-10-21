package net.narutoxboruto.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.narutoxboruto.client.renderer.entity.ChakraSphereRenderer;
import net.narutoxboruto.entities.jutsus.ChakraSphereEntity;

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
        getParentModel().translateToHand(HumanoidArm.RIGHT, poseStack);
        // The arm hangs along +y from the shoulder and the front is -z: the Rasengan is on the end of the hand, and the
        // Rasenshuriken is above the hand of the raised arm (see RaisedArmPose)
        if (shuriken) {
            poseStack.translate(0.0F, 1.3F, 0.0F);
        } else {
            poseStack.translate(-0.03F, 0.8F, -0.02F);
        }
        ChakraSphereRenderer.drawHeld(bufferSource, poseStack, sphere, partialTick);
        poseStack.popPose();
    }
}
