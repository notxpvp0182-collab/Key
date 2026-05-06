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

public class KeystrokesHudClient implements ClientModInitializer {

    public static final String MOD_ID = "keystrokeshud";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static KeyBinding openMenuKey;
    public static HudConfig config;
    public static HudManager hudManager;
    public static InputTracker inputTracker;
    public static FpsTracker fpsTracker;

    @Override
    public void onInitializeClient() {
        LOGGER.info("[KeystrokesHUD] Initialising...");

        config = HudConfig.loadOrCreate();
        fpsTracker = new FpsTracker();
        inputTracker = new InputTracker();
        hudManager = new HudManager(config, inputTracker, fpsTracker);

        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.keystrokeshud.open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                "category.keystrokeshud"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            hudManager.onTick(client);
            if (openMenuKey.wasPressed()) {
                client.setScreen(new com.keystrokeshud.screen.HudConfigScreen(client.currentScreen));
            }
        });

        HudRenderCallback.EVENT.register((drawContext, tickCounter) ->
                hudManager.render(drawContext, tickCounter));

        LOGGER.info("[KeystrokesHUD] Ready.");
    }
}
