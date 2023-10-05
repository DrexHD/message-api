package me.drex.message.mixin;

import com.google.gson.JsonElement;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Component.Serializer.class)
public interface ComponentSerializerAccessor {
    @Invoker
    static JsonElement callSerialize(Component component) {
        throw new UnsupportedOperationException();
    }
}
