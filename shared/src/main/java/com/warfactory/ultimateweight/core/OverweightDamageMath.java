package com.warfactory.ultimateweight.core;

import com.warfactory.ultimateweight.config.WeightConfig;


public final class OverweightDamageMath {
    private static final double EPSILON = 0.000001D;

    private OverweightDamageMath() {
    }


    public static boolean isOverweight(
        WeightConfig.OverweightDamage overweightDamage,
        double totalWeightKg,
        double carryCapacityKg
    ) {
        if (overweightDamage == null || !overweightDamage.enabled() || carryCapacityKg <= EPSILON) {
            return false;
        }
        return totalWeightKg / carryCapacityKg > overweightDamage.startLoadPercent() + EPSILON;
    }


    public static boolean isDue(
        WeightConfig.OverweightDamage overweightDamage,
        long currentTick,
        long lastDamageTick
    ) {
        if (overweightDamage == null || !overweightDamage.enabled()) {
            return false;
        }
        return currentTick - lastDamageTick >= overweightDamage.intervalTicks();
    }


    public static double resolveDamage(
        WeightConfig.OverweightDamage overweightDamage,
        double totalWeightKg,
        double carryCapacityKg,
        boolean hardLocked
    ) {
        if (!isOverweight(overweightDamage, totalWeightKg, carryCapacityKg)) {
            return 0.0D;
        }

        double loadPercent = totalWeightKg / carryCapacityKg;
        double damage = overweightDamage.damagePerInterval()
            + ((loadPercent - overweightDamage.startLoadPercent()) * overweightDamage.extraDamagePerLoadPercent());
        if (hardLocked) {
            damage += overweightDamage.hardLockDamageBonus();
        }

        damage = Math.min(damage, overweightDamage.maxDamagePerInterval());
        return damage <= EPSILON ? 0.0D : damage;
    }

    public static double clampToMinHealth(
        WeightConfig.OverweightDamage overweightDamage,
        double damage,
        double currentHealth
    ) {
        if (damage <= EPSILON) {
            return 0.0D;
        }
        if (overweightDamage == null || overweightDamage.minHealth() <= EPSILON) {
            return damage;
        }

        double allowed = currentHealth - overweightDamage.minHealth();
        if (allowed <= EPSILON) {
            return 0.0D;
        }
        return Math.min(damage, allowed);
    }
}
