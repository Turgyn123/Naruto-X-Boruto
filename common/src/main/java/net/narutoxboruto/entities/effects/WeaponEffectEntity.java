package net.narutoxboruto.entities.effects;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.narutoxboruto.entities.ModEntities;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * The visual effects of the weapons, drawn as real shapes and textures (see {@code WeaponEffectRenderer}) and not
 * as particles:
 * <ul>
 *   <li>{@link #WIND}: a swirl of wind that flies forward (the Giant Fan).</li>
 *   <li>{@link #CRESCENT}: a crescent shaped blade of energy that flies forward (the Cutting Whirlwind, and the
 *       chakra slash of Hiramekarei).</li>
 *   <li>{@link #THREAD}: the chakra thread of Nuibari between the wielder and the bound target. This one also
 *       holds the target in place, see {@link #tickThread}.</li>
 *   <li>{@link #FIRE_WAVE}: the Great Fire Annihilation, a huge wave of fire that spreads out in front of the caster.
 *       The server only does the damage, the renderer draws the fire.</li>
 *   <li>{@link #ASH_CLOUD}: the cloud of ash of Fire Release: Ash Pile Burning, which can be set alight.
 *       Also only particles and damage.</li>
 *   <li>{@link #FLAME_BULLET}: the bullet of flame of Fire Release: Flame Bullet, which flies straight and bursts on
 *       the first thing it hits.</li>
 * </ul>
 * The effects only live a short while, are never saved and only the server moves them.
 */
public class WeaponEffectEntity extends Entity {

    public static final int WIND = 0;
    public static final int CRESCENT = 1;
    public static final int THREAD = 2;
    public static final int FIRE_WAVE = 3;
    public static final int ASH_CLOUD = 4;
    public static final int FLAME_BULLET = 5;
    public static final int VACUUM_BULLET = 6;

    private static final EntityDataAccessor<Integer> KIND = SynchedEntityData.defineId(WeaponEffectEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> SIZE = SynchedEntityData.defineId(WeaponEffectEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(WeaponEffectEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> MAX_LIFE = SynchedEntityData.defineId(WeaponEffectEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TARGET = SynchedEntityData.defineId(WeaponEffectEntity.class, EntityDataSerializers.INT);

    // Server only: what the thread holds on to
    private int life;
    private UUID owner;
    private UUID target;
    private Item cooldownItem;
    private Vec3 anchor = Vec3.ZERO;
    private int lastOwnerHurt;
    /**
     * The ash cloud: ticks since it was set alight, or -1 while it has not been. It is also sent to the clients (in
     * TARGET) to draw the cloud catching fire.
     */
    private int igniteTicks = -1;
    /** How many more ticks the thread is reeling the target in. */
    private int reel;

    /**
     * The thread of Nuibari first reels the target in for half a second, then holds it for 3 seconds. After it ends
     * the ability rests for 8 seconds.
     */
    private static final int REEL_TICKS = 10;
    private static final int HOLD_TICKS = 3 * 20;
    public static final int THREAD_TICKS = REEL_TICKS + HOLD_TICKS;
    public static final int THREAD_COOLDOWN_TICKS = 8 * 20;
    private static final double THREAD_BREAK_DISTANCE = 32.0D;
    private static final int HOLD_AMPLIFIER = 6;
    /** The target is pulled to this far in front of the wielder. */
    private static final double REEL_STOP_DISTANCE = 2.2D;

    /** Whether a target is held by the thread of Nuibari right now. */
    public static boolean isHeldByThread(LivingEntity target) {
        MobEffectInstance hold = target.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
        return hold != null && hold.getAmplifier() == HOLD_AMPLIFIER;
    }

    public WeaponEffectEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noCulling = true;
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(KIND, WIND);
        builder.define(SIZE, 1.0F);
        builder.define(COLOR, 0xFFFFFFFF);
        builder.define(MAX_LIFE, 10);
        builder.define(TARGET, -1);
    }

    public int getKind() { return this.entityData.get(KIND); }
    public float getSize() { return this.entityData.get(SIZE); }
    public int getColor() { return this.entityData.get(COLOR); }
    public int getMaxLife() { return this.entityData.get(MAX_LIFE); }
    public int getTargetId() { return this.entityData.get(TARGET); }

    // ---------------------------------------------------------------- spawning

    private static WeaponEffectEntity create(ServerLevel level, int kind, Vec3 pos, Vec3 direction, float size, int color, int life) {
        WeaponEffectEntity effect = new WeaponEffectEntity(ModEntities.WEAPON_EFFECT, level);
        effect.entityData.set(KIND, kind);
        effect.entityData.set(SIZE, size);
        effect.entityData.set(COLOR, color);
        effect.entityData.set(MAX_LIFE, life);
        effect.life = life;
        effect.setPos(pos.x, pos.y, pos.z);
        double horizontal = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        float yaw = (float) (Mth.atan2(-direction.x, direction.z) * (180.0D / Math.PI));
        float pitch = (float) (-Mth.atan2(direction.y, horizontal) * (180.0D / Math.PI));
        effect.setYRot(yaw);
        effect.setXRot(pitch);
        effect.yRotO = yaw;
        effect.xRotO = pitch;
        return effect;
    }

    /** A swirl of wind flying in {@code direction}, {@code speed} blocks per tick. */
    public static void spawnWind(ServerLevel level, Vec3 pos, Vec3 direction, float size, int color, int life, double speed) {
        WeaponEffectEntity effect = create(level, WIND, pos, direction, size, color, life);
        effect.setDeltaMovement(direction.normalize().scale(speed));
        level.addFreshEntity(effect);
    }

    /** A crescent of energy flying in {@code direction}. With speed 0 it stays where it is. */
    public static void spawnCrescent(ServerLevel level, Vec3 pos, Vec3 direction, float size, int color, int life, double speed) {
        WeaponEffectEntity effect = create(level, CRESCENT, pos, direction, size, color, life);
        effect.setDeltaMovement(direction.normalize().scale(speed));
        level.addFreshEntity(effect);
    }

    /**
     * The chakra thread of Nuibari: reels {@code victim} in to the wielder, then holds it in place for 3 seconds.
     * {@code cooldownItem} is the sword that goes on cooldown when the thread ends, however it ends.
     */
    public static void spawnThread(ServerLevel level, ServerPlayer wielder, LivingEntity victim, Item cooldownItem) {
        WeaponEffectEntity effect = create(level, THREAD, wielder.getEyePosition(), wielder.getLookAngle(), 1.0F, 0xFFFFFFFF, THREAD_TICKS);
        effect.entityData.set(TARGET, victim.getId());
        effect.owner = wielder.getUUID();
        effect.target = victim.getUUID();
        effect.cooldownItem = cooldownItem;
        effect.anchor = victim.position();
        effect.lastOwnerHurt = wielder.hurtTime;
        effect.reel = REEL_TICKS;
        level.addFreshEntity(effect);
    }

    // ---------------------------------------------------------------- fire jutsu

    /**
     * The Great Fire Annihilation: how far the wave of fire spreads, how fast, how wide the fan is (half of the angle,
     * in degrees), how long it burns and what it does. The renderer uses the same numbers.
     */
    public static final double WAVE_RANGE = 50.0D;
    public static final double WAVE_SPEED = 1.5D;
    public static final double WAVE_HALF_ANGLE = 44.0D;
    public static final int WAVE_TICKS = 60;

    /**
     * Half of the angle of the fan at a distance from the caster, in radians. The fire pours out of the caster and
     * spreads sideways along curved edges, already wide close up and widening to {@link #WAVE_HALF_ANGLE}.
     */
    public static double waveHalfAngle(double radius) {
        return Math.toRadians(30.0D + (WAVE_HALF_ANGLE - 30.0D) * (1.0D - Math.exp(-radius / 18.0D)));
    }
    private static final float WAVE_DAMAGE = 3.0F;
    /** The fire from the jutsu burns for 30 seconds, and keeps being lit again for as long as the target stays in it. */
    private static final int WAVE_BURN_TICKS = 600;
    /** How long the fire takes to die down once the caster lets go. */
    private static final int WAVE_FADE_TICKS = 14;
    /** The cloud of ash: how big it is, how long it hangs in the air, and what it does when it burns. */
    private static final double CLOUD_RADIUS = 9.0D;
    public static final int CLOUD_TICKS = 20 * 20;
    private static final float ASH_DAMAGE = 22.0F;
    /** From the spark to the explosion, and how long the fire of the explosion is seen after it. */
    private static final int ASH_FUSE_TICKS = 6;
    public static final int ASH_FUSE = ASH_FUSE_TICKS;
    public static final int ASH_BURN = 14;
    public static final double ASH_RADIUS = CLOUD_RADIUS;

    /** A wave of fire spreading out in front of {@code caster}, the way they face, for 3 seconds. */
    public static void spawnFireWave(ServerLevel level, ServerPlayer caster) {
        // A wall of flame along the ground: only the way the caster faces counts, not up or down
        Vec3 flat = new Vec3(caster.getLookAngle().x, 0.0D, caster.getLookAngle().z);
        flat = flat.lengthSqr() < 1.0E-4D ? new Vec3(0.0D, 0.0D, 1.0D) : flat.normalize();
        WeaponEffectEntity effect = create(level, FIRE_WAVE, caster.position().add(flat.scale(0.8D)), flat, 1.0F, 0xFFFFFFFF, WAVE_TICKS);
        effect.owner = caster.getUUID();
        level.addFreshEntity(effect);
    }

    /** The Flame Bullet: how fast it flies, how far, what it does, and how long its burst is seen. */
    private static final double BULLET_SPEED = 1.8D;
    public static final int BULLET_TICKS = 14; // 25 meters
    private static final float BULLET_DAMAGE = 5.0F;
    private static final int BULLET_BURN_TICKS = 100;
    public static final int BULLET_BURST_TICKS = 6;

    /** A bullet of flame flying from the face of {@code caster} where they look. */
    public static void spawnFlameBullet(ServerLevel level, ServerPlayer caster) {
        Vec3 look = caster.getLookAngle();
        Vec3 start = caster.getEyePosition().add(look.scale(0.8D)).add(0.0D, -0.25D, 0.0D);
        WeaponEffectEntity effect = create(level, FLAME_BULLET, start, look, 1.0F, 0xFFFFFFFF, BULLET_TICKS + BULLET_BURST_TICKS);
        effect.owner = caster.getUUID();
        effect.setDeltaMovement(look.normalize().scale(BULLET_SPEED));
        level.addFreshEntity(effect);
    }

    /**
     * Flies the bullet on, to the first thing in its way. A living thing takes the damage and burns, and a block stops
     * it. Then it bursts for a moment (the clients are told, in TARGET, how long ago, to draw the burst).
     */
    private void tickFlameBullet() {
        if (!(level() instanceof ServerLevel serverLevel)) return;
        if (igniteTicks >= 0) {
            // Burst
            igniteTicks++;
            this.entityData.set(TARGET, igniteTicks);
            if (igniteTicks >= BULLET_BURST_TICKS) discard();
            return;
        }
        Vec3 motion = getDeltaMovement();
        Vec3 from = position();
        Vec3 to = from.add(motion);
        Entity ownerEntity = owner == null ? null : serverLevel.getEntity(owner);
        HitResult hit = serverLevel.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        Vec3 end = hit.getType() == HitResult.Type.MISS ? to : hit.getLocation();
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(serverLevel, this, from, end,
                new AABB(from, end).inflate(1.0D),
                e -> e instanceof LivingEntity && e.isAlive() && e != ownerEntity && !e.isSpectator());
        if (entityHit != null) {
            Entity victim = entityHit.getEntity();
            victim.hurt(fireSource(ownerEntity), BULLET_DAMAGE);
            victim.setRemainingFireTicks(BULLET_BURN_TICKS);
            burst(entityHit.getLocation());
        } else if (hit.getType() != HitResult.Type.MISS) {
            burst(hit.getLocation());
        } else {
            setPos(to.x, to.y, to.z);
            if (--life <= BULLET_BURST_TICKS) discard();
        }
    }

    private void burst(Vec3 at) {
        setPos(at.x, at.y, at.z);
        setDeltaMovement(Vec3.ZERO);
        igniteTicks = 0;
        this.entityData.set(TARGET, 0);
        level().playSound(null, at.x, at.y, at.z, SoundEvents.GENERIC_BURN, SoundSource.PLAYERS, 0.8F, 1.2F);
    }

    /** The Vacuum Sphere: 20 small bullets of wind, how fast they fly, how far, and what each one does. */
    private static final int VACUUM_BULLETS = 20;
    private static final double VACUUM_SPEED = 2.0D;
    public static final int VACUUM_TICKS = 25; // 50 meters
    private static final float VACUUM_DAMAGE = 5.0F;
    /** Half the angle of the cone the bullets are spread over: 12 degrees. */
    private static final double VACUUM_SPREAD = Math.tan(Math.toRadians(12.0D));
    /** Who this bullet has hit already: it goes through them, but hurts each one only once. */
    private final Set<Integer> hitIds = new HashSet<>();

    /** Wind Release: Vacuum Sphere: a spray of bullets of wind from the mouth of {@code caster}, spread over a wide cone. */
    public static void spawnVacuumSphere(ServerLevel level, ServerPlayer caster) {
        Vec3 look = caster.getLookAngle().normalize();
        Vec3 start = caster.getEyePosition().add(look.scale(0.8D)).add(0.0D, -0.25D, 0.0D);
        Vec3 right = look.cross(new Vec3(0.0D, 1.0D, 0.0D));
        right = right.lengthSqr() < 1.0E-4D ? new Vec3(1.0D, 0.0D, 0.0D) : right.normalize();
        Vec3 up = right.cross(look).normalize();
        for (int i = 0; i < VACUUM_BULLETS; i++) {
            // Spread evenly over the cone (a sunflower pattern), with a little chance in it
            double angle = i * 2.399963D + level.random.nextDouble() * 0.6D;
            double radius = Math.sqrt((i + 0.5D) / VACUUM_BULLETS) * VACUUM_SPREAD * (0.85D + level.random.nextDouble() * 0.3D);
            Vec3 direction = look.add(right.scale(Math.cos(angle) * radius)).add(up.scale(Math.sin(angle) * radius)).normalize();
            WeaponEffectEntity bullet = create(level, VACUUM_BULLET, start, direction, 0.3F, 0xFFE6F2FF, VACUUM_TICKS);
            bullet.owner = caster.getUUID();
            bullet.setDeltaMovement(direction.scale(VACUUM_SPEED));
            level.addFreshEntity(bullet);
        }
    }

    /** Flies the bullet on: it goes through every living thing in its way, hurting each once, and stops at a block. */
    private void tickVacuumBullet() {
        if (!(level() instanceof ServerLevel serverLevel)) return;
        Vec3 from = position();
        Vec3 to = from.add(getDeltaMovement());
        Entity ownerEntity = owner == null ? null : serverLevel.getEntity(owner);
        HitResult hit = serverLevel.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        Vec3 end = hit.getType() == HitResult.Type.MISS ? to : hit.getLocation();
        for (Entity e : serverLevel.getEntities(this, new AABB(from, end).inflate(0.5D),
                x -> x instanceof LivingEntity && x.isAlive() && x != ownerEntity && !x.isSpectator())) {
            if (hitIds.contains(e.getId())) continue;
            if (e.getBoundingBox().inflate(0.3D).clip(from, end).isEmpty()) continue;
            hitIds.add(e.getId());
            e.invulnerableTime = 0;
            e.hurt(windSource(ownerEntity), VACUUM_DAMAGE);
        }
        if (hit.getType() != HitResult.Type.MISS) {
            discard();
            return;
        }
        setPos(to.x, to.y, to.z);
        if (--life <= 0) discard();
    }

    private DamageSource windSource(Entity caster) {
        if (caster instanceof net.minecraft.world.entity.player.Player player) return this.damageSources().playerAttack(player);
        return this.damageSources().magic();
    }

    /** A cloud of ash hanging in the air around {@code center}. */
    public static void spawnAshCloud(ServerLevel level, ServerPlayer caster, Vec3 center) {
        WeaponEffectEntity effect = create(level, ASH_CLOUD, center, new Vec3(0.0D, 0.0D, 1.0D), 1.0F, 0xFFFFFFFF, CLOUD_TICKS);
        effect.owner = caster.getUUID();
        level.addFreshEntity(effect);
    }

    /** The cloud of ash of {@code caster} that is still waiting to be set alight, or null. */
    public static WeaponEffectEntity findAshCloud(ServerLevel level, ServerPlayer caster) {
        for (WeaponEffectEntity effect : level.getEntitiesOfClass(WeaponEffectEntity.class,
                new AABB(caster.blockPosition()).inflate(150.0D),
                e -> e.getKind() == ASH_CLOUD && caster.getUUID().equals(e.owner) && e.igniteTicks < 0)) {
            return effect;
        }
        return null;
    }

    /** Sets the cloud alight. It burns a moment later. */
    public boolean ignite() {
        if (getKind() != ASH_CLOUD || igniteTicks >= 0) return false;
        igniteTicks = 0;
        this.entityData.set(TARGET, 0);
        level().playSound(null, getX(), getY() + 2.0D, getZ(), SoundEvents.FLINTANDSTEEL_USE, SoundSource.PLAYERS, 1.2F, 1.0F);
        return true;
    }

    private DamageSource fireSource(Entity caster) {
        return new DamageSource(this.damageSources().onFire().typeHolder(), this, caster);
    }

    /**
     * The Great Fire Annihilation: the front of the wave spreads out at 1.5 blocks a tick, to 50 meters, in a fan that
     * widens like a trumpet to 76 degrees, and everything in the fan the front has passed burns for as long as the wave lasts. Whatever the
     * caster cannot see, because there is a wall in the way, is spared. The fire itself is drawn by the renderer.
     */
    private void tickFireWave() {
        if (!(level() instanceof ServerLevel serverLevel)) return;
        Entity ownerEntity = owner == null ? null : serverLevel.getEntity(owner);
        ServerPlayer caster = ownerEntity instanceof ServerPlayer p && p.isAlive() ? p : null;

        // As long as the caster holds the jutsu the fire keeps going and turns with them. Once they let go it dies
        // down for a moment and then goes out. The clients are told when it ends, to fade it out.
        boolean held = caster != null && caster.isUsingItem()
                && caster.getUseItem().getItem() instanceof net.narutoxboruto.items.jutsus.GreatFireAnnihilation;
        if (held) {
            life = WAVE_FADE_TICKS;
            this.entityData.set(MAX_LIFE, tickCount + WAVE_FADE_TICKS);
            Vec3 look = caster.getLookAngle();
            Vec3 flat = new Vec3(look.x, 0.0D, look.z);
            if (flat.lengthSqr() > 1.0E-4D) {
                flat = flat.normalize();
                Vec3 at = caster.position().add(flat.scale(0.8D));
                setPos(at.x, at.y, at.z);
                setYRot(caster.getYRot());
                yRotO = caster.getYRot();
            }
        }
        if (--life <= 0) {
            discard();
            return;
        }

        if (tickCount % 6 == 0) {
            serverLevel.playSound(null, getX(), getY() + 1.0D, getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.2F, 0.5F);
        }

        // Everything in the fan the front has passed burns
        if (tickCount % 5 != 0) return;
        boolean strike = tickCount % 20 == 0;
        double front = Math.min(WAVE_RANGE, (tickCount + 1) * WAVE_SPEED);
        float yaw = getYRot() * ((float) Math.PI / 180.0F);
        Vec3 forward = new Vec3(-Mth.sin(yaw), 0.0D, Mth.cos(yaw));
        AABB box = new AABB(position(), position()).inflate(front + 2.0D, 6.0D, front + 2.0D);
        for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, box, e -> e != ownerEntity && e.isAlive())) {
            Vec3 to = new Vec3(target.getX() - getX(), 0.0D, target.getZ() - getZ());
            double distance = to.length();
            if (distance > front) continue;
            if (distance > 1.5D && to.dot(forward) / distance < Math.cos(waveHalfAngle(distance))) continue;
            if (target.getY() < getY() - 3.0D || target.getY() > getY() + 8.0D) continue;
            if (caster != null && !caster.hasLineOfSight(target)) continue;
            // 3 damage every second, and on top of that the fire it sets
            if (strike) {
                target.invulnerableTime = 0;
                target.hurt(fireSource(ownerEntity), WAVE_DAMAGE);
            }
            target.setRemainingFireTicks(WAVE_BURN_TICKS);
        }
    }

    /**
     * The cloud of ash: it hangs in the air like smoke, and those in it can hardly see. Once it is set alight it
     * burns a moment later, see {@link #explodeAsh}.
     */
    private void tickAshCloud() {
        if (!(level() instanceof ServerLevel serverLevel)) return;
        Entity ownerEntity = owner == null ? null : serverLevel.getEntity(owner);

        if (igniteTicks >= 0) {
            // The spark is struck: a moment of fire running through the cloud, then it burns, and the fire is seen
            // for a little while longer. The renderer draws all of it from the ticks since the spark.
            igniteTicks++;
            this.entityData.set(TARGET, igniteTicks);
            if (igniteTicks == ASH_FUSE_TICKS) {
                explodeAsh(serverLevel, ownerEntity);
            }
            if (igniteTicks >= ASH_FUSE_TICKS + ASH_BURN) {
                discard();
            }
            return;
        }
        if (--life <= 0) {
            discard();
            return;
        }

        // Like a smokescreen: whoever is in the cloud can hardly see. The cloud itself is drawn by the renderer.
        if (tickCount % 10 == 0) {
            for (LivingEntity inside : serverLevel.getEntitiesOfClass(LivingEntity.class,
                    new AABB(position(), position()).inflate(CLOUD_RADIUS, 4.0D, CLOUD_RADIUS), e -> e != ownerEntity && e.isAlive())) {
                double dx = inside.getX() - getX();
                double dz = inside.getZ() - getZ();
                if (dx * dx + dz * dz <= CLOUD_RADIUS * CLOUD_RADIUS) {
                    inside.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0, false, false));
                }
            }
        }
    }

    /** The cloud burns: everything in it, friend or not, but not the one who made it, is burnt hard. */
    private void explodeAsh(ServerLevel serverLevel, Entity caster) {
        for (LivingEntity inside : serverLevel.getEntitiesOfClass(LivingEntity.class,
                new AABB(position(), position()).inflate(CLOUD_RADIUS, 5.0D, CLOUD_RADIUS), e -> e != caster && e.isAlive())) {
            double dx = inside.getX() - getX();
            double dz = inside.getZ() - getZ();
            if (dx * dx + dz * dz > CLOUD_RADIUS * CLOUD_RADIUS) continue;
            inside.invulnerableTime = 0;
            inside.hurt(fireSource(caster), ASH_DAMAGE);
            inside.setRemainingFireTicks(160);
        }
        serverLevel.playSound(null, getX(), getY() + 2.0D, getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 4.0F, 0.8F);
    }

    // ---------------------------------------------------------------- ticking

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) return;

        if (getKind() == THREAD) {
            tickThread();
            return;
        }
        if (getKind() == FIRE_WAVE) {
            tickFireWave();
            return;
        }
        if (getKind() == ASH_CLOUD) {
            tickAshCloud();
            return;
        }
        if (getKind() == FLAME_BULLET) {
            tickFlameBullet();
            return;
        }
        if (getKind() == VACUUM_BULLET) {
            tickVacuumBullet();
            return;
        }

        Vec3 motion = getDeltaMovement();
        setPos(getX() + motion.x, getY() + motion.y, getZ() + motion.z);
        if (--life <= 0) discard();
    }

    /**
     * Reels the target in, holds it and watches the thread. The thread breaks, and the target is free, when whoever
     * holds it is hit, dies or goes away, when the target dies, when it is too far away, or when the time is up.
     */
    private void tickThread() {
        if (!(level() instanceof ServerLevel serverLevel)) return;
        Entity ownerEntity = owner == null ? null : serverLevel.getEntity(owner);
        Entity targetEntity = target == null ? null : serverLevel.getEntity(target);

        if (!(ownerEntity instanceof ServerPlayer wielder) || !wielder.isAlive()) {
            endThread(null, targetEntity, "nuibari.snap");
            return;
        }
        if (!(targetEntity instanceof LivingEntity victim) || !victim.isAlive()) {
            endThread(wielder, targetEntity, "nuibari.released");
            return;
        }

        // The thread starts at the hand of the wielder
        Vec3 hand = wielder.getEyePosition().add(wielder.getLookAngle().scale(0.6D)).add(0.0D, -0.4D, 0.0D);
        setPos(hand.x, hand.y, hand.z);

        // Someone hits the wielder: the thread breaks
        if (wielder.hurtTime > lastOwnerHurt) {
            endThread(wielder, victim, "nuibari.snap");
            return;
        }
        lastOwnerHurt = wielder.hurtTime;

        if (wielder.distanceTo(victim) > THREAD_BREAK_DISTANCE) {
            endThread(wielder, victim, "nuibari.snap");
            return;
        }
        if (--life <= 0) {
            endThread(wielder, victim, "nuibari.expired");
            return;
        }

        // First the thread reels the target in: it is dragged to just in front of the wielder
        if (reel > 0) {
            reel--;
            Vec3 forward = wielder.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
            forward = forward.lengthSqr() < 1.0e-6D ? new Vec3(0.0D, 0.0D, 1.0D) : forward.normalize();
            Vec3 goal = wielder.position().add(forward.scale(REEL_STOP_DISTANCE));
            Vec3 toGoal = goal.subtract(victim.position());
            if (toGoal.horizontalDistanceSqr() > 1.0D) {
                Vec3 pull = toGoal.normalize().scale(Math.min(1.5D, toGoal.length() * 0.6D));
                victim.setDeltaMovement(pull.x, Mth.clamp(pull.y, -0.2D, 0.35D), pull.z);
                victim.hurtMarked = true;
                victim.fallDistance = 0.0F;
                anchor = victim.position();
                return;
            }
            // It has arrived: this is where it is held
            reel = 0;
            anchor = victim.position();
        }

        // Hold the target: it cannot walk (a slowness only we apply, refreshed every tick) and a player that
        // manages to get away is put back
        victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, HOLD_AMPLIFIER, false, false, false));
        victim.setDeltaMovement(0.0D, Math.min(victim.getDeltaMovement().y, 0.0D), 0.0D);
        if (victim instanceof ServerPlayer player) {
            double dx = player.getX() - anchor.x;
            double dz = player.getZ() - anchor.z;
            if (dx * dx + dz * dz > 0.01D) {
                player.connection.teleport(anchor.x, player.getY(), anchor.z, player.getYRot(), player.getXRot());
            }
            player.hurtMarked = true;
        } else {
            victim.setPos(anchor.x, victim.getY(), anchor.z);
        }
    }

    private void endThread(ServerPlayer wielder, Entity targetEntity, String messageKey) {
        if (targetEntity instanceof LivingEntity victim) {
            MobEffectInstance hold = victim.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
            if (hold != null && hold.getAmplifier() == HOLD_AMPLIFIER) victim.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        }
        if (wielder != null) {
            if (cooldownItem != null) wielder.getCooldowns().addCooldown(cooldownItem, THREAD_COOLDOWN_TICKS);
            wielder.displayClientMessage(Component.translatable(messageKey), true);
        }
        discard();
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 16384.0D;
    }

    /** The effects are never saved: a thread that is still around when the world closes just disappears. */
    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
    }
}
