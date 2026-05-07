package com.keystrokeshud.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;

public class HudConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger("keystrokeshud-config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE = "keystrokeshud.json";

    // Feature toggles
    public boolean showW = true, showA = true, showS = true, showD = true;
    public boolean showLMB = true, showRMB = true, showFPS = true, showCPS = true;

    // Positions
    public float xW=10,yW=100, xA=10,yA=126, xS=36,yS=126, xD=62,yD=126;
    public float xLMB=10,yLMB=152, xRMB=62,yRMB=152, xFPS=10,yFPS=10;

    // Individual sizes for each element
    public int wW=24,hW=24;
    public int wA=24,hA=24;
    public int wS=24,hS=24;
    public int wD=24,hD=24;
    public int wLMB=50,hLMB=24;
    public int wRMB=50,hRMB=24;
    public int wFPS=54,hFPS=18;

    // Individual corner radius for each element
    public int rW=4,rA=4,rS=4,rD=4,rLMB=4,rRMB=4,rFPS=4;

    // Text colors
    public int colorTextW=0xFFFFFFFF, colorTextA=0xFFFFFFFF;
    public int colorTextS=0xFFFFFFFF, colorTextD=0xFFFFFFFF;
    public int colorTextLMB=0xFFFFFFFF, colorTextRMB=0xFFFFFFFF;
    public int colorTextFPS=0xFF00FF88;

    // Border colors per element
    public int borderColorW=0xFF555577, borderColorA=0xFF555577;
    public int borderColorS=0xFF555577, borderColorD=0xFF555577;
    public int borderColorLMB=0xFF555577, borderColorRMB=0xFF555577;
    public int borderColorFPS=0xFF555577;

    // Global colors
    public int colorPressed=0xFF4466FF;
    public int colorIdle=0x99222222;

    // Background & border
    public float backgroundOpacity=0.6f;
    public boolean showBorders=true;
    public int borderThickness=1;

    // Rainbow
    public boolean rainbowMode=false;
    public float rainbowSpeed=1.0f;

    // UI
    public boolean lockPositions=false;
    public boolean snapToGrid=false;
    public int gridSize=8;
    public boolean performanceMode=false;

    // Legacy (kept for compatibility)
    public int keyWidth=24, keyHeight=24;
    public int lmbWidth=50, lmbHeight=24;
    public int fpsWidth=54, fpsHeight=18;
    public int cornerRadius=4;

    public static HudConfig loadOrCreate() {
        Path path = configPath();
        if (path.toFile().exists()) {
            try (Reader r = new FileReader(path.toFile())) {
                HudConfig cfg = GSON.fromJson(r, HudConfig.class);
                if (cfg != null) return cfg;
            } catch (Exception e) {
                LOGGER.warn("Failed to load config: {}", e.getMessage());
            }
        }
        HudConfig cfg = new HudConfig();
        cfg.save();
        return cfg;
    }

    public void save() {
        Path path = configPath();
        try {
            path.getParent().toFile().mkdirs();
            try (Writer w = new FileWriter(path.toFile())) {
                GSON.toJson(this, w);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save config: {}", e.getMessage());
        }
    }

    public void resetLayout() {
        xW=10;yW=100; xA=10;yA=126; xS=36;yS=126; xD=62;yD=126;
        xLMB=10;yLMB=152; xRMB=62;yRMB=152; xFPS=10;yFPS=10;
        save();
    }

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(FILE);
    }
}
