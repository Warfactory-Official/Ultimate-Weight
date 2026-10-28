package com.warfactory.ultimateweight.api;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Registered {@link IStaminaModifier}s, summed each stamina update. Register once, at setup. */
public final class StaminaModifiers {
    private static final List<IStaminaModifier> MODIFIERS = new CopyOnWriteArrayList<IStaminaModifier>();

    private StaminaModifiers() {
    }

    public static void register(IStaminaModifier modifier) {
        if (modifier != null) {
            MODIFIERS.add(modifier);
        }
    }

    /** Sum of every modifier's {@link IStaminaModifier#recoveryPerTick} for {@code player}. */
    public static double recoveryPerTick(Object player) {
        double sum = 0.0D;
        for (IStaminaModifier modifier : MODIFIERS) {
            sum += modifier.recoveryPerTick(player);
        }
        return sum;
    }

    /** Tests. */
    public static void clear() {
        MODIFIERS.clear();
    }
}
