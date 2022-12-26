package me.drex.message.api;

import eu.pb4.placeholders.api.*;
import eu.pb4.placeholders.api.node.parent.ParentTextNode;
import me.drex.message.impl.LanguageManager;
import me.drex.message.impl.MessageImpl;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Message {

    /**
     * Copy of Placeholders#PLACEHOLDER_GETTER, because it is unfortunately private
     */
    public static final Placeholders.PlaceholderGetter DEFAULT_PLACEHOLDER_GETTER = new Placeholders.PlaceholderGetter() {
        public PlaceholderHandler getPlaceholder(String placeholder) {
            return Placeholders.getPlaceholders().get(ResourceLocation.tryParse(placeholder));
        }
        public boolean isContextOptional() {
            return false;
        }
    };

    public static MutableComponent message(String key) {
        return new MessageImpl(key, Collections.emptyMap(), Collections.singletonList(DEFAULT_PLACEHOLDER_GETTER), null).toText();
    }

    public static MutableComponent message(String key, Map<String, Component> placeholders) {
        return new MessageImpl(key, placeholders, Collections.singletonList(DEFAULT_PLACEHOLDER_GETTER), null).toText();
    }

    public static MutableComponent message(String key, @Nullable PlaceholderContext staticContext) {
        return new MessageImpl(key, Collections.emptyMap(), Collections.singletonList(DEFAULT_PLACEHOLDER_GETTER), staticContext).toText();
    }

    public static MutableComponent message(String key, Map<String, Component> placeholders, @Nullable PlaceholderContext context) {
        return new MessageImpl(key, placeholders, Collections.singletonList(DEFAULT_PLACEHOLDER_GETTER), context).toText();
    }

    public static MutableComponent message(String key, Map<String, Component> placeholders, List<Placeholders.PlaceholderGetter> getters, @Nullable PlaceholderContext context) {
        return new MessageImpl(key, placeholders, getters, context).toText();
    }

}
