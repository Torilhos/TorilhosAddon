package mdsol.torilhosaddon.feature;

import mdsol.torilhosaddon.TorilhosAddon;
import mdsol.torilhosaddon.TorilhosAddonClient;
import mdsol.torilhosaddon.feature.base.BaseToggleableFeature;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class PerspectiveFeature extends BaseToggleableFeature {
    public static final KeyBinding THIRD_PERSON_PERSPECTIVE_KEY = new KeyBinding(
            "key.torilhos-addon.toggleThirdPersonBackPerspective",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            TorilhosAddon.MOD_CATEGORY);

    private static final int PITCH_THRESHOLD = 15;
    private boolean inverted = false;

    protected PerspectiveFeature() {
        super(TorilhosAddonClient.config::isAutomaticPerspectiveEnabled);
    }

    @Override
    public void init() {
        super.init();
        KeyBindingHelper.registerKeyBinding(THIRD_PERSON_PERSPECTIVE_KEY);
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
    }

    public void tick(MinecraftClient client) {
        var player = client.player;

        if (!isInGame() || player == null) {
            return;
        }

        var perspective = client.options.getPerspective();

        while (THIRD_PERSON_PERSPECTIVE_KEY.wasPressed()) {
            if (isEnabled()) {
                inverted = !inverted;
                return;
            }

            if (perspective != Perspective.THIRD_PERSON_BACK) {
                client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
                return;
            }

            client.options.setPerspective(Perspective.FIRST_PERSON);
        }

        if (!isEnabled()) {
            return;
        }

        var pitch = player.getPitch();
        var lowPerspective = inverted ? Perspective.FIRST_PERSON : Perspective.THIRD_PERSON_BACK;
        var highPerspective = inverted ? Perspective.THIRD_PERSON_BACK : Perspective.FIRST_PERSON;

        if (pitch > PITCH_THRESHOLD && perspective != lowPerspective) {
            client.options.setPerspective(lowPerspective);
        }

        if (pitch <= PITCH_THRESHOLD && perspective != highPerspective) {
            client.options.setPerspective(highPerspective);
        }
    }
}
