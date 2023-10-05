package me.drex.message.mixin;

import com.google.gson.JsonElement;
import eu.pb4.placeholders.api.PlaceholderContext;
import me.drex.message.impl.MessageImpl;
import me.drex.message.impl.MessageMod;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Component.Serializer.class)
public abstract class ComponentSerializerMixin {

    @Inject(method = "serialize", at = @At("HEAD"), cancellable = true)
    private static void serializeMessageImpl(Component component, CallbackInfoReturnable<JsonElement> cir) {
        ComponentContents contents = component.getContents();
        if (contents instanceof MessageImpl message) {
            ServerPlayer target = MessageMod.PACKET_LISTENER.get();
            if (target != null) {
                MutableComponent parsedMessage = message.parseMessage(MessageMod.SERVER_INSTANCE, PlaceholderContext.of(target));
                for (Component sibling : component.getSiblings()) {
                    parsedMessage.append(sibling);
                }
                parsedMessage.setStyle(parsedMessage.getStyle().applyTo(component.getStyle()));
                cir.setReturnValue(ComponentSerializerAccessor.callSerialize(parsedMessage));
            }
        }
    }

}
