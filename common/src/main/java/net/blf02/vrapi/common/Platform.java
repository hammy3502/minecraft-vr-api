package net.blf02.vrapi.common;

import net.blf02.vrapi.common.network.NetworkChannel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

public interface Platform {

    // Platform detection
    public boolean isClient();
    public boolean isDevelopmentEnvironment();

    // Registration
    public void registerKeyBinding(Object keyMapping); // Kept as Object in parameter so we can load this in dedicated server

    // Networking
    public <T> void sendToServer(T message, NetworkChannel.NetworkRegistrationData<T> data);
    public <T> void sendToPlayer(ServerPlayer player, T message, NetworkChannel.NetworkRegistrationData<T> data);

    // Event hooks
    public void registerClientPostTick(Consumer<Player> ticker);
    public void registerServerPostTick(Consumer<Player> ticker);
    public void registerClientPlayerQuit(Consumer<Player> quitHandler);
    public void registerOnPlayerJoin(Consumer<ServerPlayer> joinHandler);
    public void registerOnPlayerDisconnect(Consumer<ServerPlayer> disconnectHandler);
}
