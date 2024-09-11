package net.blf02.vrapi.common.network.packets;

import net.blf02.vrapi.server.ServerSubscriber;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class LeftVRPacket {

    public LeftVRPacket() {

    }

    public static void encode(LeftVRPacket packet, FriendlyByteBuf buffer) {

    }

    public static LeftVRPacket decode(FriendlyByteBuf buffer) {
        return new LeftVRPacket();
    }

    public static void handle(LeftVRPacket message, ServerPlayer player) {
        if (player != null) {
            ServerSubscriber.onPlayerDisconnect(player);
        }
    }
}
