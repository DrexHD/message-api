package me.drex.message.impl.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.PlainTextContents;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.List;

public class ComponentUtil {

    public static final Style DEFAULT_STYLE = Style.EMPTY
        .withColor(ChatFormatting.WHITE)
        .withItalic(false);

    public static void parseNewLines(Component component, List<Component> result) {
        MutableObject<MutableComponent> currentLine = new MutableObject<>(Component.empty());
        parseNewLines0(currentLine, DEFAULT_STYLE, component, result);
        if (!currentLine.getValue().getSiblings().isEmpty()) {
            result.add(currentLine.getValue());
        }
    }

    private static void parseNewLines0(Mutable<MutableComponent> currentLine, Style parentStyle, Component component, List<Component> result) {
        List<Component> siblings = component.getSiblings();
        ComponentContents contents = component.getContents();
        Style style = component.getStyle();
        Style mergedStyle = style.applyTo(parentStyle);
        if (contents instanceof PlainTextContents.LiteralContents literalContents) {
            String text = literalContents.text();
            String[] lines = text.split("\n", -1);
            boolean first = true;
            for (String line : lines) {
                if (first) {
                    first = false;
                } else {
                    result.add(currentLine.getValue());
                    currentLine.setValue(Component.empty());
                }
                if (!line.isEmpty()) {
                    currentLine.getValue().append(Component.literal(line).setStyle(mergedStyle));
                }
            }
        } else {
            if (!currentLine.getValue().getSiblings().isEmpty()) {
                currentLine.getValue().append(MutableComponent.create(contents).setStyle(mergedStyle));
            }
        }
        for (Component sibling : siblings) {
            parseNewLines0(currentLine, mergedStyle, sibling, result);
        }
    }


}
