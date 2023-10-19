package me.drex.message.mixin;

import com.mojang.serialization.Codec;
import eu.pb4.placeholders.api.PlaceholderContext;
import me.drex.message.impl.MessageImpl;
import me.drex.message.impl.MessageMod;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

import static me.drex.message.impl.MessageMod.PACKET_LISTENER;

@Mixin(ComponentSerialization.class)
public abstract class ComponentSerializationMixin {

    @ModifyArg(
        method = "createCodec",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/chat/ComponentSerialization;createLegacyComponentMatcher([Lnet/minecraft/util/StringRepresentable;Ljava/util/function/Function;Ljava/util/function/Function;)Lcom/mojang/serialization/MapCodec;"
        ),
        index = 0
    )
    private static StringRepresentable[] addMessageType(StringRepresentable[] original) {
        StringRepresentable[] result = new StringRepresentable[original.length + 1];
        System.arraycopy(original, 0, result, 0, original.length);
        result[original.length] = MessageImpl.TYPE;
        return result;
    }

    @Inject(method = "createCodec", at = @At("RETURN"), cancellable = true)
    private static void modifyCodec(Codec<Component> codec, CallbackInfoReturnable<Codec<Component>> cir) {
        cir.setReturnValue(
            cir.getReturnValue().xmap(Function.identity(),
                component -> {
                    ComponentContents contents = component.getContents();
                    if (!(contents instanceof MessageImpl message)) {
                        return component;
                    }
                    ServerPlayer target = PACKET_LISTENER.get();
                    if (target == null) {
                        return component;
                    }
                    MutableComponent parsedMessage = message.parseMessage(MessageMod.SERVER_INSTANCE, PlaceholderContext.of(target));
                    for (Component sibling : component.getSiblings()) {
                        parsedMessage.append(sibling);
                    }
                    parsedMessage.setStyle(parsedMessage.getStyle().applyTo(component.getStyle()));
                    return parsedMessage;
                }));
    }
}
