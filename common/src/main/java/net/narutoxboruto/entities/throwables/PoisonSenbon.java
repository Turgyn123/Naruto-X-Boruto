package net.narutoxboruto.entities.throwables;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.narutoxboruto.main.Main;
import net.narutoxboruto.entities.ModEntities;
import net.narutoxboruto.items.ModItems;

public class PoisonSenbon extends AbstractThrowableWeapon {
    public PoisonSenbon(EntityType<? extends AbstractThrowableWeapon> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public PoisonSenbon(Level world, LivingEntity shooter) {
        this(ModEntities.POISON_SENBON, world);
        this.setOwner(shooter);
    }

    @Override
    public double getBaseDamage() {
        return 1.5;
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        if (pResult.getEntity() instanceof LivingEntity target) {
            if (!target.isInvulnerableTo(this.level().damageSources().mobProjectile(this, (LivingEntity) this.getOwner()))) {
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1), this); // Level 2 poison
            }
        }
        super.onHitEntity(pResult);
    }

    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            if (!this.inGround) {

            }
        }
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, ModItems.POISON_SENBON)));
    }
}
