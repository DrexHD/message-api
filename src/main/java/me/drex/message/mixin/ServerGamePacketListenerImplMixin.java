package me.drex.message.mixin;

import me.drex.message.impl.interfaces.ClientLanguageGetter;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin implements ClientLanguageGetter {

    @Unique
    private String messageApi$language;

    @Inject(method = "handleClientInformation", at = @At(value = "TAIL"))
    public void saveClientLanguage(ServerboundClientInformationPacket clientInfoPacket, CallbackInfo ci) {
        messageApi$language = clientInfoPacket.information().language();
    }

    @Override
    public String getLanguage() {
        return this.messageApi$language;
    }
}
