package me.drex.messagetest;

import com.mojang.brigadier.arguments.StringArgumentType;
import eu.pb4.placeholders.api.PlaceholderContext;
import me.drex.message.api.LocalizedMessage;
import me.drex.message.api.MessageAPI;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TextComponentTagVisitor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import org.apache.commons.lang3.time.StopWatch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestMod implements ModInitializer {

    public static final Home[] EXAMPLE_HOMES = new Home[]{
        new Home("Base", new BlockPos(46, 73, -125), ResourceLocation.parse("minecraft:overworld")),
        new Home("End", new BlockPos(-64, 100, 0), ResourceLocation.parse("minecraft:the_end")),
        new Home("Spawn", new BlockPos(0, 65, 0), ResourceLocation.parse("minecraft:overworld")),
    };

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("testmod")
                .then(Commands.literal("reload").executes(context -> {
                    StopWatch stopWatch = new StopWatch();
                    stopWatch.start();
                    MessageAPI.reload();
                    stopWatch.stop();
                    context.getSource().sendSuccess(() -> LocalizedMessage.builder("testmod.reload").addPlaceholder("time", stopWatch.getTime()).build(), false);
                    return 1;
                }))
                .then(Commands.literal("whois").then(
                    Commands.argument("target", EntityArgument.player())
                        .executes(context -> {
                            ServerPlayer target = EntityArgument.getPlayer(context, "target");
                            context.getSource().sendSuccess(() ->
                                LocalizedMessage.builder("testmod.whois").setStaticContext(PlaceholderContext.of(target)).build(), true);
                            return 1;
                        })
                ))
                .then(Commands.literal("homes").executes(context -> {
                    context.getSource().sendSuccess(() ->
                            ComponentUtils.formatList(List.of(EXAMPLE_HOMES), LocalizedMessage.localized("testmod.homes.seperator"), home -> LocalizedMessage.localized("testmod.homes.element", home.placeholders())),
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
                                    context.getSource().sendSuccess(() -> LocalizedMessage.localized("testmod.home.teleport", home.placeholders()), false);
                                    return 1;
                                }
                            }
                            context.getSource().sendFailure(LocalizedMessage.localized("testmod.home.unknown"));
                            return 0;
                        }))
                ).then(
                    Commands.literal("item")
                        .then(
                            Commands.literal("manylines")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    ItemStack handItem = player.getMainHandItem();
                                    if (!handItem.isEmpty()) {
                                        CompoundTag tag = new CompoundTag();
                                        for (int i = 0; i < 1000; i++) {
                                            tag.putInt("tag" + i, i);
                                        }
                                        var lore = new TextComponentTagVisitor("    ").visit(tag);

                                        handItem.set(DataComponents.LORE, new ItemLore(List.of(
                                            LocalizedMessage.builder("testmod.item.lore")
                                                .addPlaceholder("player", player.getDisplayName())
                                                .addPlaceholder("variable", lore)
                                                .build()
                                        )));
                                    }
                                    return 1;
                                })
                        )
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            ItemStack handItem = player.getMainHandItem();
                            if (!handItem.isEmpty()) {
                                handItem.set(DataComponents.CUSTOM_NAME, LocalizedMessage.localized("testmod.item.name"));
                                handItem.set(DataComponents.LORE, new ItemLore(List.of(
                                    LocalizedMessage.builder("testmod.item.lore")
                                        .addPlaceholder("player", player.getDisplayName())
                                        .addPlaceholder("variable", LocalizedMessage.localized("testmod.item.lore.inner"))
                                        .build()
                                )));
                            }
                            return 1;
                        })
                ).then(
                    Commands.literal("style")
                        .executes(context -> {
                            context.getSource().sendSuccess(() ->
                                LocalizedMessage.localized("testmod.unstyled").withStyle(ChatFormatting.BLUE), false);
                            return 1;
                        })
                )
            );
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
