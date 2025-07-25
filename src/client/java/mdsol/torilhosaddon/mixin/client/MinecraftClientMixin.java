package mdsol.torilhosaddon.mixin.client;

import mdsol.torilhosaddon.event.ClientJoinWorldCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(method = "joinWorld", at = @At("TAIL"))
    private void joinWorld(
            ClientWorld world, DownloadingTerrainScreen.WorldEntryReason worldEntryReason, CallbackInfo ci) {
        ClientJoinWorldCallback.EVENT.invoker().onJoin(world);
    }
}
