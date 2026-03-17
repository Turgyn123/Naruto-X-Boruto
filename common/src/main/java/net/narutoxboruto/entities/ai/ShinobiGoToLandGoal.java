package net.narutoxboruto.entities.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.LevelReader;
import net.narutoxboruto.entities.shinobis.AbstractShinobiMob;

public class ShinobiGoToLandGoal extends MoveToBlockGoal {
    private final AbstractShinobiMob drowned;

    public ShinobiGoToLandGoal(AbstractShinobiMob p_32409_, double p_32410_) {
        super(p_32409_, p_32410_, 8, 2);
        this.drowned = p_32409_;
    }

    public boolean canUse() {
        return super.canUse() && this.drowned.isInWater();
    }

    public boolean canContinueToUse() {
        return super.canContinueToUse();
    }

    protected boolean isValidTarget(LevelReader p_32413_, BlockPos p_32414_) {
        BlockPos blockpos = p_32414_.above();
        return p_32413_.isEmptyBlock(blockpos) && p_32413_.isEmptyBlock(blockpos.above()) && p_32413_.getBlockState(
                p_32414_).entityCanStandOn(p_32413_, p_32414_, this.drowned);
    }

    public void start() {
        this.drowned.setSearchingForLand(false);
        super.start();
    }

    public void stop() {
        super.stop();
    }
}
