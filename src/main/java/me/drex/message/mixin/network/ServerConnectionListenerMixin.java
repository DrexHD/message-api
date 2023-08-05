package me.drex.message.mixin.network;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import me.drex.message.impl.interfaces.ConnectionHolder;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(targets = "net/minecraft/server/network/ServerConnectionListener$1")
public abstract class ServerConnectionListenerMixin {

    @Inject(
        method = "initChannel",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/List;add(Ljava/lang/Object;)Z"
        ), locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void setConnection(Channel channel, CallbackInfo ci, ChannelPipeline channelPipeline, int rateLimit, Connection connection) {
        ((ConnectionHolder) channel.pipeline().get("encoder")).message_api$setConnection(connection);
    }

}
