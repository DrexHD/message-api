package me.drex.message.impl;

import eu.pb4.placeholders.api.*;
import eu.pb4.placeholders.api.node.parent.ParentTextNode;
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

public class MessageImpl implements ComponentContents {

    private final String key;
    private final Map<String, Component> placeholders;
    private final List<Placeholders.PlaceholderGetter> getters;
    @Nullable
    private final PlaceholderContext staticContext;

    public MessageImpl(String key, Map<String, Component> placeholders, List<Placeholders.PlaceholderGetter> getters, @Nullable PlaceholderContext staticContext) {
        this.key = key;
        this.placeholders = placeholders;
        this.getters = getters;
        this.staticContext = staticContext;
    }

    public MutableComponent parseMessage(MinecraftServer server, @Nullable PlaceholderContext dynamicContext) {
        PlaceholderContext context;
        context = Objects.requireNonNullElseGet(this.staticContext, () -> Objects.requireNonNullElseGet(dynamicContext, () -> PlaceholderContext.of(server)));
        String text = LanguageManager.resolveMessageId(context.player(), this.key);
        ParentTextNode node = TextParserUtils.formatNodes(text);
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
        return Optional.empty();
    }

    @Override
    public <T> @NotNull Optional<T> visit(FormattedText.ContentConsumer<T> contentConsumer) {
        return Optional.empty();
    }

    @Override
    public @NotNull MutableComponent resolve(@Nullable CommandSourceStack src, @Nullable Entity entity, int i) {
        PlaceholderContext context = null;
        if (src != null) {
            context = PlaceholderContext.of(src);
        } else {
            if (entity != null) context = PlaceholderContext.of(entity);
        }
        return parseMessage(MessageMod.SERVER_INSTANCE, context);
    }

    public MutableComponent toText() {
        return MutableComponent.create(this);
    }

}
