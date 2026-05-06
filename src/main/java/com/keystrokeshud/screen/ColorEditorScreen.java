package com.keystrokeshud.screen;

import com.keystrokeshud.KeystrokesHudClient;
import com.keystrokeshud.config.HudConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class ColorEditorScreen extends Screen {

    private final Screen parent;
    private final HudConfig cfg;
    private int selectedElement = 0;
    private static final String[] NAMES = {"Global","W","A","S","D","LMB","RMB","FPS"};

    public ColorEditorScreen(Screen parent) {
        super(Text.literal("Color Editor"));
        this.parent = parent;
        this.cfg = KeystrokesHudClient.config;
    }

    @Override
    protected void init() {
        // Element selector buttons
        for (int i = 0; i < NAMES.length; i++) {
            final int idx = i;
            addDrawableChild(ButtonWidget.builder(
                    Text.literal(NAMES[i]),
                    btn -> { selectedElement = idx; rebuildWidgets(); })
                    .dimensions(8 + i * 38, 8, 36, 14).build());
        }

        int x = 8, y = 30, w = 160, h = 14, gap = 2;

        // Opacity slider
        addDrawableChild(new SliderWidget(x, y, w, h,
                Text.literal("Opacity: " + (int)(cfg.backgroundOpacity * 100) + "%"),
                cfg.backgroundOpacity) {
            @Override protected void updateMessage() {
                setMessage(Text.literal("Opacity: " + (int)(value * 100) + "%"));
            }
            @Override protected void applyValue() {
                cfg.backgroundOpacity = (float) value; cfg.save();
            }
        }); y += h + gap;

        // Rainbow speed slider
        addDrawableChild(new SliderWidget(x, y, w, h,
                Text.literal("Rainbow Speed: " + String.format("%.1f", cfg.rainbowSpeed)),
                cfg.rainbowSpeed / 5.0) {
            @Override protected void updateMessage() {
                setMessage(Text.literal("Rainbow Speed: " + String.format("%.1f", value * 5)));
            }
            @Override protected void applyValue() {
                cfg.rainbowSpeed = (float)(value * 5); cfg.save();
            }
        }); y += h + gap;

        // Border thickness slider
        addDrawableChild(new SliderWidget(x, y, w, h,
                Text.literal("Border Thickness: " + cfg.borderThickness),
                cfg.borderThickness / 4.0) {
            @Override protected void updateMessage() {
                setMessage(Text.literal("Border: " + Math.max(1,(int)(value*4))));
            }
            @Override protected void applyValue() {
                cfg.borderThickness = Math.max(1,(int)(value*4)); cfg.save();
            }
        }); y += h + gap;

        // Corner radius slider
        addDrawableChild(new SliderWidget(x, y, w, h,
                Text.literal("Corner Radius: " + cfg.cornerRadius),
                cfg.cornerRadius / 8.0) {
            @Override protected void updateMessage() {
                setMessage(Text.literal("Radius: " + (int)(value*8)));
            }
            @Override protected void applyValue() {
                cfg.cornerRadius = (int)(value*8); cfg.save();
            }
        }); y += h + gap + 8;

        // Back button
        addDrawableChild(ButtonWidget.builder(
                Text.literal("< Back"),
                btn -> client.setScreen(parent))
                .dimensions(x, y, 80, 16).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);
        ctx.fill(0, 0, width, height, 0xCC111122);
        ctx.drawCenteredTextWithShadow(textRenderer, "Color Editor — " + NAMES[selectedElement], width / 2, 1, 0xFF88AAFF);
        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public void close() { cfg.save(); client.setScreen(parent); }
                    }
