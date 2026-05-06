package com.keystrokeshud.render;

import com.keystrokeshud.config.HudConfig;
import com.keystrokeshud.input.FpsTracker;
import com.keystrokeshud.input.InputTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.awt.Color;

/**
 * HudRenderer — Handles all on-screen drawing.
 *
 * Rendering pipeline per element:
 *   1. Determine background colour (idle/pressed blend + optional rainbow).
 *   2. Draw rounded-rectangle background.
 *   3. Draw border (if enabled).
 *   4. Draw label / value text, centred in the element.
 *
 * All heavy maths (colour blending, HSB→RGB) are done here with primitive
 * operations and no heap allocations on the hot path.
 */
public class HudRenderer {

    private final HudConfig    config;
    private final InputTracker input;
    private final FpsTracker   fps;

    public HudRenderer(HudConfig config, InputTracker input, FpsTracker fps) {
        this.config = config;
        this.input  = input;
        this.fps    = fps;
    }

    // ─── Main render call ─────────────────────────────────────────────────────

    /**
     * Render the full HUD.
     *
     * @param ctx         Minecraft draw context.
     * @param rainbowHue  Current rainbow hue [0.0, 1.0) — 0 if rainbow disabled.
     */
    public void render(DrawContext ctx, float rainbowHue) {
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;

        // ── Keystrokes ────────────────────────────────────────────────────────
        if (config.showW) drawKey(ctx, tr, "W",
                (int) config.xW, (int) config.yW,
                config.keyWidth, config.keyHeight,
                input.animW, config.colorTextW, config.borderColorW, rainbowHue, false, -1);

        if (config.showA) drawKey(ctx, tr, "A",
                (int) config.xA, (int) config.yA,
                config.keyWidth, config.keyHeight,
                input.animA, config.colorTextA, config.borderColorA, rainbowHue, false, -1);

        if (config.showS) drawKey(ctx, tr, "S",
                (int) config.xS, (int) config.yS,
                config.keyWidth, config.keyHeight,
                input.animS, config.colorTextS, config.borderColorS, rainbowHue, false, -1);

        if (config.showD) drawKey(ctx, tr, "D",
                (int) config.xD, (int) config.yD,
                config.keyWidth, config.keyHeight,
                input.animD, config.colorTextD, config.borderColorD, rainbowHue, false, -1);

        // ── Mouse buttons (LMB / RMB with optional CPS) ───────────────────────
        if (config.showLMB) {
            String label = config.showCPS ? "L " + input.getLeftCPS() : "LMB";
            drawKey(ctx, tr, label,
                    (int) config.xLMB, (int) config.yLMB,
                    config.lmbWidth, config.lmbHeight,
                    input.animLMB, config.colorTextLMB, config.borderColorLMB, rainbowHue, false, -1);
        }

        if (config.showRMB) {
            String label = config.showCPS ? "R " + input.getRightCPS() : "RMB";
            drawKey(ctx, tr, label,
                    (int) config.xRMB, (int) config.yRMB,
                    config.lmbWidth, config.lmbHeight,
                    input.animRMB, config.colorTextRMB, config.borderColorRMB, rainbowHue, false, -1);
        }

        // ── FPS counter ───────────────────────────────────────────────────────
        if (config.showFPS) {
            String label = "FPS " + fps.getFpsString();
            drawKey(ctx, tr, label,
                    (int) config.xFPS, (int) config.yFPS,
                    config.fpsWidth, config.fpsHeight,
                    0f, config.colorTextFPS, config.borderColorFPS, rainbowHue, true, fps.getFps());
        }
    }

    // ─── Element drawing ──────────────────────────────────────────────────────

