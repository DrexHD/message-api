package me.drex.message.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.serialization.Codec;
import eu.pb4.placeholders.api.PlaceholderContext;
import me.drex.message.impl.MessageImpl;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Function;

import static me.drex.message.impl.MessageMod.PACKET_LISTENER;

@Mixin(ComponentSerialization.class)
public abstract class ComponentSerializationMixin {

    @ModifyArg(
        method = "createCodec",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/chat/ComponentSerialization;createLegacyComponentMatcher([Lnet/minecraft/util/StringRepresentable;Ljava/util/function/Function;Ljava/util/function/Function;Ljava/lang/String;)Lcom/mojang/serialization/MapCodec;"
        ),
        index = 0
    )
    private static StringRepresentable[] addMessageType(StringRepresentable[] original) {
        StringRepresentable[] result = new StringRepresentable[original.length + 1];
        System.arraycopy(original, 0, result, 0, original.length);
        result[original.length] = MessageImpl.TYPE;
        return result;
    }

    @ModifyReturnValue(method = "createCodec", at = @At("RETURN"))
    private static Codec<Component> modifyCodec(Codec<Component> original) {
        return original.xmap(Function.identity(),
            component -> {
                PlaceholderContext context;
                ServerPlayer target = PACKET_LISTENER.get();
                if (target != null) {
                    context = PlaceholderContext.of(target);
                } else {
                    return component;
                }
                return MessageImpl.parseComponent(component, context);
            });
    }

}
