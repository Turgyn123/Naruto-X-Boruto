package net.narutoxboruto.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.narutoxboruto.client.PlayerData;
import net.narutoxboruto.items.swords.Kiba;
import net.narutoxboruto.main.Main;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Renders lightning effects on the Kiba sword model and Lightning Chakra Mode cloak.
 * For the sword: renders the model again with a glowing overlay that pulses.
 * For the cloak: uses lightning arcs around the player body.
 */
@EventBusSubscriber(modid = Main.MOD_ID, value = Dist.CLIENT)
public class KibaLightningOverlay {
    
    private static final Random random = new Random();
    
    // Lightning colors - RGB format: 0x00RRGGBB (alpha handled separately in render)
    // Bright electric blue for both sword and cloak
    private static final int KIBA_LIGHTNING_COLOR = 0x40B0FF; // Bright electric blue for sword
    private static final int CLOAK_LIGHTNING_COLOR = 0x60C0FF; // Bright electric blue for cloak
    
    // Arc generation timing - separate for sword and cloak
    private static long lastSwordArcGenTime = 0;
    private static long lastCloakArcGenTime = 0;
    private static final long SWORD_ARC_INTERVAL_MS = 50;
    private static final long CLOAK_ARC_INTERVAL_MS = 160; // Reduced frequency (was 80)
    private static final long CLOAK_BURST_ARC_INTERVAL_MS = 20; // Fast interval during activation burst
    
    // Store current arcs for rendering - separate lists
    private static final int MAX_SWORD_ARCS = 15;
    private static final int MAX_CLOAK_ARCS = 40; // Increased for better body coverage
    private static final int BURST_MAX_CLOAK_ARCS = 80; // More arcs during burst
    
    // NOTE: Kiba sword lightning is now handled by MixinItemRenderer
    // which renders the glow directly on the model using the same UV coordinates.
    // The onRenderHand method below is disabled.
    
    /**
     * First-person hand rendering - DISABLED, now handled by MixinItemRenderer
     * The mixin renders the model again with a glowing overlay using same UVs.
     */
    // @SubscribeEvent - DISABLED
    public static void onRenderHand_DISABLED(RenderHandEvent event) {
        // Now handled by MixinItemRenderer for perfect UV-locked rendering
    }
    
    /**
     * First-person cloak rendering - uses RenderLevelStageEvent since RenderPlayerEvent doesn't fire in first-person
     * Renders the lightning cloak around the player in world space
     */
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        // Only render after translucent stage for proper blending
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;
        
        // Only render in first-person (third-person handled by RenderPlayerEvent)
        if (!mc.options.getCameraType().isFirstPerson()) return;
        
        // Check if Lightning Chakra Mode is active
        if (!PlayerData.isLightningChakraModeActive()) return;
        
        PoseStack poseStack = event.getPoseStack();
        
        // Get player position interpolated for smooth rendering
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        Vec3 playerPos = player.getPosition(partialTick);
        Vec3 cameraPos = event.getCamera().getPosition();
        
        // Translate to player position relative to camera
        poseStack.pushPose();
        poseStack.translate(
            playerPos.x - cameraPos.x,
            playerPos.y - cameraPos.y,
            playerPos.z - cameraPos.z
        );
        
        // Render cloak at player position - use first-person variant (lower body only)
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        CloakLightningRenderer.renderCloakLightningFirstPerson(poseStack, bufferSource, player);
        bufferSource.endBatch(RenderType.lightning());
        
        poseStack.popPose();
    }
    
    /**
     * Third-person player rendering - renders lightning on sword AND cloak
     */
    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Post event) {
        Player player = event.getEntity();
        Minecraft mc = Minecraft.getInstance();
        
        // Only render effects for the local player (since we use PlayerData which is local)
        // For other players, we would need a different sync mechanism
        if (player != mc.player) return;
        
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource bufferSource = event.getMultiBufferSource();
        
        // === LIGHTNING CHAKRA MODE CLOAK ===
        if (PlayerData.isLightningChakraModeActive()) {
            poseStack.pushPose();
            // Cloak renders at player origin - the pose stack is already transformed
            CloakLightningRenderer.renderCloakLightning(poseStack, bufferSource, player);
            poseStack.popPose();
        }
        
        // NOTE: Kiba sword lightning is now handled by MixinItemRenderer
        // which renders the glow directly on the model using the same UV coordinates.
    }
    
    // ==================== SWORD LIGHTNING OVERLAY (DISABLED) ====================
    // Now handled by MixinItemRenderer for perfect UV-locked rendering
    // All old sword lightning methods have been removed - see git history for reference
}
