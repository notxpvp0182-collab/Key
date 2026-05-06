package com.keystrokeshud.screen;

import com.keystrokeshud.KeystrokesHudClient;
import com.keystrokeshud.config.HudConfig;
import com.keystrokeshud.hud.HudManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * HudConfigScreen — The mod's main settings UI.
 *
 * Layout overview
 * ───────────────
 *  Left panel  (230 px) — module toggles, sliders for opacity / speed
 *  Right panel (rest)   — live HUD preview + drag-to-reposition handles
 *
 * Drag system
 * ───────────
 * Each HUD element has a "handle" — a semi-transparent rectangle drawn in
 * this screen that the user can drag.  When dragging we update the config
 * position and allow the main HudRenderer to re-draw the element at its
 * new location in real time.
 *
 * The screen extends Screen so it participates in Minecraft's normal input
 * routing and respects accessibility settings automatically.
 */
public class HudConfigScreen extends Screen {

    private static final int PANEL_WIDTH = 230;
    private static final int HEADER_H    = 24;
    private static final int HANDLE_SIZE = 8;  // px — grab handle corner indicator

    private final Screen    parent;
    private final HudConfig config;
    private final HudManager manager;

    // ─── Drag state ───────────────────────────────────────────────────────────
    private enum DragTarget { NONE, W, A, S, D, LMB, RMB, FPS }
    private DragTarget dragging  = DragTarget.NONE;
    private float      dragOffX, dragOffY;

    // ─── Scroll in left panel ─────────────────────────────────────────────────
    private int scrollY = 0;

    public HudConfigScreen(Screen parent) {
        super(Text.literal("KeystrokesHUD — Settings"));
        this.parent  = parent;
        this.config  = KeystrokesHudClient.config;
        this.manager = KeystrokesHudClient.hudManager;
    }

    // ─── Screen lifecycle ─────────────────────────────────────────────────────

    @Override
    protected void init() {
        int panelX = 8;
        int y      = HEADER_H + 6 - scrollY;
        int bw     = PANEL_WIDTH - 16;
        int bh     = 18;
        int gap    = 3;

        // ── Feature toggles ──────────────────────────────────────────────────
        addToggle(panelX, y, bw, bh, "Show W Key",    config.showW,   v -> { config.showW   = v; save(); }); y += bh + gap;
        addToggle(panelX, y, bw, bh, "Show A Key",    config.showA,   v -> { config.showA   = v; save(); }); y += bh + gap;
        addToggle(panelX, y, bw, bh, "Show S Key",    config.showS,   v -> { config.showS   = v; save(); }); y += bh + gap;
        addToggle(panelX, y, bw, bh, "Show D Key",    config.showD,   v -> { config.showD   = v; save(); }); y += bh + gap;
        addToggle(panelX, y, bw, bh, "Show LMB",      config.showLMB, v -> { config.showLMB = v; save(); }); y += bh + gap;
        addToggle(panelX, y, bw, bh, "Show RMB",      config.showRMB, v -> { config.showRMB = v; save(); }); y += bh + gap;
        addToggle(panelX, y, bw, bh, "Show FPS",      config.showFPS, v -> { config.showFPS = v; save(); }); y += bh + gap;
        addToggle(panelX, y, bw, bh, "Show CPS",      config.showCPS, v -> { config.showCPS = v; save(); }); y += bh + gap;
        addToggle(panelX, y, bw, bh, "Show Borders",  config.showBorders, v -> { config.showBorders = v; save(); }); y += bh + gap;
        addToggle(panelX, y, bw, bh, "Rainbow Mode",  config.rainbowMode,  v -> { config.rainbowMode  = v; save(); }); y += bh + gap;
        addToggle(panelX, y, bw, bh, "Lock Positions",config.lockPositions,v -> { config.lockPositions = v; save(); }); y += bh + gap;
        addToggle(panelX, y, bw, bh, "Snap to Grid",  config.snapToGrid,   v -> { config.snapToGrid   = v; save(); }); y += bh + gap;
        addToggle(panelX, y, bw, bh, "Performance Mode", config.performanceMode, v -> { config.performanceMode = v; save(); }); y += bh + gap;

        // Separator
        y += 4;

        // ── Action buttons ────────────────────────────────────────────────────
        addDrawableChild(ButtonWidget.builder(
                Text.literal("⟳ Reset Layout"),
                btn -> { config.resetLayout(); rebuildWidgets(); })
                .dimensions(panelX, y, bw, bh).build()); y += bh + gap;

        addDrawableChild(ButtonWidget.builder(
                Text.literal("Open Color Editor…"),
                btn -> client.setScreen(new ColorEditorScreen(this)))
                .dimensions(panelX, y, bw, bh).build()); y += bh + gap;

        addDrawableChild(ButtonWidget.builder(
                Text.literal("✔ Done"),
                btn -> close())
                .dimensions(panelX, y, bw, bh).build());
    }

