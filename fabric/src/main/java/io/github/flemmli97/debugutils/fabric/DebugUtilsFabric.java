package io.github.flemmli97.debugutils.fabric;

import io.github.flemmli97.debugutils.DebugCommands;
import io.github.flemmli97.debugutils.DebugToggles;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class DebugUtilsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(((dispatcher, dedicated) -> DebugCommands.register(dispatcher)));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> DebugToggles.onLogout(handler.getPlayer()));
    }
}
