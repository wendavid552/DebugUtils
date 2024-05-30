package io.github.flemmli97.debugutils.fabric;

import io.github.flemmli97.debugutils.client.AdditionalDebugRenderers;
import io.github.flemmli97.debugutils.client.RenderBools;
import io.github.flemmli97.debugutils.network.S2CDebugToggle;
import io.github.flemmli97.debugutils.network.S2CSpawnChunk;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class DebugUtilsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientLoginConnectionEvents.DISCONNECT.register((handler, client) -> RenderBools.onDisconnect());
        AdditionalDebugRenderers.init();
        PayloadTypeRegistry.playS2C().register(S2CDebugToggle.TYPE, S2CDebugToggle.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(S2CSpawnChunk.TYPE, S2CSpawnChunk.STREAM_CODEC);
        ClientPlayNetworking.registerGlobalReceiver(S2CDebugToggle.TYPE, (pkt, ctx) -> S2CDebugToggle.handle(pkt));
        ClientPlayNetworking.registerGlobalReceiver(S2CSpawnChunk.TYPE, (pkt, ctx) -> S2CSpawnChunk.handle(pkt));
    }
}
