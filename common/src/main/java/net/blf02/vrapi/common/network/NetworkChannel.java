package net.blf02.vrapi.common.network;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class NetworkChannel {

    private int identifierCounter = 0;
    private final Map<Class<?>, PacketHandlerInfo<?>> classToPacketHandler = new HashMap<>();
    private final Map<Integer, PacketHandlerInfo<?>> idToPacketHandler = new HashMap<>();

    public <T> void register(Class<T> type,
                             BiConsumer<T, FriendlyByteBuf> encoder,
                             Function<FriendlyByteBuf, T> decoder,
                             BiConsumer<T, Supplier<NetworkManager.PacketContext>> handler) {
        PacketHandlerInfo<T> handlerInfo = new PacketHandlerInfo<>(identifierCounter++, type, encoder, decoder, handler);
        classToPacketHandler.put(type, handlerInfo);
        idToPacketHandler.put(handlerInfo.intId, handlerInfo);
    }

    public void sendToPlayer(ServerPlayer player, Object payload) {
        NetworkManager.sendToPlayer(player, new S2CPacket<>(payload));
    }

    public void sendToPlayers(Iterable<ServerPlayer> players, Object payload) {
        for (ServerPlayer player : players) {
            sendToPlayer(player, payload);
        }
    }

    public void sendToServer(Object payload) {
        NetworkManager.sendToServer(new C2SPacket<>(payload));
    }

    @SuppressWarnings("unchecked")
    public <T> PacketHandlerInfo<T> getPacketHandler(T payload) {
        PacketHandlerInfo<T> handlerInfo = (PacketHandlerInfo<T>) classToPacketHandler.get(payload.getClass());
        if (handlerInfo == null) {
            throw new IllegalArgumentException("Packet type " + payload.getClass().getName() + " not registered!");
        }
        return handlerInfo;
    }

    @SuppressWarnings("unchecked")
    public <T> PacketHandlerInfo<T> getPacketHandler(int intId) {
        PacketHandlerInfo<T> handlerInfo = (PacketHandlerInfo<T>) idToPacketHandler.get(intId);
        if (handlerInfo == null) {
            throw new IllegalArgumentException("Packet int id out of range!");
        }
        return handlerInfo;
    }

    public record PacketHandlerInfo<T>(int intId, Class<T> type,
                                        BiConsumer<T, FriendlyByteBuf> encoder,
                                        Function<FriendlyByteBuf, T> decoder,
                                        BiConsumer<T, Supplier<NetworkManager.PacketContext>> handler) {}
}
