package me.drex.message.mixin.network;

import eu.pb4.placeholders.api.PlaceholderContext;
import me.drex.message.impl.util.NbtLocalizer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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
            target = "Lnet/minecraft/network/FriendlyByteBuf;writeNbt(Lnet/minecraft/nbt/Tag;)Lnet/minecraft/network/FriendlyByteBuf;"
        )
    )
    private Tag localizeItemStack(Tag tag) {
        if (tag != null) {
            ServerPlayer target = PACKET_LISTENER.get();
            if (target == null) return tag;
            NbtLocalizer localizer = new NbtLocalizer((CompoundTag) tag, PlaceholderContext.of(target));
            return localizer.getResult();
        }
        return null;
    }

}
