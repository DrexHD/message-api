package me.drex.message.mixin;

import me.drex.message.api.MessageAPI;
import me.drex.message.impl.MessageMod;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Inject(
            method = "runServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;initServer()Z"
            )
    )
    private void captureServerInstance(CallbackInfo info) {
        MessageMod.SERVER_INSTANCE = (MinecraftServer) (Object) this;
    }

    @Inject(method = "reloadResources", at = @At("HEAD"))
    private void reloadMessages(Collection<String> collection, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        MessageAPI.reload();
    }

}
