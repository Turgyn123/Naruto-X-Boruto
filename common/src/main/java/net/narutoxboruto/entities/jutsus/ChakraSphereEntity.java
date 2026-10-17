package net.narutoxboruto.entities.jutsus;

import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.narutoxboruto.entities.ModEntities;
import net.narutoxboruto.items.jutsus.Rasengan;
import net.narutoxboruto.items.jutsus.Rasenshuriken;

import java.util.UUID;

/**
 * The sphere of chakra of the Rasengan and of the Rasenshuriken. It is an entity with a model of its own (see
 * {@code ChakraSphereRenderer}). It is made in the hand of the caster while the jutsu is charged, and then:
 * <ul>
 *   <li>Rasengan: the caster dashes forward with it and it is driven into the first living thing in the way, which
 *       it hits hard and throws away.</li>
 *   <li>Rasenshuriken: it is thrown and flies straight. Where it hits it grows into a huge sphere of blades that cuts
 *       everything in it, pulls it in and slows it, and bursts at the end.</li>
 * </ul>
 * It lasts only a short while and is never saved. Its state goes to the clients so that they can draw it.
 */
public class ChakraSphereEntity extends Entity {

    public static final int RASENGAN = 0;
    public static final int RASENSHURIKEN = 1;

    /** The phases: it is charged, then driven forward or flying, then it has hit. */
    public static final int CHARGING = 0;
    public static final int ACTIVE = 1;
    public static final int IMPACT = 2;

    /** How long the sphere is charged for, in ticks, before it can be used. */
    public static final int RASENGAN_CHARGE_TICKS = 16;
    public static final int RASENSHURIKEN_CHARGE_TICKS = 30;
    /** The Rasengan: how long the caster drives it forward, how long the burst is seen, what it does. */
    public static final int RASENGAN_THRUST_TICKS = 10;
    public static final int RASENGAN_BURST_TICKS = 10;
    private static final float RASENGAN_DAMAGE = 18.0F;
    /** The Rasenshuriken: how fast and how far it flies, and the sphere of blades it becomes. */
    private static final double SHURIKEN_SPEED = 1.2D;
    public static final int SHURIKEN_FLIGHT_TICKS = 40;
    public static final int SHURIKEN_GROW_TICKS = 10;
    public static final int SHURIKEN_BLAST_TICKS = 50;
    public static final double SHURIKEN_RADIUS = 6.0D;
    private static final float SHURIKEN_TICK_DAMAGE = 3.5F;
    private static final float SHURIKEN_FINAL_DAMAGE = 10.0F;

    private static final EntityDataAccessor<Integer> KIND = SynchedEntityData.defineId(ChakraSphereEntity.class, EntityDataSerializers.INT);
    /** The phase times 1000 plus the ticks in that phase. */
    private static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(ChakraSphereEntity.class, EntityDataSerializers.INT);
    /** The entity id of the caster, so that the clients can draw the sphere in their hand. */
    private static final EntityDataAccessor<Integer> OWNER_ID = SynchedEntityData.defineId(ChakraSphereEntity.class, EntityDataSerializers.INT);

    private UUID owner;
    private int phase;
    private int phaseTicks;
    private Vec3 flight = Vec3.ZERO;

    public ChakraSphereEntity(EntityType<? extends ChakraSphereEntity> entityType, Level level) {
        super(entityType, level);
        this.noCulling = true;
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(KIND, RASENGAN);
        builder.define(STATE, 0);
        builder.define(OWNER_ID, -1);
    }

    public int getKind() { return this.entityData.get(KIND); }
    public int getPhase() { return this.entityData.get(STATE) / 1000; }
    public int getPhaseTicks() { return this.entityData.get(STATE) % 1000; }
    public int getOwnerId() { return this.entityData.get(OWNER_ID); }

    // ---------------------------------------------------------------- spawning and using

    /**
     * Where the sphere is held. The Rasengan: a little in front of the caster, to the right and low, like in a hand.
     * The Rasenshuriken: above the head of the caster, like a hand held up high.
     */
    public static Vec3 handPosition(Entity caster, int kind, float partialTick) {
        Vec3 look = caster.getViewVector(partialTick);
        if (kind == RASENSHURIKEN) {
            Vec3 flat = new Vec3(look.x, 0.0D, look.z);
            flat = flat.lengthSqr() < 1.0E-4D ? Vec3.ZERO : flat.normalize().scale(0.3D);
            return caster.getEyePosition(partialTick).add(0.0D, 1.0D, 0.0D).add(flat);
        }
        Vec3 right = new Vec3(-look.z, 0.0D, look.x);
        right = right.lengthSqr() < 1.0E-4D ? new Vec3(1.0D, 0.0D, 0.0D) : right.normalize();
        return caster.getEyePosition(partialTick).add(look.scale(1.05D)).add(right.scale(0.5D)).add(0.0D, -0.5D, 0.0D);
    }

