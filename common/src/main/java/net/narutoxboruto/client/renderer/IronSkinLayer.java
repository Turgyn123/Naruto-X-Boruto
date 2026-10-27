package net.narutoxboruto.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.narutoxboruto.effect.ModEffects;
import net.narutoxboruto.main.Main;

/**
 * Earth Release: Iron Skin: while it is on, the whole body of the player is drawn again over their own skin in hardened, darker
 * earth (the eyes stay clear). It is in the layout of a player skin, so it fits every part of the body, and it is drawn on the
 * pose the player has, so it moves with them. The arms in first person are done in MixinPlayerRenderer.
 */
public class IronSkinLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/entity/iron_skin.png");
    /** ARGB: nearly opaque, so the earth covers the skin but the armor and the eyes still show. */
    public static final int COLOR = 0xEBFFFFFF;

    public IronSkinLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (ModEffects.IRON_SKIN == null || !player.hasEffect(ModEffects.IRON_SKIN) || player.isInvisible()) return;
        getParentModel().renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.entityTranslucent(TEXTURE)), packedLight,
                LivingEntityRenderer.getOverlayCoords(player, 0.0F), COLOR);
    }
}
