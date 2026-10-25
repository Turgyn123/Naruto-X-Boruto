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
        CubeDeformation wrap = new CubeDeformation(0.6F);

        // Legs: stubby pillars on wide feet, a rope wrapped round each knee and a rough boulder over it
        for (int side = -1; side <= 1; side += 2) {
            PartDefinition leg = base.addOrReplaceChild(side < 0 ? "right_leg" : "left_leg", CubeListBuilder.create()
                            .mirror(side > 0)
                            .texOffs(0, 62).addBox(-5.0F, 0.0F, -5.0F, 10.0F, 11.0F, 10.0F)
                            .texOffs(0, 83).addBox(-6.0F, 11.0F, -9.0F, 12.0F, 5.0F, 15.0F)
                            .texOffs(80, 160).addBox(-5.0F, 4.0F, -5.0F, 10.0F, 2.0F, 10.0F, wrap),
                    PartPose.offset(side * 8.0F, 8.0F, 0.0F));
            leg.addOrReplaceChild("knee_rock", CubeListBuilder.create()
                            .texOffs(80, 193).addBox(-5.0F, -4.0F, -5.0F, 10.0F, 8.0F, 10.0F),
                    PartPose.offsetAndRotation(0.0F, 3.0F, -4.5F, 0.3F, side * 0.4F, 0.0F));
        }

        // The waist, with the rope belt tied round it, and the torso on top, leaning forward
        base.addOrReplaceChild("waist", CubeListBuilder.create()
                        .texOffs(0, 40).addBox(-11.0F, -8.0F, -7.0F, 22.0F, 8.0F, 14.0F)
                        .texOffs(0, 180).addBox(-11.0F, -5.0F, -7.0F, 22.0F, 3.0F, 14.0F, wrap)
                        .texOffs(80, 174).addBox(-3.0F, -6.0F, -9.5F, 6.0F, 6.0F, 4.0F),
                PartPose.offset(0.0F, 8.0F, 0.0F));
        PartDefinition torso = base.addOrReplaceChild("torso", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-12.0F, -24.0F, -8.0F, 24.0F, 24.0F, 16.0F)
                        .texOffs(80, 41).addBox(-10.0F, -20.0F, -12.0F, 20.0F, 12.0F, 4.0F)   // chest plate
                        .texOffs(80, 57).addBox(-9.0F, -24.0F, 8.0F, 18.0F, 14.0F, 6.0F)      // hump on the back
                        .texOffs(0, 160).addBox(-12.0F, -13.0F, -8.0F, 24.0F, 3.0F, 16.0F, wrap), // rope round the chest
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.18F, 0.0F, 0.0F));
        torso.addOrReplaceChild("hump_rock", CubeListBuilder.create()
                        .texOffs(80, 193).addBox(-5.0F, -4.0F, -5.0F, 10.0F, 8.0F, 10.0F),
                PartPose.offsetAndRotation(-3.0F, -26.0F, 9.0F, 0.5F, 0.6F, 0.3F));
        torso.addOrReplaceChild("hump_rock_two", CubeListBuilder.create()
                        .texOffs(80, 193).addBox(-5.0F, -4.0F, -5.0F, 10.0F, 8.0F, 10.0F),
                PartPose.offsetAndRotation(5.0F, -20.0F, 12.0F, -0.3F, -0.5F, 0.4F));

        // The head: small, deep between the shoulders, heavy brow, a nose, a rope round the neck
        PartDefinition head = torso.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(80, 0).addBox(-5.0F, -10.0F, -5.0F, 10.0F, 10.0F, 10.0F)
                        .texOffs(80, 20).addBox(-4.0F, -4.0F, -8.0F, 8.0F, 4.0F, 3.0F)        // chin
                        .texOffs(80, 27).addBox(-6.0F, -8.0F, -8.0F, 12.0F, 2.0F, 3.0F)       // brow
                        .texOffs(80, 185).addBox(-1.5F, -6.0F, -7.5F, 3.0F, 4.0F, 3.0F)       // nose
                        .texOffs(80, 32).addBox(-8.0F, -7.0F, -2.0F, 3.0F, 5.0F, 4.0F)        // ears
                        .texOffs(80, 32).addBox(5.0F, -7.0F, -2.0F, 3.0F, 5.0F, 4.0F)
                        .texOffs(80, 160).addBox(-5.0F, -1.0F, -5.0F, 10.0F, 2.0F, 10.0F, wrap), // rope round the neck
                PartPose.offset(0.0F, -23.0F, -3.0F));

        // Arms: a boulder for the shoulder (with a rock on top), a thick arm with ropes round the upper arm and the wrist,
        // and a big fist
        for (int side = -1; side <= 1; side += 2) {
            PartDefinition arm = torso.addOrReplaceChild(side < 0 ? "right_arm" : "left_arm", CubeListBuilder.create()
                            .mirror(side > 0)
                            .texOffs(56, 77).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F)
                            .texOffs(0, 103).addBox(-5.0F, 4.0F, -5.0F, 10.0F, 20.0F, 10.0F)
                            .texOffs(80, 160).addBox(-5.0F, 8.0F, -5.0F, 10.0F, 2.0F, 10.0F, wrap)
                            .texOffs(80, 160).addBox(-5.0F, 20.0F, -5.0F, 10.0F, 2.0F, 10.0F, wrap)
                            .texOffs(56, 109).addBox(-7.0F, 22.0F, -7.0F, 14.0F, 14.0F, 14.0F),
                    PartPose.offset(side * 19.0F, -16.0F, 0.0F));
            arm.addOrReplaceChild("shoulder_rock", CubeListBuilder.create()
                            .texOffs(80, 193).addBox(-5.0F, -4.0F, -5.0F, 10.0F, 8.0F, 10.0F),
                    PartPose.offsetAndRotation(side * 1.0F, -9.0F, 0.0F, 0.2F, side * 0.5F, side * 0.35F));
            arm.addOrReplaceChild("elbow_rock", CubeListBuilder.create()
                            .texOffs(80, 193).addBox(-5.0F, -4.0F, -5.0F, 10.0F, 8.0F, 10.0F),
                    PartPose.offsetAndRotation(0.0F, 14.0F, 5.0F, -0.3F, side * 0.4F, 0.2F));
        }

        return LayerDefinition.create(mesh, 128, 224);
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
