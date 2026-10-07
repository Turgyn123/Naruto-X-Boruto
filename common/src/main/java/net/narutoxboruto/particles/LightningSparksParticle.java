package net.narutoxboruto.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
public class LightningSparksParticle extends TextureSheetParticle {

    private final double initialXd;
    private final double initialYd;
    private final double initialZd;
    private final float baseSize;

    protected LightningSparksParticle(ClientLevel level, double x, double y, double z,
                                      double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);

        // The super constructor randomises the velocity, so set it again from the spawn arguments.
        this.initialXd = xSpeed;
        this.initialYd = ySpeed;
        this.initialZd = zSpeed;
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;

        this.lifetime = 3 + this.random.nextInt(4); // 0.15 - 0.35 s
        this.baseSize = 0.06F + this.random.nextFloat() * 0.04F;
        this.quadSize = this.baseSize;
        this.gravity = 0.0F;
        this.hasPhysics = false; // pass through blocks

        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 1.0F;
        this.alpha = 1.0F;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        // Hard cut: full brightness until the last tick, which is dimmer.
        this.alpha = this.age >= this.lifetime ? 0.5F : 1.0F;

        // Two-tone flicker: white-hot on even ticks, electric blue on odd ticks.
        if ((this.age & 1) == 0) {
            this.rCol = 1.0F;
            this.gCol = 1.0F;
            this.bCol = 1.0F;
        } else {
            this.rCol = 0.45F + this.random.nextFloat() * 0.2F;
            this.gCol = 0.8F + this.random.nextFloat() * 0.15F;
            this.bCol = 1.0F;
        }

        // Electric jitter - sharp, erratic movement
        double jitterStrength = 0.08;
        this.xd = this.initialXd * 0.9 + (this.random.nextDouble() - 0.5) * jitterStrength;
        this.yd = this.initialYd * 0.9 + (this.random.nextDouble() - 0.5) * jitterStrength;
        this.zd = this.initialZd * 0.9 + (this.random.nextDouble() - 0.5) * jitterStrength;

        // Occasional "snap" - sudden direction change like electricity
        if (this.random.nextFloat() < 0.15F) {
            this.xd += (this.random.nextDouble() - 0.5) * 0.2;
            this.yd += (this.random.nextDouble() - 0.5) * 0.15;
            this.zd += (this.random.nextDouble() - 0.5) * 0.2;
        }

        this.move(this.xd, this.yd, this.zd);

        // Size flicker around the base size (not compounding, so it can't drift away to nothing).
        this.quadSize = this.baseSize * (0.8F + this.random.nextFloat() * 0.5F);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public int getLightColor(float partialTick) {
        // Make sparks emit light (fullbright)
        return 0xF000F0;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            LightningSparksParticle particle = new LightningSparksParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
            particle.pickSprite(this.sprites);
            return particle;
        }
    }
}
