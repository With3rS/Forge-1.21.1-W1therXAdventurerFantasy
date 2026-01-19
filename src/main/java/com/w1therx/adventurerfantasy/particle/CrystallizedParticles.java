package com.w1therx.adventurerfantasy.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CrystallizedParticles extends TextureSheetParticle {
    protected CrystallizedParticles(ClientLevel pLevel, double pX, double pY, double pZ, SpriteSet spriteSet, double pXSpeed, double pYSpeed, double pZSpeed) {
        super(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
        this.yd = (float) (-Math.max(Math.pow(1.5, (double) this.age/2-5), 0));
        this.xd = 0;
        this.zd = 0;
        this.friction = 0.975F;
        this.gravity = (float) 0;
        this.quadSize = this.quadSize * (this.random.nextFloat() * 0.3F + 0.8F);
        this.lifetime = (int)(24.0 / (Math.random() * 0.3 + 0.2));
        this.setSpriteFromAge(spriteSet);
        this.rCol = 1f;
        this.bCol = 1f;
        this.gCol = 1f;
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }


        @Override
        public @Nullable Particle createParticle(@NotNull SimpleParticleType pType, @NotNull ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            return new CrystallizedParticles(pLevel, pX, pY, pZ, this.spriteSet, pXSpeed, pYSpeed, pZSpeed);
        }
    }
}
