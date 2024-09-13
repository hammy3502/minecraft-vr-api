package net.blf02.fabric;

import net.blf02.vrapi.VRAPIMod;
import net.blf02.vrapi.common.Plat;
import net.blf02.vrapi.common.network.Network;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class VRAPIFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Plat.INSTANCE = new PlatformImpl();
        ServerPlayNetworking.registerGlobalReceiver(PlatformImpl.C2S, (server, player, handler, buf, responseSender) -> {
            buf.retain();
            server.execute(() -> {
                try {
                    Network.CHANNEL.doReceive(player, buf);
                } finally {
                    buf.release();
                }
            });
        });
        if (Plat.INSTANCE.isClient()) {
            ClientPlayNetworking.registerGlobalReceiver(PlatformImpl.S2C, (client, handler, buf, responseSender) -> {
                buf.retain();
                client.execute(() -> {
                    try {
                        Network.CHANNEL.doReceive(null, buf);
                    } finally {
                        buf.release();
                    }
                });
            });
        }
        VRAPIMod.init();
    }
}
