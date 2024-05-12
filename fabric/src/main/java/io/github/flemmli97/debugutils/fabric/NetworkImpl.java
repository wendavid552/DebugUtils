package io.github.flemmli97.debugutils.fabric;

import io.github.flemmli97.debugutils.Network;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public class NetworkImpl implements Network {

    @Override
    public void sendToClient(CustomPacketPayload message, ServerPlayer player) {
        ServerPlayNetworking.send(player, message);
    }
}
