package com.warfactory.ultimateweight.api;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Registered {@link ICarryModifier}s, summed into carry capacity. Register once, at setup. */
public final class CarryModifiers {
    private static final List<ICarryModifier> MODIFIERS = new CopyOnWriteArrayList<ICarryModifier>();

    private CarryModifiers() {
    }

    public static void register(ICarryModifier modifier) {
        if (modifier != null) {
            MODIFIERS.add(modifier);
        }
    }

    /** Sum of every modifier's {@link ICarryModifier#bonusKg} for {@code player}. */
    public static double bonusKg(Object player) {
        double sum = 0.0D;
        for (ICarryModifier modifier : MODIFIERS) {
            sum += modifier.bonusKg(player);
        }
        return sum;
    }

    /** Tests. */
    public static void clear() {
        MODIFIERS.clear();
    }
}
