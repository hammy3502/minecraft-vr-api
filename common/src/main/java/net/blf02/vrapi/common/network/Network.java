package net.blf02.vrapi.common.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.blf02.vrapi.common.Constants;

public class Network {

    public static final String PROTOCOL_VERSION = Constants.getNetworkVersion();

    public static final NetworkChannel CHANNEL = new NetworkChannel();

    public static void registerInternalPackets() {
        NetworkManager.registerReceiver(NetworkManager.c2s(), C2SPacket.TYPE, C2SPacket.CODEC, C2SPacket::receiveServer);
        if (Platform.getEnvironment() == Env.CLIENT) {
            NetworkManager.registerReceiver(NetworkManager.s2c(), S2CPacket.TYPE, S2CPacket.CODEC, S2CPacket::receiveClient);
        } else {
            NetworkManager.registerS2CPayloadType(S2CPacket.TYPE, S2CPacket.CODEC);
        }
    }
}
