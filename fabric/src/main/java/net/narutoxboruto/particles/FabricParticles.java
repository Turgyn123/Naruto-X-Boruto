package net.narutoxboruto.particles;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.narutoxboruto.main.Main;

public class FabricParticles {

    public static final SimpleParticleType LIGHTNING_SPARKS = Registry.register(
            BuiltInRegistries.PARTICLE_TYPE,
            ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, "lightning_sparks"),
            FabricParticleTypes.simple()
    );

    public static void register() {
        // Class load triggers static field registration
    }
}
