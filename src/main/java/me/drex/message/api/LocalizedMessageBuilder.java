package me.drex.message.api;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface LocalizedMessageBuilder {

    LocalizedMessageBuilder addPlaceholder(String key, boolean bool);

    LocalizedMessageBuilder addPlaceholder(String key, Number number);

    LocalizedMessageBuilder addPlaceholder(String key, String string);

    LocalizedMessageBuilder addPlaceholder(String key, Component component);

    LocalizedMessageBuilder addPlaceholders(Map<String, Component> placeholders);

    LocalizedMessageBuilder addPlaceholderGetter(Placeholders.PlaceholderGetter placeholderGetter);

    LocalizedMessageBuilder setStaticContext(@Nullable PlaceholderContext staticContext);

    MutableComponent build();

}
