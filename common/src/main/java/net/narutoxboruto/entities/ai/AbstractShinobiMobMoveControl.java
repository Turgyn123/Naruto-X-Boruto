package net.narutoxboruto.entities.ai;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.narutoxboruto.entities.shinobis.AbstractShinobiMob;

public class AbstractShinobiMobMoveControl extends MoveControl {
    private final AbstractShinobiMob shinobi;

    public AbstractShinobiMobMoveControl(AbstractShinobiMob p_32433_) {
        super(p_32433_);
        this.shinobi = p_32433_;
    }

    public void tick() {
        LivingEntity livingentity = this.shinobi.getTarget();
        if (this.shinobi.wantsToSwim() && this.shinobi.isInWater()) {
            if (livingentity != null && livingentity.getY() > this.shinobi.getY() || this.shinobi.searchingForLand) {
                this.shinobi.setDeltaMovement(this.shinobi.getDeltaMovement().add(0.0D, 0.002D, 0.0D));
            }
            if (this.operation != MoveControl.Operation.MOVE_TO || this.shinobi.getNavigation().isDone()) {
                this.shinobi.setSpeed(0.0F);
                return;
            }
            double d0 = this.wantedX - this.shinobi.getX();
            double d1 = this.wantedY - this.shinobi.getY();
            double d2 = this.wantedZ - this.shinobi.getZ();
            double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
            d1 /= d3;
            float f = (float) (Mth.atan2(d2, d0) * (double) (180F / (float) Math.PI)) - 90.0F;
            this.shinobi.setYRot(this.rotlerp(this.shinobi.getYRot(), f, 90.0F));
            this.shinobi.yBodyRot = this.shinobi.getYRot();
            float f1 = (float) (this.speedModifier * this.shinobi.getAttributeValue(Attributes.MOVEMENT_SPEED));
            float f2 = Mth.lerp(0.125F, this.shinobi.getSpeed(), f1);
            this.shinobi.setSpeed(f2);
            this.shinobi.setDeltaMovement(this.shinobi.getDeltaMovement()
                    .add((double) f2 * d0 * 0.005D, (double) f2 * d1 * 0.1D, (double) f2 * d2 * 0.005D));
        }
        else {
            if (!this.shinobi.onGround()) {
                this.shinobi.setDeltaMovement(this.shinobi.getDeltaMovement().add(0.0D, -0.008D, 0.0D));
            }
            super.tick();
        }
    }
}
