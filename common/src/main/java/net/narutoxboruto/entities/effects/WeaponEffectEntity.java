package net.narutoxboruto.entities.effects;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.narutoxboruto.entities.ModEntities;

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
 * </ul>
 * The effects only live a short while, are never saved and only the server moves them.
 */
public class WeaponEffectEntity extends Entity {

    public static final int WIND = 0;
    public static final int CRESCENT = 1;
    public static final int THREAD = 2;

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

    // ---------------------------------------------------------------- ticking

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) return;

        if (getKind() == THREAD) {
            tickThread();
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
