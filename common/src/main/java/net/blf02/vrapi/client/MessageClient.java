package net.blf02.vrapi.client;

import net.blf02.vrapi.common.network.Network;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;

public class MessageClient {

    public static void msg(String message) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            player.sendMessage(new TextComponent(message), player.getUUID());
        }
    }

    public static void versionMismatchDisconnect(String serverVersion) {
        ClientPacketListener packetListener = Minecraft.getInstance().getConnection();
        if (packetListener != null) {
            packetListener.onDisconnect(new TranslatableComponent("message.vrapi.version_mismatch",
                    serverVersion, Network.PROTOCOL_VERSION));
        }
    }
}
