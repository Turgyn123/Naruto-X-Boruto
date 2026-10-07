package net.narutoxboruto.entities.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

public class WaterWalkNodeEvaluator extends WalkNodeEvaluator {

    /** Height of the walkable water surface: SURFACE_SHAPE in MixinLiquidBlock is 14.24 pixels tall out of 16. */
    private static final double SURFACE_HEIGHT = 14.24D / 16.0D;

    @Override
    protected double getFloorLevel(BlockPos pos) {
        BlockGetter level = this.mob.level(); // Use the level from the mob
        BlockPos belowPos = pos.below();

        // Check if current position is not water but position below is water
        if (!level.getFluidState(pos).is(Fluids.WATER) && level.getFluidState(belowPos).is(Fluids.WATER)) {
            return belowPos.getY() + SURFACE_HEIGHT;
        }
        return super.getFloorLevel(pos);
    }
}
