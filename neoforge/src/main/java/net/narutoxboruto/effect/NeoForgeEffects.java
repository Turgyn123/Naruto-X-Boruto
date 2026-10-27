package net.narutoxboruto.effect;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.narutoxboruto.main.Main;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NeoForgeEffects {
    public static final DeferredRegister<MobEffect> MOD_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, Main.MOD_ID);

    public static final DeferredHolder<MobEffect, MobEffect> CHAKRA_CONTROL = MOD_EFFECTS.register("chakra_control",
            () -> new ChakraControlEffect().addAttributeModifier(Attributes.JUMP_STRENGTH,
                    ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "chakra_control_jump_boost"),
                    0.5,  AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
    );

    public static void register(IEventBus eventBus) {
        MOD_EFFECTS.register(eventBus);
    }
}
