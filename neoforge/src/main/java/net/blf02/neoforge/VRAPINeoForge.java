package net.blf02.neoforge;

import net.blf02.vrapi.VRAPIMod;
import net.blf02.vrapi.common.Plat;
import net.blf02.vrapi.common.network.Network;
import net.minecraft.client.KeyMapping;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(VRAPIMod.MOD_ID)
public class VRAPINeoForge {
    public VRAPINeoForge(IEventBus modBus) {
        Plat.INSTANCE = new PlatformImpl();
        modBus.addListener(this::commonSetup);

        if (Plat.INSTANCE.isClient()) {
            modBus.addListener(this::registerKeyMappings);
        }
        modBus.addListener((RegisterPayloadHandlersEvent event) -> {
            PayloadRegistrar registrar = event.registrar(VRAPIMod.MOD_ID);
            registrar.playBidirectional(BufferPacket.ID, BufferPacket.CODEC,
                    new DirectionalPayloadHandler<>(
                            (packet, ctx) -> ctx.enqueueWork(() -> Network.CHANNEL.doReceive(null, packet.buffer())),
                            (packet, ctx) -> ctx.enqueueWork(() -> Network.CHANNEL.doReceive((ServerPlayer) ctx.player(), packet.buffer()))
                    ));
        });

        VRAPIMod.init();
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        APIProviderInit.init();
    }

    private void registerKeyMappings(RegisterKeyMappingsEvent event) {
        PlatformImpl.keyMappingsToRegister.forEach(o -> event.register((KeyMapping) o));
    }
}
