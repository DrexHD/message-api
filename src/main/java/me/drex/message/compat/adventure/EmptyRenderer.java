package me.drex.message.compat.adventure;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.VirtualComponentRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

public class EmptyRenderer implements VirtualComponentRenderer<Void> {
    @Override
    public @UnknownNullability ComponentLike apply(@NotNull Void context) {
        return Component.empty();
    }
}
