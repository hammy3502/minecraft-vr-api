package net.blf02.neoforge;

import net.blf02.vrapi.VRAPIMod;
import net.blf02.vrapi.common.Plat;
import net.minecraft.client.KeyMapping;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.IExtensionPoint;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@Mod(VRAPIMod.MOD_ID)
public class VRAPINeoForge {
    public VRAPINeoForge(IEventBus modBus) {
        Plat.INSTANCE = new PlatformImpl();
        modBus.addListener(this::commonSetup);
        // Don't show red X if the server doesn't have the API but we do.
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(() -> IExtensionPoint.DisplayTest.IGNORESERVERONLY, (a, b) -> true));

        if (Plat.INSTANCE.isClient()) {
            modBus.addListener(this::registerKeyMappings);
        }
        PlatformImpl.NETWORK.registerMessage(0, BufferPacket.class, BufferPacket::encode,
                BufferPacket::decode, BufferPacket::handle);

        VRAPIMod.init();
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        APIProviderInit.init();
    }

    private void registerKeyMappings(RegisterKeyMappingsEvent event) {
        PlatformImpl.keyMappingsToRegister.forEach(o -> event.register((KeyMapping) o));
    }
}
