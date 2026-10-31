package com.warfactory.ultimateweight.api;

import java.util.Collections;

public interface WeightPlayerView {
    String playerId();

    Iterable<? extends WeightStackView> inventory();

    default Iterable<? extends WeightStackView> equipped() {
        return Collections.emptyList();
    }

    double carryCapacityKg();

    /** The loader's player object, as passed to {@link CarryModifiers}; null when the view has none. */
    default Object handle() {
        return null;
    }
}
