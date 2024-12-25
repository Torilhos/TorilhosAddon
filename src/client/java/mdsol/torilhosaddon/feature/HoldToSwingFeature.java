package mdsol.torilhosaddon.feature;

import mdsol.torilhosaddon.TorilhosAddon;
import mdsol.torilhosaddon.feature.base.BaseToggleableFeature;
import mdsol.torilhosaddon.util.Items;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Hand;

public class HoldToSwingFeature extends BaseToggleableFeature {

    public HoldToSwingFeature() {
        super(TorilhosAddon.CONFIG.keys.enableHoldToSwing);
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
    }

    public void tick(MinecraftClient client) {
        if (!isEnabled() || !client.options.attackKey.isPressed() || client.player == null) {
            return;
        }

        var currentWeapon = Items.getCurrentPlayerWeapon();

        if (currentWeapon.isEmpty()) {
            return;
        }

        // We try to swing a bit earlier to compensate for ping. Using a static value for the delay since dynamically
        // compensating for the real ping value would add too much complexity, and with a ping over .2 we have other
        // problems anyway.
        if (client.player.getItemCooldownManager().getCooldownProgress(currentWeapon, 0.0f) > 0.2f) {
            return;
        }

        client.player.swingHand(Hand.MAIN_HAND);
    }
}
