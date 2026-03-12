package net.narutoxboruto.items.throwables;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.narutoxboruto.entities.throwables.AbstractThrowableWeapon;
import net.narutoxboruto.entities.throwables.ThrownFumaShuriken;

public class FumaShurikenItem extends ThrowableWeaponItem {
    public FumaShurikenItem(Properties pProperties, String fumaShuriken) {
        super(pProperties.stacksTo(1), "fuma");
    }

    @Override
    public AbstractThrowableWeapon getProjectile(Level pLevel, LivingEntity pShooter, ItemStack stack) {
        return new ThrownFumaShuriken(pLevel, pShooter, stack);
    }
}
