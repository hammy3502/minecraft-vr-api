package net.blf02.vrapi.server;

import net.blf02.vrapi.common.network.Network;
import net.blf02.vrapi.common.network.packets.VersionSyncPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class ServerSubscriber {

    public static void onPlayerJoin(ServerPlayer player) {
        Network.CHANNEL.sendToPlayer(player, new VersionSyncPacket(Network.PROTOCOL_VERSION));
    }

    // Also called from LeftVRPacket
    public static void onPlayerDisconnect(Player player) {
        Tracker.playerToVR.remove(player.getGameProfile().getName());
    }
}
