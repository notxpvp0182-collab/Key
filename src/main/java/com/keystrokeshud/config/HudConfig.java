package com.keystrokeshud.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;

/**
 * HudConfig — Persistent configuration model.
 *
 * All positional, colour, and feature-toggle data lives here.
 * Serialised to JSON via Gson; loaded automatically on startup.
 *
 * Design note: primitive fields are used deliberately so Gson can
 * handle them without custom adapters, keeping the dependency footprint
 * minimal and the load path exception-safe.
 */
public class HudConfig {

    // ─── Internal ────────────────────────────────────────────────────────────
    private static final Logger  LOGGER  = LoggerFactory.getLogger("keystrokeshud-config");
    private static final Gson    GSON    = new GsonBuilder().setPrettyPrinting().create();
    private static final String  FILE    = "keystrokeshud.json";

    // ─── Feature toggles ─────────────────────────────────────────────────────
    public boolean showW    = true;
    public boolean showA    = true;
    public boolean showS    = true;
    public boolean showD    = true;
    public boolean showLMB  = true;
    public boolean showRMB  = true;
    public boolean showFPS  = true;
    public boolean showCPS  = true;  // show CPS value inside LMB/RMB keys

    // ─── Positions (pixels from top-left) ────────────────────────────────────
    // Default layout: compact keystrokes cluster bottom-left + FPS top-left.
    public float xW    = 10; public float yW    = 100;
    public float xA    = 10; public float yA    = 126;
    public float xS    = 36; public float yS    = 126;
    public float xD    = 62; public float yD    = 126;
    public float xLMB  = 10; public float yLMB  = 152;
    public float xRMB  = 62; public float yRMB  = 152;
    public float xFPS  = 10; public float yFPS  = 10;

    // ─── Colours (ARGB int, packed) ───────────────────────────────────────────
    // Text colours per element
    public int colorTextW    = 0xFFFFFFFF;
    public int colorTextA    = 0xFFFFFFFF;
    public int colorTextS    = 0xFFFFFFFF;
    public int colorTextD    = 0xFFFFFFFF;
    public int colorTextLMB  = 0xFFFFFFFF;
    public int colorTextRMB  = 0xFFFFFFFF;
    public int colorTextFPS  = 0xFF00FF88;

    // Pressed background colour (all elements share one pressed colour for cohesion)
    public int colorPressed = 0xFF4466FF;

    // Idle background colour
    public int colorIdle    = 0x99222222;

    // Border colour per element
    public int borderColorW    = 0xFF555577;
    public int borderColorA    = 0xFF555577;
    public int borderColorS    = 0xFF555577;
    public int borderColorD    = 0xFF555577;
    public int borderColorLMB  = 0xFF555577;
    public int borderColorRMB  = 0xFF555577;
    public int borderColorFPS  = 0xFF555577;

    // ─── Background / Border settings ────────────────────────────────────────
    public float backgroundOpacity = 0.6f;   // 0.0 – 1.0
    public boolean showBorders      = true;
    public int    borderThickness   = 1;      // pixels

    // ─── RGB / Rainbow mode ───────────────────────────────────────────────────
    public boolean rainbowMode       = false;
    public float   rainbowSpeed      = 1.0f;  // cycles per second

    // ─── UI Lock / Grid snap ──────────────────────────────────────────────────
    public boolean lockPositions = false;
    public boolean snapToGrid    = false;
    public int     gridSize      = 8;         // pixels

    // ─── Performance mode ─────────────────────────────────────────────────────
    public boolean performanceMode  = false;  // disables animations & rainbow

    // ─── Element sizes ────────────────────────────────────────────────────────
    public int keyWidth   = 24;
    public int keyHeight  = 24;
    public int lmbWidth   = 50;
    public int lmbHeight  = 24;
    public int fpsWidth   = 54;
    public int fpsHeight  = 18;

    // ─── Corner radius ────────────────────────────────────────────────────────
    public int cornerRadius = 4;

    // ─── IO ───────────────────────────────────────────────────────────────────

    /** Load config from disk, or return a fresh default instance. */
    public static HudConfig loadOrCreate() {
        Path path = configPath();
        if (path.toFile().exists()) {
            try (Reader r = new FileReader(path.toFile())) {
                HudConfig cfg = GSON.fromJson(r, HudConfig.class);
                if (cfg != null) {
                    LOGGER.info("[KeystrokesHUD] Config loaded from {}", path);
                    return cfg;
                }
            } catch (Exception e) {
                LOGGER.warn("[KeystrokesHUD] Failed to load config, using defaults: {}", e.getMessage());
            }
        }
        HudConfig cfg = new HudConfig();
        cfg.save();
        return cfg;
    }

    /** Persist config to disk. Called after any settings change. */
    public void save() {
        Path path = configPath();
        try {
            //noinspection ResultOfMethodCallIgnored
            path.getParent().toFile().mkdirs();
            try (Writer w = new FileWriter(path.toFile())) {
                GSON.toJson(this, w);
            }
        } catch (Exception e) {
            LOGGER.error("[KeystrokesHUD] Failed to save config: {}", e.getMessage());
        }
    }

    /** Reset all element positions to their defaults. */
    public void resetLayout() {
        xW = 10; yW = 100;
        xA = 10; yA = 126;
        xS = 36; yS = 126;
        xD = 62; yD = 126;
        xLMB = 10; yLMB = 152;
        xRMB = 62; yRMB = 152;
        xFPS = 10; yFPS = 10;
        save();
    }

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(FILE);
    }
}