    private void addToggle(int x, int y, int w, int h, String label, boolean initial, java.util.function.Consumer<Boolean> onChange) {
        // Stateful toggle button — label shows current state.
        final boolean[] state = {initial};
        addDrawableChild(ButtonWidget.builder(
                Text.literal((state[0] ? "✔ " : "✘ ") + label),
                btn -> {
                    state[0] = !state[0];
                    onChange.accept(state[0]);
                    btn.setMessage(Text.literal((state[0] ? "✔ " : "✘ ") + label));
                })
                .dimensions(x, y, w, h).build());
    }

    // ─── Rendering ────────────────────────────────────────────────────────────

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Dim backdrop
        renderBackground(ctx, mouseX, mouseY, delta);

        int sw = this.width;
        int sh = this.height;

        // ── Left panel background ─────────────────────────────────────────────
        ctx.fill(0, 0, PANEL_WIDTH, sh, 0xCC111122);
        ctx.fill(PANEL_WIDTH, 0, PANEL_WIDTH + 1, sh, 0xFF334455);

        // ── Header ────────────────────────────────────────────────────────────
        ctx.fill(0, 0, PANEL_WIDTH, HEADER_H, 0xFF1A1A33);
        ctx.drawCenteredTextWithShadow(textRenderer, "⚙ KeystrokesHUD", PANEL_WIDTH / 2, 7, 0xFF88AAFF);

        // ── Right panel label ────────────────────────────────────────────────
        ctx.drawTextWithShadow(textRenderer, "Drag elements to reposition:", PANEL_WIDTH + 10, 8, 0xFFAABBCC);

        // ── Draw drag handles over HUD elements ──────────────────────────────
        if (!config.lockPositions) {
            drawHandle(ctx, (int) config.xW,   (int) config.yW,   config.keyWidth, config.keyHeight, DragTarget.W,   mouseX, mouseY);
            drawHandle(ctx, (int) config.xA,   (int) config.yA,   config.keyWidth, config.keyHeight, DragTarget.A,   mouseX, mouseY);
            drawHandle(ctx, (int) config.xS,   (int) config.yS,   config.keyWidth, config.keyHeight, DragTarget.S,   mouseX, mouseY);
            drawHandle(ctx, (int) config.xD,   (int) config.yD,   config.keyWidth, config.keyHeight, DragTarget.D,   mouseX, mouseY);
            drawHandle(ctx, (int) config.xLMB, (int) config.yLMB, config.lmbWidth, config.lmbHeight, DragTarget.LMB, mouseX, mouseY);
            drawHandle(ctx, (int) config.xRMB, (int) config.yRMB, config.lmbWidth, config.lmbHeight, DragTarget.RMB, mouseX, mouseY);
            drawHandle(ctx, (int) config.xFPS, (int) config.yFPS, config.fpsWidth, config.fpsHeight, DragTarget.FPS, mouseX, mouseY);
        }

        // Draw all widgets (buttons) on top.
        super.render(ctx, mouseX, mouseY, delta);

