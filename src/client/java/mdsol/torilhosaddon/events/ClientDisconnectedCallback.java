package mdsol.torilhosaddon.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface ClientDisconnectedCallback {

    Event<ClientDisconnectedCallback> EVENT = EventFactory.createArrayBacked(ClientDisconnectedCallback.class, listeners -> () -> {
        for (var listener : listeners) {
            listener.onDisconnected();
        }
    });

    void onDisconnected();
}
