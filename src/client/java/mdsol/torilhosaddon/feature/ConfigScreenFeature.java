package mdsol.torilhosaddon.feature;

import mdsol.torilhosaddon.TorilhosAddon;
import mdsol.torilhosaddon.config.ModConfigLoader;
import mdsol.torilhosaddon.feature.base.BaseFeature;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ConfigScreenFeature extends BaseFeature {

    public static final KeyBinding OPEN_CONFIG_KEY = new KeyBinding(
            "key.torilhos-addon.openConfigMenu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_N, TorilhosAddon.MOD_CATEGORY);

    @Override
    public void init() {
        KeyBindingHelper.registerKeyBinding(OPEN_CONFIG_KEY);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_CONFIG_KEY.wasPressed()) {
                this.client.setScreen(ModConfigLoader.getConfigScreen());
            }
        });
    }
}
