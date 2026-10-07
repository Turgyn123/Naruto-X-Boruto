package net.narutoxboruto.entities.jutsus;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.narutoxboruto.entities.ModEntities;

/**
 * Great Fireball: a guided fireball. It starts small, grows to full size as it flies, and turns toward
 * wherever the caster is looking (a limited angle per tick, so it curves instead of snapping). It has
 * no gravity, so it flies where it is aimed. On impact it explodes, burns everything in the blast and
 * scorches the ground.
 */
public class FireBallEntity extends Projectile {

    private int age = 0;
    private float rotation = 0;
    
    // Configuration
    private static final float EXPLOSION_RADIUS = 2.25F;
    private static final float FIRE_DAMAGE = 8.0F;      // Direct hit
    private static final float BLAST_DAMAGE = 6.0F;     // Everything else in the blast, at the centre
    private static final float BLAST_RADIUS = 3.5F;     // Everything inside is damaged and set on fire
    private static final int FIRE_SECONDS = 5;
    private static final int MAX_LIFETIME = 100;        // 5 seconds max flight time
    private static final double START_SPEED = 0.7;      // Speed on launch, builds up to SPEED
    private static final double SPEED = 1.2;
    private static final double SPEED_UP_PER_TICK = 0.05;
    private static final double TURN_RATE = Math.toRadians(6.0); // Max turn per tick toward the crosshair
    private static final double AIM_DISTANCE = 40.0;    // How far ahead the crosshair is followed
    private static final double TERRAIN_LOOKAHEAD = 3.0;
    private static final int GROW_TICKS = 10;           // Ticks to reach full size

    public FireBallEntity(EntityType<? extends FireBallEntity> entityType, Level level) {
        super(entityType, level);
    }

    public FireBallEntity(Level level, LivingEntity shooter) {
        this(ModEntities.FIRE_BALL, level);
        this.setOwner(shooter);
        
        // Start just in front of the caster, flying where they look
        Vec3 lookVec = shooter.getLookAngle();
        Vec3 start = shooter.getEyePosition().add(lookVec.scale(1.0)).add(0, -0.1, 0);
        this.setPos(start.x, start.y, start.z);
        this.setDeltaMovement(lookVec.scale(START_SPEED));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // No additional synced data needed
    }

