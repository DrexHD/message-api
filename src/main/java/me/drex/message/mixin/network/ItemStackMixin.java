package me.drex.message.mixin.network;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import eu.pb4.placeholders.api.PlaceholderContext;
import me.drex.message.impl.util.NbtLocalizer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static me.drex.message.impl.MessageMod.PACKET_LISTENER;

@Mixin(targets = "net/minecraft/world/item/ItemStack$1")
public abstract class ItemStackMixin {

    @WrapOperation(
        method = "encode(Lnet/minecraft/network/RegistryFriendlyByteBuf;Lnet/minecraft/world/item/ItemStack;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;getTag()Lnet/minecraft/nbt/CompoundTag;"
        )
    )
    public CompoundTag modifyTag(ItemStack instance, Operation<CompoundTag> original) {
        CompoundTag tag = original.call(instance);
        if (tag != null) {
            ServerPlayer target = PACKET_LISTENER.get();
            if (target == null) return tag;
            NbtLocalizer localizer = new NbtLocalizer(tag, PlaceholderContext.of(target));
            return localizer.getResult();
        }
        return null;
    }

}
