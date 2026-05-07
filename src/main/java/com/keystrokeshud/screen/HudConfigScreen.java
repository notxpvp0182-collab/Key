package com.keystrokeshud.screen;

import com.keystrokeshud.KeystrokesHudClient;
import com.keystrokeshud.config.HudConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class HudConfigScreen extends Screen {

    private static final int PANEL_WIDTH = 230;
    private static final int HEADER_H = 24;

    private final Screen parent;
    private final HudConfig config;

    private enum DragTarget { NONE, W, A, S, D, LMB, RMB, FPS }
    private DragTarget dragging = DragTarget.NONE;
    private float dragOffX, dragOffY;

    public HudConfigScreen(Screen parent) {
        super(Text.literal("KeystrokesHUD Settings"));
        this.parent = parent;
        this.config = KeystrokesHudClient.config;
    }

    @Override
    protected void init() {
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
        ctx.fill(0, 0, PANEL_WIDTH, height, 0xCC111122);
        ctx.fill(PANEL_WIDTH, 0, PANEL_WIDTH + 1, height, 0xFF334455);
        ctx.fill(0, 0, PANEL_WIDTH, HEADER_H, 0xFF1A1A33);
        ctx.drawCenteredTextWithShadow(textRenderer, "KeystrokesHUD", PANEL_WIDTH / 2, 7, 0xFF88AAFF);
        ctx.drawTextWithShadow(textRenderer, "Drag elements to reposition:", PANEL_WIDTH + 10, 8, 0xFFAABBCC);

        if (!config.lockPositions) {
            drawHandle(ctx, (int)config.xW,   (int)config.yW,   config.keyWidth, config.keyHeight, DragTarget.W,   mouseX, mouseY);
            drawHandle(ctx, (int)config.xA,   (int)config.yA,   config.keyWidth, config.keyHeight, DragTarget.A,   mouseX, mouseY);
            drawHandle(ctx, (int)config.xS,   (int)config.yS,   config.keyWidth, config.keyHeight, DragTarget.S,   mouseX, mouseY);
            drawHandle(ctx, (int)config.xD,   (int)config.yD,   config.keyWidth, config.keyHeight, DragTarget.D,   mouseX, mouseY);
            drawHandle(ctx, (int)config.xLMB, (int)config.yLMB, config.lmbWidth, config.lmbHeight, DragTarget.LMB, mouseX, mouseY);
            drawHandle(ctx, (int)config.xRMB, (int)config.yRMB, config.lmbWidth, config.lmbHeight, DragTarget.RMB, mouseX, mouseY);
            drawHandle(ctx, (int)config.xFPS, (int)config.yFPS, config.fpsWidth, config.fpsHeight, DragTarget.FPS, mouseX, mouseY);
        }

        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawTextWithShadow(textRenderer,
                config.lockPositions ? "Positions LOCKED" : "Drag to move",
                PANEL_WIDTH + 10, height - 14, 0xFF667788);
    }

    private void drawHandle(DrawContext ctx, int x, int y, int w, int h,
                            DragTarget target, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        boolean active  = dragging == target;
        int fill   = active ? 0x5566AAFF : hovered ? 0x3366AAFF : 0x22AAAACC;
        int border = active ? 0xFF66AAFF : hovered ? 0xFF88CCFF : 0x88AAAACC;
        ctx.fill(x, y, x + w, y + h, fill);
        ctx.fill(x, y, x + w, y + 1, border);
        ctx.fill(x, y + h - 1, x + w, y + h, border);
        ctx.fill(x, y, x + 1, y + h, border);
        ctx.fill(x + w - 1, y, x + w, y + h, border);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0 && !config.lockPositions) {
            DragTarget t = hitTest((float)mx, (float)my);
            if (t != DragTarget.NONE) {
                dragging = t;
                float[] pos = getPos(t);
                dragOffX = (float)mx - pos[0];
                dragOffY = (float)my - pos[1];
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
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
            nx = Math.max(PANEL_WIDTH + 2, Math.min(nx, width - 60));
            ny = Math.max(0, Math.min(ny, height - 20));
            setPos(dragging, nx, ny);
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { close(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() { save(); client.setScreen(parent); }

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
