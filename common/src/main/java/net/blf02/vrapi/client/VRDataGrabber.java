package net.blf02.vrapi.client;

import net.blf02.vrapi.VRAPIMod;
import net.blf02.vrapi.data.VRData;
import net.blf02.vrapi.data.VRPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.FloatBuffer;
import java.util.logging.Level;

public class VRDataGrabber {

    // vr field in net.minecraft.client.Minecraft
    public static Field Minecraft_vr;  // Type MCVR
    // Not guaranteed to be non-null. Must run initMinecraftVRInstanceIfNeeded before using
    public static Object Minecraft_vr_Instance; // Type MCVR (if null, needs to initialize at call time)
    public static Object vrHolder;

    // VRPlayer from Vivecraft
    protected static Method VRPlayer_GET; // Returns VRPlayer
    protected static Field VRPlayer_vrdata_world_post; // Type VRData
    protected static Field VRPlayer_vrdata_world_pre; // Type VRData
    protected static Field VRPlayer_vrdata_world_render; // Type VRData
    protected static Field VRPlayer_vrdata_room_pre; // Type VRData
    protected static Field VRPlayer_vrdata_room_post; // Type VRData

    // VRData from Vivecraft
    protected static Field VRData_hmd; // Type VRDevicePose
    protected static Field VRData_c0; // Type VRDevicePose
    protected static Field VRData_c1; // Type VRDevicePose
    protected static Field VRData_eye0; // Type VRDevicePose (corresponds to left eye)
    protected static Field VRData_eye1; // Type VRDevicePose (corresponds to right eye)

    // VRDevicePose from Vivecraft
    protected static Method VRDevicePose_getPosition; // Returns Vec3 (vanilla type)
    protected static Method VRDevicePose_getDirection; // Returns Vec3 (vanilla type)
    protected static Method VRDevicePose_getRoll; // Returns float
    protected static Method VRDevicePose_getMatrix; // Returns Matrix4f (Vivecraft type)

    // Matrix4f from Vivecraft
    protected static Method Matrix4f_toFloatBuffer; // Returns FloatBuffer (Java type)

    // MCVR from Vivecraft
    public static Method MCVR_triggerHapticPulse; // Returns void
    public static Method MCVR_isActive = null; // Returns boolean, hotswitch only

    // VRSettings
    public static Field Minecraft_VRSettings; // In Minecraft patch/ClientDataHolder
    public static Field VRSettings_seated; // boolean
    public static Field VRSettings_reverseHands; // boolean (represents left-handedness)

