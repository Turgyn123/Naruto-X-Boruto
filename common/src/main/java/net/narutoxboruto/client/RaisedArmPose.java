package net.narutoxboruto.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.narutoxboruto.entities.jutsus.ChakraSphereEntity;
import net.narutoxboruto.items.jutsus.ChargedSphereJutsu;

/** The right arm of a player held out to the side and up while a Rasenshuriken is charged above the hand. */
public final class RaisedArmPose {

    /** How far the right arm is turned out from hanging down, in radians: out to the side and up, 35 degrees above straight out. */
    public static final float ARM_TILT = 2.18F;

    private RaisedArmPose() {
    }

    /** Whether the right arm of {@code player} is held up to hold a Rasenshuriken. */
    public static boolean applies(Player player) {
        // The arm that is seen in first person is drawn on its own, and must not be turned
        Minecraft minecraft = Minecraft.getInstance();
        if (player == minecraft.player && minecraft.options.getCameraType().isFirstPerson()) return false;
        if (!holdsRasenshuriken(player.getMainHandItem()) && !holdsRasenshuriken(player.getOffhandItem())) return false;
        return ChakraSphereEntity.findHeldClient(player, ChakraSphereEntity.RASENSHURIKEN) != null;
    }

    private static boolean holdsRasenshuriken(ItemStack stack) {
        return stack.getItem() instanceof ChargedSphereJutsu jutsu && jutsu.sphereKind() == ChakraSphereEntity.RASENSHURIKEN;
    }
}
