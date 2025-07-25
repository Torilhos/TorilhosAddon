package mdsol.torilhosaddon.feature.base;

import mdsol.torilhosaddon.config.ModConfig;

public interface Feature {
    void init();

    void onConfigChanged(ModConfig config);
}
