package me.drex.message.api;

import eu.pb4.placeholders.api.PlaceholderContext;
import me.drex.message.impl.LocalizedMessageBuilderImpl;
import me.drex.message.impl.MessageImpl;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface LocalizedMessage {

    static LocalizedMessageBuilder builder(String key) {
        return new LocalizedMessageBuilderImpl(key);
    }

    static MutableComponent localized(String key) {
        return localized(key, Map.of());
    }

    static MutableComponent localized(String key, Map<String, Component> placeholders) {
        return localized(key, placeholders, null);
    }

    static MutableComponent localized(String key, @Nullable PlaceholderContext staticContext) {
        return localized(key, Map.of(), staticContext);
    }

    static MutableComponent localized(String key, Map<String, Component> placeholders, @Nullable PlaceholderContext staticContext) {
        return new MessageImpl(key, placeholders, staticContext).toText();
    }
}
