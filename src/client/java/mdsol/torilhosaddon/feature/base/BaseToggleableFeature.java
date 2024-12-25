package mdsol.torilhosaddon.feature.base;

import io.wispforest.owo.config.Option;
import mdsol.torilhosaddon.TorilhosAddon;
import mdsol.torilhosaddon.events.ClientDisconnectedCallback;
import mdsol.torilhosaddon.events.NetworkHandlerOnGameJoin;
import mdsol.torilhosaddon.util.Configs;

import java.util.Objects;

public abstract class BaseToggleableFeature extends BaseFeature implements ToggleableFeature {

    private boolean enabled;

    protected BaseToggleableFeature(Option.Key configKey) {
        super();
        Configs.bindFeatureToggle(configKey, this);
        ClientDisconnectedCallback.EVENT.register(() -> setEnabled(false));
        NetworkHandlerOnGameJoin.EVENT.register(() -> setEnabled((Boolean) Objects.requireNonNull(TorilhosAddon.CONFIG.optionForKey(configKey)).value()));
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled || client.world == null) {
            return;
        }

        this.enabled = enabled;

        if (enabled) {
            onEnable();
            return;
        }

        onDisable();
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }
}
