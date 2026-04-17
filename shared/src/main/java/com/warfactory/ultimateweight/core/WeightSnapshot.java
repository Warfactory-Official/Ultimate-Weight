package com.warfactory.ultimateweight.core;

public final class WeightSnapshot {
    private final double totalWeightKg;
    private final double carryCapacityKg;
    private final ThresholdEffect thresholdEffect;
    private final boolean hardLocked;

    public WeightSnapshot(
        double totalWeightKg,
        double carryCapacityKg,
        ThresholdEffect thresholdEffect,
        boolean hardLocked
    ) {
        this.totalWeightKg = totalWeightKg;
        this.carryCapacityKg = carryCapacityKg;
        this.thresholdEffect = thresholdEffect;
        this.hardLocked = hardLocked;
    }

    public double totalWeightKg() {
        return totalWeightKg;
    }

    public double carryCapacityKg() {
        return carryCapacityKg;
    }

    public ThresholdEffect thresholdEffect() {
        return thresholdEffect;
    }

    public boolean hardLocked() {
        return hardLocked;
    }
}
