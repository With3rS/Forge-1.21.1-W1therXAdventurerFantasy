package com.w1therx.adventurerfantasy.particle;

import com.w1therx.adventurerfantasy.AdventurerFantasy;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, AdventurerFantasy.MOD_ID);

    public static final RegistryObject<SimpleParticleType> CONTAMINATED_PARTICLES = PARTICLE_TYPES.register("contaminated_particles", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> CRYSTALLIZED_PARTICLES = PARTICLE_TYPES.register("crystallized_particles", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> ECSTATIC_PARTICLES = PARTICLE_TYPES.register("ecstatic_particles", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> ELECTRIFIED_PARTICLES = PARTICLE_TYPES.register("electrified_particles", () -> new SimpleParticleType(true));

    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
    }
}
