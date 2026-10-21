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
 * Draws the Rasengan in the right hand of a player, on the arm itself, so it swings with the arm when the player walks
 * and turns with the body. Drawn like an item in the hand, but just below the hand.
 */
public class RasenganHandLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public RasenganHandLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        ChakraSphereEntity sphere = ChakraSphereEntity.findHeldClient(player);
        if (sphere == null) return;
        poseStack.pushPose();
        getParentModel().translateToHand(HumanoidArm.RIGHT, poseStack);
        // The arm hangs along +y from the shoulder, the hand is 10 pixels down it and the front is -z. Just under the hand
        poseStack.translate(0.0F, 0.84F, -0.1F);
        ChakraSphereRenderer.drawHeld(bufferSource, poseStack, sphere, partialTick);
        poseStack.popPose();
    }
}
