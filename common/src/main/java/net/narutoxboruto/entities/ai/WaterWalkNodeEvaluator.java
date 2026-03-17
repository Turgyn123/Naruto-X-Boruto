package net.narutoxboruto.entities.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

public class WaterWalkNodeEvaluator extends WalkNodeEvaluator {

    @Override
    protected double getFloorLevel(BlockPos pos) {
        BlockGetter level = this.mob.level(); // Use the level from the mob
        BlockPos belowPos = pos.below();

        // Check if current position is not water but position below is water
        if (!level.getFluidState(pos).is(Fluids.WATER) && level.getFluidState(belowPos).is(Fluids.WATER)) {
            return belowPos.getY() + 0.24D; // Adjusted to walk on water surface
        }
        return super.getFloorLevel(pos);
    }
}
