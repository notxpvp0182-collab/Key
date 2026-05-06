package com.keystrokeshud.render;

import com.keystrokeshud.config.HudConfig;
import com.keystrokeshud.input.FpsTracker;
import com.keystrokeshud.input.InputTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.awt.Color;

public class HudRenderer {

    private final HudConfig config;
    private final InputTracker input;
    private final FpsTracker fps;

    public HudRenderer(HudConfig config, InputTracker input, FpsTracker fps) {
        this.config = config;
        this.input = input;
        this.fps = fps;
    }

    public void render(DrawContext ctx, float rainbowHue) {
        MinecraftClient mc = MinecraftClient.getInstance();
        TextRenderer tr = mc.textRenderer;

        if (config.showW)
            drawKey(ctx, tr, "W", (int)config.xW, (int)config.yW,
                    config.keyWidth, config.keyHeight,
                    input.animW, config.colorTextW, config.borderColorW, rainbowHue);

        if (config.showA)
            drawKey(ctx, tr, "A", (int)config.xA, (int)config.yA,
                    config.keyWidth, config.keyHeight,
                    input.animA, config.colorTextA, config.borderColorA, rainbowHue);

        if (config.showS)
            drawKey(ctx, tr, "S", (int)config.xS, (int)config.yS,
                    config.keyWidth, config.keyHeight,
                    input.animS, config.colorTextS, config.borderColorS, rainbowHue);

        if (config.showD)
            drawKey(ctx, tr, "D", (int)config.xD, (int)config.yD,
                    config.keyWidth, config.keyHeight,
                    input.animD, config.colorTextD, config.borderColorD, rainbowHue);

        if (config.showLMB) {
            String label = config.showCPS ? "L " + input.getLeftCPS() : "LMB";
            drawKey(ctx, tr, label, (int)config.xLMB, (int)config.yLMB,
                    config.lmbWidth, config.lmbHeight,
                    input.animLMB, config.colorTextLMB, config.borderColorLMB, rainbowHue);
        }

        if (config.showRMB) {
            String label = config.showCPS ? "R " + input.getRightCPS() : "RMB";
            drawKey(ctx, tr, label, (int)config.xRMB, (int)config.yRMB,
                    config.lmbWidth, config.lmbHeight,
                    input.animRMB, config.colorTextRMB, config.borderColorRMB, rainbowHue);
        }

        if (config.showFPS) {
            String label = "FPS " + fps.getFpsString();
            drawFps(ctx, tr, label, (int)config.xFPS, (int)config.yFPS,
                    config.fpsWidth, config.fpsHeight,
                    config.colorTextFPS, config.borderColorFPS, rainbowHue, fps.getFps());
        }
    }

    private void drawKey(DrawContext ctx, TextRenderer tr,
                         String label, int x, int y, int w, int h,
                         float pressAnim, int textColor, int borderColor, float rainbowHue) {

        int bgColor;
        if (config.rainbowMode && !config.performanceMode) {
            int rgb = Color.HSBtoRGB(rainbowHue, 0.8f, 0.9f);
            int alpha = applyOpacity(0xFF000000 | (rgb & 0x00FFFFFF), config.backgroundOpacity);
            bgColor = blendARGB(alpha, config.colorPressed, pressAnim);
        } else {
            int idleWithOpacity = applyOpacity(config.colorIdle, config.backgroundOpacity);
            bgColor = blendARGB(idleWithOpacity, config.colorPressed, pressAnim);
        }

        int r = config.cornerRadius;
        drawRoundedRect(ctx, x, y, w, h, r, bgColor);

        if (config.showBorders) {
            int bc = (config.rainbowMode && !config.performanceMode)
                    ? (Color.HSBtoRGB((rainbowHue + 0.5f) % 1f, 0.9f, 1f) | 0xFF000000)
                    : borderColor;
            for (int i = 0; i < config.borderThickness; i++) {
                drawRoundedRectOutline(ctx, x - i, y - i, w + i * 2, h + i * 2, r + i, bc);
            }
        }

        int textX = x + (w - tr.getWidth(label)) / 2;
        int textY = y + (h - tr.fontHeight) / 2;
        ctx.drawText(tr, label, textX, textY, textColor, false);
    }

