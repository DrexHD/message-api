package me.drex.message.api;

import me.drex.message.impl.LanguageManager;

public class MessageAPI {

    /**
     * Reloads all messages (from all mods)!
     */
    public static void reload() {
        LanguageManager.loadLanguages();
    }

}
