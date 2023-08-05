package me.drex.message.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import eu.pb4.placeholders.api.TextParserUtils;
import eu.pb4.placeholders.api.node.TextNode;
import me.drex.message.impl.interfaces.ClientLanguageGetter;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static me.drex.message.impl.MessageMod.LOGGER;


public class LanguageManager {

    // TODO: configurable default language
    public static final String DEFAULT_LANG = "en_us";
    public static final String MESSAGES = "messages";
    private static final Path CONFIG_MESSAGES_PATH = FabricLoader.getInstance().getConfigDir().resolve(MESSAGES);
    private static final String FILE_SUFFIX = ".json";

    // Gson constants
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private static final Type LANGUAGEDATA_TYPE = TypeToken.getParameterized(Map.class, String.class, String.class).getType();

    private static final Map<String, Map<String, String>> languageData = new HashMap<>();
    private static final Map<String, Map<String, TextNode>> cachedLanguageData = new HashMap<>();

    private LanguageManager() {
    }

    public static void loadLanguages() {
        // Create parent directories
        try {
            File langDir = CONFIG_MESSAGES_PATH.toFile();
            langDir.mkdirs();
            Map<String, Map<String, String>> data = new HashMap<>();
            for (ModContainer modContainer : FabricLoader.getInstance().getAllMods()) {
                mergeLanguageData(data, loadModLanguages(modContainer));
            }
            languageData.clear();
            languageData.putAll(data);
            cachedLanguageData.clear();
            cachedLanguageData.putAll(
                languageData.entrySet().stream()
                    .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().entrySet().stream().collect(
                            Collectors.toMap(
                                Map.Entry::getKey,
                                innerEntry -> TextParserUtils.formatNodes(innerEntry.getValue())
                            ))
                    ))
            );
        } catch (Throwable throwable) {
            LOGGER.error("Failed to load message data, keeping previous data!", throwable);
        }
    }

    public static TextNode resolveMessageId(@Nullable ServerPlayer player, @NotNull String key) {
        String languageCode;
        if (player != null) {
            // Attempt to load the message using the players chosen language
            languageCode = ((ClientLanguageGetter) player.connection).getLanguage();
            @Nullable Map<String, TextNode> messages = cachedLanguageData.get(languageCode);
            if (messages != null) {
                TextNode message = messages.get(key);
                if (message != null) return message;
            }
        }
        // Attempt to load the message using the default language "en_us"
        Map<String, TextNode> messages = cachedLanguageData.getOrDefault(DEFAULT_LANG, Collections.emptyMap());
        return messages.getOrDefault(key, TextParserUtils.formatNodes(key));
    }

    private static Map<String, Map<String, String>> loadModLanguages(ModContainer modContainer) {
        Optional<Path> optional = modContainer.findPath(MESSAGES);
        // Mod doesn't provide messages
        if (optional.isEmpty()) return Collections.emptyMap();

        final String modId = modContainer.getMetadata().getId();
        Map<String, Map<String, String>> result = loadModLanguagesFromPath(optional.get());
        Path modMessageConfigPath = CONFIG_MESSAGES_PATH.resolve(modId);
        modMessageConfigPath.toFile().mkdir();
        // Apply user changes on top of mod default messages
        mergeLanguageData(result, loadModLanguagesFromPath(modMessageConfigPath));
        // Save language files to config folder
        for (Map.Entry<String, Map<String, String>> entry : result.entrySet()) {
            String json = GSON.toJson(entry.getValue(), LANGUAGEDATA_TYPE);
            Path languagePath = modMessageConfigPath.resolve(entry.getKey() + FILE_SUFFIX);
            try {
                Files.writeString(languagePath, json);
            } catch (IOException e) {
                LOGGER.error("Failed to save language file \"{}\"", languagePath, e);
            }
        }
        return result;
    }

    private static Map<String, Map<String, String>> loadModLanguagesFromPath(Path root) {
        try {
            Map<String, Map<String, String>> messages = new HashMap<>();
            try (
                Stream<Path> files = Files.walk(root, 1)
            ) {
                Set<Path> paths = files.filter(p -> !Files.isDirectory(p)).collect(Collectors.toSet());
                for (Path path : paths) {
                    try {
                        String languageCode = parseLanguageCode(path.getFileName().toString());
                        messages.put(languageCode, loadLanguageData(path));
                    } catch (IllegalArgumentException e) {
                        LOGGER.error("Failed to parse language file name", e);
                    }
                }
                return messages;
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load language files", e);
            return Collections.emptyMap();
        }
    }

    private static void mergeLanguageData(Map<String, Map<String, String>> original, Map<String, Map<String, String>> otherMap) {
        for (Map.Entry<String, Map<String, String>> entry : otherMap.entrySet()) {
            Map<String, String> mergedMap = original.getOrDefault(entry.getKey(), new HashMap<>());
            mergedMap.putAll(entry.getValue());
            original.put(entry.getKey(), mergedMap);
        }
    }

    private static String parseLanguageCode(String fileName) {
        if (!fileName.endsWith(FILE_SUFFIX)) {
            throw new IllegalArgumentException("Language files have to end with \"" + FILE_SUFFIX + "\", but found \"" + fileName + "\"");
        }
        String languageCode = fileName.substring(0, fileName.length() - FILE_SUFFIX.length());
        if (languageCode.length() > 16) {
            throw new IllegalArgumentException("Language code must not be longer than 16 characters: \"" + languageCode + "\"");
        }
        return languageCode;
    }

    private static Map<String, String> loadLanguageData(Path path) {
        try {
            String data = Files.readString(path);
            return GSON.fromJson(data, LANGUAGEDATA_TYPE);
        } catch (IOException e) {
            LOGGER.error("Failed to read data from \"" + path + "\"", e);
            return Collections.emptyMap();
        }

    }
}
