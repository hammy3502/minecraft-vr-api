package net.blf02.vrapi.common.network.packets;

import net.blf02.vrapi.data.VRPlayer;
import net.blf02.vrapi.server.Tracker;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class VRDataPacket {

    public final VRPlayer vrPlayer;
    public final boolean isSeated;
    public final boolean isLeftHanded;

    public VRDataPacket(VRPlayer vrPlayer, boolean isSeated, boolean isLeftHanded) {
        this.vrPlayer = vrPlayer;
        this.isSeated = isSeated;
        this.isLeftHanded = isLeftHanded;
    }

    public static void encode(VRDataPacket packet, FriendlyByteBuf buffer) {
        VRPlayer.encode(packet.vrPlayer, buffer);
        buffer.writeBoolean(packet.isSeated);
        buffer.writeBoolean(packet.isLeftHanded);
    }

    public static VRDataPacket decode(FriendlyByteBuf buffer) {
        return new VRDataPacket(VRPlayer.decode(buffer), buffer.readBoolean(), buffer.readBoolean());
    }

    public static void handle(VRDataPacket message, ServerPlayer sender) {
        if (sender != null) {
            Tracker.playerToVR.put(sender.getGameProfile().getName(),
                    new Tracker.ServerSideVRPlayerData(message.vrPlayer, message.isSeated, message.isLeftHanded));
        }
    }
}
