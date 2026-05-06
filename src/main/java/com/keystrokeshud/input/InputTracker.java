package com.keystrokeshud.input;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * InputTracker — Lightweight key-state and CPS tracking.
 *
 * CPS is calculated using a sliding one-second window of timestamps.
 * This approach is O(1) amortised and produces smooth, accurate CPS
 * values without expensive per-frame iteration over large arrays.
 *
 * Key-press state (W/A/S/D, LMB, RMB) is polled each frame from
 * Minecraft's own key-binding objects, so we never need to intercept
 * OS-level events — keeping the implementation simple and compatible.
 */
public class InputTracker {

    // ─── CPS window ──────────────────────────────────────────────────────────
    private static final long WINDOW_MS = 1000L;

    /** Timestamps (ms) of recent left-clicks within the last second. */
    private final Deque<Long> leftClicks  = new ArrayDeque<>();
    /** Timestamps (ms) of recent right-clicks within the last second. */
    private final Deque<Long> rightClicks = new ArrayDeque<>();

    // ─── Cached CPS values (updated every tick, not every frame) ─────────────
    private int cachedLCPS = 0;
    private int cachedRCPS = 0;

    // ─── Key states ──────────────────────────────────────────────────────────
    // True while the key is held down this frame.
    private boolean wPressed, aPressed, sPressed, dPressed;
    private boolean lmbPressed, rmbPressed;

    // Previous-frame states — used to detect rising edge for CPS counting.
    private boolean prevLmb, prevRmb;

    // ─── Animation interpolation values [0.0, 1.0] ───────────────────────────
    // These smoothly approach 1.0 when pressed and 0.0 when released.
    // Updated each frame with a fixed lerp step (no delta-time needed for this).
    public float animW, animA, animS, animD, animLMB, animRMB;

    private static final float ANIM_SPEED = 0.25f; // per-frame lerp step

    // ─── Public API ──────────────────────────────────────────────────────────

    /** Called every rendered frame by HudManager. */
    public void onRender(MinecraftClient client) {
        // Poll key states from Minecraft's own input system.
        wPressed   = client.options.forwardKey.isPressed();
        aPressed   = client.options.leftKey.isPressed();
        sPressed   = client.options.backKey.isPressed();
        dPressed   = client.options.rightKey.isPressed();
        lmbPressed = client.options.attackKey.isPressed();
        rmbPressed = client.options.useKey.isPressed();

        // Detect rising edge for CPS.
        long now = System.currentTimeMillis();
        if (lmbPressed && !prevLmb) leftClicks.addLast(now);
        if (rmbPressed && !prevRmb) rightClicks.addLast(now);
        prevLmb = lmbPressed;
        prevRmb = rmbPressed;

        // Advance animations.
        animW   = lerp(animW,   wPressed   ? 1f : 0f);
        animA   = lerp(animA,   aPressed   ? 1f : 0f);
        animS   = lerp(animS,   sPressed   ? 1f : 0f);
        animD   = lerp(animD,   dPressed   ? 1f : 0f);
        animLMB = lerp(animLMB, lmbPressed ? 1f : 0f);
        animRMB = lerp(animRMB, rmbPressed ? 1f : 0f);
    }

    /**
     * Called once per tick (20 Hz) to update cached CPS values.
     * Evicting expired entries here rather than every frame prevents
     * repeated traversal of the queue during rendering.
     */
    public void onTick() {
        long cutoff = System.currentTimeMillis() - WINDOW_MS;
        evict(leftClicks, cutoff);
        evict(rightClicks, cutoff);
        cachedLCPS = leftClicks.size();
        cachedRCPS = rightClicks.size();
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    public boolean isWPressed()   { return wPressed;   }
    public boolean isAPressed()   { return aPressed;   }
    public boolean isSPressed()   { return sPressed;   }
    public boolean isDPressed()   { return dPressed;   }
    public boolean isLmbPressed() { return lmbPressed; }
    public boolean isRmbPressed() { return rmbPressed; }

    /** Left-clicks per second (updated each tick). */
    public int getLeftCPS()  { return cachedLCPS; }
    /** Right-clicks per second (updated each tick). */
    public int getRightCPS() { return cachedRCPS; }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private static void evict(Deque<Long> queue, long cutoff) {
        while (!queue.isEmpty() && queue.peekFirst() < cutoff) {
            queue.pollFirst();
        }
    }

    /** Simple fixed-step lerp — avoids delta-time overhead for UI animations. */
    private static float lerp(float current, float target) {
        float delta = target - current;
        if (Math.abs(delta) < 0.005f) return target;
        return current + delta * ANIM_SPEED;
    }
}
