package net.blf02.vrapi;

import com.mojang.blaze3d.platform.InputConstants;
import net.blf02.vrapi.common.Plat;
import net.minecraft.client.KeyMapping;

public class VRAPIModClient {

    public static KeyMapping POSITION_LEFT = new KeyMapping(
            "key." + VRAPIMod.MOD_ID + ".position_left",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_LEFT,
            "category." + VRAPIMod.MOD_ID + ".dev_keys"
    );
    public static KeyMapping POSITION_RIGHT = new KeyMapping(
            "key." + VRAPIMod.MOD_ID + ".position_right",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_RIGHT,
            "category." + VRAPIMod.MOD_ID + ".dev_keys"
    );
    public static KeyMapping POSITION_HMD = new KeyMapping(
            "key." + VRAPIMod.MOD_ID + ".position_hmd",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_DOWN,
            "category." + VRAPIMod.MOD_ID + ".dev_keys"
    );

    public static KeyMapping TOGGLE_VR_DEV = new KeyMapping(
            "key." + VRAPIMod.MOD_ID + ".toggle_vr",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_UP,
            "category." + VRAPIMod.MOD_ID + ".dev_keys"
    );

    public static void initDebugKeys() {
        Plat.INSTANCE.registerKeyBinding(POSITION_LEFT);
        Plat.INSTANCE.registerKeyBinding(POSITION_RIGHT);
        Plat.INSTANCE.registerKeyBinding(POSITION_HMD);
        Plat.INSTANCE.registerKeyBinding(TOGGLE_VR_DEV);
    }

}
