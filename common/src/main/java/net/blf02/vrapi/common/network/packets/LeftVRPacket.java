package net.blf02.vrapi.common.network.packets;

import dev.architectury.networking.NetworkManager;
import net.blf02.vrapi.server.ServerSubscriber;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.function.Supplier;

public class LeftVRPacket {

    public LeftVRPacket() {

    }

    public static void encode(LeftVRPacket packet, FriendlyByteBuf buffer) {

    }

    public static LeftVRPacket decode(FriendlyByteBuf buffer) {
        return new LeftVRPacket();
    }

    public void handle(Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> {
            Player senderP = ctx.get().getPlayer();
            if (senderP instanceof ServerPlayer sender) {
                ServerSubscriber.onPlayerDisconnect(sender);
            }
        });
    }
}
