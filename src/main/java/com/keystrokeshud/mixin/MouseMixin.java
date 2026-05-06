package com.keystrokeshud.mixin;

import com.keystrokeshud.KeystrokesHudClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MouseMixin — Optional low-level click interception.
 *
 * The primary CPS tracking is already handled via key-state polling in
 * InputTracker (rising-edge detection on isPressed()). This mixin is
 * provided as a secondary hook for absolute accuracy if needed, but is
 * not strictly required for correct operation.
 *
 * Currently this mixin is a no-op placeholder — it exists so the structure
 * is in place for future extensions (e.g., tracking non-binding clicks).
 */
@Mixin(Mouse.class)
public class MouseMixin {

    // No injection active by default — InputTracker handles CPS via polling.
    // Add @Inject here if raw GLFW mouse button events are needed in the future.
}
