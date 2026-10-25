package net.narutoxboruto.entities.jutsus;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.narutoxboruto.capabilities.info.Chakra;
import net.narutoxboruto.entities.ModEntities;
import net.narutoxboruto.main.platform.Services;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Water Release: Water Clone Technique. A real copy of the caster made of water: it wears what the caster wears, follows
 * them, and fights what they fight, but it is fragile and only hits for a fraction of the caster's strength. It costs the
 * caster upkeep chakra for as long as it stands. When it is hurt enough, runs out of the caster's range (30 meters, 80 with
 * water close by), or the caster runs out of chakra or dies, it falls back into water.
 */
public class WaterCloneEntity extends PathfinderMob {

    private static final EntityDataAccessor<Optional<UUID>> OWNER = SynchedEntityData.defineId(WaterCloneEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    public static final int MAX_CLONES = 3;
    public static final double RANGE = 30.0D;
    public static final double RANGE_NEAR_WATER = 80.0D;
    /** Upkeep: 20 chakra per second for every clone that stands. */
    private static final int UPKEEP_INTERVAL = 20;
    public static final int UPKEEP_COST = 20;

    public WaterCloneEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.34D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    public static AttributeSupplier setAttribute() {
        return createAttributes().build();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OWNER, Optional.empty());
    }

    public UUID getOwnerId() {
        return this.entityData.get(OWNER).orElse(null);
    }

    public Player getOwner() {
        UUID id = getOwnerId();
        return id == null ? null : level().getPlayerByUUID(id);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(3, new FollowOwnerGoal(this));
        this.targetSelector.addGoal(1, new OwnerTargetGoal(this));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
    }

    // ---------------------------------------------------------------- spawning

    /** How many clones this player has standing. */
    public static List<WaterCloneEntity> clonesOf(ServerLevel level, ServerPlayer owner) {
        return level.getEntitiesOfClass(WaterCloneEntity.class, owner.getBoundingBox().inflate(RANGE_NEAR_WATER * 2.0D),
                e -> owner.getUUID().equals(e.getOwnerId()) && e.isAlive());
    }