        // Hint text
        ctx.drawTextWithShadow(textRenderer, config.lockPositions ? "🔒 Positions locked" : "Drag to move", PANEL_WIDTH + 10, sh - 14, 0xFF667788);
    }

    private void drawHandle(DrawContext ctx, int x, int y, int w, int h,
                            DragTarget target, int mouseX, int mouseY) {
        boolean hovered  = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        boolean isDragging = dragging == target;
        int borderColor  = isDragging ? 0xFF66AAFF : (hovered ? 0xFF88CCFF : 0x88AAAACC);
        int fillColor    = isDragging ? 0x5566AAFF : (hovered ? 0x3366AAFF : 0x22AAAACC);

        ctx.fill(x, y, x + w, y + h, fillColor);
        // Border
        ctx.fill(x, y, x + w, y + 1, borderColor);
        ctx.fill(x, y + h - 1, x + w, y + h, borderColor);
        ctx.fill(x, y, x + 1, y + h, borderColor);
        ctx.fill(x + w - 1, y, x + w, y + h, borderColor);
        // Move cursor icon (4-arrow cross drawn with pixels)
        int cx = x + w / 2, cy = y + h / 2;
        ctx.fill(cx - 1, cy - 3, cx + 2, cy + 4, borderColor); // vertical
        ctx.fill(cx - 3, cy - 1, cx + 4, cy + 2, borderColor); // horizontal
    }

    // ─── Input handling ───────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && !config.lockPositions) {
            DragTarget t = hitTest((float) mouseX, (float) mouseY);
            if (t != DragTarget.NONE) {
                dragging = t;
                float[] pos = getPos(t);
                dragOffX = (float) mouseX - pos[0];
                dragOffY = (float) mouseY - pos[1];
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (dragging != DragTarget.NONE) {
            dragging = DragTarget.NONE;
            save();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dX, double dY) {
        if (dragging != DragTarget.NONE && button == 0) {
            float nx = (float) mouseX - dragOffX;
            float ny = (float) mouseY - dragOffY;
            if (config.snapToGrid) {
                nx = Math.round(nx / config.gridSize) * config.gridSize;
                ny = Math.round(ny / config.gridSize) * config.gridSize;
            }
            // Clamp to screen bounds.
            int sw = this.width, sh = this.height;
            nx = Math.max(PANEL_WIDTH + 2, Math.min(nx, sw - 60));
            ny = Math.max(0, Math.min(ny, sh - 20));
            setPos(dragging, nx, ny);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dX, dY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        save();
        client.setScreen(parent);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private DragTarget hitTest(float mx, float my) {
        if (hits(mx, my, config.xW,   config.yW,   config.keyWidth, config.keyHeight)) return DragTarget.W;
        if (hits(mx, my, config.xA,   config.yA,   config.keyWidth, config.keyHeight)) return DragTarget.A;
        if (hits(mx, my, config.xS,   config.yS,   config.keyWidth, config.keyHeight)) return DragTarget.S;
        if (hits(mx, my, config.xD,   config.yD,   config.keyWidth, config.keyHeight)) return DragTarget.D;
        if (hits(mx, my, config.xLMB, config.yLMB, config.lmbWidth, config.lmbHeight)) return DragTarget.LMB;
        if (hits(mx, my, config.xRMB, config.yRMB, config.lmbWidth, config.lmbHeight)) return DragTarget.RMB;
        if (hits(mx, my, config.xFPS, config.yFPS, config.fpsWidth, config.fpsHeight)) return DragTarget.FPS;
        return DragTarget.NONE;
    }

    private static boolean hits(float mx, float my, float ex, float ey, int ew, int eh) {
        return mx >= ex && mx < ex + ew && my >= ey && my < ey + eh;
    }

    private float[] getPos(DragTarget t) {
        return switch (t) {
            case W   -> new float[]{config.xW,   config.yW};
            case A   -> new float[]{config.xA,   config.yA};
            case S   -> new float[]{config.xS,   config.yS};
            case D   -> new float[]{config.xD,   config.yD};
            case LMB -> new float[]{config.xLMB, config.yLMB};
            case RMB -> new float[]{config.xRMB, config.yRMB};
            case FPS -> new float[]{config.xFPS, config.yFPS};
            default  -> new float[]{0, 0};
        };
    }

    private void setPos(DragTarget t, float x, float y) {
        switch (t) {
            case W   -> { config.xW   = x; config.yW   = y; }
            case A   -> { config.xA   = x; config.yA   = y; }
            case S   -> { config.xS   = x; config.yS   = y; }
            case D   -> { config.xD   = x; config.yD   = y; }
            case LMB -> { config.xLMB = x; config.yLMB = y; }
            case RMB -> { config.xRMB = x; config.yRMB = y; }
            case FPS -> { config.xFPS = x; config.yFPS = y; }
        }
    }

    private void save() { config.save(); }
}
