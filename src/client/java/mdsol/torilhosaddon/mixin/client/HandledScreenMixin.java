package mdsol.torilhosaddon.mixin.client;

import mdsol.torilhosaddon.event.HandledScreenRemovedCallback;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public class HandledScreenMixin {

    @Inject(method = "removed", at = @At("HEAD"))
    private void removed(CallbackInfo info) {
        HandledScreenRemovedCallback.EVENT.invoker().onRemoved(((Screen) (Object) this));
    }
}
