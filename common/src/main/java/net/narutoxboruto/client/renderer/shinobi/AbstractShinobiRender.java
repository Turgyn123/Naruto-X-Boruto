package net.narutoxboruto.client.renderer.shinobi;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.ArrowLayer;
import net.minecraft.client.renderer.entity.layers.BeeStingerLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.narutoxboruto.entities.shinobis.AbstractShinobiMob;
import net.narutoxboruto.main.Main;

public class AbstractShinobiRender extends HumanoidMobRenderer<AbstractShinobiMob, PlayerModel<AbstractShinobiMob>> {
    private final String textureId;

    public AbstractShinobiRender(EntityRendererProvider.Context pContext, String textureId) {
        this(pContext, textureId, false);
    }

    public AbstractShinobiRender(EntityRendererProvider.Context pContext, String textureId, boolean slim) {
        super(pContext,
                new PlayerModel<>(pContext.bakeLayer(slim ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), slim), 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidModel<>(pContext.bakeLayer(slim ? ModelLayers.PLAYER_SLIM_INNER_ARMOR : ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(pContext.bakeLayer(slim ? ModelLayers.PLAYER_SLIM_OUTER_ARMOR : ModelLayers.PLAYER_OUTER_ARMOR)),
                pContext.getModelManager())); // Added model manager parameter
        this.addLayer(new ArrowLayer<>(pContext, this));
        this.addLayer(new BeeStingerLayer<>(this)); // Added model set parameter
        this.textureId = textureId;
    }

    @Override
    public ResourceLocation getTextureLocation(AbstractShinobiMob pEntity) {
        return  ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "textures/entity/" + textureId + ".png");
    }
}
