package me.drex.message.mixin.network;

import eu.pb4.placeholders.api.PlaceholderContext;
import me.drex.message.impl.MessageMod;
import me.drex.message.impl.util.NbtLocalizer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import static me.drex.message.impl.MessageMod.PACKET_LISTENER;

@Mixin(FriendlyByteBuf.class)
public abstract class FriendlyByteBufMixin {

    @ModifyArg(
            method = "writeItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/FriendlyByteBuf;writeNbt(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/network/FriendlyByteBuf;"
            )
    )
    private CompoundTag localizeItemStack(CompoundTag compoundTag) {
        if (compoundTag != null) {
            ServerPlayer target = PACKET_LISTENER.get();
            if (target == null) return compoundTag;
            NbtLocalizer localizer = new NbtLocalizer(compoundTag, MessageMod.SERVER_INSTANCE, PlaceholderContext.of(target));
            return localizer.getResult();
        }
        return null;
    }

}
