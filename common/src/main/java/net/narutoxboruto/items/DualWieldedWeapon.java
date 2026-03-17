package net.narutoxboruto.items;

import net.minecraft.world.item.Item;

public interface DualWieldedWeapon {
    Item getMainHandWeapon();

    Item getOffHandWeapon();

    Item getUnitedWeapon();

    boolean isMainHand();

    boolean isUnited();
}
