package com.keystrokeshud.input;

import net.minecraft.client.MinecraftClient;

/**
 * FpsTracker — Reads the game's native FPS counter.
 *
 * Minecraft's {@code MinecraftClient#getCurrentFps()} is already maintained
 * by the engine; we simply cache the value once per tick so the renderer
 * never needs to call it on the hot path.
 *
 * The value is updated at tick rate (20 Hz) to avoid string allocations
 * every rendered frame.
 */
public class FpsTracker {

    private int cachedFps = 0;
    private String cachedFpsStr = "0";

    /** Call once per tick. */
    public void onTick(MinecraftClient client) {
        int fps = client.getCurrentFps();
        if (fps != cachedFps) {
            cachedFps    = fps;
            cachedFpsStr = String.valueOf(fps); // pre-build string once
        }
    }

    /** Current FPS as an integer. */
    public int getFps() { return cachedFps; }

    /** Current FPS as a pre-built string (avoids Integer.toString allocation per frame). */
    public String getFpsString() { return cachedFpsStr; }
}
