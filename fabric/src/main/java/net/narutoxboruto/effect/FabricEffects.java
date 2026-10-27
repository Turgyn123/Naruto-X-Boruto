package net.narutoxboruto.effect;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.narutoxboruto.main.Main;

public class FabricEffects {

    public static final Holder<MobEffect> CHAKRA_CONTROL = Registry.registerForHolder(
            BuiltInRegistries.MOB_EFFECT,
            ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "chakra_control"),
            new ChakraControlEffect().addAttributeModifier(Attributes.JUMP_STRENGTH,
                    ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "chakra_control_jump_boost"),
                    0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
    );

    public static void register() {
        // Class load triggers static field registration
    }
}
