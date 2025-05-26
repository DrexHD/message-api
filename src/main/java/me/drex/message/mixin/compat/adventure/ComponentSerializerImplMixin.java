package me.drex.message.mixin.compat.adventure;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.kyori.adventure.text.BuildableComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

// A best effort attempt at proving some compatability with https://github.com/KyoriPowered/adventure
@Mixin(targets = "net/kyori/adventure/text/serializer/gson/ComponentSerializerImpl", remap = false)
public abstract class ComponentSerializerImplMixin {

    @Shadow
    public abstract BuildableComponent<?, ?> read(JsonReader in) throws IOException;

    @WrapOperation(
        method = "read(Lcom/google/gson/stream/JsonReader;)Lnet/kyori/adventure/text/BuildableComponent;",
        at = @At(
            value = "INVOKE",
            target = "Lcom/google/gson/stream/JsonReader;nextName()Ljava/lang/String;",
            ordinal = 0
        )
    )
    public String saveFieldName(
        JsonReader instance, Operation<String> original, @Share("fieldName") LocalRef<String> fieldNameRef
    ) {
        String result = original.call(instance);
        fieldNameRef.set(result);
        return result;
    }

    @WrapOperation(
        method = "read(Lcom/google/gson/stream/JsonReader;)Lnet/kyori/adventure/text/BuildableComponent;",
        at = @At(
            value = "INVOKE",
            target = "Lcom/google/gson/JsonObject;add(Ljava/lang/String;Lcom/google/gson/JsonElement;)V"
        )
    )
    public void parseComponentFields(
        JsonObject style, String fieldName, JsonElement element, Operation<Void> original,
        @Local(argsOnly = true) JsonReader in,
        @Share("key") LocalRef<String> keyRef,
        @Share("placeholders") LocalRef<Map<String, BuildableComponent<?, ?>>> placeholdersRef
    ) throws IOException {
        if (fieldName.equals("key")) {
            keyRef.set(in.nextString());
        } else if (fieldName.equals("placeholders")) {
            Map<String, BuildableComponent<?, ?>> placeholders = new HashMap<>();
            in.beginObject();
            while (in.hasNext()) {
                String key = in.nextName();
                BuildableComponent<?, ?> value = read(in);
                placeholders.put(key, value);
            }
            in.endObject();
            placeholdersRef.set(placeholders);
        } else {
            original.call(style, fieldName, element);
        }
    }

    // prevent gson from reading the argument if we are parsing our own stuff
    @WrapOperation(
        method = "read(Lcom/google/gson/stream/JsonReader;)Lnet/kyori/adventure/text/BuildableComponent;",
        at = @At(
            value = "INVOKE",
            target = "Lcom/google/gson/Gson;fromJson(Lcom/google/gson/stream/JsonReader;Ljava/lang/reflect/Type;)Ljava/lang/Object;",
            ordinal = 4
        )
    )
    public <T> T dontRead(
        Gson instance, JsonReader reader, Type typeOfT, Operation<T> original,
        @Share("fieldName") LocalRef<String> fieldNameRef
    ) {
        return switch (fieldNameRef.get()) {
            case "key", "placeholders": {
                yield null;
            }
            default: {
                yield original.call(instance, reader, typeOfT);
            }
        };
    }


    /**
     * The next two mixins attempt to change
     *
     * <pre>
     * if (text != null) {
     *   builder = Component.text().content(text);
     * } else if ...
     * </pre>
     * to something 'like' this
     * <pre>
     * if (text != null) {
     *   builder = Component.text().content(text);
     * } else if (key != null) {
     *   builder = Component.virtual(...);
     * } else if ...
     * </pre>
     */

    @Definition(id = "text", local = @Local(type = String.class, ordinal = 0))
    @Expression("text != null")
    @WrapOperation(
        method = "read(Lcom/google/gson/stream/JsonReader;)Lnet/kyori/adventure/text/BuildableComponent;",
        at = @At("MIXINEXTRAS:EXPRESSION")
    )
    public boolean allowCustomBuilder(
        Object object,
        Object object2,
        Operation<Boolean> original,
        @Share("key") LocalRef<String> keyRef,
        @Share("placeholders") LocalRef<Map<String, BuildableComponent<?, ?>>> placeholdersRef
    ) {
        return original.call(object, object2) || keyRef.get() != null;
    }

    @WrapOperation(
        method = "read(Lcom/google/gson/stream/JsonReader;)Lnet/kyori/adventure/text/BuildableComponent;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/kyori/adventure/text/TextComponent$Builder;content(Ljava/lang/String;)Lnet/kyori/adventure/text/TextComponent$Builder;"
        )
    )
    public TextComponent.Builder customBuilder(
        TextComponent.Builder instance, String s, Operation<TextComponent.Builder> original,
        @Share("key") LocalRef<String> keyRef,
        @Share("placeholders") LocalRef<Map<String, BuildableComponent<?, ?>>> placeholdersRef
    ) {
        if (keyRef.get() != null) {
            return Component.empty().toBuilder();
        } else {
            return original.call(instance, s);
        }
    }

}
