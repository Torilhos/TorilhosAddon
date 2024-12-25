package mdsol.torilhosaddon.feature;

import mdsol.torilhosaddon.TorilhosAddon;
import mdsol.torilhosaddon.feature.base.BaseToggleableFeature;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

public class FullBrightFeature extends BaseToggleableFeature {

    private boolean clientStarted = false;

    public FullBrightFeature() {
        super(TorilhosAddon.CONFIG.keys.enableFullBright);
        // Wait for the client to start, otherwise client.options will not be available yet.
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            clientStarted = true;
            setEnabled(TorilhosAddon.CONFIG.enableFullBright());
        });
    }

    @Override
    protected void onEnable() {
        if (clientStarted) {
            client.options.getGamma().setValue(1000.0d);
        }
    }

    @Override
    protected void onDisable() {
        if (clientStarted) {
            client.options.getGamma().setValue(1.0d);
        }
    }
}
