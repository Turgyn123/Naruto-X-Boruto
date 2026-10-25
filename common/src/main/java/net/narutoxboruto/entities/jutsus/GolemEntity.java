package net.narutoxboruto.entities.jutsus;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.narutoxboruto.capabilities.info.Chakra;
import net.narutoxboruto.entities.ModEntities;
import net.narutoxboruto.main.platform.Services;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Earth Release: Golem Technique. A huge humanoid of rock that rises out of the ground in front of the caster, follows
 * them and fights what they fight. It is very tough (it shrugs off explosions, fire, drowning and falls), very strong, and
 * it knocks what it hits into the air. It costs the caster upkeep chakra for as long as it stands. It crumbles back into
 * earth when it takes enough damage, goes further than 25 meters from the caster, or the caster runs out of chakra or dies.
 */
public class GolemEntity extends IronGolem {

    private static final EntityDataAccessor<Optional<UUID>> OWNER = SynchedEntityData.defineId(GolemEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    public static final double RANGE = 25.0D;
    /** Upkeep: 30 chakra per second. */
    private static final int UPKEEP_INTERVAL = 20;
    public static final int UPKEEP_COST = 30;

    public GolemEntity(EntityType<? extends IronGolem> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier setAttribute() {
        return IronGolem.createAttributes()
                .add(Attributes.MAX_HEALTH, 160.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.ATTACK_DAMAGE, 20.0D)
                .add(Attributes.ARMOR, 12.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .build();
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

    /** The golem serves its caster only: none of the iron golem's own wants (villagers, wandering, anger). */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.1D, true));
        this.goalSelector.addGoal(2, new FollowOwnerGoal(this));
        this.targetSelector.addGoal(1, new OwnerTargetGoal(this));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
    }

    // ---------------------------------------------------------------- spawning

    public static GolemEntity of(ServerLevel level, ServerPlayer owner) {
        for (GolemEntity golem : level.getEntitiesOfClass(GolemEntity.class, owner.getBoundingBox().inflate(RANGE * 3.0D),
                e -> owner.getUUID().equals(e.getOwnerId()) && e.isAlive())) {
            return golem;
        }
        return null;
    }

    /** The golem rises out of the ground in front of {@code owner}. Returns false when they already have one. */
    public static boolean spawn(ServerLevel level, ServerPlayer owner) {
        if (of(level, owner) != null) {
            owner.displayClientMessage(Component.literal("Your golem is already standing"), true);
            return false;
        }
        GolemEntity golem = new GolemEntity(ModEntities.GOLEM, level);
        golem.entityData.set(OWNER, Optional.of(owner.getUUID()));
        Vec3 look = owner.getLookAngle();
        Vec3 flat = new Vec3(look.x, 0.0D, look.z);
        flat = flat.lengthSqr() < 1.0E-4D ? new Vec3(0.0D, 0.0D, 1.0D) : flat.normalize();
        Vec3 at = owner.position();
        for (double distance : new double[]{3.5D, 2.8D, 4.5D, 0.0D}) {
            Vec3 candidate = owner.position().add(flat.scale(distance));
            golem.setPos(candidate.x, candidate.y, candidate.z);
            if (level.noCollision(golem, golem.getBoundingBox())) {
                at = candidate;
                break;
            }
        }
        golem.moveTo(at.x, at.y, at.z, owner.getYRot() + 180.0F, 0.0F);
        golem.setYHeadRot(golem.getYRot());
        level.addFreshEntity(golem);
        BlockState ground = level.getBlockState(BlockPos.containing(at.x, at.y - 0.5D, at.z));
        if (ground.isAir()) ground = Blocks.DIRT.defaultBlockState();
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), at.x, at.y + 0.2D, at.z, 80, 1.0D, 0.3D, 1.0D, 0.15D);
        level.playSound(null, at.x, at.y, at.z, SoundEvents.GRAVEL_BREAK, SoundSource.PLAYERS, 2.0F, 0.5F);
        level.playSound(null, at.x, at.y, at.z, SoundEvents.STONE_BREAK, SoundSource.PLAYERS, 2.0F, 0.6F);
        return true;
    }

    // ---------------------------------------------------------------- upkeep and life

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide() || !(level() instanceof ServerLevel)) return;
        Player owner = getOwner();
        if (!(owner instanceof ServerPlayer caster) || !caster.isAlive()) {
            revert();
            return;
        }
        if (tickCount % 10 == 0 && (distanceToSqr(caster) > RANGE * RANGE || caster.level() != level())) {
            revert();
            return;
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
    }

    /** The golem crumbles back into earth. */
    public void revert() {
        if (level() instanceof ServerLevel serverLevel && !isRemoved()) {
            BlockState ground = serverLevel.getBlockState(blockPosition().below());
            if (ground.isAir()) ground = Blocks.DIRT.defaultBlockState();
            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), getX(), getY() + 1.5D, getZ(), 120, 0.9D, 1.4D, 0.9D, 0.15D);
            serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.STONE_BREAK, SoundSource.PLAYERS, 2.0F, 0.5F);
            serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.GRAVEL_BREAK, SoundSource.PLAYERS, 2.0F, 0.6F);
        }
        discard();
    }

    // ---------------------------------------------------------------- damage

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof Player player && player.getUUID().equals(getOwnerId())) return false;
        if (source.getEntity() instanceof GolemEntity || source.getEntity() instanceof WaterCloneEntity) {
            Object other = source.getEntity();
            UUID otherOwner = other instanceof GolemEntity g ? g.getOwnerId() : ((WaterCloneEntity) other).getOwnerId();
            if (Objects.equals(otherOwner, getOwnerId())) return false;
        }
        return super.hurt(source, amount);
    }

    /** Rock: it withstands a point-blank explosion, and fire, drowning and falls. */
    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return source.is(DamageTypeTags.IS_EXPLOSION) || source.is(DamageTypeTags.IS_FIRE)
                || source.is(DamageTypes.DROWN) || source.is(DamageTypeTags.IS_FALL)
                || super.isInvulnerableTo(source);
    }

    /** When it dies it crumbles at once and leaves nothing (not even iron). */
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

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.STONE_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.STONE_BREAK;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.STONE_STEP, 1.2F, 0.7F);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        UUID id = getOwnerId();
        if (id != null) tag.putUUID("Owner", id);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("Owner")) this.entityData.set(OWNER, Optional.of(tag.getUUID("Owner")));
    }

    // ---------------------------------------------------------------- goals

    /** Keeps close to the caster as a guard: walks to them when they are more than a few blocks away. */
    private static class FollowOwnerGoal extends Goal {
        private final GolemEntity golem;
        private Player owner;
        private int recalc;

        FollowOwnerGoal(GolemEntity golem) {
            this.golem = golem;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            owner = golem.getOwner();
            return owner != null && golem.getTarget() == null && golem.distanceToSqr(owner) > 6.0D * 6.0D;
        }

        @Override
        public boolean canContinueToUse() {
            return owner != null && owner.isAlive() && golem.getTarget() == null && golem.distanceToSqr(owner) > 3.5D * 3.5D;
        }

        @Override
        public void start() {
            recalc = 0;
        }

        @Override
        public void tick() {
            golem.getLookControl().setLookAt(owner, 10.0F, golem.getMaxHeadXRot());
            if (--recalc <= 0) {
                recalc = 8;
                golem.getNavigation().moveTo(owner, golem.distanceToSqr(owner) > 144.0D ? 1.5D : 1.1D);
            }
        }

        @Override
        public void stop() {
            golem.getNavigation().stop();
        }
    }

    /** Fights what the caster fights: whatever they hit last, or whatever hit them. */
    private static class OwnerTargetGoal extends TargetGoal {
        private final GolemEntity golem;
        private LivingEntity wanted;

        OwnerTargetGoal(GolemEntity golem) {
            super(golem, false);
            this.golem = golem;
            setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            Player owner = golem.getOwner();
            if (owner == null) return false;
            LivingEntity candidate = null;
            if (owner.tickCount - owner.getLastHurtMobTimestamp() < 100) candidate = owner.getLastHurtMob();
            if (candidate == null && owner.tickCount - owner.getLastHurtByMobTimestamp() < 100) candidate = owner.getLastHurtByMob();
            if (candidate == null || !candidate.isAlive() || candidate == golem || candidate == owner) return false;
            if (candidate instanceof WaterCloneEntity clone && Objects.equals(clone.getOwnerId(), golem.getOwnerId())) return false;
            wanted = candidate;
            return true;
        }

        @Override
        public void start() {
            golem.setTarget(wanted);
            super.start();
        }
    }
}
