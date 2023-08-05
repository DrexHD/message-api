package me.drex.message.impl.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.pb4.placeholders.api.PlaceholderContext;
import me.drex.message.impl.MessageImpl;
import me.drex.message.impl.MessageMod;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class NbtLocalizer {

    public static final Style DEFAULT_STYLE = Style.EMPTY
        .withColor(ChatFormatting.WHITE)
        .withItalic(false);
    private final CompoundTag compoundTag;
    private final MinecraftServer server;
    private final PlaceholderContext placeholderContext;
    private boolean localized = false;

    public NbtLocalizer(CompoundTag original, MinecraftServer server, PlaceholderContext placeholderContext) {
        this.server = server;
        this.placeholderContext = placeholderContext;
        this.compoundTag = original.copy();
    }

    private static List<String> splitComponents(List<MutableComponent> components) {
        List<String> result = new ArrayList<>();
        MutableComponent builder = Component.empty();
        for (MutableComponent component : components) {
            if (component.getContents() instanceof LiteralContents literal && literal.text().contains("\n")) {
                String[] lines = literal.text().split("\\n", -1);
                for (int i = 0; i < lines.length - 1; i++) {
                    String line = lines[i];
                    builder.append(Component.literal(line).withStyle(component.getStyle()));
                    result.add(Component.Serializer.toJson(builder));
                    builder = Component.empty();
                }
                builder.append(Component.literal(lines[lines.length - 1]).withStyle(component.getStyle()));
            } else {
                builder.append(component);
            }
        }
        if (!builder.getSiblings().isEmpty()) {
            result.add(Component.Serializer.toJson(builder));
        }
        return result;
    }

    private void localize() {
        if (compoundTag.contains("display", Tag.TAG_COMPOUND)) {
            localizeDisplay(compoundTag.getCompound("display"));
        }
        localized = true;
    }

    private void localizeDisplay(CompoundTag display) {
        if (display.contains("Name", Tag.TAG_STRING)) {
            display.putString("Name", localizeMessageJson(display.getString("Name")));
        }
        if (display.contains("Lore", Tag.TAG_LIST)) {
            localizeLore(display);
        }
    }

    private void localizeLore(CompoundTag display) {
        @NotNull ListTag lore = display.getList("Lore", Tag.TAG_STRING);
        ListTag localizedLore = new ListTag();
        for (int i = 0; i < lore.size(); i++) {
            for (String line : localizeMessageLoreJson(lore.getString(i))) {
                localizedLore.add(StringTag.valueOf(line));
            }
        }
        display.put("Lore", localizedLore);
    }

    public CompoundTag getResult() {
        if (!localized) localize();
        return compoundTag;
    }

    private String localizeMessageJson(String json) {
        MutableComponent component;
        try {
            component = Component.Serializer.fromJsonLenient(json);
        } catch (Exception e) {
            MessageMod.LOGGER.error("Failed to localize item name", e);
            return json;
        }
        if (component.getContents() instanceof MessageImpl message) {
            component = message.parseMessage(server, placeholderContext);
            component.setStyle(component.getStyle().applyTo(DEFAULT_STYLE));
            return Component.Serializer.toJson(component.withStyle());
        } else {
            return json;
        }
    }

    private List<String> localizeMessageLoreJson(String json) {
        MutableComponent component;
        try {
            component = Component.Serializer.fromJsonLenient(json);
        } catch (Exception e) {
            MessageMod.LOGGER.error("Failed to localize item lore", e);
            return List.of(json);
        }
        if (component != null && component.getContents() instanceof MessageImpl) {
            try {
                component = ComponentUtils.updateForEntity(placeholderContext.source(), component, placeholderContext.entity(), 0);
            } catch (CommandSyntaxException e) {
                return List.of(json);
            }
            List<MutableComponent> components = new ArrayList<>();
            collectComponents(components, component, DEFAULT_STYLE);
            return splitComponents(components);
        } else {
            return List.of(json);
        }
    }

    private void collectComponents(List<MutableComponent> components, Component current, Style style) {
        Style updatedStyle = current.getStyle().applyTo(style);
        components.add(MutableComponent.create(current.getContents()).setStyle(
            updatedStyle));
        for (Component sibling : current.getSiblings()) {
            collectComponents(components, sibling, updatedStyle);
        }
    }


}