    /**
     * Draw a single HUD element (key, mouse button, or FPS counter).
     *
     * @param ctx         Draw context.
     * @param tr          Text renderer.
     * @param label       Text to display.
     * @param x           Screen X (top-left).
     * @param y           Screen Y (top-left).
     * @param w           Width in pixels.
     * @param h           Height in pixels.
     * @param pressAnim   Press animation value [0.0, 1.0].
     * @param textColor   ARGB text colour.
     * @param borderColor ARGB border colour.
     * @param rainbowHue  Rainbow hue [0.0, 1.0).
     * @param isFps       True = use FPS colour gradient logic.
     * @param fpsValue    Raw FPS value (only used when isFps = true).
     */
    private void drawKey(DrawContext ctx, TextRenderer tr,
                         String label, int x, int y, int w, int h,
                         float pressAnim, int textColor, int borderColor,
                         float rainbowHue, boolean isFps, int fpsValue) {

        // ── Background colour ─────────────────────────────────────────────────
        int bgColor;
        if (config.rainbowMode && !config.performanceMode) {
            // Rainbow background replaces idle colour.
            int rgb = Color.HSBtoRGB(rainbowHue, 0.8f, 0.9f);
            int alpha = applyOpacity(0xFF000000 | (rgb & 0x00FFFFFF), config.backgroundOpacity);
            bgColor = blendARGB(alpha, config.colorPressed, pressAnim);
        } else if (isFps) {
            bgColor = fpsBackgroundColor(fpsValue, config.backgroundOpacity);
        } else {
            int idleWithOpacity = applyOpacity(config.colorIdle, config.backgroundOpacity);
            bgColor = blendARGB(idleWithOpacity, config.colorPressed, pressAnim);
        }

        // ── Draw background ───────────────────────────────────────────────────
        int r = config.cornerRadius;
        drawRoundedRect(ctx, x, y, w, h, r, bgColor);

        // ── Draw border ───────────────────────────────────────────────────────
        if (config.showBorders) {
            int bc = config.rainbowMode && !config.performanceMode
                    ? (Color.HSBtoRGB((rainbowHue + 0.5f) % 1f, 0.9f, 1f) | 0xFF000000)
                    : borderColor;
            int t = config.borderThickness;
            for (int i = 0; i < t; i++) {
                drawRoundedRectOutline(ctx, x - i, y - i, w + i * 2, h + i * 2, r + i, bc);
            }
        }

        // ── Draw label ────────────────────────────────────────────────────────
        int textX = x + (w - tr.getWidth(label)) / 2;
        int textY = y + (h - tr.fontHeight) / 2;
        ctx.drawText(tr, label, textX, textY, textColor, false);
    }

    // ─── Shape primitives ─────────────────────────────────────────────────────

    /**
     * Draws a filled rounded rectangle using overlapping axis-aligned rects.
     * This approach avoids fragment shaders and is fast on all hardware.
     */
    private void drawRoundedRect(DrawContext ctx, int x, int y, int w, int h, int r, int color) {
        r = Math.min(r, Math.min(w / 2, h / 2));
        // Centre fill
        ctx.fill(x + r, y, x + w - r, y + h, color);
        // Left / right strips
        ctx.fill(x, y + r, x + r, y + h - r, color);
        ctx.fill(x + w - r, y + r, x + w, y + h - r, color);
        // Four corners (quarter-circle approximation with small rects)
        drawCornerArc(ctx, x, y, r, 180, color);
        drawCornerArc(ctx, x + w - r, y, r, 270, color);
        drawCornerArc(ctx, x, y + h - r, r, 90, color);
        drawCornerArc(ctx, x + w - r, y + h - r, r, 0, color);
    }

    /**
     * Draws a quarter-circle corner using a pixel-perfect scan-line approach.
     * angle encodes which corner: 0=BR, 90=BL, 180=TL, 270=TR.
     */
    private void drawCornerArc(DrawContext ctx, int ox, int oy, int r, int angle, int color) {
        // We iterate over a square of size r×r and fill pixels inside the radius.
        for (int dy = 0; dy < r; dy++) {
            for (int dx = 0; dx < r; dx++) {
                // Centre of this pixel relative to the corner origin.
                float cx, cy;
                switch (angle) {
                    case 0  -> { cx = dx + 0.5f - r; cy = dy + 0.5f - r; } // BR
                    case 90 -> { cx = dx + 0.5f;     cy = dy + 0.5f - r; } // BL
                    case 180-> { cx = dx + 0.5f;     cy = dy + 0.5f;     } // TL
                    default -> { cx = dx + 0.5f - r; cy = dy + 0.5f;     } // TR
                }
                if (cx * cx + cy * cy <= (float) r * r) {
                    ctx.fill(ox + dx, oy + dy, ox + dx + 1, oy + dy + 1, color);
                }
            }
        }
    }

