package com.keystrokeshud.screen;

import com.keystrokeshud.KeystrokesHudClient;
import com.keystrokeshud.config.HudConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

/**
 * ColorEditorScreen — Per-element colour customisation.
 *
 * Provides RGB sliders for:
 *  - Each key's text colour
 *  - Pressed background colour
 *  - Idle background colour & opacity
 *  - Rainbow mode speed
 *  - Border thickness
 *  - Corner radius
 *
 * Design: simple slider-based approach avoids needing a full colour-wheel
 * widget, keeping the implementation lightweight and compatible with all
 * screen sizes.
 */
public class ColorEditorScreen extends Screen {

    private final Screen    parent;
    private final HudConfig cfg;

    // Currently selected element for per-element colour editing.
    private int selectedElement = 0; // 0=Global, 1=W, 2=A, 3=S, 4=D, 5=LMB, 6=RMB, 7=FPS
    private static final String[] ELEMENT_NAMES = {"Global", "W", "A", "S", "D", "LMB", "RMB", "FPS"};

    public ColorEditorScreen(Screen parent) {
        super(Text.literal("KeystrokesHUD — Color Editor"));
        this.parent = parent;
        this.cfg    = KeystrokesHudClient.config;
    }

    @Override
    protected void init() {
        int x = 10, y = 30, bw = 160, bh = 16, gap = 2;

        // ── Element selector ──────────────────────────────────────────────────
        for (int i = 0; i < ELEMENT_NAMES.length; i++) {
            final int idx = i;
            addDrawableChild(ButtonWidget.builder(
                    Text.literal(ELEMENT_NAMES[i]),
                    btn -> { selectedElement = idx; rebuildWidgets(); })
                    .dimensions(x + i * 38, 10, 36, 16).build());
        }

        y = 36;
        String el = ELEMENT_NAMES[selectedElement];

        // ── Text colour sliders (R/G/B) ───────────────────────────────────────
        int[] textColor = getTextColor();
        addLabel(x, y, "Text Color — " + el); y += 12;
        addRgbSliders(x, y, bw, bh, gap, textColor, newColor -> setTextColor(newColor));
        y += (bh + gap) * 3 + 8;

        // ── Pressed colour ────────────────────────────────────────────────────
        addLabel(x, y, "Pressed Color"); y += 12;
        int[] pressed = unpackRGB(cfg.colorPressed);
        addRgbSliders(x, y, bw, bh, gap, pressed, c -> { cfg.colorPressed = packRGB(c, 255); save(); });
        y += (bh + gap) * 3 + 8;

        // ── Idle colour ───────────────────────────────────────────────────────
        addLabel(x, y, "Idle Color"); y += 12;
        int[] idle = unpackRGB(cfg.colorIdle);
        addRgbSliders(x, y, bw, bh, gap, idle, c -> { cfg.colorIdle = packRGB(c, (cfg.colorIdle >> 24) & 0xFF); save(); });
        y += (bh + gap) * 3 + 8;

        // ── Background opacity ────────────────────────────────────────────────
        addLabel(x, y, "Background Opacity"); y += 12;
        addSlider(x, y, bw, bh, "Opacity", cfg.backgroundOpacity, v -> { cfg.backgroundOpacity = v; save(); });
        y += bh + gap + 8;

        // ── Border thickness ──────────────────────────────────────────────────
        addLabel(x, y, "Border Thickness (1-4)"); y += 12;
        addSlider(x, y, bw, bh, "Thickness", cfg.borderThickness / 4.0, v -> {
            cfg.borderThickness = Math.max(1, (int)(v * 4));
            save();
        });
        y += bh + gap + 8;

        // ── Corner radius ──────────────────────────────────────────────────────
        addLabel(x, y, "Corner Radius (0-8)"); y += 12;
        addSlider(x, y, bw, bh, "Radius", cfg.cornerRadius / 8.0, v -> {
            cfg.cornerRadius = (int)(v * 8);
            save();
        });
        y += bh + gap + 8;

        // ── Rainbow speed ─────────────────────────────────────────────────────
        addLabel(x, y, "Rainbow Speed"); y += 12;
        addSlider(x, y, bw, bh, "Speed", cfg.rainbowSpeed / 5.0, v -> {
            cfg.rainbowSpeed = (float)(v * 5.0);
            save();
        });
        y += bh + gap + 16;

        // ── Close ─────────────────────────────────────────────────────────────
        addDrawableChild(ButtonWidget.builder(
                Text.literal("← Back"),
                btn -> client.setScreen(parent))
                .dimensions(x, y, 80, 18).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);
        ctx.fill(0, 0, width, height, 0xCC111122);
        ctx.drawCenteredTextWithShadow(textRenderer, "Color Editor", width / 2, 1, 0xFF88AAFF);

        // Preview swatches
        int[] tc = getTextColor();
        ctx.fill(180, 36, 200, 52, packRGB(tc, 255));
        ctx.fill(182, 38, 198, 50, packRGB(tc, 255));

        super.render(ctx, mouseX, mouseY, delta);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void addLabel(int x, int y, String text) {
        // Labels are drawn in render() — we use a button-like widget here just
        // to anchor text; we override the render in render().
        // Simpler: just rely on DrawContext in render, but we don't have per-widget
        // positions at that point.  Instead we'll draw labels directly.
        // Note: Minecraft's Screen doesn't have a label widget in 1.21, so we
        // draw them manually in render() by tracking positions.  For simplicity,
        // we'll use a disabled button as a label.
        ButtonWidget lbl = ButtonWidget.builder(Text.literal(text), b -> {})
                .dimensions(x, y, 200, 10).build();
        lbl.active = false;
        addDrawableChild(lbl);
    }

    private void addRgbSliders(int x, int y, int w, int h, int gap,
                               int[] rgb, java.util.function.Consumer<int[]> onChange) {
        addSlider(x, y, w, h, "R: " + rgb[0], rgb[0] / 255.0, v -> {
            int[] c = { (int)(v * 255), rgb[1], rgb[2] };
            rgb[0] = c[0];
            onChange.accept(c);
        });
        addSlider(x, y + h + gap, w, h, "G: " + rgb[1], rgb[1] / 255.0, v -> {
            int[] c = { rgb[0], (int)(v * 255), rgb[2] };
            rgb[1] = c[1];
            onChange.accept(c);
        });
        addSlider(x, y + (h + gap) * 2, w, h, "B: " + rgb[2], rgb[2] / 255.0, v -> {
            int[] c = { rgb[0], rgb[1], (int)(v * 255) };
            rgb[2] = c[2];
            onChange.accept(c);
        });
    }

    private void addSlider(int x, int y, int w, int h, String label, double value,
                           java.util.function.Consumer<Float> onChange) {
        addDrawableChild(new SliderWidget(x, y, w, h, Text.literal(label), value) {
            @Override
            protected void updateMessage() {
                // Label already set
            }
            @Override
            protected void applyValue() {
                onChange.accept((float) value);
            }
        });
    }

    // ─── Per-element colour accessors ─────────────────────────────────────────

    private int[] getTextColor() {
        int c = switch (selectedElement) {
            case 1 -> cfg.colorTextW;
            case 2 -> cfg.colorTextA;
            case 3 -> cfg.colorTextS;
            case 4 -> cfg.colorTextD;
            case 5 -> cfg.colorTextLMB;
            case 6 -> cfg.colorTextRMB;
            case 7 -> cfg.colorTextFPS;
            default -> 0xFFFFFFFF;
        };
        return unpackRGB(c);
    }

    private void setTextColor(int[] rgb) {
        int packed = packRGB(rgb, 255);
        switch (selectedElement) {
            case 1 -> cfg.colorTextW   = packed;
            case 2 -> cfg.colorTextA   = packed;
            case 3 -> cfg.colorTextS   = packed;
            case 4 -> cfg.colorTextD   = packed;
            case 5 -> cfg.colorTextLMB = packed;
            case 6 -> cfg.colorTextRMB = packed;
            case 7 -> cfg.colorTextFPS = packed;
        }
        save();
    }

    private static int[] unpackRGB(int argb) {
        return new int[]{(argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF};
    }

    private static int packRGB(int[] rgb, int alpha) {
        return (alpha << 24) | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
    }

    private void save() { cfg.save(); }
}
