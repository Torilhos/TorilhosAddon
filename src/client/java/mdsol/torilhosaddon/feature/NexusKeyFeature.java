package mdsol.torilhosaddon.feature;

import mdsol.torilhosaddon.feature.base.BaseFeature;
import mdsol.torilhosaddon.util.Items;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

public class NexusKeyFeature extends BaseFeature {

    private static final KeyBinding NEXUS_KEY = new KeyBinding(
            "key.torilhos-addon.nexus",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "category.torilhos-addon"
    );

    public NexusKeyFeature() {
        super();
        KeyBindingHelper.registerKeyBinding(NEXUS_KEY);
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
    }

    private void tick(@NotNull MinecraftClient client) {
        if (!NEXUS_KEY.wasPressed()
                || NEXUS_KEY.isUnbound()
                || client.player == null
                || client.interactionManager == null
        ) {
            return;
        }

        var slot = -1;
        for (var i = 8; i > -1; i--) {
            var stack = client.player.getInventory().getStack(i);

            if (Items.isNexus(stack)) {
                slot = i;
                break;
            }
        }

        if (slot == -1) {
            return;
        }

        client.player.getInventory().selectedSlot = slot;
        client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
    }
}