    /** A sphere of {@code kind} charging in the hand of {@code caster}. */
    public static ChakraSphereEntity spawn(ServerLevel level, ServerPlayer caster, int kind) {
        ChakraSphereEntity sphere = new ChakraSphereEntity(ModEntities.CHAKRA_SPHERE, level);
        sphere.entityData.set(KIND, kind);
        sphere.entityData.set(OWNER_ID, caster.getId());
        sphere.owner = caster.getUUID();
        Vec3 hand = handPosition(caster, kind, 1.0F);
        sphere.setPos(hand.x, hand.y, hand.z);
        sphere.setRot(caster.getYRot(), caster.getXRot());
        level.addFreshEntity(sphere);
        return sphere;
    }

    /** The sphere of {@code caster} that is still being charged, or null. */
    public static ChakraSphereEntity findHeld(ServerLevel level, ServerPlayer caster, int kind) {
        for (ChakraSphereEntity sphere : level.getEntitiesOfClass(ChakraSphereEntity.class,
                new AABB(caster.blockPosition()).inflate(8.0D),
                e -> e.getKind() == kind && caster.getUUID().equals(e.owner) && e.phase == CHARGING)) {
            return sphere;
        }
        return null;
    }

    /** Whether the sphere has been charged long enough to be used. */
    public boolean isCharged() {
        if (phase != CHARGING) return false;
        return phaseTicks >= (getKind() == RASENGAN ? RASENGAN_CHARGE_TICKS : RASENSHURIKEN_CHARGE_TICKS);
    }

    /** The caster let go too early: the sphere falls apart. */
    public void cancel() {
        discard();
    }

    /** The caster uses the charged sphere: the Rasengan is driven forward, the Rasenshuriken is thrown. */
    public void release(ServerPlayer caster) {
        if (phase != CHARGING) return;
        enterPhase(ACTIVE);
        if (getKind() == RASENSHURIKEN) {
            Vec3 look = caster.getLookAngle().normalize();
            flight = look.scale(SHURIKEN_SPEED);
            Vec3 hand = handPosition(caster, RASENSHURIKEN, 1.0F);
            setPos(hand.x, hand.y, hand.z);
            setRot(caster.getYRot(), caster.getXRot());
            level().playSound(null, hand.x, hand.y, hand.z, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.6F, 0.5F);
        }
    }

    private void enterPhase(int newPhase) {
        phase = newPhase;
        phaseTicks = 0;
        this.entityData.set(STATE, phase * 1000);
    }

    private void countPhase() {
        phaseTicks++;
        this.entityData.set(STATE, phase * 1000 + Math.min(phaseTicks, 999));
    }

    private DamageSource sphereSource(Entity caster) {
        if (caster instanceof ServerPlayer player) return this.damageSources().playerAttack(player);
        return this.damageSources().magic();
    }

    /** Whether the caster is still holding the jutsu that made this sphere. */
    private boolean stillHeld(ServerPlayer caster) {
        if (!caster.isUsingItem()) return false;
        Item using = caster.getUseItem().getItem();
        return getKind() == RASENGAN ? using instanceof Rasengan : using instanceof Rasenshuriken;
    }

