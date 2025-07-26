package mdsol.torilhosaddon.feature.base;

import java.util.function.BooleanSupplier;
import mdsol.torilhosaddon.config.ModConfig;

public abstract class BaseToggleableFeature extends BaseFeature implements ToggleableFeature {

    private final BooleanSupplier configToggleSupplier;
    private boolean enabled;

    protected BaseToggleableFeature(BooleanSupplier configToggleSupplier) {
        super();
        this.configToggleSupplier = configToggleSupplier;
    }

    @Override
    public void init() {
        this.setEnabled(this.configToggleSupplier.getAsBoolean());
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }

        this.enabled = enabled;

        if (enabled) {
            onEnable();
            return;
        }

        onDisable();
    }

    @Override
    public void onConfigChanged(ModConfig config) {
        super.onConfigChanged(config);
        setEnabled(configToggleSupplier.getAsBoolean());
    }

    protected boolean isEnabledAndInWorld() {
        return isEnabled() && isInWorld();
    }

    protected void onEnable() {}

    protected void onDisable() {}
}
