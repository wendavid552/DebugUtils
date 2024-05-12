package io.github.flemmli97.debugutils;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class DebugCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(DebugUtils.MODID)
                .then(Commands.argument("module", ResourceLocationArgument.id()).requires(src -> src.hasPermission(2)).suggests(DebugCommands::getToggles)
                        .then(Commands.argument("on", BoolArgumentType.bool())
                                .executes(DebugCommands::toggle)))
                .then(Commands.literal("player").requires(src -> src.hasPermission(2))
                        .then(Commands.argument("module", ResourceLocationArgument.id()).suggests(DebugCommands::getToggles)
                                .then(Commands.argument("on", BoolArgumentType.bool())
                                        .executes(DebugCommands::toggleFor)))));
    }

    private static int toggleFor(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "player");
        boolean on = BoolArgumentType.getBool(context, "on");
        ResourceLocation id = ResourceLocationArgument.getId(context, "module");
        if (id.equals(DebugToggles.ALL)) {
            DebugToggles.toggleAll(players, true);
            context.getSource().sendSuccess(() -> Component.literal("Turned all debugging features off"), true);
            return players.size();
        }
        DebugToggles.ResourcedToggle t = DebugToggles.get(id);
        if (t != null) {
            t.updateFor(players);
            Component comp = Component.literal("Turned " + id + (on ? " on" : " off") + " for " + players.stream().map(p -> p.getGameProfile().getName()).toList());
            context.getSource().sendSuccess(() -> comp, true);
            return players.size();
        }
        context.getSource().sendFailure(Component.literal("No such toggle " + id));
        return 0;
    }

    private static int toggle(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getEntity() instanceof ServerPlayer serverPlayer ? serverPlayer : null;
        Collection<ServerPlayer> players = player != null ? List.of(player) : List.of();
        ResourceLocation id = ResourceLocationArgument.getId(context, "module");
        boolean on = BoolArgumentType.getBool(context, "on");
        if (id.equals(DebugToggles.ALL)) {
            DebugToggles.toggleAll(players, false);
            context.getSource().sendSuccess(() -> Component.literal("Turned all debugging features off"), true);
            return players.size();
        }
        DebugToggles.ResourcedToggle t = DebugToggles.get(id);
        if (t != null) {
            t.toggleFor(players, on);
            Component comp = Component.literal("Turned " + id + (on ? " on" : " off"));
            context.getSource().sendSuccess(() -> comp, true);
            return players.size();
        }
        context.getSource().sendFailure(Component.literal("No such toggle " + id));
        return 0;
    }

    private static CompletableFuture<Suggestions> getToggles(CommandContext<CommandSourceStack> context, SuggestionsBuilder build) {
        List<ResourceLocation> ids = new ArrayList<>();
        ids.add(DebugToggles.ALL);
        ids.addAll(DebugToggles.getRegistered());
        return suggestResource(ids, build);
    }

    private static void filterResources(Iterable<ResourceLocation> resources, String input, Consumer<ResourceLocation> resourceConsumer) {
        boolean bl = input.indexOf(58) > -1;
        for (ResourceLocation resource : resources) {
            if (bl) {
                String string = resource.toString();
                if (SharedSuggestionProvider.matchesSubStr(input, string)) {
                    resourceConsumer.accept(resource);
                }
            } else if (SharedSuggestionProvider.matchesSubStr(input, resource.getNamespace()) || resource.getNamespace().equals("minecraft") &&
                    (SharedSuggestionProvider.matchesSubStr(input, resource.getPath()) || SharedSuggestionProvider.matchesSubStr(input, resource.getPath().replace("debug/", "")))) {
                resourceConsumer.accept(resource);
            }
        }
    }

    private static CompletableFuture<Suggestions> suggestResource(Iterable<ResourceLocation> resources, SuggestionsBuilder builder) {
        String string = builder.getRemaining().toLowerCase(Locale.ROOT);
        filterResources(resources, string, res -> builder.suggest(res.toString()));
        return builder.buildFuture();
    }
}
