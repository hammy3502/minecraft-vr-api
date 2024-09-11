package net.blf02.vrapi.common.network.packets;

import net.blf02.vrapi.common.VRAPI;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class VRRumblePacket {

    protected final int controllerNum;
    protected final float duration;
    protected final float frequency;
    protected final float amplitude;
    protected final float delay;

    public VRRumblePacket(int controllerNum, float durationSeconds, float frequency, float amplitude, float delaySeconds) {
        this.controllerNum = controllerNum;
        this.duration = durationSeconds;
        this.frequency = frequency;
        this.amplitude = amplitude;
        this.delay = delaySeconds;
    }

    public static void encode(VRRumblePacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.controllerNum).writeFloat(packet.duration)
                .writeFloat(packet.frequency).writeFloat(packet.amplitude)
                .writeFloat(packet.delay);
    }

    public static VRRumblePacket decode(FriendlyByteBuf buffer) {
        return new VRRumblePacket(buffer.readInt(), buffer.readFloat(),
                buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
    }

    public static void handle(VRRumblePacket message, ServerPlayer player) {
        if (player == null) {  // From server to client
            VRAPI.VRAPIInstance.triggerHapticPulse(message.controllerNum, message.duration, message.frequency,
                    message.amplitude, message.delay, null);
        }
    }
}
