package me.drex.message.impl;

import net.fabricmc.api.ModInitializer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageMod implements ModInitializer {

    public static final String MOD_ID = "message-api";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static MinecraftServer SERVER_INSTANCE;
    public static final ThreadLocal<ServerPlayer> PACKET_LISTENER = ThreadLocal.withInitial(() -> null);

    @Override
    public void onInitialize() {
        LanguageManager.loadLanguages();
    }

}
