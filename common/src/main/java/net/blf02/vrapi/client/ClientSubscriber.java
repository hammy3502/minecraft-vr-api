package net.blf02.vrapi.client;

import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.blf02.vrapi.VRAPIMod;
import net.blf02.vrapi.VRAPIModClient;
import net.blf02.vrapi.common.VRAPI;
import net.blf02.vrapi.common.network.Network;
import net.blf02.vrapi.common.network.packets.LeftVRPacket;
import net.blf02.vrapi.common.network.packets.VRDataPacket;
import net.blf02.vrapi.common.network.packets.VersionSyncPacket;
import net.blf02.vrapi.data.VRData;
import net.blf02.vrapi.data.VRPlayer;
import net.blf02.vrapi.debug.DevModeData;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class ClientSubscriber {
    public static boolean didJoinPacket = false;
    public static boolean sentVRDisabledPacket = false;

    public static void onPlayerTick(Player player) {
        if (player.level.isClientSide && player == Minecraft.getInstance().player) {
            VRPlayer vrPlayer = VRDataGrabber.getVRPlayer(VRDataGrabber.PlayerType.WORLD_POST);

            // Let dev set pos here and monitor keystrokes
            if (VRAPIMod.USE_DEV_FEATURES) {

                // Toggle VR mode
                if (VRAPIModClient.TOGGLE_VR_DEV.consumeClick()) {
                    DevModeData.devModeInVR = !DevModeData.devModeInVR;
                    player.sendMessage(
                            DevModeData.devModeInVR ?
                                    new TranslatableComponent("message.vrapi.dev.in_vr") :
                                    new TranslatableComponent("message.vrapi.dev.out_of_vr"),
                            player.getUUID()
                    );
                }

                if (DevModeData.devModeInVR) {
                    if (VRAPIModClient.POSITION_LEFT.isDown()) {
                        DevModeData.leftPos = player.getEyePosition().add(player.getLookAngle().scale(2));
                        DevModeData.leftRot = Vec3.atLowerCornerOf(player.getDirection().getNormal());
                    }

                    if (VRAPIModClient.POSITION_RIGHT.isDown()) {
                        DevModeData.rightPos = player.getEyePosition().add(player.getLookAngle().scale(2));
                        DevModeData.rightRot = Vec3.atLowerCornerOf(player.getDirection().getNormal());
                    }

                    if (VRAPIModClient.POSITION_HMD.isDown()) {
                        DevModeData.hmdPos = player.getEyePosition().add(player.getLookAngle().scale(2));
                        DevModeData.hmdRot = Vec3.atLowerCornerOf(player.getDirection().getNormal());
                    }

                    VRData hmdData = new VRData(DevModeData.hmdPos, DevModeData.hmdRot, 0, new Matrix4f());
                    VRData leftData = new VRData(DevModeData.leftPos, DevModeData.leftRot, 0, new Matrix4f());
                    VRData rightData = new VRData(DevModeData.rightPos, DevModeData.rightRot, 0, new Matrix4f());
                    vrPlayer = new VRPlayer(hmdData, rightData, leftData, hmdData, hmdData);
                    DevModeData.fakePlayer = vrPlayer;

                    // Core position particle display
                    player.level.addParticle(new DustParticleOptions(new Vector3f(1, 0, 0), 1),
                            DevModeData.leftPos.x(), DevModeData.leftPos.y(), DevModeData.leftPos.z(),
                            0.01, 0.01, 0.01);
                    player.level.addParticle(new DustParticleOptions(new Vector3f(0, 0, 1), 1),
                            DevModeData.rightPos.x(), DevModeData.rightPos.y(), DevModeData.rightPos.z(),
                            0.01, 0.01, 0.01);
                    player.level.addParticle(new DustParticleOptions(new Vector3f(1, 1, 1), 1),
                            DevModeData.hmdPos.x(), DevModeData.hmdPos.y(), DevModeData.hmdPos.z(),
                            0.01, 0.01, 0.01);

                    // Rotation particle display
                    player.level.addParticle(new DustParticleOptions(new Vector3f(0, 0, 0), 0.5f),
                            DevModeData.leftPos.x() + DevModeData.leftRot.x(),
                            DevModeData.leftPos.y() + DevModeData.leftRot.y(),
                            DevModeData.leftPos.z() + DevModeData.leftRot.z(),
                            0.01, 0.01, 0.01);
                    player.level.addParticle(new DustParticleOptions(new Vector3f(0, 0, 0), 0.5f),
                            DevModeData.rightPos.x() + DevModeData.rightRot.x(),
                            DevModeData.rightPos.y() + DevModeData.rightRot.y(),
                            DevModeData.rightPos.z() + DevModeData.rightRot.z(),
                            0.01, 0.01, 0.01);
                    player.level.addParticle(new DustParticleOptions(new Vector3f(0, 0, 0), 0.5f),
                            DevModeData.hmdPos.x() + DevModeData.hmdRot.x(),
                            DevModeData.hmdPos.y() + DevModeData.hmdRot.y(),
                            DevModeData.hmdPos.z() + DevModeData.hmdRot.z(),
                            0.01, 0.01, 0.01);
                }
            }

            if (vrPlayer != null && VRAPI.VRAPIInstance.playerInVR(player)) {
                boolean seated = (VRAPIMod.USE_DEV_FEATURES && DevModeData.devModeInVR) ? false : VRDataGrabber.isSeated();
                boolean leftHanded = (VRAPIMod.USE_DEV_FEATURES && DevModeData.devModeInVR) ? false : VRDataGrabber.isLeftHanded();
                Network.CHANNEL.sendToServer(new VRDataPacket(vrPlayer,
                        seated, leftHanded));
                sentVRDisabledPacket = false;
            } else if (!VRAPI.VRAPIInstance.playerInVR(player) && !sentVRDisabledPacket) {
                Network.CHANNEL.sendToServer(new LeftVRPacket());
                sentVRDisabledPacket = true;

            }
            if (ServerHasAPI.apiResponseCountdown > 0) {
                if (ServerHasAPI.apiResponseCountdown == 200) {
                    // Needed so we have backwards compat with older MC VR API versions
                    // TODO: Remove on 3.1.z or 4.y.z
                    Network.CHANNEL.sendToServer(new VersionSyncPacket(Network.PROTOCOL_VERSION));
                }
                if (--ServerHasAPI.apiResponseCountdown < 1) {
                    player.sendMessage(
                            new TranslatableComponent("message.vrapi.no_api_server"), player.getUUID());
                }
            }
        }
    }

    public static void onLogout(Player player) {
        didJoinPacket = false;
        sentVRDisabledPacket = false;
        ServerHasAPI.serverHasAPI = false;
        ServerHasAPI.apiResponseCountdown = 200;
    }
}
