package com.keystrokeshud;

import com.keystrokeshud.config.HudConfig;
import com.keystrokeshud.hud.HudManager;
import com.keystrokeshud.input.InputTracker;
import com.keystrokeshud.input.FpsTracker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * KeystrokesHUD — Client-side entrypoint.
 * Registers all hooks, keybinds, and initialises subsystems.
 */
public class KeystrokesHudClient implements ClientModInitializer {

    public static final String MOD_ID = "keystrokeshud";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /** Keybind to open the HUD configuration menu. */
    public static KeyBinding openMenuKey;

    /** Singleton references used by renderers and screens. */
    public static HudConfig config;
    public static HudManager hudManager;
    public static InputTracker inputTracker;
    public static FpsTracker fpsTracker;

    @Override
    public void onInitializeClient() {
        LOGGER.info("[KeystrokesHUD] Initialising…");

        // 1. Load / create configuration from disk.
        config = HudConfig.loadOrCreate();

        // 2. Initialise subsystems.
        fpsTracker  = new FpsTracker();
        inputTracker = new InputTracker();
        hudManager  = new HudManager(config, inputTracker, fpsTracker);

        // 3. Register the menu keybind (default: K).
        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.keystrokeshud.open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                "category.keystrokeshud"
        ));

        // 4. Per-tick update (20 Hz) — used for CPS decay & other cheap updates.
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            hudManager.onTick(client);

            // Open menu when keybind is pressed.
            if (openMenuKey.wasPressed()) {
                client.setScreen(new com.keystrokeshud.screen.HudConfigScreen(client.currentScreen));
            }
        });

        // 5. Register HUD renderer — called every rendered frame.
        HudRenderCallback.EVENT.register((drawContext, tickDelta) ->
                hudManager.render(drawContext, tickDelta));

        LOGGER.info("[KeystrokesHUD] Ready.");
    }
}
