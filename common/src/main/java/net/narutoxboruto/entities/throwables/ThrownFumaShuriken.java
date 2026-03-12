package net.narutoxboruto.entities.throwables;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.narutoxboruto.main.Main;
import net.narutoxboruto.entities.ModEntities;
import net.narutoxboruto.items.ModItems;

import javax.annotation.Nullable;

public class ThrownFumaShuriken extends AbstractThrowableWeapon {
    private static final EntityDataAccessor<Byte> ID_LOYALTY = SynchedEntityData.defineId(ThrownFumaShuriken.class,
            EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> ID_FOIL = SynchedEntityData.defineId(ThrownFumaShuriken.class,
            EntityDataSerializers.BOOLEAN);
    public int clientSideReturnTridentTickCount;
    private ItemStack tridentItem = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, ModItems.FUMA_SHURIKEN)).getDefaultInstance();
    private boolean dealtDamage;


    public ThrownFumaShuriken(EntityType<? extends AbstractThrowableWeapon> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public ThrownFumaShuriken(Level pLevel, LivingEntity pShooter, ItemStack pStack) {
        super(ModEntities.FUMA_SHURIKEN, pLevel);
        this.tridentItem = pStack.copy();
        this.entityData.set(ID_LOYALTY, (byte) 2);
        this.entityData.set(ID_FOIL, pStack.hasFoil());
        this.setOwner(pShooter);
    }

    @Override
    public double getBaseDamage() {
        return 5;
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, ModItems.FUMA_SHURIKEN)));
    }

    @Override
    protected boolean fumaSpin() {
        return true;
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ID_LOYALTY, (byte) 2);  // Default loyalty level
        builder.define(ID_FOIL, false);  // No enchantment glow by default
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void tick() {
        super.tick(); // Moved to top for correct physics application

        // Handle in-ground behavior
        if (this.inGround) {
            if (this.inGroundTime > 4) {
                this.dealtDamage = true;
            }
        }

        Entity entity = this.getOwner();
        int loyaltyLevel = this.entityData.get(ID_LOYALTY);

        // Loyalty return logic
        if (loyaltyLevel > 0 && (this.dealtDamage || this.isNoPhysics()) && entity != null) {
            if (!this.isAcceptibleReturnOwner()) {
                if (!this.level().isClientSide && this.pickup == AbstractArrow.Pickup.ALLOWED) {
                    this.spawnAtLocation(this.getPickupItem(), 0.1F);
                }
                this.discard();
            } else {
                this.setNoPhysics(true);
                Vec3 ownerEyePos = entity.getEyePosition();
                Vec3 toOwner = ownerEyePos.subtract(this.position());

                // Adjust position for return path
                this.setPosRaw(this.getX(), this.getY() + toOwner.y * 0.015D * loyaltyLevel, this.getZ());

                if (this.level().isClientSide) {
                    this.yOld = this.getY(); // Update client-side rendering position
                }

                // Calculate return velocity
                double speedFactor = 0.05D * loyaltyLevel;
                Vec3 newMotion = this.getDeltaMovement()
                        .scale(0.95D)
                        .add(toOwner.normalize().scale(speedFactor));
                this.setDeltaMovement(newMotion);

                // Play return sound (client-side only)
                if (this.clientSideReturnTridentTickCount == 0) {
                    this.playSound(SoundEvents.TRIDENT_RETURN, 10.0F, 1.0F);
                }
                this.clientSideReturnTridentTickCount++;
            }
        }
    }

    private boolean isAcceptibleReturnOwner() {
        Entity entity = this.getOwner();
        if (entity != null && entity.isAlive()) {
            return !(entity instanceof ServerPlayer) || !entity.isSpectator();
        }
        else {
            return false;
        }
    }

    protected ItemStack getPickupItem() {
        return this.tridentItem.copy();
    }

    public boolean isFoil() {
        return this.entityData.get(ID_FOIL);
    }

    /**
     * Gets the EntityHitResult representing the entity hit
     */
    @Nullable
    protected EntityHitResult findHitEntity(Vec3 pStartVec, Vec3 pEndVec) {
        return this.dealtDamage ? null : super.findHitEntity(pStartVec, pEndVec);
    }

    /**
     * Called when the arrow hits an entity
     */
    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        // Remove the dealtDamage check here to allow normal collision
        Entity target = hitResult.getEntity();
        float damage = 8.0F;

        Entity owner = this.getOwner();
        DamageSource damageSource = this.damageSources().trident(this, owner != null ? owner : this);

        // Your damage calculation
        boolean wasHurt = target.hurt(damageSource, damage);

        if (wasHurt) {
            if (target.getType() == EntityType.ENDERMAN) {
                return;
            }

            if (target instanceof LivingEntity livingTarget) {
                // Apply knockback manually
                if (owner instanceof LivingEntity) {
                    int knockback = 0;
                    livingTarget.knockback(knockback * 0.5F,
                            owner.getX() - target.getX(),
                            owner.getZ() - target.getZ());
                }
                this.doPostHurtEffects(livingTarget);
            }

            // Set dealtDamage ONLY when damage was actually dealt
            this.dealtDamage = true;
        }

        // Adjust physics instead of completely killing momentum
        this.setDeltaMovement(this.getDeltaMovement().scale(-0.1D));
        this.playSound(SoundEvents.TRIDENT_HIT, 1.0F, 1.0F);
    }

    // Helper method to determine critical hits (like vanilla)
    private boolean isCritical() {
        return this.isInWater() && this.random.nextInt(10) == 0;
    }

    protected boolean tryPickup(Player pPlayer) {
        return super.tryPickup(pPlayer) || this.isNoPhysics() && this.ownedBy(pPlayer) && pPlayer.getInventory().add(
                this.getPickupItem());
    }

    /**
     * The sound made when an entity is hit by this projectile
     */
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }

    /**
     * Called by a player entity when they collide with an entity
     */
    public void playerTouch(Player pEntity) {
        if (this.ownedBy(pEntity) || this.getOwner() == null) {
            super.playerTouch(pEntity);
        }
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */

    public void tickDespawn() {
        int i = this.entityData.get(ID_LOYALTY);
        if (this.pickup != AbstractArrow.Pickup.ALLOWED || i == 0) {
            super.tickDespawn();
        }
    }

    protected float getWaterInertia() {
        return 0.99F;
    }

    public boolean shouldRender(double pX, double pY, double pZ) {
        return true;
    }
}