    public static void init() {
        if (!ReflectionConstants.clientHasVivecraft()) {
            VRAPIMod.LOGGER.log(Level.INFO, "Vivecraft was not detected! Not reflecting...");
        } else {
            try {
                VRPlayer_GET = getMethod(ReflectionConstants.VRPlayerRaw, "get");
                VRPlayer_vrdata_world_post = getField(ReflectionConstants.VRPlayerRaw, "vrdata_world_post");
                VRPlayer_vrdata_world_pre = getField(ReflectionConstants.VRPlayerRaw, "vrdata_world_pre");
                VRPlayer_vrdata_world_render = getField(ReflectionConstants.VRPlayerRaw, "vrdata_world_render");
                VRPlayer_vrdata_room_pre = getField(ReflectionConstants.VRPlayerRaw, "vrdata_room_pre");
                VRPlayer_vrdata_room_post = getField(ReflectionConstants.VRPlayerRaw, "vrdata_room_post");

                VRData_hmd = getField(ReflectionConstants.VRDataRaw, "hmd");
                VRData_c0 = getField(ReflectionConstants.VRDataRaw, "c0");
                VRData_c1 = getField(ReflectionConstants.VRDataRaw, "c1");
                VRData_eye0 = getField(ReflectionConstants.VRDataRaw, "eye0");
                VRData_eye1 = getField(ReflectionConstants.VRDataRaw, "eye1");

                VRDevicePose_getPosition = getMethod(ReflectionConstants.VRDevicePoseRaw, "getPosition");
                VRDevicePose_getDirection = getMethod(ReflectionConstants.VRDevicePoseRaw, "getDirection");
                VRDevicePose_getRoll = getMethod(ReflectionConstants.VRDevicePoseRaw, "getRoll");
                VRDevicePose_getMatrix = getMethod(ReflectionConstants.VRDevicePoseRaw, "getMatrix");

                if (!ReflectionConstants.useJOMLClasses()) {
                    Matrix4f_toFloatBuffer = getMethod(ReflectionConstants.Matrix4f, "toFloatBuffer");

                }

                try {
                    Minecraft_vr = getField(Minecraft.class, "vr");
                    Minecraft_VRSettings = getField(Minecraft.class, "vrSettings");
                    vrHolder = Minecraft.getInstance();
                } catch (RuntimeException e) {
                    Class<?> ClientDataHolder;
                    try {
                        ClientDataHolder = Class.forName(ReflectionConstants.VIVECRAFT_PACKAGE + ".ClientDataHolder");
                    } catch (ClassNotFoundException e2) {
                        ClientDataHolder = Class.forName(ReflectionConstants.VIVECRAFT_CLIENT_VR_PACKAGE + ".ClientDataHolderVR");
                    }
                    Method getCDHInstance = getMethod(ClientDataHolder, "getInstance");
                    Object cdhInstance = getCDHInstance.invoke(null);
                    Minecraft_vr = getField(ClientDataHolder, "vr");
                    Minecraft_VRSettings = getField(ClientDataHolder, "vrSettings");
                    vrHolder = cdhInstance;
                }

                // Not guaranteed to be non-null. Must run initMinecraftVRInstanceIfNeeded before using
                Minecraft_vr_Instance = Minecraft_vr.get(vrHolder);

                VRSettings_seated = getField(ReflectionConstants.VRSettings, "seated");
                VRSettings_reverseHands = getField(ReflectionConstants.VRSettings, "reverseHands");

                MCVR_triggerHapticPulse = getMethod(ReflectionConstants.MCVR, "triggerHapticPulse",
                        ReflectionConstants.ControllerType, float.class, float.class, float.class, float.class);
                try {
                    MCVR_isActive = getMethod(ReflectionConstants.MCVR, "isActive");
                } catch (RuntimeException ignored) {} // Method only exists in hotswitch, so can ignore otherwise
            } catch (IllegalAccessException | ClassNotFoundException | InvocationTargetException e) {
                VRAPIMod.LOGGER.log(Level.SEVERE, "Error: " + e.getMessage());
                throw new RuntimeException("Fatal error! Could not get! Please report this, along with the error message above!");
            }

        }
    }

