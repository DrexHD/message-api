package me.drex.message.mixin;

import com.mojang.authlib.GameProfile;
import me.drex.message.impl.LanguageManager;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerConfigurationPacketListenerImpl.class)
public abstract class ServerConfigurationPacketListenerImplMixin {

    @Shadow
    @Final
    private GameProfile gameProfile;

    @Inject(method = "handleClientInformation", at = @At(value = "TAIL"))
    public void saveClientLanguage(ServerboundClientInformationPacket clientInfoPacket, CallbackInfo ci) {
        LanguageManager.updatePlayerLanguage(gameProfile./*?if <= 1.21.8 {*/getId() /*? } else {*/ /*id()*/ /*?}*/, clientInfoPacket.information().language());
    }
}
