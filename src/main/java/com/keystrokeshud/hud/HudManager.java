package com.keystrokeshud.hud;

import com.keystrokeshud.config.HudConfig;
import com.keystrokeshud.input.FpsTracker;
import com.keystrokeshud.input.InputTracker;
import com.keystrokeshud.render.HudRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public class HudManager {

    private final HudConfig config;
    private final InputTracker input;
    private final FpsTracker fps;
    private final HudRenderer renderer;

    private float rainbowHue = 0f;

    public HudManager(HudConfig config, InputTracker input, FpsTracker fps) {
        this.config = config;
        this.input = input;
        this.fps = fps;
        this.renderer = new HudRenderer(config, input, fps);
    }

    public void onTick(MinecraftClient client) {
        if (client.world == null) return;
        input.onTick();
        fps.onTick(client);
    }

    public void render(DrawContext drawContext, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        input.onRender(client);

        if (config.rainbowMode && !config.performanceMode) {
            rainbowHue = (rainbowHue + config.rainbowSpeed * 0.005f) % 1.0f;
        }

        renderer.render(drawContext, rainbowHue);
    }

    public HudConfig getConfig() { return config; }
    public InputTracker getInput() { return input; }
    public FpsTracker getFps() { return fps; }
    public HudRenderer getRenderer() { return renderer; }
}
