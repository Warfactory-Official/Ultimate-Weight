package com.warfactory.ultimateweight.core;

import com.warfactory.ultimateweight.api.WeightStackView;

public final class WeightInventoryCalculator {
    private final WeightResolver resolver;

    public WeightInventoryCalculator(WeightResolver resolver) {
        this.resolver = resolver;
    }

    public double calculateTotalWeightKg(Iterable<? extends WeightStackView> inventory) {
        double total = 0.0D;
        if (inventory == null) {
            return total;
        }

        for (WeightStackView stack : inventory) {
            if (stack != null) {
                total += resolver.resolve(stack).stackWeightKg();
            }
        }
        return total;
    }
}
