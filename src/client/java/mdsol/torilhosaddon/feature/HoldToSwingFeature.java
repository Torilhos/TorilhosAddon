package mdsol.torilhosaddon.feature;

import mdsol.torilhosaddon.TorilhosAddonClient;
import mdsol.torilhosaddon.feature.base.BaseToggleableFeature;
import mdsol.torilhosaddon.util.ItemUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Hand;

public class HoldToSwingFeature extends BaseToggleableFeature {

    public HoldToSwingFeature() {
        super(TorilhosAddonClient.config::isHoldToSwingEnabled);
    }

    @Override
    public void init() {
        super.init();
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
    }

    public void tick(MinecraftClient client) {
        var player = client.player;

        if (!isEnabledAndInGame() || player == null || !client.options.attackKey.isPressed()) {
            return;
        }

        var currentWeapon = ItemUtils.getCurrentPlayerWeapon();

        if (currentWeapon.isEmpty()) {
            return;
        }

        // We try to swing a bit earlier to compensate for ping. Using a static value for the delay since dynamically
        // compensating for the real ping value would add too much complexity, and with a ping over .2 we have other
        // problems anyway.
        if (player.getItemCooldownManager().getCooldownProgress(currentWeapon, 0.0f) > 0.2f) {
            return;
        }

        player.swingHand(Hand.MAIN_HAND);
    }
}
