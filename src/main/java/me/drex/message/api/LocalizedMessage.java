package me.drex.message.api;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import me.drex.message.impl.MessageImpl;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

import static eu.pb4.placeholders.api.Placeholders.DEFAULT_PLACEHOLDER_GETTER;

public interface LocalizedMessage {

    static MutableComponent localized(String key) {
        return localized(key, Map.of());
    }

    static MutableComponent localized(String key, Map<String, Component> placeholders) {
        return localized(key, placeholders, null);
    }

    static MutableComponent localized(String key, @Nullable PlaceholderContext staticContext) {
        return localized(key, Map.of(), List.of(DEFAULT_PLACEHOLDER_GETTER), staticContext);
    }

    static MutableComponent localized(String key, Map<String, Component> placeholders, @Nullable PlaceholderContext context) {
        return localized(key, placeholders, List.of(DEFAULT_PLACEHOLDER_GETTER), context);
    }

    static MutableComponent localized(String key, Map<String, Component> placeholders, List<Placeholders.PlaceholderGetter> getters, @Nullable PlaceholderContext context) {
        return new MessageImpl(key, placeholders, getters, context).toText();
    }

}
