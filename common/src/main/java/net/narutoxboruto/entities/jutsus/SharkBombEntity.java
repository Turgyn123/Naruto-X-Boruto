package net.narutoxboruto.entities.jutsus;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import net.narutoxboruto.entities.ModEntities;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Shark Bomb Projectile - Water Release Jutsu
 * 
 * - Launches immediately on cast at full size
 * - Strong homing behavior, re-acquires targets every tick, passes through non-target entities
 * - Impact: AOE water damage + explosion effect + temporary water blocks
 */
public class SharkBombEntity extends Projectile implements GeoEntity {
    
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    
    private int age = 0;
    private LivingEntity targetEntity = null;
    private Vec3 startPos; // set on spawn, used for range check
    
    // === Configuration ===
    private static final float DAMAGE = 16.0F;         // Direct hit damage
    private static final float AOE_DAMAGE = 8.0F;      // AOE splash damage at center
    private static final float SPEED = 1.0F;            // Top flight speed (blocks/tick)
    private static final float START_SPEED = 0.5F;      // Speed on launch, builds up to SPEED
    private static final float SPEED_UP_PER_TICK = 0.05F;
    private static final int MAX_FLIGHT_TICKS = 120;    // 6s max flight
    private static final double MAX_RANGE = 50.0;       // Max travel distance from launch point
    private static final double HOMING_RANGE = 50.0;    // Target detection range
    private static final double LOCK_CONE = 0.88;       // How close to the crosshair a target must be to lock on (dot product, ~28 degrees)
    private static final double TURN_RATE_TARGET = Math.toRadians(10.0); // Max turn per tick toward a locked target
    private static final double TURN_RATE_AIM = Math.toRadians(7.0);     // Max turn per tick toward the crosshair
    private static final double AIM_DISTANCE = 40.0;    // How far ahead the crosshair is followed
    private static final double TERRAIN_LOOKAHEAD = 3.5; // Blocks ahead checked for terrain to climb over
    private static final int RETARGET_INTERVAL = 5;     // Ticks between looking for a new target
    private static final float AOE_RADIUS = 3.0F;       // Splash damage radius
    private static final float EXPLOSION_POWER = 2.0F;  // Visual explosion power
    
    public SharkBombEntity(EntityType<? extends SharkBombEntity> entityType, Level level) {
        super(entityType, level);
    }
    
