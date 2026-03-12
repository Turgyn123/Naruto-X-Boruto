package net.narutoxboruto.entities.throwables;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.narutoxboruto.main.Main;
import net.narutoxboruto.entities.ModEntities;
import net.narutoxboruto.items.ModItems;

public class Shuriken extends AbstractThrowableWeapon {

    public Shuriken(EntityType<? extends AbstractThrowableWeapon> entityType, Level level) {
        super(entityType, level);
    }

    public Shuriken(Level level, LivingEntity shooter) {
        this(ModEntities.SHURIKEN, level);
        this.setOwner(shooter);
    }

    @Override
    public double getBaseDamage() {
        return 3;
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(Main.MOD_ID, ModItems.SHURIKEN)));
    }

    @Override
    protected boolean shouldSpin() {
        return true;
    }
}