    public static VRPlayer getVRPlayer(PlayerType type) {
        if (!inVR()) {
            return null;
        }

        try {
            Object vrPlayerRaw = VRPlayer_GET.invoke(null); // Get our "VRPlayer" from Vivecraft}
            // In case we switch out of VR after the !inVR() check
            if (vrPlayerRaw == null) {
                return null;
            }
            Object vrDataRaw;
            // Get the "VRData" from Vivecraft
            switch (type) {
                case WORLD_PRE -> vrDataRaw = VRPlayer_vrdata_world_pre.get(vrPlayerRaw);
                case WORLD_RENDER -> vrDataRaw = VRPlayer_vrdata_world_render.get(vrPlayerRaw);
                case ROOM_PRE -> vrDataRaw = VRPlayer_vrdata_room_pre.get(vrPlayerRaw);
                case ROOM_POST -> vrDataRaw = VRPlayer_vrdata_room_post.get(vrPlayerRaw);
                default -> vrDataRaw = VRPlayer_vrdata_world_post.get(vrPlayerRaw); // Covers POST and null
            }

            // In case we switch out of VR after the !inVR() check
            if (vrDataRaw == null) {
                return null;
            }


            Object hmdDevicePoseRaw = VRData_hmd.get(vrDataRaw); // Get the VRDevicePose for the HMD
            Object c0DevicePoseRaw = VRData_c0.get(vrDataRaw);
            Object c1DevicePoseRaw = VRData_c1.get(vrDataRaw);
            Object eye0DevicePoseRaw = VRData_eye0.get(vrDataRaw);
            Object eye1DevicePoseRaw = VRData_eye1.get(vrDataRaw);

            Vec3 hmdPosition = (Vec3) VRDevicePose_getPosition.invoke(hmdDevicePoseRaw); // Gets the position for the HMD in the world.
            Vec3 hmdLookVec = fromVivecraftVec3(VRDevicePose_getDirection.invoke(hmdDevicePoseRaw));
            float hmdRoll = (float) VRDevicePose_getRoll.invoke(hmdDevicePoseRaw);
            Matrix4f hmdRotMatr = fromVivecraftMatrix4f(VRDevicePose_getMatrix.invoke(hmdDevicePoseRaw));

            Vec3 c0Position = (Vec3) VRDevicePose_getPosition.invoke(c0DevicePoseRaw);
            Vec3 c0LookVec = fromVivecraftVec3(VRDevicePose_getDirection.invoke(c0DevicePoseRaw));
            float c0roll = (float) VRDevicePose_getRoll.invoke(c0DevicePoseRaw);
            Matrix4f c0RotMatr = fromVivecraftMatrix4f(VRDevicePose_getMatrix.invoke(c0DevicePoseRaw));

            Vec3 c1Position = (Vec3) VRDevicePose_getPosition.invoke(c1DevicePoseRaw);
            Vec3 c1LookVec = fromVivecraftVec3(VRDevicePose_getDirection.invoke(c1DevicePoseRaw));
            float c1roll = (float) VRDevicePose_getRoll.invoke(c1DevicePoseRaw);
            Matrix4f c1RotMatr = fromVivecraftMatrix4f(VRDevicePose_getMatrix.invoke(c1DevicePoseRaw));

            Vec3 eye0Position = (Vec3) VRDevicePose_getPosition.invoke(eye0DevicePoseRaw);
            Vec3 eye0LookVec = fromVivecraftVec3(VRDevicePose_getDirection.invoke(eye0DevicePoseRaw));
            float eye0roll = (float) VRDevicePose_getRoll.invoke(eye0DevicePoseRaw);
            Matrix4f eye0RotMatr = fromVivecraftMatrix4f(VRDevicePose_getMatrix.invoke(eye0DevicePoseRaw));

            Vec3 eye1Position = (Vec3) VRDevicePose_getPosition.invoke(eye1DevicePoseRaw);
            Vec3 eye1LookVec = fromVivecraftVec3(VRDevicePose_getDirection.invoke(eye1DevicePoseRaw));
            float eye1roll = (float) VRDevicePose_getRoll.invoke(eye1DevicePoseRaw);
            Matrix4f eye1RotMatr = fromVivecraftMatrix4f(VRDevicePose_getMatrix.invoke(eye1DevicePoseRaw));

            return new VRPlayer(new VRData(hmdPosition, hmdLookVec, hmdRoll, hmdRotMatr),
                    new VRData(c0Position, c0LookVec, c0roll, c0RotMatr), new VRData(c1Position, c1LookVec, c1roll, c1RotMatr),
                    new VRData(eye0Position, eye0LookVec, eye0roll, eye0RotMatr), new VRData(eye1Position, eye1LookVec, eye1roll, eye1RotMatr));
        }  catch (InvocationTargetException | IllegalAccessException e) {
            // We shouldn't error here, as we know these fields and methods exist due to getField() and getMethod()
            // combined with having access to Vivecraft's codebase on GitHub
            throw new RuntimeException("Could not obtain data from Vivecraft! Something has gone horribly wrong.");
        } catch (NullPointerException ignored) {
            // In case something returns null after the inVR() check due to hotswap.
            return null;
        }
    }

