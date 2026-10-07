package net.narutoxboruto.entities.jutsus;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import net.narutoxboruto.entities.ModEntities;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Water Dragon Projectile - Powerful Water Release Jutsu
 * 
 * Three distinct phases, each with its own animation:
 * 
 * - Phase 1 (Rising): Spawns below ground, rises vertically.
 *   "Spawn" animation unfurls the dragon as it emerges.
 * - Phase 2 (Idle): Hovers in place, rotates yaw toward target.
 *   "Idle" animation loops with gentle swaying.
 * - Phase 3 (Attack): "Attack" animation straightens the dragon from vertical
 *   to horizontal via per-bone rotations (no global pitch distortion).
 *   After the animation completes, the dragon launches toward the target.
 * - Impact: Massive AOE explosion + water damage + temporary water blocks.
 */
public class WaterDragonEntity extends Projectile implements GeoEntity {

    /** Where the dragon will go, as a unit vector. The server updates it and the client just reads it. */
    private static final EntityDataAccessor<Vector3f> DATA_AIM =
            SynchedEntityData.defineId(WaterDragonEntity.class, EntityDataSerializers.VECTOR3);
    
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    
    private int age = 0;
    private boolean launched = false;
    private Vec3 startPos;             // Set when launched, for range check
    private LivingEntity lockedTarget = null;  // Target it has locked onto in flight
    
    // === Configuration ===
    private static final float DAMAGE = 20.0F;          // Direct hit damage
    private static final float AOE_DAMAGE = 10.0F;      // AOE splash damage
    private static final float SPEED = 0.5F;             // Flight speed
    // Phase durations match bbmodel animation lengths exactly:
    // Spawn = 1s (hold_on_last_frame), Idle = 2s (loop), Attack = 1s (hold_on_last_frame)
    private static final int RISE_TICKS = 20;            // 1s — matches Spawn animation length
    private static final int IDLE_TICKS = 40;            // 2s — matches one full Idle loop
    private static final int ATTACK_ANIM_TICKS = 20;     // 1s — matches Attack animation length
    private static final int PHASE2_END = RISE_TICKS + IDLE_TICKS;           // tick 60
    private static final int PHASE3_END = PHASE2_END + ATTACK_ANIM_TICKS;    // tick 80
    private static final int MAX_FLIGHT_TICKS = 60;      // ~3s flight after launch
    private static final int MAX_TOTAL_TICKS = 160;      // ~8s total lifetime
    private static final double MAX_RANGE = 50.0;        // Max travel distance after launch
    private static final float AOE_RADIUS = 4.0F;        // Large splash radius
    private static final float EXPLOSION_POWER = 0.5F;   // Visual-only explosion (low power to avoid extra knockback)
    private static final int LAUNCH_GRACE_TICKS = 5;     // Ticks after launch to ignore block collisions
    private static final int FLIGHT_SCAN_INTERVAL = 5;   // Re-scan for targets every N flight ticks
    private static final double TARGET_SCAN_RANGE = 24.0; // Range for in-flight target scanning
    private static final double TARGET_SCAN_CONE = 0.6;  // Dot product threshold for flight cone (cos ~53°)
    private static final double TURN_RATE_TARGET = Math.toRadians(6.0); // Max turn per tick toward a locked target (a heavy dragon turns slowly)
    private static final double AIM_DISTANCE = 50.0;     // How far along the look ray the caster is aiming
    private static final double MAX_AIM_PITCH_UP = 0.0;   // An untargeted dragon never climbs; it flies level or dives
    private static final double MAX_AIM_PITCH_DOWN = 30.0; // Degrees below level
    private static final double SPAWN_DISTANCE = 3.5;    // The dragon rises this far in front of the caster
    private static final int LOCK_DELAY_TICKS = 25;      // In flight, it flies straight this long (about 12 blocks) before it can lock on
    private static final double FLIGHT_HEIGHT = 1.5;     // The whole dragon starts this many blocks above the ground (the puddle in the geo file sits 16 model units per 3 blocks below: -8 for 1.5)
    private static final double BODY_HEIGHT = 0.3;       // Height of the middle of the body above the entity's position
    private static final double HIT_FORWARD = 1.5;       // The hit zone sits this far ahead of the entity, around the front of the body
    private static final double HIT_RADIUS = 1.6;        // Blocks around that point that count as a hit (the model is far bigger than its box)
    private static final float BOX_WIDTH = 3.0F;         // Size of the entity's own box (shown with F3+B)
    private static final float BOX_HEIGHT = 2.0F;
    private static final double MIN_AIM_AHEAD = 2.0;     // The crosshair point must be this far in front of the launch point to aim at it
    private static final float AIM_YAW_STEP = 30.0F;     // Max degrees per tick the dragon turns to follow where the caster looks
    private static final double TERRAIN_LOOKAHEAD = 4.0; // Blocks ahead checked for terrain to climb over
    private static final double PUDDLE_OFFSET = 0.65;    // Blocks from the entity to the middle of the puddle in the model
    private static final double PUDDLE_RADIUS = 1.4;     // Blocks, roughly the size of the puddle
    
