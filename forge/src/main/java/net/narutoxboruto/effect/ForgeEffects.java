package net.narutoxboruto.effect;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.narutoxboruto.main.Main;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ForgeEffects {
    public static final DeferredRegister<MobEffect> MOD_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, Main.MOD_ID);

    public static final RegistryObject<MobEffect> CHAKRA_CONTROL = MOD_EFFECTS.register("chakra_control",
            () -> new ChakraControlEffect().addAttributeModifier(Attributes.JUMP_STRENGTH,
                    ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "chakra_control_jump_boost"),
                    0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
    );

    public static void register(IEventBus eventBus) {
        MOD_EFFECTS.register(eventBus);
    }
}