    private void drawFps(DrawContext ctx, TextRenderer tr,
                         String label, int x, int y, int w, int h,
                         int textColor, int borderColor, float rainbowHue, int fpsValue) {

        int bgColor = fpsBackgroundColor(fpsValue, config.backgroundOpacity);
        int r = config.cornerRadius;
        drawRoundedRect(ctx, x, y, w, h, r, bgColor);

        if (config.showBorders) {
            int bc = (config.rainbowMode && !config.performanceMode)
                    ? (Color.HSBtoRGB((rainbowHue + 0.5f) % 1f, 0.9f, 1f) | 0xFF000000)
                    : borderColor;
            drawRoundedRectOutline(ctx, x, y, w, h, r, bc);
        }

        int textX = x + (w - tr.getWidth(label)) / 2;
        int textY = y + (h - tr.fontHeight) / 2;
        ctx.drawText(tr, label, textX, textY, textColor, false);
    }

    private void drawRoundedRect(DrawContext ctx, int x, int y, int w, int h, int r, int color) {
        r = Math.min(r, Math.min(w / 2, h / 2));
        ctx.fill(x + r, y, x + w - r, y + h, color);
        ctx.fill(x, y + r, x + r, y + h - r, color);
        ctx.fill(x + w - r, y + r, x + w, y + h - r, color);
        drawCornerArc(ctx, x, y, r, 180, color);
        drawCornerArc(ctx, x + w - r, y, r, 270, color);
        drawCornerArc(ctx, x, y + h - r, r, 90, color);
        drawCornerArc(ctx, x + w - r, y + h - r, r, 0, color);
    }

    private void drawCornerArc(DrawContext ctx, int ox, int oy, int r, int angle, int color) {
        for (int dy = 0; dy < r; dy++) {
            for (int dx = 0; dx < r; dx++) {
                float cx, cy;
                switch (angle) {
                    case 0   -> { cx = dx + 0.5f - r; cy = dy + 0.5f - r; }
                    case 90  -> { cx = dx + 0.5f;     cy = dy + 0.5f - r; }
                    case 180 -> { cx = dx + 0.5f;     cy = dy + 0.5f;     }
                    default  -> { cx = dx + 0.5f - r; cy = dy + 0.5f;     }
                }
                if (cx * cx + cy * cy <= (float) r * r) {
                    ctx.fill(ox + dx, oy + dy, ox + dx + 1, oy + dy + 1, color);
                }
            }
        }
    }

    private void drawRoundedRectOutline(DrawContext ctx, int x, int y, int w, int h, int r, int color) {
        r = Math.min(r, Math.min(w / 2, h / 2));
        ctx.fill(x + r, y, x + w - r, y + 1, color);
        ctx.fill(x + r, y + h - 1, x + w - r, y + h, color);
        ctx.fill(x, y + r, x + 1, y + h - r, color);
        ctx.fill(x + w - 1, y + r, x + w, y + h - r, color);
    }

    private static int blendARGB(int a, int b, float t) {
        if (t <= 0f) return a;
        if (t >= 1f) return b;
        int aa = (a >> 24) & 0xFF, ra = (a >> 16) & 0xFF, ga = (a >> 8) & 0xFF, ba2 = a & 0xFF;
        int ab = (b >> 24) & 0xFF, rb = (b >> 16) & 0xFF, gb = (b >> 8) & 0xFF, bb2 = b & 0xFF;
        return ((aa + (int)((ab - aa) * t)) << 24)
             | ((ra + (int)((rb - ra) * t)) << 16)
             | ((ga + (int)((gb - ga) * t)) << 8)
             |  (ba2 + (int)((bb2 - ba2) * t));
    }

    private static int applyOpacity(int argb, float opacity) {
        int alpha = (int)(((argb >> 24) & 0xFF) * opacity);
        return (argb & 0x00FFFFFF) | (alpha << 24);
    }

    private static int fpsBackgroundColor(int fpsValue, float opacity) {
        float t = Math.min(1f, fpsValue / 60f);
        int r = (int)(255 * (1f - t));
        int g = (int)(255 * t);
        int alpha = (int)(0x66 * opacity);
        return (alpha << 24) | (r << 16) | (g << 8);
    }
                    }
