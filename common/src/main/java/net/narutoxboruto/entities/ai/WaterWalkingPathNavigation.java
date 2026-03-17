package net.narutoxboruto.entities.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.PathType;

public class WaterWalkingPathNavigation extends GroundPathNavigation {
    public WaterWalkingPathNavigation(Mob p_26594_, Level p_26595_) {
        super(p_26594_, p_26595_);
    }

    @Override
    protected PathFinder createPathFinder(int p_26598_) {
        this.nodeEvaluator = new WaterWalkNodeEvaluator();
        return new PathFinder(nodeEvaluator, p_26598_);
    }

    protected boolean hasValidPathType(PathType p_33974_) {
        return p_33974_ == PathType.WATER || super.hasValidPathType(p_33974_);
    }

    public boolean isStableDestination(BlockPos p_33976_) {
        return this.level.getBlockState(p_33976_).is(Blocks.WATER) || super.isStableDestination(p_33976_);
    }
}

