package net.narutoxboruto.blocks.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.narutoxboruto.blocks.ModBlocks;
import net.narutoxboruto.main.Main;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NeoForgeBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
            Registries.BLOCK_ENTITY_TYPE, Main.MOD_ID);

    public static final Supplier<BlockEntityType<PaperBombBlockEntity>> PAPER_BOMB = BLOCK_ENTITIES.register(
            "paper_bomb",
            () -> BlockEntityType.Builder.of(PaperBombBlockEntity::new, ModBlocks.PAPER_BOMB.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}

