package net.blf02.neoforge;

import net.blf02.vrapi.VRAPIMod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(VRAPIMod.MOD_ID)
public class VRAPINeoForge {
    public VRAPINeoForge(IEventBus eventBus) {
        eventBus.addListener(this::commonSetup);
        VRAPIMod.init();
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        APIProviderInit.init();
    }
}
