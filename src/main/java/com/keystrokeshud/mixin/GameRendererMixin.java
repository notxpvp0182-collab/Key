package com.keystrokeshud.mixin;

import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;

/**
 * GameRendererMixin — Placeholder for potential future render hooks.
 *
 * The HUD is currently rendered via Fabric's HudRenderCallback event,
 * which is cleaner and more compatible than a mixin on GameRenderer.
 * This class is kept as a registered mixin so the JSON config stays valid.
 */
@Mixin(GameRenderer.class)
public class GameRendererMixin {
    // No injections — kept for structural completeness.
}
