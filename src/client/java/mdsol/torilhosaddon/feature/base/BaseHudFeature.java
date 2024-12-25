package mdsol.torilhosaddon.feature.base;

import io.wispforest.owo.config.Option;
import mdsol.torilhosaddon.ui.hud.base.BaseHudItem;

public abstract class BaseHudFeature extends BaseToggleableFeature {

    private final BaseHudItem hudItem;

    protected BaseHudFeature(Option.Key configKey, BaseHudItem hudItem) {
        super(configKey);
        this.hudItem = hudItem;

        if (isEnabled()) {
            this.hudItem.enable();
        }
    }

    @Override
    protected void onEnable() {
        // Null check is important here because BaseToggleableFeature calls setEnabled() in the constructor and by then
        // we don't have the hudItem set yet.
        if (hudItem != null) {
            hudItem.enable();
        }
    }

    @Override
    protected void onDisable() {
        if (hudItem != null) {
            hudItem.disable();
        }
    }

    protected BaseHudItem getHudItem() {
        return hudItem;
    }
}
