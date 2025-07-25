package mdsol.torilhosaddon;

import mdsol.torilhosaddon.config.ModConfig;
import mdsol.torilhosaddon.config.ModConfigLoader;
import mdsol.torilhosaddon.feature.Features;
import mdsol.torilhosaddon.feature.base.Feature;
import net.fabricmc.api.ClientModInitializer;

public class TorilhosAddonClient implements ClientModInitializer {

    public static ModConfig config = new ModConfig();

    @Override
    public void onInitializeClient() {
        config = ModConfigLoader.loadConfig();
        ModConfigLoader.registerSaveListener(Features::onConfigChanged);
        Features.FEATURES.forEach(Feature::init);
    }
}