    /** Draws a 1-pixel outline of a rounded rectangle. */
    private void drawRoundedRectOutline(DrawContext ctx, int x, int y, int w, int h, int r, int color) {
        r = Math.min(r, Math.min(w / 2, h / 2));
        // Top & bottom edges
        ctx.fill(x + r, y, x + w - r, y + 1, color);
        ctx.fill(x + r, y + h - 1, x + w - r, y + h, color);
        // Left & right edges
        ctx.fill(x, y + r, x + 1, y + h - r, color);
        ctx.fill(x + w - 1, y + r, x + w, y + h - r, color);
        // Corners (simplified — single pixel diagonals)
        drawOutlineCorner(ctx, x, y, r, 180, color);
        drawOutlineCorner(ctx, x + w - r, y, r, 270, color);
        drawOutlineCorner(ctx, x, y + h - r, r, 90, color);
        drawOutlineCorner(ctx, x + w - r, y + h - r, r, 0, color);
    }

    /** Draws corner outline pixels for rounded rectangle border. */
    private void drawOutlineCorner(DrawContext ctx, int ox, int oy, int r, int angle, int color) {
        for (int dy = 0; dy < r; dy++) {
            for (int dx = 0; dx < r; dx++) {
                float cx, cy;
                switch (angle) {
                    case 0  -> { cx = dx + 0.5f - r; cy = dy + 0.5f - r; }
                    case 90 -> { cx = dx + 0.5f;     cy = dy + 0.5f - r; }
                    case 180-> { cx = dx + 0.5f;     cy = dy + 0.5f;     }
                    default -> { cx = dx + 0.5f - r; cy = dy + 0.5f;     }
                }
                float dist = cx * cx + cy * cy;
                float outer = (float) r * r;
                float inner = (float) (r - 1) * (r - 1);
                if (dist <= outer && dist >= inner) {
                    ctx.fill(ox + dx, oy + dy, ox + dx + 1, oy + dy + 1, color);
                }
            }
        }
    }

    // ─── Colour utilities ─────────────────────────────────────────────────────

    /**
     * Linearly interpolate between two ARGB colours.
     * t = 0.0 → a, t = 1.0 → b.
     */
    private static int blendARGB(int a, int b, float t) {
        if (t <= 0f) return a;
        if (t >= 1f) return b;
        int aa = (a >> 24) & 0xFF, ra = (a >> 16) & 0xFF, ga = (a >> 8) & 0xFF, ba2 = a & 0xFF;
        int ab = (b >> 24) & 0xFF, rb = (b >> 16) & 0xFF, gb = (b >> 8) & 0xFF, bb2 = b & 0xFF;
        int ao = aa + (int)((ab - aa) * t);
        int ro = ra + (int)((rb - ra) * t);
        int go = ga + (int)((gb - ga) * t);
        int bo = ba2 + (int)((bb2 - ba2) * t);
        return (ao << 24) | (ro << 16) | (go << 8) | bo;
    }

    /**
     * Apply an opacity multiplier [0.0, 1.0] to the alpha channel of an ARGB colour.
     */
    private static int applyOpacity(int argb, float opacity) {
        int originalAlpha = (argb >> 24) & 0xFF;
        int newAlpha      = (int)(originalAlpha * opacity);
        return (argb & 0x00FFFFFF) | (newAlpha << 24);
    }

    /**
     * Returns a background colour for the FPS counter that shifts
     * from green (high) through yellow to red (low) based on FPS.
     */
    private int fpsBackgroundColor(int fpsValue, float opacity) {
        // 60+ = green, 30 = yellow, 0 = red.
        float t = Math.min(1f, fpsValue / 60f);
        int r = (int)(255 * (1f - t));
        int g = (int)(255 * t);
        int rgb = (r << 16) | (g << 8);
        int alpha = (int)(0x66 * opacity);
        return (alpha << 24) | rgb;
    }
}
