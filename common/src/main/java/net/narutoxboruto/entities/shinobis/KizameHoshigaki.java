package net.narutoxboruto.entities.shinobis;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.narutoxboruto.items.ModItems;

public class KizameHoshigaki extends AbstractShinobiMob {
    public KizameHoshigaki(EntityType<? extends KizameHoshigaki> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setItemInHand(InteractionHand.MAIN_HAND, ModItems.SAMEHADA_ITEM.getDefaultInstance());

    }

    @Override
    protected ItemStack getWeapon() {
        return ModItems.SAMEHADA_ITEM.getDefaultInstance();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
    }

    public static AttributeSupplier setAttribute() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 300.0D)
                .add(Attributes.ARMOR, 25)
                .add(Attributes.MOVEMENT_SPEED, 0.27D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .build();
    }
}
