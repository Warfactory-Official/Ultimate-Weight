package com.warfactory.ultimateweight.api;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Registered {@link IGroupLimitModifier}s, summed onto group limits. Register once, at setup. */
public final class GroupLimitModifiers {
    private static final List<IGroupLimitModifier> MODIFIERS = new CopyOnWriteArrayList<IGroupLimitModifier>();

    private GroupLimitModifiers() {
    }

    public static void register(IGroupLimitModifier modifier) {
        if (modifier != null) {
            MODIFIERS.add(modifier);
        }
    }

    public static int bonus(Object player, String groupId) {
        int sum = 0;
        if (player != null) {
            for (IGroupLimitModifier modifier : MODIFIERS) {
                sum += modifier.bonus(player, groupId);
            }
        }
        return sum;
    }

    /** Tests. */
    public static void clear() {
        MODIFIERS.clear();
    }
}
