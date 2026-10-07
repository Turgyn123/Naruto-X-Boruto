package net.narutoxboruto.mixin;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.narutoxboruto.client.WallFrame;
import net.narutoxboruto.util.WallClimbing;
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
        } else if (pEntity instanceof Player player && WallClimbing.isHangingOnWall(player)) {
            // Another player hanging on a wall with Chakra Control: arms up, swinging with the height gained
            float reach = Mth.sin((float) (player.getY() * 5.0D)) * 0.4F;
            rightArm.xRot = -2.6F + reach;
            leftArm.xRot = -2.6F - reach;
            rightLeg.xRot = -reach * 0.8F;
            leftLeg.xRot = reach * 0.8F;
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
