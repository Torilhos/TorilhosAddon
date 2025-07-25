package mdsol.torilhosaddon.ui.hud;

import net.minecraft.client.MinecraftClient;

public abstract class BaseComponent implements Component {
    protected final MinecraftClient client;

    protected BaseComponent() {
        this.client = MinecraftClient.getInstance();
    }
}
