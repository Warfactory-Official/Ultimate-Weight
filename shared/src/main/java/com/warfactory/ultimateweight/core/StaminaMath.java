package com.warfactory.ultimateweight.core;

import com.warfactory.ultimateweight.config.WeightConfig;

public final class StaminaMath {
    private static final double EPSILON = 0.000001D;

    private StaminaMath() {
    }

    public static boolean isEnabled(WeightConfig.Stamina stamina) {
        return stamina != null
            && stamina.totalStamina() > EPSILON
            && (stamina.drainWhileRunning() || stamina.drainOnJump());
    }

    public static boolean isEnabled(WeightConfig.Stamina stamina, double maxStamina) {
        return stamina != null
            && maxStamina > EPSILON
            && (stamina.drainWhileRunning() || stamina.drainOnJump());
    }

    public static double clamp(double value, double maxValue) {
        if (maxValue <= 0.0D) {
            return 0.0D;
        }
        if (value <= 0.0D) {
            return 0.0D;
        }
        return Math.min(value, maxValue);
    }

    public static double resolveUseMultiplier(
        WeightConfig.Stamina stamina,
        double totalWeightKg,
        double carryCapacityKg
    ) {
        if (stamina == null || stamina.penalties().isEmpty() || carryCapacityKg <= EPSILON) {
            return 1.0D;
        }

        double loadPercent = totalWeightKg / carryCapacityKg;
        double multiplier = 1.0D;
        for (int index = 0; index < stamina.penalties().size(); index++) {
            WeightConfig.UsagePenaltyRule penalty = stamina.penalties().get(index);
            if (loadPercent >= penalty.percent()) {
                multiplier = penalty.useMultiplier();
            }
        }
        return multiplier;
    }

    public static double exhaustionThreshold(WeightConfig.Stamina stamina, double maxStamina) {
        if (stamina == null || maxStamina <= 0.0D) {
            return 0.0D;
        }
        return clamp(stamina.exhaustionThreshold(), maxStamina);
    }

    public static double recoveryThreshold(WeightConfig.Stamina stamina, double maxStamina) {
        if (stamina == null || maxStamina <= 0.0D) {
            return 0.0D;
        }
        return Math.max(exhaustionThreshold(stamina, maxStamina), maxStamina * stamina.recoveryPercent());
    }

    public static boolean resolveExhausted(
        WeightConfig.Stamina stamina,
        double currentStamina,
        double maxStamina,
        boolean staminaEnabled,
        boolean currentlyExhausted
    ) {
        if (!staminaEnabled || stamina == null || maxStamina <= EPSILON) {
            return false;
        }

        double clampedStamina = clamp(currentStamina, maxStamina);
        if (currentlyExhausted) {
            double recoveryThreshold = recoveryThreshold(stamina, maxStamina);
            if (recoveryThreshold <= EPSILON) {
                return clampedStamina <= EPSILON;
            }
            return clampedStamina + EPSILON < recoveryThreshold;
        }
        return clampedStamina <= exhaustionThreshold(stamina, maxStamina) + EPSILON;
    }
}
