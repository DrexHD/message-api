package me.drex.message.mixin.network;

import com.llamalad7.mixinextras.sugar.Local;
import io.netty.channel.Channel;
import me.drex.message.impl.interfaces.ConnectionHolder;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/server/network/ServerConnectionListener$1")
public abstract class ServerConnectionListenerMixin {

    @Inject(
        method = "initChannel",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/List;add(Ljava/lang/Object;)Z"
        )
    )
    private void setConnection(Channel channel, CallbackInfo ci, @Local Connection connection) {
        ((ConnectionHolder) channel.pipeline().get("encoder")).message_api$setConnection(connection);
    }

}
