package com.warfactory.ultimateweight.v1211.client;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * Flashback replay state. Flashback hides the vanilla HUD in a replay or export by reporting spectator mode, which
 * mods drawing on the GUI event do not consult, so their overlays showed over the footage. Flashback is optional, so
 * this is reflective; absent or failing => not a replay.
 */
final class FlashbackReplay1211 {
    private static final MethodHandle IN_REPLAY = resolve();

    private FlashbackReplay1211() {
    }

    static boolean active() {
        if (IN_REPLAY == null) {
            return false;
        }
        try {
            return (boolean) IN_REPLAY.invokeExact();
        } catch (Throwable t) {
            return false;
        }
    }

    private static MethodHandle resolve() {
        try {
            return MethodHandles.publicLookup().findStatic(Class.forName("com.moulberry.flashback.Flashback"),
                "isInReplay", MethodType.methodType(boolean.class));
        } catch (ReflectiveOperationException | LinkageError e) {
            return null;
        }
    }
}
