package net.narutoxboruto.dojutsu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.narutoxboruto.capabilities.info.Dojutsu;
import net.narutoxboruto.main.platform.Services;

/**
 * Byakugan client-side vision: outlines all entities within {@link #RADIUS} blocks
 * for the local player only when their Byakugan is equipped and activated.
 */
public final class ByakuganVision {

    public static final String DOJUTSU_ID = "byakugan";
    /** Highlight radius in blocks. TODO: expose as a runtime config option. */
    public static final double RADIUS = 100.0D;
    private static final double RADIUS_SQ = RADIUS * RADIUS;

    private ByakuganVision() {}

    /** True when the given player has Byakugan equipped (either eye) AND eyes are currently visible/active. */
    public static boolean isActiveFor(Player player) {
        if (player == null) return false;
        Dojutsu dojutsu = Services.PLATFORM.getDojutsu(player);
        if (dojutsu == null || !dojutsu.areEyesVisible()) return false;
        return DOJUTSU_ID.equals(dojutsu.getLeftEye()) || DOJUTSU_ID.equals(dojutsu.getRightEye());
    }

    /**
     * Client-side check: should the given entity be glowing because the local player
     * has Byakugan active and the entity is within range? Excludes the local player itself.
     */
    public static boolean shouldHighlight(Entity entity) {
        if (entity == null) return false;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer self = mc.player;
        if (self == null || entity == self) return false;
        if (!isActiveFor(self)) return false;
        return entity.distanceToSqr(self) <= RADIUS_SQ;
    }
}
