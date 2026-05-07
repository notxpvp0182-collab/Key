package com.keystrokeshud.screen;

import com.keystrokeshud.KeystrokesHudClient;
import com.keystrokeshud.config.HudConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class HudConfigScreen extends Screen {

    private static final int PANEL_WIDTH = 240;
    private static final int HEADER_H = 24;

    private final Screen parent;
    private final HudConfig config;

    private boolean keystrokeEditMode = false;

    private enum DragTarget { NONE, W, A, S, D, LMB, RMB, FPS }
    private DragTarget dragging = DragTarget.NONE;
    private float dragOffX, dragOffY;

    // Popup
    private DragTarget popupTarget = DragTarget.NONE;
    private int popupX, popupY;

    // Popup color picker state
    private boolean showColorPicker = false;
    private int colorPickerR, colorPickerG, colorPickerB;

    public HudConfigScreen(Screen parent) {
        super(Text.literal("KeystrokesHUD Settings"));
        this.parent = parent;
        this.config = KeystrokesHudClient.config;
    }

    @Override
    protected void init() {
        if (keystrokeEditMode) {
            addDrawableChild(ButtonWidget.builder(
                    Text.literal("< Back to Menu"),
                    btn -> {
                        keystrokeEditMode = false;
                        popupTarget = DragTarget.NONE;
                        showColorPicker = false;
                        clearAndInit();
                    })
                    .dimensions(8, 8, 120, 18).build());
        } else {
            int x = 8, y = HEADER_H + 6, bw = PANEL_WIDTH - 16, bh = 18, gap = 3;

            addToggle(x,y,bw,bh,"Show W Key",       config.showW,          v->{config.showW=v;          save();}); y+=bh+gap;
            addToggle(x,y,bw,bh,"Show A Key",       config.showA,          v->{config.showA=v;          save();}); y+=bh+gap;
            addToggle(x,y,bw,bh,"Show S Key",       config.showS,          v->{config.showS=v;          save();}); y+=bh+gap;
            addToggle(x,y,bw,bh,"Show D Key",       config.showD,          v->{config.showD=v;          save();}); y+=bh+gap;
            addToggle(x,y,bw,bh,"Show LMB",         config.showLMB,        v->{config.showLMB=v;        save();}); y+=bh+gap;
            addToggle(x,y,bw,bh,"Show RMB",         config.showRMB,        v->{config.showRMB=v;        save();}); y+=bh+gap;
            addToggle(x,y,bw,bh,"Show FPS",         config.showFPS,        v->{config.showFPS=v;        save();}); y+=bh+gap;
            addToggle(x,y,bw,bh,"Show CPS",         config.showCPS,        v->{config.showCPS=v;        save();}); y+=bh+gap;
            addToggle(x,y,bw,bh,"Show Borders",     config.showBorders,    v->{config.showBorders=v;    save();}); y+=bh+gap;
            addToggle(x,y,bw,bh,"Rainbow Mode",     config.rainbowMode,    v->{config.rainbowMode=v;    save();}); y+=bh+gap;
            addToggle(x,y,bw,bh,"Lock Positions",   config.lockPositions,  v->{config.lockPositions=v;  save();}); y+=bh+gap;
            addToggle(x,y,bw,bh,"Snap to Grid",     config.snapToGrid,     v->{config.snapToGrid=v;     save();}); y+=bh+gap;
            addToggle(x,y,bw,bh,"Performance Mode", config.performanceMode,v->{config.performanceMode=v;save();}); y+=bh+gap+4;

            addDrawableChild(ButtonWidget.builder(
                    Text.literal("★ Keystroke Edit"),
                    btn -> { keystrokeEditMode=true; popupTarget=DragTarget.NONE; clearAndInit(); })
                    .dimensions(x,y,bw,bh).build()); y+=bh+gap;

            addDrawableChild(ButtonWidget.builder(
                    Text.literal("Reset Layout"),
                    btn -> { config.resetLayout(); clearAndInit(); })
                    .dimensions(x,y,bw,bh).build()); y+=bh+gap;

            addDrawableChild(ButtonWidget.builder(
                    Text.literal("Done"),
                    btn -> close())
                    .dimensions(x,y,bw,bh).build());
        }
    }

    private void addToggle(int x, int y, int w, int h, String label,
                           boolean initial, java.util.function.Consumer<Boolean> onChange) {
        final boolean[] state = {initial};
        addDrawableChild(ButtonWidget.builder(
                Text.literal((state[0]?"[ON] ":"[OFF] ")+label),
                btn -> {
                    state[0] = !state[0];
                    onChange.accept(state[0]);
                    btn.setMessage(Text.literal((state[0]?"[ON] ":"[OFF] ")+label));
                })
                .dimensions(x,y,w,h).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);

        if (keystrokeEditMode) {
            // Header bar
            ctx.fill(0, 0, width, 30, 0xDD111122);
            ctx.drawCenteredTextWithShadow(textRenderer,
                    "Keystroke Edit  |  Drag = Move  |  Right-click = Options",
                    width/2, 10, 0xFF88AAFF);

            // Draw handles for all elements
            drawHandle(ctx,(int)config.xW,  (int)config.yW,  config.wW,  config.hW,  DragTarget.W,  mouseX,mouseY);
            drawHandle(ctx,(int)config.xA,  (int)config.yA,  config.wA,  config.hA,  DragTarget.A,  mouseX,mouseY);
            drawHandle(ctx,(int)config.xS,  (int)config.yS,  config.wS,  config.hS,  DragTarget.S,  mouseX,mouseY);
            drawHandle(ctx,(int)config.xD,  (int)config.yD,  config.wD,  config.hD,  DragTarget.D,  mouseX,mouseY);
            drawHandle(ctx,(int)config.xLMB,(int)config.yLMB,config.wLMB,config.hLMB,DragTarget.LMB,mouseX,mouseY);
            drawHandle(ctx,(int)config.xRMB,(int)config.yRMB,config.wRMB,config.hRMB,DragTarget.RMB,mouseX,mouseY);
            drawHandle(ctx,(int)config.xFPS,(int)config.yFPS,config.wFPS,config.hFPS,DragTarget.FPS,mouseX,mouseY);

            // Popup
            if (popupTarget != DragTarget.NONE) {
                if (showColorPicker) drawColorPicker(ctx, mouseX, mouseY);
                else drawPopup(ctx, mouseX, mouseY);
            }

        } else {
            ctx.fill(0,0,PANEL_WIDTH,height,0xCC111122);
            ctx.fill(PANEL_WIDTH,0,PANEL_WIDTH+1,height,0xFF334455);
            ctx.fill(0,0,PANEL_WIDTH,HEADER_H,0xFF1A1A33);
            ctx.drawCenteredTextWithShadow(textRenderer,"KeystrokesHUD",PANEL_WIDTH/2,7,0xFF88AAFF);
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    // ── Popup ─────────────────────────────────────────────────────────────────

    private void drawPopup(DrawContext ctx, int mouseX, int mouseY) {
        int pw=190, ph=175;
        int px=Math.min(popupX, width-pw-4);
        int py=Math.min(popupY, height-ph-4);

        // Background + border
        ctx.fill(px,py,px+pw,py+ph,0xEE0D1226);
        ctx.fill(px,py,px+pw,py+1,0xFF5577AA);
        ctx.fill(px,py+ph-1,px+pw,py+ph,0xFF5577AA);
        ctx.fill(px,py,px+1,py+ph,0xFF5577AA);
        ctx.fill(px+pw-1,py,px+pw,py+ph,0xFF5577AA);

        // Title
        String name = targetName(popupTarget);
        ctx.drawCenteredTextWithShadow(textRenderer,"[ "+name+" ]",px+pw/2,py+5,0xFF88AAFF);

        // Info
        int[] wh = getWH(popupTarget);
        int radius = getRadius(popupTarget);
        ctx.drawTextWithShadow(textRenderer,"Size: "+wh[0]+"×"+wh[1],px+8,py+18,0xFFCCCCCC);
        ctx.drawTextWithShadow(textRenderer,"Radius: "+radius,px+8,py+28,0xFFCCCCCC);
        ctx.drawTextWithShadow(textRenderer,"Opacity: "+(int)(config.backgroundOpacity*100)+"%",px+8,py+38,0xFFCCCCCC);
        ctx.drawTextWithShadow(textRenderer,"Border: "+config.borderThickness+"px",px+8,py+48,0xFFCCCCCC);

        int bx=px+6, by=py+62, bw=84, bh=16, gap=4;

        // Row 1: Size
        drawBtn(ctx,"W +"  ,bx,      by,bw,bh,mouseX,mouseY);
        drawBtn(ctx,"W -"  ,bx+bw+4, by,bw,bh,mouseX,mouseY); by+=bh+gap;
        drawBtn(ctx,"H +"  ,bx,      by,bw,bh,mouseX,mouseY);
        drawBtn(ctx,"H -"  ,bx+bw+4, by,bw,bh,mouseX,mouseY); by+=bh+gap;

        // Row 3: Corner radius
        drawBtn(ctx,"Radius+"  ,bx,      by,bw,bh,mouseX,mouseY);
        drawBtn(ctx,"Radius-"  ,bx+bw+4, by,bw,bh,mouseX,mouseY); by+=bh+gap;

        // Row 4: Opacity
        drawBtn(ctx,"Opacity+",bx,      by,bw,bh,mouseX,mouseY);
        drawBtn(ctx,"Opacity-",bx+bw+4, by,bw,bh,mouseX,mouseY); by+=bh+gap;

        // Row 5: Border
        drawBtn(ctx,"Border+" ,bx,      by,bw,bh,mouseX,mouseY);
        drawBtn(ctx,"Border-" ,bx+bw+4, by,bw,bh,mouseX,mouseY); by+=bh+gap;

        // Row 6: Color + Close
        drawBtn(ctx,"🎨 Color" ,bx,      by,bw,bh,mouseX,mouseY);
        drawBtn(ctx,"✖ Close" ,bx+bw+4, by,bw,bh,mouseX,mouseY);
    }

    private void drawColorPicker(DrawContext ctx, int mouseX, int mouseY) {
        int pw=200, ph=130;
        int px=Math.min(popupX, width-pw-4);
        int py=Math.min(popupY, height-ph-4);

        ctx.fill(px,py,px+pw,py+ph,0xEE0D1226);
        ctx.fill(px,py,px+pw,py+1,0xFF88AAFF);
        ctx.fill(px,py+ph-1,px+pw,py+ph,0xFF88AAFF);
        ctx.fill(px,py,px+1,py+ph,0xFF88AAFF);
        ctx.fill(px+pw-1,py,px+pw,py+ph,0xFF88AAFF);

        ctx.drawCenteredTextWithShadow(textRenderer,"Color: "+targetName(popupTarget),px+pw/2,py+5,0xFF88AAFF);

        // Color preview
        int previewColor = 0xFF000000|(colorPickerR<<16)|(colorPickerG<<8)|colorPickerB;
        ctx.fill(px+pw-30,py+2,px+pw-2,py+14,previewColor);

        int bx=px+6, by=py+18, bw=180, bh=14, gap=3;

        // R slider visual
        ctx.drawTextWithShadow(textRenderer,"R: "+colorPickerR,bx,by,0xFFFF6666);
        drawSliderBar(ctx,bx+30,by,bw-30,bh,colorPickerR/255f,0xFFFF4444); by+=bh+gap;

        // G slider visual
        ctx.drawTextWithShadow(textRenderer,"G: "+colorPickerG,bx,by,0xFF66FF66);
        drawSliderBar(ctx,bx+30,by,bw-30,bh,colorPickerG/255f,0xFF44FF44); by+=bh+gap;

        // B slider visual
        ctx.drawTextWithShadow(textRenderer,"B: "+colorPickerB,bx,by,0xFF6688FF);
        drawSliderBar(ctx,bx+30,by,bw-30,bh,colorPickerB/255f,0xFF4466FF); by+=bh+gap+2;

        drawBtn(ctx,"Apply",bx,by,86,14,mouseX,mouseY);
        drawBtn(ctx,"Cancel",bx+90,by,86,14,mouseX,mouseY);
    }

    private void drawSliderBar(DrawContext ctx, int x, int y, int w, int h, float value, int color) {
        ctx.fill(x,y,x+w,y+h,0xFF222233);
        ctx.fill(x,y,x+(int)(w*value),y+h,color);
        ctx.fill(x,y,x+w,y+1,0xFF445566);
        ctx.fill(x,y+h-1,x+w,y+h,0xFF445566);
    }

    private void drawBtn(DrawContext ctx, String label, int x, int y, int w, int h, int mx, int my) {
        boolean hov = mx>=x&&mx<x+w&&my>=y&&my<y+h;
        ctx.fill(x,y,x+w,y+h,hov?0xFF334466:0xFF1A2244);
        ctx.fill(x,y,x+w,y+1,0xFF556688);
        ctx.fill(x,y+h-1,x+w,y+h,0xFF556688);
        ctx.fill(x,y,x+1,y+h,0xFF556688);
        ctx.fill(x+w-1,y,x+w,y+h,0xFF556688);
        ctx.drawCenteredTextWithShadow(textRenderer,label,x+w/2,y+(h-textRenderer.fontHeight)/2,0xFFFFFFFF);
    }

    private void drawHandle(DrawContext ctx, int x, int y, int w, int h,
                            DragTarget target, int mouseX, int mouseY) {
        boolean hov = mouseX>=x&&mouseX<x+w&&mouseY>=y&&mouseY<y+h;
        boolean act = dragging==target;
        boolean pop = popupTarget==target;
        int fill  = pop?0x7788AAFF:act?0x5566AAFF:hov?0x3366AAFF:0x33AAAACC;
        int border= pop?0xFFFFAA00:act?0xFF66AAFF:hov?0xFF88CCFF:0xFFAABBCC;
        ctx.fill(x,y,x+w,y+h,fill);
        // Corner-only border on handle too
        int cs=Math.max(3,Math.min(w,h)/4);
        ctx.fill(x,y,x+cs,y+1,border); ctx.fill(x,y,x+1,y+cs,border);
        ctx.fill(x+w-cs,y,x+w,y+1,border); ctx.fill(x+w-1,y,x+w,y+cs,border);
        ctx.fill(x,y+h-1,x+cs,y+h,border); ctx.fill(x,y+h-cs,x+1,y+h,border);
        ctx.fill(x+w-cs,y+h-1,x+w,y+h,border); ctx.fill(x+w-1,y+h-cs,x+w,y+h,border);
        ctx.drawCenteredTextWithShadow(textRenderer,targetName(target),x+w/2,y+(h-textRenderer.fontHeight)/2,0xFFFFFFFF);
    }

    // ── Mouse ─────────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (keystrokeEditMode) {

            // Color picker clicks
            if (showColorPicker && popupTarget != DragTarget.NONE) {
                if (handleColorPickerClick((float)mx,(float)my)) return true;
            }

            // Popup clicks
            if (!showColorPicker && popupTarget != DragTarget.NONE) {
                if (handlePopupClick((float)mx,(float)my)) return true;
                // Click outside popup → close
                popupTarget = DragTarget.NONE;
                return true;
            }

            // Right click → open popup
            if (button == 1) {
                DragTarget t = hitTest((float)mx,(float)my);
                if (t != DragTarget.NONE) {
                    popupTarget = t;
                    popupX = (int)mx;
                    popupY = (int)my;
                    showColorPicker = false;
                    // Init color from current text color
                    int c = getTextColor(t);
                    colorPickerR = (c>>16)&0xFF;
                    colorPickerG = (c>>8)&0xFF;
                    colorPickerB = c&0xFF;
                    return true;
                }
                return true;
            }

            // Left click drag
            if (button == 0) {
                DragTarget t = hitTest((float)mx,(float)my);
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
        return super.mouseClicked(mx,my,button);
    }

    private boolean handlePopupClick(float mx, float my) {
        int pw=190;
        int px=Math.min(popupX,width-pw-4);
        int py=Math.min(popupY,height-175-4);
        int bx=px+6, by=py+62, bw=84, bh=16, gap=4;

        // W+
        if (hits(mx,my,bx,by,bw,bh)) { addW(popupTarget,2); save(); return true; }
        if (hits(mx,my,bx+bw+4,by,bw,bh)) { addW(popupTarget,-2); save(); return true; } by+=bh+gap;
        // H+
        if (hits(mx,my,bx,by,bw,bh)) { addH(popupTarget,2); save(); return true; }
        if (hits(mx,my,bx+bw+4,by,bw,bh)) { addH(popupTarget,-2); save(); return true; } by+=bh+gap;
        // Radius+/-
        if (hits(mx,my,bx,by,bw,bh)) { addRadius(popupTarget,1); save(); return true; }
        if (hits(mx,my,bx+bw+4,by,bw,bh)) { addRadius(popupTarget,-1); save(); return true; } by+=bh+gap;
        // Opacity+/-
        if (hits(mx,my,bx,by,bw,bh)) { config.backgroundOpacity=Math.min(1f,config.backgroundOpacity+0.05f); save(); return true; }
        if (hits(mx,my,bx+bw+4,by,bw,bh)) { config.backgroundOpacity=Math.max(0f,config.backgroundOpacity-0.05f); save(); return true; } by+=bh+gap;
        // Border+/-
        if (hits(mx,my,bx,by,bw,bh)) { config.borderThickness=Math.min(8,config.borderThickness+1); save(); return true; }
        if (hits(mx,my,bx+bw+4,by,bw,bh)) { config.borderThickness=Math.max(0,config.borderThickness-1); save(); return true; } by+=bh+gap;
        // Color
        if (hits(mx,my,bx,by,bw,bh)) { showColorPicker=true; return true; }
        // Close
        if (hits(mx,my,bx+bw+4,by,bw,bh)) { popupTarget=DragTarget.NONE; return true; }

        return false;
    }

    private boolean handleColorPickerClick(float mx, float my) {
        int pw=200;
        int px=Math.min(popupX,width-pw-4);
        int py=Math.min(popupY,height-130-4);
        int bx=px+6, by=py+18, bw=180, bh=14, gap=3;

        // Slider click — R
        if (hits(mx,my,bx+30,by,bw-30,bh)) { colorPickerR=(int)(((mx-bx-30)/(bw-30))*255); return true; } by+=bh+gap;
        // G
        if (hits(mx,my,bx+30,by,bw-30,bh)) { colorPickerG=(int)(((mx-bx-30)/(bw-30))*255); return true; } by+=bh+gap;
        // B
        if (hits(mx,my,bx+30,by,bw-30,bh)) { colorPickerB=(int)(((mx-bx-30)/(bw-30))*255); return true; } by+=bh+gap+2;

        // Apply
        if (hits(mx,my,bx,by,86,14)) {
            int newColor = 0xFF000000|(colorPickerR<<16)|(colorPickerG<<8)|colorPickerB;
            setTextColor(popupTarget, newColor);
            save();
            showColorPicker=false;
            return true;
        }
        // Cancel
        if (hits(mx,my,bx+90,by,86,14)) { showColorPicker=false; return true; }

        return false;
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (dragging!=DragTarget.NONE) { dragging=DragTarget.NONE; save(); return true; }
        return super.mouseReleased(mx,my,button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragging!=DragTarget.NONE && button==0) {
            // Smooth drag — NO grid snap in edit mode
            float nx=(float)mx-dragOffX;
            float ny=(float)my-dragOffY;
            nx=Math.max(0,Math.min(nx,width-60));
            ny=Math.max(30,Math.min(ny,height-20));
            setPos(dragging,nx,ny);
            return true;
        }
        return super.mouseDragged(mx,my,button,dx,dy);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode==256) {
            if (showColorPicker) { showColorPicker=false; return true; }
            if (keystrokeEditMode) { keystrokeEditMode=false; popupTarget=DragTarget.NONE; clearAndInit(); return true; }
            close();
            return true;
        }
        return super.keyPressed(keyCode,scanCode,modifiers);
    }

    @Override
    public void close() { save(); client.setScreen(parent); }

    // ── Per-element size helpers ───────────────────────────────────────────────

    private void addW(DragTarget t, int d) {
        switch(t) {
            case W -> config.wW=Math.max(16,config.wW+d);
            case A -> config.wA=Math.max(16,config.wA+d);
            case S -> config.wS=Math.max(16,config.wS+d);
            case D -> config.wD=Math.max(16,config.wD+d);
            case LMB -> config.wLMB=Math.max(30,config.wLMB+d);
            case RMB -> config.wRMB=Math.max(30,config.wRMB+d);
            case FPS -> config.wFPS=Math.max(30,config.wFPS+d);
        }
    }

    private void addH(DragTarget t, int d) {
        switch(t) {
            case W -> config.hW=Math.max(14,config.hW+d);
            case A -> config.hA=Math.max(14,config.hA+d);
            case S -> config.hS=Math.max(14,config.hS+d);
            case D -> config.hD=Math.max(14,config.hD+d);
            case LMB -> config.hLMB=Math.max(14,config.hLMB+d);
            case RMB -> config.hRMB=Math.max(14,config.hRMB+d);
            case FPS -> config.hFPS=Math.max(12,config.hFPS+d);
        }
    }

    private void addRadius(DragTarget t, int d) {
        switch(t) {
            case W -> config.rW=Math.max(0,Math.min(12,config.rW+d));
            case A -> config.rA=Math.max(0,Math.min(12,config.rA+d));
            case S -> config.rS=Math.max(0,Math.min(12,config.rS+d));
            case D -> config.rD=Math.max(0,Math.min(12,config.rD+d));
            case LMB -> config.rLMB=Math.max(0,Math.min(12,config.rLMB+d));
            case RMB -> config.rRMB=Math.max(0,Math.min(12,config.rRMB+d));
            case FPS -> config.rFPS=Math.max(0,Math.min(12,config.rFPS+d));
        }
    }

    private int[] getWH(DragTarget t) {
        return switch(t) {
            case W -> new int[]{config.wW,config.hW};
            case A -> new int[]{config.wA,config.hA};
            case S -> new int[]{config.wS,config.hS};
            case D -> new int[]{config.wD,config.hD};
            case LMB -
