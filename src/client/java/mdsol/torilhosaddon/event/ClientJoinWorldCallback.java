package mdsol.torilhosaddon.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.world.ClientWorld;

public interface ClientJoinWorldCallback {

    Event<ClientJoinWorldCallback> EVENT =
            EventFactory.createArrayBacked(ClientJoinWorldCallback.class, listeners -> world -> {
                for (var listener : listeners) {
                    listener.onJoin(world);
                }
            });

    void onJoin(ClientWorld world);
}
