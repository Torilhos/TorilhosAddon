package mdsol.torilhosaddon.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.event.ConfigSerializeEvent;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.minecraft.client.gui.screen.Screen;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ModConfigLoader {

    private ModConfigLoader() {}

    public static ModConfig loadConfig() {
        return AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new)
                .getConfig();
    }

    public static void registerSaveListener(ConfigSerializeEvent.Save<ModConfig> listener) {
        AutoConfig.getConfigHolder(ModConfig.class).registerSaveListener(listener);
    }

    public static Screen getConfigScreen(@Nullable Screen parent) {
        @SuppressWarnings("nullness") // parent screen can be null, but is not annotated as such.
        var configScreen = AutoConfig.getConfigScreen(ModConfig.class, parent).get();
        return configScreen;
    }

    public static Screen getConfigScreen() {
        return getConfigScreen(null);
    }
}
