package me.drex.messagetest;

import com.mojang.brigadier.arguments.StringArgumentType;
import eu.pb4.placeholders.api.PlaceholderContext;
import me.drex.message.api.Message;
import me.drex.message.impl.LanguageManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.time.StopWatch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestMod implements ModInitializer {

    public static final Home[] EXAMPLE_HOMES = new Home[]{
            new Home("Base", new BlockPos(46, 73, -125), new ResourceLocation("minecraft:overworld")),
            new Home("End", new BlockPos(-64, 100, 0), new ResourceLocation("minecraft:the_end")),
            new Home("Spawn", new BlockPos(0, 65, 0), new ResourceLocation("minecraft:overworld")),
    };

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("testmod")
                    .then(Commands.literal("reload").executes(context -> {
                        StopWatch stopWatch = new StopWatch();
                        stopWatch.start();
                        LanguageManager.loadLanguages();
                        stopWatch.stop();
                        context.getSource().sendSuccess(Message.message("testmod.reload", new HashMap<>() {{
                            put("time", Component.literal(String.valueOf(stopWatch.getTime())));
                        }}), false);
                        return 1;
                    }))
                    .then(Commands.literal("whois").then(
                            Commands.argument("target", EntityArgument.player())
                                    .executes(context -> {
                                        ServerPlayer target = EntityArgument.getPlayer(context, "target");
                                        context.getSource().sendSuccess(
                                                Message.message("testmod.whois", PlaceholderContext.of(target)), false);
                                        return 1;
                                    })
                    ))
                    .then(Commands.literal("homes").executes(context -> {
                        context.getSource().sendSuccess(
                                ComponentUtils.formatList(List.of(EXAMPLE_HOMES), Message.message("testmod.homes.seperator"), home -> Message.message("testmod.homes.element", home.placeholders())),
                                false
                        );
                        return 1;
                    }))
                    .then(Commands.literal("home")
                            .then(Commands.argument("name", StringArgumentType.word())
                                    .executes(context -> {
                                        String name = StringArgumentType.getString(context, "name");
                                        for (Home home : EXAMPLE_HOMES) {
                                            if (home.name.equals(name)) {
                                                context.getSource().sendSuccess(Message.message("testmod.home.teleport", home.placeholders()), false);
                                                return 1;
                                            }
                                        }
                                        context.getSource().sendFailure(Message.message("testmod.home.unknown"));
                                        return 0;
                                    }))
                    ));
        });
    }

    record Home(String name, BlockPos pos, ResourceLocation dim) {
        Map<String, Component> placeholders() {
            return new HashMap<>() {{
                put("home_name", Component.literal(name));
                put("home_pos", Component.literal(pos.getX() + " " + pos.getY() + " " + pos.getZ()));
                put("home_dim", Component.literal(dim.toString()));
            }};
        }
    }
}
