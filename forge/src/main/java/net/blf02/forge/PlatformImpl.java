package net.blf02.forge;

import io.netty.buffer.Unpooled;
import net.blf02.vrapi.VRAPIMod;
import net.blf02.vrapi.common.Platform;
import net.blf02.vrapi.common.network.NetworkChannel;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PlatformImpl implements Platform {

    public static final List<Object> keyMappingsToRegister = new ArrayList<>();
    public static final SimpleChannel NETWORK = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(VRAPIMod.MOD_ID, "network"))
            .networkProtocolVersion(() -> "3.0.10+")
            .serverAcceptedVersions(ignored -> true)
            .clientAcceptedVersions(ignored -> true).simpleChannel();

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
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeInt(data.id());
        data.encoder().accept(message, buffer);
        NETWORK.sendToServer(new BufferPacket(buffer));
    }

    @Override
    public <T> void sendToPlayer(ServerPlayer player, T message, NetworkChannel.NetworkRegistrationData<T> data) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeInt(data.id());
        data.encoder().accept(message, buffer);
        NETWORK.send(PacketDistributor.PLAYER.with(() -> player), new BufferPacket(buffer));
    }

    @Override
    public void registerClientPostTick(Consumer<Player> ticker) {
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END && Minecraft.getInstance().player != null) {
                ticker.accept(Minecraft.getInstance().player);
            }
        });
    }

    @Override
    public void registerServerPostTick(Consumer<Player> ticker) {
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ServerTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) {
                ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers().forEach(ticker);
            }
        });
    }

    @Override
    public void registerClientPlayerQuit(Consumer<Player> quitHandler) {
        MinecraftForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> {
            if (event.getEntity() == Minecraft.getInstance().player) {
                quitHandler.accept((Player) event.getEntity());
            }
        });
    }

    @Override
    public void registerOnPlayerJoin(Consumer<ServerPlayer> joinHandler) {
        MinecraftForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer sp) {
                joinHandler.accept(sp);
            }
        });
    }

    @Override
    public void registerOnPlayerDisconnect(Consumer<ServerPlayer> disconnectHandler) {
        MinecraftForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer sp) {
                disconnectHandler.accept(sp);
            }
        });
    }
}
