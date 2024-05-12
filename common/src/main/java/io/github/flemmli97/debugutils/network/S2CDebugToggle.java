package io.github.flemmli97.debugutils.network;

import io.github.flemmli97.debugutils.DebugUtils;
import io.github.flemmli97.debugutils.client.RenderBools;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public record S2CDebugToggle(ResourceLocation toggle, boolean on) implements CustomPacketPayload {

    public static final Type<S2CDebugToggle> TYPE = new Type<>(new ResourceLocation(DebugUtils.MODID, "s2c_debug_toggle"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CDebugToggle> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public S2CDebugToggle decode(RegistryFriendlyByteBuf buf) {
            return new S2CDebugToggle(buf.readResourceLocation(), buf.readBoolean());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, S2CDebugToggle pkt) {
            buf.writeResourceLocation(pkt.toggle);
            buf.writeBoolean(pkt.on);
        }
    };

    public static void handle(S2CDebugToggle pkt) {
        Consumer<Boolean> c = RenderBools.HANDLERS.get(pkt.toggle);
        if (c != null)
            c.accept(pkt.on);
        else
            DebugUtils.LOGGER.error("Unkown debug toggle " + pkt.toggle);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
