package me.drex.message.mixin.network;

import com.mojang.serialization.Codec;
import eu.pb4.placeholders.api.PlaceholderContext;
import me.drex.message.impl.MessageImpl;
import me.drex.message.impl.util.ComponentUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.component.ItemLore;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import static me.drex.message.impl.MessageMod.PACKET_LISTENER;

@Mixin(ItemLore.class)
public abstract class ItemLoreMixin {

    @ModifyArg(
        method = "<clinit>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/codec/StreamCodec;map(Ljava/util/function/Function;Ljava/util/function/Function;)Lnet/minecraft/network/codec/StreamCodec;"
        ), index = 1
    )
    private static Function<ItemLore, List<Component>> parseLoreNewLines(Function<ItemLore, List<Component>> from) {
        return itemLore -> {
            PlaceholderContext context;
            ServerPlayer target = PACKET_LISTENER.get();
            if (target != null) {
                context = PlaceholderContext.of(target);
                List<Component> result = new LinkedList<>();
                for (Component line : itemLore.lines()) {
                    // We need to parse lore manually, to apply our new line feature to the parsed components
                    ComponentUtil.parseNewLines(MessageImpl.parseComponent(line, context), result);
                }
                return result;
            }
            return itemLore.lines();
        };
    }

}
