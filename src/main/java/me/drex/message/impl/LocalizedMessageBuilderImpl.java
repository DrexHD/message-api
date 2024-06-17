package me.drex.message.impl;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import me.drex.message.api.LocalizedMessageBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class LocalizedMessageBuilderImpl implements LocalizedMessageBuilder {

    private final String key;
    private final Map<String, Component> placeholders = new HashMap<>();
    private final List<Placeholders.PlaceholderGetter> placeholderGetters = new LinkedList<>();
    @Nullable
    private PlaceholderContext staticContext = null;

    public LocalizedMessageBuilderImpl(String key) {
        this.key = key;
    }

    @Override
    public LocalizedMessageBuilder addPlaceholder(String key, boolean bool) {
        this.placeholders.put(key, Component.literal(String.valueOf(bool)));
        return this;
    }

    @Override
    public LocalizedMessageBuilder addPlaceholder(String key, Number number) {
        this.placeholders.put(key, Component.literal(String.valueOf(number)));
        return this;
    }

    @Override
    public LocalizedMessageBuilder addPlaceholder(String key, String string) {
        this.placeholders.put(key, Component.literal(string));
        return this;
    }

    @Override
    public LocalizedMessageBuilder addPlaceholder(String key, Component component) {
        this.placeholders.put(key, component);
        return this;
    }

    @Override
    public LocalizedMessageBuilder addPlaceholders(Map<String, Component> placeholders) {
        this.placeholders.putAll(placeholders);
        return this;
    }

    @Override
    public LocalizedMessageBuilder addPlaceholderGetter(Placeholders.PlaceholderGetter placeholderGetter) {
        this.placeholderGetters.add(placeholderGetter);
        return this;
    }

    @Override
    public LocalizedMessageBuilder setStaticContext(@Nullable PlaceholderContext staticContext) {
        this.staticContext = staticContext;
        return this;
    }

    @Override
    public MutableComponent build() {
        return new MessageImpl(key, placeholders, placeholderGetters, staticContext).toText();

    }
}
