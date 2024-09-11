package net.blf02.forge;

import net.blf02.vrapi.VRAPIMod;
import net.blf02.vrapi.common.Plat;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(VRAPIMod.MOD_ID)
public class VRAPIForge {
    public VRAPIForge() {
        Plat.INSTANCE = new PlatformImpl();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        // Don't show red X if the server doesn't have the API but we do.
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(() -> IExtensionPoint.DisplayTest.IGNORESERVERONLY, (a, b) -> true));

        if (Plat.INSTANCE.isClient()) {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerKeyMappings);
        }
        PlatformImpl.NETWORK.messageBuilder(BufferPacket.class)
                .encoder(BufferPacket::encode)
                .decoder(BufferPacket::decode)
                .consumerNetworkThread(BufferPacket::handle)
                .add();

        VRAPIMod.init();
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        APIProviderInit.init();
    }

    private void registerKeyMappings(RegisterKeyMappingsEvent event) {
        PlatformImpl.keyMappingsToRegister.forEach(o -> event.register((KeyMapping) o));
    }
}
