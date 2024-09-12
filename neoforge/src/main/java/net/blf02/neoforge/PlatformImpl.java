package net.blf02.neoforge;

import io.netty.buffer.Unpooled;
import net.blf02.vrapi.client.ClientRegistryAccess;
import net.blf02.vrapi.common.Platform;
import net.blf02.vrapi.common.network.NetworkChannel;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.connection.ConnectionType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PlatformImpl implements Platform {

    public static final List<Object> keyMappingsToRegister = new ArrayList<>();

    @Override
    public boolean isClient() {
        return FMLEnvironment.dist == Dist.CLIENT;
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLEnvironment.production;
    }

    @Override
    public void registerKeyBinding(Object keyMapping) {
        keyMappingsToRegister.add(keyMapping);
    }

    @Override
    public <T> void sendToServer(T message, NetworkChannel.NetworkRegistrationData<T> data) {
        try {
            RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), ClientRegistryAccess.get(), ConnectionType.NEOFORGE);
            buffer.writeInt(data.id());
            data.encoder().accept(message, buffer);
            PacketDistributor.sendToServer(new BufferPacket(buffer));
        } catch (UnsupportedOperationException ignored) {} // Ignore errors from sending packets when the other side can't receive
    }

    @Override
    public <T> void sendToPlayer(ServerPlayer player, T message, NetworkChannel.NetworkRegistrationData<T> data) {
        try {
            RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), player.registryAccess(), ConnectionType.NEOFORGE);
            buffer.writeInt(data.id());
            data.encoder().accept(message, buffer);
            PacketDistributor.sendToPlayer(player, new BufferPacket(buffer));
        } catch (UnsupportedOperationException ignored) {} // Ignore errors from sending packets when the other side can't receive

    }

    @Override
    public void registerClientPostTick(Consumer<Player> ticker) {
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> {
            if (Minecraft.getInstance().player != null) {
                ticker.accept(Minecraft.getInstance().player);
            }
        });
    }

    @Override
    public void registerServerPostTick(Consumer<Player> ticker) {
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> {
            event.getServer().getPlayerList().getPlayers().forEach(ticker);
        });
    }

    @Override
    public void registerClientPlayerQuit(Consumer<Player> quitHandler) {
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> {
            if (event.getEntity() == Minecraft.getInstance().player) {
                quitHandler.accept(event.getEntity());
            }
        });
    }

    @Override
    public void registerOnPlayerJoin(Consumer<ServerPlayer> joinHandler) {
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer sp) {
                joinHandler.accept(sp);
            }
        });
    }

    @Override
    public void registerOnPlayerDisconnect(Consumer<ServerPlayer> disconnectHandler) {
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer sp) {
                disconnectHandler.accept(sp);
            }
        });
    }
}
