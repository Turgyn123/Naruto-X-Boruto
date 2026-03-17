package net.narutoxboruto.entities.shinobis;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathType;
import net.narutoxboruto.entities.ai.AbstractShinobiMobMoveControl;
import net.narutoxboruto.entities.ai.ShinobiRangedAttackGoal;
import net.narutoxboruto.entities.ai.ShinobiSwimUpGoal;
import net.narutoxboruto.entities.throwables.AbstractThrowableWeapon;
import net.narutoxboruto.items.ModItems;
import net.narutoxboruto.items.throwables.ThrowableWeaponItem;
import org.jetbrains.annotations.Nullable;

public class AbstractShinobiMob extends PathfinderMob implements RangedAttackMob {

    public boolean searchingForLand;

    public AbstractShinobiMob(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.maxUpStep();
        this.moveControl = new AbstractShinobiMobMoveControl(this);
        this.blocksBuilding = true;
        this.setPathfindingMalus(PathType.BLOCKED.WATER, 0.0F);
    }

    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        this.setItemInHand(InteractionHand.MAIN_HAND, this.getWeapon());
        return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData);
    }

    protected ItemStack getWeapon() {
        return Items.AIR.getDefaultInstance();
    }

    @Override
    public boolean isSilent() {
        return true;
    }

    public boolean shouldHoldThrowableWeapon() {
        return this.getTarget() != null && (this.distanceTo(this.getTarget()) >= 8 || this.navigation.isStuck());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 2, false)); // Higher priority for attacking
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 60F));
        this.goalSelector.addGoal(3, new ShinobiSwimUpGoal(this));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 1.5));
        this.goalSelector.addGoal(2, new ShinobiRangedAttackGoal<>(this, 1.5, 30, 50, 25));
        this.targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Villager.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        super.registerGoals();
    }

    protected void dropCustomDeathLoot(DamageSource pSource, int pLooting, boolean pRecentlyHit) {
    }

    @Override
    public boolean canStandOnFluid(FluidState p_204042_) {
        return p_204042_.is(FluidTags.WATER) && this.fallDistance < 3 && !this.isInWater() && !wantsToSwim();
    }

    @Override
    protected void customServerAiStep() {
        if (shouldHoldThrowableWeapon()) {
            this.setItemInHand(InteractionHand.OFF_HAND, this.getThrowableWeapon());
        }
        else if (this.getItemInHand(InteractionHand.OFF_HAND).is(this.getThrowableWeapon().getItem())) {
            this.setItemInHand(InteractionHand.OFF_HAND, this.getOffhandWeapon());
        }
        super.customServerAiStep();
    }

    public void updateSwimming() {
        if (!this.level().isClientSide) {
            if (this.isEffectiveAi() && this.isInWater() && this.wantsToSwim()) {
                this.setSwimming(false);
            } else {
                this.setSwimming(false);
            }
        }

    }

    protected ItemStack getOffhandWeapon() {
        return Items.AIR.getDefaultInstance();
    }

    private ItemStack getThrowableWeapon() {
        return ModItems.SHURIKEN_ITEM.getDefaultInstance();
    }

    @Override
    public void performRangedAttack(LivingEntity pTarget, float pVelocity) {
        AbstractThrowableWeapon throwable = ((ThrowableWeaponItem) this.getThrowableWeapon().getItem()).getProjectile(
                this.level(), this, getThrowableWeapon());
        double d0 = pTarget.getEyeY() - (double) 1.1F;
        double d1 = pTarget.getX() - this.getX();
        double d2 = d0 - throwable.getY();
        double d3 = pTarget.getZ() - this.getZ();
        double d4 = Math.sqrt(d1 * d1 + d3 * d3) * (double) 0.2F;
        throwable.shoot(d1, d2 + d4, d3, 1.6F, 8.0F);
        this.swing(InteractionHand.OFF_HAND);
        this.playSound(SoundEvents.SNOW_GOLEM_SHOOT, 1.0F, 0.4F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.level().addFreshEntity(throwable);
    }

    public boolean wantsToSwim() {
        if (this.searchingForLand) {
            return false;
        }
        else {
            LivingEntity livingentity = this.getTarget();
            return livingentity != null && livingentity.isUnderWater();
        }
    }

    public void setSearchingForLand(boolean p_32399_) {
        this.searchingForLand = p_32399_;
    }

    @Override
    public boolean isInWater() {
        return false; // Treat the entity as if it's not in water
    }

    @Override
    public boolean isSwimming() {
        return false; // Prevent the entity from acting like it's swimming
    }
}
