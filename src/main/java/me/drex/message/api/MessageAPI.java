package me.drex.message.api;

import me.drex.message.impl.LanguageManager;

public interface MessageAPI {

    /**
     * Reloads all messages (from all mods)!
     */
    static void reload() {
        LanguageManager.loadLanguages();
    }

}
