package net.narutoxboruto.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public class BlockUtils {
    public static BlockPos getBlockPlayerIsLookingAt(ServerPlayer serverPlayer) {
        Vec3 from = serverPlayer.getEyePosition(1.0F);
        Vec3 lookVec = serverPlayer.getViewVector(1.0F);
        Vec3 rayPath = lookVec.scale(7);
        Vec3 to = from.add(rayPath);
        ClipContext rayContext = new ClipContext(from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, CollisionContext.empty());
        BlockHitResult rayHit = serverPlayer.level().clip(rayContext);
        if (rayHit.getType() == HitResult.Type.BLOCK) {
            return rayHit.getBlockPos();
        }
        return null;
    }

    public static boolean isAir(Level pLevel, BlockPos pPos) {
        return pLevel.getBlockState(pPos).getBlock() == Blocks.AIR;
    }

    public static boolean isBelowAir(Level pLevel, BlockPos pPos) {
        return pLevel.getBlockState(pPos.below()).getBlock() == Blocks.AIR;
    }

    public static boolean isBlockEntity(Level pLevel, BlockPos pPos) {
        return pLevel.getBlockEntity(pPos) != null;
    }

    public static boolean isOre(Level pLevel, BlockPos pPos) {
        return pLevel.getBlockState(pPos).getBlock().getName().toString().contains("_ore") || pLevel.getBlockState(pPos)
                .getBlock() == Blocks.OBSIDIAN;
    }
}
