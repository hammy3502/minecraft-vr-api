package net.blf02.vrapi.common.network.packets;

import dev.architectury.networking.NetworkManager;
import net.blf02.vrapi.client.MessageClient;
import net.blf02.vrapi.client.ServerHasAPI;
import net.blf02.vrapi.common.network.Network;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.function.Supplier;

public class VersionSyncPacket {

    public final String protocolVersion;

    public VersionSyncPacket(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public static void encode(VersionSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.protocolVersion);
    }

    public static VersionSyncPacket decode(FriendlyByteBuf buffer) {
        return new VersionSyncPacket(buffer.readUtf());
    }

    public void handle(Supplier<NetworkManager.PacketContext> ctx) {
        ctx.get().queue(() -> {
            Player senderP = ctx.get().getPlayer();
            if (!(senderP instanceof ServerPlayer)) { // Only handled on client
                if (this.protocolVersion.equals(Network.PROTOCOL_VERSION) ||
                    this.protocolVersion.equals("GoodToGo!")) { // TODO: Remove GoodToGo! for backwards compat on 3.1.z or 4.y.z
                    ServerHasAPI.serverHasAPI = true;
                    ServerHasAPI.apiResponseCountdown = -1;
                } else {
                    MessageClient.versionMismatchDisconnect(this.protocolVersion);
                }
            } else { // Handle backwards-compat with older MC VR API versions in the 3.0.x series.
                // TODO: Remove on 3.1.z or 4.y.z
                ServerPlayer sender = (ServerPlayer) senderP;
                if (!this.protocolVersion.equals(Network.PROTOCOL_VERSION)) {
                    sender.connection.connection.disconnect(new TextComponent(
                            "Version mismatch! The server is on " + Network.PROTOCOL_VERSION + " but you're on " + this.protocolVersion + "!"));
                } else {
                    Network.CHANNEL.sendToPlayer(sender, new VersionSyncPacket(Network.PROTOCOL_VERSION));
                }
            }
        });
    }
}
