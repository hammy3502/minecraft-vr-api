package net.blf02.vrapi.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;

public class MessageClient {

    public static void msg(String message) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            player.sendMessage(new TextComponent(message), player.getUUID());
        }
    }
}