    /** Makes a clone of {@code owner} beside them. Returns false when they already have the most clones. */
    public static boolean spawn(ServerLevel level, ServerPlayer owner) {
        if (clonesOf(level, owner).size() >= MAX_CLONES) {
            owner.displayClientMessage(Component.literal("You can only keep " + MAX_CLONES + " water clones"), true);
            return false;
        }
        WaterCloneEntity clone = new WaterCloneEntity(ModEntities.WATER_CLONE, level);
        clone.entityData.set(OWNER, Optional.of(owner.getUUID()));
        clone.setCustomName(owner.getName());
        // Beside the caster, on the side where there is room
        Vec3 look = owner.getLookAngle();
        Vec3 flat = new Vec3(look.x, 0.0D, look.z);
        flat = flat.lengthSqr() < 1.0E-4D ? new Vec3(0.0D, 0.0D, 1.0D) : flat.normalize();
        Vec3 right = new Vec3(-flat.z, 0.0D, flat.x);
        Vec3 at = owner.position();
        for (double side : new double[]{1.6D, -1.6D, 0.0D}) {
            Vec3 candidate = owner.position().add(right.scale(side)).add(flat.scale(1.2D));
            clone.setPos(candidate.x, candidate.y, candidate.z);
            if (level.noCollision(clone, clone.getBoundingBox())) {
                at = candidate;
                break;
            }
        }
        clone.moveTo(at.x, at.y, at.z, owner.getYRot(), 0.0F);
        clone.setYHeadRot(owner.getYRot());
        // It wears and holds what the caster does (and never drops it)
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot == EquipmentSlot.BODY) continue;
            clone.setItemSlot(slot, owner.getItemBySlot(slot).copy());
            clone.setDropChance(slot, 0.0F);
        }
        level.addFreshEntity(clone);
        clone.splash(level, 1.0F);
        return true;
    }

    // ---------------------------------------------------------------- upkeep and life

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide() || !(level() instanceof ServerLevel serverLevel)) return;
        Player owner = getOwner();
        if (!(owner instanceof ServerPlayer caster) || !caster.isAlive()) {
            revert();
            return;
        }
        if (tickCount % 10 == 0) {
            double limit = nearWater() ? RANGE_NEAR_WATER : RANGE;
            if (distanceToSqr(caster) > limit * limit || caster.level() != level()) {
                revert();
                return;
            }
        }
        if (tickCount > 0 && tickCount % UPKEEP_INTERVAL == 0) {
            Chakra chakra = Services.PLATFORM.getChakra(caster);
            if (chakra.getValue() < UPKEEP_COST) {
                caster.displayClientMessage(Component.translatable("msg.no_chakra"), true);
                revert();
                return;
            }
            chakra.subValue(UPKEEP_COST, caster);
        }
        if (tickCount % 4 == 0) {
            // A shimmer of water drops
            serverLevel.sendParticles(ParticleTypes.FALLING_WATER, getX(), getY() + 1.0D, getZ(), 1, 0.25D, 0.7D, 0.25D, 0.0D);
        }
    }

    /** Whether there is water within a few blocks: then the clone can go much further from the caster. */
    private boolean nearWater() {
        BlockPos center = blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-6, -3, -6), center.offset(6, 3, 6))) {
            if (level().getFluidState(pos).is(FluidTags.WATER)) return true;
        }
        return false;
    }

    /** The clone falls apart into water. */
    public void revert() {
        if (level() instanceof ServerLevel serverLevel && !isRemoved()) {
            splash(serverLevel, 1.4F);
        }
        discard();
    }

    private void splash(ServerLevel serverLevel, float strength) {
        serverLevel.sendParticles(ParticleTypes.SPLASH, getX(), getY() + 1.0D, getZ(), (int) (40 * strength), 0.4D, 0.8D, 0.4D, 0.1D);
        serverLevel.sendParticles(ParticleTypes.BUBBLE_POP, getX(), getY() + 1.0D, getZ(), (int) (12 * strength), 0.4D, 0.8D, 0.4D, 0.02D);
        serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.PLAYER_SPLASH, SoundSource.PLAYERS, 1.0F, 1.2F);
    }

    // ---------------------------------------------------------------- damage

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Whoever made it cannot hurt it, and neither can its own kind
        if (source.getEntity() instanceof Player player && player.getUUID().equals(getOwnerId())) return false;
        if (source.getEntity() instanceof WaterCloneEntity other && java.util.Objects.equals(other.getOwnerId(), getOwnerId())) return false;
        boolean hurt = super.hurt(source, amount);
        if (hurt && level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SPLASH, getX(), getY() + 1.0D, getZ(), 12, 0.3D, 0.6D, 0.3D, 0.05D);
        }
        return hurt;
    }

    /** When it dies it does not fall over: it falls back into water at once and leaves nothing. */
    @Override
    protected void tickDeath() {
        revert();
    }

    @Override
    protected boolean shouldDropLoot() {
        return false;
    }

    @Override
    public boolean shouldDropExperience() {
        return false;
    }

    /** It is water: it cannot drown. */
    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return source.is(net.minecraft.world.damagesource.DamageTypes.DROWN) || super.isInvulnerableTo(source);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        UUID id = getOwnerId();
        if (id != null) tag.putUUID("Owner", id);
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("Owner")) this.entityData.set(OWNER, Optional.of(tag.getUUID("Owner")));
    }

    // ---------------------------------------------------------------- goals

    /** Keeps close to the caster: runs after them when they are more than a few blocks away. */
    private static class FollowOwnerGoal extends Goal {
        private final WaterCloneEntity clone;
        private Player owner;
        private int recalc;

        FollowOwnerGoal(WaterCloneEntity clone) {
            this.clone = clone;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            owner = clone.getOwner();
            return owner != null && clone.getTarget() == null && clone.distanceToSqr(owner) > 5.0D * 5.0D;
        }

        @Override
        public boolean canContinueToUse() {
            return owner != null && owner.isAlive() && clone.getTarget() == null && clone.distanceToSqr(owner) > 2.5D * 2.5D;
        }

        @Override
        public void start() {
            recalc = 0;
        }

        @Override
        public void tick() {
            clone.getLookControl().setLookAt(owner, 10.0F, clone.getMaxHeadXRot());
            if (--recalc <= 0) {
                recalc = 8;
                double speed = clone.distanceToSqr(owner) > 144.0D ? 1.7D : 1.25D;
                clone.getNavigation().moveTo(owner, speed);
            }
        }

        @Override
        public void stop() {
            clone.getNavigation().stop();
        }
    }

    /** Fights what the caster fights: whatever they hit last, or whatever hit them. */
    private static class OwnerTargetGoal extends TargetGoal {
        private final WaterCloneEntity clone;
        private LivingEntity wanted;

        OwnerTargetGoal(WaterCloneEntity clone) {
            super(clone, false);
            this.clone = clone;
            setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            Player owner = clone.getOwner();
            if (owner == null) return false;
            LivingEntity candidate = null;
            if (owner.tickCount - owner.getLastHurtMobTimestamp() < 100) candidate = owner.getLastHurtMob();
            if (candidate == null && owner.tickCount - owner.getLastHurtByMobTimestamp() < 100) candidate = owner.getLastHurtByMob();
            if (candidate == null || !candidate.isAlive() || candidate == clone || candidate == owner) return false;
            if (candidate instanceof WaterCloneEntity other && java.util.Objects.equals(other.getOwnerId(), clone.getOwnerId())) return false;
            wanted = candidate;
            return true;
        }

        @Override
        public void start() {
            clone.setTarget(wanted);
            super.start();
        }
    }
}
