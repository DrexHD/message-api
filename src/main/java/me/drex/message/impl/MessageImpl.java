package me.drex.message.impl;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.node.TextNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static eu.pb4.placeholders.api.Placeholders.DEFAULT_PLACEHOLDER_GETTER;

public class MessageImpl implements ComponentContents {

    private final String key;
    private final Map<String, Component> placeholders;
    private final List<Placeholders.PlaceholderGetter> getters;
    @Nullable
    private final PlaceholderContext staticContext;

    public static final MapCodec<MessageImpl> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.STRING.fieldOf("key").forGetter(MessageImpl::getKey),
        Codec.unboundedMap(Codec.STRING, ComponentSerialization.CODEC).optionalFieldOf("placeholders", Map.of()).forGetter(MessageImpl::getPlaceholders)
    ).apply(instance, (key, placeholders) -> new MessageImpl(key, placeholders, List.of(DEFAULT_PLACEHOLDER_GETTER), null)));
    public static final ComponentContents.Type<MessageImpl> TYPE = new ComponentContents.Type<>(CODEC, "message");

    public MessageImpl(String key, Map<String, Component> placeholders, List<Placeholders.PlaceholderGetter> getters, @Nullable PlaceholderContext staticContext) {
        this.key = key;
        this.placeholders = placeholders;
        this.getters = getters;
        this.staticContext = staticContext;
    }

    public MutableComponent parseMessage(MinecraftServer server, @Nullable PlaceholderContext dynamicContext) {
        PlaceholderContext context;
        context = Objects.requireNonNullElseGet(this.staticContext, () -> Objects.requireNonNullElseGet(dynamicContext, () -> PlaceholderContext.of(server)));
        TextNode node = LanguageManager.resolveMessageId(context.player(), this.key);
        if (this.placeholders != null) {
            node = Placeholders.parseNodes(node, Placeholders.PREDEFINED_PLACEHOLDER_PATTERN, this.placeholders);
        }
        for (Placeholders.PlaceholderGetter getter : getters) {
            node = Placeholders.parseNodes(node, Placeholders.PLACEHOLDER_PATTERN, getter);
        }
        return (MutableComponent) node.toText(ParserContext.of(PlaceholderContext.KEY, context), true);
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

    @Override
    public Type<?> type() {
        return TYPE;
    }

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
