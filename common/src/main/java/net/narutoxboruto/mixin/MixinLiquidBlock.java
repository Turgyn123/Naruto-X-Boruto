package net.narutoxboruto.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LiquidBlock.class)
public abstract class MixinLiquidBlock extends Block {
    @Unique
    private static final VoxelShape SURFACE_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 14.24D, 16.0D);
    @Shadow
    @Final
    public static VoxelShape STABLE_SHAPE;
    @Shadow @Final public static IntegerProperty LEVEL;

    public MixinLiquidBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        if (pContext instanceof EntityCollisionContext entitycollisioncontext) {
            Entity entity = entitycollisioncontext.getEntity();
            if (entity instanceof LivingEntity living && living.canStandOnFluid(pState.getFluidState())
                    && pContext.isAbove(SURFACE_SHAPE, pPos, true) && !pContext.isDescending()) {
                return SURFACE_SHAPE;
            }
        }
        if (pContext.isAbove(STABLE_SHAPE, pPos, true) && pState.getValue(LEVEL) == 0 && pContext.canStandOnFluid(
                pLevel.getFluidState(pPos.above()), pState.getFluidState())) {
            return STABLE_SHAPE;
        }
        return Shapes.empty();
    }
}
