package me.drex.message.mixin;

import com.google.gson.*;
import eu.pb4.placeholders.api.PlaceholderContext;
import me.drex.message.impl.MessageImpl;
import me.drex.message.impl.MessageMod;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static eu.pb4.placeholders.api.Placeholders.DEFAULT_PLACEHOLDER_GETTER;

@Mixin(Component.Serializer.class)
public abstract class ComponentSerializerMixin {

    @Shadow
    public abstract JsonElement serialize(Component component, Type type, JsonSerializationContext jsonSerializationContext);

    @Shadow
    public abstract MutableComponent deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException;

    @Inject(
        method = "serialize(Lnet/minecraft/network/chat/Component;Ljava/lang/reflect/Type;Lcom/google/gson/JsonSerializationContext;)Lcom/google/gson/JsonElement;",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/network/chat/ComponentContents;EMPTY:Lnet/minecraft/network/chat/ComponentContents;"
        ),
        locals = LocalCapture.CAPTURE_FAILHARD,
        cancellable = true
    )
    public void serializeMessageImpl(Component component, Type type, JsonSerializationContext context, CallbackInfoReturnable<JsonElement> cir, JsonObject jsonObject) {
        ServerPlayer target = MessageMod.PACKET_LISTENER.get();
        ComponentContents contents = component.getContents();
        if (contents instanceof MessageImpl message) {
            if (target != null) {
                MutableComponent parsedMessage = message.parseMessage(MessageMod.SERVER_INSTANCE, PlaceholderContext.of(target));
                for (Component sibling : component.getSiblings()) {
                    parsedMessage.append(sibling);
                }
                parsedMessage.setStyle(parsedMessage.getStyle().applyTo(component.getStyle()));
                cir.setReturnValue(this.serialize(parsedMessage, parsedMessage.getClass(), context));
            } else {
                jsonObject.addProperty("key", message.getKey());
                Map<String, Component> placeholders = message.getPlaceholders();
                if (!placeholders.isEmpty()) {
                    JsonObject placeholdersJson = new JsonObject();
                    for (Map.Entry<String, Component> placeholderEntry : placeholders.entrySet()) {
                        placeholdersJson.add(placeholderEntry.getKey(), context.serialize(placeholderEntry.getValue()));
                    }
                    jsonObject.add("placeholders", placeholdersJson);
                }
                cir.setReturnValue(jsonObject);
            }
        }
    }

    @Inject(
        method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/network/chat/MutableComponent;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void deserializeMessageImpl(JsonElement jsonElement, Type type, JsonDeserializationContext context, CallbackInfoReturnable<MutableComponent> cir) {
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if (jsonObject.has("key")) {
                String key = GsonHelper.getAsString(jsonObject, "key");
                Map<String, Component> placeholders = new HashMap<>();
                if (jsonObject.has("placeholders")) {
                    for (Map.Entry<String, JsonElement> placeholderJsonEntry : jsonObject.getAsJsonObject("placeholders").asMap().entrySet()) {
                        placeholders.put(placeholderJsonEntry.getKey(), context.deserialize(placeholderJsonEntry.getValue(), Component.class));
                    }
                }
                MutableComponent mutableComponent = new MessageImpl(key, placeholders, List.of(DEFAULT_PLACEHOLDER_GETTER), null).toText();

                // [VanillaCopy] - Start
                if (jsonObject.has("extra")) {
                    JsonArray jsonArray2 = GsonHelper.getAsJsonArray(jsonObject, "extra");
                    if (jsonArray2.size() <= 0) throw new JsonParseException("Unexpected empty array of components");
                    for (int j = 0; j < jsonArray2.size(); ++j) {
                        mutableComponent.append(this.deserialize(jsonArray2.get(j), type, context));
                    }
                }
                mutableComponent.setStyle(context.deserialize(jsonElement, Style.class));
                // [VanillaCopy] - End
                cir.setReturnValue(mutableComponent);
            }
        }
    }

}
