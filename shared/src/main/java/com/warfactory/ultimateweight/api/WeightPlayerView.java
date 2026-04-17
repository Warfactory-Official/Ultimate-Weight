package com.warfactory.ultimateweight.api;

public interface WeightPlayerView {
    String playerId();

    Iterable<? extends WeightStackView> inventory();

    double carryCapacityKg();
}