    public SharkBombEntity(Level level, LivingEntity shooter) {
        this(ModEntities.SHARK_BOMB, level);
        this.setOwner(shooter);
        
        // Spawn 1.5 blocks in front of the shooter's eyes
        Vec3 look = shooter.getLookAngle();
        Vec3 spawnPos = shooter.getEyePosition().add(look.scale(1.5));
        this.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        this.startPos = this.position();
        
        // Find target and launch immediately (no charge phase)
        if (shooter instanceof Player player) {
            this.targetEntity = findBestTarget(player);
        }
        
        // Set velocity toward target or look direction
        Vec3 launchDir;
        if (this.targetEntity != null) {
            launchDir = this.targetEntity.getEyePosition().subtract(this.position()).normalize();
        } else {
            launchDir = shooter.getLookAngle();
        }
        this.setDeltaMovement(launchDir.scale(START_SPEED));
        
        // Face the launch direction (negate atan2 to match MC yaw convention)
        double horizDist = launchDir.horizontalDistance();
        this.setYRot(-(float)(Mth.atan2(launchDir.x, launchDir.z) * (180.0 / Math.PI)));
        this.setXRot((float)(Mth.atan2(launchDir.y, horizDist) * (180.0 / Math.PI)));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }
    
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // No additional synced data needed
    }
    
    @Override
    public void tick() {
        super.tick();
        this.age++;
        
        Entity owner = this.getOwner();
        
        // If owner dies/disconnects, discard
        if (owner == null || !owner.isAlive()) {
            explodeInWater();
            this.discard();
            return;
        }
        
        // Max flight time / range check
        if (this.age > MAX_FLIGHT_TICKS || 
            (this.startPos != null && this.startPos.distanceTo(this.position()) > MAX_RANGE)) {
            this.explodeInWater();
            this.discard();
            return;
        }
        
        // Smooth guided flight toward the locked target or the caster's crosshair
        steer();
        
        // Hit detection
        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hitResult.getType() != HitResult.Type.MISS) {
            this.onHit(hitResult);
            return;
        }
        
        // Update position
        Vec3 velocity = this.getDeltaMovement();
        this.setPos(this.getX() + velocity.x, this.getY() + velocity.y, this.getZ() + velocity.z);
        
        // Update rotation from velocity (negate atan2 to match MC yaw convention)
        double horizSpeed = velocity.horizontalDistance();
        if (horizSpeed > 0.001) {
            this.setYRot(-(float)(Mth.atan2(velocity.x, velocity.z) * (180.0 / Math.PI)));
            this.setXRot((float)(Mth.atan2(velocity.y, horizSpeed) * (180.0 / Math.PI)));
        }
    }
    
    /**
     * Guides the shark. It turns by a limited angle each tick, so its path is a smooth arc rather
     * than a snap toward the target: toward a locked target if there is one, otherwise toward the
     * point the caster is looking at, so the caster can steer it. It also climbs over terrain.
     */
    private void steer() {
        Entity owner = this.getOwner();
        if (!(owner instanceof LivingEntity caster)) return;

        // Look for a target near the crosshair now and then, and again if the old one is gone.
        boolean targetLost = this.targetEntity != null
                && (!this.targetEntity.isAlive() || this.targetEntity.distanceTo(this) > HOMING_RANGE);
        if (targetLost) this.targetEntity = null;
        if (this.targetEntity == null && this.age % RETARGET_INTERVAL == 0 && caster instanceof Player player) {
            this.targetEntity = findBestTarget(player);
        }

        Vec3 current = this.getDeltaMovement();
        Vec3 desired;
        double turn;
        if (this.targetEntity != null) {
            desired = this.targetEntity.position()
                    .add(0, this.targetEntity.getBbHeight() * 0.5, 0)
                    .subtract(this.position());
            turn = TURN_RATE_TARGET;
        } else {
            desired = JutsuSteering.aimPoint(caster, AIM_DISTANCE).subtract(this.position());
            turn = TURN_RATE_AIM;
            // Close to the aim point: stop turning so it does not circle around it.
            if (desired.lengthSqr() < 4.0) desired = current;
        }

        Vec3 direction = JutsuSteering.turnToward(current, desired, turn);
        direction = JutsuSteering.avoidTerrain(this, direction, TERRAIN_LOOKAHEAD);

        float speed = Math.min(SPEED, START_SPEED + SPEED_UP_PER_TICK * this.age);
        this.setDeltaMovement(direction.scale(speed));
    }
    
    /**
     * Find the entity closest to the player's crosshair, within range and within a cone around it.
     * Anything outside the cone is ignored, so the shark does not swing round to something behind you.
     */
    private LivingEntity findBestTarget(Player player) {
        AABB searchBox = this.getBoundingBox().inflate(HOMING_RANGE);
        
        List<LivingEntity> nearbyEntities = this.level().getEntitiesOfClass(
            LivingEntity.class, searchBox,
            entity -> entity != player && entity.isAlive() && !entity.isSpectator()
        );
        
        Vec3 playerLook = player.getLookAngle();
        Vec3 playerPos = player.getEyePosition();
        
        LivingEntity bestTarget = null;
        double bestScore = -1;
        
        for (LivingEntity entity : nearbyEntities) {
            Vec3 toEntity = entity.position().add(0, entity.getBbHeight() * 0.5, 0).subtract(playerPos);
            double distance = toEntity.length();
            if (distance < 0.5) continue;
            double dot = playerLook.dot(toEntity.scale(1.0 / distance));
            if (dot < LOCK_CONE) continue;
            
            double score = dot - (distance / HOMING_RANGE) * 0.3;
            if (score > bestScore) {
                bestScore = score;
                bestTarget = entity;
            }
        }
        
        return bestTarget;
    }
    
    @Override
    protected boolean canHitEntity(Entity entity) {
        if (!super.canHitEntity(entity) || entity == this.getOwner()) return false;
        
        // Pass through non-target entities (like reference mod)
        // Only hit the specific locked target, or any entity if no target
        if (this.targetEntity != null && this.targetEntity.isAlive()) {
            return entity == this.targetEntity;
        }
        return entity instanceof LivingEntity;
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
        if (target instanceof LivingEntity living && living != this.getOwner()) {
            // Direct hit damage
            living.hurt(this.damageSources().mobProjectile(this,
                this.getOwner() instanceof LivingEntity le ? le : null), DAMAGE);
            
            // Knockback
            Vec3 knockback = this.getDeltaMovement().normalize().scale(0.8);
            living.setDeltaMovement(living.getDeltaMovement().add(knockback));
        }
        
        // AOE explosion on impact
        explodeInWater();
        this.discard();
    }
    
    @Override
    protected void onHitBlock(BlockHitResult result) {
        explodeInWater();
        this.discard();
    }
    
    /**
     * Create a water explosion on impact:
     * - AOE damage to nearby entities (falls off with distance)
     * - Visual explosion effect
     * - Temporary water blocks (removed after 1 second)
     * - Water splash particles
     */
    private void explodeInWater() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        
        // --- AOE Damage ---
        for (LivingEntity nearby : serverLevel.getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(AOE_RADIUS))) {
            if (nearby != this.getOwner() && nearby.isAlive()) {
                double dist = nearby.distanceTo(this);
                if (dist <= AOE_RADIUS) {
                    // Damage falls off with distance
                    float dmg = AOE_DAMAGE * (float)(1.0 - dist / AOE_RADIUS);
                    nearby.hurt(this.damageSources().mobProjectile(this,
                        this.getOwner() instanceof LivingEntity le ? le : null), dmg);
                    
                    // Knockback away from center
                    Vec3 kb = nearby.position().subtract(this.position()).normalize().scale(0.5);
                    nearby.setDeltaMovement(nearby.getDeltaMovement().add(kb));
                }
            }
        }
        
        // --- Visual explosion (no block damage) ---
        serverLevel.explode(null, this.getX(), this.getY(), this.getZ(),
            EXPLOSION_POWER, Level.ExplosionInteraction.NONE);
        
        // --- Water splash particles ---
        serverLevel.sendParticles(ParticleTypes.SPLASH,
            this.getX(), this.getY(), this.getZ(), 50, 1.0, 1.0, 1.0, 0.5);
        serverLevel.sendParticles(ParticleTypes.FALLING_WATER,
            this.getX(), this.getY() + 1, this.getZ(), 30, 0.5, 0.5, 0.5, 0.2);
        
        // --- Temporary water blocks (removed after 1 second) ---
        placeTemporaryWater(serverLevel);
        
        // --- Impact sound ---
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
            SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.PLAYERS, 0.3F, 1.0F);
    }
    
    /**
     * Place temporary flowing water blocks at the impact site.
     * Water is automatically removed after ~1 second (20 ticks).
     */
    private void placeTemporaryWater(ServerLevel level) {
        BlockPos center = BlockPos.containing(this.position());
        List<BlockPos> waterPositions = new ArrayList<>();
        
        // Fill a small area with water (2 block radius at ground level)
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-2, 0, -2), center.offset(2, 0, 2))) {
            if (level.getBlockState(pos).isAir()) {
                level.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
                waterPositions.add(pos.immutable());
            }
        }
        
        // Schedule removal after 20 ticks (1 second)
        if (!waterPositions.isEmpty() && level.getServer() != null) {
            int removeAt = level.getServer().getTickCount() + 20;
            level.getServer().tell(new TickTask(removeAt, () -> {
                for (BlockPos pos : waterPositions) {
                    if (level.getBlockState(pos).is(Blocks.WATER)) {
                        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                    }
                }
            }));
        }
    }
    
    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.age = compound.getInt("Age");
    }
    
    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("Age", this.age);
    }
    
    // === GeckoLib Animation ===
    
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "swim_controller", 0, state -> {
            return state.setAndContinue(RawAnimation.begin().thenLoop("swim"));
        }));
    }
    
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
