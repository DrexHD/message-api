package me.drex.message.impl;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.node.TextNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MessageImpl implements ComponentContents {

    private final String key;
    private final Map<String, Component> placeholders;
    @Nullable
    private final PlaceholderContext staticContext;

    public static final MapCodec<MessageImpl> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.STRING.fieldOf("key").forGetter(MessageImpl::getKey),
        Codec.unboundedMap(Codec.STRING, ComponentSerialization.CODEC).optionalFieldOf("placeholders", Map.of()).forGetter(MessageImpl::getPlaceholders)
    ).apply(instance, (key, placeholders) -> new MessageImpl(key, placeholders, null)));
    //? if <= 1.21.8 {
    public static final ComponentContents.Type<MessageImpl> TYPE = new ComponentContents.Type<>(CODEC, "message");
    //?}

    public MessageImpl(String key, Map<String, Component> placeholders, @Nullable PlaceholderContext staticContext) {
        this.key = key;
        this.placeholders = placeholders;
        this.staticContext = staticContext;
    }

    public MutableComponent parseMessage(MinecraftServer server, @Nullable PlaceholderContext dynamicContext) {
        PlaceholderContext context;
        context = Objects.requireNonNullElseGet(this.staticContext, () -> Objects.requireNonNullElseGet(dynamicContext, () -> PlaceholderContext.of(server)));
        TextNode node = LanguageManager.resolveMessageId(context.player(), this.key);
        Map<String, Component> parsedPlaceholders = new HashMap<>();
        if (this.placeholders != null) {
            this.placeholders.forEach((key1, value) -> parsedPlaceholders.put(key1, parseComponent(value, dynamicContext)));
        }
        return (MutableComponent) node.toText(context.asParserContext().with(LanguageManager.PLACEHOLDERS, parsedPlaceholders::get), true);
    }

    public static Component parseComponent(Component component, PlaceholderContext context) {
        ComponentContents contents = component.getContents();
        if (!(contents instanceof MessageImpl message)) {
            return component;
        }
        MutableComponent parsedMessage = message.parseMessage(MessageMod.SERVER_INSTANCE, context);
        for (Component sibling : component.getSiblings()) {
            parsedMessage.append(parseComponent(sibling, context));
        }
        parsedMessage.setStyle(parsedMessage.getStyle().applyTo(component.getStyle()));
        return parsedMessage;
    }

    @Override
    public <T> @NotNull Optional<T> visit(FormattedText.StyledContentConsumer<T> styledContentConsumer, Style style) {
        try {
            return resolve(null, null, 0).visit(styledContentConsumer, style);
        } catch (Throwable e) {
            return Optional.empty();
        }
    }

    @Override
    public <T> @NotNull Optional<T> visit(FormattedText.ContentConsumer<T> contentConsumer) {
        try {
            return resolve(null, null, 0).visit(contentConsumer);
        } catch (Throwable e) {
            return Optional.empty();
        }
    }

    @Override
    public @NotNull MutableComponent resolve(@Nullable CommandSourceStack src, @Nullable Entity entity, int i) throws CommandSyntaxException {
        PlaceholderContext context = null;
        if (src != null) {
            context = PlaceholderContext.of(src);
        } else {
            if (entity != null) {
                context = PlaceholderContext.of(entity);
            }
        }

        MutableComponent component = parseMessage(MessageMod.SERVER_INSTANCE, context);
        MutableComponent result = MutableComponent.create(component.getContents()).withStyle(component.getStyle());
        for (Component sibling : component.getSiblings()) {
            result.append(ComponentUtils.updateForEntity(src, sibling, entity, i + 1));
        }
        return result;
    }

    //? if >= 1.21.9 {
    /*@Override
    public MapCodec<? extends ComponentContents> codec() {
        return CODEC;
    }
    *///?} else {
    @Override
    public Type<?> type() {
        return TYPE;
    }
    //?}

    public String getKey() {
        return key;
    }

    public Map<String, Component> getPlaceholders() {
        return placeholders;
    }


    public MutableComponent toText() {
        return MutableComponent.create(this);
    }

    @Override
    public String toString() {
        return "message{key='" + this.key + "'" + ", placeholders=" + this.placeholders + "}";
    }

}
