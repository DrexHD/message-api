package me.drex.message.mixin;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import eu.pb4.placeholders.api.PlaceholderContext;
import me.drex.message.api.Message;
import me.drex.message.impl.MessageImpl;
import me.drex.message.impl.MessageMod;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.lang.reflect.Type;

@Mixin(Component.Serializer.class)
public abstract class ComponentSerializerMixin {

    @Shadow
    public abstract JsonElement serialize(Component component, Type type, JsonSerializationContext jsonSerializationContext);

    @Inject(
            method = "serialize(Lnet/minecraft/network/chat/Component;Ljava/lang/reflect/Type;Lcom/google/gson/JsonSerializationContext;)Lcom/google/gson/JsonElement;",
            at = @At("HEAD"),
            cancellable = true
    )
    public void messageApi$serializeCustomMessage(Component component, Type type, JsonSerializationContext jsonSerializationContext, CallbackInfoReturnable<JsonElement> cir) {
        ServerPlayer target = PacketContext.get().getTarget();
        if (component instanceof MutableComponent mutableComponent) {
            ComponentContents contents = mutableComponent.getContents();
            if (contents instanceof MessageImpl message) {
                MutableComponent parsedMessage = message.parseMessage(MessageMod.SERVER_INSTANCE, target != null ? PlaceholderContext.of(target) : null);
                cir.setReturnValue(this.serialize(parsedMessage, parsedMessage.getClass(), jsonSerializationContext));
            }
        }
    }

}
