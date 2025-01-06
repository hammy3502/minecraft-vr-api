package net.blf02.vrapi.client;

public class ReflectionConstants {
    // String constants
    public static final String VIVECRAFT_PACKAGE = "org.vivecraft";

    // String constants for Optifine/pre-hotswitch Vivecraft
    public static final String VIVECRAFT_MATH_PACKAGE = VIVECRAFT_PACKAGE + ".utils.math";
    public static final String VIVECRAFT_PROVIDER_PACKAGE = VIVECRAFT_PACKAGE + ".provider";
    public static final String VIVECRAFT_GAMEPLAY_PACKAGE = VIVECRAFT_PACKAGE + ".gameplay";

    // String constants for hotswitch Vivecraft
    public static final String VIVECRAFT_CLIENT_VR_PACKAGE = VIVECRAFT_PACKAGE + ".client_vr";


    // Reflected class references
    public static Class<?> VRPlayerRaw = null;
    public static Class<?> VRDataRaw = null;
    public static Class<?> VRDevicePoseRaw = null;
    public static Class<?> MCVR = null; // Note that this class is abstract
    public static Class<?> Matrix4f = null;
    public static Class<?> ControllerType = null; // Enum
    public static Class<?> VRSettings = null;
    public static Object[] ControllerType_ENUMS = null;
    // Whether Vivecraft is successfully loaded by the library.
    private static boolean hasVivecraft = false;
    // Whether we're using Vivecraft 1.2.0 or later which uses JOML matrices
    private static boolean useJOMLClasses = false;

    public static void init() {
        try {
            VRPlayerRaw = Class.forName(VIVECRAFT_GAMEPLAY_PACKAGE + ".VRPlayer");
            VRDataRaw = Class.forName(VIVECRAFT_PACKAGE + ".api.VRData");
            VRDevicePoseRaw = VRDataRaw.getDeclaredClasses()[0];
            MCVR = Class.forName(VIVECRAFT_PROVIDER_PACKAGE + ".MCVR");
            ControllerType = Class.forName(VIVECRAFT_PROVIDER_PACKAGE + ".ControllerType");
            ControllerType_ENUMS = ControllerType.getEnumConstants();
            Matrix4f = Class.forName(VIVECRAFT_MATH_PACKAGE + ".Matrix4f");
            VRSettings = Class.forName(VIVECRAFT_PACKAGE + ".settings.VRSettings");
            hasVivecraft = true;
        } catch (ClassNotFoundException ignored) {
            // Hotswitch Vivecraft locations
            try {
                VRPlayerRaw = Class.forName(VIVECRAFT_CLIENT_VR_PACKAGE + ".gameplay.VRPlayer");
                VRDataRaw = Class.forName(VIVECRAFT_CLIENT_VR_PACKAGE + ".VRData");
                VRDevicePoseRaw = VRDataRaw.getDeclaredClasses()[0];
                MCVR = Class.forName(VIVECRAFT_CLIENT_VR_PACKAGE + ".provider.MCVR");
                ControllerType = Class.forName(VIVECRAFT_CLIENT_VR_PACKAGE + ".provider.ControllerType");
                ControllerType_ENUMS = ControllerType.getEnumConstants();
                VRSettings = Class.forName(VIVECRAFT_CLIENT_VR_PACKAGE + ".settings.VRSettings");
                try {
                    Matrix4f = Class.forName(VIVECRAFT_PACKAGE + ".common.utils.math.Matrix4f");
                } catch (ClassNotFoundException ignored2) {
                    useJOMLClasses = true;
                }
                hasVivecraft = true;
            } catch (ClassNotFoundException ignored2) {}
        }
    }

    public static boolean clientHasVivecraft() {
        return hasVivecraft;
    }

    public static boolean useJOMLClasses() {
        return useJOMLClasses;
    }
}