    public WaterDragonEntity(EntityType<? extends WaterDragonEntity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true; // No collision during windup
        this.noCulling = true; // Prevent frustum culling — scaled model extends far beyond the small entity hitbox
        this.refreshDimensions(); // Pick up the box from getDimensions below
    }

    /** The registered size differs per loader and is far smaller than the dragon, so it is set here. */
    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.scalable(BOX_WIDTH, BOX_HEIGHT);
    }
    
    public WaterDragonEntity(Level level, LivingEntity shooter) {
        this(ModEntities.WATER_DRAGON, level);
        this.setOwner(shooter);
        
        // Spawn at the shooter's position.
        // The Spawn animation handles the visual rise from underground
        // (bone11 starts at y=-37px and animates to rest position over 1s).
        // Rise a few blocks in front of the caster, not on top of them (the camera ended up inside the dragon).
        Vec3 ahead = Vec3.directionFromRotation(0.0F, shooter.getYHeadRot()).scale(SPAWN_DISTANCE);
        // The whole dragon starts FLIGHT_HEIGHT above the ground there. Its tail reaches down to the
        // puddle on the ground, and its body is already at flying height when it swings flat.
        double x = shooter.getX() + ahead.x;
        double z = shooter.getZ() + ahead.z;
        this.setPos(x, findGroundY(level, x, shooter.getY(), z) + FLIGHT_HEIGHT, z);
        
        // It is aimed at where the caster looks from the very first tick, so it never has to turn around
        this.trackAim(shooter);
        this.setYRot(aimYaw());
        this.setXRot(0);
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
        
        // No movement yet
        this.setDeltaMovement(Vec3.ZERO);
    }
    
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_AIM, new Vector3f(0.0F, 0.0F, 1.0F));
    }
    
    @Override
    public void tick() {
        super.tick();
        this.age++;
        
        Entity owner = this.getOwner();
        
        // If owner dies/disconnects, discard
        if (owner == null || !owner.isAlive()) {
            if (this.launched) explodeWithWater();
            this.discard();
            return;
        }
        
        // Max lifetime check
        if (this.age > MAX_TOTAL_TICKS) {
            this.explodeWithWater();
            this.discard();
            return;
        }
        
        // The dragon climbs out of a puddle: spray around it while it rises and winds up
        if (this.age <= PHASE2_END + 1 && this.level() instanceof ServerLevel serverLevel) {
            sprayPuddle(serverLevel);
        }

        // ==================== PHASE 1: RISING + PHASE 2: IDLE ====================
        // The entity stays on the ground. The Spawn animation rises the dragon out of the puddle and the
        // Idle animation sways it. It keeps facing where the caster looks, but never locks onto a target:
        // that only happens in flight, after it has flown a while.
        if (this.age <= PHASE2_END) {
            this.trackAim(owner);
            this.turnToAim(AIM_YAW_STEP);
            return;
        }

        // ==================== PHASE 3: ATTACK ====================
        // The Attack animation swings the dragon from upright to flat. It does not move up or down:
        // it is already at flying height. The aim is frozen from here on: looking at the dragon (or
        // away from it) no longer changes where it goes.
        if (this.age <= PHASE3_END) {
            this.turnToAim(AIM_YAW_STEP);
            return;
        }
        
        // ==================== FLIGHT (after attack animation) ====================
        if (!this.launched) {
            this.launched = true;
            this.startPos = this.position();
            
            // It goes where it was aimed; a target is only picked up later, in flight.
            this.setDeltaMovement(this.getAim().scale(SPEED));
        }
        
        int flightAge = this.age - PHASE3_END;
        
        // Enable block collision after grace period (avoids clipping ground at spawn)
        if (this.noPhysics && flightAge > LAUNCH_GRACE_TICKS) {
            this.noPhysics = false;
        }
        
        // Check max flight time / range
        if (flightAge > MAX_FLIGHT_TICKS ||
            (this.startPos != null && this.startPos.distanceTo(this.position()) > MAX_RANGE)) {
            this.explodeWithWater();
            this.discard();
            return;
        }
        
        // The model is far bigger than the entity's box, so anything close to its front counts as hit
        LivingEntity inFront = this.findEntityInFront();
        if (inFront != null) {
            this.onHit(new EntityHitResult(inFront));
            return;
        }

        // Hit detection (entity hits always, block hits only after grace period)
        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hitResult.getType() == HitResult.Type.ENTITY) {
            this.onHit(hitResult);
            return;
        } else if (hitResult.getType() == HitResult.Type.BLOCK && !this.noPhysics) {
            this.onHit(hitResult);
            return;
        }
        
        // Update position
        Vec3 velocity = this.getDeltaMovement();
        this.setPos(this.getX() + velocity.x, this.getY() + velocity.y, this.getZ() + velocity.z);
        
        // Periodically scan for new targets during flight if none locked (or current died)
        if ((this.lockedTarget == null || !this.lockedTarget.isAlive()) 
                && flightAge >= LOCK_DELAY_TICKS && flightAge % FLIGHT_SCAN_INTERVAL == 0) {
            scanForFlightTarget();
        }
        
        // Flight: the dragon goes where it was aimed when the attack started and does not follow the
        // caster's crosshair. Once it has flown a little it can lock onto a target ahead, and then turns
        // toward it by a limited angle per tick. It climbs over terrain in its way.
        Vec3 desired = velocity;
        if (this.lockedTarget != null && this.lockedTarget.isAlive()) {
            desired = this.lockedTarget.position()
                    .add(0, this.lockedTarget.getBbHeight() * 0.5, 0)
                    .subtract(this.position());
        }
        Vec3 direction = JutsuSteering.turnToward(velocity, desired, TURN_RATE_TARGET);
        if (flightAge > LAUNCH_GRACE_TICKS) {
            direction = JutsuSteering.avoidTerrain(this, direction, TERRAIN_LOOKAHEAD);
        }
        this.setDeltaMovement(direction.scale(SPEED));
        
        // Update yaw and pitch to face movement direction
        Vec3 vel = this.getDeltaMovement();
        double horizSpeed = vel.horizontalDistance();
        if (horizSpeed > 0.001) {
            this.setYRot(-(float)(Mth.atan2(vel.x, vel.z) * (180.0 / Math.PI)));
            this.setXRot((float)(-(Mth.atan2(vel.y, horizSpeed) * (180.0 / Math.PI))));
        }
    }
    
    /**
     * Water spraying up from the puddle the dragon rises out of. The puddle is part of the model, a little
     * in front of the entity's position, and it shrinks away again while the dragon launches.
     */
    private void sprayPuddle(ServerLevel level) {
        Vec3 facing = Vec3.directionFromRotation(0.0F, this.getYRot());
        double x = this.getX() + facing.x * PUDDLE_OFFSET;
        double z = this.getZ() + facing.z * PUDDLE_OFFSET;
        double ground = this.getY() - FLIGHT_HEIGHT;

        if (this.age == 1) {
            level.playSound(null, x, ground, z, SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 1.0F, 0.8F);
            level.sendParticles(ParticleTypes.SPLASH, x, ground + 0.1, z, 30, PUDDLE_RADIUS, 0.1, PUDDLE_RADIUS, 0.3);
        } else if (this.age % 2 == 0) {
            int count = this.age == PHASE2_END + 1 ? 24 : 4;
            level.sendParticles(ParticleTypes.SPLASH, x, ground + 0.1, z, count, PUDDLE_RADIUS, 0.05, PUDDLE_RADIUS, 0.1);
        }
    }

    /**
     * Scan for targets during flight. Looks for enemies in a cone ahead of the dragon.
     * If a target is found, locks onto it for homing.
     */
    private void scanForFlightTarget() {
        Entity owner = this.getOwner();
        Vec3 flyDir = this.getDeltaMovement().normalize();
        
        List<LivingEntity> nearby = this.level().getEntitiesOfClass(
            LivingEntity.class,
            this.getBoundingBox().inflate(TARGET_SCAN_RANGE),
            e -> e != owner && e.isAlive() && !e.isSpectator()
                && !(e instanceof Player) && e.distanceTo(this) <= TARGET_SCAN_RANGE
        );
        
        if (nearby.isEmpty()) return;
        
        LivingEntity best = null;
        double bestScore = -1;
        
        for (LivingEntity e : nearby) {
            Vec3 toE = e.getEyePosition().subtract(this.position());
            double dist = toE.length();
            if (dist < 0.5) continue;
            double dot = flyDir.dot(toE.normalize());
            if (dot < TARGET_SCAN_CONE) continue; // Outside forward cone
            // Prefer closer targets that are more aligned with flight path
            double score = dot * (1.0 + 10.0 / (dist + 3.0));
            if (score > bestScore) {
                bestScore = score;
                best = e;
            }
        }
        
        if (best != null) {
            this.lockedTarget = best;
        }
    }
    
    private Vec3 getAim() {
        Vector3f aim = this.entityData.get(DATA_AIM);
        return new Vec3(aim.x(), aim.y(), aim.z());
    }

    private float aimYaw() {
        Vec3 aim = this.getAim();
        return -(float) (Mth.atan2(aim.x, aim.z) * (180.0 / Math.PI));
    }

    /** Turns toward the aim by at most {@code maxStep} degrees. */
    private void turnToAim(float maxStep) {
        if (this.getAim().horizontalDistanceSqr() < 1.0e-4) return; // aiming straight up or down: keep facing
        float diff = Mth.wrapDegrees(this.aimYaw() - this.getYRot());
        this.setYRot(this.getYRot() + Mth.clamp(diff, -maxStep, maxStep));
    }

    /** The top of the ground (or the water surface) just below the caster, at the spot the dragon rises. */
    private static double findGroundY(Level level, double x, double fromY, double z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(Mth.floor(x), Mth.floor(fromY), Mth.floor(z));
        for (int i = 0; i < 10; i++) {
            BlockState state = level.getBlockState(pos);
            if (!state.getCollisionShape(level, pos).isEmpty()) return pos.getY() + 1.0;
            if (!state.getFluidState().isEmpty()) return pos.getY() + state.getFluidState().getHeight(level, pos);
            pos.move(Direction.DOWN);
        }
        return fromY;
    }

    /**
     * The nearest living thing around the front of the dragon, as far as it moves this tick. The model is
     * a long body with a head, so this is a zone around the head rather than the (much smaller) box.
     */
    private LivingEntity findEntityInFront() {
        Vec3 movement = this.getDeltaMovement();
        if (movement.lengthSqr() < 1.0e-6) return null;

        Vec3 front = this.position().add(movement.normalize().scale(HIT_FORWARD)).add(0.0, BODY_HEIGHT, 0.0);
        AABB zone = new AABB(front, front).inflate(HIT_RADIUS).expandTowards(movement);

        LivingEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (LivingEntity candidate : this.level().getEntitiesOfClass(LivingEntity.class, zone,
                e -> e.isAlive() && !e.isSpectator() && this.canHitEntity(e))) {
            double distance = candidate.distanceToSqr(front);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
        }
        return best;
    }

    /**
     * Server side: points the aim at the spot the caster looks at, measured from where the dragon will
     * start flying. Measuring from the dragon itself used to send it backwards when the caster looked at
     * the ground closer than the dragon is, so anything that is not clearly in front of the launch point
     * falls back to the way the caster looks.
     */
    private void trackAim(Entity owner) {
        if (this.level().isClientSide || !(owner instanceof LivingEntity shooter)) return;

        Vec3 look = shooter.getLookAngle();
        Vec3 launchPoint = this.position().add(0.0, BODY_HEIGHT, 0.0);
        Vec3 toAim = JutsuSteering.aimPoint(shooter, AIM_DISTANCE).subtract(launchPoint);
        Vec3 dir = toAim.dot(look) > MIN_AIM_AHEAD ? toAim.normalize() : look;

        Vec3 aim = limitPitch(dir);
        this.entityData.set(DATA_AIM, new Vector3f((float) aim.x, (float) aim.y, (float) aim.z));
    }

    /**
     * Keeps an untargeted launch close to level: a shallow climb at most, and a steeper dive allowed so
     * it can hit the ground in front of the caster. Looking up to watch the dragon rise would otherwise
     * send it into the sky.
     */
    private static Vec3 limitPitch(Vec3 dir) {
        double maxY = Math.sin(Math.toRadians(MAX_AIM_PITCH_UP));
        double minY = -Math.sin(Math.toRadians(MAX_AIM_PITCH_DOWN));
        double y = Math.max(minY, Math.min(maxY, dir.y));
        if (y == dir.y) return dir;

        double horizontal = Math.hypot(dir.x, dir.z);
        if (horizontal < 1.0e-6) return new Vec3(0, y, 0).normalize();
        double scale = Math.sqrt(1.0 - y * y) / horizontal;
        return new Vec3(dir.x * scale, y, dir.z * scale);
    }
    
    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && entity != this.getOwner();
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
            // Devastating direct hit damage
            living.hurt(this.damageSources().mobProjectile(this,
                this.getOwner() instanceof LivingEntity le ? le : null), DAMAGE);
            
            // Subtle knockback in movement direction
            Vec3 knockback = this.getDeltaMovement().normalize().scale(0.35);
            living.setDeltaMovement(living.getDeltaMovement().add(knockback.x, 0.15, knockback.z));
            living.hurtMarked = true;
        }
        
        // Massive explosion on impact
        explodeWithWater();
        this.discard();
    }
    
    @Override
    protected void onHitBlock(BlockHitResult result) {
        explodeWithWater();
        this.discard();
    }
    
    /**
     * Create a massive water explosion on impact:
     * - Large AOE damage to all nearby entities
     * - Dramatic explosion visual (power 5)
     * - Temporary water blocks (removed after 1 second)
     * - Massive water particle effects
     */
    private void explodeWithWater() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        
        // --- AOE Damage (hits ANY entity, not target-specific) ---
        for (LivingEntity nearby : serverLevel.getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(AOE_RADIUS))) {
            if (nearby != this.getOwner() && nearby.isAlive()) {
                double dist = nearby.distanceTo(this);
                if (dist <= AOE_RADIUS) {
                    float dmg = AOE_DAMAGE * (float)(1.0 - dist / AOE_RADIUS);
                    nearby.hurt(this.damageSources().mobProjectile(this,
                        this.getOwner() instanceof LivingEntity le ? le : null), dmg);
                    
                    // Subtle knockback away from explosion with distance falloff
                    double falloff = 1.0 - dist / AOE_RADIUS;
                    Vec3 kb = nearby.position().subtract(this.position()).normalize().scale(0.3 * falloff);
                    nearby.setDeltaMovement(nearby.getDeltaMovement().add(kb.x, 0.1 * falloff, kb.z));
                    nearby.hurtMarked = true;
                }
            }
        }
        
        // --- Massive visual explosion (no block damage) ---
        serverLevel.explode(null, this.getX(), this.getY(), this.getZ(),
            EXPLOSION_POWER, Level.ExplosionInteraction.NONE);
        
        // --- Water particles ---
        serverLevel.sendParticles(ParticleTypes.SPLASH,
            this.getX(), this.getY(), this.getZ(), 100, 2.0, 2.0, 2.0, 0.5);
        serverLevel.sendParticles(ParticleTypes.FALLING_WATER,
            this.getX(), this.getY() + 2, this.getZ(), 60, 2.0, 0.5, 2.0, 0.3);
        serverLevel.sendParticles(ParticleTypes.RAIN,
            this.getX(), this.getY() + 3, this.getZ(), 40, 2.5, 0.5, 2.5, 0.3);
        
        // --- Temporary water blocks ---
        placeTemporaryWater(serverLevel);
        
        // --- Impact sound ---
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
            SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 2.0F, 0.6F);
    }
    
    /**
     * Place temporary water blocks at the impact site.
     * Covers a larger area than shark bomb. Removed after ~1 second.
     */
    private void placeTemporaryWater(ServerLevel level) {
        BlockPos center = BlockPos.containing(this.position());
        List<BlockPos> waterPositions = new ArrayList<>();
        
        // Fill a larger area with water (3 block radius)
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-3, 0, -3), center.offset(3, 0, 3))) {
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
        this.launched = compound.getBoolean("Launched");
    }
    
    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("Age", this.age);
        compound.putBoolean("Launched", this.launched);
    }
    
    /**
     * Allow rendering at extended distance since the 5x scaled model is visible
     * from much further than the small entity hitbox would normally allow.
     */
    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 16384.0; // Render up to 128 blocks away
    }
    
    // === GeckoLib Animation ===
    
    private static final RawAnimation SPAWN_ANIM = RawAnimation.begin().thenPlayAndHold("Spawn");
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("Idle");
    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenPlayAndHold("Attack");
    private static final RawAnimation TAIL_ANIM = RawAnimation.begin().thenLoop("Tail");
    
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main_controller", 5, state -> {
            // Phase 1 (Rise): Spawn animation unfurls dragon from coiled state.
            // Phase 2 (Idle): Idle animation — gentle swaying while hovering.
            // Phase 3 (Attack): Attack animation — dragon straightens from vertical
            //   to horizontal via per-bone rotations. Holds on last frame for flight.
            if (this.age <= RISE_TICKS) {
                return state.setAndContinue(SPAWN_ANIM);
            } else if (this.age <= PHASE2_END) {
                return state.setAndContinue(IDLE_ANIM);
            }
            return state.setAndContinue(ATTACK_ANIM);
        }));

        // The tail keeps waving through every phase. It only moves the tail bones, so it can run on its
        // own next to the main controller.
        controllers.add(new AnimationController<>(this, "tail_controller", 0,
                state -> state.setAndContinue(TAIL_ANIM)));
    }
    
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
