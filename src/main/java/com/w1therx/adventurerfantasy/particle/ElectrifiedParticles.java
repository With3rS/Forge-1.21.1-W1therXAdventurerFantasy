package com.w1therx.adventurerfantasy.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ElectrifiedParticles extends TextureSheetParticle {
    protected ElectrifiedParticles(ClientLevel pLevel, double pX, double pY, double pZ, SpriteSet spriteSet, double pXSpeed, double pYSpeed, double pZSpeed) {
        super(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
        this.friction = 0.975F;
        this.xd *= (Math.max(Math.random()*2, 1)-0.5)/8;
        this.yd *= (Math.max(Math.random()*2, 1)-0.5)/8;
        this.zd *= (Math.max(Math.random()*2, 1)-0.5)/8;
        this.quadSize = (float) (this.quadSize * (Math.random()+0.5)/1.8);
        this.lifetime = (int)(20.0 / (Math.random() * 0.5 + 0.5));
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
            return new ElectrifiedParticles(pLevel, pX, pY, pZ, this.spriteSet, pXSpeed, pYSpeed, pZSpeed);
        }
    }
}
