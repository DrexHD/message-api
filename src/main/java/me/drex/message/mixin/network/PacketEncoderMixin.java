package me.drex.message.mixin.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import me.drex.message.impl.MessageMod;
import me.drex.message.impl.interfaces.ConnectionHolder;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PacketEncoder.class)
public abstract class PacketEncoderMixin implements ConnectionHolder {

    @Unique
    private Connection message_api$connection;

    @Inject(
        method = "encode(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;Lio/netty/buffer/ByteBuf;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/protocol/Packet;write(Lnet/minecraft/network/FriendlyByteBuf;)V",
            shift = At.Shift.BEFORE
        )
    )
    private void setPacketListener(ChannelHandlerContext channelHandlerContext, Packet<?> packet, ByteBuf byteBuf, CallbackInfo ci) {
        if (message_api$connection.getPacketListener() instanceof ServerGamePacketListenerImpl listener) {
            MessageMod.PACKET_LISTENER.set(listener.getPlayer());
        }
    }

    @Inject(
        method = "encode(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;Lio/netty/buffer/ByteBuf;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/protocol/Packet;write(Lnet/minecraft/network/FriendlyByteBuf;)V",
            shift = At.Shift.AFTER
        )
    )
    private void clearPacketListener(ChannelHandlerContext channelHandlerContext, Packet<?> packet, ByteBuf byteBuf, CallbackInfo ci) {
        MessageMod.PACKET_LISTENER.remove();
    }

    @Override
    public void message_api$setConnection(Connection connection) {
        this.message_api$connection = connection;
    }
}
