package net.blf02.fabric;

import io.netty.buffer.Unpooled;
import net.blf02.vrapi.client.ClientRegistryAccess;
import net.blf02.vrapi.common.Platform;
import net.blf02.vrapi.common.network.NetworkChannel;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

public class PlatformImpl implements Platform {

    @Override
    public boolean isClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public void registerKeyBinding(Object keyMapping) {
        if (keyMapping instanceof KeyMapping km) {
            KeyBindingHelper.registerKeyBinding(km);
        }
    }

    @Override
    public <T> void sendToServer(T message, NetworkChannel.NetworkRegistrationData<T> data) {
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), ClientRegistryAccess.get());
        buffer.writeInt(data.id());
        data.encoder().accept(message, buffer);
        ClientPlayNetworking.send(new BufferPacket(buffer));
    }

    @Override
    public <T> void sendToPlayer(ServerPlayer player, T message, NetworkChannel.NetworkRegistrationData<T> data) {
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), player.registryAccess());
        buffer.writeInt(data.id());
        data.encoder().accept(message, buffer);
        ServerPlayNetworking.send(player, new BufferPacket(buffer));
    }

    @Override
    public void registerClientPostTick(Consumer<Player> ticker) {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                ticker.accept(client.player);
            }
        });
    }

    @Override
    public void registerServerPostTick(Consumer<Player> ticker) {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getPlayerList().getPlayers().forEach(ticker);
        });
    }

    @Override
    public void registerClientPlayerQuit(Consumer<Player> quitHandler) {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> quitHandler.accept(client.player));
    }

    @Override
    public void registerOnPlayerJoin(Consumer<ServerPlayer> joinHandler) {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> joinHandler.accept(handler.getPlayer()));
    }

    @Override
    public void registerOnPlayerDisconnect(Consumer<ServerPlayer> disconnectHandler) {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> disconnectHandler.accept(handler.getPlayer()));
    }
}
