package io.github.flemmli97.debugutils.network;

import io.github.flemmli97.debugutils.DebugUtils;
import io.github.flemmli97.debugutils.client.spawnchunks.SpawnChunkRenderer;
import io.github.flemmli97.debugutils.utils.DistanceManagerTicketGetter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;

public class S2CSpawnChunk implements CustomPacketPayload {

    public static final Type<S2CSpawnChunk> TYPE = new Type<>(ResourceLocation.tryBuild(DebugUtils.MODID, "s2c_spawn_chunk"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CSpawnChunk> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public S2CSpawnChunk decode(RegistryFriendlyByteBuf buf) {
            return new S2CSpawnChunk(buf.readInt());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, S2CSpawnChunk pkt) {
            buf.writeInt(pkt.ticketLevel);
        }
    };

    private final int ticketLevel;

    public S2CSpawnChunk(ServerLevel level) {
        this(((DistanceManagerTicketGetter) level.getChunkSource().chunkMap.getDistanceManager()).debugUtils$getTicketLevel(TicketType.START, level.getSharedSpawnPos()));
    }

    private S2CSpawnChunk(int ticketLevel) {
        this.ticketLevel = ticketLevel;
    }

    public static void handle(S2CSpawnChunk pkt) {
        SpawnChunkRenderer.INSTANCE.updateSpawnChunk(pkt.ticketLevel);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
