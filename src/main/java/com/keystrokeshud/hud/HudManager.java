package com.keystrokeshud.hud;

import com.keystrokeshud.config.HudConfig;
import com.keystrokeshud.input.FpsTracker;
import com.keystrokeshud.input.InputTracker;
import com.keystrokeshud.render.HudRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/**
 * HudManager — Central coordinator between config, input, and rendering.
 *
 * Responsibilities:
 *  - Forward tick-rate events to InputTracker and FpsTracker.
 *  - Gate rendering behind the in-game check (no HUD on screens / loading).
 *  - Provide a clean hand-off to HudRenderer each frame.
 */
public class HudManager {

    private final HudConfig    config;
    private final InputTracker input;
    private final FpsTracker   fps;
    private final HudRenderer  renderer;

    /** Rainbow hue accumulator [0.0, 1.0). Advanced each rendered frame. */
    private float rainbowHue = 0f;

    public HudManager(HudConfig config, InputTracker input, FpsTracker fps) {
        this.config   = config;
        this.input    = input;
        this.fps      = fps;
        this.renderer = new HudRenderer(config, input, fps);
    }

    // ─── Tick (20 Hz) ────────────────────────────────────────────────────────

    /**
     * Called at the end of every client tick.
     * Cheap operations only — no rendering here.
     */
    public void onTick(MinecraftClient client) {
        // Skip during screen transitions / loading.
        if (client.world == null) return;

        input.onTick();
        fps.onTick(client);
    }

    // ─── Frame render ─────────────────────────────────────────────────────────

    /**
     * Called every rendered frame via HudRenderCallback.
     *
     * @param drawContext Fabric's draw context for the current frame.
     * @param tickDelta   Partial tick interpolation value [0.0, 1.0).
     */
    public void render(DrawContext drawContext, net.minecraft.client.render.RenderTickCounter tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();

        // Only render while actually in a world with no open screen
        // (chat/inventory open would obscure the HUD anyway).
        if (client.world == null) return;
        // Allow rendering even with a screen open so config screen can show preview.
        // The config screen itself filters out clicks, so this is safe.

        // Update input animations every frame for smoothness.
        input.onRender(client);

        // Advance rainbow hue if enabled and not in performance mode.
        if (config.rainbowMode && !config.performanceMode) {
            // Use real delta time to make rainbow speed frame-rate independent.
            rainbowHue = (rainbowHue + config.rainbowSpeed * 0.005f) % 1.0f;
        }

        renderer.render(drawContext, rainbowHue);
    }

    // ─── Accessors used by config screen ─────────────────────────────────────

    public HudConfig    getConfig()   { return config;   }
    public InputTracker getInput()    { return input;    }
    public FpsTracker   getFps()      { return fps;      }
    public HudRenderer  getRenderer() { return renderer; }
}
