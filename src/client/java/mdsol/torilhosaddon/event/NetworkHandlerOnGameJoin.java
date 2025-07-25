package mdsol.torilhosaddon.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface NetworkHandlerOnGameJoin {

    Event<NetworkHandlerOnGameJoin> EVENT =
            EventFactory.createArrayBacked(NetworkHandlerOnGameJoin.class, listeners -> () -> {
                for (var listener : listeners) {
                    listener.onGameJoin();
                }
            });

    void onGameJoin();
}
