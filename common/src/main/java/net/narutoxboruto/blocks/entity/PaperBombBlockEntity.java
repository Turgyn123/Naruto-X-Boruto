package net.narutoxboruto.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class PaperBombBlockEntity extends BlockEntity {
    private static final int EXPLODE_TIME = 80;
    private int timer = 0;

    public PaperBombBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.PAPER_BOMB, pPos, pBlockState);
    }

    public static void explode(Level level, BlockPos pPos) {
        // Remove the block first
        level.setBlock(pPos, Blocks.AIR.defaultBlockState(), 3);
        // Create explosion at the block position
        level.explode(
                null, // No entity caused the explosion
                (double)pPos.getX() + 0.5D, // Center X
                (double)pPos.getY() + 0.5D, // Center Y
                (double)pPos.getZ() + 0.5D, // Center Z
                5.0F, // Explosion power
                Level.ExplosionInteraction.TNT // Explosion type
        );
    }

    public boolean hasLineOfSight(Entity pEntity, Level level, BlockPos pPos) {
        if (pEntity.level() != level) {
            return false;
        }
        else {
            Vec3 vec3 = new Vec3(pPos.getX(), pPos.getY(), pPos.getZ());
            Vec3 vec31 = new Vec3(pEntity.getX(), pEntity.getEyeY(), pEntity.getZ());
            if (vec31.distanceTo(vec3) > 128.0D) {
                return false;
            }
            else {
                return level.clip(
                                new ClipContext(vec3, vec31, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, pEntity))
                        .getType() == HitResult.Type.MISS;
            }
        }
    }

    public void checkPlayersAround(Level level, BlockPos pPos) {
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (player.distanceToSqr(pPos.getX() + 0.5, pPos.getY() + 0.5, pPos.getZ() + 0.5) <= 9.0) {
                if (hasLineOfSight(player, level, pPos)) {
                    explode(level, pPos);
                }
            }
        }
    }

    public void tick() {
        if (level == null) {
            return;
        }
        timer++;
        if (!this.level.isClientSide()) {
            if (timer >= EXPLODE_TIME) {
                checkPlayersAround(level, this.getBlockPos());
            }
        }
    }
}