    private static Vec3 fromVivecraftVec3(Object vecIn) {
        if (!ReflectionConstants.useJOMLClasses()) {
            return (Vec3) vecIn;
        }
        Vector3f vec = (Vector3f) vecIn;
        return new Vec3(vec.x, vec.y, vec.z);
    }

    public static Matrix4f fromVivecraftMatrix4f(Object matrixIn) throws InvocationTargetException, IllegalAccessException {
        if (ReflectionConstants.useJOMLClasses()) {
            return (Matrix4f) matrixIn;
        }
        FloatBuffer buffer = (FloatBuffer) Matrix4f_toFloatBuffer.invoke(matrixIn);
        Matrix4f matr = new Matrix4f(
                buffer.get(), buffer.get(), buffer.get(), buffer.get(),
                buffer.get(), buffer.get(), buffer.get(), buffer.get(),
                buffer.get(), buffer.get(), buffer.get(), buffer.get(),
                buffer.get(), buffer.get(), buffer.get(), buffer.get());
        return matr;
    }

    public static boolean inVR() {
        if (!ReflectionConstants.clientHasVivecraft()) {
            return false;
        }
        try {
            Object vrPlayerRaw = VRPlayer_GET.invoke(null); // Try to get vrPlayer from Vivecraft
            // Since this function may exist in Vivecraft Mixin, we need to check if it's nonnull to see if we're
            // in VR or not. This is also cleared when leaving VR for hotswitch, so checking this is fine for that.
            return vrPlayerRaw != null && hotswitchVRActive();
        } catch (InvocationTargetException | IllegalAccessException e) {
            return false; // If we failed to grab the above, we definitely are NOT in VR.
        }
    }

    public static boolean isSeated() {
        if (!ReflectionConstants.clientHasVivecraft()) {
            throw new IllegalArgumentException("Cannot retrieve seated status of player outside VR!");
        }
        try {
            Object VRSettingsInstance = Minecraft_VRSettings.get(vrHolder);
            return (boolean) VRSettings_seated.get(VRSettingsInstance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not get seated status of local player!");
        }
    }

    public static boolean isLeftHanded() {
        if (!ReflectionConstants.clientHasVivecraft()) {
            throw new IllegalArgumentException("Cannot retrieve hand status of player outside VR!");
        }
        try {
            Object VRSettingsInstance = Minecraft_VRSettings.get(vrHolder);
            return (boolean) VRSettings_reverseHands.get(VRSettingsInstance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not get handedness of local player!");
        }
    }

    public static void isSelf(Player player) {
        if (player != Minecraft.getInstance().player) {
            throw new IllegalArgumentException("Client side can only retrieve player data about themself!");
        }
    }

    public static void initMinecraftVRInstanceIfNeeded() {
        if (Minecraft_vr_Instance == null) {
            try {
                Minecraft_vr_Instance = Minecraft_vr.get(vrHolder);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

        }
    }

    private static boolean hotswitchVRActive() {
        if (MCVR_isActive != null) {
            try {
                initMinecraftVRInstanceIfNeeded();
                return (boolean) MCVR_isActive.invoke(Minecraft_vr_Instance);
            } catch (IllegalAccessException | InvocationTargetException e) {
                return false;
            }
        }
        return true; // Non-hotswitch ignores this
    }

    public static Field getField(Class<?> clazz, String field) {
        try {
            return clazz.getField(field);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Could not get field " + field + " from " + clazz);
        }
    }

    public static Method getMethod(Class<?> clazz, String method, Class<?>... parameterTypes) {
        try {
            return clazz.getMethod(method, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Could not load method " + method + " from " + clazz);
        }
    }

    public enum PlayerType {
        WORLD_POST, WORLD_PRE, WORLD_RENDER, ROOM_PRE, ROOM_POST;
    }

}
