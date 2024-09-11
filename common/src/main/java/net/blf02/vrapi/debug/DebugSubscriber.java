package net.blf02.vrapi.debug;

import net.blf02.vrapi.api.data.IVRData;
import net.blf02.vrapi.common.VRAPI;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class DebugSubscriber {

    public static void onPlayerTick(Player player) {
        if (VRAPI.VRAPIInstance.playerInVR(player) && player instanceof ServerPlayer sp) {
            IVRData data = VRAPI.VRAPIInstance.getVRPlayer(player).getHMD();
            Vec3 pos = data.position().add(data.getLookAngle());
            sp.getLevel().sendParticles(ParticleTypes.SNOWFLAKE,
                    pos.x, pos.y, pos.z, 1,
                    0.01, 0.01, 0.01, 0.0001);
            data = VRAPI.VRAPIInstance.getVRPlayer(player).getController1();
            pos = data.position().add(data.getLookAngle());
            sp.getLevel().sendParticles(ParticleTypes.ANGRY_VILLAGER,
                    pos.x, pos.y, pos.z, 1,
                    0.01, 0.01, 0.01, 0.0001);
            data = VRAPI.VRAPIInstance.getVRPlayer(player).getController0();
            pos = data.position().add(data.getLookAngle());
            sp.getLevel().sendParticles(ParticleTypes.FALLING_WATER,
                    pos.x, pos.y, pos.z, 1,
                    0.01, 0.01, 0.01, 0.0001);
            VRAPI.VRAPIInstance.triggerHapticPulse(0, 0.025f, sp);
        }
    }
}
