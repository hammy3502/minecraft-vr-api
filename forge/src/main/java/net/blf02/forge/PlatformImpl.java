package net.blf02.forge;

import io.netty.buffer.Unpooled;
import net.blf02.vrapi.VRAPIMod;
import net.blf02.vrapi.client.ClientRegistryAccess;
import net.blf02.vrapi.common.Platform;
import net.blf02.vrapi.common.network.Network;
import net.blf02.vrapi.common.network.NetworkChannel;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PlatformImpl implements Platform {

    public static final List<Object> keyMappingsToRegister = new ArrayList<>();
    public static final SimpleChannel NETWORK = ChannelBuilder.named(ResourceLocation.fromNamespaceAndPath(VRAPIMod.MOD_ID, "network"))
            .optional()
            .simpleChannel()
            .play().bidirectional().add(BufferPacket.class, BufferPacket.CODEC, (bufferPacket, context) -> {
                context.enqueueWork(() -> Network.CHANNEL.doReceive(context.getSender(), bufferPacket.buffer()));
                context.setPacketHandled(true);
            })
            .build();

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
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), ClientRegistryAccess.get());
        buffer.writeInt(data.id());
        data.encoder().accept(message, buffer);
        NETWORK.send(new BufferPacket(buffer), PacketDistributor.SERVER.noArg());
    }

    @Override
    public <T> void sendToPlayer(ServerPlayer player, T message, NetworkChannel.NetworkRegistrationData<T> data) {
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), player.registryAccess());
        buffer.writeInt(data.id());
        data.encoder().accept(message, buffer);
        NETWORK.send(new BufferPacket(buffer), PacketDistributor.PLAYER.with(player));
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
                event.getServer().getPlayerList().getPlayers().forEach(ticker);
            }
        });
    }

    @Override
    public void registerClientPlayerQuit(Consumer<Player> quitHandler) {
        MinecraftForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> {
            quitHandler.accept(event.getPlayer());
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
