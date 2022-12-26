package me.drex.message.impl;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageMod implements ModInitializer {

    public static final String MOD_ID = "message-api";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static MinecraftServer SERVER_INSTANCE;

    @Override
    public void onInitialize() {
        LanguageManager.loadLanguages();
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            SERVER_INSTANCE = server;
        });
    }

}
