package net.narutoxboruto.entities.throwables;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class AbstractThrowableWeapon extends AbstractArrow {
    private int age;
    private float rotation;

    public AbstractThrowableWeapon(EntityType<? extends AbstractThrowableWeapon> type, Level world) {
        super(type, world);
    }


    /**
     * Provides the default item stack used for pickup. Never returns null.
     */
    @Override
    protected ItemStack getDefaultPickupItem() {
        ItemStack stack = getItem();
        return stack != null ? stack : ItemStack.EMPTY;
    }

    /**
     * Returns the actual item this projectile represents.
     */
    public abstract ItemStack getItem();

    @Override
    public void tick() {
        super.tick();
        if (this.age != -32768) {
            ++this.age;
        }
    }

    public int getAge() {
        return age;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Rotation", this.rotation);
        super.addAdditionalSaveData(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        this.rotation = tag.getFloat("Rotation");
        super.readAdditionalSaveData(tag);
    }

    protected boolean fumaSpin() {
        return true;
    }

    // to let the fuma keep spinning
    public float getFumaSpin(float pPartialTicks){
        if (!inGround && fumaSpin()) {
            this.rotation = (this.getAge() + pPartialTicks)/3;
        }
        if (fumaSpin()) {
            this.inGround = false;
        }
        return this.rotation;
    }

    protected boolean shouldSpin() {
        return true;
    }

    public float getSpin(float partialTicks) {
        return 0;
    }
}