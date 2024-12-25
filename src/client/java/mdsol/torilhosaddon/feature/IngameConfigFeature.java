package mdsol.torilhosaddon.feature;

import mdsol.torilhosaddon.feature.base.BaseFeature;
import mdsol.torilhosaddon.ui.screen.ConfigScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class IngameConfigFeature extends BaseFeature {

    public static final KeyBinding OPEN_CONFIG_KEY = new KeyBinding(
            "key.torilhos-addon.openConfigMenu",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_N,
            "category.torilhos-addon"
    );

    public IngameConfigFeature() {
        KeyBindingHelper.registerKeyBinding(OPEN_CONFIG_KEY);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_CONFIG_KEY.wasPressed()) {
                client.setScreen(new ConfigScreen());
            }
        });
    }
}
