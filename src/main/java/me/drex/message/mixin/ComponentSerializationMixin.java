package me.drex.message.mixin;

import me.drex.message.impl.MessageImpl;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.StringRepresentable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

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
}
