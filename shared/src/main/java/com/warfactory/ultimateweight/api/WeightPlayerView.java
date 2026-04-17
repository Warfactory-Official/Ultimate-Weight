package com.warfactory.ultimateweight.api;

import java.util.Collections;

public interface WeightPlayerView {
    String playerId();

    Iterable<? extends WeightStackView> inventory();

    default Iterable<? extends WeightStackView> equipped() {
        return Collections.emptyList();
    }

    double carryCapacityKg();
}
