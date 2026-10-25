package net.narutoxboruto.client.model;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.narutoxboruto.entities.jutsus.GolemEntity;
import net.narutoxboruto.main.Main;

/**
 * The rock Golem: a hunched giant built out of boulders. A stocky body on stubby legs, a hump of rock on the back, heavy
 * boulders for shoulders, long arms that end in big stone fists, and a small head set deep between the shoulders with a
 * heavy brow. The texture is {@code textures/entity/golem.png} (128 x 160).
 */
public class GolemModel extends HierarchicalModel<GolemEntity> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "golem"), "main");

    private final ModelPart root;
    private final ModelPart torso;
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private int attackTicks;
    private float partialTick;

    public GolemModel(ModelPart root) {
        this.root = root;
        this.torso = root.getChild("torso");
        this.head = torso.getChild("head");
        this.rightArm = torso.getChild("right_arm");
        this.leftArm = torso.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition base = mesh.getRoot();

        // Legs: stubby pillars on wide feet. The hip is 8 above the ground line (y = 24)... in model units, y = 8.
        base.addOrReplaceChild("right_leg", CubeListBuilder.create()
                        .texOffs(0, 62).addBox(-5.0F, 0.0F, -5.0F, 10.0F, 11.0F, 10.0F)
                        .texOffs(0, 83).addBox(-6.0F, 11.0F, -9.0F, 12.0F, 5.0F, 15.0F),
                PartPose.offset(-8.0F, 8.0F, 0.0F));
        base.addOrReplaceChild("left_leg", CubeListBuilder.create().mirror()
                        .texOffs(0, 62).addBox(-5.0F, 0.0F, -5.0F, 10.0F, 11.0F, 10.0F)
                        .texOffs(0, 83).addBox(-6.0F, 11.0F, -9.0F, 12.0F, 5.0F, 15.0F),
                PartPose.offset(8.0F, 8.0F, 0.0F));

        // The waist (a band of darker rock) and the torso on top of it, leaning forward a little
        base.addOrReplaceChild("waist", CubeListBuilder.create()
                        .texOffs(0, 40).addBox(-11.0F, -8.0F, -7.0F, 22.0F, 8.0F, 14.0F),
                PartPose.offset(0.0F, 8.0F, 0.0F));
        PartDefinition torso = base.addOrReplaceChild("torso", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-12.0F, -24.0F, -8.0F, 24.0F, 24.0F, 16.0F)
                        .texOffs(80, 41).addBox(-10.0F, -20.0F, -12.0F, 20.0F, 12.0F, 4.0F)   // chest plate
                        .texOffs(80, 57).addBox(-9.0F, -24.0F, 8.0F, 18.0F, 14.0F, 6.0F),     // hump on the back
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.18F, 0.0F, 0.0F));

        torso.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(80, 0).addBox(-5.0F, -10.0F, -5.0F, 10.0F, 10.0F, 10.0F)
                        .texOffs(80, 20).addBox(-4.0F, -4.0F, -8.0F, 8.0F, 4.0F, 3.0F)        // chin
                        .texOffs(80, 27).addBox(-6.0F, -8.0F, -8.0F, 12.0F, 2.0F, 3.0F)       // brow
                        .texOffs(80, 32).addBox(-8.0F, -7.0F, -2.0F, 3.0F, 5.0F, 4.0F)        // ears
                        .texOffs(80, 32).addBox(5.0F, -7.0F, -2.0F, 3.0F, 5.0F, 4.0F),
                PartPose.offset(0.0F, -23.0F, -3.0F));

        // Arms: a boulder for the shoulder, a thick arm and a big fist, hung from the shoulder
        torso.addOrReplaceChild("right_arm", CubeListBuilder.create()
                        .texOffs(56, 77).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F)
                        .texOffs(0, 103).addBox(-5.0F, 4.0F, -5.0F, 10.0F, 20.0F, 10.0F)
                        .texOffs(56, 109).addBox(-7.0F, 22.0F, -7.0F, 14.0F, 14.0F, 14.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-19.0F, -16.0F, 0.0F));
        torso.addOrReplaceChild("left_arm", CubeListBuilder.create().mirror()
                        .texOffs(56, 77).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F)
                        .texOffs(0, 103).addBox(-5.0F, 4.0F, -5.0F, 10.0F, 20.0F, 10.0F)
                        .texOffs(56, 109).addBox(-7.0F, 22.0F, -7.0F, 14.0F, 14.0F, 14.0F, new CubeDeformation(0.0F)),
                PartPose.offset(19.0F, -16.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 160);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void prepareMobModel(GolemEntity golem, float limbSwing, float limbSwingAmount, float partialTick) {
        this.attackTicks = golem.getAttackAnimationTick();
        this.partialTick = partialTick;
    }

    @Override
    public void setupAnim(GolemEntity golem, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = Mth.clamp(netHeadYaw, -50.0F, 50.0F) * ((float) Math.PI / 180.0F);
        this.head.xRot = Mth.clamp(headPitch, -30.0F, 30.0F) * ((float) Math.PI / 180.0F) - 0.18F;

        // A heavy walk: slow legs, arms that swing a little and hang low
        this.rightLeg.xRot = -1.2F * Mth.triangleWave(limbSwing * 0.8F, 13.0F) * limbSwingAmount;
        this.leftLeg.xRot = 1.2F * Mth.triangleWave(limbSwing * 0.8F, 13.0F) * limbSwingAmount;
        float breath = Mth.sin(ageInTicks * 0.06F) * 0.03F;
        if (this.attackTicks > 0) {
            // Both fists up and smashed down
            float smash = -2.0F + 1.6F * Mth.triangleWave(this.attackTicks - this.partialTick, 10.0F);
            this.rightArm.xRot = smash;
            this.leftArm.xRot = smash;
        } else {
            this.rightArm.xRot = (0.1F + 0.9F * Mth.triangleWave(limbSwing * 0.8F, 13.0F)) * limbSwingAmount + breath;
            this.leftArm.xRot = (0.1F - 0.9F * Mth.triangleWave(limbSwing * 0.8F, 13.0F)) * limbSwingAmount - breath;
        }
        this.rightArm.zRot = 0.06F;
        this.leftArm.zRot = -0.06F;
    }
}