    // ---------------------------------------------------------------- ticking

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) return;
        if (getKind() == RASENGAN) tickRasengan();
        else tickRasenshuriken();
    }

    /**
     * The Rasengan. Charging: it follows the hand of the caster. Then the caster dashes forward with it and it is
     * driven into the first living thing in the way, which it hits hard and throws away.
     */
    private void tickRasengan() {
        if (!(level() instanceof ServerLevel serverLevel)) return;
        Entity ownerEntity = owner == null ? null : serverLevel.getEntity(owner);
        if (!(ownerEntity instanceof ServerPlayer caster) || !caster.isAlive()) {
            discard();
            return;
        }
        if (phase == CHARGING && !stillHeld(caster)) {
            discard();
            return;
        }
        countPhase();
        if (phase == CHARGING) {
            Vec3 hand = handPosition(caster, RASENGAN, 1.0F);
            setPos(hand.x, hand.y, hand.z);
            setRot(caster.getYRot(), caster.getXRot());
            if (phaseTicks % 6 == 0) {
                serverLevel.playSound(null, hand.x, hand.y, hand.z, SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 0.4F, 1.8F);
            }
        } else if (phase == ACTIVE) {
            Vec3 hand = handPosition(caster, RASENGAN, 1.0F);
            setPos(hand.x, hand.y, hand.z);
            setRot(caster.getYRot(), caster.getXRot());
            // The caster dashes forward with it
            Vec3 look = caster.getLookAngle();
            caster.setDeltaMovement(look.x * 0.95D, Math.max(caster.getDeltaMovement().y, 0.0D) * 0.5D, look.z * 0.95D);
            caster.hurtMarked = true;
            for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, new AABB(hand, hand).inflate(1.5D),
                    e -> e != caster && e.isAlive() && !e.isSpectator())) {
                target.invulnerableTime = 0;
                target.hurt(sphereSource(caster), RASENGAN_DAMAGE);
                target.setDeltaMovement(look.x * 2.2D, 0.55D, look.z * 2.2D);
                target.hurtMarked = true;
                serverLevel.playSound(null, hand.x, hand.y, hand.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.6F, 1.5F);
                enterPhase(IMPACT);
                break;
            }
            if (phase == ACTIVE && phaseTicks >= RASENGAN_THRUST_TICKS) discard();
        } else if (phaseTicks >= RASENGAN_BURST_TICKS) {
            discard();
        }
    }

    /**
     * The Rasenshuriken. Charging: it follows the hand of the caster. Then it is thrown and flies straight. Where it
     * hits, or where it runs out of range, it grows into a huge sphere of blades that cuts everything in it, pulls it
     * in a little and slows it, and bursts at the end.
     */
    private void tickRasenshuriken() {
        if (!(level() instanceof ServerLevel serverLevel)) return;
        Entity ownerEntity = owner == null ? null : serverLevel.getEntity(owner);
        if (phase == CHARGING && !(ownerEntity instanceof ServerPlayer p && p.isAlive() && stillHeld(p))) {
            discard();
            return;
        }
        countPhase();
        if (phase == CHARGING) {
            Vec3 hand = handPosition(ownerEntity, RASENSHURIKEN, 1.0F);
            setPos(hand.x, hand.y, hand.z);
            setRot(ownerEntity.getYRot(), ownerEntity.getXRot());
            if (phaseTicks % 6 == 0) {
                serverLevel.playSound(null, hand.x, hand.y, hand.z, SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 0.5F, 1.3F);
            }
        } else if (phase == ACTIVE) {
            Vec3 from = position();
            Vec3 to = from.add(flight);
            HitResult hit = serverLevel.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            Vec3 end = hit.getType() == HitResult.Type.MISS ? to : hit.getLocation();
            EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(serverLevel, this, from, end,
                    new AABB(from, end).inflate(1.0D),
                    e -> e instanceof LivingEntity && e.isAlive() && e != ownerEntity && !e.isSpectator());
            if (entityHit != null) {
                explodeShuriken(entityHit.getLocation());
            } else if (hit.getType() != HitResult.Type.MISS) {
                explodeShuriken(hit.getLocation());
            } else {
                setPos(to.x, to.y, to.z);
                if (phaseTicks >= SHURIKEN_FLIGHT_TICKS) explodeShuriken(to);
            }
        } else {
            float grown = Math.min(1.0F, phaseTicks / (float) SHURIKEN_GROW_TICKS);
            double radius = SHURIKEN_RADIUS * grown;
            if (phaseTicks % 4 == 0 && phaseTicks <= SHURIKEN_BLAST_TICKS - 4) {
                for (LivingEntity inside : serverLevel.getEntitiesOfClass(LivingEntity.class,
                        new AABB(position(), position()).inflate(radius), e -> e != ownerEntity && e.isAlive() && !e.isSpectator())) {
                    if (inside.position().add(0.0D, inside.getBbHeight() * 0.5D, 0.0D).distanceToSqr(position()) > radius * radius) continue;
                    inside.invulnerableTime = 0;
                    inside.hurt(sphereSource(ownerEntity), SHURIKEN_TICK_DAMAGE);
                    inside.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 2, false, false));
                    Vec3 pull = position().subtract(inside.position());
                    if (pull.lengthSqr() > 1.0E-4D) {
                        Vec3 n = pull.normalize().scale(0.25D);
                        inside.setDeltaMovement(inside.getDeltaMovement().multiply(0.5D, 0.5D, 0.5D).add(n));
                        inside.hurtMarked = true;
                    }
                }
                serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.5F, 0.6F);
            }
            if (phaseTicks == SHURIKEN_BLAST_TICKS - 2) {
                for (LivingEntity inside : serverLevel.getEntitiesOfClass(LivingEntity.class,
                        new AABB(position(), position()).inflate(SHURIKEN_RADIUS), e -> e != ownerEntity && e.isAlive() && !e.isSpectator())) {
                    if (inside.position().distanceToSqr(position()) > SHURIKEN_RADIUS * SHURIKEN_RADIUS) continue;
                    inside.invulnerableTime = 0;
                    inside.hurt(sphereSource(ownerEntity), SHURIKEN_FINAL_DAMAGE);
                    Vec3 away = inside.position().subtract(position());
                    away = away.lengthSqr() < 1.0E-4D ? new Vec3(0.0D, 1.0D, 0.0D) : away.normalize();
                    inside.setDeltaMovement(away.x * 1.6D, 0.7D, away.z * 1.6D);
                    inside.hurtMarked = true;
                }
                serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 3.0F, 1.2F);
            }
            if (phaseTicks >= SHURIKEN_BLAST_TICKS) discard();
        }
    }

    private void explodeShuriken(Vec3 at) {
        setPos(at.x, at.y, at.z);
        enterPhase(IMPACT);
        level().playSound(null, at.x, at.y, at.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.5F, 1.6F);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 16384.0D;
    }

    /** The spheres are never saved: one that is still around when the world closes just disappears. */
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
