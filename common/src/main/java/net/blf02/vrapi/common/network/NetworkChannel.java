package net.blf02.vrapi.common.network;

import net.blf02.vrapi.VRAPIMod;
import net.blf02.vrapi.common.Plat;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.logging.Level;

public class NetworkChannel {

    private final List<NetworkRegistrationData<?>> packets = new ArrayList<>();

    public <T> void register(Class<T> clazz, BiConsumer<T, FriendlyByteBuf> encoder,
                             Function<FriendlyByteBuf, T> decoder, BiConsumer<T, ServerPlayer> handler) {
        packets.add(new NetworkRegistrationData<>(packets.size(), clazz, encoder, decoder, handler));
    }

    public <T> void sendToServer(T message) {
        NetworkRegistrationData<T> data = getData(message);
        Plat.INSTANCE.sendToServer(message, data);
    }

    public <T> void sendToPlayer(ServerPlayer player, T message) {
        NetworkRegistrationData<T> data = getData(message);
        Plat.INSTANCE.sendToPlayer(player, message, data);
    }

    @SuppressWarnings("unchecked")
    public <T> void doReceive(@Nullable ServerPlayer player, FriendlyByteBuf buffer) {
        NetworkRegistrationData<T> data = (NetworkRegistrationData<T>) packets.get(buffer.readInt());
        T message;
        try {
            message = data.decoder.apply(buffer);
        } catch (Exception e) {
            VRAPIMod.LOGGER.log(Level.SEVERE, "Error while decoding packet.", e);
            return;
        }
        data.handler.accept(message, player);
    }

    @SuppressWarnings("unchecked")
    private <T> NetworkRegistrationData<T> getData(T message) {
        NetworkRegistrationData<T> data = (NetworkRegistrationData<T>) packets.stream()
                .filter(d -> d.clazz == message.getClass())
                .findFirst().orElse(null);
        if (data == null) {
            throw new IllegalArgumentException("Packet type %s not registered!".formatted(message.getClass().getName()));
        }
        return data;
    }

    public record NetworkRegistrationData<T>(int id, Class<T> clazz, BiConsumer<T, FriendlyByteBuf> encoder,
                                              Function<FriendlyByteBuf, T> decoder, BiConsumer<T, ServerPlayer> handler) {}
}
