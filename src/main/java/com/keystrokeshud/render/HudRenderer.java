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
                    config.wW, config.hW, config.rW,
                    input.animW, config.colorTextW, config.borderColorW, rainbowHue);

        if (config.showA)
            drawKey(ctx, tr, "A", (int)config.xA, (int)config.yA,
                    config.wA, config.hA, config.rA,
                    input.animA, config.colorTextA, config.borderColorA, rainbowHue);

        if (config.showS)
            drawKey(ctx, tr, "S", (int)config.xS, (int)config.yS,
                    config.wS, config.hS, config.rS,
                    input.animS, config.colorTextS, config.borderColorS, rainbowHue);

        if (config.showD)
            drawKey(ctx, tr, "D", (int)config.xD, (int)config.yD,
                    config.wD, config.hD, config.rD,
                    input.animD, config.colorTextD, config.borderColorD, rainbowHue);

        if (config.showLMB) {
            String label = config.showCPS ? "L " + input.getLeftCPS() : "LMB";
            drawKey(ctx, tr, label, (int)config.xLMB, (int)config.yLMB,
                    config.wLMB, config.hLMB, config.rLMB,
                    input.animLMB, config.colorTextLMB, config.borderColorLMB, rainbowHue);
        }

        if (config.showRMB) {
            String label = config.showCPS ? "R " + input.getRightCPS() : "RMB";
            drawKey(ctx, tr, label, (int)config.xRMB, (int)config.yRMB,
                    config.wRMB, config.hRMB, config.rRMB,
                    input.animRMB, config.colorTextRMB, config.borderColorRMB, rainbowHue);
        }

        if (config.showFPS) {
            String label = "FPS " + fps.getFpsString();
            drawKey(ctx, tr, label, (int)config.xFPS, (int)config.yFPS,
                    config.wFPS, config.hFPS, config.rFPS,
                    0f, config.colorTextFPS, config.borderColorFPS, rainbowHue);
        }
    }

    private void drawKey(DrawContext ctx, TextRenderer tr,
                         String label, int x, int y, int w, int h, int radius,
                         float pressAnim, int textColor, int borderColor, float rainbowHue) {

        // Background color
        int bgColor;
        if (config.rainbowMode && !config.performanceMode) {
            int rgb = Color.HSBtoRGB(rainbowHue, 0.8f, 0.9f);
            bgColor = blendARGB(applyOpacity(0xFF000000|(rgb&0xFFFFFF), config.backgroundOpacity),
                    config.colorPressed, pressAnim);
        } else {
            bgColor = blendARGB(applyOpacity(config.colorIdle, config.backgroundOpacity),
                    config.colorPressed, pressAnim);
        }

        // Draw background
        drawRoundedRect(ctx, x, y, w, h, radius, bgColor);

        // Draw corner-only border
        if (config.showBorders) {
            int bc = (config.rainbowMode && !config.performanceMode)
                    ? (Color.HSBtoRGB((rainbowHue+0.5f)%1f, 0.9f, 1f)|0xFF000000)
                    : borderColor;
            drawCornerBorder(ctx, x, y, w, h, radius, bc);
        }

        // Draw text
        int textX = x + (w - tr.getWidth(label)) / 2;
        int textY = y + (h - tr.fontHeight) / 2;
        ctx.drawText(tr, label, textX, textY, textColor, false);
    }

    // ── Corner-only border ────────────────────────────────────────────────────
    // শুধু চার কোণায় ছোট border থাকবে — পুরো border না
    private void drawCornerBorder(DrawContext ctx, int x, int y, int w, int h, int r, int color) {
        int cs = Math.max(4, Math.min(r + 3, Math.min(w, h) / 3)); // corner segment length

        // Top-left
        ctx.fill(x, y, x + cs, y + 1, color);
        ctx.fill(x, y, x + 1, y + cs, color);

        // Top-right
        ctx.fill(x + w - cs, y, x + w, y + 1, color);
        ctx.fill(x + w - 1, y, x + w, y + cs, color);

        // Bottom-left
        ctx.fill(x, y + h - 1, x + cs, y + h, color);
        ctx.fill(x, y + h - cs, x + 1, y + h, color);

        // Bottom-right
        ctx.fill(x + w - cs, y + h - 1, x + w, y + h, color);
        ctx.fill(x + w - 1, y + h - cs, x + w, y + h, color);
    }

    // ── Rounded rect ──────────────────────────────────────────────────────────
    private void drawRoundedRect(DrawContext ctx, int x, int y, int w, int h, int r, int color) {
        r = Math.min(r, Math.min(w/2, h/2));
        ctx.fill(x+r, y, x+w-r, y+h, color);
        ctx.fill(x, y+r, x+r, y+h-r, color);
        ctx.fill(x+w-r, y+r, x+w, y+h-r, color);
        drawCornerArc(ctx, x, y, r, 180, color);
        drawCornerArc(ctx, x+w-r, y, r, 270, color);
        drawCornerArc(ctx, x, y+h-r, r, 90, color);
        drawCornerArc(ctx, x+w-r, y+h-r, r, 0, color);
    }

    private void drawCornerArc(DrawContext ctx, int ox, int oy, int r, int angle, int color) {
        for (int dy=0; dy<r; dy++) {
            for (int dx=0; dx<r; dx++) {
                float cx, cy;
                switch (angle) {
                    case 0   -> { cx=dx+0.5f-r; cy=dy+0.5f-r; }
                    case 90  -> { cx=dx+0.5f;   cy=dy+0.5f-r; }
                    case 180 -> { cx=dx+0.5f;   cy=dy+0.5f;   }
                    default  -> { cx=dx+0.5f-r; cy=dy+0.5f;   }
                }
                if (cx*cx+cy*cy <= (float)r*r)
                    ctx.fill(ox+dx, oy+dy, ox+dx+1, oy+dy+1, color);
            }
        }
    }

    // ── Color utils ───────────────────────────────────────────────────────────
    private static int blendARGB(int a, int b, float t) {
        if (t<=0f) return a; if (t>=1f) return b;
        int aa=(a>>24)&0xFF, ra=(a>>16)&0xFF, ga=(a>>8)&0xFF, ba=a&0xFF;
        int ab=(b>>24)&0xFF, rb=(b>>16)&0xFF, gb=(b>>8)&0xFF, bb=b&0xFF;
        return ((aa+(int)((ab-aa)*t))<<24)|((ra+(int)((rb-ra)*t))<<16)
              |((ga+(int)((gb-ga)*t))<<8)|(ba+(int)((bb-ba)*t));
    }

    private static int applyOpacity(int argb, float opacity) {
        return (argb&0x00FFFFFF)|((int)(((argb>>24)&0xFF)*opacity)<<24);
    }
                    }
