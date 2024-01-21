package me.drex.message.mixin.network;

import io.netty.channel.Channel;
import me.drex.message.impl.interfaces.ConnectionHolder;
import net.minecraft.network.Connection;
import net.minecraft.network.ProtocolInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public abstract class ConnectionMixin {

    @Shadow private Channel channel;

    @Inject(
        method = "setupOutboundProtocol",
        at = @At(
            value = "TAIL"
        )
    )
    private void setConnection(ProtocolInfo<?> protocolInfo, CallbackInfo ci) {
        ((ConnectionHolder) this.channel.pipeline().get("encoder")).message_api$setConnection((Connection) (Object) this);
    }

}
