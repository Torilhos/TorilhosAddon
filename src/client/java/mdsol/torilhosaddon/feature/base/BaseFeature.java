package mdsol.torilhosaddon.feature.base;

import mdsol.torilhosaddon.config.ModConfig;
import net.minecraft.client.MinecraftClient;

public abstract class BaseFeature implements Feature {

    protected final MinecraftClient client;

    protected BaseFeature() {
        client = MinecraftClient.getInstance();
    }

    protected boolean isInWorld() {
        return client.player != null && client.world != null;
    }

    @Override
    public void onConfigChanged(ModConfig config) {}
}
