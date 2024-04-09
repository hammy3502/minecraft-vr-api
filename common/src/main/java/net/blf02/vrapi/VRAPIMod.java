package net.blf02.vrapi;

import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.blf02.vrapi.client.ClientSubscriber;
import net.blf02.vrapi.client.ReflectionConstants;
import net.blf02.vrapi.client.VRDataGrabber;
import net.blf02.vrapi.common.CommonSubscriber;
import net.blf02.vrapi.common.Constants;
import net.blf02.vrapi.common.network.Network;
import net.blf02.vrapi.common.network.packets.LeftVRPacket;
import net.blf02.vrapi.common.network.packets.VRDataPacket;
import net.blf02.vrapi.common.network.packets.VRRumblePacket;
import net.blf02.vrapi.common.network.packets.VersionSyncPacket;
import net.blf02.vrapi.debug.DebugSubscriber;

import java.util.logging.Logger;

public class VRAPIMod {
    public static final Logger LOGGER = Logger.getLogger(VRAPIMod.MOD_ID);

    public static final String MOD_ID = "vrapi";

    // Dev
    public static boolean USE_DEV_FEATURES = false;

    public static void init() {
        // Client only
        if (Platform.getEnvironment() == Env.CLIENT) {
            TickEvent.PLAYER_POST.register(ClientSubscriber::onPlayerTick);
        }

        // Common
        PlayerEvent.PLAYER_QUIT.register(CommonSubscriber::onPlayerDisconnect);
        if (Constants.doDebugging) {
            TickEvent.PLAYER_POST.register(DebugSubscriber::onPlayerTick);
        }

        // Only bother to grab VR Data when on the client-side
        if (Platform.getEnvironment() == Env.CLIENT) {
            ReflectionConstants.init();
            VRDataGrabber.init();
            // Set USE_DEV_FEATURES based on if in dev environment and Vivecraft not detected.
            if (!ReflectionConstants.clientHasVivecraft() && Platform.isDevelopmentEnvironment()) {
                USE_DEV_FEATURES = true;
                VRAPIModClient.initDebugKeys();
            }
        }

        Network.CHANNEL.register(VRDataPacket.class, VRDataPacket::encode, VRDataPacket::decode,
                VRDataPacket::handle);
        Network.CHANNEL.register(VRRumblePacket.class, VRRumblePacket::encode, VRRumblePacket::decode,
                VRRumblePacket::handle);
        Network.CHANNEL.register(VersionSyncPacket.class, VersionSyncPacket::encode, VersionSyncPacket::decode,
                VersionSyncPacket::handle);
        Network.CHANNEL.register(LeftVRPacket.class, LeftVRPacket::encode, LeftVRPacket::decode,
                LeftVRPacket::handle);
    }
}
