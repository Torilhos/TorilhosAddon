package mdsol.torilhosaddon.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.screen.Screen;

public interface HandledScreenRemovedCallback {

    Event<HandledScreenRemovedCallback> EVENT =
            EventFactory.createArrayBacked(HandledScreenRemovedCallback.class, listeners -> screen -> {
                for (var listener : listeners) {
                    listener.onRemoved(screen);
                }
            });

    void onRemoved(Screen screen);
}
