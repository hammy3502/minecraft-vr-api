package net.blf02.vrapi.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;

public class ClientRegistryAccess {

    public static RegistryAccess get() {
        return Minecraft.getInstance().level.registryAccess();
    }
}
