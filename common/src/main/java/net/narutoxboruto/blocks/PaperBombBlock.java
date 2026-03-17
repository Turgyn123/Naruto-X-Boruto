package net.narutoxboruto.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.narutoxboruto.blocks.entity.PaperBombBlockEntity;
import org.jetbrains.annotations.Nullable;

public class PaperBombBlock extends BaseEntityBlock {
    public static final EnumProperty<AttachFace> FACE = BlockStateProperties.ATTACH_FACE;
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    protected static final VoxelShape CEILING_AABB_Z = Block.box(4.0D, 15.95D, 0.0D, 12.0D, 16.0D, 16.0D);
    protected static final VoxelShape CEILING_AABB_X = Block.box(0.0D, 15.95D, 4.0D, 16.0D, 16.0D, 12.0D);
    protected static final VoxelShape FLOOR_AABB_Z = Block.box(4.0D, 0.0D, 0.0D, 12.0D, 0.05D, 16.0D);
    protected static final VoxelShape FLOOR_AABB_X = Block.box(0.0D, 0.0D, 4.0D, 16.0D, 0.05D, 12.0D);
    protected static final VoxelShape NORTH_AABB = Block.box(4.0D, 0.0D, 15.95D, 12.0D, 16.0D, 16.0D);
    protected static final VoxelShape WEST_AABB = Block.box(15.95D, 0.0D, 4.0D, 16.0D, 16.0D, 12.0D);
    protected static final VoxelShape SOUTH_AABB = Block.box(4.0D, 0.0D, 0.0D, 12.0D, 16.0D, 0.05D);
    protected static final VoxelShape EAST_AABB = Block.box(0.0D, 0.0D, 4.0D, 0.05D, 16.0D, 12.0D);

    public static final MapCodec<PaperBombBlock> CODEC = simpleCodec(PaperBombBlock::new);

    public PaperBombBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(
                this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(FACE, AttachFace.WALL));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    /*PAPER BOMB POSITIONING*/
    public static boolean canAttach(LevelReader pReader, BlockPos pPos, Direction pDirection) {
        BlockPos blockpos = pPos.relative(pDirection);
        return pReader.getBlockState(blockpos).isFaceSturdy(pReader, blockpos, pDirection.getOpposite());
    }

    protected static Direction getConnectedDirection(BlockState pState) {
        return switch (pState.getValue(FACE)) {
            case CEILING -> Direction.DOWN;
            case FLOOR -> Direction.UP;
            default -> pState.getValue(FACING);
        };
    }

    public static void explode(Level level, BlockPos pPos) {
        // Simply remove the block and let the explosion happen naturally
        level.setBlock(pPos, Blocks.AIR.defaultBlockState(), 3);

        // Create explosion at the block position
        level.explode(
                null,
                pPos.getX() + 0.5D,
                pPos.getY() + 0.5D,
                pPos.getZ() + 0.5D,
                5.0F,
                Level.ExplosionInteraction.TNT
        );
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        for (Direction direction : pContext.getNearestLookingDirections()) {
            BlockState blockstate;
            if (direction.getAxis() == Direction.Axis.Y) {
                blockstate = this.defaultBlockState().setValue(FACE,
                        direction == Direction.UP ? AttachFace.CEILING : AttachFace.FLOOR).setValue(FACING,
                        pContext.getHorizontalDirection());
            }
            else {
                blockstate = this.defaultBlockState().setValue(FACE, AttachFace.WALL).setValue(FACING,
                        direction.getOpposite());
            }
            if (blockstate.canSurvive(pContext.getLevel(), pContext.getClickedPos())) {
                return blockstate;
            }
        }
        return null;
    }

    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        return canAttach(pLevel, pPos, getConnectedDirection(pState).getOpposite());
    }

    @Override
    public BlockState rotate(BlockState pState, Rotation pRotation) {
        return pState.setValue(FACING, pRotation.rotate(pState.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        Direction direction = pState.getValue(FACING);
        switch (pState.getValue(FACE)) {
            case FLOOR -> {
                return direction.getAxis() == Direction.Axis.X ? FLOOR_AABB_X : FLOOR_AABB_Z;
            }
            case WALL -> {
                return switch (direction) {
                    case EAST -> EAST_AABB;
                    case WEST -> WEST_AABB;
                    case SOUTH -> SOUTH_AABB;
                    default -> NORTH_AABB;
                };
            }
            default -> {
                return direction.getAxis() == Direction.Axis.X ? CEILING_AABB_X : CEILING_AABB_Z;
            }
        }
    }

    /*PAPER BOMB USAGE*/

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING, FACE);
    }

    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (!pLevel.isClientSide) {
            explode(pLevel, pPos);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean dropFromExplosion(Explosion pExplosion) {
        return false;
    }

    @Override
    public void wasExploded(Level pLevel, BlockPos pPos, Explosion pExplosion) {
        // Don't call explode here to prevent recursion
        super.wasExploded(pLevel, pPos, pExplosion);
    }

    @Override
    public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity) {
        if (!pLevel.isClientSide() && pEntity instanceof Player) {
            explode(pLevel, pPos);
        }
        super.entityInside(pState, pLevel, pPos, pEntity);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new PaperBombBlockEntity(pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pType) {
        return pLevel.isClientSide() ? null : ((pLevel1, pPos, pState1, pBlockEntity) -> {
            if (pBlockEntity instanceof PaperBombBlockEntity pBomb) {
                pBomb.tick();
            }
        });
    }
}