package com.keystrokeshud.screen;

import com.keystrokeshud.KeystrokesHudClient;
import com.keystrokeshud.config.HudConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class HudConfigScreen extends Screen {

    private static final int PANEL_WIDTH = 240;
    private static final int HEADER_H = 24;

    private final Screen parent;
    private final HudConfig config;

    // Modes
    private boolean keystrokeEditMode = false;

    // Drag
    private enum DragTarget { NONE, W, A, S, D, LMB, RMB, FPS }
    private DragTarget dragging = DragTarget.NONE;
    private float dragOffX, dragOffY;

    // Right-click popup
    private DragTarget popupTarget = DragTarget.NONE;
    private int popupX, popupY;

    public HudConfigScreen(Screen parent) {
        super(Text.literal("KeystrokesHUD Settings"));
        this.parent = parent;
        this.config = KeystrokesHudClient.config;
    }

    @Override
    protected void init() {
        if (keystrokeEditMode) {
            // Keystroke Edit Mode — শুধু Back বাটন
            addDrawableChild(ButtonWidget.builder(
                    Text.literal("< Back to Menu"),
                    btn -> { keystrokeEditMode = false; popupTarget = DragTarget.NONE; clearAndInit(); })
                    .dimensions(8, 8, 120, 18).build());
        } else {
            // Normal Mode
            int x = 8, y = HEADER_H + 6, bw = PANEL_WIDTH - 16, bh = 18, gap = 3;

            addToggle(x, y, bw, bh, "Show W Key",       config.showW,          v -> { config.showW = v;            save(); }); y += bh + gap;
            addToggle(x, y, bw, bh, "Show A Key",       config.showA,          v -> { config.showA = v;            save(); }); y += bh + gap;
            addToggle(x, y, bw, bh, "Show S Key",       config.showS,          v -> { config.showS = v;            save(); }); y += bh + gap;
            addToggle(x, y, bw, bh, "Show D Key",       config.showD,          v -> { config.showD = v;            save(); }); y += bh + gap;
            addToggle(x, y, bw, bh, "Show LMB",         config.showLMB,        v -> { config.showLMB = v;          save(); }); y += bh + gap;
            addToggle(x, y, bw, bh, "Show RMB",         config.showRMB,        v -> { config.showRMB = v;          save(); }); y += bh + gap;
            addToggle(x, y, bw, bh, "Show FPS",         config.showFPS,        v -> { config.showFPS = v;          save(); }); y += bh + gap;
            addToggle(x, y, bw, bh, "Show CPS",         config.showCPS,        v -> { config.showCPS = v;          save(); }); y += bh + gap;
            addToggle(x, y, bw, bh, "Show Borders",     config.showBorders,    v -> { config.showBorders = v;      save(); }); y += bh + gap;
            addToggle(x, y, bw, bh, "Rainbow Mode",     config.rainbowMode,    v -> { config.rainbowMode = v;      save(); }); y += bh + gap;
            addToggle(x, y, bw, bh, "Lock Positions",   config.lockPositions,  v -> { config.lockPositions = v;    save(); }); y += bh + gap;
            addToggle(x, y, bw, bh, "Snap to Grid",     config.snapToGrid,     v -> { config.snapToGrid = v;       save(); }); y += bh + gap;
            addToggle(x, y, bw, bh, "Performance Mode", config.performanceMode,v -> { config.performanceMode = v;  save(); }); y += bh + gap + 4;

            // ★ Keystroke Edit বাটন
            addDrawableChild(ButtonWidget.builder(
                    Text.literal("★ Keystroke Edit"),
                    btn -> { keystrokeEditMode = true; popupTarget = DragTarget.NONE; clearAndInit(); })
                    .dimensions(x, y, bw, bh).build()); y += bh + gap;

            addDrawableChild(ButtonWidget.builder(
                    Text.literal("Reset Layout"),
                    btn -> { config.resetLayout(); clearAndInit(); })
                    .dimensions(x, y, bw, bh).build()); y += bh + gap;

            addDrawableChild(ButtonWidget.builder(
                    Text.literal("Color Editor..."),
                    btn -> client.setScreen(new ColorEditorScreen(this)))
                    .dimensions(x, y, bw, bh).build()); y += bh + gap;

            addDrawableChild(ButtonWidget.builder(
                    Text.literal("Done"),
                    btn -> close())
                    .dimensions(x, y, bw, bh).build());
        }
    }

    private void addToggle(int x, int y, int w, int h, String label,
                           boolean initial, java.util.function.Consumer<Boolean> onChange) {
        final boolean[] state = {initial};
        addDrawableChild(ButtonWidget.builder(
                Text.literal((state[0] ? "[ON] " : "[OFF] ") + label),
                btn -> {
                    state[0] = !state[0];
                    onChange.accept(state[0]);
                    btn.setMessage(Text.literal((state[0] ? "[ON] " : "[OFF] ") + label));
                })
                .dimensions(x, y, w, h).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);

        if (keystrokeEditMode) {
            // Keystroke Edit Mode UI
            ctx.fill(0, 0, width, 30, 0xCC111122);
            ctx.drawCenteredTextWithShadow(textRenderer,
                    "Keystroke Edit — Drag to move | Right-click to customize",
                    width / 2, 10, 0xFF88AAFF);

            // Draw drag handles
            drawHandle(ctx, (int)config.xW,   (int)config.yW,   config.keyWidth, config.keyHeight, DragTarget.W,   mouseX, mouseY);
            drawHandle(ctx, (int)config.xA,   (int)config.yA,   config.keyWidth, config.keyHeight, DragTarget.A,   mouseX, mouseY);
            drawHandle(ctx, (int)config.xS,   (int)config.yS,   config.keyWidth, config.keyHeight, DragTarget.S,   mouseX, mouseY);
            drawHandle(ctx, (int)config.xD,   (int)config.yD,   config.keyWidth, config.keyHeight, DragTarget.D,   mouseX, mouseY);
            drawHandle(ctx, (int)config.xLMB, (int)config.yLMB, config.lmbWidth, config.lmbHeight, DragTarget.LMB, mouseX, mouseY);
            drawHandle(ctx, (int)config.xRMB, (int)config.yRMB, config.lmbWidth, config.lmbHeight, DragTarget.RMB, mouseX, mouseY);
            drawHandle(ctx, (int)config.xFPS, (int)config.yFPS, config.fpsWidth, config.fpsHeight, DragTarget.FPS, mouseX, mouseY);

            // Draw popup if open
            if (popupTarget != DragTarget.NONE) {
                drawPopup(ctx, mouseX, mouseY);
            }

        } else {
            // Normal mode panel
            ctx.fill(0, 0, PANEL_WIDTH, height, 0xCC111122);
            ctx.fill(PANEL_WIDTH, 0, PANEL_WIDTH + 1, height, 0xFF334455);
            ctx.fill(0, 0, PANEL_WIDTH, HEADER_H, 0xFF1A1A33);
            ctx.drawCenteredTextWithShadow(textRenderer, "KeystrokesHUD", PANEL_WIDTH / 2, 7, 0xFF88AAFF);
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    // ── Popup UI ─────────────────────────────────────────────────────────────

    private void drawPopup(DrawContext ctx, int mouseX, int mouseY) {
        String name = targetName(popupTarget);
        int pw = 180, ph = 130;
        int px = Math.min(popupX, width - pw - 4);
        int py = Math.min(popupY, height - ph - 4);

        // Background
        ctx.fill(px, py, px + pw, py + ph, 0xEE111133);
        ctx.fill(px, py, px + pw, py + 1, 0xFF88AAFF);
        ctx.fill(px, py + ph - 1, px + pw, py + ph, 0xFF88AAFF);
        ctx.fill(px, py, px + 1, py + ph, 0xFF88AAFF);
        ctx.fill(px + pw - 1, py, px + pw, py + ph, 0xFF88AAFF);

        // Title
        ctx.drawCenteredTextWithShadow(textRenderer, "Edit: " + name, px + pw / 2, py + 4, 0xFF88AAFF);

        // Info lines
        int[] wh = getWH(popupTarget);
        ctx.drawTextWithShadow(textRenderer, "Size: " + wh[0] + " x " + wh[1], px + 6, py + 16, 0xFFCCCCCC);
        ctx.drawTextWithShadow(textRenderer, "Opacity: " + (int)(config.backgroundOpacity * 100) + "%", px + 6, py + 26, 0xFFCCCCCC);
        ctx.drawTextWithShadow(textRenderer, "Border: " + config.borderThickness + "px", px + 6, py + 36, 0xFFCCCCCC);

        // Buttons inside popup
        int bx = px + 4, by = py + 48, bw = 82, bh = 14, gap = 3;

        // Size buttons
        drawSmallButton(ctx, bx,      by, bw, bh, "Size +", mouseX, mouseY);
        drawSmallButton(ctx, bx + bw + 4, by, bw, bh, "Size -", mouseX, mouseY); by += bh + gap;

        // Opacity buttons
        drawSmallButton(ctx, bx,      by, bw, bh, "Opacity +", mouseX, mouseY);
        drawSmallButton(ctx, bx + bw + 4, by, bw, bh, "Opacity -", mouseX, mouseY); by += bh + gap;

        // Border buttons
        drawSmallButton(ctx, bx,      by, bw, bh, "Border +", mouseX, mouseY);
        drawSmallButton(ctx, bx + bw + 4, by, bw, bh, "Border -", mouseX, mouseY); by += bh + gap;

        // Color button
        drawSmallButton(ctx, bx, by, bw * 2 + 4, bh, "Change Color", mouseX, mouseY); by += bh + gap;

        // Close button
        drawSmallButton(ctx, bx, by, bw * 2 + 4, bh, "Close", mouseX, mouseY);
    }

    private void drawSmallButton(DrawContext ctx, int x, int y, int w, int h,
                                 String label, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        ctx.fill(x, y, x + w, y + h, hovered ? 0xFF334466 : 0xFF222244);
        ctx.fill(x, y, x + w, y + 1, 0xFF556688);
        ctx.fill(x, y + h - 1, x + w, y + h, 0xFF556688);
        ctx.fill(x, y, x + 1, y + h, 0xFF556688);
        ctx.fill(x + w - 1, y, x + w, y + h, 0xFF556688);
        ctx.drawCenteredTextWithShadow(textRenderer, label, x + w / 2, y + (h - textRenderer.fontHeight) / 2, 0xFFFFFFFF);
    }

    private void drawHandle(DrawContext ctx, int x, int y, int w, int h,
                            DragTarget target, int mouseX, int mouseY) {
        boolean hovered  = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        boolean active   = dragging == target;
        boolean isPopup  = popupTarget == target;
        int fill   = isPopup ? 0x7788AAFF : active ? 0x5566AAFF : hovered ? 0x3366AAFF : 0x22AAAACC;
        int border = isPopup ? 0xFFFFAA00 : active ? 0xFF66AAFF : hovered ? 0xFF88CCFF : 0x88AAAACC;
        ctx.fill(x, y, x + w, y + h, fill);
        ctx.fill(x, y, x + w, y + 1, border);
        ctx.fill(x, y + h - 1, x + w, y + h, border);
        ctx.fill(x, y, x + 1, y + h, border);
        ctx.fill(x + w - 1, y, x + w, y + h, border);
        // Label
        String lbl = targetName(target);
        ctx.drawCenteredTextWithShadow(textRenderer, lbl,
                x + w / 2, y + (h - textRenderer.fontHeight) / 2, 0xFFFFFFFF);
    }

    // ── Mouse input ───────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (keystrokeEditMode) {
            // Right click → open popup
            if (button == 1) {
                DragTarget t = hitTest((float)mx, (float)my);
                if (t != DragTarget.NONE) {
                    popupTarget = t;
                    popupX = (int)mx;
                    popupY = (int)my;
                    return true;
                }
                // Click outside popup → close
                popupTarget = DragTarget.NONE;
                return true;
            }

            // Left click on popup buttons
            if (button == 0 && popupTarget != DragTarget.NONE) {
                if (handlePopupClick((float)mx, (float)my)) return true;
            }

            // Left click → drag
            if (button == 0 && !config.lockPositions) {
                DragTarget t = hitTest((float)mx, (float)my);
                if (t != DragTarget.NONE) {
                    dragging = t;
                    popupTarget = DragTarget.NONE;
                    float[] pos = getPos(t);
                    dragOffX = (float)mx - pos[0];
                    dragOffY = (float)my - pos[1];
                    return true;
                }
            }
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    private boolean handlePopupClick(float mx, float my) {
        // Recalculate popup position same as drawPopup
        int pw = 180;
        int px = Math.min(popupX, width - pw - 4);
        int py = Math.min(popupY, height - 134);
        int bx = px + 4, by = py + 48, bw = 82, bh = 14, gap = 3;

        // Size +
        if (hits(mx, my, bx, by, bw, bh)) { changeSize(popupTarget, 2, 2); save(); return true; }
        // Size -
        if (hits(mx, my, bx + bw + 4, by, bw, bh)) { changeSize(popupTarget, -2, -2); save(); return true; }
        by += bh + gap;
        // Opacity +
        if (hits(mx, my, bx, by, bw, bh)) { config.backgroundOpacity = Math.min(1f, config.backgroundOpacity + 0.1f); save(); return true; }
        // Opacity -
        if (hits(mx, my, bx + bw + 4, by, bw, bh)) { config.backgroundOpacity = Math.max(0f, config.backgroundOpacity - 0.1f); save(); return true; }
        by += bh + gap;
        // Border +
        if (hits(mx, my, bx, by, bw, bh)) { config.borderThickness = Math.min(8, config.borderThickness + 1); save(); return true; }
        // Border -
        if (hits(mx, my, bx + bw + 4, by, bw, bh)) { config.borderThickness = Math.max(0, config.borderThickness - 1); save(); return true; }
        by += bh + gap;
        // Change Color
        if (hits(mx, my, bx, by, bw * 2 + 4, bh)) { client.setScreen(new ColorEditorScreen(this)); return true; }
        by += bh + gap;
        // Close
        if (hits(mx, my, bx, by, bw * 2 + 4, bh)) { popupTarget = DragTarget.NONE; return true; }

        return false;
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (dragging != DragTarget.NONE) { dragging = DragTarget.NONE; save(); return true; }
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragging != DragTarget.NONE && button == 0) {
            float nx = (float)mx - dragOffX;
            float ny = (float)my - dragOffY;
            if (config.snapToGrid) {
                nx = Math.round(nx / config.gridSize) * config.gridSize;
                ny = Math.round(ny / config.gridSize) * config.gridSize;
            }
            nx = Math.max(0, Math.min(nx, width - 60));
            ny = Math.max(30, Math.min(ny, height - 20));
            setPos(dragging, nx, ny);
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            if (keystrokeEditMode) { keystrokeEditMode = false; popupTarget = DragTarget.NONE; clearAndInit(); }
            else close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() { save(); client.setScreen(parent); }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void changeSize(DragTarget t, int dw, int dh) {
        switch (t) {
            case W, A, S, D -> {
                config.keyWidth  = Math.max(16, config.keyWidth  + dw);
                config.keyHeight = Math.max(16, config.keyHeight + dh);
            }
            case LMB, RMB -> {
                config.lmbWidth  = Math.max(30, config.lmbWidth  + dw);
                config.lmbHeight = Math.max(14, config.lmbHeight + dh);
            }
            case FPS -> {
                config.fpsWidth  = Math.max(30, config.fpsWidth  + dw);
                config.fpsHeight = Math.max(12, config.fpsHeight + dh);
            }
        }
    }

    private String targetName(DragTarget t) {
        return switch (t) {
            case W   -> "W";
            case A   -> "A";
            case S   -> "S";
            case D   -> "D";
            case LMB -> "LMB";
            case RMB -> "RMB";
            case FPS -> "FPS";
            default  -> "";
        };
    }

    private int[] getWH(DragTarget t) {
        return switch (t) {
            case W, A, S, D -> new int[]{config.keyWidth,  config.keyHeight};
            case LMB, RMB   -> new int[]{config.lmbWidth,  config.lmbHeight};
            case FPS        -> new int[]{config.fpsWidth,  config.fpsHeight};
            default         -> new int[]{0, 0};
        };
    }

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