    @Override
    public void tick() {
        super.tick();
        
        this.age++;
        
        // Update rotation for visual spinning effect
        this.rotation += 15.0F; // Rotate 15 degrees per tick
        if (this.rotation >= 360.0F) {
            this.rotation -= 360.0F;
        }
        
        // Out of fuel: burn out in a small burst instead of vanishing
        if (this.age > MAX_LIFETIME) {
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY(), this.getZ(), 20, 0.4, 0.4, 0.4, 0.05);
                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY(), this.getZ(), 6, 0.3, 0.3, 0.3, 0.02);
            }
            this.discard();
            return;
        }
        
        // Guidance (server side; the client follows the motion it is sent)
        if (!this.level().isClientSide() && this.getOwner() instanceof LivingEntity caster) {
            Vec3 velocity = this.getDeltaMovement();
            Vec3 desired = JutsuSteering.aimPoint(caster, AIM_DISTANCE).subtract(this.position());
            // Close to the aim point: stop turning so it does not circle around it.
            if (desired.lengthSqr() < 4.0) desired = velocity;
            
            Vec3 direction = JutsuSteering.turnToward(velocity, desired, TURN_RATE);
            direction = JutsuSteering.avoidTerrain(this, direction, TERRAIN_LOOKAHEAD);
            double speed = Math.min(SPEED, START_SPEED + SPEED_UP_PER_TICK * this.age);
            this.setDeltaMovement(direction.scale(speed));
        }
        
        // Hit detection. A hit explodes and removes the fireball, so stop there.
        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hitResult.getType() != HitResult.Type.MISS) {
            this.onHit(hitResult);
            if (this.isRemoved()) return;
        }
        
        // Move
        Vec3 velocity = this.getDeltaMovement();
        this.setPos(this.getX() + velocity.x, this.getY() + velocity.y, this.getZ() + velocity.z);
        
        // Flame trail, thicker as the fireball grows
        if (this.level() instanceof ServerLevel serverLevel) {
            float growth = this.getGrowth(0.0F);
            serverLevel.sendParticles(ParticleTypes.FLAME,
                    this.getX(), this.getY(), this.getZ(),
                    3 + (int) (growth * 4), 0.25 * growth + 0.05, 0.25 * growth + 0.05, 0.25 * growth + 0.05, 0.02);
            serverLevel.sendParticles(ParticleTypes.SMOKE,
                    this.getX(), this.getY(), this.getZ(),
                    1 + (int) (growth * 2), 0.15 * growth + 0.05, 0.15 * growth + 0.05, 0.15 * growth + 0.05, 0.01);
        }
    }

    /** 0 on launch to 1 at full size, for the renderer. */
    public float getGrowth(float partialTicks) {
        return Math.min(1.0F, (this.age + partialTicks) / GROW_TICKS);
    }

    /** Fire damage that counts as the caster's: it keeps fire immunity working but credits their kills. */
    private DamageSource fireSource() {
        return new DamageSource(this.damageSources().onFire().typeHolder(), this, this.getOwner());
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        
        if (!this.level().isClientSide()) {
            if (hitResult.getType() == HitResult.Type.ENTITY) {
                this.onHitEntity((EntityHitResult) hitResult);
            } else if (hitResult.getType() == HitResult.Type.BLOCK) {
                this.onHitBlock((BlockHitResult) hitResult);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        
        if (target instanceof LivingEntity livingTarget) {
            // Don't damage the caster
            if (livingTarget == this.getOwner()) {
                return;
            }
            
            // Direct hit: full fire damage and set alight
            livingTarget.hurt(this.fireSource(), FIRE_DAMAGE);
            livingTarget.setRemainingFireTicks(FIRE_SECONDS * 20);
        }
        
        detonate(target);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        detonate(null);
    }

    /**
     * The explosion: scorches the ground (this respects the mob-griefing rule), burns every living
     * thing in the blast radius with damage that fades toward the edge, and removes the fireball.
     *
     * @param directHit the entity that was hit head-on, which has already taken the full damage, or null
     */
    private void detonate(Entity directHit) {
        if (this.level() instanceof ServerLevel serverLevel) {
            for (LivingEntity nearby : serverLevel.getEntitiesOfClass(LivingEntity.class,
                    this.getBoundingBox().inflate(BLAST_RADIUS))) {
                if (nearby == this.getOwner() || nearby == directHit || !nearby.isAlive()) continue;
                double distance = nearby.distanceTo(this);
                if (distance > BLAST_RADIUS) continue;

                nearby.hurt(this.fireSource(), BLAST_DAMAGE * (float) (1.0 - distance / BLAST_RADIUS * 0.7));
                nearby.setRemainingFireTicks(FIRE_SECONDS * 20);
            }

            serverLevel.explode(
                    this.getOwner(),
                    this.getX(), this.getY(), this.getZ(),
                    EXPLOSION_RADIUS,
                    true, // causes fire
                    Level.ExplosionInteraction.MOB
            );
            serverLevel.sendParticles(ParticleTypes.FLAME,
                    this.getX(), this.getY(), this.getZ(), 40, 1.0, 0.8, 1.0, 0.08);
            serverLevel.sendParticles(ParticleTypes.LAVA,
                    this.getX(), this.getY(), this.getZ(), 10, 0.5, 0.5, 0.5, 0.1);
        }
        this.discard();
    }

    /**
     * Creates a visual explosion effect without block damage.
     */
    private void createExplosionEffect() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        
        // Spawn explosion particles
        serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                this.getX(), this.getY(), this.getZ(),
                1, 0, 0, 0, 0);
        serverLevel.sendParticles(ParticleTypes.FLAME,
                this.getX(), this.getY(), this.getZ(),
                20, 0.5, 0.5, 0.5, 0.1);
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        // Don't hit the owner
        if (target == this.getOwner()) {
            return false;
        }
        return super.canHitEntity(target);
    }

    /**
     * Get the current rotation for rendering.
     */
    public float getRotation(float partialTicks) {
        return this.rotation + (15.0F * partialTicks);
    }

    public int getAge() {
        return this.age;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Age", this.age);
        tag.putFloat("Rotation", this.rotation);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.age = tag.getInt("Age");
        this.rotation = tag.getFloat("Rotation");
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false; // Fireball cannot be damaged
    }
}
