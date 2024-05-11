package io.github.flemmli97.debugutils;

import io.github.flemmli97.debugutils.network.S2CDebugToggle;
import io.github.flemmli97.debugutils.network.S2CSpawnChunk;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * Serverside toggles
 */
public class DebugToggles {

    public static ResourceLocation ALL = new ResourceLocation("debug/all");

    private static final Map<ResourceLocation, ResourcedToggle> GETTER = new TreeMap<>();

    private static final Map<UUID, Set<ResourceLocation>> PLAYER_ENABLED = new HashMap<>();

    public static final ResourcedToggle DEBUG_POI = register(new ResourceLocation("debug/poi"));
    public static final ResourcedToggle DEBUG_NEIGHBORSUPDATES = register(ClientboundCustomPayloadPacket.DEBUG_NEIGHBORSUPDATE_PACKET);
    public static final ResourcedToggle DEBUG_STRUCTURES = register(ClientboundCustomPayloadPacket.DEBUG_STRUCTURES_PACKET);
    public static final ResourcedToggle DEBUG_PATHS = register(ClientboundCustomPayloadPacket.DEBUG_PATHFINDING_PACKET);
    public static final ResourcedToggle DEBUG_GOALS = register(ClientboundCustomPayloadPacket.DEBUG_GOAL_SELECTOR);
    public static final ResourcedToggle DEBUG_RAIDS = register(ClientboundCustomPayloadPacket.DEBUG_RAIDS);
    public static final ResourcedToggle DEBUG_BRAINS = register(ClientboundCustomPayloadPacket.DEBUG_BRAIN);
    public static final ResourcedToggle DEBUG_BEES = register(ClientboundCustomPayloadPacket.DEBUG_BEE);
    public static final ResourcedToggle DEBUG_GAME_EVENT = register(ClientboundCustomPayloadPacket.DEBUG_GAME_EVENT);
    public static final ResourcedToggle DEBUG_GAME_EVENT_LISTENER = register(ClientboundCustomPayloadPacket.DEBUG_GAME_EVENT_LISTENER);
    public static final ResourcedToggle DEBUG_BEE_HIVES = register(ClientboundCustomPayloadPacket.DEBUG_HIVE);

    public static final ResourcedToggle DEBUG_WATER = register(new ResourceLocation("debug/water"));
    public static final ResourcedToggle DEBUG_HEIGHTMAP = register(new ResourceLocation("debug/heightmap"));
    public static final ResourcedToggle DEBUG_COLLISION = register(new ResourceLocation("debug/collision"));
    public static final ResourcedToggle DEBUG_LIGHT = register(new ResourceLocation("debug/light"));
    public static final ResourcedToggle DEBUG_SOLID_FACES = register(new ResourceLocation("debug/solid_faces"));
    public static final ResourcedToggle DEBUG_CHUNK = register(new ResourceLocation("debug/chunk"));
    public static final ResourcedToggle DEBUG_SPAWN_CHUNK = register(new ResourcedToggle(new ResourceLocation("debug/spawn_chunk"), (b, players) -> players.forEach(p -> {
        S2CSpawnChunk pkt = new S2CSpawnChunk(p.getLevel());
        Network.INSTANCE.sendToClient(pkt, p);
    })));

    public static ResourcedToggle register(ResourceLocation id) {
        return register(new ResourcedToggle(id));
    }

    public static synchronized ResourcedToggle register(ResourcedToggle toggle) {
        if (GETTER.containsKey(toggle.id))
            throw new IllegalArgumentException("A toggle with id" + toggle.id + " is already registered");
        GETTER.put(toggle.id, toggle);
        return toggle;
    }

    public static ResourcedToggle get(ResourceLocation id) {
        return GETTER.get(id);
    }

    public static Collection<ResourceLocation> getRegistered() {
        return GETTER.keySet();
    }

    public static void onLogout(ServerPlayer player) {
        PLAYER_ENABLED.remove(player.getUUID());
    }

    public static void toggleAll(Collection<ServerPlayer> players, boolean updateOnly) {
        GETTER.values().forEach(t -> {
            if (!t.get()) {
                if (updateOnly)
                    t.updateFor(players);
                else
                    t.toggleFor(players, false);
            }
        });
    }

    public static boolean isEnabled(Player player, ResourcedToggle toggle) {
        Set<ResourceLocation> enabled = PLAYER_ENABLED.get(player.getUUID());
        return enabled != null && enabled.contains(toggle.id);
    }

    public static class ResourcedToggle {

        public final ResourceLocation id;
        private boolean on;
        private final BiConsumer<Boolean, Collection<ServerPlayer>> onToggle;

        public ResourcedToggle(ResourceLocation id) {
            this.id = id;
            this.onToggle = null;
        }

        public ResourcedToggle(ResourceLocation id, BiConsumer<Boolean, Collection<ServerPlayer>> onToggle) {
            this.id = id;
            this.onToggle = onToggle;
        }

        public void toggleFor(Collection<ServerPlayer> players, boolean toggle) {
            this.on = toggle;
            this.updateFor(players);
        }

        public void updateFor(Collection<ServerPlayer> players) {
            S2CDebugToggle pkt = new S2CDebugToggle(this.id, this.on);
            players.forEach(player -> {
                Set<ResourceLocation> enabled = PLAYER_ENABLED.computeIfAbsent(player.getUUID(), k -> new HashSet<>());
                if (this.on)
                    enabled.add(this.id);
                else {
                    enabled.remove(this.id);
                    if (enabled.isEmpty())
                        PLAYER_ENABLED.remove(player.getUUID());
                }
                Network.INSTANCE.sendToClient(pkt, player);
            });
            if (this.onToggle != null) {
                if (this.on)
                    players = players.stream().filter(p -> isEnabled(p, this)).toList();
                this.onToggle.accept(this.on, players);
            }
        }

        public boolean get() {
            return this.on;
        }
    }
}
