package net.narutoxboruto.mixin;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.narutoxboruto.client.BorutoDash;
import net.narutoxboruto.client.RaisedArmPose;
import net.narutoxboruto.client.WallFrame;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public abstract class MixinPlayerModel <T extends LivingEntity> extends HumanoidModel<T> {
    @Shadow
    @Final
    public ModelPart jacket, rightSleeve, leftSleeve, rightPants, leftPants;

    public MixinPlayerModel(ModelPart pRoot) {
        super(pRoot);
    }

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch, CallbackInfo ci) {
        if (pEntity instanceof Player player && WallFrame.appliesTo(player)) {
            // The local player walking on a wall: the head follows the sideways look and the limbs swing
            WallFrame.poseModel(head, rightArm, leftArm, rightLeg, leftLeg, pAgeInTicks);
        }

        if (pEntity instanceof Player player && RaisedArmPose.applies(player)) {
            // Charging a Rasenshuriken: the right arm out to the side and up
            rightArm.xRot = 0.0F;
            rightArm.yRot = 0.0F;
            rightArm.zRot = RaisedArmPose.ARM_TILT;
        }

        if (pEntity instanceof Player player && BorutoDash.applies(player)) {
            // Launched by the shadow clone: both arms stretched out in front, legs together
            rightArm.xRot = -(float) Math.PI;
            leftArm.xRot = -(float) Math.PI;
            rightArm.zRot = 0.0F;
            leftArm.zRot = 0.0F;
            rightLeg.xRot = 0.0F;
            leftLeg.xRot = 0.0F;
        }

        hat.copyFrom(head);
        jacket.copyFrom(body);
        rightSleeve.copyFrom(rightArm);
        leftSleeve.copyFrom(leftArm);
        rightPants.copyFrom(rightLeg);
        leftPants.copyFrom(leftLeg);
    }

    public void prepareMobModel(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick) {
        super.prepareMobModel(pEntity, pLimbSwing, pLimbSwingAmount, pPartialTick);
    }
}
