package net.blf02.vrapi.common.network.packets;

import net.blf02.vrapi.client.MessageClient;
import net.blf02.vrapi.client.ServerHasAPI;
import net.blf02.vrapi.common.network.Network;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

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

    public static void handle(VersionSyncPacket message, ServerPlayer sender) {
        if (sender == null) { // Only handled on client
            if (message.protocolVersion.equals(Network.PROTOCOL_VERSION) ||
                    message.protocolVersion.equals("GoodToGo!")) { // TODO: Remove GoodToGo! for backwards compat on 3.1.z or 4.y.z
                ServerHasAPI.serverHasAPI = true;
                ServerHasAPI.apiResponseCountdown = -1;
            } else {
                MessageClient.versionMismatchDisconnect(message.protocolVersion);
            }
        } else { // Handle backwards-compat with older MC VR API versions in the 3.0.x series.
            // TODO: Remove on 3.1.z or 4.y.z
            if (!message.protocolVersion.equals(Network.PROTOCOL_VERSION)) {
                sender.connection.connection.disconnect(new TextComponent(
                        "Version mismatch! The server is on " + Network.PROTOCOL_VERSION + " but you're on " + message.protocolVersion + "!"));
            } else {
                Network.CHANNEL.sendToPlayer(sender, new VersionSyncPacket(Network.PROTOCOL_VERSION));
            }
        }
    }
}
