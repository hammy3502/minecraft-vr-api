package net.blf02.fabric;

import net.blf02.vrapi.VRAPIMod;
import net.blf02.vrapi.common.Plat;
import net.blf02.vrapi.common.network.Network;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class VRAPIFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Plat.INSTANCE = new PlatformImpl();
        PayloadTypeRegistry.playS2C().register(BufferPacket.ID, BufferPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(BufferPacket.ID, BufferPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(BufferPacket.ID, ((payload, context) -> {
            payload.buffer().retain();
            context.server().execute(() -> {
                try {
                    Network.CHANNEL.doReceive(context.player(), payload.buffer());
                } finally {
                    payload.buffer().release();
                }
            });
        }));
        if (Plat.INSTANCE.isClient()) {
            ClientPlayNetworking.registerGlobalReceiver(BufferPacket.ID, (payload, context) -> {
                payload.buffer().retain();
                context.client().execute(() -> {
                    try {
                        Network.CHANNEL.doReceive(null, payload.buffer());
                    } finally {
                        payload.buffer().release();
                    }
                });
            });
        }
        VRAPIMod.init();
    }
}
